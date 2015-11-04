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
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;

/**
 * An archive for specifying Arquillian micro-deployments with selected parts of UPS
 */
public class UnifiedPushServiceArchive extends UnifiedPushArchiveBase<UnifiedPushServiceArchive> {

    public UnifiedPushServiceArchive(Archive<?> delegate) {
        super(UnifiedPushServiceArchive.class, delegate);
 
        addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    public static UnifiedPushServiceArchive forTestClass(Class<?> clazz) {
        return ShrinkWrap.create(UnifiedPushServiceArchive.class, String.format("%s.war", clazz.getSimpleName()));
    }
    
  
    @Override
    public UnifiedPushServiceArchive withApi() {
        return addPackage(org.jboss.aerogear.unifiedpush.api.PushApplication.class.getPackage());
    }
    
	
    @Override
    public UnifiedPushServiceArchive withDAOs() {
        return addPackage(org.jboss.aerogear.unifiedpush.dao.PushApplicationDao.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.dto.Count.class.getPackage());
    }
}
