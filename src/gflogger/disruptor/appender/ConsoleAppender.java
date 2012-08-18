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

import gflogger.helpers.LogLog;

import java.io.Flushable;
import java.io.IOException;

/**
 * ConsoleAppender
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class ConsoleAppender extends AbstractAsyncAppender {

	private final Appendable out;
	private final Flushable flushable;

	public ConsoleAppender() {
		this(System.out);
	}

	public ConsoleAppender(final int bufferSize) {
		this(bufferSize, false);
	}

	public ConsoleAppender(final int bufferSize, final boolean multibyte) {
		this(bufferSize, multibyte, System.out);
	}

	public ConsoleAppender(final Appendable out) {
		this.out = out;
		this.flushable =  (out instanceof Flushable) ? (Flushable)out : null;
	}

	public ConsoleAppender(final int bufferSize, final boolean multibyte, final Appendable out) {
		super(bufferSize, multibyte);
		this.out = out;
		this.flushable =  (out instanceof Flushable) ? (Flushable)out : null;
	}

	@Override
	protected void processCharBuffer() {
		flushBuffer();
	}

	@Override
	protected void flushBuffer() {
		if (multibyte) {
			if (charBuffer.position() > 0){
				charBuffer.flip();
				try {
					while(charBuffer.hasRemaining()) out.append(charBuffer.get());
					if (flushable != null) flushable.flush();
				} catch (IOException e){
					LogLog.error("[" + Thread.currentThread().getName() +
						"] exception at " + getName() + " - " + e.getMessage(), e);
				} finally {
					charBuffer.clear();
				}
			}
		} else {
			if (byteBuffer.position() > 0){
				byteBuffer.flip();
				try {
					while(byteBuffer.hasRemaining()) out.append((char) byteBuffer.get());
					if (flushable != null) flushable.flush();
				} catch (IOException e){
					LogLog.error("[" + Thread.currentThread().getName() +
						"] exception at " + getName() + " - " + e.getMessage(), e);
				} finally {
					byteBuffer.clear();
				}
			}
		}
	}

	@Override
	protected String getName() {
		return "console";
	}
}
