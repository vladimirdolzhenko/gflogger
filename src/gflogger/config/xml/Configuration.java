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

package gflogger.config.xml;

import gflogger.Layout;
import gflogger.LogLevel;
import gflogger.LoggerService;
import gflogger.LoggerServiceView;
import gflogger.appender.AppenderFactory;
import gflogger.helpers.LogLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.commons.lang.text.StrSubstitutor;

/**
 *
 * @author Harald Wendel
 */
public class Configuration extends DefaultHandler {

	private final Stack<Object> stack = new Stack<Object>();

	private final Map<String, AppenderFactory> appenderFactories = new HashMap<String, AppenderFactory>();

	private final Map<String, LoggerService> loggerServices = new HashMap<String, LoggerService>();

	private final Map<String, LoggerService> loggerViews = new HashMap<String, LoggerService>();

	public Map<String, AppenderFactory> getAppenderFactories() {
		return appenderFactories;
	}

	public Map<String, LoggerService> getLoggerViews() {
		return loggerViews;
	}

	private String getAttribute(Attributes attributes, String name) {
		return StrSubstitutor.replaceSystemProperties(attributes.getValue(name));
	}

	private void setProperty(Object bean, String property, Object value) throws Exception {
		MethodUtils.invokeMethod(bean, "set" + StringUtils.capitalize(property), value);
	}

	private void startAppenderFactory(Attributes attributes) throws Exception {

		final String name = getAttribute(attributes, "name");
		final String clazz = getAttribute(attributes, "class");
		final String datePattern = getAttribute(attributes, "datePattern");
		final String append = getAttribute(attributes, "append");
		final String immediateFlush = getAttribute(attributes, "immediateFlush");
		final String logLevel = getAttribute(attributes, "logLevel");
		final String fileName = getAttribute(attributes, "fileName");

		final AppenderFactory appenderFactory = (AppenderFactory)Class.forName(clazz).newInstance();

		if (datePattern != null) {
			setProperty(appenderFactory, "datePattern", datePattern);
		}
		if (append != null) {
			setProperty(appenderFactory, "append", Boolean.parseBoolean(append));
		}
		if (immediateFlush != null) {
			setProperty(appenderFactory, "immediateFlush", Boolean.parseBoolean(immediateFlush));
		}
		if (logLevel != null) {
			setProperty(appenderFactory, "logLevel", LogLevel.valueOf(logLevel));
		}
		if (fileName != null) {
			setProperty(appenderFactory, "fileName", fileName);
		}

		stack.push(appenderFactory);

		appenderFactories.put(name, appenderFactory);

		log("Created AppenderFactory '" + name + "'");
	}

	private void startLayout(Attributes attributes) throws Exception{
		final String clazz = getAttribute(attributes, "class");
		final String pattern  = getAttribute(attributes, "pattern");
		final String timeZoneId = getAttribute(attributes, "timeZoneId");
		final Layout layout = (Layout)Class.forName(clazz).getConstructor(String.class, String.class).newInstance(pattern, timeZoneId);
		setProperty(stack.peek(), "layout", layout);
	}

	private void startLoggerService(Attributes attributes) throws Exception {
		final String name = getAttribute(attributes, "name");
		final String className = getAttribute(attributes, "class");
		final int count = Integer.parseInt(getAttribute(attributes, "count"));
		final int maxMessageSize = Integer.parseInt(getAttribute(attributes, "maxMessageSize"));
		final Class clazz = className != null ? Class.forName(className) : DefaultLoggerServiceFactory.class;
		final LoggerServiceFactory loggerServiceFactory =
				(LoggerServiceFactory)clazz.getConstructor(String.class, int.class, int.class).
					newInstance(name, count, maxMessageSize);
		stack.push(loggerServiceFactory);
	}

	private void startAppenderRef(Attributes attributes) {
		final String name = getAttribute(attributes, "name");
		final AppenderFactory appenderFactory = appenderFactories.get(name);
		if (appenderFactory == null) {
			log("No AppenderFactory '" + name + "' found");
			return;
		}
		((LoggerServiceFactory)stack.peek()).addAppenderFactory(appenderFactory);
	}

	private void startLoggerView(Attributes attributes) {
		final String name = getAttribute(attributes, "name");
		final String serviceRef = getAttribute(attributes, "service-ref");
		final LogLevel logLevel = LogLevel.valueOf(getAttribute(attributes, "logLevel"));
		final LoggerService loggerService = loggerServices.get(serviceRef);
		if (loggerService == null) {
			log("No LoggerService '" + serviceRef + "' found");
			return;
		}
		loggerViews.put(name, new LoggerServiceView(loggerService,logLevel));
		log("Created LoggerServiceView '" + name + "'");
	}

	private void startLoggerViewRoot(Attributes attributes) {
		final String serviceRef = getAttribute(attributes, "service-ref");
		final LogLevel logLevel = LogLevel.valueOf(getAttribute(attributes, "logLevel"));
		final LoggerService loggerService = loggerServices.get(serviceRef);
		if (loggerService == null) {
			log("No LoggerService '" + serviceRef + "' found");
			return;
		}
		loggerViews.put(null, new LoggerServiceView(loggerService,logLevel));
		log("Created root LoggerServiceView");
	}

	private void endAppenderFactory() {
		stack.pop();
	}

	private void endLoggerService() {
		final LoggerServiceFactory factory = (LoggerServiceFactory)stack.pop();
		loggerServices.put(factory.getName(), factory.getLoggerService());
		log("Created LoggerService '" + factory.getName() + "'");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		try {
			if (qName.equals("appender-factory")) {
				startAppenderFactory(attributes);
			} else if (qName.equals("layout")) {
				startLayout(attributes);
			} else if (qName.equals("logger-service")) {
				startLoggerService(attributes);
			} else if (qName.equals("appender-ref")) {
				startAppenderRef(attributes);
			} else if (qName.equals("logger-view")) {
				startLoggerView(attributes);
			} else if (qName.equals("logger-view-root")) {
				startLoggerViewRoot(attributes);
			}
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("appender-factory")) {
			endAppenderFactory();
		} else if (qName.equals("logger-service")) {
			endLoggerService();
		}
	}

	private static void log(String message) {
		LogLog.debug("[GFLogger-Init] " + message);
	}

}
