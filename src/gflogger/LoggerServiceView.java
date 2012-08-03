package gflogger;

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
	public LogEntry log(LogLevel level, String categoryName) {
		return service.log(level, categoryName);
	}

	@Override
	public void entryFlushed(LocalLogEntry localEntry) {
		service.entryFlushed(localEntry);
	}

	@Override
	public void stop() {
		service.stop();
	}

}
