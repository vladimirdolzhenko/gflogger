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
import gflogger.ObjectFormatter;
import gflogger.appender.AppenderFactory;
import gflogger.helpers.LogLog;
import gflogger.helpers.OptionConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Harald Wendel
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
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
		return OptionConverter.substVars(attributes.getValue(name), System.getProperties());
	}

	private void setProperty(Object bean, String property, Object value) throws Exception {
		if (value == null) return;
		final String setterMethodName = "set" +
			Character.toTitleCase(property.charAt(0)) + property.substring(1);
		final Class<? extends Object> clazz = bean.getClass();
		final Method[] methods = clazz.getMethods();

		Class<? extends Object> valueClass = value.getClass();
		Method targetMethod = null;

		// there is no reason to register all mapping
		final Class[] mappings = new Class[]{
			boolean.class, Boolean.class,
			int.class, Integer.class,
			long.class, Long.class
		};

		for (final Method method : methods) {
			if (setterMethodName.equals(method.getName()) &&
				method.getParameterTypes().length == 1){
				Class paramClass = method.getParameterTypes()[0];


				if (paramClass.isPrimitive()){
					for(int i = 0; i < mappings.length; i+= 2){
						if (mappings[i].equals(paramClass) && (mappings[i + 1].equals(valueClass))){
							paramClass = valueClass;
							break;
						}

						if (mappings[i].equals(paramClass) && (String.class.equals(valueClass))){
							paramClass = mappings[i + 1];
							break;
						}
					}
				}

				// handle valueOf(String) case
				if (value != null &&
					String.class.equals(valueClass) &&
					!String.class.equals(paramClass)){
					final Method valueOfMethod =
						paramClass.getMethod("valueOf", new Class[]{String.class});
					value = valueOfMethod.invoke(null, new Object[]{value});
					valueClass = value.getClass();
				}

				if (paramClass.equals(valueClass) ||
						paramClass.isAssignableFrom(valueClass)){
					targetMethod = method;
					break;
				}
			}
		}
		if (targetMethod == null){
			throw new IllegalArgumentException("there is no proper setter for property "
				+ property + " at " + clazz.getName());
		}
		targetMethod.invoke(bean, value);
	}

	private void startAppenderFactory(Attributes attributes) throws Exception {
		final String name = getAttribute(attributes, "name");
		final String clazz = getAttribute(attributes, "class");
		final String timeZone = getAttribute(attributes, "timeZone");
		final String locale = getAttribute(attributes, "locale");

		final AppenderFactory appenderFactory = (AppenderFactory)Class.forName(clazz).newInstance();

		for(final String attributeName : new String[]{"datePattern", "patternLayout",
				"append", "bufferSize", "multibyte", "immediateFlush", "fileName", "logLevel", "enabled"}){
			setProperty(appenderFactory, attributeName, getAttribute(attributes, attributeName));
		}

		if (timeZone != null) {
			setProperty(appenderFactory, "timeZone", TimeZone.getTimeZone(timeZone) );
		}
		if (locale != null) {
			setProperty(appenderFactory, "locale", new Locale(locale) );
		}

		stack.push(appenderFactory);

		appenderFactories.put(name, appenderFactory);

		debug("Created AppenderFactory '" + name + "'");
	}

	private void startLayout(Attributes attributes) throws Exception{
		final String className = getAttribute(attributes, "class");
		final String pattern  = getAttribute(attributes, "pattern");
		final String timeZoneId = getAttribute(attributes, "timeZoneId");
		final String language = getAttribute(attributes, "language");
		final Class clazz = Class.forName(className);
		final Constructor constructor = clazz.getConstructor(String.class,
				String.class, String.class);
		final Layout layout = (Layout)constructor.newInstance(pattern, timeZoneId, language);
		setProperty(stack.peek(), "layout", layout);
	}

	private void startLoggerService(Attributes attributes) throws Exception {
		final String name = getAttribute(attributes, "name");
		final String className = getAttribute(attributes, "class");
		final int count = Integer.parseInt(getAttribute(attributes, "count"));
		final int maxMessageSize = Integer.parseInt(getAttribute(attributes, "maxMessageSize"));
		final Class clazz = className != null ?
				Class.forName(className) :
				DefaultLoggerServiceFactory.class;
		final LoggerServiceFactory loggerServiceFactory =
				(LoggerServiceFactory)clazz.getConstructor(String.class, int.class, int.class).
					newInstance(name, count, maxMessageSize);
		stack.push(loggerServiceFactory);
	}

	private void startObjectFormatter(Attributes attributes) throws Exception {
		final String className = getAttribute(attributes, "class");
		final Class clazz = Class.forName(className);

		final String formatterClassName = getAttribute(attributes, "formatter");
		final Class formatterClass = Class.forName(formatterClassName);

		final ObjectFormatter objectFormatter = (ObjectFormatter) formatterClass.newInstance();
		((LoggerServiceFactory)stack.peek()).addObjectFormatter(clazz, objectFormatter);
		debug("added ObjectFormatter '" + className + "'");
	}

	private void startAppenderRef(Attributes attributes) {
		final String name = getAttribute(attributes, "name");
		final AppenderFactory appenderFactory = appenderFactories.get(name);
		if (appenderFactory == null) {
			debug("No AppenderFactory '" + name + "' found");
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
			debug("No LoggerService '" + serviceRef + "' found");
			return;
		}
		loggerViews.put(name, new LoggerServiceView(loggerService,logLevel));
		debug("Created LoggerServiceView '" + name + "'");
	}

	private void startLoggerViewRoot(Attributes attributes) {
		final String serviceRef = getAttribute(attributes, "service-ref");
		final LogLevel logLevel = LogLevel.valueOf(getAttribute(attributes, "logLevel"));
		final LoggerService loggerService = loggerServices.get(serviceRef);
		if (loggerService == null) {
			debug("No LoggerService '" + serviceRef + "' found");
			return;
		}
		loggerViews.put(null, new LoggerServiceView(loggerService,logLevel));
		debug("Created root LoggerServiceView");
	}

	private void endAppenderFactory() {
		stack.pop();
	}

	private void endLoggerService() {
		final LoggerServiceFactory factory = (LoggerServiceFactory)stack.pop();
		loggerServices.put(factory.getName(), factory.getLoggerService());
		debug("Created LoggerService '" + factory.getName() + "'");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	throws SAXException {
		try {
			if (qName.equals("appender-factory")) {
				startAppenderFactory(attributes);
			} else if (qName.equals("layout")) {
				startLayout(attributes);
			} else if (qName.equals("logger-service")) {
				startLoggerService(attributes);
			} else if (qName.equals("object-formatter")) {
				startObjectFormatter(attributes);
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

	@Override
	public void error(SAXParseException e) throws SAXException {
		super.error(e);
		error("error " + e.getMessage());
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		super.warning(e);
		error("warning " + e.getMessage());
	}

	private static void error(String message) {
		System.err.println("[GFLogger-Init] " + message);
	}

	private static void debug(String message) {
		LogLog.debug("[GFLogger-Init] " + message);
	}

}
