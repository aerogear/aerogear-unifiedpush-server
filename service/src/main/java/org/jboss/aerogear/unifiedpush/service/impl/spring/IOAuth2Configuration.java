package org.jboss.aerogear.unifiedpush.service.impl.spring;

import org.jboss.aerogear.unifiedpush.service.impl.spring.OAuth2Configuration.DomainMatcher;

interface IOAuth2Configuration {

	boolean isOAuth2Enabled();

	String getOAuth2Url();

	String getUpsRealm();

	String getUpsiRealm();

	String getUpsMasterRealm();

	String getAdminClient();

	String getAdminUserName();

	String getAdminPassword();

	String getRooturlDomain();

	String getRooturlProtocol();

	DomainMatcher getRooturlMatcher();

    String getMasterPassword();
}
