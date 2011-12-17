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

package gflogger.helpers;

/**
 * This class used to output log statements from within the gflogger package.
 * 
 * <p>
 * Log4j components cannot make gflogger logging calls. However, it is sometimes
 * useful for the user to learn about what gflogger is doing. You can enable gflogger
 * internal logging by defining the <b>gflogger.configDebug</b> variable.
 * 
 * <p>
 * All gflogger internal debug calls go to <code>System.out</code> where as
 * internal error messages are sent to <code>System.err</code>. All internal
 * messages are prepended with the string "gflogger: ".
 * 
 * @since 0.8.2
 * @author Ceki G&uuml;lc&uuml;
 * @author Vladimir Dolzhenko
 */
public class LogLog {

	/**
	 * Defining this value makes gflogger print gflogger-internal debug statements to
	 * <code>System.out</code>.
	 * 
	 * <p>
	 * The value of this string is <b>gflogger.debug</b>.
	 * 
	 * <p>
	 * Note that the search for all option names is case sensitive.
	 */
	public static final String  DEBUG_KEY		= "logger.debug";

	/**
	 * Defining this value makes gflogger components print gflogger-internal debug
	 * statements to <code>System.out</code>.
	 * 
	 * <p>
	 * The value of this string is <b>gflogger.configDebug</b>.
	 * 
	 * <p>
	 * Note that the search for all option names is case sensitive.
	 * 
	 * @deprecated Use {@link #DEBUG_KEY} instead.
	 */
	public static final String  CONFIG_DEBUG_KEY = "logger.configDebug";

	protected static boolean	debugEnabled	 = false;

	/**
	 * In quietMode not even errors generate any output.
	 */
	private static boolean	  quietMode		= false;

	private static final String PREFIX		   = "gflogger: ";
	private static final String ERR_PREFIX	   = "gflogger:ERROR ";
	private static final String WARN_PREFIX	   = "gflogger:WARN ";

	static {
		String key = OptionConverter.getSystemProperty(DEBUG_KEY, null);

		if (key == null) {
			key = OptionConverter.getSystemProperty(CONFIG_DEBUG_KEY, null);
		}

		if (key != null) {
			debugEnabled = OptionConverter.toBoolean(key, true);
		}
	}

	/**
	 * Allows to enable/disable gflogger internal logging.
	 */
	static public void setInternalDebugging(boolean enabled) {
		debugEnabled = enabled;
	}

	/**
	 * This method is used to output gflogger internal debug statements. Output
	 * goes to <code>System.out</code>.
	 */
	public static void debug(String msg) {
		if (debugEnabled && !quietMode) {
			System.out.println(PREFIX + msg);
		}
	}

	/**
	 * This method is used to output gflogger internal debug statements. Output
	 * goes to <code>System.out</code>.
	 */
	public static void debug(String msg, Throwable t) {
		if (debugEnabled && !quietMode) {
			System.out.println(PREFIX + msg);
			if (t != null)
				t.printStackTrace(System.out);
		}
	}

	/**
	 * This method is used to output gflogger internal error statements. There is
	 * no way to disable error statements. Output goes to
	 * <code>System.err</code>.
	 */
	public static void error(String msg) {
		if (quietMode)
			return;
		System.err.println(ERR_PREFIX + msg);
	}

	/**
	 * This method is used to output gflogger internal error statements. There is
	 * no way to disable error statements. Output goes to
	 * <code>System.err</code>.
	 */
	public static void error(String msg, Throwable t) {
		if (quietMode)
			return;

		System.err.println(ERR_PREFIX + msg);
		if (t != null) {
			t.printStackTrace();
		}
	}

	/**
	 * In quite mode no LogLog generates strictly no output, not even for
	 * errors.
	 * 
	 * @param quietMode
	 *			A true for not
	 */
	public static void setQuietMode(boolean quietMode) {
		LogLog.quietMode = quietMode;
	}

	/**
	 * This method is used to output gflogger internal warning statements. There is
	 * no way to disable warning statements. Output goes to
	 * <code>System.err</code>.
	 */
	public static void warn(String msg) {
		if (quietMode)
			return;

		System.err.println(WARN_PREFIX + msg);
	}

	/**
	 * This method is used to output gflogger internal warnings. There is no way to
	 * disable warning statements. Output goes to <code>System.err</code>.
	 */
	public static void warn(String msg, Throwable t) {
		if (quietMode)
			return;

		System.err.println(WARN_PREFIX + msg);
		if (t != null) {
			t.printStackTrace();
		}
	}
}
