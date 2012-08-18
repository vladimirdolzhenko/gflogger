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

package gflogger.base;

import static gflogger.formatter.BufferFormatter.allocate;
import gflogger.LogEntryItem;
import gflogger.LogLevel;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * LogEntryItemImpl
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class LogEntryItemImpl implements LogEntryItem {

	private final ByteBuffer buffer;
	private final CharBuffer charBuffer;

	private String categoryName;
	private LogLevel logLevel;
	private long timestamp;
	private String threadName;

	public LogEntryItemImpl(final int size) {
		this(size, false);
	}

	public LogEntryItemImpl(final int size, final boolean multibyte) {
		this(allocate(size), multibyte);
	}

	public LogEntryItemImpl(final ByteBuffer buffer, final boolean multibyte) {
		this.buffer = buffer;
		this.charBuffer = multibyte ? buffer.asCharBuffer() : null;
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}

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
	public String toString() {
		return "[" + logLevel
		+ " pos:" + buffer.position() + " limit:" + buffer.limit() + " capacity:" + buffer.capacity() + "]";
	}

}
