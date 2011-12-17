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

package gflogger;

/**
 * MockLogEntry
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class MockLogEntry implements LogEntry {

	@Override
	public LogEntry append(final char c) {
		return this;
	}

	@Override
	public LogEntry append(final CharSequence csq) {
		return this;
	}

	@Override
	public LogEntry append(final CharSequence csq, final int start, final int end) {
		return this;
	}

	@Override
	public LogEntry append(final boolean b) {
		return this;
	}

	@Override
	public LogEntry append(final byte i) {
		return this;
	}

	@Override
	public LogEntry append(final short i) {
		return this;
	}

	@Override
	public LogEntry append(final int i) {
		return this;
	}

	@Override
	public LogEntry append(final long i) {
		return this;
	}

	@Override
	public LogEntry append(final double i, final int precision) {
		return this;
	}
	
	@Override
	public LogEntry append(Throwable e) {
		return this;
	}
	
	@Override
	public LogEntry append(Object o) {
		return this;
	}

	@Override
	public void commit() {
		// nothing
	}
}
