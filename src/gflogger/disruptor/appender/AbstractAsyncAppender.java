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

package gflogger.disruptor.appender;

import static gflogger.formatter.BufferFormatter.allocate;

import gflogger.Layout;
import gflogger.LogLevel;
import gflogger.disruptor.DLogEntryItem;
import gflogger.helpers.LogLog;

import java.nio.CharBuffer;

/**
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public abstract class AbstractAsyncAppender implements DAppender {

	// inner thread buffer
	protected final CharBuffer charBuffer;

	protected LogLevel logLevel = LogLevel.ERROR;
	protected Layout layout;
	protected boolean immediateFlush = false;
	protected int bufferedIOThreshold = 50;
	protected long awaitTimeout = 100;

	public AbstractAsyncAppender() {
		// 4M
		this(1 << 22);
	}

	public AbstractAsyncAppender(final int bufferSize) {
		// unicode char has 2 bytes
		charBuffer = allocate(bufferSize << 1).asCharBuffer();
		charBuffer.clear();
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
	public void onEvent(DLogEntryItem event, long sequence, boolean endOfBatch)
			throws Exception {
		//System.out.println(">" + event.getSequenceId() + " " + sequence + " " + endOfBatch);
		// handle entry that has a log level equals or higher than required
		final LogLevel entryLevel = event.getLogLevel();
		assert entryLevel != null;
		final boolean hasProperLevel = logLevel.compareTo(entryLevel) >= 0;
		if (hasProperLevel) {
			// it could be on different threads
			synchronized (charBuffer) {
				formatMessage(event);
				processCharBuffer();

				if (immediateFlush) {
					flushCharBuffer();
				}
			}
		}
	}
	
	@Override
	public void flush() {
		try{
			// it could be on different threads
			synchronized (charBuffer) {
				flushCharBuffer();
			}
		} catch (RuntimeException e){
			LogLog.error("[" + Thread.currentThread().getName() +  
				"] exception at " + getName() + " - " + e.getMessage(), e);
		}
	}

	protected void processCharBuffer() {
		// empty

	}

	protected void flushCharBuffer(){
		// empty
	}

	protected void formatMessage(DLogEntryItem entry) {
		//System.out.println("formatMessage:" + entry.getSequenceId());
		final CharBuffer buffer = entry.getBuffer();
		synchronized (buffer) {
			final int position = buffer.position();
			final int limit = buffer.limit();

			try { 
				layout.format(charBuffer, entry);
			} catch (RuntimeException e){
				LogLog.error("[" + Thread.currentThread().getName() 
					+ "] exception at " + getName() + " pos: " + 
					position + ", limit:" + limit +
					" - " + e.getMessage(), e);
			}
	
			buffer.position(position);
			buffer.limit(limit);
		}
		//System.out.println("formatedMessage:" + entry.getSequenceId());
	}


	protected abstract String getName();
	
	@Override
	public void onStart() {
		LogLog.debug("[" + Thread.currentThread().getName() + "] " + 
			getName() + " is starting");
	}

	@Override
	public void onShutdown() {
		LogLog.debug("[" + Thread.currentThread().getName() + "] " + 
			getName() + " is about to shutdown");
		immediateFlush = true;
		flushCharBuffer();
	}
}
