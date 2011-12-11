package gflogger;

/**
 * LoggerView
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LoggerView implements Logger {

	private final LoggerService loggerService;
	private final LogLevel level;

	private final MockLogEntry mockLogEntry;

	private final String name;
	private final String className;

	public LoggerView(final LoggerService loggerService, final String name) {
		this.loggerService = loggerService;
		this.level = loggerService != null ? loggerService.getLevel() : LogLevel.ERROR;
		this.mockLogEntry = new MockLogEntry();
		this.name = name;
		this.className = null;
	}

	public LoggerView(final LoggerService loggerService, final Class clazz) {
		this.loggerService = loggerService;
		this.level = loggerService != null ? loggerService.getLevel() : LogLevel.ERROR;
		this.mockLogEntry = new MockLogEntry();
		this.name = null;
		this.className = clazz.getName();
	}

	@Override
	public boolean isDebugEnabled() {
		return loggerService != null && level.compareTo(LogLevel.DEBUG) <= 0;
	}

	@Override
	public LogEntry debug() {
		if (!isDebugEnabled()) return mockLogEntry;
		return loggerService.log(LogLevel.DEBUG, name, className);
	}

	@Override
	public boolean isInfoEnabled() {
		return loggerService != null && level.compareTo(LogLevel.INFO) <= 0;
	}

	@Override
	public LogEntry info() {
		if (!isInfoEnabled()) return mockLogEntry;
		return loggerService.log(LogLevel.INFO, name, className);
	}

	@Override
	public boolean isErrorEnabled() {
		return loggerService != null && level.compareTo(LogLevel.ERROR) <= 0;
	}

	@Override
	public LogEntry error() {
		if (!isErrorEnabled()) return mockLogEntry;
		return loggerService.log(LogLevel.ERROR, name, className);
	}
}
