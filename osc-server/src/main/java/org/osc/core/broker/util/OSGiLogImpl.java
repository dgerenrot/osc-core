package org.osc.core.broker.util;

import org.apache.log4j.PropertyConfigurator;
import org.osc.core.broker.util.log.LogUtil;
import org.osc.core.common.logging.OSGiLog;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.log4j12.Log4j12ServiceProvider;	

@Component(service={ OSGiLog.class, OSGiLogImpl.class}, immediate = true)
public class OSGiLogImpl implements OSGiLog {
	
	private BundleContext context;
	
	private ILoggerFactory loggerFactory;
	
	@Activate
	public void activate(BundleContext context) {
		this.context = context;
    	PropertyConfigurator.configureAndWatch("./log4j.properties");
    	Log4j12ServiceProvider provider = new Log4j12ServiceProvider();
    	provider.initialize();
    	loggerFactory = provider.getLoggerFactory();
	}
	
	public Logger getLogger(Class<?> clazz) {
		return loggerFactory.getLogger(clazz.getName());
	}

	public Logger getLogger(String className) {
		return loggerFactory.getLogger(className);
	}
	
	public ILoggerFactory getLoggerFactory() {		
			return loggerFactory;
	}
}
