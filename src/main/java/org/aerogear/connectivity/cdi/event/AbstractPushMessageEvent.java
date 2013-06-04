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

package org.aerogear.connectivity.cdi.event;

import java.util.LinkedHashMap;

import org.aerogear.connectivity.model.PushApplication;

public abstract class AbstractPushMessageEvent {
    
    private PushApplication pushApplication;
    private LinkedHashMap<String, ? extends Object> message;

    public AbstractPushMessageEvent(PushApplication pushApplication,
            LinkedHashMap<String, ? extends Object> message) {

        this.pushApplication = pushApplication;
        this.message = message;
    }
    public PushApplication getPushApplication() {
        return pushApplication;
    }
    public void setPushApplication(PushApplication pushApplication) {
        this.pushApplication = pushApplication;
    }
    public LinkedHashMap<String, ? extends Object> getMessage() {
        return message;
    }
    public void setMessage(LinkedHashMap<String, ? extends Object> message) {
        this.message = message;
    }

    
    
}
