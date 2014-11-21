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

import org.gflogger.LoggerService;


/**
 * ConsoleAppenderFactory
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class ConsoleAppenderFactory extends AbstractAppenderFactory<ConsoleAppender> {

	protected Appendable outputStream = System.out;

	@Override
	public ConsoleAppender createAppender(Class<? extends LoggerService> loggerServiceClass) {
		preinit(loggerServiceClass);
		final ConsoleAppender appender = createAppender();

		appender.setLayout(layout);
		appender.setImmediateFlush(immediateFlush);
		appender.setBufferedIOThreshold(bufferedIOThreshold);
		appender.setAwaitTimeout(awaitTimeout);

		return appender;
	}

	protected ConsoleAppender createAppender() {
		return new ConsoleAppender(
				bufferSize,
				multibyte,
		        logLevel, enabled,
		        outputStream
		);
	}

	/*===================== Setters'n'Getters =================================*/

	public Appendable getOutputStream() {
		return this.outputStream;
	}

	public void setOutputStream(Appendable outputStream) {
		this.outputStream = outputStream;
	}

}
