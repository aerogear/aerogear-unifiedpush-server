package org.jboss.aerogear.unifiedpush.spring;

import org.jboss.aerogear.unifiedpush.jpa.JPAConfig;
import org.jboss.aerogear.unifiedpush.service.impl.OtpCodeService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IConfigurationService;
import org.jboss.aerogear.unifiedpush.service.metrics.IPushMessageMetricsService;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ ConfigurationEnvironment.class, ServiceCacheConfig.class, JPAConfig.class })
@ComponentScan(basePackageClasses = { OtpCodeService.class, IConfigurationService.class, IPushMessageMetricsService.class })
public class ServiceConfig {


}
