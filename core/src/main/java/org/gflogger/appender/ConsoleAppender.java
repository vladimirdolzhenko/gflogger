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


import java.io.Flushable;
import java.io.IOException;

import org.gflogger.LogLevel;
import org.gflogger.helpers.LogLog;

/**
 * ConsoleAppender
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class ConsoleAppender extends AbstractAsyncAppender {

	private static final String NAME = "console";

	private final Appendable out;
	private final Flushable flushable;

	public ConsoleAppender(
		final LogLevel logLevel,
		final boolean enabled
	) {
		this(/*multibyte=*/false, logLevel, enabled );
	}

	public ConsoleAppender(
		final boolean multibyte,
		final LogLevel logLevel,
		final boolean enabled
	) {
		this( multibyte, logLevel, enabled, System.out );
	}

	public ConsoleAppender(
		final int bufferSize,
		final boolean multibyte,
		final LogLevel logLevel,
		final boolean enabled
	) {
		this(bufferSize, multibyte, logLevel, enabled, System.out);
	}

	public ConsoleAppender(
		final boolean multibyte,
		final LogLevel logLevel,
		final boolean enabled,
		final Appendable out
	) {
		super(NAME, multibyte, logLevel, enabled);
		this.out = out;
		this.flushable =  (out instanceof Flushable) ? (Flushable) out : null;
	}

	public ConsoleAppender(
		final int bufferSize,
		final boolean multibyte,
		final LogLevel logLevel,
		final boolean enabled,
		final Appendable out
	) {
		super(NAME,bufferSize,multibyte,logLevel, enabled);
		this.out = out;
		this.flushable = (out instanceof Flushable) ? (Flushable) out : null;
	}

	@Override
	protected void processCharBuffer() {
		flush();
	}

	@Override
	public void flush(boolean force) {
		if (!(force || immediateFlush)) return;
		if (multibyte) {
			if (charBuffer.position() > 0) {
				charBuffer.flip();
				try {
					while (charBuffer.hasRemaining()) {
						out.append(charBuffer.get());
					}

					if (flushable != null) flushable.flush();
				} catch (IOException e) {
					LogLog.error("[" + Thread.currentThread().getName()
						+ "] exception at " + getName() + " - " + e.getMessage(), e);
				} finally {
					charBuffer.clear();
				}
			}
		} else {
			if (byteBuffer.position() > 0) {
				byteBuffer.flip();
				try {
					while (byteBuffer.hasRemaining()) {
						out.append((char) byteBuffer.get());
					}

					if (flushable != null) flushable.flush();
				} catch (IOException e) {
					LogLog.error("[" + Thread.currentThread().getName()
						+ "] exception at " + getName() + " - " + e.getMessage(), e);
				} finally {
					byteBuffer.clear();
				}
			}
		}
	}

	@Override
	public String getName() {
		return "console";
	}
}
