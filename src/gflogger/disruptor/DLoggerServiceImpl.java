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

package gflogger.disruptor;

import static com.lmax.disruptor.util.Util.getMinimumSequence;
import static gflogger.formatter.BufferFormatter.*;

import gflogger.LocalLogEntry;
import gflogger.LogEntry;
import gflogger.LogLevel;
import gflogger.LoggerService;
import gflogger.appender.AppenderFactory;
import gflogger.disruptor.appender.DAppender;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.helpers.LogLog;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * DLoggerServiceImpl - is the garbage-free logger implementation on the top of disruptor.
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DLoggerServiceImpl implements LoggerService {

	private final LogLevel level;

	private final DAppender[] appenders;
	
	private final ThreadLocal<LocalLogEntry> logEntryThreadLocal;

	private final Disruptor<DLogEntryItem> disruptor;

	private final ExecutorService executorService;
	
	private final RingBuffer<DLogEntryItem> ringBuffer;
	
	private volatile boolean running = false;
	

	/**
	 * @param count a number of items in the ring
	 * @param maxMessageSize max message size in the ring (in chars) 
	 * @param appenders
	 */
	public DLoggerServiceImpl(final int count, final int maxMessageSize, final AppenderFactory ... appenderFactories) {
		this(count, maxMessageSize, createAppenders(appenderFactories));
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
	 * @param appenders
	 */
	public DLoggerServiceImpl(final int count, final int maxMessageSize, final DAppender ... appenders) {
		if (appenders.length == 0){
			throw new IllegalArgumentException("Expected at least one appender");
		}
		this.appenders = appenders;
		
		// quick check is count = 2^k ?
		final int c = (count & (count - 1)) != 0 ?
			roundUpNextPower2(count) : count;
		
		this.level = initLevel(appenders);

		// unicode char has 2 bytes
		final int bufferSize = maxMessageSize << 1;
		final ByteBuffer buffer = allocate(c * bufferSize);
		
		this.logEntryThreadLocal = new ThreadLocal<LocalLogEntry>(){
			@Override
			protected LocalLogEntry initialValue() {
				final LocalLogEntry logEntry = 
					new LocalLogEntry(Thread.currentThread(), 
						maxMessageSize, 
						DLoggerServiceImpl.this);
				return logEntry;
			}
		};
		
		executorService = Executors.newFixedThreadPool(appenders.length);

		disruptor = new Disruptor<DLogEntryItem>(new EventFactory<DLogEntryItem>() {
			int i = 0;
			@Override
			public DLogEntryItem newInstance() {
				buffer.limit((i + 1) * bufferSize);
				buffer.position(i * bufferSize);
				i++;
				final ByteBuffer subBuffer = buffer.slice();
				// System.out.println("item at " + i + " has capacity " + subBuffer.capacity());
				return new DLogEntryItem(subBuffer.asCharBuffer());
			}
		}, 
		executorService, 
		new MultiThreadedClaimStrategy(c),
		new WaitStrategy() {
			private static final int SPIN_TRIES = 100;

			@Override
			public long waitFor(final long sequence,
					final Sequence cursor, final Sequence[] dependents,
					final SequenceBarrier barrier)
					throws AlertException, InterruptedException {
				long availableSequence;
				int counter = SPIN_TRIES;

				if (0 == dependents.length) {
					while ((availableSequence = cursor.get()) < sequence) {
						counter = applyWaitMethod(barrier, counter);
					}
				} else {
					while ((availableSequence = getMinimumSequence(dependents)) < sequence) {
						counter = applyWaitMethod(barrier, counter);
					}
				}

				return availableSequence;
			}

			@Override
			public long waitFor(final long sequence,
					final Sequence cursor, final Sequence[] dependents,
					final SequenceBarrier barrier, final long timeout,
					final TimeUnit sourceUnit) throws AlertException,
					InterruptedException {
				final long timeoutMs = sourceUnit.toMillis(timeout);
				final long startTime = System.currentTimeMillis();
				long availableSequence;
				int counter = SPIN_TRIES;

				if (0 == dependents.length) {
					while ((availableSequence = cursor.get()) < sequence) {
						counter = applyWaitMethod(barrier, counter);

						final long elapsedTime = System.currentTimeMillis() - startTime;
						if (elapsedTime > timeoutMs) {
							break;
						}
					}
				} else {
					while ((availableSequence = getMinimumSequence(dependents)) < sequence) {
						counter = applyWaitMethod(barrier, counter);

						final long elapsedTime = System.currentTimeMillis() - startTime;
						if (elapsedTime > timeoutMs) {
							break;
						}
					}
				}

				return availableSequence;
			}

			@Override
			public void signalAllWhenBlocking() {
				// nothing
			}

			private int applyWaitMethod(final SequenceBarrier barrier, int counter) 
			throws AlertException {
				barrier.checkAlert();

				if (0 == counter) {
					flush();
				} else {
					--counter;
				}

				return counter;
			}
		});
		
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
		disruptor.handleEventsWith(appenders);
		
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
		entry.getBuffer().clear();
		return entry;
	}

	@Override
	public void entryFlushed(LocalLogEntry localEntry) {
		long sequence = ringBuffer.next();
		final DLogEntryItem entry = ringBuffer.get(sequence);
		
		try {
			entry.setCategoryName(localEntry.getCategoryName());
			entry.setLogLevel(localEntry.getLogLevel());
			entry.setThreadName(localEntry.getThreadName());
			entry.setTimestamp(System.currentTimeMillis());
			final CharBuffer buffer = entry.getBuffer();
			buffer.clear();
			buffer.put(localEntry.getBuffer()).flip();
			
			entry.setSequenceId(sequence);
		} finally {
			ringBuffer.publish(sequence);
		}
	}

	
	@Override
	public LogLevel getLevel() {
		return level;
	}

	@Override
	public void stop(){
		if (!running) return;
		
		running = false;
		
		disruptor.halt();
		flush();
		executorService.shutdown();
		try {
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	void flush() {
		for(int i = 0; i < appenders.length; i++){
			appenders[i].flush();
		}
	}

}
