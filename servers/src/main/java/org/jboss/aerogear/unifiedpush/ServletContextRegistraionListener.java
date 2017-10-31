package org.jboss.aerogear.unifiedpush;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ServletContextRegistraionListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent sce) {
		ApplicationUtil.setServletContext(sce.getServletContext());
	}

	public void contextDestroyed(ServletContextEvent sce) {

	}
}
