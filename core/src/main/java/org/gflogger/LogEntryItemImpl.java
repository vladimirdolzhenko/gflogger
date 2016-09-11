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

import org.gflogger.formatter.BufferFormatter;
import org.gflogger.ring.Publishable;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import static org.gflogger.formatter.BufferFormatter.allocate;

/**
 * LogEntryItemImpl
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class LogEntryItemImpl extends AbstractLocalLogEntry implements LogEntryItem, Publishable {

	private final ByteBuffer buffer;
	private final CharBuffer charBuffer;

	private volatile boolean published;

	private String categoryName;
	private LogLevel logLevel;
	private long timestamp;
	private String threadName;
	private long appenderMask;
	private long	sequence;

	public LogEntryItemImpl(final int size, final LoggingStrategy strategy) {
		this(size, false, strategy);
	}

	public LogEntryItemImpl(final int size, final boolean multibyte,final LoggingStrategy strategy) {
		this(allocate(size), multibyte,strategy);
	}

	public LogEntryItemImpl(final ByteBuffer buffer, final boolean multibyte,final LoggingStrategy strategy) {
		this(null, null, buffer, multibyte,strategy);
	}

	public LogEntryItemImpl(final ObjectFormatterFactory formatterFactory,
			final LoggerService loggerService,
			final ByteBuffer buffer, final boolean multibyte,final LoggingStrategy strategy) {
		super(formatterFactory, loggerService, null, strategy);
		this.buffer = buffer;
		this.charBuffer = multibyte ? buffer.asCharBuffer() : null;
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}

	@Override
	public void setLogLevel(final LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public String getCategoryName() {
		return categoryName;
	}

	@Override
	public String getThreadName() {
		return threadName;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public ByteBuffer getBuffer() {
		return buffer;
	}

	@Override
	public CharBuffer getCharBuffer() {
		return charBuffer;
	}

	@Override
	public void setCategoryName(String name) {
		this.categoryName = name;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	@Override
	public long getAppenderMask() {
		return appenderMask;
	}

	@Override
	public void setAppenderMask(long appenderMask) {
		this.appenderMask = appenderMask;
	}

	@Override
	public boolean isPublished() {
		return this.published;
	}

	@Override
	public void setPublished(boolean published) {
		this.published = published;
	}

	@Override
	public void clear() {
		buffer.clear();
	}

	@Override
	public <T extends java.nio.Buffer> void copyTo(T buffer) {
		buffer.clear();
		((ByteBuffer)buffer).put(this.buffer);
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
	public LogEntryItemImpl append(final char c) {
		try {
			BufferFormatter.append(buffer, c);
		} catch (Throwable e){
			error("append(char c)", e);
		}
		return this;
	}

	@Override
	public LogEntryItemImpl append(final CharSequence csq) {
		try{
			BufferFormatter.append(buffer, csq);
		} catch (Throwable e){
			error("append(CharSequence csq)", e);
		}
		return this;
	}

	@Override
	public LogEntryItemImpl append(final CharSequence csq, final int start, final int end) {
		try{
			BufferFormatter.append(buffer, csq, start, end);
		} catch (Throwable e){
			error("append(CharSequence csq, int start, int end)", e);
		}
		return this;
	}

	@Override
	public LogEntryItemImpl append(final boolean b) {
		try{
			BufferFormatter.append(buffer, b);
		} catch (Throwable e){
			error("append(boolean b)", e);
		}
		return this;
	}

	@Override
	public LogEntryItemImpl append(final int i){
		try{
			BufferFormatter.append(buffer, i);
		} catch (Throwable e){
			error("append(int i)", e);
		}
		return this;
	}

	@Override
	public LogEntryItemImpl append(final long i) {
		try{
			BufferFormatter.append(buffer, i);
		} catch (Throwable e){
			error("append(long i)", e);
		}
		return this;
	}

	@Override
	public LogEntryItemImpl append(final double i, final int precision) {
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
		loggerService.entryFlushed(this);
	}

	@Override
	public String stringValue() {
		final int pos = buffer.position();
		final int limit = buffer.limit();
		buffer.flip();
		final byte[] bs = new byte[pos];
		buffer.get(bs);
		buffer.position(pos);
		buffer.limit(limit);
		return new String(bs);
	}



	@Override
	public String toString() {
		return "[" + logLevel
		+ " pos:" + buffer.position() + " limit:" + buffer.limit() + " capacity:" + buffer.capacity() + "]";
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}

	public long getSequence() {
		return this.sequence;
	}

}
