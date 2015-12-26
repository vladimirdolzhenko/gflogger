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
public abstract class AbstractAsyncAppender extends AbstractAppender {

	// 4M
	private static final int DEFAULT_BUFFER_SIZE = 1 << 22;

	// inner thread buffer
	protected final CharBuffer	charBuffer;
	protected final ByteBuffer	byteBuffer;

	protected Layout			layout;
	protected boolean			immediateFlush		= false;
	protected int				bufferedIOThreshold	= 100;
	protected long				awaitTimeout		= 10L;

	// runtime changing properties

	protected volatile boolean	running				= false;

	protected AbstractAsyncAppender(
		final String name,
		final boolean multibyte,
		final LogLevel logLevel,
		final boolean enabled
	) {
		this(name,
		     DEFAULT_BUFFER_SIZE,
		     multibyte,
		     logLevel, enabled
		);
	}

	protected AbstractAsyncAppender(
		final String name,
		final int bufferSize,
		final boolean multibyte,
		final LogLevel logLevel,
		final boolean enabled
	) {
		super(name, multibyte, logLevel, enabled);
		// unicode char has 2 bytes
		byteBuffer = allocate(multibyte ? bufferSize << 1 : bufferSize);
		byteBuffer.clear();
		charBuffer = multibyte ?
			allocate(bufferSize << 1).asCharBuffer()
			: null;
	}

	public void setLayout(final Layout layout) {
		this.layout = layout;
	}

	public boolean isImmediateFlush() {
		return immediateFlush;
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
	public void process(LogEntryItemImpl entry) {
		if(!enabled || logLevel.greaterThan(entry.getLogLevel())) return;

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
	public void flush(){
		flush(true);
	}

	@Override
	public void start() {
		if (running) throw new IllegalStateException("Already running");

		LogLog.debug(getName() + " is starting ");

		if (layout == null){
			layout = new PatternLayout();
		}
		running = true;
	}

	/**
	 * @deprecated it is about to be removed in future versions, use {@linkplain #stop()}
	 *             instead
	 */
	@Deprecated
	protected void workerIsAboutToFinish(){
		flush();
		BufferFormatter.purge(byteBuffer);
		BufferFormatter.purge(charBuffer);
	}

	@Override
	public void stop(){
		if (!running) return;
		LogLog.debug(getName() + " is stopping ");
		workerIsAboutToFinish();
		running = false;
	}
}
