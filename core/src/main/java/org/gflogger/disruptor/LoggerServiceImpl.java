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

import static com.lmax.disruptor.util.Util.getMinimumSequence;
import static org.gflogger.formatter.BufferFormatter.allocate;
import static org.gflogger.formatter.BufferFormatter.roundUpNextPower2;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.gflogger.*;
import org.gflogger.appender.AppenderFactory;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * garbage-free logger service implementation on the top of LMAX's disruptor.
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LoggerServiceImpl extends AbstractLoggerServiceImpl {

	private final Disruptor<LogEntryItemImpl>	disruptor;

	private final RingBuffer<LogEntryItemImpl>	ringBuffer;

	private final WaitStrategyImpl				strategy;

	/**
	 * @param count a number of items in the ring
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param appenders
	 */
	public LoggerServiceImpl(final int count, final int maxMessageSize,
		final GFLoggerBuilder[] loggerBuilders,
		final AppenderFactory ... appenderFactories) {
		this(count, maxMessageSize, null,
			createAppenders(appenderFactories),
			createLoggers(appenderFactories, loggerBuilders));
	}

	/**
	 * @param count a number of items in the ring
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param objectFormatterFactory
	 * @param appenders
	 */
	public LoggerServiceImpl(final int count, final int maxMessageSize,
		final ObjectFormatterFactory objectFormatterFactory,
		final GFLoggerBuilder[] loggerBuilders,
		final AppenderFactory ... appenderFactories) {
		this(count, maxMessageSize, objectFormatterFactory,
			createAppenders(appenderFactories),
			createLoggers(appenderFactories, loggerBuilders));
	}

	/**
	 * @param count a number of items in the ring
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param objectFormatterFactory
	 * @param appenders
	 */
	private LoggerServiceImpl(final int count, final int maxMessageSize,
		final ObjectFormatterFactory objectFormatterFactory,
		final Appender[] appenders,
		final GFLogger[] loggers) {

		super(count, maxMessageSize, objectFormatterFactory, loggers, appenders);

		// quick check is count = 2^k ?
		final int c = (count & (count - 1)) != 0 ?
			roundUpNextPower2(count) : count;

		// unicode char has 2 bytes
		final int bufferSize = multibyte ? maxMessageSize << 1 : maxMessageSize;
		final ByteBuffer buffer = allocate(c * bufferSize);

		strategy = new WaitStrategyImpl();

		final LoggerServiceImpl service = this;

		disruptor = new Disruptor<LogEntryItemImpl>(new EventFactory<LogEntryItemImpl>() {
			int i = 0;
			@Override
			public LogEntryItemImpl newInstance() {
				buffer.limit((i + 1) * bufferSize);
				buffer.position(i * bufferSize);
				i++;
				final ByteBuffer subBuffer = buffer.slice();
				return new LogEntryItemImpl(objectFormatterFactory, service, subBuffer, multibyte);
			}
		},
		executorService,
		new MultiThreadedClaimStrategy(c),
		//new MultiThreadedLowContentionClaimStrategy(c),
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

		final EntryHandler entryHandler = new EntryHandler(this, appenders);
		disruptor.handleEventsWith(entryHandler);

		ringBuffer = disruptor.start();
		running = true;
	}

	@Override
	public void entryFlushed(LocalLogEntry localEntry) {
		final String categoryName = localEntry.getCategoryName();
		final LogLevel logLevel = localEntry.getLogLevel();
		final String threadName = localEntry.getThreadName();
		final long appenderMask = localEntry.getAppenderMask();

		final long now = System.currentTimeMillis();

		long sequence = ringBuffer.next();
		final LogEntryItemImpl entry = ringBuffer.get(sequence);
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
			ringBuffer.publish(sequence);
		}
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
		disruptor.halt();

		super.stop();
	}

	@Override
	protected String name() {
		return "dgflogger";
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
						//*/
						lock.wait();
						/*/
						Thread.sleep(1);
						//*/
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
						//*/
						lock.wait(timeoutMs);

						if (!signalled || (System.currentTimeMillis() - startTime) > timeoutMs) break;
						/*/
						Thread.sleep(timeoutMs);
						//*/
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
			//*/
			if (0 != numWaiters) {
				// TODO: remove sync notification
				synchronized (lock) {
					signalled = true;
					lock.notifyAll();
				}
			}
			/*/
			//*/
		}

	}

}
