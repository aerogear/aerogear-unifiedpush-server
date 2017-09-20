package org.jboss.aerogear.unifiedpush.message;

import org.jboss.aerogear.unifiedpush.event.iOSVariantUpdateEvent;
import org.jboss.aerogear.unifiedpush.message.configuration.SenderConfigurationProvider;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.message.token.TokenLoader.TokenLoaderWrapper;
import org.jboss.aerogear.unifiedpush.spring.ServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import reactor.core.publisher.TopicProcessor;
import reactor.core.publisher.WorkQueueProcessor;

@Configuration
@Import({ ServiceConfig.class })
@ComponentScan(basePackageClasses = { SenderConfig.class, SenderConfigurationProvider.class, TokenLoaderWrapper.class })
public class SenderConfig {

	@Bean
	public WorkQueueProcessor<MessageHolderWithTokens> getTokensProcessor() {
		return WorkQueueProcessor.<MessageHolderWithTokens>builder().build();
	}

	@Bean
	public TopicProcessor<MessageHolderWithVariants> getBatchProcessor() {
		return TopicProcessor.<MessageHolderWithVariants>builder().build();
	}

	@Bean
	public WorkQueueProcessor<iOSVariantUpdateEvent> getIOsVariantUpdateProcessor() {
		return WorkQueueProcessor.<iOSVariantUpdateEvent>builder().build();
	}

}
