package org.jboss.aerogear.unifiedpush.service.impl.spring;

import org.jboss.aerogear.unifiedpush.service.impl.spring.OAuth2Configuration.DomainMatcher;

public interface IOAuth2Configuration {

	boolean isOAuth2Enabled();

	String getOAuth2Url();

	String getUpsRealm();

	String getUpsiRealm();

	String getUpsMasterRealm();

	String getAdminClient();

	String getAdminUserName();

	String getAdminPassword();

	String getMasterRealmAdminUserName();

	String getMasterRealmAdminPassword();

	String getRootUrlDomain();

	String getRooturlProtocol();

	DomainMatcher getRooturlMatcher();
}
