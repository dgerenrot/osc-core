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
package org.osc.core.ui;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LogComponent {

	private static BundleContext context;
	private static ILoggerFactory loggerFactory;
	
	@Activate
	public void activate(BundleContext context) {
		LogComponent.context = context;
		init(context);
	}
	
	/**
	 * Intended for use by non-osgi - aware components only
	 * @param clazz
	 * @return
	 */
	public static Logger getLogger(Class clazz) {
		return getLogger(clazz.getName());
	}
	
	/**
	 * Intended for use by non-osgi - aware components only
	 * @param className
	 * @return
	 */
	public static Logger getLogger(String className) {
		if (loggerFactory != null) {
			return loggerFactory.getLogger(className);	
		} else {
			init(context);	
			if (loggerFactory != null) {
				return loggerFactory.getLogger(className);	
			} 
		}
		
		return LoggerFactory.getLogger(className);
		
	}
	
	private static void init(BundleContext context) {
		if (context != null) {
			LogComponent.context = context;
			
			ServiceReference<ILoggerFactory> ref = context.getServiceReference(ILoggerFactory.class);
			if (ref != null) {
				loggerFactory = context.getService(ref);
			}
		}		

	}
}
