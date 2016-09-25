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

package org.gflogger;

import java.nio.ByteBuffer;

import org.gflogger.formatter.BufferFormatter;

import static org.gflogger.formatter.BufferFormatter.allocate;

/**
 * ByteBufferLocalLogEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class ByteBufferLocalLogEntry extends AbstractBufferLocalLogEntry {

	public ByteBufferLocalLogEntry(
		final int maxMessageSize,
		final ObjectFormatterFactory formatterFactory,
		final LoggerService loggerService,
		final FormattingStrategy strategy
	) {
		this(Thread.currentThread(), maxMessageSize, formatterFactory, loggerService, strategy);
	}

	public ByteBufferLocalLogEntry(
		final Thread owner,
		final int maxMessageSize,
		final ObjectFormatterFactory formatterFactory,
		final LoggerService loggerService,
		final FormattingStrategy strategy
	) {
		this(owner, allocate(maxMessageSize), formatterFactory, loggerService, strategy);
	}

	public ByteBufferLocalLogEntry(
		final Thread owner,
		final ByteBuffer byteBuffer,
		final ObjectFormatterFactory formatterFactory,
		final LoggerService loggerService,
		final FormattingStrategy strategy
	) {
		super(owner, formatterFactory, loggerService, byteBuffer,strategy);
	}

	public ByteBufferLocalLogEntry(
		final Thread owner,
		final ByteBuffer byteBuffer,
		final ObjectFormatterFactory formatterFactory,
		final LoggerService loggerService,
		String logErrorsMsg,
		final FormattingStrategy strategy
	) {
		super(owner, formatterFactory, loggerService, logErrorsMsg, byteBuffer, strategy);
	}

	@Override
	public void clear() {
		byteBuffer.clear();
	}

	@Override
	public <T extends java.nio.Buffer> void copyTo(T buffer) {
		buffer.clear();
		((ByteBuffer)buffer).put(this.byteBuffer);
	}

	@Override
	protected void moveAndAppendSilent(String message) {
		final int length = message.length();
		final int remaining = byteBuffer.remaining();
		if (remaining < length) {
			byteBuffer.position(byteBuffer.position() - (length - remaining));
		}
		try {
			BufferFormatter.append(byteBuffer, message);
		} catch (Throwable e) {
		}
	}

	@Override
	public ByteBufferLocalLogEntry append(final char c) {
		checkIfCommitted();
		try {
			BufferFormatter.append(byteBuffer, c);
		} catch (Throwable e) {
			error("append(char c)", e);
		}
		return this;
	}

	@Override
	public ByteBufferLocalLogEntry append(final CharSequence csq) {
		checkIfCommitted();
		try {
			BufferFormatter.append(byteBuffer, csq);
		} catch (Throwable e) {
			error("append(CharSequence csq)", e);
		}
		return this;
	}

	@Override
	public ByteBufferLocalLogEntry append(final CharSequence csq, final int start, final int end) {
		checkIfCommitted();
		try {
			BufferFormatter.append(byteBuffer, csq, start, end);
		} catch (Throwable e) {
			error("append(CharSequence csq, int start, int end)", e);
		}
		return this;
	}

	@Override
	public GFLogEntry append(final boolean b) {
		checkIfCommitted();
		try {
			BufferFormatter.append(byteBuffer, b);
		} catch (Throwable e) {
			error("append(boolean b)", e);
		}
		return this;
	}

	@Override
	public ByteBufferLocalLogEntry append(final int i) {
		checkIfCommitted();
		try {
			BufferFormatter.append(byteBuffer, i);
		} catch (Throwable e) {
			error("append(int i)", e);
		}
		return this;
	}

	@Override
	public GFLogEntry append(final long i) {
		checkIfCommitted();
		try {
			BufferFormatter.append(byteBuffer, i);
		} catch (Throwable e) {
			error("append(long i)", e);
		}
		return this;
	}

	@Override
	public GFLogEntry append(final double i, final int precision) {
		checkIfCommitted();
		try {
			BufferFormatter.append(byteBuffer, i, precision);
		} catch (Throwable e) {
			error("append(double i, int precision)", e);
		}
		return this;
	}

	@Override
	protected void commit0() {
		byteBuffer.flip();
	}

	@Override
	public String stringValue() {
		final int pos = byteBuffer.position();
		final int limit = byteBuffer.limit();
		byteBuffer.flip();
		final byte[] bs = new byte[pos];
		byteBuffer.get(bs);
		byteBuffer.position(pos);
		byteBuffer.limit(limit);
		return new String(bs);
	}

	@Override
	public String toString() {
		return "[local of " + threadName
			+ " " + logLevel
			+ " pos:" + byteBuffer.position()
			+ " limit:" + byteBuffer.limit()
			+ " capacity:" + byteBuffer.capacity()
			+ "]";
	}

}
