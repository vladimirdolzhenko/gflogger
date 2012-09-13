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

public interface FormattedLogEntry {

	/**
	 * appends with a single char parameter
	 * @return a reference to this object.
	 */
	FormattedLogEntry with(char c);

	FormattedLogEntry with(CharSequence csq);

	FormattedLogEntry with(CharSequence csq, int start, int end);

	FormattedLogEntry with(boolean b);

	FormattedLogEntry with(int i);

	FormattedLogEntry with(long i);

	FormattedLogEntry with(double i, int precision);

	FormattedLogEntry with(Throwable e);

	FormattedLogEntry with(Loggable loggable);

	/**
	 * appends with a string representation of an object using <code>o.toString()</code> method
	 *
	 * Leads to garbage footprint !
	 *
	 * @param o
	 * @return
	 */
	FormattedLogEntry with(Object o);


	/**
	 * with a last single char parameter
	 * @return a reference to this object.
	 */
	void withLast(char c);

	void withLast(CharSequence csq);

	void withLast(CharSequence csq, int start, int end);

	void withLast(boolean b);

	void withLast(int i);

	void withLast(long i);

	void withLast(double i, int precision);

	void withLast(Throwable e);

	void withLast(Loggable loggable);

	/**
	 * with a last parameter string representation of an object using <code>o.toString()</code> method
	 *
	 * Leads to garbage footprint !
	 *
	 * @param o
	 */
	void withLast(Object o);
}
