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

import gflogger.LocalLogEntry;
import gflogger.LogEntry;
import gflogger.LogLevel;
import gflogger.LoggerService;
import gflogger.appender.AppenderFactory;
import gflogger.base.appender.Appender;
import gflogger.ring.BlockingWaitStrategy;
import gflogger.ring.RingBuffer;
import gflogger.util.NamedThreadFactory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.helpers.LogLog;

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
		
		final int c = (count & (count - 1)) != 0 ? 
			roundUpNextPower2(count) : count;
		this.ringBuffer = 
			new RingBuffer<LogEntryItemImpl>(new BlockingWaitStrategy(), 
				initEnties(c, maxMessageSize));
		this.ringBuffer.setEntryProcessors(appenders);
		this.level = initLogLevel(appenders);
		
		this.logEntryThreadLocal  = new ThreadLocal<LocalLogEntry>(){
			@Override
			protected LocalLogEntry initialValue() {
				final LocalLogEntry logEntry = 
					new LocalLogEntry(maxMessageSize, DefaultLoggerServiceImpl.this);
				return logEntry;
			}
		};
		
		executorService = initExecutorService(appenders);
		
		start(appenders);
	}
	
	private LogLevel initLogLevel(final Appender... appenders) {
		LogLevel level = LogLevel.ERROR;
		for (int i = 0; i < appenders.length; i++) {
			final LogLevel l = appenders[i].getLogLevel();
			level = level.compareTo(l) <= 0 ? level : l;
		}
		return level;
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
		final int bufferSize = maxMessageSize << 1;
		final ByteBuffer buffer = allocate(count * bufferSize);

		final LogEntryItemImpl[] entries = new LogEntryItemImpl[count];
		for (int i = 0; i < count; i++) {
			buffer.limit((i + 1) * bufferSize);
			buffer.position(i * bufferSize);
			final ByteBuffer subBuffer = buffer.slice();
			entries[i] = new LogEntryItemImpl(subBuffer.asCharBuffer());
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
		final LocalLogEntry entry = logEntryThreadLocal.get();
		
		if (!entry.isCommited()){
			LogLog.error("ERROR! log message was not properly commited.");
			entry.commit();
		}
		
		entry.setCommited(false);
		entry.setLogLevel(level);
		entry.setCategoryName(categoryName);
		entry.getBuffer().clear();
		return entry;
	}
	
	@Override
    public void entryFlushed(final LocalLogEntry localEntry){
		final long next = ringBuffer.next();
		final LogEntryItemImpl entry = ringBuffer.get(next);
		
		entry.setCategoryName(localEntry.getCategoryName());
		entry.setLogLevel(localEntry.getLogLevel());
		entry.setThreadName(localEntry.getThreadName());
		entry.setTimestamp(System.currentTimeMillis());
		final CharBuffer buffer = entry.getBuffer();
		buffer.clear();
		buffer.put(localEntry.getBuffer()).flip();
		
		ringBuffer.publish(next);
	}
	
	@Override
	public LogLevel getLevel() {
		return level;
	}

	@Override
	public void stop(){
		for(int i = 0; i < appenders.length; i++){
			appenders[i].stop();
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// ignore
		}
	}

}
