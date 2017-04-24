package org.jboss.aerogear.unifiedpush.service.impl.spring;

interface IOAuth2Configuration {

	boolean isOAuth2Enabled();

	String getOAuth2Url();

	String getUpsRealm();

	String getUpsiRealm();

	String getAdminClient();

	String getAdminUserName();

	String getAdminPassword();

	String getRooturlDomain();

	String getRooturlProtocol();

}
