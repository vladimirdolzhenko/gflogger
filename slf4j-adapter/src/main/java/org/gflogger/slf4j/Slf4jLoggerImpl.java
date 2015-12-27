package org.gflogger.slf4j;

import org.gflogger.FormattedGFLogEntry;
import org.gflogger.GFLog;
import org.slf4j.helpers.MarkerIgnoringBase;

/**
 * @author Denis Gburg
 */
public final class Slf4jLoggerImpl extends MarkerIgnoringBase {

	private final GFLog log;

	public Slf4jLoggerImpl(final GFLog log) {
		this.log = log;
	}

	@Override
	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}

	@Override
	public void trace(String pattern) {
		log.debug(pattern);
	}

	@Override
	public void trace(String pattern, Object obj) {
		log.debug(pattern).with(obj);
	}

	@Override
	public void trace(String pattern, Object obj1, Object obj2) {
		log.debug(pattern).with(obj1).with(obj2);
	}

	private void logArray(FormattedGFLogEntry entry, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			entry = entry.with(objects[i]);
		}
	}

	@Override
	public void trace(String pattern, Object... objects) {
		logArray(log.debug(pattern), objects);
	}

	@Override
	public void trace(String pattern, Throwable throwable) {
		log.debug(pattern).with(throwable);
	}

	@Override
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	@Override
	public void debug(String pattern) {
		log.debug(pattern);
	}

	@Override
	public void debug(String pattern, Object obj) {
		log.debug(pattern).with(obj);
	}

	@Override
	public void debug(String pattern, Object obj1, Object obj2) {
		log.debug(pattern).with(obj1).with(obj2);
	}

	@Override
	public void debug(String pattern, Object... objects) {
		logArray(log.debug(pattern), objects);
	}

	@Override
	public void debug(String pattern, Throwable throwable) {
		log.debug(pattern).with(throwable);
	}

	@Override
	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	@Override
	public void info(String pattern) {
		log.info(pattern);
	}

	@Override
	public void info(String pattern, Object obj) {
		log.info(pattern).with(obj);
	}

	@Override
	public void info(String pattern, Object obj1, Object obj2) {
		log.info(pattern).with(obj1).with(obj2);
	}

	@Override
	public void info(String pattern, Object... objects) {
		logArray(log.info(pattern), objects);
	}

	@Override
	public void info(String pattern, Throwable throwable) {
		log.info(pattern).with(throwable);
	}

	@Override
	public boolean isWarnEnabled() {
		return log.isWarnEnabled();
	}

	@Override
	public void warn(String pattern) {
		log.warn(pattern);
	}

	@Override
	public void warn(String pattern, Object obj) {
		log.warn(pattern).with(obj);
	}

	@Override
	public void warn(String pattern, Object... objects) {
		logArray(log.warn(pattern), objects);
	}

	@Override
	public void warn(String pattern, Object obj1, Object obj2) {
		log.warn(pattern).with(obj1).with(obj2);
	}

	@Override
	public void warn(String pattern, Throwable throwable) {
		log.warn(pattern).with(throwable);
	}

	@Override
	public boolean isErrorEnabled() {
		return log.isErrorEnabled();
	}

	@Override
	public void error(String pattern) {
		log.error(pattern);
	}

	@Override
	public void error(String pattern, Object obj) {
		log.error(pattern).with(obj);
	}

	@Override
	public void error(String pattern, Object obj1, Object obj2) {
		log.error(pattern).with(obj1).with(obj2);
	}

	@Override
	public void error(String pattern, Object... objects) {
		logArray(log.error(pattern), objects);
	}

	@Override
	public void error(String pattern, Throwable throwable) {
		log.error(pattern).with(throwable);
	}
}
