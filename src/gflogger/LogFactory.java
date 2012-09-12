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
import java.util.Map.Entry;

/**
 * LogFactory
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class LogFactory {

	private final Object lock = new Object();

	private final Map<String, LoggerService> services;
	private final Map<String, LoggerView> namedLogger;
	private final Map<Class, LoggerView> classedLogger;

	private LogFactory(){
		services = new HashMap<String, LoggerService>();
		namedLogger = new HashMap<String, LoggerView>();
		classedLogger = new HashMap<Class, LoggerView>();

		final String implementationVersion = LogFactory.class.getPackage().getImplementationVersion();
		System.out.println("GFLogger version " + implementationVersion);
	}

	private Logger get(final String name){
		return get0(name, name, namedLogger);
	}

	private Logger get(final Class clazz){
		return get0(clazz, clazz.getName(), classedLogger);
	}

	private <T> Logger get0(final T key, final String name, final Map<T, LoggerView> map){
		LoggerView logger = map.get(key);
		if (logger != null) return logger;
		synchronized (lock) {
			logger = map.get(name);
			if (logger != null) return logger;

			logger = new LoggerView(name);
			final LoggerService service = getService(name);
			logger.setLoggerService(service);
			map.put(key, logger);
			return logger;
		}
	}

	public static LoggerService lookupService(final String name) {
		return Helper.FACTORY.getService(name);
	}

	private LoggerService getService(final String name){
		String n = name;
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
			return services.get(null);
		}
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
				loggerView.invalidate();
			}
			for(final LoggerView loggerView : Helper.FACTORY.classedLogger.values()){
				loggerView.invalidate();
			}
		}
	}

	public static LogFactory init(String category, LoggerService service){
		synchronized (Helper.FACTORY.lock) {
			stop();
			if (service != null){
				Helper.FACTORY.services.put(category, service);
			}
		}
		return Helper.FACTORY;
	}

	public static LogFactory init(final Map<String, LoggerService> services){
		synchronized (Helper.FACTORY.lock) {
			stop();
			if (services != null){
				for (final Entry<String, LoggerService> entry : services.entrySet()) {
					final String category = entry.getKey();
					final LoggerService loggerService = entry.getValue();
					for (final LoggerService factoryLoggerService : Helper.FACTORY.services.values()) {
						if (loggerService == factoryLoggerService){
							throw new IllegalStateException();
						}
					}
					Helper.FACTORY.services.put(category, loggerService);
				}
			}
		}
		return Helper.FACTORY;
	}

	private static class Helper {
		final static LogFactory FACTORY = new LogFactory();
	}
}
