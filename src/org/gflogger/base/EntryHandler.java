package org.gflogger.base;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gflogger.base.appender.Appender;
import org.gflogger.helpers.LogLog;
import org.gflogger.ring.EntryProcessor;
import org.gflogger.ring.MutableLong;
import org.gflogger.ring.PaddedAtomicLong;
import org.gflogger.ring.RingBuffer;
import org.gflogger.ring.RingBufferAware;

public class EntryHandler implements Runnable, EntryProcessor, RingBufferAware<LogEntryItemImpl> {

	protected final AtomicBoolean running = new AtomicBoolean();

	protected final Appender[] appenders;

	protected RingBuffer<LogEntryItemImpl> ringBuffer;

	// runtime changing properties

	protected final PaddedAtomicLong cursor = new PaddedAtomicLong(RingBuffer.INITIAL_CURSOR_VALUE);

	protected final ThreadLocal<MutableLong> idxLocal = new ThreadLocal<MutableLong>(){

		@Override
		protected MutableLong initialValue() {
			return new MutableLong(RingBuffer.INITIAL_CURSOR_VALUE);
		}
	};

	protected boolean immediateFlush = false;
	protected int bufferedIOThreshold = 1000;
	protected long awaitTimeout = 500L;

	public EntryHandler(Appender[] appenders) {
		this.appenders = appenders;
	}

	@Override
	public final long getSequence() {
		return cursor.get();
	}

	@Override
	public void run() {
		LogLog.debug(Thread.currentThread().getName() + " is started.");
		final MutableLong idx = idxLocal.get();
		long loopCounter = 0;
		do{

			long maxIndex;
			try {
				maxIndex = ringBuffer.waitFor(idx.get() + 1, awaitTimeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				break;
			}

		 // handle all available changes in a row
			while ((maxIndex > idx.get()) || ((maxIndex = ringBuffer.getCursor()) > idx.get())){
				final LogEntryItemImpl entry = ringBuffer.get(idx.get() + 1);

				try {
					long mask = entry.getAppenderMask();
					int appenderIdx = 0;
					while(mask != 0L){
						if ((mask & 1L) != 0L){
							try {
								appenders[appenderIdx].process(entry);
							} catch (Throwable e){
								// TODO:
							}
						}
						appenderIdx++;
						mask >>= 1;
					}
				} finally {
					// release entry anyway
					final long id = idx.get();
					final long leftIdx = cursor.get();
					if (leftIdx < id){
						cursor.set(id);
					}
					idx.set(id + 1);
				}

				if (immediateFlush){
					flushBuffer(false);
					loopCounter = 0;
				}

			}

			if (loopCounter > bufferedIOThreshold){
				flushBuffer();
				loopCounter = 0;
			}

			loopCounter++;
		} while(running.get() && !Thread.interrupted());
		workerIsAboutToFinish();
		LogLog.debug(Thread.currentThread().getName() + " is finished.");
	}

	@Override
	public final void setRingBuffer(RingBuffer<LogEntryItemImpl> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	protected final void workerIsAboutToFinish() {
		for(int i = 0; i < appenders.length; i++){
			appenders[i].workerIsAboutToFinish();
		}
	}

	protected final void flushBuffer() {
		flushBuffer(true);
	}

	protected final void flushBuffer(boolean force) {
		for(int i = 0; i < appenders.length; i++){
			appenders[i].flush(force);
		}
	}

	public void start() {
		if (running.getAndSet(true)) throw new IllegalStateException();

		for(int i = 0; i < appenders.length; i++){
			appenders[i].start();
		}
	}

	public void stop(){
		if (!running.getAndSet(false)) return;

		for(int i = 0; i < appenders.length; i++){
			appenders[i].stop();
		}
	}

}