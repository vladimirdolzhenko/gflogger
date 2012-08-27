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

package gflogger;

import static gflogger.formatter.BufferFormatter.*;
import gflogger.formatter.BufferFormatter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * ByteBufferLocalLogEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class ByteBufferLocalLogEntry extends AbstractBufferLocalLogEntry {

	private final ByteBuffer buffer;

	public ByteBufferLocalLogEntry(final int maxMessageSize,
		final ObjectFormatterFactory formatterFactory,
		final LoggerService loggerService) {
		this(Thread.currentThread(), maxMessageSize, formatterFactory, loggerService);
	}

	public ByteBufferLocalLogEntry(final Thread owner, final int maxMessageSize,
			final ObjectFormatterFactory formatterFactory,
			final LoggerService loggerService) {
		this(owner, allocate(maxMessageSize), formatterFactory, loggerService);
	}

	public ByteBufferLocalLogEntry(final Thread owner, final ByteBuffer byteBuffer,
		final ObjectFormatterFactory formatterFactory,
		final LoggerService loggerService) {
		super(owner, formatterFactory, loggerService);
		this.buffer = byteBuffer;
	}

	public ByteBufferLocalLogEntry(final Thread owner, final ByteBuffer byteBuffer,
		final ObjectFormatterFactory formatterFactory,
		final LoggerService loggerService, String logErrorsMsg) {
		super(owner, formatterFactory, loggerService, logErrorsMsg);
		this.buffer = byteBuffer;
	}

	@Override
	public CharBuffer getCharBuffer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ByteBuffer getByteBuffer() {
		return buffer;
	}

	@Override
	protected void moveAndAppendSilent(String message) {
		final int length = message.length();
		final int remaining = buffer.remaining();
		if (remaining < length){
			buffer.position(buffer.position() - (length - remaining));
		}
		try {
			BufferFormatter.append(buffer, message);
		} catch (Throwable e){
		}
	}

	@Override
	public ByteBufferLocalLogEntry append(final char c) {
		try {
			BufferFormatter.append(buffer, c);
		} catch (Throwable e){
			error("append(char c)", e);
		}
		return this;
	}

	@Override
	public ByteBufferLocalLogEntry append(final CharSequence csq) {
		try{
			BufferFormatter.append(buffer, csq);
		} catch (Throwable e){
			error("append(CharSequence csq)", e);
		}
		return this;
	}

	@Override
	public ByteBufferLocalLogEntry append(final CharSequence csq, final int start, final int end) {
		try{
			BufferFormatter.append(buffer, csq, start, end);
		} catch (Throwable e){
			error("append(CharSequence csq, int start, int end)", e);
		}
		return this;
	}

	@Override
	public LogEntry append(final boolean b) {
		try{
			BufferFormatter.append(buffer, b);
		} catch (Throwable e){
			error("append(boolean b)", e);
		}
		return this;
	}

	@Override
	public ByteBufferLocalLogEntry append(final int i){
		try{
			BufferFormatter.append(buffer, i);
		} catch (Throwable e){
			error("append(int i)", e);
		}
		return this;
	}

	@Override
	public LogEntry append(final long i) {
		try{
			BufferFormatter.append(buffer, i);
		} catch (Throwable e){
			error("append(long i)", e);
		}
		return this;
	}

	@Override
	public LogEntry append(final double i, final int precision) {
		try{
			BufferFormatter.append(buffer, i, precision);
		} catch (Throwable e){
			error("append(double i, int precision)", e);
		}
		return this;
	}

	@Override
	protected void commit0() {
		buffer.flip();
	}

	@Override
	public String toString() {
		return "[local of " + threadName +
			" " + logLevel +
			" pos:" + buffer.position() +
			" limit:" + buffer.limit() +
			" capacity:" + buffer.capacity() +
			"]";
	}

}
