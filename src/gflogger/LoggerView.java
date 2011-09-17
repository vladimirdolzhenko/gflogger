package gflogger;

/**
 * LoggerView
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LoggerView implements Logger {

    private final LoggerImpl loggerImpl;
    private final LogLevel level;

    private final MockLogEntry mockLogEntry;

    private final String name;
    private final String className;

    public LoggerView(final LoggerImpl loggerImpl, final String name) {
        this.loggerImpl = loggerImpl;
        this.level = loggerImpl != null ? loggerImpl.getLevel() : LogLevel.ERROR;
        this.mockLogEntry = new MockLogEntry();
        this.name = name;
        this.className = null;
    }

    public LoggerView(final LoggerImpl loggerImpl, final Class clazz) {
        this.loggerImpl = loggerImpl;
        this.level = loggerImpl != null ? loggerImpl.getLevel() : LogLevel.ERROR;
        this.mockLogEntry = new MockLogEntry();
        this.name = null;
        this.className = clazz.getName();
    }

    @Override
    public boolean isDebugEnabled() {
        return loggerImpl != null && level.compareTo(LogLevel.DEBUG) <= 0;
    }

    @Override
    public LogEntry debug() {
        if (!isDebugEnabled()) return mockLogEntry;
        return loggerImpl.log(LogLevel.DEBUG, name, className);
    }

    @Override
    public boolean isInfoEnabled() {
        return loggerImpl != null && level.compareTo(LogLevel.INFO) <= 0;
    }

    @Override
    public LogEntry info() {
        if (!isInfoEnabled()) return mockLogEntry;
        return loggerImpl.log(LogLevel.INFO, name, className);
    }

    @Override
    public boolean isErrorEnabled() {
        return loggerImpl != null && level.compareTo(LogLevel.ERROR) <= 0;
    }

    @Override
    public LogEntry error() {
        if (!isErrorEnabled()) return mockLogEntry;
        return loggerImpl.log(LogLevel.ERROR, name, className);
    }
}
