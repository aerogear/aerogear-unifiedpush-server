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
package com.notnoop.apns;

import java.util.Collection;
import java.util.Map;

public final class PayloadBuilder {

    private String alert;

    private String sound;

    private int badge;

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    PayloadBuilder() {
    }

    public PayloadBuilder alertBody(final String alert) {
        this.alert = alert;
        return this;
    }

    public PayloadBuilder sound(final String sound) {
        this.sound = sound;
        return this;
    }

    public PayloadBuilder badge(final int badge) {
        this.badge = badge;
        return this;
    }

    public PayloadBuilder clearBadge() {
        return badge(0);
    }

    public PayloadBuilder actionKey(final String actionKey) {
        return this;
    }

    public PayloadBuilder noActionButton() {
        return this;
    }

    public PayloadBuilder forNewsstand() {
        return this;
    }

    public PayloadBuilder localizedKey(final String key) {
        return this;
    }

    public PayloadBuilder localizedArguments(final Collection<String> arguments) {
        return this;
    }

    public PayloadBuilder localizedArguments(final String... arguments) {
        return this;
    }

    public PayloadBuilder launchImage(final String launchImage) {
        return this;
    }

    public PayloadBuilder customField(final String key, final Object value) {
        return this;
    }

    public PayloadBuilder mdm(final String s) {
        return this;
    }

    public PayloadBuilder customFields(final Map<String, ? extends Object> values) {
        return this;
    }

    public String build() {
        return "alert:" + alert + ",sound:" + sound + ",badge:" + badge;
    }

}
