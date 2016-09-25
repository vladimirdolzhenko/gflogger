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

import java.util.Locale;
import java.util.TimeZone;

import org.gflogger.Appender;
import org.gflogger.Layout;
import org.gflogger.LogLevel;
import org.gflogger.LoggerService;
import org.gflogger.PatternLayout;

import static org.gflogger.helpers.OptionConverter.getBooleanProperty;
import static org.gflogger.helpers.OptionConverter.getIntProperty;
import static org.gflogger.helpers.OptionConverter.getStringProperty;

/**
 * AbstractAppenderFactory
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public abstract class AbstractAppenderFactory<A extends Appender> implements AppenderFactory<A> {

	protected int bufferSize;
	protected boolean multibyte;
	protected LogLevel logLevel;
	protected TimeZone timeZone;
	protected Locale locale;
	protected String layoutPattern;
	protected Layout layout;
	protected boolean immediateFlush;
	protected int bufferedIOThreshold;
	protected long awaitTimeout;
	protected boolean enabled;
	protected int index;

	public AbstractAppenderFactory() {
		// 1M
		bufferSize = getIntProperty("gflogger.buffer.size", 1 << 20);
		multibyte = getBooleanProperty("gflogger.multibyte", false);
		logLevel = LogLevel.valueOf(getStringProperty("gflogger.loglevel", "TRACE"));
		timeZone = getStringProperty("gflogger.timeZoneId", null) != null
			? TimeZone.getTimeZone(getStringProperty("gflogger.timeZoneId", null)) : null;
		locale = getStringProperty("gflogger.language", null) != null
			? new Locale(getStringProperty("gflogger.language", null)) : null;
		layoutPattern = getStringProperty("gflogger.pattern", "%m%n");
		immediateFlush = getBooleanProperty("gflogger.immediateFlush", false);
		bufferedIOThreshold = getIntProperty("gflogger.bufferedIOThreshold", 100);
		awaitTimeout = getIntProperty("gflogger.awaitTimeout", 10);
		enabled = true;
	}

	protected void preinit(Class<? extends LoggerService> loggerServiceClass) {
		if (layout == null) {
			layout = new PatternLayout(layoutPattern, timeZone, locale);
		}
	}

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

	@Override
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

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public String getTimeZoneId() {
		return timeZone != null ? timeZone.getID() : null;
	}

	public void setTimeZoneId(String timeZoneId) {
		this.timeZone = timeZoneId != null ? TimeZone.getTimeZone(timeZoneId) : null;
	}

	public String getLayoutPattern() {
		return this.layoutPattern;
	}

	public void setLayoutPattern(String layoutPattern) {
		this.layoutPattern = layoutPattern;
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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public void setIndex(int index) {
		this.index = index;
	}


}
