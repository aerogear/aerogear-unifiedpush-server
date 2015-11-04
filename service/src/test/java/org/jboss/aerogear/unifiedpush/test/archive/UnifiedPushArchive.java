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
package org.jboss.aerogear.unifiedpush.test.archive;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;
import org.jboss.shrinkwrap.api.container.ServiceProviderContainer;
import org.jboss.shrinkwrap.api.container.WebContainer;
import org.jboss.shrinkwrap.impl.base.container.WebContainerBase;

/**
 * An archive for specifying Arquillian micro-deployments with selected parts of UPS
 */
public abstract class UnifiedPushArchive <T extends Archive<T>> extends WebContainerBase<T> implements Archive<T>, LibraryContainer<T>,
        WebContainer<T>, ResourceContainer<T>, ServiceProviderContainer<T> {

    public UnifiedPushArchive(Class<T> actualType, final Archive<?> delegate) {
        super(actualType, delegate);
    }

    public abstract T addMavenDependencies(String... deps);
    
    /**
     * Add maven Dependency Library, include transitive dependencies.
     * Find resource (findR) is replaced eith new resource.
     */
    public abstract T addAsLibrary(String mavenDependency, String[] findR, String[] replaceR);
    
    public abstract T withMockito();
    
    public abstract T withAssertj();
    
    public abstract T withLang();
    
    public abstract T withHttpclient();

    public abstract T withServices();

    public abstract T withDAOs();

    public abstract T withApi();
}
