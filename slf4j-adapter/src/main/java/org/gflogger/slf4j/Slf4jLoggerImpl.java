package org.gflogger.slf4j;

import org.gflogger.FormattedGFLogEntry;
import org.gflogger.GFLog;
import org.gflogger.GFLogEntry;
import org.gflogger.LocalLogEntry;
import org.gflogger.helpers.LogLog;
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
        try {
            return log.isTraceEnabled();
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
            return false;
        }
    }

    @Override
    public void trace(String pattern) {
        try {
            logUnsafe(log.trace(), pattern);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void trace(String pattern, Object obj) {
        try {
            logUnsafe(log.trace(pattern), obj);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void trace(String pattern, Object obj1, Object obj2) {
        try {
            logUnsafe(log.trace(pattern), obj1, obj2);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void trace(String pattern, Object... objects) {
        try {
            logArray(log.trace(pattern), objects);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void trace(String pattern, Throwable throwable) {
        try {
            logUnsafe(log.trace(), pattern, throwable);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        try {
            return log.isDebugEnabled();
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
            return false;
        }
    }

    @Override
    public void debug(String pattern) {
        try {
            logUnsafe(log.debug(), pattern);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void debug(String pattern, Object obj) {
        try {
            logUnsafe(log.debug(pattern), obj);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void debug(String pattern, Object obj1, Object obj2) {
        try {
            logUnsafe(log.debug(pattern), obj1, obj2);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void debug(String pattern, Object... objects) {
        try {
            logArray(log.debug(pattern), objects);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void debug(String pattern, Throwable throwable) {
        try {
            logUnsafe(log.debug(), pattern, throwable);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        try {
            return log.isInfoEnabled();
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
            return false;
        }
    }

    @Override
    public void info(String pattern) {
        try {
            logUnsafe(log.info(), pattern);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void info(String pattern, Object obj) {
        try {
            logUnsafe(log.info(pattern), obj);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void info(String pattern, Object obj1, Object obj2) {
        try {
            logUnsafe(log.info(pattern), obj1, obj2);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void info(String pattern, Object... objects) {
        try {
            logArray(log.info(pattern), objects);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void info(String pattern, Throwable throwable) {
        try {
            logUnsafe(log.info(), pattern, throwable);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        try {
            return log.isWarnEnabled();
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
            return false;
        }
    }

    @Override
    public void warn(String pattern) {
        try {
            logUnsafe(log.warn(), pattern);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void warn(String pattern, Object obj) {
        try {
            logUnsafe(log.warn(pattern), obj);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void warn(String pattern, Object... objects) {
        try {
            logArray(log.warn(pattern), objects);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void warn(String pattern, Object obj1, Object obj2) {
        try {
            logUnsafe(log.warn(pattern), obj1, obj2);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void warn(String pattern, Throwable throwable) {
        try {
            logUnsafe(log.warn(), pattern, throwable);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        try {
            return log.isErrorEnabled();
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
            return false;
        }
    }

    @Override
    public void error(String pattern) {
        try {
            logUnsafe(log.error(), pattern);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void error(String pattern, Object obj) {
        try {
            logUnsafe(log.error(pattern), obj);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void error(String pattern, Object obj1, Object obj2) {
        try {
            logUnsafe(log.error(pattern), obj1, obj2);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void error(String pattern, Object... objects) {
        try {
            logArray(log.error(pattern), objects);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    @Override
    public void error(String pattern, Throwable throwable) {
        try {
            logUnsafe(log.error(), pattern, throwable);
        } catch (Throwable e) {
            LogLog.error("Unhandled error: ", e);
        }
    }

    private void logUnsafe(GFLogEntry entry, String pattern) {
        entry.append(pattern).commit();
    }

    private void logUnsafe(GFLogEntry entry, String pattern, Throwable t) {
        entry.append(pattern).append(t).commit();
    }

    private void logUnsafe(FormattedGFLogEntry entry, Object obj) {
        entry.withLast(obj);
    }

    private void logUnsafe(FormattedGFLogEntry entry, Object obj1, Object obj2) {
        entry.with(obj1);
        processLast(entry, obj2);
    }

    private void logArray(FormattedGFLogEntry entry, Object[] objects) {
        if (objects == null) {
            logUnsafe(entry, null);
            return;
        }
        if (objects.length == 0) {
            logUnsafe(entry, "[]");
            return;
        }
        for (int i = 0; i < objects.length - 1; i++) {
            entry = entry.with(objects[i]);
        }
        Object last = objects[objects.length - 1];
        processLast(entry, last);
    }

    private void processLast(FormattedGFLogEntry entry, Object last) {
        if (entry.isPatternEnd() && last instanceof Throwable) {
            if (entry instanceof LocalLogEntry) {
                ((LocalLogEntry) entry).append((Throwable) last).commit();
            } else {
                LogLog.error("LocalLogEntry expected in slf4j adapter");
            }
        } else {
            entry.withLast(last);
        }
    }
}
