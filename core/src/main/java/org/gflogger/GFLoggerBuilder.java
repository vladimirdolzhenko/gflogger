/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gflogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.gflogger.appender.AppenderFactory;


/**
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class GFLoggerBuilder {

	private LogLevel					logLevel;
	private String						name;
	private boolean						additivity;
	private final List<AppenderFactory>	factories;

	public GFLoggerBuilder() {
		this(LogLevel.FATAL, null, false);
	}

	public GFLoggerBuilder(AppenderFactory factory) {
		this(factory.getLogLevel(), null, false, factory);
	}

	public GFLoggerBuilder(String category, AppenderFactory factory) {
		this(factory.getLogLevel(), category, false, factory);
	}

	public GFLoggerBuilder(
		LogLevel logLevel,
		String category,
		AppenderFactory ... appenderFactories
	) {
		this(logLevel, category, false, appenderFactories);
	}

	public GFLoggerBuilder(
		LogLevel logLevel,
		String category,
		boolean additivity,
		AppenderFactory ... appenderFactories
	) {
		this.logLevel = logLevel;
		this.name = category;
		this.additivity = additivity;
		this.factories = new ArrayList<>(Arrays.asList(appenderFactories));
	}

	public LogLevel getLogLevel() {
		return this.logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String category) {
		this.name = category;
	}

	public boolean isAdditivity() {
		return this.additivity;
	}

	public void setAdditivity(boolean additivity) {
		this.additivity = additivity;
	}

	public void setAppenderFactory(final AppenderFactory appender) {
		this.factories.clear();
		if (appender != null) {
			this.factories.add(appender);
		}
	}

	public void setAppenderFactories(final Collection<AppenderFactory> appenders) {
		this.factories.clear();
		if (appenders != null) {
			this.factories.addAll(appenders);
		}
	}

	public void addAppenderFactory(final AppenderFactory appender) {
		this.factories.add(appender);
	}

	public void removeAppenderFactory(final AppenderFactory appender) {
		this.factories.remove(appender);
	}

	public GFLogger build() {
		return new GFLoggerFinal(logLevel, name, additivity,
			factories.toArray(new AppenderFactory[factories.size()]));
	}

	@Override
	public String toString() {
		return "GFLoggerBuilder [logLevel:" + logLevel + ", category:" + name
			+ ", additivity=" + additivity + "]";
	}

	private static final class GFLoggerFinal implements GFLogger {
		private final LogLevel	logLevel;
		private final String	category;
		private final long		mask;
		private final boolean	additivity;

		public GFLoggerFinal(
			LogLevel logLevel,
			String category,
			boolean additivity,
			AppenderFactory ... appenderFactories
		) {
			this(logLevel, category, additivity, mask(appenderFactories));
		}

		private GFLoggerFinal(
			LogLevel logLevel,
			String category,
			boolean additivity,
			long mask
		) {
			this.logLevel = logLevel;
			this.category = category;
			this.additivity = additivity;
			this.mask = mask;
		}

		private static long mask(AppenderFactory ... appenderFactories) {
			long mask = 0;
			for (final AppenderFactory appenderFactory : appenderFactories) {
				final int idx = appenderFactory.getIndex();
				if (idx < 0) {
					throw new IllegalArgumentException("Negative indeces are not supported.");
				}
				if (idx >= Long.SIZE) {
					throw new IllegalArgumentException("Index " + idx
						+ " is too large. Max value:" + (Long.SIZE - 1));
				}
				final int appenderMask = 1 << idx;
				if ((mask & appenderMask) != 0) {
					throw new IllegalArgumentException("Duplicate index " + idx);
				}
				mask |= appenderMask;
			}
			return mask;
		}

		@Override
		public LogLevel getLogLevel() {
			return logLevel;
		}

		@Override
		public String getCategory() {
			return category;
		}

		@Override
		public boolean hasAdditivity() {
			return additivity;
		}

		@Override
		public long getAppenderMask(final LogLevel level) {
			return !this.logLevel.greaterThan(level) ? mask : 0;
		}

		@Override
		public String toString() {
			return "GFLoggerImpl [logLevel:" + logLevel + ", category:" + category
				+ ", additivity=" + additivity + "]";
		}
	}


}
