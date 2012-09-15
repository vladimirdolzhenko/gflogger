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

import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.gflogger.formatter.Bytes;

/**
 * ByteLocalLogEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class ByteLocalLogEntry extends AbstractLocalLogEntry {

	private final Bytes	bytes;

	public ByteLocalLogEntry(final int maxMessageSize,
		final ObjectFormatterFactory formatterFactory,
		final LoggerService loggerService) {
		this(Thread.currentThread(), maxMessageSize, formatterFactory, loggerService);
	}

	public ByteLocalLogEntry(final Thread owner, final int maxMessageSize,
			final ObjectFormatterFactory formatterFactory,
			final LoggerService loggerService) {
		this(owner,maxMessageSize, formatterFactory, loggerService, null);
	}

	public ByteLocalLogEntry(final Thread owner, final int maxMessageSize,
		final ObjectFormatterFactory formatterFactory,
		final LoggerService loggerService, String logErrorsMsg) {
		super(owner, formatterFactory, loggerService, logErrorsMsg);
		this.bytes = new Bytes(maxMessageSize);
	}

	@Override
	public <T extends Buffer> void copyTo(T buffer) {
		buffer.clear();
		bytes.copyTo((ByteBuffer) buffer);
	}

	@Override
	public void clear() {
		bytes.clear();
	}

	@Override
	public String stringValue() {
		return bytes.asString();
	}

	@Override
	public LogEntry append(char c) {
		try {
			bytes.put(c);
		} catch (Throwable e){
			error("append(char c)", e);
		}
		return this;
	}

	@Override
	public LogEntry append(CharSequence csq) {
		try{
			bytes.put(csq);
		} catch (Throwable e){
			error("append(CharSequence csq)", e);
		}
		return this;
	}

	@Override
	public LogEntry append(CharSequence csq, int start, int end) {
		try {
			bytes.put(csq, start, end);
		} catch (Throwable e){
			error("append(CharSequence csq, int start, int end)", e);
		}
		return this;
	}

	@Override
	public LogEntry append(boolean b) {
		try{
			bytes.put(b);
		} catch (Throwable e){
			error("append(boolean b)", e);
		}
		return this;
	}

	@Override
	public LogEntry append(int i) {
		try{
			bytes.put(i);
		} catch (Throwable e){
			error("append(int i)", e);
		}
		return this;
	}

	@Override
	public LogEntry append(long i) {
		try{
			bytes.put(i);
		} catch (Throwable e){
			error("append(long i)", e);
		}
		return this;
	}

	@Override
	public LogEntry append(double i, int precision) {
		try{
			bytes.put(i, precision);
		} catch (Throwable e){
			error("append(double i, int precision)", e);
		}
		return this;
	}

	@Override
	protected void moveAndAppendSilent(String message) {
		final int length = message.length();
		final int remaining = bytes.remaining();
		if (remaining < length){
			bytes.position(bytes.position() - (length - remaining));
		}
		try {
			bytes.put(message);
		} catch (Throwable e){
		}
	}

	@Override
	protected void commit0() {
		// nothing
	}

}
