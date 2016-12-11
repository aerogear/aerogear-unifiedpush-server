package org.jboss.aerogear.unifiedpush.spring;

import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ ConfigurationEnvironment.class })
public class ServiceConfig {

}
