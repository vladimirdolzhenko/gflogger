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

	protected final LogLevel logLevel;

	protected final boolean enabled;

	protected final boolean multibyte;

	protected final String name;

	protected AbstractAppender(final String name,
	                           final boolean multibyte,
	                           final LogLevel logLevel,
	                           final boolean enabled) {
		this.name = name;
		this.multibyte = multibyte;
		this.logLevel = logLevel;
		this.enabled = enabled;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isMultibyte() {
		return multibyte;
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
