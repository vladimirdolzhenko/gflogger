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
		synchronized (namedLogger) {
			logger = namedLogger.get(name);
			if (logger != null) return logger;

			String n = name;

			// look for name
			for(;;){
				final LoggerService impl = services.get(n);
				if (impl != null){
					logger = new LoggerView(impl, name);
					namedLogger.put(name, logger);
					return logger;
				}
				final int idx = n.lastIndexOf('.');
				if (idx < 0) {
					break;
				}
				n = n.substring(0, idx);
			}


			// otherwise looking for dumpl root
			logger = new LoggerView(null, name);
			namedLogger.put(name, logger);
			return logger;
		}
	}

	private Logger get(final Class clazz){
		LoggerView logger = classedLogger.get(clazz);
		if (logger != null) return logger;
		synchronized (classedLogger) {
			logger = classedLogger.get(clazz);
			if (logger != null) return logger;

			String n = clazz.getName();

			// look for name
			for(;;){
				final LoggerService impl = services.get(n);
				if (impl != null){
					logger = new LoggerView(impl, clazz);
					classedLogger.put(clazz, logger);
					return logger;
				}
				final int idx = n.lastIndexOf('.');
				if (idx < 0) {
					break;
				}
				n = n.substring(0, idx);
			}


			// otherwise looking for dumpl root
			logger = new LoggerView(null, clazz);
			classedLogger.put(clazz, logger);
			return logger;
		}
	}

	public static Logger getLog(final String name){
		return Helper.FACTORY.get(name);
	}

	public static Logger getLog(final Class clazz){
		return Helper.FACTORY.get(clazz);
	}

	public static void stop(){
		final Collection<LoggerService> values = Helper.FACTORY.services.values();
		for (final LoggerService impl : values) {
			impl.stop();
		}
	}

	public static void init(final Map<String, LoggerService> impls){
		Helper.FACTORY.services.clear();
		if (impls != null){
			Helper.FACTORY.services.putAll(impls);
		}
	}

	private static class Helper {
		final static LogFactory FACTORY = new LogFactory();
	}
}
