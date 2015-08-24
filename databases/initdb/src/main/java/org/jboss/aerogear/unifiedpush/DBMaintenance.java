package org.jboss.aerogear.unifiedpush;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class DBMaintenance {
	public static void main(final String[] args) {
		// Initialize spring context to create DB schema
		AnnotationConfigApplicationContext applicationContext = null;

		applicationContext = createApplicationContext();

		if (null != applicationContext) {
			applicationContext.close();
		}

		System.exit(0);
	}

	public static AnnotationConfigApplicationContext createApplicationContext() {
		final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(PersistenceJPAConfig.class);
		ctx.refresh();

		return ctx;
	}

	public static AnnotationConfigApplicationContext inititializeApplicationContext() {
		final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(PersistenceJPAConfig.class);
		ctx.refresh();

		return ctx;
	}

}
