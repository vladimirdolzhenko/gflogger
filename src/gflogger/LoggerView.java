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

/**
 * LoggerView
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LoggerView implements Logger {

	private LoggerService loggerService;
	private LogLevel level;
	private volatile boolean valid;

	private final NullLogEntry mockLogEntry;

	private final String name;

	public LoggerView(final String name) {
		this.mockLogEntry = NullLogEntry.INSTANCE;
		this.name = name;
	}

	void invalidate(){
		this.loggerService = null;
		this.level = null;
		this.valid = false;
	}

	LoggerService setLoggerService(LoggerService loggerService) {
		this.loggerService = loggerService;
		this.level = loggerService != null ? loggerService.getLevel() : LogLevel.ERROR;
		this.valid = true;
		return this.loggerService;
	}

	private boolean hasNecessaryLevel(LogLevel level) {
		return loggerService() != null && this.level.isHigher(level);
	}

	private LoggerService loggerService() {
		if (valid) return loggerService;

		// lazy reinit
		return setLoggerService(LogFactory.lookupService(name));
 	}

	private LogEntry logEntry(final LogLevel logLevel) {
		return hasNecessaryLevel(logLevel) ?
			loggerService.log(logLevel, name) :
			mockLogEntry;
	}

	private FormattedLogEntry formattedLogEntry(final LogLevel logLevel, String pattern) {
		return hasNecessaryLevel(logLevel) ?
				loggerService.formattedLog(logLevel, name, pattern) :
					mockLogEntry;
	}

	@Override
	public boolean isTraceEnabled() {
		return hasNecessaryLevel(LogLevel.TRACE);
	}

	@Override
	public LogEntry trace() {
		return logEntry(LogLevel.TRACE);
	}

	@Override
	public FormattedLogEntry trace(String pattern) {
		return formattedLogEntry(LogLevel.TRACE, pattern);
	}

	@Override
	public boolean isDebugEnabled() {
		return hasNecessaryLevel(LogLevel.DEBUG);
	}

	@Override
	public LogEntry debug() {
		return logEntry(LogLevel.DEBUG);
	}

	@Override
	public FormattedLogEntry debug(String pattern) {
		return formattedLogEntry(LogLevel.DEBUG, pattern);
	}

	@Override
	public boolean isInfoEnabled() {
		return hasNecessaryLevel(LogLevel.INFO);
	}

	@Override
	public LogEntry info() {
		return logEntry(LogLevel.INFO);
	}

	@Override
	public FormattedLogEntry info(String pattern) {
		return formattedLogEntry(LogLevel.INFO, pattern);
	}

	@Override
	public boolean isWarnEnabled() {
		return hasNecessaryLevel(LogLevel.WARN);
	}

	@Override
	public LogEntry warn() {
		return logEntry(LogLevel.WARN);
	}

	@Override
	public FormattedLogEntry warn(String pattern) {
		return formattedLogEntry(LogLevel.WARN, pattern);
	}

	@Override
	public boolean isErrorEnabled() {
		return hasNecessaryLevel(LogLevel.ERROR);
	}

	@Override
	public LogEntry error() {
		return logEntry(LogLevel.ERROR);
	}

	@Override
	public FormattedLogEntry error(String pattern) {
		return formattedLogEntry(LogLevel.ERROR, pattern);
	}

	@Override
	public boolean isFatalEnabled() {
		return hasNecessaryLevel(LogLevel.ERROR);
	}

	@Override
	public LogEntry fatal() {
		return logEntry(LogLevel.FATAL);
	}

	@Override
	public FormattedLogEntry fatal(String pattern) {
		return formattedLogEntry(LogLevel.FATAL, pattern);
	}
}
