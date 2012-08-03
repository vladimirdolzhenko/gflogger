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
import static gflogger.util.StackTraceUtils.*;

import gflogger.formatter.BufferFormatter;
import gflogger.helpers.LogLog;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * LocalLogEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class ByteBufferLocalLogEntry implements LocalLogEntry {

	private final String threadName;
	private final ByteBuffer buffer;
	private final LoggerService loggerService;

	private String categoryName;
	private LogLevel logLevel;

	private boolean commited = true;
	private Throwable error;

	public ByteBufferLocalLogEntry(final int maxMessageSize, final LoggerService loggerService) {
		this(Thread.currentThread(), maxMessageSize, loggerService);
	}

	public ByteBufferLocalLogEntry(final Thread owner, final int maxMessageSize, final LoggerService loggerService) {
		this(owner, allocate(maxMessageSize), loggerService);
	}

	public ByteBufferLocalLogEntry(final Thread owner, final ByteBuffer byteBuffer, final LoggerService loggerService) {
		/*
		 * It worth to cache thread categoryName at thread local variable cause
		 * thread.getName() creates new String(char[])
		 */
		this.threadName = owner.getName();
		this.buffer = byteBuffer;
		this.loggerService = loggerService;
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
    public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
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
	public CharBuffer getCharBuffer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ByteBuffer getByteBuffer() {
	    return buffer;
	}

	@Override
    public boolean isCommited() {
	    return this.commited;
    }

	@Override
    public void setCommited(boolean commited) {
	    this.commited = commited;
    }

	@Override
    public Throwable getError() {
	    return this.error;
    }

	@Override
	public ByteBufferLocalLogEntry append(final char c) {
		try {
			BufferFormatter.append(buffer, c);
		} catch (BufferOverflowException e){
			this.error = e;
			// there is insufficient space in this buffer
			LogLog.error("append(char c):" + e.getMessage(), e);
		}
		return this;
	}

	@Override
	public ByteBufferLocalLogEntry append(final CharSequence csq) {
		try{
			BufferFormatter.append(buffer, csq);
		} catch (BufferOverflowException e){
			this.error = e;
			// there is insufficient space in this buffer
			LogLog.error("append(CharSequence csq):" + e.getMessage(), e);
		}
		return this;
	}

	@Override
	public ByteBufferLocalLogEntry append(final CharSequence csq, final int start, final int end) {
		try{
			BufferFormatter.append(buffer, csq, start, end);
		} catch (BufferOverflowException e){
			this.error = e;
			// there is insufficient space in this buffer
			LogLog.error("append(CharSequence csq, int start, int end):" + e.getMessage(), e);
		}
		return this;
	}

	@Override
	public LogEntry append(final boolean b) {
		try{
			BufferFormatter.append(buffer, b);
		} catch (BufferOverflowException e){
			this.error = e;
			// there is insufficient space in this buffer
			LogLog.error("append(boolean b):" + e.getMessage(), e);
		}
		return this;
	}

	@Override
	public LogEntry append(final byte i) {
		try{
			BufferFormatter.append(buffer, i);
		} catch (BufferOverflowException e){
			this.error = e;
			// there is insufficient space in this buffer
			LogLog.error("append(byte i):" + e.getMessage(), e);
		}
		return this;
	}

	@Override
	public LogEntry append(final short i) {
		try{
			BufferFormatter.append(buffer, i);
		} catch (BufferOverflowException e){
			this.error = e;
			// there is insufficient space in this buffer
			LogLog.error("append(short i):" + e.getMessage(), e);
		}
		return this;
	}

	@Override
	public ByteBufferLocalLogEntry append(final int i){
		try{
			BufferFormatter.append(buffer, i);
		} catch (BufferOverflowException e){
			this.error = e;
			// there is insufficient space in this buffer
			LogLog.error("append(int i):" + e.getMessage(), e);
		}
		return this;
	}

	@Override
	public LogEntry append(final long i) {
		try{
			BufferFormatter.append(buffer, i);
		} catch (BufferOverflowException e){
			this.error = e;
			// there is insufficient space in this buffer
			LogLog.error("append(long i):" + e.getMessage(), e);
		}
		return this;
	}

	@Override
	public LogEntry append(final double i, final int precision) {
		try{
			BufferFormatter.append(buffer, i, precision);
		} catch (BufferOverflowException e){
			this.error = e;
			// there is insufficient space in this buffer
			LogLog.error("append(double i, int precision):" + e.getMessage(), e);
		}
		return this;
	}

	@Override
	public LogEntry append(Throwable e) {
		if (e != null){
			try {
				append(e.getClass().getName());
				String message = e.getLocalizedMessage();
				if (message != null){
					append(": ").append(message);
				}
				append('\n');
				final StackTraceElement[] trace = e.getStackTrace();
				for (int i = 0; i < trace.length; i++) {
					append("\tat ").append(trace[i].getClassName()).append('.').
						append(trace[i].getMethodName());
					append('(');
					if (trace[i].isNativeMethod()){
						append("native");
					} else {
						final String fileName = trace[i].getFileName();
						final int lineNumber = trace[i].getLineNumber();
						if (fileName != null){
							append(fileName);
							if (lineNumber >= 0){
								append(':').append(lineNumber);
							}

							final Class clazz =
								loadClass(trace[i].getClassName());
							if (clazz != null){
								append('[').append(getCodeLocation(clazz));
								final String implVersion = getImplementationVersion(clazz);
								if (implVersion != null){
									append(':').append(implVersion);
								}
								append(']');
							}

						} else {
							append("unknown");
						}
					}
					append(')').append('\n');
				}
			} catch (Throwable t){
				// there is insufficient space in this buffer
				LogLog.error("append(Throwable e):" + t.getMessage(), t);
			}
		}
		return this;
	}

	@Override
	public LogEntry append(Loggable loggable) {
		loggable.append(this);
		return this;
	}

	@Override
	public LogEntry append(Object o) {
		try {
			if (o != null){
				BufferFormatter.append(buffer, o.toString());
			} else {
				BufferFormatter.append(buffer, 'n');
				BufferFormatter.append(buffer, 'u');
				BufferFormatter.append(buffer, 'l');
				BufferFormatter.append(buffer, 'l');
			}
		} catch (BufferOverflowException e){
			this.error = e;
			// there is insufficient space in this buffer
			LogLog.error("append(Object o):" + e.getMessage(), e);
		}
		return this;
	}

	@Override
	public LogEntry appendIf(boolean condition, final char c) {
		if (condition) append(c);
		return this;
	}

	@Override
	public LogEntry appendIf(boolean condition, final CharSequence csq) {
		if (condition) append(csq);
		return this;
	}

	@Override
	public LogEntry appendIf(boolean condition, final CharSequence csq, final int start, final int end) {
		if (condition) append(csq, start, end);
		return this;
	}

	@Override
	public LogEntry appendIf(boolean condition, final boolean b) {
		if (condition) append(b);
		return this;
	}

	@Override
	public LogEntry appendIf(boolean condition, final byte i) {
		if (condition) append(i);
		return this;
	}

	@Override
	public LogEntry appendIf(boolean condition, final short i) {
		if (condition) append(i);
		return this;
	}

	@Override
	public LogEntry appendIf(boolean condition, final int i) {
		if (condition) append(i);
		return this;
	}

	@Override
	public LogEntry appendIf(boolean condition, final long i) {
		if (condition) append(i);
		return this;
	}

	@Override
	public LogEntry appendIf(boolean condition, final double i, final int precision) {
		if (condition) append(i, precision);
		return this;
	}

	@Override
	public LogEntry appendIf(boolean condition, Throwable e) {
		if (condition) append(e);
		return this;
	}

	@Override
	public LogEntry appendIf(boolean condition, Loggable loggable) {
		if (condition) append(loggable);
		return this;
	}

	@Override
	public LogEntry appendIf(boolean condition, Object o) {
		if (condition) append(o);
		return this;
	}

	@Override
	public void commit() {
		buffer.flip();
		loggerService.entryFlushed(this);
		commited = true;
		error = null;
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
