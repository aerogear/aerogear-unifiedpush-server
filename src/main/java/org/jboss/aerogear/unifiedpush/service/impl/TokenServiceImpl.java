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
import org.jboss.aerogear.security.token.ExpirationTime;
import org.jboss.aerogear.security.token.service.TokenService;
import org.jboss.aerogear.security.token.util.Configuration;

import org.jboss.aerogear.unifiedpush.model.token.Token;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

@Stateless
public class TokenServiceImpl implements TokenService {

    private static final Logger LOGGER = Logger.getLogger(TokenServiceImpl.class.getSimpleName());

    @Inject
    private EntityManager em;

    @Inject
    private ExpirationTime expirationTime;

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

    //Send to some place the url for password reset
    @Override
    public String generate() {

        Token token;

        //Here of course we need to validate the e-mail against the database or PicketLink
        //if (FakeService.userExists(email)) {

            String secret = Configuration.getSecret();
            try {
                Hmac hmac = new Hmac(secret);
                token = save(hmac.digest());
                return Configuration.uri(token.getId());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        //}
        return null;
    }

    //Private method because it' up to the implementer
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
