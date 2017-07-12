package org.jboss.aerogear.unifiedpush.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;

/**
 * Override CassandraSessionFactoryBean and retry connection X times.
 * TODO - Extract loop count and sleep interval to configuration.
 */
public class RetryCassandraSessionFactoryBean extends CassandraSessionFactoryBean {
	private final Logger logger = LoggerFactory.getLogger(RetryCassandraSessionFactoryBean.class);

	@Override
	public void afterPropertiesSet() throws Exception {
		for (int i = 0; 1 < 20; i++) {
			try {
				super.afterPropertiesSet();
				break;
			} catch (Exception e) {
				if (i == 20)
					throw e;
				logger.warn("Unable to connect to cassandra endpoints, retry " + (i + 1) + "/20");
				Thread.sleep(10000);
			}
		}
	}

}
