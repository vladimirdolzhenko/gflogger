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

import org.gflogger.appender.AppenderFactory;


/**
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class GFLoggerImpl implements GFLogger {

	private final LogLevel	logLevel;
	private final String		category;
	private final long		mask;
	private final boolean		additivity;

	public GFLoggerImpl(LogLevel logLevel, String category, Appender ... appenders) {
		this(logLevel, category, false, appenders);
	}

	public GFLoggerImpl(LogLevel logLevel, String category, boolean additivity, Appender ... appenders) {
		this(logLevel, category, additivity, mask(appenders));
	}

	public GFLoggerImpl(AppenderFactory factory) {
		this(factory.getLogLevel(), null, false, factory);
	}

	public GFLoggerImpl(String category, AppenderFactory factory) {
		this(factory.getLogLevel(), category, false, factory);
	}

	public GFLoggerImpl(LogLevel logLevel, String category, AppenderFactory ... appenderFactories) {
		this(logLevel, category, false, appenderFactories);
	}

	public GFLoggerImpl(LogLevel logLevel, String category, boolean additivity, AppenderFactory ... appenderFactories) {
		this(logLevel, category, additivity, mask(appenderFactories));
	}

	private GFLoggerImpl(LogLevel logLevel, String category, boolean additivity, long mask, Appender ... appenders){
		this.logLevel = logLevel;
		this.category = category;
		this.additivity = additivity;
		this.mask = mask;
	}

	private static long mask(AppenderFactory ... appenderFactories){
		long mask = 0;
		for (final AppenderFactory appenderFactory : appenderFactories) {
			final int idx = appenderFactory.getIndex();
			if (idx < 0){
				throw new IllegalArgumentException("Negative indeces are not supported.");
			}
			if (idx >= Long.SIZE){
				throw new IllegalArgumentException("Index " + idx +
						" is too large. Max value:" + (Long.SIZE - 1));
			}
			final int appenderMask = 1 << idx;
			if ((mask & appenderMask) != 0){
				throw new IllegalArgumentException("Duplicate index " + idx);
			}
			mask |= appenderMask;
		}
		return mask;
	}

	private static long mask(Appender ... appenders){
		long mask = 0;
		for (final Appender appender : appenders) {
			final int idx = appender.getIndex();
			if (idx < 0){
				throw new IllegalArgumentException("Negative indeces are not supported.");
			}
			if (idx >= Long.SIZE){
				throw new IllegalArgumentException("Index " + idx +
						" is too large. Max value:" + (Long.SIZE - 1));
			}
			final int appenderMask = 1 << idx;
			if ((mask & appenderMask) != 0){
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
		return this.logLevel.isHigher(level) ? mask : 0;
	}

	@Override
	public String toString() {
		return "GFLoggerImpl [logLevel:" + logLevel + ", category:" + category +
			", additivity=" + additivity + "]";
	}


}
