package org.jboss.aerogear.unifiedpush.service.spring;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.validateMockitoUsage;

import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.OAuth2Configuration.DomainMatcher;
import org.jboss.aerogear.unifiedpush.service.spring.KeycloakServiceTest.KeycloakServiceTestConfig;
import org.jboss.aerogear.unifiedpush.spring.ServiceCacheConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { KeycloakServiceTestConfig.class, ServiceCacheConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
public class KeycloakServiceTest {

	@Autowired
	private IKeycloakService kcServiceMock;
	@Autowired
	private MockProvider mockProvider;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test() {
		assertThat(DomainMatcher.DOT.matches("test.aerogear"), is("test"));
		assertThat(DomainMatcher.DOT.matches("a-bc.test.aerogear.eu.uk"), is("a-bc"));
		assertThat(DomainMatcher.DASH.matches("test-aerogear"), is("test"));
		assertThat(DomainMatcher.DASH.matches("a-bc-test-aerogear"), is("a-bc-test"));
		assertThat(DomainMatcher.DASH.matches("a-bc-test-aerogear.eu.uk"), is("a-bc-test"));
		assertThat(DomainMatcher.NONE.matches("test.aerogear.eu.uk"), is("test.aerogear.eu.uk"));
		assertThat(DomainMatcher.NONE.matches("test-aerogear.eu.uk"), is("test-aerogear.eu.uk"));
	}

	@After
	public void validate() {
		validateMockitoUsage();
	}

	@Configuration
	static class KeycloakServiceTestConfig {
		private IKeycloakService mockKcService = mock(IKeycloakService.class);

		@Bean
		public IKeycloakService kcServiceMock() {
			return mockKcService;
		}

		@Bean
		public MockProvider mockProvider() {
			return new MockProvider(mockKcService);
		}
	}

	public static class MockProvider {
		private final IKeycloakService repository;

		public MockProvider(IKeycloakService repository) {
			this.repository = repository;
		}

		public IKeycloakService get() {
			return this.repository;
		}

	}
}
