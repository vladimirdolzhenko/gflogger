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

package gflogger.appender;

import gflogger.Appender;
import gflogger.LoggerService;
import gflogger.base.DefaultLoggerServiceImpl;
import gflogger.disruptor.DLoggerServiceImpl;

/**
 * ConsoleAppenderFactory
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class ConsoleAppenderFactory extends AbstractAppenderFactory {

	protected Appendable outputStream = System.out;

	@Override
	public Appender createAppender(Class<? extends LoggerService> loggerServiceClass) {
		if (DefaultLoggerServiceImpl.class.equals(loggerServiceClass)){
			final gflogger.base.appender.ConsoleAppender appender =
				new gflogger.base.appender.ConsoleAppender(bufferSize, outputStream);

			appender.setLogLevel(logLevel);
			appender.setLayout(layout);
			appender.setImmediateFlush(immediateFlush);
			appender.setBufferedIOThreshold(bufferedIOThreshold);
			appender.setAwaitTimeout(awaitTimeout);

			return appender;
		} else if (DLoggerServiceImpl.class.equals(loggerServiceClass)){
			final gflogger.disruptor.appender.ConsoleAppender appender =
				new gflogger.disruptor.appender.ConsoleAppender(bufferSize, multichar, outputStream);

			appender.setLogLevel(logLevel);
			appender.setLayout(layout);
			appender.setImmediateFlush(immediateFlush);
			appender.setBufferedIOThreshold(bufferedIOThreshold);
			appender.setAwaitTimeout(awaitTimeout);

			return appender;
		}
		throw new IllegalArgumentException(loggerServiceClass.getName()
			+ " is unsupported type of logger service");
	}

	/*
	 * Setters'n'Getters
	 */

	public Appendable getOutputStream() {
		return this.outputStream;
	}

	public void setOutputStream(Appendable outputStream) {
		this.outputStream = outputStream;
	}

}
