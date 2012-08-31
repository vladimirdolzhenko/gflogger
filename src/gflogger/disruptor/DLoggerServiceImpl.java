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

import gflogger.ByteBufferLocalLogEntry;
import gflogger.CharBufferLocalLogEntry;
import gflogger.DefaultObjectFormatterFactory;
import gflogger.FormattedLogEntry;
import gflogger.LocalLogEntry;
import gflogger.LogEntry;
import gflogger.LogLevel;
import gflogger.LoggerService;
import gflogger.ObjectFormatterFactory;
import gflogger.appender.AppenderFactory;
import gflogger.disruptor.appender.DAppender;
import gflogger.helpers.LogLog;
import gflogger.util.NamedThreadFactory;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * DLoggerServiceImpl - is the garbage-free logger implementation on the top of LMAX's disruptor.
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

	private final boolean multibyte;

	private volatile boolean running = false;

	/**
	 * @param count a number of items in the ring
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param appenders
	 */
	public DLoggerServiceImpl(final int count, final int maxMessageSize,
		final AppenderFactory ... appenderFactories) {
		this(count, maxMessageSize, null, createAppenders(appenderFactories));
	}

	/**
	 * @param count a number of items in the ring
	 * @param maxMessageSize max message size in the ring (in chars)
	 * @param objectFormatterFactory
	 * @param appenders
	 */
	public DLoggerServiceImpl(final int count, final int maxMessageSize,
		final ObjectFormatterFactory objectFormatterFactory,
		final AppenderFactory ... appenderFactories) {
		this(count, maxMessageSize, objectFormatterFactory, createAppenders(appenderFactories));
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
		final DAppender ... appenders) {
		if (appenders.length == 0){
			throw new IllegalArgumentException("Expected at least one appender");
		}
		this.appenders = appenders;
		this.multibyte = multibyte(appenders);

		// quick check is count = 2^k ?
		final int c = (count & (count - 1)) != 0 ?
			roundUpNextPower2(count) : count;

		this.level = initLevel(appenders);

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

		executorService = initExecutorService(appenders);

		disruptor = new Disruptor<DLogEntryItem>(new EventFactory<DLogEntryItem>() {
			int i = 0;
			@Override
			public DLogEntryItem newInstance() {
				buffer.limit((i + 1) * bufferSize);
				buffer.position(i * bufferSize);
				i++;
				final ByteBuffer subBuffer = buffer.slice();
				// System.out.println("item at " + i + " has capacity " + subBuffer.capacity());
				return new DLogEntryItem(subBuffer, multibyte);
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

		// handle appenders in a sequence mode to avoid extra synchronization
		disruptor.handleEventsWith(appenders[0]);
		for(int i = 1; i < appenders.length; i++){
			disruptor.after(appenders[i-1]).then(appenders[i]);
		}

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
	public LogEntry log(final LogLevel level, final String categoryName){
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
	public LogLevel getLevel() {
		return level;
	}

	@Override
	public void stop(){
		if (!running) return;

		running = false;
		logEntryThreadLocal.remove();

		executorService.shutdown();
		disruptor.halt();
		flush();
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
