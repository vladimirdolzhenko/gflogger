package org.gflogger.base;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gflogger.base.appender.Appender;
import org.gflogger.helpers.LogLog;
import org.gflogger.ring.AlertException;
import org.gflogger.ring.EntryProcessor;
import org.gflogger.ring.PaddedAtomicLong;
import org.gflogger.ring.RingBuffer;
import org.gflogger.ring.RingBufferAware;

public class EntryHandler implements Runnable, EntryProcessor, RingBufferAware<LogEntryItemImpl> {

	protected final AtomicBoolean running = new AtomicBoolean();

	protected final Appender[] appenders;

	protected RingBuffer<LogEntryItemImpl> ringBuffer;

	// runtime changing properties

	protected final PaddedAtomicLong cursor = new PaddedAtomicLong(RingBuffer.INITIAL_CURSOR_VALUE);

	protected boolean immediateFlush = false;
	protected int bufferedIOThreshold = 10000;
	protected long awaitTimeout = 10L;

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

		long idx = RingBuffer.INITIAL_CURSOR_VALUE;
		long loopCounter = 0;
		try {
			while(true){

				long maxIndex =
					/*/
					ringBuffer.waitFor(idx + 1);
					/*/
					ringBuffer.waitFor(idx + 1, awaitTimeout, TimeUnit.MILLISECONDS);
					//*/

			 // handle all available changes in a row
				while (maxIndex > idx){
					final LogEntryItemImpl entry = ringBuffer.get(idx + 1);

					assert entry.isPublished();

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
						entry.setPublished(false);
						cursor.lazySet(idx);
						idx++;
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
			}
		} catch (InterruptedException e){
			//
		} catch (AlertException e){
			// nothing
		} catch (Throwable e){
			e.printStackTrace();
		}
		workerIsAboutToFinish();
		LogLog.debug(Thread.currentThread().getName() + " is finished. ");
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
			LogLog.debug("going to stop appender " + appenders[i].getName());
			appenders[i].stop();
		}
	}

}