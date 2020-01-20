package org.jboss.aerogear.unifiedpush.auth;

import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@KeycloakConfiguration
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

	/**
	 * Request matcher that matches requests to any request with a
	 * <code>Authorization</code> header.
	 */

	public static final RequestMatcher DATABASE_REQUEST_MATCHER = //
			new AndRequestMatcher( //
					new AntPathRequestMatcher("/rest/database/**"), //
					new BasicAuthenticationRequestMatcher() //
			);

	/**
	 * Registers the KeycloakAuthenticationProvider with the authentication
	 * manager.
	 */
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(keycloakAuthenticationProvider());
	}

	/**
	 * Defines the session authentication strategy.
	 */
	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}

	@Bean
	public PathBasedKeycloakConfigResolver getResolver() {
		return new PathBasedKeycloakConfigResolver();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);

		// Disable CSRF for all URLS.
		// TODO - Revisit this decision and consider CSRF for UI based requests.
		http.csrf().disable();
		http.rememberMe().disable();

		// Cluster environment can't use spring sessions without either sticky
		// or session replication. NODE1 session is invalid at NODE2.
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		http.authorizeRequests() //
				.antMatchers("/rest/database/**").hasAnyRole("INSTALLATION") //
				.antMatchers("/rest/registry/type/**").hasAnyRole("INSTALLATION") //
				.antMatchers("/rest/**").hasAnyRole("ADMIN", "DEVELOPER") //
				.antMatchers("/**").denyAll();
	}

	@Override
	public void init(WebSecurity web) throws Exception {
		super.init(web);

		// Public Access
		web.ignoring().antMatchers("/rest/keycloak/config");
		web.ignoring().antMatchers("/rest/heartbeat*");
		web.ignoring().antMatchers("/rest/otp/*");

		// JWT validation internally
		web.ignoring().antMatchers("/rest/shortlinks/**");

		// Application Level Basic Authentication
		web.ignoring().antMatchers("/rest/alias/**");
		// /sender endpoint is referenced differently (/sender || /sender/)
		web.ignoring().antMatchers("/rest/sender");
		web.ignoring().antMatchers("/rest/sender/*");
		web.ignoring().antMatchers("/rest/database/*/aliases");

		// Variant Level Basic Authentication
		web.ignoring().antMatchers("/rest/registry/device/**");
	
		// Either Application or Variant Level Basic Authentication
		// 1) /rest/database/**
		web.ignoring().requestMatchers(DATABASE_REQUEST_MATCHER);

	}

	@Override
	protected KeycloakAuthenticationProvider keycloakAuthenticationProvider() {
		KeycloakAuthenticationProvider authenticationProvider = new KeycloakAuthenticationProvider();
		authenticationProvider.setGrantedAuthoritiesMapper(authorityMapper());

		return authenticationProvider;
	}

	@Bean
	public SimpleAuthorityMapper authorityMapper() {
		SimpleAuthorityMapper authorityMapper = new SimpleAuthorityMapper();
		authorityMapper.setConvertToUpperCase(true);

		return authorityMapper;
	}
}
