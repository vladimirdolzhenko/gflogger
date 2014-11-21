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

import java.util.concurrent.TimeUnit;

import org.gflogger.AbstractEntryHandler;
import org.gflogger.Appender;
import org.gflogger.LogEntryItemImpl;
import org.gflogger.State;
import org.gflogger.appender.AbstractAsyncAppender;
import org.gflogger.helpers.LogLog;
import org.gflogger.ring.*;

/**
 * EntryHandler
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class EntryHandler extends AbstractEntryHandler<LoggerServiceImpl> implements EntryProcessor, RingBufferAware<LogEntryItemImpl> {

	protected RingBuffer<LogEntryItemImpl> ringBuffer;

	// runtime changing properties

	protected final PaddedAtomicLong cursor = new PaddedAtomicLong(RingBuffer.INITIAL_CURSOR_VALUE);

	protected boolean immediateFlush = false;
	protected int bufferedIOThreshold = 10000;
	protected long awaitTimeout = 10L;

	public EntryHandler(LoggerServiceImpl service, Appender[] appenders) {
		super(service, appenders);
		for (Appender appender : appenders) {
			if (appender instanceof AbstractAsyncAppender) {
				immediateFlush |= ((AbstractAsyncAppender) appender).isImmediateFlush();
			}
		}
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
		while(true) {
			try {
				long maxIndex =
					/*/
					ringBuffer.waitFor(idx + 1);
					/*/
						ringBuffer.waitFor(idx + 1, awaitTimeout, TimeUnit.MILLISECONDS);
				//*/

				// handle all available changes in a row
				while (maxIndex > idx) {
					final LogEntryItemImpl entry = ringBuffer.get(idx + 1);

					assert entry.isPublished();

					try {
						process(entry);
					} finally {
						// release entry anyway
						entry.setPublished(false);
						cursor.lazySet(idx);
						idx++;
					}

					if (immediateFlush) {
						flushBuffer(false);
						loopCounter = 0;
					}

				}

				if (loopCounter > bufferedIOThreshold) {
					flushBuffer();
					loopCounter = 0;
				}

				loopCounter++;
			} catch (InterruptedException e) {
				//
			} catch (AlertException e) {
				if (service.getState() == State.STOPPED) {
					break;
				}
			} catch (Throwable e) {
				LogLog.error("Unhandled exception " + e.getMessage() + " at " + Thread.currentThread().getName(), e);
			}
		}
		stop();
		LogLog.debug(Thread.currentThread().getName() + " is finished. ");
	}

	@Override
	public final void setRingBuffer(RingBuffer<LogEntryItemImpl> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

}