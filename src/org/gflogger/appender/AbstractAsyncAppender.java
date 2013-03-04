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

package org.gflogger.appender;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.gflogger.Appender;
import org.gflogger.Layout;
import org.gflogger.LogEntryItemImpl;
import org.gflogger.LogLevel;
import org.gflogger.PatternLayout;
import org.gflogger.formatter.BufferFormatter;
import org.gflogger.helpers.LogLog;

import static org.gflogger.formatter.BufferFormatter.allocate;

/**
*
* @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
*/
public abstract class AbstractAsyncAppender implements Appender<LogEntryItemImpl> {

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

	// runtime changing properties

	protected volatile boolean running = false;

	protected final boolean multibyte;

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
	public void process(LogEntryItemImpl entry) {

		if(!!logLevel.greaterThan(entry.getLogLevel())) return;

		if (multibyte) {
			final CharBuffer buffer = entry.getCharBuffer();
			final int position0 = buffer.position();
			final int limit0 = buffer.limit();

			final int position = charBuffer.position();
			final int limit = charBuffer.limit();
			final int size = layout.size(entry);
			if (position + size >= limit){
				flush();
				charBuffer.clear();
			}

			buffer.flip();

			layout.format(charBuffer, entry);

			buffer.limit(limit0).position(position0);

			processCharBuffer();
		} else {
			final ByteBuffer buffer = entry.getBuffer();

			final int position0 = buffer.position();
			final int limit0 = buffer.limit();

			final int position = byteBuffer.position();
			final int limit = byteBuffer.limit();
			final int size = layout.size(entry);
			if (position + size >= limit){
				flush();
				byteBuffer.clear();
			}

			buffer.flip();

			layout.format(byteBuffer, entry);

			buffer.limit(limit0).position(position0);
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

	@Override
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
