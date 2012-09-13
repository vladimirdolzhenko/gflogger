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

package org.gflogger.base.appender;

import static org.gflogger.formatter.BufferFormatter.allocate;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.TimeUnit;

import org.gflogger.Layout;
import org.gflogger.LogLevel;
import org.gflogger.PatternLayout;
import org.gflogger.base.LogEntryItemImpl;
import org.gflogger.formatter.BufferFormatter;
import org.gflogger.helpers.LogLog;
import org.gflogger.ring.MutableLong;
import org.gflogger.ring.PaddedAtomicLong;
import org.gflogger.ring.RingBuffer;
import org.gflogger.ring.RingBufferAware;

public abstract class AbstractAsyncAppender implements Appender<LogEntryItemImpl>, RingBufferAware<LogEntryItemImpl> {

	// inner thread buffer
	protected final CharBuffer charBuffer;
	protected final ByteBuffer byteBuffer;

	protected LogLevel logLevel = LogLevel.TRACE;
	protected Layout layout;
	protected boolean immediateFlush = false;
	protected int bufferedIOThreshold = 100;
	protected long awaitTimeout = 10L;
	protected boolean enabled = true;
	protected int index;

	protected RingBuffer<LogEntryItemImpl> ringBuffer;

	// runtime changing properties

	protected volatile boolean running = false;

	protected final PaddedAtomicLong cursor = new PaddedAtomicLong(RingBuffer.INITIAL_CURSOR_VALUE);

	protected final boolean multibyte;

	protected final ThreadLocal<MutableLong> idxLocal = new ThreadLocal<MutableLong>(){

		@Override
		protected MutableLong initialValue() {
			return new MutableLong(RingBuffer.INITIAL_CURSOR_VALUE);
		}
	};

	public AbstractAsyncAppender(final boolean multibyte) {
		// 4M
		this(1 << 22, multibyte);
	}

	public AbstractAsyncAppender(final int bufferSize, final boolean multibyte) {
		this.multibyte = multibyte;
		// unicode char has 2 bytes
		byteBuffer = allocate(multibyte ? bufferSize << 1 : bufferSize);
		byteBuffer.clear();
		charBuffer = multibyte ? allocate(multibyte ? bufferSize << 1 : bufferSize).asCharBuffer() : null;
	}

	@Override
	public boolean isMultibyte() {
		return multibyte;
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

	public void setEnabled(boolean enabled) {
	    this.enabled = enabled;
    }

	@Override
    public int getIndex() {
    	return index;
    }

	public void setIndex(int index) {
    	this.index = index;
    }

	@Override
	public long getSequence() {
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
				// handle entry that has a log level equals or higher than required
				final boolean hasProperLevel = enabled &&
					logLevel.compareTo(entry.getLogLevel()) <= 0;

				try {
					if (hasProperLevel){
						process(entry);
					}
				} finally {
					// release entry anyway
					releaseEntry(entry, idx);
				}

				if (hasProperLevel){
					if (multibyte) {
						processCharBuffer();
					}

					if (immediateFlush){
						flush();
						loopCounter = 0;
					}
				}
			}

			if (loopCounter > bufferedIOThreshold){
				flush();
				loopCounter = 0;
			}

			loopCounter++;
		} while(running && !Thread.interrupted());
		workerIsAboutToFinish();
		LogLog.debug(Thread.currentThread().getName() + " is finished.");
	}

	@Override
	public void process(LogEntryItemImpl entry) {
		if (multibyte) {
			final CharBuffer buffer = entry.getCharBuffer();
			final int position = buffer.position();
            final int limit = buffer.limit();
            final int size = layout.size(entry);
            if (position + size >= limit){
            	flush();
            	charBuffer.clear();
            }

            buffer.flip();

            layout.format(charBuffer, entry);

            buffer.limit(limit).position(position);
		} else {
			final ByteBuffer buffer = entry.getBuffer();
			final int position = buffer.position();
            final int limit = buffer.limit();
            final int size = layout.size(entry);
            if (position + size >= limit){
            	flush();
            	byteBuffer.clear();
            }

            buffer.flip();

            layout.format(byteBuffer, entry);

            buffer.limit(limit).position(position);
		}
	}

	protected void processCharBuffer(){
		// empty
	}

	@Override
    public void workerIsAboutToFinish(){
		flush();
		BufferFormatter.purge(byteBuffer);
		BufferFormatter.purge(charBuffer);
	}

	protected void releaseEntry(final LogEntryItemImpl entry, final MutableLong idx) {
		final long id = idx.get();
		final long leftIdx = cursor.get();
		if (leftIdx < id){
			cursor.set(id);
		}
		idx.set(id + 1);
	}

	@Override
	public void setRingBuffer(RingBuffer<LogEntryItemImpl> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	public void flush(){
		flush(true);
	}

	@Override
	public void start() {
		if (running) throw new IllegalStateException();

		LogLog.debug(getName() + " is starting ");

		if (layout == null){
			layout = new PatternLayout();
		}
		running = true;
	}

	@Override
	public void stop(){
		if (!running) return;
		LogLog.debug(getName() + " is stopping ");
		running = false;
	}
}
