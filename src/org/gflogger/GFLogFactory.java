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

package org.gflogger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.gflogger.helpers.LogLog;

/**
 * LogFactory
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class GFLogFactory {

	private final Object lock = new Object();

	private final AtomicReference<LoggerService> loggerService;
	private final Map<String, GFLogView> namedLogger;
	private final Map<Class, GFLogView> classedLogger;

	private GFLogFactory(){
		loggerService = new AtomicReference<LoggerService>();
		namedLogger = new HashMap<String, GFLogView>();
		classedLogger = new HashMap<Class, GFLogView>();

		final String ver = GFLogFactory.class.getPackage().getImplementationVersion();
		LogLog.info(" version " + (ver != null ? ver : "*dev*") );
	}

	private GFLog get(final String name){
		return get0(name, name, namedLogger);
	}

	private GFLog get(final Class clazz){
		return get0(clazz, clazz.getName(), classedLogger);
	}

	private <T> GFLog get0(final T key, final String name, final Map<T, GFLogView> map){
		GFLogView logger = map.get(key);
		if (logger != null) return logger;
		synchronized (lock) {
			logger = map.get(name);
			if (logger != null) return logger;

			logger = new GFLogView(name);
			final LoggerService service = getService();
			logger.setLoggerService(service);
			map.put(key, logger);
			return logger;
		}
	}

	public static LoggerService lookupService(final String name) {
		return Helper.FACTORY.getService();
	}

	private LoggerService getService(){
		synchronized (lock) {
			return loggerService.get();
		}
	}

	public static GFLog getLog(final String name){
		return Helper.FACTORY.get(name);
	}

	public static GFLog getLog(final Class clazz){
		return Helper.FACTORY.get(clazz);
	}

	public static void stop(){
		synchronized (Helper.FACTORY.lock) {
			final LoggerService service = Helper.FACTORY.loggerService.getAndSet(null);
			if (service == null) return;

			service.stop();

			for(final GFLogView loggerView : Helper.FACTORY.namedLogger.values()){
				loggerView.invalidate();
			}
			for(final GFLogView loggerView : Helper.FACTORY.classedLogger.values()){
				loggerView.invalidate();
			}
		}
	}

	public static GFLogFactory init(LoggerService service){
		synchronized (Helper.FACTORY.lock) {
			stop();
			if (service == null)
				throw new IllegalArgumentException("Not a null logger service is expected");
			Helper.FACTORY.loggerService.set(service);
		}
		return Helper.FACTORY;
	}

	private final static class Helper {
		final static GFLogFactory FACTORY = new GFLogFactory();
	}
}
