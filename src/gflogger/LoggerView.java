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

	private final LoggerService loggerService;
	private final LogLevel level;

	private final MockLogEntry mockLogEntry;

	private final String categoryName;

	public LoggerView(final LoggerService loggerService, final String name) {
		this.loggerService = loggerService;
		this.level = loggerService != null ? loggerService.getLevel() : LogLevel.ERROR;
		this.mockLogEntry = new MockLogEntry();
		this.categoryName = name;
	}

	public LoggerView(final LoggerService loggerService, final Class clazz) {
		this.loggerService = loggerService;
		this.level = loggerService != null ? loggerService.getLevel() : LogLevel.ERROR;
		this.mockLogEntry = new MockLogEntry();
		this.categoryName = clazz.getName();
	}

	@Override
	public boolean isDebugEnabled() {
		return loggerService != null && level.compareTo(LogLevel.DEBUG) <= 0;
	}

	@Override
	public LogEntry debug() {
		if (!isDebugEnabled()) return mockLogEntry;
		return loggerService.log(LogLevel.DEBUG, categoryName);
	}

	@Override
	public boolean isInfoEnabled() {
		return loggerService != null && level.compareTo(LogLevel.INFO) <= 0;
	}

	@Override
	public LogEntry info() {
		if (!isInfoEnabled()) return mockLogEntry;
		return loggerService.log(LogLevel.INFO, categoryName);
	}

	@Override
	public boolean isErrorEnabled() {
		return loggerService != null && level.compareTo(LogLevel.ERROR) <= 0;
	}

	@Override
	public LogEntry error() {
		if (!isErrorEnabled()) return mockLogEntry;
		return loggerService.log(LogLevel.ERROR, categoryName);
	}
}
