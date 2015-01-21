package org.gflogger.appender;

import org.gflogger.Appender;
import org.gflogger.LogEntryItemImpl;
import org.gflogger.LogLevel;
import org.gflogger.helpers.LogLog;

/**
 * @author BegemoT cheremin@gmail.com
 *         created 11/21/14 at 12:07 PM
 */
public abstract class AbstractAppender implements Appender<LogEntryItemImpl> {

	protected final String name;

	protected final Buffer buffer;

	protected final LogLevel logLevel;

	protected final boolean enabled;

	protected AbstractAppender(final String name,
	                           final Buffer buffer,
	                           final LogLevel logLevel,
	                           final boolean enabled) {
		this.name = name;
		this.buffer = buffer;
		this.logLevel = logLevel;
		this.enabled = enabled;
	}

	protected AbstractAppender(final String name,
	                           final int bufferSize,
	                           final boolean multibyte,
	                           final LogLevel logLevel,
	                           final boolean enabled) {
		this.name = name;
		this.logLevel = logLevel;
		this.enabled = enabled;
		this.buffer = multibyte ? new CharBufferImpl(bufferSize, this) : new BufferImpl(bufferSize, this);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isMultibyte() {
		return buffer.isMultibyte();
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void onUncatchException(final Throwable e) {
		LogLog.error("Unhandled exception in " + Thread.currentThread().getName() + " :" + e.getMessage(), e);
	}

	@Override
	public String toString() {
		return getName();
	}
}
