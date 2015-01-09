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
package org.jboss.aerogear.unifiedpush.message.windows;

import java.util.ArrayList;
import java.util.List;

/**
 * Windows specific push notification settings support for Tile, Raw, Badge and Toast messages
 */
public class Windows {
    private Type type;
    private DurationType duration;
    private BadgeType badge;
    private TileType tileType;
    private ToastType toastType;
    private List<String> images = new ArrayList<String>();
    private List<String> textFields = new ArrayList<String>();

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public DurationType getDuration() {
        return duration;
    }

    public void setDuration(DurationType duration) {
        this.duration = duration;
    }

    public BadgeType getBadge() {
        return badge;
    }

    public void setBadge(BadgeType badge) {
        this.badge = badge;
    }

    public TileType getTileType() {
        return tileType;
    }

    public void setTileType(TileType tileType) {
        this.tileType = tileType;
    }

    public ToastType getToastType() {
        return toastType;
    }

    public void setToastType(ToastType toastType) {
        this.toastType = toastType;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getTextFields() {
        return textFields;
    }

    public void setTextFields(List<String> textFields) {
        this.textFields = textFields;
    }
}
