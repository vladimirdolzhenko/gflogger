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

package org.gflogger.appender;

import static org.gflogger.helpers.OptionConverter.getStringProperty;

import org.gflogger.Appender;
import org.gflogger.LoggerService;


/**
 * DailyRollingFileAppenderFactory
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DailyRollingFileAppenderFactory extends FileAppenderFactory {

	/**
	 * The date pattern. By default, the pattern is set to "'.'yyyy-MM-dd"
	 * meaning daily rollover.
	 */
	protected String datePattern = getStringProperty("gflogger.rolling.pattern", "'.'yyyy-MM-dd");

	@Override
	public Appender createAppender(Class<? extends LoggerService> loggerServiceClass) {
		preinit(loggerServiceClass);
		final org.gflogger.appender.DailyRollingFileAppender appender =
			new org.gflogger.appender.DailyRollingFileAppender(bufferSize, multibyte);

		appender.setLogLevel(logLevel);
		appender.setLayout(layout);
		appender.setImmediateFlush(immediateFlush);
		appender.setBufferedIOThreshold(bufferedIOThreshold);
		appender.setAwaitTimeout(awaitTimeout);
		appender.setEnabled(enabled);

		appender.setFileName(fileName);
		appender.setCodepage(codepage);
		appender.setAppend(append);
		appender.setIndex(index);

		appender.setDatePattern(datePattern);
		return appender;
	}

	/*
	 * Setters'n'Getters
	 */

	public String getDatePattern() {
		return this.datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

}
