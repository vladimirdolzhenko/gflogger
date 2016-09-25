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


import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.gflogger.GFLoggerBuilder;
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

	private final Stack<Object>					stack				= new Stack<Object>();

	private final Map<String, AppenderFactory>	appenderFactories	= new LinkedHashMap<String, AppenderFactory>();

	private final List<GFLoggerBuilder>			loggerBuilders		= new ArrayList<GFLoggerBuilder>();

	private final Map<Class, ObjectFormatter>	objectFormatters	= new LinkedHashMap<Class, ObjectFormatter>();

	private LoggerServiceFactory				loggerServiceFactory;

	private LoggerService						loggerService;

	public Map<String, AppenderFactory> getAppenderFactories() {
		return appenderFactories;
	}

	public LoggerService getLoggerService() {
		if (loggerService == null) {
			for (final AppenderFactory appenderFactory: appenderFactories.values()) {
				loggerServiceFactory.addAppenderFactory(appenderFactory);
			}
			for (final GFLoggerBuilder loggerBuilder: loggerBuilders) {
				loggerServiceFactory.addGFLoggerBuilder(loggerBuilder);
			}

			for (final Entry<Class, ObjectFormatter> entry : objectFormatters.entrySet()) {
				loggerServiceFactory.addObjectFormatter(entry.getKey(), entry.getValue());
			}

			loggerService = loggerServiceFactory.createService();
			debug("Created LoggerService.");
		}
		return loggerService;
	}

	private String getAttribute(Attributes attributes, String name) {
		//TODO RC: doing this way will always replace unresolved ${..} to empty string,
		//which is odd, since it is easier to find out root cause having message like
		//'${no-set-property} is not appropriate format', then 'empty string is not allowed'
		return OptionConverter.substVars(attributes.getValue(name), System.getProperties());
	}

	private void startAppender(Attributes attributes) throws Exception {
		final String name = getAttribute(attributes, "name");
		final String factoryClassName = getAttribute(attributes, "class");

		//TODO RC: allow to specify plain Appender implementation as class,
		// in addition of factory style.
		final Class factoryClazz = Class.forName( factoryClassName );
		final AppenderFactory appenderFactory = (AppenderFactory) factoryClazz.newInstance();

		for ( final PropertyDescriptor property : BeanUtils.classProperties( factoryClazz ) ) {
			if ( property.getWriteMethod() != null ) {
				final String propertyName = property.getName();
				//TODO RC: skip 'name' and 'class' properties?
				final String attributeValue = getAttribute( attributes, propertyName );
				if ( attributeValue != null ) { //property is writeable
					BeanUtils.setPropertyStringValue( appenderFactory, property, attributeValue );
				}
			}
		}

		stack.push(appenderFactory);

		appenderFactories.put(name, appenderFactory);

		debug("Created AppenderFactory '" + name + "'");
	}

	private void startLayout(Attributes attributes) throws Exception {
		final String className = getAttribute(attributes, "class");
		final String pattern  = getAttribute(attributes, "pattern");
		final String timeZoneId = getAttribute(attributes, "timeZoneId");
		final String language = getAttribute(attributes, "language");
		final Class clazz = Class.forName(className);
		final Constructor constructor = clazz.getConstructor(String.class,
				String.class, String.class);
		final Layout layout = (Layout)constructor.newInstance(pattern, timeZoneId, language);
		BeanUtils.setPropertyValue( stack.peek(), "layout", layout );
	}

	private void startLoggerService(Attributes attributes) throws Exception {
		final String className = getAttribute(attributes, "class");
		final Class clazz = className != null
			? Class.forName(className)
			: DLoggerServiceFactory.class;
		loggerServiceFactory = (LoggerServiceFactory)clazz.newInstance();
		for ( final PropertyDescriptor property : BeanUtils.classProperties( clazz ) ) {
			final String propertyName = property.getName();
			if ( property.getWriteMethod() != null ) {
				BeanUtils.setPropertyStringValue(
						loggerServiceFactory,
						property,
						getAttribute( attributes, propertyName )
				);
			}
		}
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

		for ( final PropertyDescriptor property : BeanUtils.classProperties( builder.getClass() ) ) {
			final String propertyName = property.getName();
			if ( property.getWriteMethod() != null ) {
				BeanUtils.setPropertyStringValue(
						builder,
						property,
						getAttribute( attributes, propertyName )
				);
			}
		}

		final String name = builder.getName();

		debug("Created " + (name != null ? "'" + name + "'" : "root") + " logger");

		stack.push(builder);
	}

	private void endLogger() {
		final GFLoggerBuilder gfLogger = (GFLoggerBuilder)stack.peek();
		loggerBuilders.add(gfLogger);
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
		LogLog.error("[GFLogger-Init] " + message);
	}

	private static void debug(String message) {
		LogLog.debug("[GFLogger-Init] " + message);
	}

}
