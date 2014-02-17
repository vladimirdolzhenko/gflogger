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

import static org.gflogger.formatter.BufferFormatter.allocate;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.gflogger.formatter.BufferFormatter;

/**
 * CharBufferLocalLogEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class CharBufferLocalLogEntry extends AbstractBufferLocalLogEntry {

	private final CharBuffer buffer;

	public CharBufferLocalLogEntry(final int maxMessageSize,
			ObjectFormatterFactory formatterFactory,
			final LoggerService loggerService) {
		this(Thread.currentThread(), maxMessageSize, formatterFactory, loggerService);
	}

	public CharBufferLocalLogEntry(final Thread owner, final int maxMessageSize,
			ObjectFormatterFactory formatterFactory,
			final LoggerService loggerService) {
		this(owner, allocate(maxMessageSize), formatterFactory, loggerService);
	}

	public CharBufferLocalLogEntry(final Thread owner, final ByteBuffer byteBuffer,
			ObjectFormatterFactory formatterFactory, final LoggerService loggerService) {
		super(owner, formatterFactory, loggerService, byteBuffer);
		this.buffer = byteBuffer.asCharBuffer();
	}

	@Override
	public void clear() {
		buffer.clear();
	}

	@Override
	public <T extends java.nio.Buffer> void copyTo(T buffer) {
		buffer.clear();
		((CharBuffer)buffer).put(this.buffer);
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
			// ignore
		}
	}

	@Override
	public CharBufferLocalLogEntry append(final char c) {
		checkIfCommitted();
		try {
			buffer.append(c);
		} catch (Throwable e){
			error("append(char c)", e);
		}
		return this;
	}

	@Override
	public CharBufferLocalLogEntry append(final CharSequence csq) {
		checkIfCommitted();
		try{
			BufferFormatter.append(buffer, csq);
		} catch (Throwable e){
			error("append(CharSequence csq)", e);
		}
		return this;
	}

	@Override
	public CharBufferLocalLogEntry append(final CharSequence csq, final int start, final int end) {
		checkIfCommitted();
		try{
			BufferFormatter.append(buffer, csq, start, end);
		} catch (Throwable e){
			error("append(CharSequence csq, int start, int end)", e);
		}
		return this;
	}

	@Override
	public GFLogEntry append(final boolean b) {
		checkIfCommitted();
		try{
			BufferFormatter.append(buffer, b);
		} catch (Throwable e){
			error("append(boolean b)", e);
		}
		return this;
	}

	@Override
	public CharBufferLocalLogEntry append(final int i){
		checkIfCommitted();
		try{
			BufferFormatter.append(buffer, i);
		} catch (Throwable e){
			error("append(int i)", e);
		}
		return this;
	}

	@Override
	public GFLogEntry append(final long i) {
		checkIfCommitted();
		try{
			BufferFormatter.append(buffer, i);
		} catch (Throwable e){
			error("append(long i)", e);
		}
		return this;
	}

	@Override
	public GFLogEntry append(final double i, final int precision) {
		checkIfCommitted();
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
	public String stringValue() {
		final int pos = buffer.position();
		final int limit = buffer.limit();
		buffer.flip();
		final char[] bs = new char[pos];
		buffer.get(bs);
		buffer.position(pos);
		buffer.limit(limit);
		return new String(bs);
	}

	@Override
	public String toString() {
		return "[local of " + threadName +
			" " + logLevel +
			" pos:" + byteBuffer.position() +
			" limit:" + byteBuffer.limit() +
			" capacity:" + byteBuffer.capacity() +
			"]";
	}

}
