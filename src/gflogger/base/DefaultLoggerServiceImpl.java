package gflogger.base;

import gflogger.LocalLogEntry;
import gflogger.LogEntry;
import gflogger.LogLevel;
import gflogger.LoggerService;
import gflogger.base.appender.Appender;
import gflogger.util.MutableLong;
import gflogger.util.PaddedAtomicLong;
import gflogger.util.Sequence;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.locks.LockSupport;



/**
 * DefaultLoggerServiceImpl is the garbage-free implementation on the top of 
 * own ring buffer implementation and off-heap buffer.
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DefaultLoggerServiceImpl implements LoggerService {

	private final LogLevel level;
	private final Appender[] appenders;
	private final Sequence cursor;
	
	//*/
	private final ThreadLocal<MutableLong> maxIndexThreadLocal;
	/*/
	private final PaddedAtomicLong maxIndexAtomic;
	//*/
	
	private final ThreadLocal<LocalLogEntry> logEntryThreadLocal;

	private final RingBuffer<LogEntryItemImpl> ringBuffer;
	private final int size;

	/**
	 * @param count a number of items in the ring, could be rounded up to the next power of 2
	 * @param bufferSize buffer size of each item in the ring 
	 * @param appenders
	 */
	public DefaultLoggerServiceImpl(final int count, final int bufferSize, final Appender ... appenders) {
		if (appenders.length == 0){
			throw new IllegalArgumentException("Expected at least one appender");
		}
		// flag size restriction
		if (appenders.length > (Integer.SIZE - 1)){
			throw new IllegalArgumentException("Expected less than " + (Integer.SIZE - 1) + " appenders");
		}
		this.appenders = appenders;
		
		this.cursor = new Sequence(0);
		
		final int c = (count & (count - 1)) != 0 ? 
			roundUpNextPower2(count) : count;
		this.ringBuffer = 
			new RingBuffer<LogEntryItemImpl>(initEnties(c, bufferSize));
		this.level = initLogLevel(appenders);
		this.size = this.ringBuffer.size();
		
		//*/
		this.maxIndexThreadLocal = new ThreadLocal<MutableLong>(){
			@Override
			protected MutableLong initialValue() {
				return new MutableLong(c - 1);
			}
		};
		/*/
		this.maxIndexAtomic = new PaddedAtomicLong(c - 1);
		//*/
		
		this.logEntryThreadLocal  = new ThreadLocal<LocalLogEntry>(){
			@Override
			protected LocalLogEntry initialValue() {
				final LocalLogEntry logEntry = 
					new LocalLogEntry(Thread.currentThread(), 
						bufferSize, 
						DefaultLoggerServiceImpl.this);
				return logEntry;
			}
		};
		
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

	private LogEntryItemImpl[] initEnties(int count, final int bufferSize) {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(count * bufferSize);

		final LogEntryItemImpl[] entries = new LogEntryItemImpl[count];
		for (int i = 0; i < count; i++) {
			buffer.limit((i + 1) * bufferSize - 1);
			buffer.position(i * bufferSize);
			final ByteBuffer subBuffer = buffer.slice();
			entries[i] = new LogEntryItemImpl(subBuffer.asCharBuffer());
		}
		return entries;
	}

	private void start(final Appender... appenders) {
		for (int i = 0; i < appenders.length; i++) {
			appenders[i].start(ringBuffer);
		}
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
	
	
	private LogEntryItemImpl log0(){
		//*/
		final MutableLong mutableMaxIndex = maxIndexThreadLocal.get();
		long maxIdx = mutableMaxIndex.get();
		/*/
		long maxIdx = maxIndexAtomic.get();
		//*/
		
		final long localIndex = cursor.getAndIncrement();
		
		for(int i = 0;  ; i++){
			//
			//   maxIndex - size < index <= maxIndex	
			//   
			if (localIndex <= maxIdx){
				final LogEntryItemImpl entry = ringBuffer.get(localIndex);
				entry.setId(localIndex);
				return entry;
			}
			
			//miss.get().set(miss.get().get() + 1);

			while(localIndex > maxIdx){
				long max = maxIdx;
				// find the min of max release
				for (int j = 0; j < appenders.length; j++) {
					// fence reading
					final long maxReleased = appenders[j].getMaxReleased();
					max = max <= maxReleased ? max : maxReleased;
				}
				maxIdx = max + size;
				
				if (localIndex <= maxIdx) break;
				/*/
				final MutableLong mutableLong = park.get();
				park.get().set(mutableLong.get() + 1);
				/*/
				//*/
				LockSupport.parkNanos(1L);
			}

			//*/
			mutableMaxIndex.set(maxIdx);
			/*/
			maxIndexAtomic.set(maxIdx);
			//*/
		}
	}

	@Override
    public void entryFlushed(final LocalLogEntry localEntry){
		//final long time0 = System.nanoTime();
		final LogEntryItemImpl entry = log0();
		entry.setName(localEntry.getName());
		entry.setClassName(localEntry.getClassName());
		entry.setLogLevel(localEntry.getLogLevel());
		entry.setThreadName(localEntry.getThreadName());
		entry.setTimestamp(System.currentTimeMillis());
		final CharBuffer buffer = entry.getBuffer();
		buffer.clear();
		buffer.put(localEntry.getBuffer()).flip();
		//final long time1 = System.nanoTime();
		for (int i = 0; i < appenders.length; i++) {
			appenders[i].entryFlushed(entry);
		}
		/*/
		final long time2 = System.nanoTime();
		{
			final MutableLong mutableLong = acq.get();
			mutableLong.set(mutableLong.get() + time1 - time0);
		}
		{
			final MutableLong mutableLong = commit.get();
			mutableLong.set(mutableLong.get() + time2 - time1);
		}
		/*/
		//*/
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
	
	public final ThreadLocal<MutableLong> park = new ThreadLocal<MutableLong>(){
		@Override
		protected MutableLong initialValue() {
			return new MutableLong();
		}
	};
	
	public final ThreadLocal<MutableLong> miss = new ThreadLocal<MutableLong>(){
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
		for(int i = 0; i < appenders.length; i++){
			appenders[i].stop();
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
