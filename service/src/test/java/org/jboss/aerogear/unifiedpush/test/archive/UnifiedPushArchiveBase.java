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

import java.util.Map;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

public abstract class UnifiedPushArchiveBase <T extends Archive<T>> extends UnifiedPushArchive <T> {

	private PomEquippedResolveStage resolver;
	
 // -------------------------------------------------------------------------------------||
    // Class Members ----------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(UnifiedPushArchiveBase.class.getName());

    /**
     * Path to the web inside of the Archive.
     */
    private static final ArchivePath PATH_WEB = ArchivePaths.root();

    /**
     * Path to the WEB-INF inside of the Archive.
     */
    private static final ArchivePath PATH_WEB_INF = ArchivePaths.create("WEB-INF");

    /**
     * Path to the resources inside of the Archive.
     */
    private static final ArchivePath PATH_RESOURCE = ArchivePaths.create(PATH_WEB_INF, "classes");

    /**
     * Path to the libraries inside of the Archive.
     */
    private static final ArchivePath PATH_LIBRARY = ArchivePaths.create(PATH_WEB_INF, "lib");

    /**
     * Path to the classes inside of the Archive.
     */
    private static final ArchivePath PATH_CLASSES = ArchivePaths.create(PATH_WEB_INF, "classes");

    /**
     * Path to the manifests inside of the Archive.
     */
    private static final ArchivePath PATH_MANIFEST = ArchivePaths.create("META-INF");

    /**
     * Path to web archive service providers.
     */
    private static final ArchivePath PATH_SERVICE_PROVIDERS = ArchivePaths.create(PATH_CLASSES, "META-INF/services");

    // -------------------------------------------------------------------------------------||
    // Instance Members -------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    // -------------------------------------------------------------------------------------||
    // Constructor ------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Create a new WebArchive with any type storage engine as backing.
     *
     * @param delegate
     *            The storage backing.
     */
    public UnifiedPushArchiveBase(Class<T> actualType, final Archive<?> delegate) {
        super(actualType, delegate);
        
        resolver = Maven.resolver().loadPomFromFile("pom.xml");
    }

    // -------------------------------------------------------------------------------------||
    // Required Implementations -----------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getManifestPath()
     */
    @Override
    protected ArchivePath getManifestPath() {
        return PATH_MANIFEST;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getClassesPath()
     */
    @Override
    protected ArchivePath getClassesPath() {
        return PATH_CLASSES;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getResourcePath()
     */
    @Override
    protected ArchivePath getResourcePath() {
        return PATH_RESOURCE;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getLibraryPath()
     */
    @Override
    protected ArchivePath getLibraryPath() {
        return PATH_LIBRARY;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.WebContainerBase#getWebPath()
     */
    @Override
    protected ArchivePath getWebPath() {
        return PATH_WEB;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.WebContainerBase#getWebInfPath()
     */
    @Override
    protected ArchivePath getWebInfPath() {
        return PATH_WEB_INF;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.shrinkwrap.impl.base.container.WebContainerBase#getWebInfPath()
     */
    @Override
    protected ArchivePath getServiceProvidersPath() {
        return PATH_SERVICE_PROVIDERS;
    }
    
    @Override
    public T addMavenDependencies(String... deps) {
        return addAsLibraries(resolver.resolve(deps).withTransitivity().asFile());
    }
    
    public T addAsLibrary(String mavenDependency, String[] findR, String[] replaceR) {
    	JavaArchive[] archives = resolver.resolve(mavenDependency).withoutTransitivity().as(JavaArchive.class);
    
    	JavaArchive p = null;
    	for (JavaArchive jar : archives) {
    		p = jar;
    		break;
    	}
    	
    	if (p == null)
    		throw new IllegalStateException("Could not resolve desired artifact");

    	// Replace production persistence.xml with test context version
    	for (int i=0; i<findR.length; i++){
	    	p.delete(findR[i]);
	    	p.add(new ClassLoaderAsset(replaceR[i]), findR[i]);
	    	
    	}

    	Map<ArchivePath, Node> nodes = getArchive().getContent();
    	
    	// Remove old library according to maven dependency name groupId:artifactId
    	for (ArchivePath path: nodes.keySet()){
    		if (path.get().contains(mavenDependency.split(":")[1])){
    			delete(path);
    		}
    	}
    	
    	// Add the manipulated dependency to the test archive
    	return addAsLibrary(p);
    }
    
    @Override
    public T withMockito() {
        return addMavenDependencies("org.mockito:mockito-core");
    }
    
    @Override
    public T withAssertj() {
        return addMavenDependencies("org.assertj:assertj-core");
    }
    
    @Override
    public T withLang() {
        return addMavenDependencies("commons-lang:commons-lang");
        
    }
    
    @Override
    public T withHttpclient() {
        return addMavenDependencies("org.apache.httpcomponents:httpclient");
    }
    
    @Override
    public T withDAOs() {
        return addPackage(org.jboss.aerogear.unifiedpush.dao.PushApplicationDao.class.getPackage());
    }
    
    @Override
    public T withServices() {
        return addPackage(org.jboss.aerogear.unifiedpush.service.PushApplicationService.class.getPackage());
    }
}