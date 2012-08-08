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
 * NullLogEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class NullLogEntry implements LogEntry, FormattedLogEntry {

	public static final NullLogEntry INSTANCE = new NullLogEntry();

	public NullLogEntry() {
	}

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
	public LogEntry append(Loggable loggable) {
		return this;
	}

	@Override
	public LogEntry append(Object o) {
		return this;
	}

	@Override
	public void appendLast(char c) {
		// nothing
	}

	@Override
	public void appendLast(CharSequence csq) {
		// nothing
	}

	@Override
	public void appendLast(CharSequence csq, int start, int end) {
		// nothing
	}

	@Override
	public void appendLast(boolean b) {
		// nothing
	}

	@Override
	public void appendLast(byte i) {
		// nothing
	}

	@Override
	public void appendLast(short i) {
		// nothing
	}

	@Override
	public void appendLast(int i) {
		// nothing
	}

	@Override
	public void appendLast(long i) {
		// nothing
	}

	@Override
	public void appendLast(double i, int precision) {
		// nothing
	}

	@Override
	public void appendLast(Throwable e) {
		// nothing
	}

	@Override
	public void appendLast(Loggable loggable) {
		// nothing
	}

	@Override
	public void appendLast(Object o) {
		// nothing
	}

	@Override
	public void commit() {
		// nothing
	}

	@Override
	public FormattedLogEntry with(char c) {
		return this;
	}

	@Override
	public FormattedLogEntry with(CharSequence csq) {
		return this;
	}

	@Override
	public FormattedLogEntry with(CharSequence csq, int start, int end) {
		return this;
	}

	@Override
	public FormattedLogEntry with(boolean b) {
		return this;
	}

	@Override
	public FormattedLogEntry with(byte i) {
		return this;
	}

	@Override
	public FormattedLogEntry with(short i) {
		return this;
	}

	@Override
	public FormattedLogEntry with(int i) {
		return this;
	}

	@Override
	public FormattedLogEntry with(long i) {
		return this;
	}

	@Override
	public FormattedLogEntry with(double i, int precision) {
		return this;
	}

	@Override
	public FormattedLogEntry with(Throwable e) {
		return this;
	}

	@Override
	public FormattedLogEntry with(Loggable loggable) {
		return this;
	}

	@Override
	public FormattedLogEntry with(Object o) {
		return this;
	}

	@Override
	public void endWith(char c) {
		// nothing
	}

	@Override
	public void endWith(CharSequence csq) {
		// nothing
	}

	@Override
	public void endWith(CharSequence csq, int start, int end) {
		// nothing
	}

	@Override
	public void endWith(boolean b) {
		// nothing
	}

	@Override
	public void endWith(byte i) {
		// nothing
	}

	@Override
	public void endWith(short i) {
		// nothing
	}

	@Override
	public void endWith(int i) {
		// nothing
	}

	@Override
	public void endWith(long i) {
		// nothing
	}

	@Override
	public void endWith(double i, int precision) {
		// nothing
	}

	@Override
	public void endWith(Throwable e) {
		// nothing
	}

	@Override
	public void endWith(Loggable loggable) {
		// nothing
	}

	@Override
	public void endWith(Object o) {
		// nothing
	}


}
