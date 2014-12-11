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

import org.gflogger.Layout;
import org.gflogger.LogEntryItemImpl;
import org.gflogger.LogLevel;
import org.gflogger.PatternLayout;
import org.gflogger.helpers.LogLog;

/**
*
* @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
*/
public abstract class AbstractAsyncAppender extends AbstractAppender {

	private static final int DEFAULT_BUFFER_SIZE = 1 << 22;

	protected boolean			immediateFlush		= false;
	protected int				bufferedIOThreshold	= 100;
	protected long				awaitTimeout		= 10L;

	// runtime changing properties

	protected volatile boolean	running				= false;

	protected AbstractAsyncAppender(final String name,
	                                final boolean multibyte,
	                                final LogLevel logLevel,
	                                final boolean enabled) {
		this(name,
				DEFAULT_BUFFER_SIZE/*4Mb*/,
				multibyte,
				logLevel, enabled
		);
	}

	protected AbstractAsyncAppender(final String name,
	                                final int bufferSize,
	                                final boolean multibyte,
	                                final LogLevel logLevel,
	                                final boolean enabled) {
		super(name, bufferSize, multibyte , logLevel, enabled);
	}

	public void setLayout(final Layout layout) {
		this.buffer.setLayout(layout);
	}

	public boolean isImmediateFlush() {
		return immediateFlush;
	}

	public void setImmediateFlush(final boolean immediateFlush) {
		this.immediateFlush = immediateFlush;
	}

	public void setBufferedIOThreshold(final int bufferedIOThreshold) {
		this.bufferedIOThreshold = bufferedIOThreshold;
	}

	public void setAwaitTimeout(final long awaitTimeout) {
		this.awaitTimeout = awaitTimeout;
	}

	@Override
	public void process(LogEntryItemImpl entry) {
		if(!enabled || logLevel.greaterThan(entry.getLogLevel())) return;

		buffer.process(entry);
	}

	@Override
	public void flush(){
		flush(true);
	}

	@Override
	public void start() {
		if (running) throw new IllegalStateException("Already running");

		LogLog.debug(getName() + " is starting ");

		if (buffer.getLayout() == null){
			buffer.setLayout(new PatternLayout());
		}
		buffer.start();
		running = true;
	}

	/**
	 * @deprecated it is about to be removed in future versions, use {@linkplain #stop()}
	 *             instead
	 */
	@Deprecated
	protected void workerIsAboutToFinish(){
		flush();
		buffer.stop();
	}

	@Override
	public void stop(){
		if (!running) return;
		LogLog.debug(getName() + " is stopping ");
		workerIsAboutToFinish();
		running = false;
	}
}
