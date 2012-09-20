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
 * NullLogEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class NullLogEntry implements GFLogEntry, FormattedGFLogEntry {

	public static final NullLogEntry INSTANCE = new NullLogEntry();

	public NullLogEntry() {
	}

	@Override
	public GFLogEntry append(final char c) {
		return this;
	}

	@Override
	public GFLogEntry append(final CharSequence csq) {
		return this;
	}

	@Override
	public GFLogEntry append(final CharSequence csq, final int start, final int end) {
		return this;
	}

	@Override
	public GFLogEntry append(final boolean b) {
		return this;
	}

	@Override
	public GFLogEntry append(final int i) {
		return this;
	}

	@Override
	public GFLogEntry append(final long i) {
		return this;
	}

	@Override
	public GFLogEntry append(final double i, final int precision) {
		return this;
	}

	@Override
	public GFLogEntry append(Throwable e) {
		return this;
	}

	@Override
	public GFLogEntry append(Loggable loggable) {
		return this;
	}

	@Override
	public GFLogEntry append(Object o) {
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
	public FormattedGFLogEntry with(char c) {
		return this;
	}

	@Override
	public FormattedGFLogEntry with(CharSequence csq) {
		return this;
	}

	@Override
	public FormattedGFLogEntry with(CharSequence csq, int start, int end) {
		return this;
	}

	@Override
	public FormattedGFLogEntry with(boolean b) {
		return this;
	}

	@Override
	public FormattedGFLogEntry with(int i) {
		return this;
	}

	@Override
	public FormattedGFLogEntry with(long i) {
		return this;
	}

	@Override
	public FormattedGFLogEntry with(double i, int precision) {
		return this;
	}

	@Override
	public FormattedGFLogEntry with(Throwable e) {
		return this;
	}

	@Override
	public FormattedGFLogEntry with(Loggable loggable) {
		return this;
	}

	@Override
	public FormattedGFLogEntry with(Object o) {
		return this;
	}

	@Override
	public void withLast(char c) {
		// nothing
	}

	@Override
	public void withLast(CharSequence csq) {
		// nothing
	}

	@Override
	public void withLast(CharSequence csq, int start, int end) {
		// nothing
	}

	@Override
	public void withLast(boolean b) {
		// nothing
	}

	@Override
	public void withLast(int i) {
		// nothing
	}

	@Override
	public void withLast(long i) {
		// nothing
	}

	@Override
	public void withLast(double i, int precision) {
		// nothing
	}

	@Override
	public void withLast(Throwable e) {
		// nothing
	}

	@Override
	public void withLast(Loggable loggable) {
		// nothing
	}

	@Override
	public void withLast(Object o) {
		// nothing
	}


}
