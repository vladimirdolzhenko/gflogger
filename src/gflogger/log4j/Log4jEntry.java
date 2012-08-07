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

import gflogger.FormattedLogEntry;
import gflogger.LogEntry;
import gflogger.LogLevel;
import gflogger.Loggable;
import gflogger.formatter.BufferFormatter;

/**
 * Log4jEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class Log4jEntry implements LogEntry, FormattedLogEntry {

	// 2k
	private static final int DEFAULT_BUFFER_SIZE = 1 << 11;

	private final Log log;
	private final StringBuilder builder;

	private LogLevel logLevel;

	private String pattern;
	private int pPos;

	public Log4jEntry(Log log) {
		this.log = log;
		this.builder = new StringBuilder(DEFAULT_BUFFER_SIZE);
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public void setPattern(String pattern) {
		if (pattern == null){
			throw new IllegalArgumentException("expected not null pattern.");
		}
		this.pattern = pattern;
		this.pPos = 0;
		appendNextPatternChank();
	}

	protected void appendNextPatternChank(){
		final int len = pattern.length();
		for(; pPos < len; pPos++){
			final char ch = pattern.charAt(pPos);
			if (ch == '%' && (pPos + 1) < len){
				if (pattern.charAt(pPos + 1) != '%') break;
				pPos++;
			}
			append(ch);
		}
		if (this.pPos == len){
			commit();
		}
	}

	protected void checkPlaceholder(){
		if (pPos + 2 >= pattern.length()){
			throw new IllegalStateException("Illegal pattern '" + pattern + "' or position " + pPos);
		}
		final char ch1 = pattern.charAt(pPos);
		final char ch2 = pattern.charAt(pPos + 1);
		if (ch1 != '%' || ch2 != 's'){
			throw new IllegalArgumentException("Illegal pattern placeholder '" + ch1 + "" + ch2 + " at " + pPos);
		}
		pPos += 2;
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
	public LogEntry append(Loggable loggable) {
		loggable.appendTo(this);
		return this;
	}

	@Override
	public LogEntry append(Object o) {
		this.builder.append(String.valueOf(o));
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
	public FormattedLogEntry with(char c){
		checkPlaceholder();
		append(c);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedLogEntry with(CharSequence csq){
		checkPlaceholder();
		append(csq);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedLogEntry with(CharSequence csq, int start, int end){
		checkPlaceholder();
		append(csq, start, end);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedLogEntry with(boolean b){
		checkPlaceholder();
		append(b);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedLogEntry with(byte i){
		checkPlaceholder();
		append(i);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedLogEntry with(short i){
		checkPlaceholder();
		append(i);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedLogEntry with(int i){
		checkPlaceholder();
		append(i);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedLogEntry with(long i){
		checkPlaceholder();
		append(i);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedLogEntry with(double i, int precision){
		checkPlaceholder();
		append(i, precision);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedLogEntry with(Throwable e){
		checkPlaceholder();
		append(e);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedLogEntry with(Loggable loggable){
		checkPlaceholder();
		append(loggable);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedLogEntry with(Object o){
		checkPlaceholder();
		append(o);
		appendNextPatternChank();
		return this;
	}

	@Override
	public void endWith(char c){
		with(c);
		commit();
	}

	@Override
	public void endWith(CharSequence csq){
		with(csq);
		commit();
	}

	@Override
	public void endWith(CharSequence csq, int start, int end){
		with(csq, start, end);
		commit();
	}

	@Override
	public void endWith(boolean b){
		with(b);
		commit();
	}

	@Override
	public void endWith(byte i){
		with(i);
		commit();
	}

	@Override
	public void  endWith(short i){
		with(i);
		commit();
	}

	@Override
	public void  endWith(int i){
		with(i);
		commit();
	}

	@Override
	public void  endWith(long i){
		with(i);
		commit();
	}

	@Override
	public void endWith(double i, int precision){
		with(i, precision);
		commit();
	}

	@Override
	public void endWith(Throwable e){
		with(e);
		commit();
	}

	@Override
	public void endWith(Loggable loggable){
		with(loggable);
		commit();
	}

	@Override
	public void endWith(Object o){
		with(o);
		commit();
	}

	@Override
	public void commit() {
		switch (logLevel) {
		case TRACE:
			if (log.isTraceEnabled()) {
				log.trace(builder.toString());
			}
			break;
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
		case WARN:
			if (log.isWarnEnabled()) {
				log.warn(builder.toString());
			}
			break;
		case ERROR:
			if (log.isErrorEnabled()) {
				log.error(builder.toString());
			}
			break;
		case FATAL:
			if (log.isFatalEnabled()) {
				log.fatal(builder.toString());
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
