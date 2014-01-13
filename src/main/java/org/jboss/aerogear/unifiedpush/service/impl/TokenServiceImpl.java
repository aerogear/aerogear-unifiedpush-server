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

import org.jboss.aerogear.crypto.Hmac;
import org.jboss.aerogear.security.authz.IdentityManagement;
import org.jboss.aerogear.security.token.ExpirationTime;
import org.jboss.aerogear.security.token.service.TokenService;
import org.jboss.aerogear.security.web.filter.PasswordHandlerConfig;
import org.jboss.aerogear.unifiedpush.model.token.Token;
import org.jboss.aerogear.unifiedpush.service.UserService;
import org.picketlink.idm.model.basic.User;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

/**
 * The purpose of this class is to generate, store and validate tokens.
 * Tokens are generated using a keyed-hash message authentication code (HMAC)
 */
@Stateless
public class TokenServiceImpl implements TokenService {

    private static final Logger LOGGER = Logger.getLogger(TokenServiceImpl.class.getSimpleName());

    @Inject
    private EntityManager em;

    @Inject
    private ExpirationTime expirationTime;

    @Inject
    private IdentityManagement<User> configuration;

    @Override
    public void destroy(String id) {
        try {
            Token token = em.find(Token.class, id);
            em.remove(token);
            em.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * First we retrieve the persisted Token, then we check for its validity by looking at the expiration time.
     *
     * @param id
     * @return if the token is valid or not
     */
    @Override
    public boolean isValid(String id) {

        Token token = null;
        try {
            token = em.createQuery("SELECT t FROM Token t WHERE t.id = :id", Token.class)
                    .setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            //Do nothing atm because we don't want to give any clue to an attacker
        }

        return (token != null && !expirationTime.isExpired(token.getExpiration()));
    }

    /**
     * Here the token is generate after checking that the user exist.
     *
     * @param loginName of the user
     * @return the token appended to the "reset" URL.
     */
    @Override
    public String generate(String loginName) {
        User user =  configuration.findByUsername(loginName);
        Token token;
        //make sure this user exist
        if(user != null) {
         String secret = PasswordHandlerConfig.getSecret();
            try {
                Hmac hmac = new Hmac(secret);
                token = save(hmac.digest());
                return PasswordHandlerConfig.uri(token.getId());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * Persist the token
     *
     * @param id representing the token
     * @return
     */
    private Token save(String id) {

        Token token = null;
        try {
            token = new Token(id);
            em.merge(token);
            em.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return token;
    }

}
