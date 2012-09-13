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

package org.gflogger.base.appender;

import org.gflogger.LogEntryItem;
import org.gflogger.base.LogEntryItemImpl;
import org.gflogger.ring.EntryProcessor;


public interface Appender<T extends LogEntryItem> extends org.gflogger.Appender, EntryProcessor, Runnable {

	void start();

	void stop();

	void workerIsAboutToFinish();

	void flush(boolean force);

	void process(LogEntryItemImpl entry);

}
