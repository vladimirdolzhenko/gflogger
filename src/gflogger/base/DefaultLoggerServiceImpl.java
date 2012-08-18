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

package gflogger.base;

import static gflogger.formatter.BufferFormatter.*;

import gflogger.ByteBufferLocalLogEntry;
import gflogger.CharBufferLocalLogEntry;
import gflogger.FormattedLogEntry;
import gflogger.LocalLogEntry;
import gflogger.LogEntry;
import gflogger.LogLevel;
import gflogger.LoggerService;
import gflogger.appender.AppenderFactory;
import gflogger.base.appender.Appender;
import gflogger.helpers.LogLog;
import gflogger.ring.BlockingWaitStrategy;
import gflogger.ring.RingBuffer;
import gflogger.util.NamedThreadFactory;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * DefaultLoggerServiceImpl is the garbage-free implementation on the top of
 * own ring buffer implementation and off-heap buffer.
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DefaultLoggerServiceImpl implements LoggerService {

	private final LogLevel level;
	private final Appender[] appenders;

	private final ThreadLocal<LocalLogEntry> logEntryThreadLocal;

	private final RingBuffer<LogEntryItemImpl> ringBuffer;
	private final ExecutorService executorService;

	private final boolean multibyte;

	private volatile boolean running = false;

	/**
	 * @param count a number of items in the ring, could be rounded up to the next power of 2
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param appenderFactories
	 */
	public DefaultLoggerServiceImpl(final int count, final int maxMessageSize, final AppenderFactory ... appenderFactories) {
		this(count, maxMessageSize, createAppenders(appenderFactories));
	}

	private static Appender[] createAppenders(AppenderFactory[] appenderFactories) {
		final Appender[] appenders = new Appender[appenderFactories.length];
		for (int i = 0; i < appenders.length; i++) {
			appenders[i] = (Appender) appenderFactories[i].createAppender(DefaultLoggerServiceImpl.class);
		}
		return appenders;
	}

	/**
	 * @param count a number of items in the ring, could be rounded up to the next power of 2
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param appenders
	 */
	public DefaultLoggerServiceImpl(final int count, final int maxMessageSize, final Appender ... appenders) {
		if (appenders.length <= 0){
			throw new IllegalArgumentException("Expected at least one appender");
		}
		this.appenders = appenders;
		this.multibyte = multibyte(appenders);

		// unicode char has 2 bytes
		final int maxMessageSize0 = multibyte ? maxMessageSize << 1 : maxMessageSize;

		final int c = (count & (count - 1)) != 0 ?
			roundUpNextPower2(count) : count;
		this.ringBuffer =
			new RingBuffer<LogEntryItemImpl>(new BlockingWaitStrategy(),
				initEnties(c, maxMessageSize0));
		this.ringBuffer.setEntryProcessors(appenders);
		this.level = initLogLevel(appenders);

		this.logEntryThreadLocal  = new ThreadLocal<LocalLogEntry>(){
			@Override
			protected LocalLogEntry initialValue() {
				final LocalLogEntry logEntry =
					multibyte ?
					new CharBufferLocalLogEntry(Thread.currentThread(),
						maxMessageSize0,
						DefaultLoggerServiceImpl.this) :
					new ByteBufferLocalLogEntry(Thread.currentThread(),
						maxMessageSize0,
						DefaultLoggerServiceImpl.this);
				return logEntry;
			}
		};

		executorService = initExecutorService(appenders);

		start(appenders);

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

	private void start(final Appender ... appenders) {
		for (int i = 0; i < appenders.length; i++) {
			appenders[i].start();
		}
		for (int i = 0; i < appenders.length; i++) {
			executorService.execute(appenders[i]);
		}
	}

	@Override
	public LogEntry log(final LogLevel level, final String categoryName){
		if (!running) throw new IllegalStateException("Logger was stopped.");
		final LocalLogEntry entry = logEntryThreadLocal.get();

		if (!entry.isCommited()){
			LogLog.error("ERROR! log message was not properly commited.");
			entry.commit();
		}

		entry.setCommited(false);
		entry.setLogLevel(level);
		entry.setCategoryName(categoryName);
		final Buffer b = multibyte ? entry.getCharBuffer() : entry.getByteBuffer();
		b.clear();
		return entry;
	}

	@Override
	public FormattedLogEntry formattedLog(LogLevel level, String categoryName, String pattern) {
		if (!running) throw new IllegalStateException("Logger was stopped.");
		final LocalLogEntry entry = logEntryThreadLocal.get();

		if (!entry.isCommited()){
			LogLog.error("ERROR! log message was not properly commited.");
			entry.commit();
		}

		entry.setCommited(false);
		entry.setLogLevel(level);
		entry.setCategoryName(categoryName);
		final Buffer b = multibyte ? entry.getCharBuffer() : entry.getByteBuffer();
		b.clear();
		entry.setPattern(pattern);
		return entry;
	}

	@Override
	public void entryFlushed(final LocalLogEntry localEntry){
		final ByteBuffer localByteBuffer = multibyte ? null : localEntry.getByteBuffer();
		final CharBuffer localCharBuffer = multibyte ? localEntry.getCharBuffer() : null;

		final long next = ringBuffer.next();
		final LogEntryItemImpl entry = ringBuffer.get(next);

		try {
			final ByteBuffer byteBuffer = multibyte ? null : entry.getBuffer();
			final CharBuffer charBuffer = multibyte ? entry.getCharBuffer() : null;

			entry.setCategoryName(localEntry.getCategoryName());
			entry.setLogLevel(localEntry.getLogLevel());
			entry.setThreadName(localEntry.getThreadName());
			entry.setTimestamp(System.currentTimeMillis());

			if (multibyte) {
				charBuffer.clear();
				charBuffer.put(localCharBuffer);
			} else {
				byteBuffer.clear();
				byteBuffer.put(localByteBuffer);
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
	public void stop(){
		running = false;
		for(int i = 0; i < appenders.length; i++){
			appenders[i].stop();
		}
		ringBuffer.stop();
		executorService.shutdown();
		try {
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// ignore
		}
	}

}
