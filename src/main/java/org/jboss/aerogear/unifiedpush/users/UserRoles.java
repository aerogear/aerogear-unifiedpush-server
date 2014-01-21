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
package org.jboss.aerogear.unifiedpush.users;

/**
 * A type-safe identifier for the user role types.
 */
public class UserRoles {
    /**
     * The type identifier for the viewer role.
     * an user with the role "viewer" can list all the applications and the variants
     * but can not create/edit or delete
     *
     */
    public static final String VIEWER = "viewer";

    /**
     * The type identifier for the developer role.
     * an user with the role "developer" can create/edit/delete applications or variants that he owns.
     * He can not see other applications/variants
     */
    public static final String DEVELOPER = "developer";

    /**
     * The type identifier for the admin.
     * an user with the role "admin" can list all the application and the variants,
     * he has create/edit or delete rights and can manage users.
     */
    public static final String ADMIN = "admin";

}
