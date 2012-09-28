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

import static org.gflogger.formatter.BufferFormatter.roundUpNextPower2;

import java.util.concurrent.TimeUnit;

import org.gflogger.AbstractLoggerServiceImpl;
import org.gflogger.Appender;
import org.gflogger.GFLogger;
import org.gflogger.GFLoggerBuilder;
import org.gflogger.LocalLogEntry;
import org.gflogger.LogEntryItemImpl;
import org.gflogger.LogLevel;
import org.gflogger.ObjectFormatterFactory;
import org.gflogger.appender.AppenderFactory;
import org.gflogger.ring.RingBuffer;

/**
 * garbage-free logger service implementation on the top of
 * own ring buffer implementation and off-heap buffer.
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LoggerServiceImpl extends AbstractLoggerServiceImpl {

	private final RingBuffer<LogEntryItemImpl>	ringBuffer;
	private final EntryHandler					entryHandler;

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

		super(count, maxMessageSize, objectFormatterFactory, loggers, appenders);

		// unicode char has 2 bytes
		final int maxMessageSize0 = multibyte ? maxMessageSize << 1 : maxMessageSize;

		final int c = (count & (count - 1)) != 0 ?
			roundUpNextPower2(count) : count;

		entryHandler = new EntryHandler(appenders);
		this.ringBuffer =
				new RingBuffer<LogEntryItemImpl>(initEnties(c, maxMessageSize0), entryHandler);
		entryHandler.start();
		executorService.execute(entryHandler);

		//*/
		running = true;
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
