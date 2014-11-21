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

package org.gflogger;


/**
 * Appender
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public interface Appender<T extends LogEntryItem> {

	boolean isMultibyte();

	boolean isEnabled();

	LogLevel getLogLevel();

	String getName();

	void flush();

	void flush(boolean force);

	void process(T entry);

	/**
	 * @deprecated we're going to replace all it's usage with {@linkplain #stop()}
	 *             and remove it in future versions. Be aware
	 */
	@Deprecated
	void workerIsAboutToFinish();

	void onUncatchException(Throwable e);

	void start();

	void stop();
}
