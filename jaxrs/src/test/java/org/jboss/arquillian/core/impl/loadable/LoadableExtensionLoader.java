/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.core.impl.loadable;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.container.chameleon.ChameleonContainer;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ExtensionLoader;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.core.spi.LoadableExtension.ExtensionBuilder;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.spi.event.ManagerProcessing;

/**
 * LoadableExtensionLoader
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 * 
 * Override (Replace jboss LoadableExtensionLoader) and remove multiple services from class path.
 * This is required just when running from eclipse.
 */
public class LoadableExtensionLoader
{
   private static Logger log = Logger.getLogger(LoadableExtensionLoader.class.getName());
   private static Level level = Level.FINER;
   
   
   @Inject
   private Instance<Injector> injector;
   
   @Inject @ApplicationScoped
   private InstanceProducer<ServiceLoader> serviceLoaderProducer;
 
   private JavaSPIExtensionLoader serviceLoader;
   
   public LoadableExtensionLoader()
   {
      this(new JavaSPIExtensionLoader());
   }
   
   LoadableExtensionLoader(JavaSPIExtensionLoader serviceLoader)
   {
      this.serviceLoader = serviceLoader;
   }

   public void load(@Observes final ManagerProcessing event)
   {
      ExtensionLoader extensionLoader = locateExtensionLoader();
      
      final ServiceRegistry registry = new ServiceRegistry(injector.get());
      
      Collection<LoadableExtension> extensions = extensionLoader.load();

      for(LoadableExtension extension : extensions)
      {
         log.log(level, "extension: {0}", new Object[]{extension.getClass().getSimpleName()});
         extension.register(new ExtensionBuilder()
         {
            @Override
            public <T> ExtensionBuilder service(Class<T> service, Class<? extends T> impl)
            {
               log.log(level, "\tservice: {0} - {1}", new Object[]{service, impl});
               
               if (DeployableContainer.class.isAssignableFrom(impl) && !ChameleonContainer.class.isAssignableFrom(impl))
            	   return this;
               registry.addService(service, impl);
               return this;
            }

            @Override
            public <T> ExtensionBuilder override(Class<T> service, Class<? extends T> oldServiceImpl, Class<? extends T> newServiceImpl)
            {
               log.log(level, "\toverride: {0} overrides {1} for {2}", new Object[]{newServiceImpl, oldServiceImpl, service});
               registry.overrideService(service, oldServiceImpl, newServiceImpl);
               return this;
            }
            
            @Override
            public ExtensionBuilder observer(Class<?> handler)
            {
               log.log(level, "\tobserver: {0}", new Object[]{handler});
               event.observer(handler);
               return this;
            }
            
            @Override
            public ExtensionBuilder context(Class<? extends Context> context)
            {
               log.log(level, "\tcontext: {0}", new Object[]{context});
               event.context(context);
               return this;
            }
         });
      }
      serviceLoaderProducer.set(registry.getServiceLoader());
   }
   
   /**
    * Some environments need to handle their own ExtensionLoading and can't rely on Java's META-INF/services approach. 
    * 
    * @return configured ExtensionLoader if found or defaults to JavaSPIServiceLoader
    */
   private ExtensionLoader locateExtensionLoader()
   {
      Collection<ExtensionLoader> loaders = Collections.emptyList();
      if (SecurityActions.getThreadContextClassLoader() != null) {
          loaders = serviceLoader.all(SecurityActions.getThreadContextClassLoader(), ExtensionLoader.class);
      }
      if(loaders.size() == 0)
      {
         loaders = serviceLoader.all(LoadableExtensionLoader.class.getClassLoader(), ExtensionLoader.class);
      }
      if(loaders.size() > 1)
      {
         throw new RuntimeException("Multiple ExtensionLoader's found on classpath: " + toString(loaders));
      }
      if(loaders.size() == 1)
      {
         return loaders.iterator().next();
      }
      return serviceLoader;
   }
   
   private String toString(Collection<ExtensionLoader> loaders)
   {
      StringBuilder sb = new StringBuilder();
      for(ExtensionLoader loader : loaders)
      {
         sb.append(loader.getClass().getName()).append(", ");
      }
      return sb.toString();
   }
}
