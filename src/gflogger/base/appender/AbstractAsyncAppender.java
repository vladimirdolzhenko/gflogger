package gflogger.base.appender;

import gflogger.Layout;
import gflogger.LogLevel;
import gflogger.PatternLayout;
import gflogger.base.LogEntryItemImpl;
import gflogger.base.RingBuffer;
import gflogger.util.MutableLong;
import gflogger.util.Sequence;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.locks.LockSupport;

public abstract class AbstractAsyncAppender implements Appender<LogEntryItemImpl>, Runnable {

	protected final Object lock;
	// inner thread buffer
	protected final CharBuffer charBuffer;

	protected LogLevel logLevel = LogLevel.ERROR;
	protected Layout layout;
	protected boolean immediateFlush = false;
	protected int bufferedIOThreshold = 100;
	protected long awaitTimeout = 10L;
	protected RingBuffer<LogEntryItemImpl> ringBuffer;
	
	// runtime changing properties
	
	protected volatile boolean running = false;

	protected final Sequence leftIndex = new Sequence(-1);
	protected final Sequence rightIndex = new Sequence(-1);

	protected final ThreadLocal<MutableLong> idxLocal = new ThreadLocal<MutableLong>(){
	  @Override
		protected MutableLong initialValue() {
			return new MutableLong(-1);
		}  
	};
	
	public AbstractAsyncAppender() {
		// 4M
		this(1 << 22);
	}

	public AbstractAsyncAppender(final int sizeOfBuffer) {
		charBuffer = ByteBuffer.allocateDirect(sizeOfBuffer).asCharBuffer();
		lock = new Object();
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}

	public synchronized void setLogLevel(final LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public void setLayout(final Layout layout) {
		this.layout = layout;
	}

	public void setImmediateFlush(final boolean immediateFlush) {
		this.immediateFlush = immediateFlush;
	}

	public void setBufferedIOThreshold(final int bufferedIOThreshold) {
		this.bufferedIOThreshold = bufferedIOThreshold;
	}

	public void setAwaitTimeout(final long awaitTimeout) {
		this.awaitTimeout = awaitTimeout;
	}
	
	@Override
	public long getMaxReleased() {
		return leftIndex.get();
	}

	@Override
	public void run() {
		System.out.println(Thread.currentThread().getName() + " is started.");
		int loopCounter = 0;
		final MutableLong idx = idxLocal.get();
		do{
			// handle all available changes in a row
			long localMaxIndex = rightIndex.get();
			while (localMaxIndex > idx.get() || ((localMaxIndex = rightIndex.get()) > idx.get())){
				// handle if entry has not been processed yet. 
				while(idx.get() < localMaxIndex){
					final LogEntryItemImpl entry = ringBuffer.get(idx.get() + 1);
					// handle entry that has a log level equals or higher than required
					final boolean hasProperLevel =
						logLevel.compareTo(entry.getLogLevel()) >= 0;
						if (hasProperLevel){
							formatMessage(entry);
						}

						// release entry anyway
						releaseEntry(entry, idx);

						if (hasProperLevel){
							processCharBuffer();

							if (immediateFlush){
								flushCharBuffer();
								loopCounter = 0;
							}
						}
				}
			}

			if (immediateFlush || loopCounter > bufferedIOThreshold){
				flushCharBuffer();
				loopCounter = 0;
				
				if (!immediateFlush) {
					// to eliminate cpu-burned busy spin
					//*/
					synchronized (lock) {
						try {
							lock.wait(awaitTimeout);
						} catch (final InterruptedException e) {
							// nothing
						}
					}
					/*/
					LockSupport.parkNanos(1000L);
					//*/
				}
			}

			loopCounter++;
	   } while((running || rightIndex.get() > idx.get()) && !Thread.interrupted());
		workerIsAboutToFinish();
		System.out.println(Thread.currentThread().getName() + " is finished.");
	}

	protected void processCharBuffer(){
		// empty
	}

	protected void flushCharBuffer(){
		// empty
	}

	protected void workerIsAboutToFinish(){
		flushCharBuffer();
	}

	@Override
	public void entryFlushed(final LogEntryItemImpl entry) {
		final long id = entry.getId();
		final long expectedId = id - 1;
		while(!rightIndex.compareAndSet(expectedId, id)){
			// busy-spin on await index
		}
	}
	

	protected void formatMessage(final LogEntryItemImpl entry) {
		final CharBuffer buffer = entry.getBuffer();
		synchronized (buffer) {
			final int position = buffer.position();
			final int limit = buffer.limit();

			layout.format(charBuffer, entry);

			buffer.position(position);
			buffer.limit(limit);
		}
	}

	protected void releaseEntry(final LogEntryItemImpl entry, final MutableLong idx) {
		final long id = idx.get();
		final long leftIdx = leftIndex.get();
		if (leftIdx < id){
			leftIndex.set(id);
		}
		idx.set(id + 1);
	}

	protected abstract String name();

	@Override
	public void start(final RingBuffer<LogEntryItemImpl> ringBuffer) {
		if (running)
			throw new IllegalStateException();
		
		running = true;

		// just fence
		synchronized(lock){
			if (layout == null){
				layout = new PatternLayout();
			}

			this.ringBuffer = ringBuffer;
		}

		final Thread thread = new Thread(this, name() + "-logger");
		thread.start();
	}

	@Override
	public void stop(){
		if (!running)
			throw new IllegalStateException();
		System.out.println(name() + " stop ");
		running = false;
		synchronized (lock) {
			lock.notifyAll();
		}
	}
}
