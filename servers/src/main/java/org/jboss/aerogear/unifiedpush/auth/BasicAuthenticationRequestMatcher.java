package org.jboss.aerogear.unifiedpush.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class BasicAuthenticationRequestMatcher implements RequestMatcher {
	private static final String AUTHORIZATION_HEADER = "Authorization";

	private final String expectedHeaderName = AUTHORIZATION_HEADER;
	private final String expectedHeaderValue = "Basic";

	public boolean matches(HttpServletRequest request) {
		String actualHeaderValue = request.getHeader(expectedHeaderName);
		if (StringUtils.isEmpty(actualHeaderValue)) {
			return false;
		}

		return actualHeaderValue.trim().startsWith(expectedHeaderValue);
	}

	@Override
	public String toString() {
		return "RequestHeaderRequestMatcher [expectedHeaderName=" + expectedHeaderName + ", expectedHeaderValue="
				+ expectedHeaderValue + "]";
	}

}
