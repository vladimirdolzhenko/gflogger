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

import java.nio.Buffer;

/**
 * LocalLogEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public interface LocalLogEntry extends GFLogEntry, FormattedGFLogEntry {

	LogLevel getLogLevel();

	void setLogLevel(final LogLevel logLevel);

	void setCategoryName(String categoryName);

	void setPattern(final String pattern);

	void setAppenderMask(final long mask);

	String getCategoryName();

	String getThreadName();

	long getAppenderMask();

	<T extends Buffer> void copyTo(T buffer);

	void clear();

	boolean isCommited();

	void setCommited(boolean commited);

	Throwable getError();

	String stringValue();

}
