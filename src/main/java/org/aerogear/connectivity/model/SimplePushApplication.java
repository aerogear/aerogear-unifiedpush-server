/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aerogear.connectivity.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("'simplePush'")
public class SimplePushApplication extends AbstractMobileApplication {

    private static final long serialVersionUID = 9046963507735955449L;

    @Column
    private String pushNetworkURL;

    public String getPushNetworkURL() {
        return pushNetworkURL;
    }

    public void setPushNetworkURL(String pushNetworkURL) {
        this.pushNetworkURL = pushNetworkURL;
    }
}
