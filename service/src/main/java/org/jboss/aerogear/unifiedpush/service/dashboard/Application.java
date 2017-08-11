/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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
package org.jboss.aerogear.unifiedpush.service.dashboard;

import java.util.Date;

/**
 * The purpose of this class is to act as a "value object" for the Dashboard services.
 * It contains information about a particular FlatPushMessageInformation instance.
 */
public class Application {

    private String name;
    private String id;
    private Date submittedDate;

    public Application(String name, String id, Date submittedDate) {
        this.name = name;
        this.id = id;
        this.submittedDate = submittedDate;
    }

    /**
     * The name of the Push Application
     *
     * @return the name of the Push Application
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the Push Application
     *
     * @param name of the Push Application
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The id of the Push Application
     *
     * @return the id of the Push Application
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id of the Push Application
     *
     * @param id of the Push Application
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * The timestamp of when the Push Message was submitted to the Push Networks
     *
     * @return the timestamp of when the Push Message was submitted to the Push Networks
     */
    public Date getSubmittedDate() {
        return submittedDate;
    }

    /**
     * Set the timestamp of when the Push Message was submitted to the Push Networks
     *
     * @param submittedDate, timestamp of when the Push Message was submitted to the Push Networks
     */
    public void setSubmittedDate(Date submittedDate) {
        this.submittedDate = submittedDate;
    }
}
