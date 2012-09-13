package org.gflogger;

@Deprecated
public class LoggerServiceView implements LoggerService {

	private final LoggerService service;
	private final LogLevel logLevel;

	public LoggerServiceView(LoggerService service, LogLevel logLevel) {
		this.service = service;
		this.logLevel = logLevel;
	}

	@Override
	public LogLevel getLevel() {
		return logLevel;
	}

	@Override
	public LogEntry log(LogLevel level, String categoryName, final long appenderMask) {
		return service.log(level, categoryName, appenderMask);
	}

	@Override
	public FormattedLogEntry formattedLog(LogLevel level, String categoryName, String pattern, final long appenderMask) {
		return service.formattedLog(level, categoryName, pattern, appenderMask);
	}

	@Override
	public void entryFlushed(LocalLogEntry localEntry) {
		service.entryFlushed(localEntry);
	}

	@Override
	public GFLogger[] lookupLoggers(String name) {
	    return service.lookupLoggers(name);
	}

	@Override
	public void stop() {
		service.stop();
	}

}
