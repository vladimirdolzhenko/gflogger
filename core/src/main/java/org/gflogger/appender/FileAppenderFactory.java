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

import static org.gflogger.helpers.OptionConverter.getBooleanProperty;
import static org.gflogger.helpers.OptionConverter.getStringProperty;


/**
 * FileAppenderFactory
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class FileAppenderFactory<A extends FileAppender> extends AbstractAppenderFactory<A> {

	protected String fileName = getStringProperty("gflogger.filename", null);
	protected String codepage = getStringProperty("gflogger.codepage", "UTF-8");
	protected boolean append = getBooleanProperty("gflogger.append", true);

	@Override
	public A createAppender(Class<? extends LoggerService> loggerServiceClass) {
		preinit(loggerServiceClass);
		final A appender = createAppender();

		appender.setLayout(layout);
		appender.setImmediateFlush(immediateFlush);
		appender.setBufferedIOThreshold(bufferedIOThreshold);
		appender.setAwaitTimeout(awaitTimeout);

		appender.setFileName(fileName);
		appender.setCodepage(codepage);
		appender.setAppend(append);

		return appender;
	}

	@SuppressWarnings("unchecked")
	protected A createAppender() {
		return (A) new FileAppender(bufferSize, multibyte, logLevel, enabled);
	}

	/*===================== Setters'n'Getters =================================*/

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getCodepage() {
		return this.codepage;
	}

	public void setCodepage(String codepage) {
		this.codepage = codepage;
	}

	public boolean isAppend() {
		return this.append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}

}
