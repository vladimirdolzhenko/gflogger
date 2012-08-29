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

import java.nio.ByteBuffer;

import static gflogger.helpers.OptionConverter.getStringProperty;
import static gflogger.util.StackTraceUtils.getCodeLocation;
import static gflogger.util.StackTraceUtils.getImplementationVersion;
import static gflogger.util.StackTraceUtils.loadClass;
import gflogger.formatter.BufferFormatter;
import gflogger.helpers.LogLog;

/**
 * AbstractBufferLocalLogEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
abstract class AbstractBufferLocalLogEntry implements LocalLogEntry {

	protected final ByteBuffer byteBuffer;

	protected final String threadName;
	protected final LoggerService loggerService;
	protected final ObjectFormatterFactory	formatterFactory;
	protected final String logErrorsMessage;

	protected String categoryName;
	protected LogLevel logLevel;

	protected boolean commited = true;
	protected Throwable error;

	protected String pattern;
	protected int pPos;

	public AbstractBufferLocalLogEntry(final Thread owner,
			final ObjectFormatterFactory formatterFactory,
			final LoggerService loggerService,
			final ByteBuffer byteBuffer) {
		this(owner, formatterFactory,
			loggerService, getStringProperty("gflogger.errorMessage", ">>TRNCTD>>"), byteBuffer);
	}

	public AbstractBufferLocalLogEntry(final Thread owner,
			final ObjectFormatterFactory formatterFactory,
			final LoggerService loggerService,
			final String logErrorsMessage,
			final ByteBuffer byteBuffer) {
		this.byteBuffer = byteBuffer;
		/*
		 * It worth to cache thread categoryName at thread local variable cause
		 * thread.getName() creates new String(char[])
		 */
		this.threadName = owner.getName();
		this.formatterFactory = formatterFactory;
		this.loggerService = loggerService;
		this.logErrorsMessage = logErrorsMessage != null && logErrorsMessage.length() > 0 ? logErrorsMessage : null;

		// there is no reason to register in Cleaner as direct byte buffer registers in it by its own
		// Cleaner.create(this, new BufferPurger(this.byteBuffer));
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
		if (pPos + 1 >= pattern.length()){
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
		if (commited) return;
		if (pPos + 1 != pattern.length()){
			throw new IllegalStateException("The pattern has not been finished. More parameters are required.");
		}
		commit();
	}

	protected void error(String msg, Throwable e){
		this.error = e;
		// there is insufficient space in this buffer
		if (logErrorsMessage == null) {
			LogLog.error(msg + ":" + e.getMessage(), e);
		} else {
			moveAndAppendSilent(logErrorsMessage);
		}
	}

	protected abstract void moveAndAppendSilent(String message);

	@Override
	public LogEntry append(Loggable loggable) {
		if (loggable != null){
			try {
				loggable.appendTo(this);
			} catch(Throwable e){
				error("append(Loggable loggable)", e);
			}
		} else {
			append('n').append('u').append('l').append('l');
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
						append("native)");
					} else {
						final String fileName = trace[i].getFileName();
						final int lineNumber = trace[i].getLineNumber();
						if (fileName != null){
							append(fileName);
							if (lineNumber >= 0){
								append(':').append(lineNumber);
							}
							append(')');
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
					append('\n');
				}
				Throwable cause = e.getCause();
				if (cause != null) {
					append("\n caused by: \n");
					append(cause);
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
				final ObjectFormatter formatter = formatterFactory.getObjectFormatter(o);
				formatter.append(o, this);
			} else {
				append('n').append('u').append('l').append('l');
			}
		} catch (Throwable e){
			error("append(Object o)", e);
		}
		return this;
	}

	@Override
	public void appendLast (final char c) {
		append(c);
		commit();
	}

	@Override
	public void appendLast (final CharSequence csq) {
		append(csq);
		commit();
	}

	@Override
	public void appendLast (final CharSequence csq, final int start, final int end) {
		append(csq, start, end);
		commit();
	}

	@Override
	public void appendLast (final boolean b) {
		append(b);
		commit();
	}

	@Override
	public void appendLast (final int i) {
		append(i);
		commit();
	}

	@Override
	public void appendLast (final long i) {
		append(i);
		commit();
	}

	@Override
	public void appendLast (final double i, final int precision) {
		append(i, precision);
		commit();
	}

	@Override
	public void appendLast (Throwable e) {
		append(e);
		commit();
	}

	@Override
	public void appendLast (Loggable loggable) {
		append(loggable);
		commit();
	}

	@Override
	public void appendLast (Object o) {
		append(o);
		commit();
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
	public void withLast(double i, int precision){
		with(i, precision);
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
	public final void commit() {
		if (commited) return;
		commit0();
		loggerService.entryFlushed(this);
		commited = true;
		pattern = null;
		error = null;
	}

	protected abstract void commit0();

	protected static class BufferPurger implements Runnable {
		private final ByteBuffer buffer;

		public BufferPurger(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		@Override
		public void run() {
			BufferFormatter.purge(buffer);
		}

	}
}
