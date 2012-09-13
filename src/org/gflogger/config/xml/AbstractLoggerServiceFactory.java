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


import java.util.ArrayList;
import java.util.List;

import org.gflogger.DefaultObjectFormatterFactory;
import org.gflogger.GFLogger;
import org.gflogger.ObjectFormatter;
import org.gflogger.appender.AppenderFactory;

/**
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
abstract class AbstractLoggerServiceFactory implements LoggerServiceFactory {

	protected final List<AppenderFactory> appenderFactories = new ArrayList<AppenderFactory>();

	protected final List<GFLogger> loggers = new ArrayList<GFLogger>();

	protected final DefaultObjectFormatterFactory objectFormatterFactory = new DefaultObjectFormatterFactory();

	protected int count;

	protected int maxMessageSize;

	public int getCount() {
		return count;
	}

	@Override
	public void setCount(int count) {
		this.count = count;
	}

	public int getMaxMessageSize() {
		return maxMessageSize;
	}

	@Override
	public void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}

	@Override
	public void addAppenderFactory(AppenderFactory factory) {
		appenderFactories.add(factory);
	}

	@Override
	public void addObjectFormatter(Class clazz, ObjectFormatter objectFormatter) {
		objectFormatterFactory.registerObjectFormatter(clazz, objectFormatter);
	}

	@Override
	public void addLogger(GFLogger logger) {
		loggers.add(logger);
	}

}
