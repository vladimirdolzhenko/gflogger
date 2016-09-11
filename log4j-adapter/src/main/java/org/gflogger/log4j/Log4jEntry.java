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

package org.gflogger.log4j;

import static org.gflogger.util.StackTraceUtils.getCodeLocation;
import static org.gflogger.util.StackTraceUtils.getImplementationVersion;
import static org.gflogger.util.StackTraceUtils.loadClass;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.gflogger.DefaultObjectFormatterFactory;
import org.gflogger.FormattedGFLogEntry;
import org.gflogger.GFLogEntry;
import org.gflogger.LogLevel;
import org.gflogger.Loggable;
import org.gflogger.ObjectFormatter;
import org.gflogger.ObjectFormatterFactory;
import org.gflogger.formatter.BufferFormatter;


/**
 * Log4jEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class Log4jEntry implements GFLogEntry, FormattedGFLogEntry {

	// 2k
	private static final int DEFAULT_BUFFER_SIZE = 1 << 11;

	private final Log log;
	private final StringBuilder builder;

	private LogLevel logLevel;

	private final ObjectFormatterFactory	formatterFactory = new DefaultObjectFormatterFactory();

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
		if (pattern == null){
			throw new IllegalStateException("Entry has been commited.");
		}
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

	protected void checkAndCommit(){
		if (pattern == null) return;
		if (pPos + 1 != pattern.length()){
			throw new IllegalStateException("The pattern has not been finished. More parameters are required.");
		}
		commit();
	}

	public void reset(){
		builder.setLength(0);
	}

	@Override
	public GFLogEntry append(char c) {
		this.builder.append(c);
		return this;
	}

	@Override
	public GFLogEntry append(CharSequence csq) {
		this.builder.append(csq);
		return this;
	}

	@Override
	public GFLogEntry append(CharSequence csq, int start, int end) {
		this.builder.append(csq, start, end);
		return this;
	}

	@Override
	public GFLogEntry append(boolean b) {
		this.builder.append(b);
		return this;
	}

	@Override
	public GFLogEntry append(int i) {
		this.builder.append(i);
		return this;
	}

	@Override
	public GFLogEntry append(long i) {
		this.builder.append(i);
		return this;
	}

	@Override
	public GFLogEntry append(double i) {
		this.builder.append(i);
		return this;
	}

	@Override
	public GFLogEntry append(double i, int precision) {
		long x = (long)i;
		this.builder.append(x);
		this.builder.append('.');
		x = (long)((i -x) * (precision > 0 ? BufferFormatter.LONG_SIZE_TABLE[precision - 1] : 1));
		this.builder.append(x < 0 ? -x : x);
		return this;
	}

	@Override
	public <T> GFLogEntry append(T[] array, String separator) {
		if (array == null){
			append('n').append('u').append('l').append('l');
		} else {
			try {
				append('[');
				ObjectFormatter formatter = null;
				for(int i = 0; i < array.length; i++){
					if (i > 0){
						append(separator);
					}
					final T obj = array[i];
					if (obj != null){
						if (formatter == null) {
							formatter = formatterFactory.getObjectFormatter(obj);
						}
						formatter.append(obj, this);
					} else {
						append('n').append('u').append('l').append('l');
					}
				}
				append(']');
			} catch (Throwable e){
				//error("append(Object o)", e);
			}
			return this;
		}
		return this;
	}

	@Override
	public <T> GFLogEntry append(Iterable<T> iterable, String separator) {
		if (iterable == null){
			append('n').append('u').append('l').append('l');
		} else {
			try {
				append('[');
				ObjectFormatter formatter = null;
				for(final Iterator<T> it = iterable.iterator();it.hasNext();){
					final T obj = it.next();
					if (obj != null){
						if (formatter == null) {
							formatter = formatterFactory.getObjectFormatter(obj);
						}
						formatter.append(obj, this);
					} else {
						append('n').append('u').append('l').append('l');
					}
					if (it.hasNext()){
						append(separator);
					}
				}
				append(']');
			} catch (Throwable e){
				//error("append(Object o)", e);
			}
		}
		return this;
	}

	@Override
	public GFLogEntry append(Throwable e) {
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
	public GFLogEntry append(Loggable loggable) {
		if (loggable != null){
			loggable.appendTo(this);
		} else {
			append('n').append('u').append('l').append('l');
		}
		return this;
	}

	@Override
	public GFLogEntry append(Object o) {
		try {
			if (o != null){
				final ObjectFormatter formatter = formatterFactory.getObjectFormatter(o);
				formatter.append(o, this);
			} else {
				append('n').append('u').append('l').append('l');
			}
		} catch (Throwable e){
			//error("append(Object o)", e);
		}
		return this;
	}

	@Override
	public void appendLast(final char c) {
		append(c);
		commit();
	}

	@Override
	public void appendLast(final CharSequence csq) {
		append(csq);
		commit();
	}

	@Override
	public void appendLast(final CharSequence csq, final int start, final int end) {
		append(csq, start, end);
		commit();
	}

	@Override
	public void appendLast(final boolean b) {
		append(b);
		commit();
	}

	@Override
	public void appendLast(final int i) {
		append(i);
		commit();
	}

	@Override
	public void appendLast(final long i) {
		append(i);
		commit();
	}

	@Override
	public void appendLast(final double i) {
		append(i);
		commit();
	}

	@Override
	public void appendLast(final double i, final int precision) {
		append(i, precision);
		commit();
	}

	@Override
	public <T> void appendLast(T[] array, String separator) {
		append(array, separator);
		commit();
	}

	@Override
	public <T> void appendLast(Iterable<T> iterable, String separator) {
		append(iterable, separator);
		commit();
	}

	@Override
	public void appendLast(Throwable e) {
		append(e);
		commit();
	}

	@Override
	public void appendLast(Loggable loggable) {
		append(loggable);
		commit();
	}

	@Override
	public void appendLast(Object o) {
		append(o);
		commit();
	}

	@Override
	public boolean isPatternEnd() {
		return pPos == pattern.length();
	}

	@Override
	public FormattedGFLogEntry with(char c){
		checkPlaceholder();
		append(c);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(CharSequence csq){
		checkPlaceholder();
		append(csq);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(CharSequence csq, int start, int end){
		checkPlaceholder();
		append(csq, start, end);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(boolean b){
		checkPlaceholder();
		append(b);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(int i){
		checkPlaceholder();
		append(i);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(long i){
		checkPlaceholder();
		append(i);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(double i){
		checkPlaceholder();
		append(i);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(double i, int precision){
		checkPlaceholder();
		append(i, precision);
		appendNextPatternChank();
		return this;
	}

	@Override
	public <T> FormattedGFLogEntry with(T[] array, String separator) {
		checkPlaceholder();
		append(array, separator);
		appendNextPatternChank();
		return this;
	}

	@Override
	public <T> FormattedGFLogEntry with(Iterable<T> iterable, String separator) {
		checkPlaceholder();
		append(iterable, separator);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(Throwable e){
		checkPlaceholder();
		append(e);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(Loggable loggable){
		checkPlaceholder();
		append(loggable);
		appendNextPatternChank();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(Object o){
		checkPlaceholder();
		append(o);
		appendNextPatternChank();
		return this;
	}

	@Override
	public void withLast(char c){
		with(c);
		checkAndCommit();
	}

	@Override
	public void withLast(CharSequence csq){
		with(csq);
		checkAndCommit();
	}

	@Override
	public void withLast(CharSequence csq, int start, int end){
		with(csq, start, end);
		checkAndCommit();
	}

	@Override
	public void withLast(boolean b){
		with(b);
		checkAndCommit();
	}

	@Override
	public void  withLast(int i){
		with(i);
		checkAndCommit();
	}

	@Override
	public void  withLast(long i){
		with(i);
		checkAndCommit();
	}

	@Override
	public void withLast(double i){
		with(i);
		checkAndCommit();
	}

	@Override
	public void withLast(double i, int precision){
		with(i, precision);
		checkAndCommit();
	}

	@Override
	public <T> void withLast(T[] array, String separator) {
		with(array, separator);
		checkAndCommit();
	}

	@Override
	public <T> void withLast(Iterable<T> iterable, String separator) {
		with(iterable, separator);
		checkAndCommit();
	}

	@Override
	public void withLast(Throwable e){
		with(e);
		checkAndCommit();
	}

	@Override
	public void withLast(Loggable loggable){
		with(loggable);
		checkAndCommit();
	}

	@Override
	public void withLast(Object o){
		with(o);
		checkAndCommit();
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
		pattern = null;
	}
}
