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

package org.gflogger.log4j;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.gflogger.FormattedGFLogEntry;
import org.gflogger.GFLogEntry;
import org.gflogger.GFLogger;
import org.gflogger.LocalLogEntry;
import org.gflogger.LogLevel;
import org.gflogger.LoggerService;

import java.util.HashMap;
import java.util.Map;


/**
 * Log4jLoggerServiceImpl
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class Log4jLoggerServiceImpl implements LoggerService {

	private final ThreadLocal<Map<String, Log4jEntry>> entries;
	private final LogLevel level;

	public Log4jLoggerServiceImpl() {
		this(LogLevel.DEBUG);
	}

	public Log4jLoggerServiceImpl(final LogLevel level) {
		this.level = level;
		this.entries = new ThreadLocal<Map<String, Log4jEntry>>(){
			@Override
			protected Map<String, Log4jEntry> initialValue() {
				return new HashMap<>();
			}
		};
		BasicConfigurator.configure();
	}

	@Override
	public LogLevel getLevel() {
		return level;
	}

	@Override
	public GFLogEntry log(LogLevel level, String categoryName, final long appenderMask) {
		final Map<String, Log4jEntry> map = entries.get();
		Log4jEntry entry = map.get(categoryName);
		if (entry == null){
			entry = new Log4jEntry(LogFactory.getLog(categoryName));
			map.put(categoryName, entry);
		}
		entry.setLogLevel(level);
		entry.reset();
		return entry;
	}

	@Override
	public FormattedGFLogEntry formattedLog(LogLevel level, String categoryName, String pattern, final long appenderMask) {
		final Map<String, Log4jEntry> map = entries.get();
		Log4jEntry entry = map.get(categoryName);
		if (entry == null){
			entry = new Log4jEntry(LogFactory.getLog(categoryName));
			map.put(categoryName, entry);
		}
		entry.reset();
		entry.setPattern(pattern);
		entry.setLogLevel(level);
		return entry;
	}

	@Override
	public GFLogger[] lookupLoggers(String name) {
		return GFLogger.EMPTY;
	}

	@Override
	public void entryFlushed(LocalLogEntry localEntry) {
		// nothing
	}

	@Override
	public void stop() {
		LogManager.shutdown();
	}

}
