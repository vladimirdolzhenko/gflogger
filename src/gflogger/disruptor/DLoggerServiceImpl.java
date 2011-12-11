package gflogger.disruptor;

import static com.lmax.disruptor.util.Util.getMinimumSequence;
import gflogger.LocalLogEntry;
import gflogger.LogEntry;
import gflogger.LogLevel;
import gflogger.LoggerService;
import gflogger.disruptor.appender.DAppender;
import gflogger.util.MutableLong;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

	private final RingBuffer<DLogEntryItem> ringBuffer;

	private final ExecutorService executorService;

	/**
	 * @param count a number of items in the ring
	 * @param bufferSize buffer size of each item in the ring 
	 * @param appenders
	 */
	public DLoggerServiceImpl(final int count, final int bufferSize, final DAppender ... appenders) {
		if (appenders.length == 0){
			throw new IllegalArgumentException("Expected at least one appender");
		}
		if (appenders.length > (Integer.SIZE - 1)){
			throw new IllegalArgumentException("Expected less than " + (Integer.SIZE - 1) + " appenders");
		}
		this.appenders = appenders;
		
		// quick check is count = 2^k ?
		final int c = (count & (count - 1)) != 0 ?
			roundUpNextPower2(count) : count;
		
		this.level = initLevel(appenders);

		final ByteBuffer buffer = ByteBuffer.allocate(c * bufferSize);
		
		this.logEntryThreadLocal  = new ThreadLocal<LocalLogEntry>(){
			@Override
			protected LocalLogEntry initialValue() {
				final LocalLogEntry logEntry = 
					new LocalLogEntry(Thread.currentThread(), 
						bufferSize, 
						DLoggerServiceImpl.this);
				return logEntry;
			}
		};
		
		executorService = Executors.newFixedThreadPool(appenders.length);
		disruptor = new Disruptor<DLogEntryItem>(new EventFactory<DLogEntryItem>() {
			int i = 0;
			@Override
			public DLogEntryItem newInstance() {
				buffer.limit((i + 1) * bufferSize - 1);
				buffer.position(i * bufferSize);
				i++;
				final ByteBuffer subBuffer = buffer.slice();
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
	}

	private LogLevel initLevel(final DAppender... appenders) {
		LogLevel level = LogLevel.ERROR;
		for (int i = 0; i < appenders.length; i++) {
			final LogLevel l = appenders[i].getLogLevel();
			level = level.compareTo(l) <= 0 ? level : l;
		}
		return level;
	}

	@Override
	public LogEntry log(final LogLevel level, final String name, final String className){
		final LocalLogEntry entry = logEntryThreadLocal.get();
		entry.setLogLevel(level);
		entry.setName(name);
		entry.setClassName(className);
		entry.getBuffer().clear();
		return entry;
	}

	@Override
	public void entryFlushed(LocalLogEntry localEntry) {
		
		//final long time0 = System.nanoTime();
		long sequence = ringBuffer.next();
		final DLogEntryItem entry = ringBuffer.get(sequence);
		//final long time1 = System.nanoTime();
		
		entry.setName(localEntry.getName());
		entry.setClassName(localEntry.getClassName());
		entry.setLogLevel(localEntry.getLogLevel());
		entry.setThreadName(localEntry.getThreadName());
		entry.setTimestamp(System.currentTimeMillis());
		final CharBuffer buffer = entry.getBuffer();
		buffer.clear();
		buffer.put(localEntry.getBuffer()).flip();
		
		
		entry.setSequenceId(sequence);
		
		ringBuffer.publish(sequence);
		
		/*/
		final long time2 = System.nanoTime();
		{
			final MutableLong mutableLong = acq.get();
			mutableLong.set(mutableLong.get() + time1 - time0);
		}
		/*/
		//*/
		
		/*/
		final long time1 = System.nanoTime();
		{
			final MutableLong mutableLong = commit.get();
			mutableLong.set(mutableLong.get() + time2 - time1);
		}
		/*/
		//*/
		//System.out.println(sequenceId);
	}

	public final ThreadLocal<MutableLong> acq = new ThreadLocal<MutableLong>(){
		@Override
		protected MutableLong initialValue() {
			return new MutableLong();
		}
	};
	
	public final ThreadLocal<MutableLong> commit = new ThreadLocal<MutableLong>(){
		@Override
		protected MutableLong initialValue() {
			return new MutableLong();
		}
	};
	
	@Override
	public LogLevel getLevel() {
		return level;
	}

	@Override
	public void stop(){
		disruptor.halt();
		flush();
		executorService.shutdown();
	}

	void flush() {
		for(int i = 0; i < appenders.length; i++){
			appenders[i].flush();
		}
	}
	
	private int roundUpNextPower2(int x) {
		// HD, Figure 3-3
		x = x - 1; 
		x = x | (x >> 1); 
		x = x | (x >> 2); 
		x = x | (x >> 4); 
		x = x | (x >> 8); 
		x = x | (x >>16); 
		return x + 1; 
	}

}
