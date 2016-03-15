/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.jms;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.ResultStreamException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream.QueryBuilder;
import org.jboss.aerogear.unifiedpush.message.util.JmsClient;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;

@Stateless
public class MocksForTokenLoaderTransactionFailForGCM  {

    @Resource(mappedName = "java:/queue/TestTokenLoaderTransactionFailForGCM")
    private Queue allTokens;

    @Inject
    private JmsClient jmsClient;

    @Produces
    public ClientInstallationService getClientInstallationService() {
        return new ClientInstallationService() {

            @Override
            public void updateInstallation(Installation toUpdate, Installation postedInstallation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void updateInstallation(Installation installation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void removeInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens) {
                // TODO Auto-generated method stub

            }

            @Override
            public void removeInstallations(List<Installation> installations) {
                // TODO Auto-generated method stub

            }

            @Override
            public void removeInstallationForVariantByDeviceToken(String variantID, String deviceToken) {
                // TODO Auto-generated method stub

            }

            @Override
            public void removeInstallation(Installation installation) {
                // TODO Auto-generated method stub

            }

            @Override
            public Installation findInstallationForVariantByDeviceToken(String variantID, String deviceToken) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Installation findById(String primaryKey) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public QueryBuilder<String> findAllDeviceTokenForVariantIDByCriteria(final String variantID, List<String> categories,
                    List<String> aliases, List<String> deviceTypes, int maxResults, String lastTokenFromPreviousBatch) {

                return new QueryBuilder<String>() {

                    @Override
                    public QueryBuilder<String> fetchSize(int fetchSize) {
                        return this;
                    }

                    @Override
                    public ResultsStream<String> executeQuery() {
                        return new ResultsStream<String>() {

                            private int counter = 0;

                            @Override
                            public String get() throws ResultStreamException {
                                if (counter >= 0) {
                                    return "eHlfnI0__dI:APA91bEhtHefML2lr_sBQ-bdXIyEn5owzkZg_p_y7SRyNKRMZ3XuzZhBpTOYIh46tqRYQIc-7RTADk4nM5H-ONgPDWHodQDS24O5GuKP8EZEKwNh4Zxdv1wkZJh7cU2PoLz9gn4Nxqz-" + counter;
                                }
                                return null;
                            }

                            @Override
                            public boolean next() throws ResultStreamException {
                                if (--counter >= 0) {
                                    return true;
                                }
                                if (null != jmsClient.receive().inTransaction().noWait().withSelector("id = '%s'", TestTokenLoaderTransactionFailForGCM.messageId).from(allTokens)) {
                                    counter = 1000;
                                    return next();
                                }
                                return false;
                            }
                        };
                    }
                };
            }

            @Override
            public void addInstallations(Variant variant, List<Installation> installations) {
                // TODO Auto-generated method stub

            }

            @Override
            public void addInstallation(Variant variant, Installation installation) {
                // TODO Auto-generated method stub

            }
        };
    }
}
