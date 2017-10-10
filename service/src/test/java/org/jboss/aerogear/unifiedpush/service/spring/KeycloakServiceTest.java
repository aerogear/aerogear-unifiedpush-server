package org.jboss.aerogear.unifiedpush.service.spring;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

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

		when(mockProvider.get().getVariantIdsFromClient(eq("test-client-1")))
				.thenReturn(Arrays.asList("variant-1", "variant-2"));
		when(mockProvider.get().getVariantIdsFromClient(eq("test-client-2")))
				.thenReturn(Arrays.asList("variant-3", "variant-4"));
	}

	@Test
	public void cacheTest() {
		List<String> firstInvocation = kcServiceMock.getVariantIdsFromClient("test-client-1");
		assertThat(firstInvocation.get(0), is("variant-1"));

		List<String> secondInvocation = kcServiceMock.getVariantIdsFromClient("test-client-1");
		assertThat(secondInvocation.get(0), is("variant-1"));

		verify(mockProvider.get(), times(1)).getVariantIdsFromClient("test-client-1");
	}

	@Test
	public void test() {
		assertThat(DomainMatcher.DOT.matches("test.aerobase"), is("test"));
		assertThat(DomainMatcher.DOT.matches("a-bc.test.aerobase.eu.uk"), is("a-bc"));
		assertThat(DomainMatcher.DASH.matches("test-aerobase"), is("test"));
		assertThat(DomainMatcher.DASH.matches("a-bc-test-aerobase"), is("a-bc-test"));
		assertThat(DomainMatcher.DASH.matches("a-bc-test-aerobase.eu.uk"), is("a-bc-test"));
		assertThat(DomainMatcher.NONE.matches("test.aerobase.eu.uk"), is("test.aerobase.eu.uk"));
		assertThat(DomainMatcher.NONE.matches("test-aerobase.eu.uk"), is("test-aerobase.eu.uk"));
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
