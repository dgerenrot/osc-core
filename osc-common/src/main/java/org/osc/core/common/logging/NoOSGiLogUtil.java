package org.osc.core.common.logging;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility is intended to be used by non-osgi-aware classes.
 * TODO: rewrite as a context listener!
 */
public class NoOSGiLogUtil {
	
	private static final OSGiLog DUMMY_OSGI_LOG_IMPL = new OSGiLog() {
		
		@Override
		public ILoggerFactory getLoggerFactory() {
			return LoggerFactory.getILoggerFactory();
		}
		
		@Override
		public Logger getLogger(String className) {
			return LoggerFactory.getLogger(className);
		}
		
		@Override
		public Logger getLogger(Class<?> clazz) {
			return LoggerFactory.getLogger(clazz);
		}
	};
	
	private static OSGiLog osgiLoggerFactory;
		
	
	/**
	 * This method is intended to be used by non-OSGi-aware classes. It will attempt to
	 * to get a working logger service from OSGi on every invocation, until one succeeds.
	 * 
	 * TODO: what if the bundle with the loggerFactory is un-installed?
	 *  
	 * @param clazz
	 * @return
	 */
	public static Logger getLogger(Class clazz) {
		
		if (osgiLoggerFactory == null) {			
			Bundle bundle = FrameworkUtil.getBundle(clazz);
			BundleContext context = bundle.getBundleContext();			
					
			@SuppressWarnings("unchecked")
			ServiceReference<OSGiLog> ref = 
				(ServiceReference<OSGiLog>) context.getServiceReference(OSGiLog.class);
			
			OSGiLog service = context.getService(ref);
			
			if (service != null) {
				osgiLoggerFactory = service;
			}			
		}
		
		OSGiLog loggerFactory = (osgiLoggerFactory != null ? osgiLoggerFactory : DUMMY_OSGI_LOG_IMPL);
		return loggerFactory.getLogger(clazz);
	}
}
