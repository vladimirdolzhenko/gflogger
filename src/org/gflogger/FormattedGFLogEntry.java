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
 * FormattedGFLogEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public interface FormattedGFLogEntry {

	/**
	 * appends with a single char parameter
	 * @return a reference to this object.
	 */
	FormattedGFLogEntry with(char c);

	FormattedGFLogEntry with(CharSequence csq);

	FormattedGFLogEntry with(CharSequence csq, int start, int end);

	FormattedGFLogEntry with(boolean b);

	FormattedGFLogEntry with(int i);

	FormattedGFLogEntry with(long i);

	FormattedGFLogEntry with(double i, int precision);

	<T> FormattedGFLogEntry with(T[] array, String separator);

	/**
	 * <b>Note</b>: leads to garbage footprint
	 *
	 * @param iterable
	 * @param separator
	 * @return
	 */
	<T> FormattedGFLogEntry with(Iterable<T> iterable, String separator);

	FormattedGFLogEntry with(Throwable e);

	FormattedGFLogEntry with(Loggable loggable);

	/**
	 * appends with a string representation of an object using <code>o.toString()</code> method
	 *
	 * <b>Note</b>: could lead to garbage footprint
	 *
	 * @param o
	 * @return
	 */
	FormattedGFLogEntry with(Object o);

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

	<T> void withLast(T[] array, String separator);

	/**
	 * <b>Note</b>: leads to garbage footprint
	 *
	 * @param iterable
	 * @param separator
	 */
	<T> void withLast(Iterable<T> iterable, String separator);

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
