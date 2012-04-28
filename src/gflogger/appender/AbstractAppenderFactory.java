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

import static gflogger.helpers.OptionConverter.*;
import gflogger.Layout;
import gflogger.LogLevel;
import gflogger.PatternLayout;

/**
 * AbstractAppenderFactory
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public abstract class AbstractAppenderFactory implements AppenderFactory {

	// 1M
	protected int bufferSize = getIntProperty("gflogger.buffer.size", 1 << 20);
	protected boolean multibyte = getBooleanProperty("gflogger.multibyte", false);

	protected LogLevel logLevel = LogLevel.valueOf(getStringProperty("gflogger.loglevel", "ERROR"));
	protected Layout layout = new PatternLayout(getStringProperty("gflogger.pattern", "%m%n"));
	protected boolean immediateFlush = getBooleanProperty("gflogger.immediateFlush", false);
	protected int bufferedIOThreshold = getIntProperty("gflogger.bufferedIOThreshold", 100);
	protected long awaitTimeout = getIntProperty("gflogger.awaitTimeout", 10);

	/*
	 * Setters'n'Getters
	 */
	public int getBufferSize() {
		return this.bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public boolean isMultibyte() {
		return this.multibyte;
	}

	public void setMultibyte(boolean multibyte) {
		this.multibyte = multibyte;
	}

	public LogLevel getLogLevel() {
		return this.logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public Layout getLayout() {
		return this.layout;
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
	}

	public String getLayoutPattern(){
		return this.layout != null ? this.layout.getContentType() : null;
	}

	public void setLayoutPattern(String layoutPattern){
		this.layout = new PatternLayout(layoutPattern);
	}

	public boolean isImmediateFlush() {
		return this.immediateFlush;
	}

	public void setImmediateFlush(boolean immediateFlush) {
		this.immediateFlush = immediateFlush;
	}

	public int getBufferedIOThreshold() {
		return this.bufferedIOThreshold;
	}

	public void setBufferedIOThreshold(int bufferedIOThreshold) {
		this.bufferedIOThreshold = bufferedIOThreshold;
	}

	public long getAwaitTimeout() {
		return this.awaitTimeout;
	}

	public void setAwaitTimeout(long awaitTimeout) {
		this.awaitTimeout = awaitTimeout;
	}

}
