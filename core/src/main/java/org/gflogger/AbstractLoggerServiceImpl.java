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


import org.gflogger.appender.AppenderFactory;
import org.gflogger.disruptor.LoggerServiceImpl;
import org.gflogger.helpers.LogLog;
import org.gflogger.util.NamedThreadFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.gflogger.formatter.BufferFormatter.allocate;
import static org.gflogger.helpers.OptionConverter.getBooleanProperty;

/**
 * abstract garbage-free logger service
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public abstract class AbstractLoggerServiceImpl implements LoggerService {

	protected final LogLevel							level;
	protected final Appender[]							appenders;
	protected final GFLogger[]							loggers;

	protected final ThreadLocal<LocalLogEntry>			logEntryThreadLocal;

	protected final ExecutorService						executorService;

	protected final boolean								multibyte;

	protected volatile State 							state = State.NOT_STARTED;

	/**
	 * @param count a number of items in the ring, could be rounded up to the next power of 2
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param objectFormatterFactory
	 * @param appenders
	 */
	public AbstractLoggerServiceImpl(
		final int count,
		final int maxMessageSize,
		final ObjectFormatterFactory objectFormatterFactory,
		final GFLogger[] loggers,
		final Appender ... appenders
	) {
		if (appenders.length <= 0){
			throw new IllegalArgumentException("Expected at least one appender");
		}
		this.loggers = loggers;
		this.appenders = appenders;
		this.multibyte = multibyte(appenders);

		// unicode char has 2 bytes
		final int maxMessageSize0 = multibyte ? maxMessageSize << 1 : maxMessageSize;

		final ObjectFormatterFactory formatterFactory =
			objectFormatterFactory != null ?
				objectFormatterFactory :
				new DefaultObjectFormatterFactory();

		this.level = initLogLevel(loggers);

		final AbstractLoggerServiceImpl service = this;

		final boolean typeOfByteBuffer = getBooleanProperty("gflogger.bytebuffer", true);

		this.logEntryThreadLocal = new ThreadLocal<LocalLogEntry>(){
			@Override
			protected LocalLogEntry initialValue() {
				final LocalLogEntry logEntry =
					multibyte ?
					new CharBufferLocalLogEntry(Thread.currentThread(),
						maxMessageSize0,
						formatterFactory,
						service,
						getFormattingStrategy()) :
					typeOfByteBuffer ?
						new ByteBufferLocalLogEntry(Thread.currentThread(),
							maxMessageSize0,
							formatterFactory,
							service,
							getFormattingStrategy()):
						new ByteLocalLogEntry(Thread.currentThread(),
							maxMessageSize0,
							formatterFactory,
							service,
							getFormattingStrategy());
				return logEntry;
			}
		};

		executorService = initExecutorService();
	}

	protected static Appender[] createAppenders(AppenderFactory[] appenderFactories) {
		final Appender[] appenders = new Appender[appenderFactories.length];
		for (int i = 0; i < appenders.length; i++) {
			appenderFactories[i].setIndex(i);
			appenders[i] = appenderFactories[i].createAppender(LoggerServiceImpl.class);
		}
		return appenders;
	}

	protected static GFLogger[] createLoggers(
		AppenderFactory[] appenderFactories,
		GFLoggerBuilder[] loggerBuilders
	) {
		final GFLogger[] loggers = new GFLogger[loggerBuilders.length];
		for (int i = 0; i < loggerBuilders.length; i++) {
			loggers[i] = loggerBuilders[i].build();
		}
		return loggers;
	}

	protected final LogLevel initLogLevel(final GFLogger ... loggers) {
		LogLevel level = LogLevel.FATAL;
		for (int i = 0; i < loggers.length; i++) {
			final LogLevel l = loggers[i].getLogLevel();
			level = !level.greaterThan(l) ? level : l;
		}
		return level;
	}

	protected final boolean multibyte(final Appender ... appenders) {
		boolean multibyte = appenders[0].isMultibyte();
		for (int i = 1; i < appenders.length; i++) {
			if (appenders[i].isMultibyte() != multibyte){
				throw new IllegalArgumentException(
					"Expected " + (multibyte ? "multibyte" : "single byte") +
					" mode for appender #" + i);
			}
		}
		return multibyte;
	}

	protected abstract String name();

	protected abstract LoggingStrategy getFormattingStrategy();

	protected ExecutorService initExecutorService(){
		return Executors.newFixedThreadPool(1, new NamedThreadFactory(name()));
	}

	protected LogEntryItemImpl[] initEnties(int count, final int maxMessageSize) {
		// unicode char has 2 bytes
		final int bufferSize = multibyte ? maxMessageSize << 1 : maxMessageSize;
		final ByteBuffer buffer = allocate(count * bufferSize);

		final LogEntryItemImpl[] entries = new LogEntryItemImpl[count];
		for (int i = 0; i < count; i++) {
			buffer.limit((i + 1) * bufferSize);
			buffer.position(i * bufferSize);
			final ByteBuffer subBuffer = buffer.slice();
			entries[i] = new LogEntryItemImpl(subBuffer, multibyte, getFormattingStrategy());
		}
		return entries;
	}

	protected void start() {
		for (int i = 0; i < appenders.length; i++) {
			if (appenders[i].isEnabled()){
				appenders[i].start();
			}
		}

		state = State.RUNNING;
	}

	@Override
	public GFLogEntry log(final LogLevel level, final String categoryName, final long appenderMask){
		if (state == State.STOPPED) throw new IllegalStateException("Logger was stopped.");

		final LocalLogEntry entry = logEntryThreadLocal.get();

		if (!entry.isCommited()){
			LogLog.error("ERROR! log message '" + entry.stringValue()
					+ "' at thread '" + entry.getThreadName() + "' has not been commited properly.");
			entry.commit();
		}

		entry.setCommited(false);
		entry.setLogLevel(level);
		entry.setCategoryName(categoryName);
		entry.setAppenderMask(appenderMask);
		entry.clear();
		return entry;
	}

	@Override
	public FormattedGFLogEntry formattedLog(LogLevel level, String categoryName,
			String pattern, final long appenderMask) {
		if (state == State.STOPPED) throw new IllegalStateException("Logger was stopped.");

		final LocalLogEntry entry = logEntryThreadLocal.get();

		if (!entry.isCommited()){
			LogLog.error("ERROR! log message '" + entry.stringValue()
					+ "' at thread '" + entry.getThreadName() + "' has not been commited properly.");
			entry.commit();
		}

		entry.setCommited(false);
		entry.setLogLevel(level);
		entry.setCategoryName(categoryName);
		entry.setAppenderMask(appenderMask);
		entry.clear();
		entry.setPattern(pattern);
		return entry;
	}

	@Override
	public final GFLogger[] lookupLoggers(String name) {
		final List<GFLogger> candidates = new ArrayList<GFLogger>();
		for(final GFLogger logger : this.loggers){
			final String category = logger.getCategory();
			if (category == null || name.startsWith(category)){
				candidates.add(logger);
			}
		}

		Collections.sort(candidates, new Comparator<GFLogger>() {
			@Override
			public int compare(GFLogger o1, GFLogger o2) {
				final String c1 = o1.getCategory();
				final String c2 = o2.getCategory();
				return c2 == null ? -1 :
					c1 == null ?  1 :
						c2.length() - c1.length();
			}
		});

		if (candidates.isEmpty()) return GFLogger.EMPTY;

		int matchedCount = 0;
		for(final Iterator<GFLogger> it = candidates.iterator(); it.hasNext();){
			matchedCount++;
			final GFLogger candidate = it.next();
			if (!candidate.hasAdditivity()) {
				break;
			}
		}

		final List<GFLogger> matched = candidates.subList(0, matchedCount);
		return matched.toArray(new GFLogger[matched.size()]);
	}

	@Override
	public LogLevel getLevel() {
		return level;
	}

	public State getState() {
		return state;
	}

	@Override
	public void stop(){
		state = State.STOPPED;
		logEntryThreadLocal.remove();
	}

}
