/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gflogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * LogFactory
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LogFactory {

	private final Object lock = new Object();
	
	private final Map<String, LoggerService> services;
	private final Map<String, LoggerView> namedLogger;
	private final Map<Class, LoggerView> classedLogger;

	private LogFactory(){
		services = new HashMap<String, LoggerService>();
		namedLogger = new HashMap<String, LoggerView>();
		classedLogger = new HashMap<Class, LoggerView>();
	}

	private Logger get(final String name){
		LoggerView logger = namedLogger.get(name);
		if (logger != null) return logger;
		synchronized (lock) {
			logger = namedLogger.get(name);
			if (logger != null) return logger;

			logger = new LoggerView(name);
			final LoggerService service = getService(name);
			logger.setLoggerService(service);
			namedLogger.put(name, logger);
			return logger;
		}
	}

	private Logger get(final Class clazz){
		LoggerView logger = classedLogger.get(clazz);
		if (logger != null) return logger;
		synchronized (lock) {
			logger = classedLogger.get(clazz);
			if (logger != null) return logger;

			logger = new LoggerView(clazz);
			final LoggerService service = getService(clazz.getName());
			logger.setLoggerService(service);
			classedLogger.put(clazz, logger);
			return logger;
		}
	}
	
	static LoggerService lookupService(final String categoryName) {
		return Helper.FACTORY.getService(categoryName);
	}
	
	private LoggerService getService(final String categoryName){
		String n = categoryName;
		synchronized (lock) {
			// look for name
			for(;;){
				final LoggerService service = services.get(n);
				if (service != null){
					return service;
				}
				final int idx = n.lastIndexOf('.');
				if (idx < 0) {
					break;
				}
				n = n.substring(0, idx);
			}
		}
		return null;
	}

	public static Logger getLog(final String name){
		return Helper.FACTORY.get(name);
	}

	public static Logger getLog(final Class clazz){
		return Helper.FACTORY.get(clazz);
	}

	public static void stop(){
		synchronized (Helper.FACTORY.lock) {
			final Collection<LoggerService> values = Helper.FACTORY.services.values();
			if (values.isEmpty()) return;
			for (final LoggerService service : values) {
				service.stop();
			}
			
			Helper.FACTORY.services.clear();
			for(final LoggerView loggerView : Helper.FACTORY.namedLogger.values()){
				loggerView.setLoggerService(null);
			}
			for(final LoggerView loggerView : Helper.FACTORY.classedLogger.values()){
				loggerView.setLoggerService(null);
			}
		}
	}

	public static LogFactory init(final Map<String, LoggerService> services){
		synchronized (Helper.FACTORY.lock) {
			stop();
			if (services != null){
				Helper.FACTORY.services.putAll(services);
			}
		}
		return Helper.FACTORY;
	}

	private static class Helper {
		final static LogFactory FACTORY = new LogFactory();
	}
}
