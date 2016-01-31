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
		log.debug(adjustPattern(pattern, obj)).withLast(obj);
	}

	@Override
	public void trace(String pattern, Object obj1, Object obj2) {
		FormattedGFLogEntry entry = log.debug(adjustPattern(pattern, obj2)).with(obj1);
		if (obj2 instanceof Throwable) {
			entry.withLast((Throwable)obj2);
		} else {
			entry.withLast(obj2);
		}
	}

	@Override
	public void trace(String pattern, Object... objects) {
		logArray(log.debug(adjustPattern(pattern, objects)), objects);
	}

	@Override
	public void trace(String pattern, Throwable throwable) {
		log.debug(adjustPattern(pattern, throwable)).withLast(throwable);
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
		log.debug(adjustPattern(pattern, obj)).withLast(obj);
	}

	@Override
	public void debug(String pattern, Object obj1, Object obj2) {
		FormattedGFLogEntry entry = log.debug(adjustPattern(pattern, obj2)).with(obj1);
		if (obj2 instanceof Throwable) {
			entry.withLast((Throwable)obj2);
		} else {
			entry.withLast(obj2);
		}
	}

	@Override
	public void debug(String pattern, Object... objects) {
		logArray(log.debug(adjustPattern(pattern, objects)), objects);
	}

	@Override
	public void debug(String pattern, Throwable throwable) {
		log.debug(adjustPattern(pattern, throwable)).withLast(throwable);
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
		log.info(adjustPattern(pattern, obj)).with(obj);
	}

	@Override
	public void info(String pattern, Object obj1, Object obj2) {
		FormattedGFLogEntry entry = log.info(adjustPattern(pattern, obj2)).with(obj1);
		if (obj2 instanceof Throwable) {
			entry.withLast((Throwable)obj2);
		} else {
			entry.withLast(obj2);
		}
	}

	@Override
	public void info(String pattern, Object... objects) {
		logArray(log.info(adjustPattern(pattern, objects)), objects);
	}

	@Override
	public void info(String pattern, Throwable throwable) {
		log.info(adjustPattern(pattern, throwable)).withLast(throwable);
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
		log.warn(adjustPattern(pattern, obj)).withLast(obj);
	}

	@Override
	public void warn(String pattern, Object... objects) {
		logArray(log.warn(adjustPattern(pattern, objects)), objects);
	}

	@Override
	public void warn(String pattern, Object obj1, Object obj2) {
		FormattedGFLogEntry entry = log.warn(adjustPattern(pattern, obj2)).with(obj1);
		if (obj2 instanceof Throwable) {
			entry.withLast((Throwable)obj2);
		} else {
			entry.withLast(obj2);
		}
	}

	@Override
	public void warn(String pattern, Throwable throwable) {
		log.warn(adjustPattern(pattern, throwable)).withLast(throwable);
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
		log.error(adjustPattern(pattern, obj)).withLast(obj);
	}

	@Override
	public void error(String pattern, Object obj1, Object obj2) {
		FormattedGFLogEntry entry = log.error(adjustPattern(pattern, obj2)).with(obj1);
		if (obj2 instanceof Throwable) {
			entry.withLast((Throwable)obj2);
		} else {
			entry.withLast(obj2);
		}
	}

	@Override
	public void error(String pattern, Object... objects) {
		logArray(log.error(adjustPattern(pattern, objects)), objects);
	}

	@Override
	public void error(String pattern, Throwable throwable) {
		log.error(adjustPattern(pattern, throwable)).withLast(throwable);
	}

	private void logArray(FormattedGFLogEntry entry, Object[] objects) {
		int length = objects.length;
		for (int i = 0; i < length - 1; i++) {
			entry = entry.with(objects[i]);
		}
		if (length > 1){
			if (objects[length - 1] instanceof Throwable){
				entry.withLast((Throwable)objects[length - 1]);
			} else {
				entry.withLast(objects[length - 1]);
			}
		}
	}

	private String adjustPattern(String pattern, Object[] args) {
		return args.length > 1 ? adjustPattern(pattern, args[args.length - 1]) : pattern;
	}

	private String adjustPattern(String pattern, Object lastArg) {
		if (lastArg instanceof Throwable){
			pattern = pattern + " {}";
		}
		return pattern;
	}
}
