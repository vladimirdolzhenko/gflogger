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

package org.gflogger;

/**
 * LoggerView
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class GFLogView implements GFLog {

	private LoggerService loggerService;
	private LogLevel level;
	private volatile boolean valid;
	private final long[] appenderMask = new long[LogLevel.values.length];

	private final NullLogEntry mockLogEntry;

	private final String name;

	public GFLogView(final String name) {
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

		final GFLogger[] loggers = loggerService != null ? loggerService.lookupLoggers(name) : GFLogger.EMPTY;
		for(int i = 0; i < LogLevel.values.length; i++){
			final LogLevel level = LogLevel.values[i];
			final int ordinal = level.ordinal();
			appenderMask[ordinal] = 0;

			for (final GFLogger gfLogger : loggers) {
				final LogLevel loggerLevel = gfLogger.getLogLevel();
				if (loggerLevel.isHigher(level)) {
					final long m = gfLogger.getAppenderMask(level);
					appenderMask[ordinal] |= m;
					if (!gfLogger.hasAdditivity()) break;
				}
			}
		}

		this.level = LogLevel.FATAL;
		for (final GFLogger gfLogger : loggers) {
			final LogLevel loggerLevel = gfLogger.getLogLevel();
			this.level = this.level.isHigher(loggerLevel) ? this.level : loggerLevel;
		}

		this.valid = loggerService != null;
		return this.loggerService;
	}
	private boolean hasNecessaryLevel(LogLevel level) {
		return loggerService() != null && this.level.isHigher(level);
	}

	private LoggerService loggerService() {
		if (valid) return loggerService;

		// lazy reinit
		return setLoggerService(GFLogFactory.lookupService(name));
 	}

	private GFLogEntry logEntry(final LogLevel logLevel) {
		return hasNecessaryLevel(logLevel) ?
			loggerService.log(logLevel, name, appenderMask[logLevel.ordinal()]) :
			mockLogEntry;
	}

	private FormattedGFLogEntry formattedLogEntry(final LogLevel logLevel, String pattern) {
		return hasNecessaryLevel(logLevel) ?
				loggerService.formattedLog(logLevel, name, pattern,  appenderMask[logLevel.ordinal()]) :
					mockLogEntry;
	}

	@Override
	public boolean isTraceEnabled() {
		return hasNecessaryLevel(LogLevel.TRACE);
	}

	@Override
	public GFLogEntry trace() {
		return logEntry(LogLevel.TRACE);
	}

	@Override
	public FormattedGFLogEntry trace(String pattern) {
		return formattedLogEntry(LogLevel.TRACE, pattern);
	}

	@Override
	public boolean isDebugEnabled() {
		return hasNecessaryLevel(LogLevel.DEBUG);
	}

	@Override
	public GFLogEntry debug() {
		return logEntry(LogLevel.DEBUG);
	}

	@Override
	public FormattedGFLogEntry debug(String pattern) {
		return formattedLogEntry(LogLevel.DEBUG, pattern);
	}

	@Override
	public boolean isInfoEnabled() {
		return hasNecessaryLevel(LogLevel.INFO);
	}

	@Override
	public GFLogEntry info() {
		return logEntry(LogLevel.INFO);
	}

	@Override
	public FormattedGFLogEntry info(String pattern) {
		return formattedLogEntry(LogLevel.INFO, pattern);
	}

	@Override
	public boolean isWarnEnabled() {
		return hasNecessaryLevel(LogLevel.WARN);
	}

	@Override
	public GFLogEntry warn() {
		return logEntry(LogLevel.WARN);
	}

	@Override
	public FormattedGFLogEntry warn(String pattern) {
		return formattedLogEntry(LogLevel.WARN, pattern);
	}

	@Override
	public boolean isErrorEnabled() {
		return hasNecessaryLevel(LogLevel.ERROR);
	}

	@Override
	public GFLogEntry error() {
		return logEntry(LogLevel.ERROR);
	}

	@Override
	public FormattedGFLogEntry error(String pattern) {
		return formattedLogEntry(LogLevel.ERROR, pattern);
	}

	@Override
	public boolean isFatalEnabled() {
		return hasNecessaryLevel(LogLevel.ERROR);
	}

	@Override
	public GFLogEntry fatal() {
		return logEntry(LogLevel.FATAL);
	}

	@Override
	public FormattedGFLogEntry fatal(String pattern) {
		return formattedLogEntry(LogLevel.FATAL, pattern);
	}
}
