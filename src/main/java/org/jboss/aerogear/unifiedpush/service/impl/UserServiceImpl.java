/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.aerogear.unifiedpush.service.impl;

import org.jboss.aerogear.security.auth.AuthenticationManager;
import org.jboss.aerogear.security.auth.LoggedUser;
import org.jboss.aerogear.security.authz.IdentityManagement;
import org.jboss.aerogear.security.exception.AeroGearSecurityException;
import org.jboss.aerogear.security.exception.HttpStatus;
import org.jboss.aerogear.security.token.service.TokenService;
import org.jboss.aerogear.unifiedpush.model.token.Credential;
import org.jboss.aerogear.unifiedpush.service.UserService;
import org.jboss.aerogear.unifiedpush.users.Developer;
import org.jboss.aerogear.unifiedpush.users.UserRoles;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;


public class UserServiceImpl implements UserService {

    @Inject
    @LoggedUser
    private Instance<String> loginName;

    @Inject
    private PartitionManager partitionManager;

    @Inject
    private TokenService tokenService;

    @Inject
    private AuthenticationManager<Agent> authenticationManager;

    @Inject
    private IdentityManagement<User> configuration;

    private IdentityManager identityManager;
    private RelationshipManager relationshipManager;

    @PostConstruct
    private void initManagers(){
        this.identityManager = partitionManager.createIdentityManager();
        this.relationshipManager = partitionManager.createRelationshipManager();
    }

    @Override
    public Developer login(Developer developer) {
        authenticationManager.login(developer, developer.getPassword());
        //let's retrieve the user
        User simpleUser = findUserByLoginName(developer.getLoginName());
        //let's set his role

        Developer authenticatedDeveloper = new Developer();
        authenticatedDeveloper.setLoginName(simpleUser.getLoginName());
        authenticatedDeveloper.setRole(this.getRole(simpleUser));
        return authenticatedDeveloper;
    }

    @Override
    public User findUserByLoginName(String loginName) {
        return configuration.findByUsername(loginName);
    }

    @Override
    public void logout() {
        authenticationManager.logout();
    }

    @Override
    public void updateUserPasswordAndRole(Developer developer) {
        User simpleUser = findUserByLoginName(developer.getLoginName());
        configuration.reset(simpleUser, developer.getPassword(), developer.getNewPassword());
    }

    @Override
    public Developer enroll(Developer developer) {
        User user = new User(developer.getLoginName());
        //user.setEnabled(false);
        identityManager.add(user);
        Role developerRole = BasicModel.getRole(identityManager, developer.getRole());

        grantRoles(user, developerRole);
        List<User> list = identityManager.createIdentityQuery(User.class)
                .setParameter(User.LOGIN_NAME, user.getLoginName()).getResultList();
        user = list.get(0);
        developer.setId(user.getId());
        developer.setRegistrationLink(tokenService.generate(user.getLoginName()));
        return developer;
    }

    @Override
    public Developer reset(Developer developer) {
        developer.setRegistrationLink(tokenService.generate(developer.getLoginName()));
        return developer;
    }


    @Override
    public Developer findById(String id) {
        //TODO that has to be refactored once Developer is correctly mapped into PicketLink model
        User user = identityManager.lookupIdentityById(User.class, id);
        Developer developer = new Developer();
        developer.setId(user.getId());
        developer.setLoginName(user.getLoginName());
        developer.setRole(this.getRole(user));
        return developer;
    }

    @Override
    public List<Developer> listAll() {
        IdentityQuery<User> identityQuery = identityManager.createIdentityQuery(User.class);
        List<Developer> developers = new ArrayList<Developer>();
        List<User> users = identityQuery.getResultList();
        for(User user:users) {
            Developer developer = new Developer();
            developer.setId(user.getId());
            developer.setLoginName(user.getLoginName());
            developer.setRole(this.getRole(user));
            //developer.setEnabled(user.isEnabled());
            if(developer.getLoginName().equals(loginName.get())){
                developer.setLoggedIn(true);
            }
            developers.add(developer);
        }
        return developers;
    }

    @Override
    public void deleteById(String id) {
        User simpleUser = identityManager.lookupIdentityById(User.class, id);
        identityManager.remove(simpleUser);
    }

    @Override
    public void confirm(Credential credential) {
        if (tokenService.isValid(credential.getToken())) {
            this.identityManager = partitionManager.createIdentityManager();
            tokenService.destroy(credential.getToken());
            User simpleUser = configuration.findByUsername(credential.getEmail());
            Password password = new Password(credential.getPassword().toCharArray());
            identityManager.updateCredential(simpleUser, password);
            simpleUser.setEnabled(true);
            identityManager.update(simpleUser);


        } else {
            throw new AeroGearSecurityException(HttpStatus.PASSWORD_RESET_FAILED);
        }
    }

    private String getRole(User user) {
        //not really liking this but atm seems the only way to retrieve the roles of an user
        Role developer  =   BasicModel.getRole(identityManager, UserRoles.DEVELOPER);
        Role admin  =   BasicModel.getRole(identityManager, UserRoles.ADMIN );
        if(BasicModel.hasRole(relationshipManager,user,developer)){
            return UserRoles.DEVELOPER;
        }
        else {
            return UserRoles.ADMIN;
        }
    }

    private void grantRoles(User user, Role role) {
        BasicModel.grantRole(relationshipManager, user, role);
    }
}
