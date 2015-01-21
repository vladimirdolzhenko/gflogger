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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.gflogger.helpers.LogLog;

/**
 * LogFactory
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class GFLogFactory {

	private static volatile GFLogFactory FACTORY;

	private final Object lock = new Object();
	private final AtomicReference<LoggerService> loggerService;
	private final ConcurrentMap<String, GFLogView> loggers;

	private GFLogFactory(){
		loggerService = new AtomicReference<LoggerService>();
		loggers = new ConcurrentHashMap<String, GFLogView>();

		final String ver = GFLogFactory.class.getPackage().getImplementationVersion();
		LogLog.info(" version " + (ver != null ? ver : "*dev*") );
	}

	private GFLog get(final String name){
		GFLogView logger = loggers.get(name);

		if (logger != null) return logger;

		logger = new GFLogView(name);
		final LoggerService service = getService();
		logger.setLoggerService(service);

		final GFLogView existed = loggers.putIfAbsent( name, logger );
		if (existed != null){
			logger = existed;
		}

		return logger;
	}

	private GFLog get(final Class clazz){
		return get(clazz.getName());
	}

	public static LoggerService lookupService(final String name) {
		return getFactory().getService();
	}

	private LoggerService getService(){
		return loggerService.get();
	}

	private static GFLogFactory getFactory(){
		if (FACTORY != null) return FACTORY;
		synchronized ( GFLogFactory.class ){
			if (FACTORY == null) {
				FACTORY = new GFLogFactory();
			}
		}
		return FACTORY;
	}

	public static GFLog getLog(final String name){
		return getFactory().get( name );
	}

	public static GFLog getLog(final Class clazz){
		return getFactory().get( clazz );
	}

	public static void stop(){
		final GFLogFactory factory = getFactory();
		synchronized ( factory.lock) {
			final LoggerService service = factory.loggerService.getAndSet(null);
			if (service == null) return;

			service.stop();

			for(final GFLogView loggerView : factory.loggers.values()){
				loggerView.invalidate();
			}
		}
	}

	public static GFLogFactory init(LoggerService service){
		final GFLogFactory factory = getFactory();
		synchronized (factory.lock) {
			stop();
			if (service == null)
				throw new IllegalArgumentException("Not a null logger service is expected");
			factory.loggerService.set(service);
		}
		return factory;
	}

}
