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

	LogEntry append(byte i);

	LogEntry append(short i);

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
	 * appends a single char if condition is true
	 * @param condition
	 * @param c char to add
	 * @return a reference to this object.
	 */
	LogEntry appendIf(boolean condition, char c);

	/**
	 * appends char sequence if condition is true
	 * @param condition
	 * @param csq char sequence to add
	 * @return a reference to this object.
	 */
	LogEntry appendIf(boolean condition, CharSequence csq);

	LogEntry appendIf(boolean condition, CharSequence csq, int start, int end);

	LogEntry appendIf(boolean condition, boolean b);

	LogEntry appendIf(boolean condition, byte i);

	LogEntry appendIf(boolean condition, short i);

	LogEntry appendIf(boolean condition, int i);

	LogEntry appendIf(boolean condition, long i);

	LogEntry appendIf(boolean condition, double i, int precision);

	LogEntry appendIf(boolean condition, Throwable e);

	LogEntry appendIf(boolean condition, Loggable loggable);

	/**
	 * appends string representation of an object  if condition is true
	 * using <code>o.toString()</code> method
	 *
	 * Leads to garbage footprint !
	 *
	 * @param condition
	 * @param o
	 * @return
	 */
	LogEntry appendIf(boolean condition, Object o);

	/**
	 * commit an entry
	 */
	void commit();

}
