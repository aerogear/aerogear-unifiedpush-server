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

import org.jboss.aerogear.unifiedpush.rest.RestApplication;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.util.Authenticator;
import org.jboss.aerogear.unifiedpush.rest.util.ClientAuthHelper;
import org.jboss.aerogear.unifiedpush.utils.DateUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;

/**
 * An archive for specifying Arquillian micro-deployments with selected parts of
 * UPS
 */
public class UnifiedPushRestArchive extends UnifiedPushArchiveBase<UnifiedPushRestArchive> {

	public UnifiedPushRestArchive(Archive<?> delegate) {
		super(UnifiedPushRestArchive.class, delegate);

		addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	public static UnifiedPushRestArchive forTestClass(Class<?> clazz) {
		return ShrinkWrap.create(UnifiedPushRestArchive.class, String.format("%s.war", clazz.getSimpleName()));
	}

	public UnifiedPushRestArchive withUtils() {
		return addPackage(DateUtils.class.getPackage());
	}

	public UnifiedPushRestArchive withPushSender() {
		return addMavenDependencies("org.jboss.aerogear.unifiedpush:unifiedpush-push-sender") //
				.addMavenDependencies("com.github.fge:json-patch");
	}

	public UnifiedPushRestArchive withRest() {
		return withPushSender() // Push sender already include services module.
				.withCassandra() //
				.withAssert() //
				.withModelJPA() //
				.withTestDS() //
				.withTestResources() //
				.withMockito() //
				.addPackage(RestApplication.class.getPackage()) //
				.addPackage(ClientAuthHelper.class.getPackage()) //
				.addPackage(RestEndpointTest.class.getPackage()) //
				.addPackage(Authenticator.class.getPackage());

	}
}
