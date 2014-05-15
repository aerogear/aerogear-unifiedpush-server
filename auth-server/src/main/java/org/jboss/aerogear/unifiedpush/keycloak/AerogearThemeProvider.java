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
package org.jboss.aerogear.unifiedpush.keycloak;

import org.keycloak.freemarker.Theme;
import org.keycloak.freemarker.ThemeProvider;
import org.keycloak.theme.ClassLoaderTheme;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AerogearThemeProvider implements ThemeProvider {

    public static final String AEROGEAR = "aerogear";

    private static Set<String> ACCOUNT_THEMES = new HashSet<String>();
    private static Set<String> LOGIN_THEMES = new HashSet<String>();
    private static Set<String> ADMIN_THEMES = new HashSet<String>();

    static {
        Collections.addAll(ACCOUNT_THEMES, AEROGEAR);
        Collections.addAll(LOGIN_THEMES, AEROGEAR);
        Collections.addAll(ADMIN_THEMES, AEROGEAR);
    }

    @Override
    public int getProviderPriority() {
        return 0;
    }

    @Override
    public Theme createTheme(String name, Theme.Type type) throws IOException {
        if (hasTheme(name, type)) {
            return new ClassLoaderTheme(name, type, getClass().getClassLoader());
        } else {
            return null;
        }
    }

    @Override
    public Set<String> nameSet(Theme.Type type) {
        switch (type) {
            case LOGIN:
                return LOGIN_THEMES;
            case ACCOUNT:
                return ACCOUNT_THEMES;
            case ADMIN:
                return ADMIN_THEMES;
            default:
                return Collections.emptySet();
        }
    }

    @Override
    public boolean hasTheme(String name, Theme.Type type) {
        return nameSet(type).contains(name);
    }

}
