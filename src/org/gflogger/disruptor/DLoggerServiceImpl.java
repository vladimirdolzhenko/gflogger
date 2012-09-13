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

package org.gflogger.disruptor;

import static org.gflogger.formatter.BufferFormatter.allocate;
import static org.gflogger.formatter.BufferFormatter.roundUpNextPower2;
import static com.lmax.disruptor.util.Util.getMinimumSequence;


import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
import org.gflogger.FormattedLogEntry;
import org.gflogger.GFLogger;
import org.gflogger.LocalLogEntry;
import org.gflogger.LogEntry;
import org.gflogger.LogLevel;
import org.gflogger.LoggerService;
import org.gflogger.ObjectFormatterFactory;
import org.gflogger.appender.AppenderFactory;
import org.gflogger.disruptor.appender.DAppender;
import org.gflogger.helpers.LogLog;
import org.gflogger.util.NamedThreadFactory;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.MultiThreadedClaimStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * DLoggerServiceImpl - is the garbage-free logger implementation on the top of LMAX's disruptor.
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DLoggerServiceImpl implements LoggerService {

	private final LogLevel                   level;

	private final DAppender[]                appenders;

	private final GFLogger[]                 loggers;

	private final ThreadLocal<LocalLogEntry> logEntryThreadLocal;

	private final Disruptor<DLogEntryItem>   disruptor;

	private final ExecutorService            executorService;

	private final RingBuffer<DLogEntryItem>  ringBuffer;

	private final boolean                    multibyte;

	private final WaitStrategyImpl           strategy;

	private volatile boolean                 running = false;

	/**
	 * @param count a number of items in the ring
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param appenders
	 */
	public DLoggerServiceImpl(final int count, final int maxMessageSize,
		final GFLogger[] loggers,
		final AppenderFactory ... appenderFactories) {
		this(count, maxMessageSize, null, loggers, createAppenders(appenderFactories));
	}

	/**
	 * @param count a number of items in the ring
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param objectFormatterFactory
	 * @param appenders
	 */
	public DLoggerServiceImpl(final int count, final int maxMessageSize,
		final ObjectFormatterFactory objectFormatterFactory,
		final GFLogger[] loggers,
		final AppenderFactory ... appenderFactories) {
		this(count, maxMessageSize, objectFormatterFactory, loggers, createAppenders(appenderFactories));
	}

	private static DAppender[] createAppenders(AppenderFactory[] appenderFactories) {
		final DAppender[] appenders = new DAppender[appenderFactories.length];
		for (int i = 0; i < appenders.length; i++) {
			appenders[i] = (DAppender) appenderFactories[i].createAppender(DLoggerServiceImpl.class);
		}
		return appenders;
	}

	/**
	 * @param count a number of items in the ring
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param objectFormatterFactory
	 * @param appenders
	 */
	public DLoggerServiceImpl(final int count, final int maxMessageSize,
		final ObjectFormatterFactory objectFormatterFactory,
		final GFLogger[] loggers,
		final DAppender ... appenders) {
		if (appenders.length == 0){
			throw new IllegalArgumentException("Expected at least one appender");
		}
		this.appenders = appenders;
		this.multibyte = multibyte(appenders);

		// quick check is count = 2^k ?
		final int c = (count & (count - 1)) != 0 ?
			roundUpNextPower2(count) : count;

		this.loggers = loggers;

		this.level = initLogLevel(loggers);

		final ObjectFormatterFactory formatterFactory =
			objectFormatterFactory != null ?
				objectFormatterFactory :
				new DefaultObjectFormatterFactory();

		// unicode char has 2 bytes
		final int bufferSize = multibyte ? maxMessageSize << 1 : maxMessageSize;
		final ByteBuffer buffer = allocate(c * bufferSize);

		this.logEntryThreadLocal = new ThreadLocal<LocalLogEntry>(){
			@Override
			protected LocalLogEntry initialValue() {
				final LocalLogEntry logEntry =
					multibyte ?
					new CharBufferLocalLogEntry(Thread.currentThread(),
						bufferSize,
						formatterFactory,
						DLoggerServiceImpl.this) :
					new ByteBufferLocalLogEntry(Thread.currentThread(),
						bufferSize,
						formatterFactory,
						DLoggerServiceImpl.this);
				return logEntry;
			}
		};

		executorService =
				/*/
				initExecutorService(appenders);
				//*/
				Executors.newFixedThreadPool(1,
						new NamedThreadFactory("dgflogger"));
				//*/

		strategy = new WaitStrategyImpl();

		disruptor = new Disruptor<DLogEntryItem>(new EventFactory<DLogEntryItem>() {
			int i = 0;
			@Override
			public DLogEntryItem newInstance() {
				buffer.limit((i + 1) * bufferSize);
				buffer.position(i * bufferSize);
				i++;
				final ByteBuffer subBuffer = buffer.slice();
				return new DLogEntryItem(subBuffer, multibyte);
			}
		},
		executorService,
		new MultiThreadedClaimStrategy(c),
		strategy);

		disruptor.handleExceptionsWith(new ExceptionHandler() {

			@Override
			public void handleOnStartException(Throwable ex) {
				ex.printStackTrace();
			}

			@Override
			public void handleOnShutdownException(Throwable ex) {
				ex.printStackTrace();
			}

			@Override
			public void handleEventException(Throwable ex, long sequence, Object event) {
				ex.printStackTrace();
			}
		});

		/*/
		// handle appenders in a sequence mode to avoid extra synchronization
		disruptor.handleEventsWith(appenders[0]);
		for(int i = 1; i < appenders.length; i++){
			disruptor.after(appenders[i-1]).then(appenders[i]);
		}
		/*/
		final EntryHandler entryHandler = new EntryHandler(appenders);
		disruptor.handleEventsWith(entryHandler);
		//*/

		ringBuffer = disruptor.start();
		running = true;
	}

	private LogLevel initLevel(final DAppender... appenders) {
		LogLevel level = LogLevel.ERROR;
		for (int i = 0; i < appenders.length; i++) {
			final LogLevel l = appenders[i].getLogLevel();
			level = level.isHigher(l) ? level : l;
		}
		return level;
	}

	protected final LogLevel initLogLevel(final GFLogger ... loggers) {
		LogLevel level = LogLevel.FATAL;
		for (int i = 0; i < loggers.length; i++) {
			final LogLevel l = loggers[i].getLogLevel();
			level = level.isHigher(l) ? level : l;
		}
		return level;
	}

	private ExecutorService initExecutorService(final DAppender... appenders){
		final String[] names = new String[appenders.length];
		for (int i = 0; i < appenders.length; i++) {
			names[i] = appenders[i].getName();
		}

		return Executors.newFixedThreadPool(appenders.length,
			new NamedThreadFactory("appender", names));
	}

	private boolean multibyte(final DAppender... appenders) {
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

	@Override
	public LogEntry log(final LogLevel level, final String categoryName, final long appenderMask){
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
		final Buffer b = multibyte ? entry.getCharBuffer() : entry.getByteBuffer();
		b.clear();
		return entry;
	}

	@Override
	public FormattedLogEntry formattedLog(LogLevel level, String categoryName,
			String pattern, final long appenderMask) {
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
		final Buffer b = multibyte ? entry.getCharBuffer() : entry.getByteBuffer();
		b.clear();
		entry.setPattern(pattern);
		return entry;
	}

	@Override
	public void entryFlushed(LocalLogEntry localEntry) {
		final ByteBuffer localByteBuffer = multibyte ? null : localEntry.getByteBuffer();
		final CharBuffer localCharBuffer = multibyte ? localEntry.getCharBuffer() : null;

		final String categoryName = localEntry.getCategoryName();
		final LogLevel logLevel = localEntry.getLogLevel();
		final String threadName = localEntry.getThreadName();
		final long now = System.currentTimeMillis();

		long sequence = ringBuffer.next();
		final DLogEntryItem entry = ringBuffer.get(sequence);
		try {
			final ByteBuffer byteBuffer = multibyte ? null : entry.getBuffer();
			final CharBuffer charBuffer = multibyte ? entry.getCharBuffer() : null;

			entry.setCategoryName(categoryName);
			entry.setLogLevel(logLevel);
			entry.setThreadName(threadName);
			entry.setTimestamp(now);

			entry.setAppenderMask(localEntry.getAppenderMask());

			if (multibyte) {
				charBuffer.clear();
				charBuffer.put(localCharBuffer);
			} else {
				byteBuffer.clear();
				byteBuffer.put(localByteBuffer);
			}
		} finally {
			ringBuffer.publish(sequence);
		}
	}

	@Override
	public final GFLogger[] lookupLoggers(String name) {
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
	public LogLevel getLevel() {
		return level;
	}

	@Override
	public void stop(){
		if (!running) return;
		running = false;
		strategy.signalAllWhenBlocking();
//		for(int i = 0; i < appenders.length; i++){
//			appenders[i].stop();
//		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// ignore
		}
		logEntryThreadLocal.remove();

		disruptor.halt();
	}

	void flush() {
		for(int i = 0; i < appenders.length; i++){
			appenders[i].flush();
		}
	}

	private class WaitStrategyImpl implements WaitStrategy {

	    private volatile int numWaiters = 0;
	    private boolean signalled;
	    private final Object lock = new Object();

	    @Override
	    public long waitFor(final long sequence, final Sequence cursor, final Sequence[] dependents, final SequenceBarrier barrier)
	        throws AlertException, InterruptedException {
	        long availableSequence;
	        if ((availableSequence = cursor.get()) < sequence) {
	        	flush();
	            synchronized (lock) {
	                ++numWaiters;
	                while ((availableSequence = cursor.get()) < sequence) {
	                	if (!running){
	                		disruptor.halt();
	                		throw AlertException.INSTANCE;
	                	}
	                    barrier.checkAlert();
	                    lock.wait();
	                }
	                --numWaiters;
	            }
	        }

	        if (0 != dependents.length) {
	            while ((availableSequence = getMinimumSequence(dependents)) < sequence) {
	                barrier.checkAlert();
	            }
	        }

	        return availableSequence;
	    }

	    @Override
	    public long waitFor(final long sequence, final Sequence cursor, final Sequence[] dependents, final SequenceBarrier barrier,
	                        final long timeout, final TimeUnit sourceUnit)
	        throws AlertException, InterruptedException {
	        long availableSequence;
	        if ((availableSequence = cursor.get()) < sequence) {
	        	final long timeoutMs = sourceUnit.toMillis(timeout);
				final long startTime = System.currentTimeMillis() ;
				flush();
	            synchronized (lock) {
	                ++numWaiters;
	                while ((availableSequence = cursor.get()) < sequence) {
	                    barrier.checkAlert();
	                    if (!running){
	                    	disruptor.halt();
	                		throw AlertException.INSTANCE;
	                	}
	                    lock.wait(timeoutMs);

						if (!signalled || (System.currentTimeMillis() - startTime) > timeoutMs) break;
	                }
	                --numWaiters;
	            }
	        }

	        if (0 != dependents.length) {
	            while ((availableSequence = getMinimumSequence(dependents)) < sequence) {
	                barrier.checkAlert();
	            }
	        }

	        return availableSequence;
	    }

	    @Override
	    public void signalAllWhenBlocking() {
	        if (0 != numWaiters) {
	            synchronized (lock) {
	            	signalled = true;
					lock.notifyAll();
				}
	        }
	    }

	}

}
