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

import org.gflogger.LoggerService;


/**
 * DailyRollingFileAppenderFactory
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DailyRollingFileAppenderFactory extends FileAppenderFactory<DailyRollingFileAppender> {

	/**
	 * The date pattern. By default, the pattern is set to "'.'yyyy-MM-dd"
	 * meaning daily rollover.
	 */
	protected String datePattern = getStringProperty("gflogger.rolling.pattern", "'.'yyyy-MM-dd");

	@Override
	public DailyRollingFileAppender createAppender(Class<? extends LoggerService> loggerServiceClass) {
		preinit(loggerServiceClass);
		final DailyRollingFileAppender appender = createAppender();

		appender.setLayout(layout);
		appender.setImmediateFlush(immediateFlush);
		appender.setBufferedIOThreshold(bufferedIOThreshold);
		appender.setAwaitTimeout(awaitTimeout);

		appender.setFileName(fileName);
		appender.setCodepage(codepage);
		appender.setAppend(append);

		appender.setDatePattern(datePattern);

		return appender;
	}

	protected DailyRollingFileAppender createAppender() {
		return new DailyRollingFileAppender(
			bufferSize,
			multibyte,
			logLevel,
			enabled
		);
	}

	/*===================== Setters'n'Getters =================================*/

	public String getDatePattern() {
		return this.datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

}
