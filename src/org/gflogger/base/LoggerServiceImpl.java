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

package org.gflogger.base;

import static org.gflogger.formatter.BufferFormatter.allocate;
import static org.gflogger.formatter.BufferFormatter.roundUpNextPower2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.gflogger.ByteBufferLocalLogEntry;
import org.gflogger.CharBufferLocalLogEntry;
import org.gflogger.DefaultObjectFormatterFactory;
import org.gflogger.FormattedGFLogEntry;
import org.gflogger.GFLogEntry;
import org.gflogger.GFLogger;
import org.gflogger.GFLoggerBuilder;
import org.gflogger.LocalLogEntry;
import org.gflogger.LogLevel;
import org.gflogger.LoggerService;
import org.gflogger.ObjectFormatterFactory;
import org.gflogger.appender.AppenderFactory;
import org.gflogger.base.appender.Appender;
import org.gflogger.helpers.LogLog;
import org.gflogger.ring.RingBuffer;
import org.gflogger.util.NamedThreadFactory;

/**
 * garbage-free logger service implementation on the top of
 * own ring buffer implementation and off-heap buffer.
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LoggerServiceImpl implements LoggerService {

	private final LogLevel						level;
	private final Appender[]					appenders;
	private final GFLogger[]					loggers;

	private final ThreadLocal<LocalLogEntry>	logEntryThreadLocal;

	private final RingBuffer<LogEntryItemImpl>	ringBuffer;
	private final ExecutorService				executorService;
	private final EntryHandler					entryHandler;

	private final boolean						multibyte;

	private volatile boolean					running	= false;

	/**
	 * @param count a number of items in the ring, could be rounded up to the next power of 2
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param objectFormatterFactory
	 * @param appenderFactories
	 */
	public LoggerServiceImpl(final int count,
			final int maxMessageSize,
			final GFLoggerBuilder[] loggerBuilders,
			final AppenderFactory ... appenderFactories) {
		this(count, maxMessageSize, null,
			createAppenders(appenderFactories),
			createLoggers(appenderFactories, loggerBuilders));
	}

	/**
	 * @param count a number of items in the ring, could be rounded up to the next power of 2
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param objectFormatterFactory
	 * @param appenderFactories
	 */
	public LoggerServiceImpl(final int count,
			final int maxMessageSize,
			final ObjectFormatterFactory objectFormatterFactory,
			final GFLoggerBuilder[] loggersBuilders,
			final AppenderFactory ... appenderFactories) {
		this(count, maxMessageSize, objectFormatterFactory,
			createAppenders(appenderFactories),
			createLoggers(appenderFactories, loggersBuilders));
	}

	private static Appender[] createAppenders(AppenderFactory[] appenderFactories) {
		final Appender[] appenders = new Appender[appenderFactories.length];
		for (int i = 0; i < appenders.length; i++) {
			appenderFactories[i].setIndex(i);
			appenders[i] = (Appender) appenderFactories[i].createAppender(LoggerServiceImpl.class);
		}
		return appenders;
	}

	private static GFLogger[] createLoggers(AppenderFactory[] appenderFactories, GFLoggerBuilder[] loggerBuilders) {
		final GFLogger[] loggers = new GFLogger[loggerBuilders.length];
		for (int i = 0; i < loggerBuilders.length; i++) {
			loggers[i] = loggerBuilders[i].build();
		}
		return loggers;
	}

	/**
	 * @param count a number of items in the ring, could be rounded up to the next power of 2
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param objectFormatterFactory
	 * @param appenders
	 */
	private LoggerServiceImpl(final int count,
			final int maxMessageSize,
			final ObjectFormatterFactory objectFormatterFactory,
			final Appender[] appenders,
			final GFLogger[] loggers) {
		if (appenders.length <= 0){
			throw new IllegalArgumentException("Expected at least one appender");
		}
		this.appenders = appenders;
		this.loggers = loggers;
		this.multibyte = multibyte(appenders);

		// unicode char has 2 bytes
		final int maxMessageSize0 = multibyte ? maxMessageSize << 1 : maxMessageSize;

		final ObjectFormatterFactory formatterFactory =
			objectFormatterFactory != null ?
				objectFormatterFactory :
				new DefaultObjectFormatterFactory();

		final int c = (count & (count - 1)) != 0 ?
			roundUpNextPower2(count) : count;

		this.level = initLogLevel(loggers);

		this.logEntryThreadLocal  = new ThreadLocal<LocalLogEntry>(){
			@Override
			protected LocalLogEntry initialValue() {
				final LocalLogEntry logEntry =
					multibyte ?
					new CharBufferLocalLogEntry(Thread.currentThread(),
						maxMessageSize0,
						formatterFactory,
						LoggerServiceImpl.this) :
					new ByteBufferLocalLogEntry(Thread.currentThread(),
						maxMessageSize0,
						formatterFactory,
						LoggerServiceImpl.this);
				return logEntry;
			}

		};

		/*/
		executorService = initExecutorService(appenders);

		start(appenders);
		/*/
		executorService = Executors.newFixedThreadPool(1, new NamedThreadFactory("gflogger"));
		entryHandler = new EntryHandler(appenders);
		this.ringBuffer =
				new RingBuffer<LogEntryItemImpl>(initEnties(c, maxMessageSize0), entryHandler);
		entryHandler.start();
		executorService.execute(entryHandler);

		//*/
		running = true;
	}

	private LogLevel initLogLevel(final Appender ... appenders) {
		LogLevel level = LogLevel.ERROR;
		for (int i = 0; i < appenders.length; i++) {
			final LogLevel l = appenders[i].getLogLevel();
			level = level.isHigher(l) ? level : l;
		}
		return level;
	}

	private final LogLevel initLogLevel(final GFLogger ... loggers) {
		LogLevel level = LogLevel.FATAL;
		for (int i = 0; i < loggers.length; i++) {
			final LogLevel l = loggers[i].getLogLevel();
			level = level.isHigher(l) ? level : l;
		}
		return level;
	}

	private boolean multibyte(final Appender ... appenders) {
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

	private ExecutorService initExecutorService(final Appender... appenders){
		final String[] names = new String[appenders.length];
		for (int i = 0; i < appenders.length; i++) {
			names[i] = appenders[i].getName();
		}

		return Executors.newFixedThreadPool(appenders.length, new NamedThreadFactory("appender", names));
	}

	private LogEntryItemImpl[] initEnties(int count, final int maxMessageSize) {
		// unicode char has 2 bytes
		final int bufferSize = multibyte ? maxMessageSize << 1 : maxMessageSize;
		final ByteBuffer buffer = allocate(count * bufferSize);

		final LogEntryItemImpl[] entries = new LogEntryItemImpl[count];
		for (int i = 0; i < count; i++) {
			buffer.limit((i + 1) * bufferSize);
			buffer.position(i * bufferSize);
			final ByteBuffer subBuffer = buffer.slice();
			entries[i] = new LogEntryItemImpl(subBuffer, multibyte);
		}
		return entries;
	}

	@Override
	public GFLogEntry log(final LogLevel level, final String categoryName, final long appenderMask){
		if (!running) throw new IllegalStateException("Logger was stopped.");
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
	public FormattedGFLogEntry formattedLog(LogLevel level, String categoryName, String pattern, final long appenderMask) {
		if (!running) throw new IllegalStateException("Logger was stopped.");
		final LocalLogEntry entry = logEntryThreadLocal.get();

		if (!entry.isCommited()){
			LogLog.error("ERROR! log message was not properly commited.");
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
	public void entryFlushed(final LocalLogEntry localEntry){
		final String categoryName = localEntry.getCategoryName();
		final LogLevel logLevel = localEntry.getLogLevel();
		final String threadName = localEntry.getThreadName();
		final long appenderMask = localEntry.getAppenderMask();

		final long now = System.currentTimeMillis();

		final long next = ringBuffer.next();
		final LogEntryItemImpl entry = ringBuffer.get(next);

		try {
			entry.setCategoryName(categoryName);
			entry.setLogLevel(logLevel);
			entry.setThreadName(threadName);
			entry.setTimestamp(now);
			entry.setAppenderMask(appenderMask);

			if (multibyte) {
				localEntry.copyTo(entry.getCharBuffer());
			} else {
				localEntry.copyTo(entry.getBuffer());
			}
		} finally {
			ringBuffer.publish(next);
		}
	}

	@Override
	public LogLevel getLevel() {
		return level;
	}

	@Override
	public GFLogger[] lookupLoggers(String name) {
		List<GFLogger> list = new ArrayList<GFLogger>();
		for(final GFLogger logger : this.loggers){
			final String category = logger.getCategory();
			if (category == null || name.startsWith(category)){
				list.add(logger);
			}
		}

		Collections.sort(list, new Comparator<GFLogger>() {
			@Override
			public int compare(GFLogger o1, GFLogger o2) {
				final String c1 = o1.getCategory();
				final String c2 = o2.getCategory();
				return c2 == null ? -1 :
					c1 == null ?  1 :
						c2.length() - c1.length();
			}
		});

		int idx = 0;
		for(final Iterator<GFLogger> it = list.iterator(); it.hasNext();){
			final GFLogger gfLogger = it.next();
			if (!gfLogger.hasAdditivity()) {
				break;
			}
			idx++;
		}

		final List<GFLogger> subList = list.subList(0, idx + 1);
		final GFLogger[] array = subList.toArray(new GFLogger[subList.size()]);

		return array;
	}

	@Override
	public void stop(){
		running = false;
		//logEntryThreadLocal.remove();
		/*/
		for(int i = 0; i < appenders.length; i++){
			appenders[i].stop();
		}
		/*/
		//*/
		ringBuffer.stop();
		executorService.shutdown();
		try {
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// ignore
		}
	}

}
