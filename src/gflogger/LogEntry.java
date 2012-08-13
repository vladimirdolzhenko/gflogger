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
 * LogEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public interface LogEntry {

	/**
	 * appends a single char
	 * @return a reference to this object.
	 */
	LogEntry append(char c);

	LogEntry append(CharSequence csq);

	LogEntry append(CharSequence csq, int start, int end);

	LogEntry append(boolean b);

	LogEntry append(int i);

	LogEntry append(long i);

	LogEntry append(double i, int precision);

	LogEntry append(Throwable e);

	LogEntry append(Loggable loggable);

	/**
	 * appends string representation of an object using <code>o.toString()</code> method
	 *
	 * Leads to garbage footprint !
	 *
	 * @param o
	 * @return
	 */
	LogEntry append(Object o);

	/**
	 * appends last a single char
	 * @param c char to add
	 * @return a reference to this object.
	 */
	void appendLast(char c);

	/**
	 * appends last char sequence
	 * @param csq char sequence to add
	 * @return a reference to this object.
	 */
	void appendLast(CharSequence csq);

	void appendLast(CharSequence csq, int start, int end);

	void appendLast(boolean b);

	void appendLast(int i);

	void appendLast(long i);

	void appendLast(double i, int precision);

	void appendLast(Throwable e);

	void appendLast(Loggable loggable);

	/**
	 * appends last string representation of an object
	 * using <code>o.toString()</code> method
	 *
	 * Leads to garbage footprint !
	 *
	 * @param o
	 * @return
	 */
	void appendLast(Object o);

	/**
	 * commit an entry
	 */
	void commit();

}
