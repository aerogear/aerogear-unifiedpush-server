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

/**
 * Badge notifications type for badges that are not numbers, for numbers use the value in the main part of the message.
 * <a href="https://msdn.microsoft.com/en-us/library/windows/apps/hh761494.aspx">Tile and badge catalog</a>
 */
public enum BadgeType {
    none,
    activity,
    alert,
    available,
    away,
    busy,
    newMessage,
    paused,
    playing,
    unavailable,
    error,
    attention
}
