/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.osc.core.broker.util.log;

import java.io.PrintStream;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class LogUtil {

	private static BundleContext context;
	private static ILoggerFactory loggerFactory;

	public static Logger getLogger(Class<?> clazz) {
		return getLoggerFactory().getLogger(clazz.getName());
	}

	public static Logger getLogger(String className) {
		return getLoggerFactory().getLogger(className);
	}
	
	public static ILoggerFactory getLoggerFactory() {
		
		if (loggerFactory != null) {
			return loggerFactory;
		}

		if (context != null) {
			ServiceReference<ILoggerFactory> reference = context.getServiceReference(ILoggerFactory.class);			
			loggerFactory = context.getService(reference);
			return loggerFactory;
		} 
				
		ILoggerFactory factory = LoggerFactory.getILoggerFactory(); 
		Logger log = factory.getLogger(LogUtil.class.getName());
		log.warn("Attempt to use logging before OSGi Service is registered!");
		return factory; 
	}
	
    public static synchronized void initLogging(BundleContext context) {
        try {
    
        	if (context != null) {
	        	LogUtil.context = context;
	        	
	        	LoggerContext provider = new LoggerContext();
	        	
	        	try {
		        	JoranConfigurator configurator = new JoranConfigurator();
		        	configurator.setContext(provider);
		        	provider.reset();
		        	configurator.doConfigure("./logback.xml");
	        	
	        	} catch (JoranException e) {
	        		getLogger(LogUtil.class).error("Exception starting logger!", e);
	        	}

	        	context.registerService(ILoggerFactory.class, provider, null);
        	}
        	
            StdOutErrLog.tieSystemOutAndErrToLog();
        } catch (Exception ex) {
            System.out.println("failed to initialize log4j");
            ex.printStackTrace();
        }
    }

    public static class StdOutErrLog {

        private static final Logger logger = LoggerFactory.getLogger(StdOutErrLog.class);

        public static void tieSystemOutAndErrToLog() {
            System.setOut(createLoggingProxy(System.out, false));
            System.setErr(createLoggingProxy(System.err, true));
        }

        public static PrintStream createLoggingProxy(final PrintStream realPrintStream, final boolean isError) {
            return new PrintStream(realPrintStream) {
                @Override
                public void print(final String string) {
                    realPrintStream.print(string);
                    if (isError) {
                        logger.error(string);
                    } else {
                        logger.info(string);
                    }
                }

                @Override
                public void print(Object obj) {
                    realPrintStream.print(obj);
                    if (isError) {
                        logger.error(String.valueOf(obj));
                    } else {
                        logger.info(String.valueOf(obj));
                    }
                }


            };
        }
    }
}
