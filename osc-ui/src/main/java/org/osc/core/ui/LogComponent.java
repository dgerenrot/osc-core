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

import static org.osgi.service.component.annotations.ReferenceCardinality.OPTIONAL;
import static org.osgi.service.component.annotations.ReferencePolicyOption.GREEDY;

import java.util.concurrent.atomic.AtomicReference;

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
	
	private static final ILoggerFactory FALLBACK_IMPL = LoggerFactory.getILoggerFactory();
	
	private static AtomicReference<ILoggerFactory> loggerFactoryRef 
				= new AtomicReference<>(FALLBACK_IMPL);
	
	@Activate
	public void activate(BundleContext context) {
		LogComponent.context = context;		
	}
	
	@Reference(cardinality=OPTIONAL, policyOption=GREEDY)
	public void setLoggerFactoryInst(ILoggerFactory instance) {
		setLoggerFactory(instance);		
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
		return loggerFactoryRef.get().getLogger(className);
	}
	
	
	private static void setLoggerFactory(ILoggerFactory instance) {
		loggerFactoryRef.accumulateAndGet(instance, (prev, next) -> next != null ? next : prev);
	}
	
}
