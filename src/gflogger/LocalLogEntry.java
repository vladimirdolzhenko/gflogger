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
public final class LocalLogEntry implements LogEntry {

	private final String threadName;
	private final ByteBuffer byteBuffer;
	private final CharBuffer buffer;
	private final LoggerService loggerService;

	private String categoryName;
	private LogLevel logLevel;
	
	private boolean commited = true;
	private Throwable error;
	
	public LocalLogEntry(final int maxMessageSize, final LoggerService loggerService) {
		this(Thread.currentThread(), maxMessageSize, loggerService);
	}
	
	public LocalLogEntry(final Thread owner, final int maxMessageSize, final LoggerService loggerService) {
		// char has max 2 bytes
		this(owner, allocate(maxMessageSize << 1), loggerService);
	}

	public LocalLogEntry(final Thread owner, final ByteBuffer byteBuffer, final LoggerService loggerService) {
		/*
		 * It worth to cache thread categoryName at thread local variable cause
		 * thread.getName() creates new String(char[])
		 */
		this.threadName = owner.getName();
		this.byteBuffer = byteBuffer;
		this.buffer = byteBuffer.asCharBuffer();
		this.loggerService = loggerService;
	}
	
	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(final LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	public String getCategoryName() {
		return categoryName;
	}

	public String getThreadName() {
		return threadName;
	}

	public CharBuffer getBuffer() {
		return buffer;
	}
	
	public boolean isCommited() {
	    return this.commited;
    }
	
	public void setCommited(boolean commited) {
	    this.commited = commited;
    }
	
	public Throwable getError() {
	    return this.error;
    }

	@Override
	public LocalLogEntry append(final char c) {
		try {
			buffer.append(c);
		} catch (BufferOverflowException e){
			this.error = e;
			// there is insufficient space in this buffer
			LogLog.error("append(char c):" + e.getMessage(), e);
		}
		return this;
	}

	@Override
	public LocalLogEntry append(final CharSequence csq) {
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
	public LocalLogEntry append(final CharSequence csq, final int start, final int end) {
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
	public LocalLogEntry append(final int i){
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
	public LogEntry append(Object o) {
		try {
			if (o != null){
				buffer.append(o.toString());
			} else {
				buffer.put('n').put('u').put('l').put('l');
			}
		} catch (BufferOverflowException e){
			this.error = e;
			// there is insufficient space in this buffer
			LogLog.error("append(Object o):" + e.getMessage(), e);
		}
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
			" pos:" + byteBuffer.position() + 
			" limit:" + byteBuffer.limit() + 
			" capacity:" + byteBuffer.capacity() + 
			"]";
	}

}
