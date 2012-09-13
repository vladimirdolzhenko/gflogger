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

package org.gflogger.config.xml;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TimeZone;

import org.gflogger.GFLogger;
import org.gflogger.Layout;
import org.gflogger.LoggerService;
import org.gflogger.ObjectFormatter;
import org.gflogger.appender.AppenderFactory;
import org.gflogger.helpers.LogLog;
import org.gflogger.helpers.OptionConverter;
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

	private final Map<String, AppenderFactory> appenderFactories = new LinkedHashMap<String, AppenderFactory>();

	private final List<GFLogger> loggers = new ArrayList<GFLogger>();

	private final Map<Class, ObjectFormatter> objectFormatters = new LinkedHashMap<Class, ObjectFormatter>();

	private LoggerService loggerService;

	public Map<String, AppenderFactory> getAppenderFactories() {
		return appenderFactories;
	}

	public LoggerService getLoggerService() {
		return loggerService;
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

	private void startAppender(Attributes attributes) throws Exception {
		final String name = getAttribute(attributes, "name");
		final String clazz = getAttribute(attributes, "class");
		final String timeZone = getAttribute(attributes, "timeZone");
		final String locale = getAttribute(attributes, "locale");

		final AppenderFactory appenderFactory = (AppenderFactory)Class.forName(clazz).newInstance();

		appenderFactory.setIndex(appenderFactories.size());

		for(final String attributeName : new String[]{"datePattern", "patternLayout",
				"append", "bufferSize", "multibyte", "immediateFlush", "fileName", "enabled"}){
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
		final String className = getAttribute(attributes, "class");
		final Class clazz = className != null ?
				Class.forName(className) :
				DefaultLoggerServiceFactory.class;
		final LoggerServiceFactory factory = (LoggerServiceFactory)clazz.newInstance();

		for(final String attributeName : new String[]{"count", "maxMessageSize"}){
			setProperty(factory, attributeName, getAttribute(attributes, attributeName));
		}

		for(final AppenderFactory appenderFactory: appenderFactories.values()){
			factory.addAppenderFactory(appenderFactory);
		}
		for(final GFLogger logger: loggers){
			factory.addLogger(logger);
		}

		for (final Entry<Class, ObjectFormatter> entry : objectFormatters.entrySet()) {
			factory.addObjectFormatter(entry.getKey(), entry.getValue());
		}

		loggerService = factory.createService();
		debug("Created LoggerService.");
	}

	private void startObjectFormatter(Attributes attributes) throws Exception {
		final String className = getAttribute(attributes, "class");
		final Class clazz = Class.forName(className);

		final String formatterClassName = getAttribute(attributes, "formatter");
		final Class formatterClass = Class.forName(formatterClassName);

		final ObjectFormatter objectFormatter = (ObjectFormatter) formatterClass.newInstance();
		objectFormatters.put(clazz, objectFormatter);

		debug("added ObjectFormatter '" + className + "'");
	}

	private void startAppenderRef(Attributes attributes) {
		final String name = getAttribute(attributes, "ref");
		final AppenderFactory appenderFactory = appenderFactories.get(name);
		if (appenderFactory == null) {
			debug("No AppenderFactory '" + name + "' found");
			return;
		}
		((GFLoggerBuilder)stack.peek()).addAppenderFactory(appenderFactory);
	}

	private void startLogger(Attributes attributes) throws Exception {
		final GFLoggerBuilder builder = new GFLoggerBuilder();

		for(final String attributeName : new String[]{"name", "additivity", "logLevel"}){
			setProperty(builder, attributeName, getAttribute(attributes, attributeName));
		}

		final String name = builder.getName();

		debug("Created " + (name != null ? "'" + name + "'" : "root") + " logger");

		stack.push(builder);
	}

	private void endLogger() {
		final GFLogger gfLogger = ((GFLoggerBuilder)stack.peek()).build();
		loggers.add(gfLogger);
	}

	private void endAppenderFactory() {
		stack.pop();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	throws SAXException {
		debug("start element:" + qName);
		try {
			if (qName.equals("appender")) {
				startAppender(attributes);
			} else if (qName.equals("layout")) {
				startLayout(attributes);
			} else if (qName.equals("service")) {
				startLoggerService(attributes);
			} else if (qName.equals("object-formatter")) {
				startObjectFormatter(attributes);
			} else if (qName.equals("appender-ref")) {
				startAppenderRef(attributes);
			} else if (qName.equals("logger")) {
				startLogger(attributes);
			} else if (qName.equals("root")) {
				startLogger(attributes);
			}
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		debug("end element:" + qName);
		if (qName.equals("appender-factory")) {
			endAppenderFactory();
		} else if (qName.equals("logger")) {
			endLogger();
		} else if (qName.equals("root")) {
			endLogger();
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
