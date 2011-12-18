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

package gflogger.log4j;

import static gflogger.util.StackTraceUtils.*;

import org.apache.commons.logging.Log;

import gflogger.LogEntry;
import gflogger.LogLevel;
import gflogger.formatter.BufferFormatter;

/**
 * Log4jEntry
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class Log4jEntry implements LogEntry {

	// 2k
	private static final int DEFAULT_BUFFER_SIZE = 1 << 11;
	
	private final Log log;
	private final StringBuilder builder;
	
	private LogLevel logLevel;
	
	public Log4jEntry(Log log) {
		this.log = log;
		this.builder = new StringBuilder(DEFAULT_BUFFER_SIZE);
	}
	
	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}
	
	public void reset(){
		builder.setLength(0);
	}
	
	@Override
	public LogEntry append(char c) {
		this.builder.append(c);
		return this;
	}

	@Override
	public LogEntry append(CharSequence csq) {
		this.builder.append(csq);
		return this;
	}

	@Override
	public LogEntry append(CharSequence csq, int start, int end) {
		this.builder.append(csq, start, end);
		return this;
	}

	@Override
	public LogEntry append(boolean b) {
		this.builder.append(b);
		return this;
	}

	@Override
	public LogEntry append(byte i) {
		this.builder.append(i);
		return null;
	}

	@Override
	public LogEntry append(short i) {
		this.builder.append(i);
		return this;
	}

	@Override
	public LogEntry append(int i) {
		this.builder.append(i);
		return this;
	}

	@Override
	public LogEntry append(long i) {
		this.builder.append(i);
		return this;
	}

	@Override
	public LogEntry append(double i, int precision) {
		long x = (long)i;
		this.builder.append(x);
		this.builder.append('.');
		x = (long)((i -x) * (precision > 0 ? BufferFormatter.LONG_SIZE_TABLE[precision - 1] : 1));
		this.builder.append(x < 0 ? -x : x);
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
				//
				t.printStackTrace();
			}
		}
		return this;
	}
	
	@Override
	public LogEntry append(Object o) {
		this.builder.append(String.valueOf(o));
		return this;
	}

	@Override
	public void commit() {
		switch (logLevel) {
		case DEBUG:
			if (log.isDebugEnabled()) {
				log.debug(builder.toString());
			}
			break;
		case INFO:
			if (log.isInfoEnabled()) {
				log.info(builder.toString());
			}
			break;
		case ERROR:
			if (log.isErrorEnabled()) {
				log.error(builder.toString());
			}
			break;
		}
		if (builder.length() > DEFAULT_BUFFER_SIZE){
			builder.setLength(DEFAULT_BUFFER_SIZE);
			builder.trimToSize();
		}
		builder.setLength(0);
	}
}
