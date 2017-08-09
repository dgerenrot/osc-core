package org.osc.core.common.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public interface OSGiLog {
	
	Logger getLogger(Class<?> clazz);

	Logger getLogger(String className);
	
	ILoggerFactory getLoggerFactory();

}
