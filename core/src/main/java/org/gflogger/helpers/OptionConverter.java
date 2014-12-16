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

package org.gflogger.helpers;

import java.util.Properties;

// Contributors:   Avy Sharell (sharell@online.fr)
//				 Matthieu Verbert (mve@zurich.ibm.com)
//				 Colin Sampaleanu

/**
 * A convenience class to convert property values to specific types.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Simon Kitching;
 * @author Anders Kristensen
 */
public class OptionConverter {

	static String DELIM_START	 = "${";
	static char   DELIM_STOP	  = '}';
	static int	DELIM_START_LEN = 2;
	static int	DELIM_STOP_LEN  = 1;

	/** OptionConverter is a static class. */
	private OptionConverter() {
	}

	public static String[] concatanateArrays(String[] l, String[] r) {
		int len = l.length + r.length;
		String[] a = new String[len];

		System.arraycopy(l, 0, a, 0, l.length);
		System.arraycopy(r, 0, a, l.length, r.length);

		return a;
	}

	public static String convertSpecialChars(String s) {
		char c;
		int len = s.length();
		StringBuffer sbuf = new StringBuffer(len);

		int i = 0;
		while (i < len) {
			c = s.charAt(i++);
			if (c == '\\') {
				c = s.charAt(i++);
				if (c == 'n')
					c = '\n';
				else if (c == 'r')
					c = '\r';
				else if (c == 't')
					c = '\t';
				else if (c == 'f')
					c = '\f';
				else if (c == '\b')
					c = '\b';
				else if (c == '\"')
					c = '\"';
				else if (c == '\'')
					c = '\'';
				else if (c == '\\')
					c = '\\';
			}
			sbuf.append(c);
		}
		return sbuf.toString();
	}

	/**
	 * Very similar to <code>System.getProperty</code> except that the
	 * {@link SecurityException} is hidden.
	 *
	 * @param key
	 *			The key to search for.
	 * @param def
	 *			The default value to return.
	 * @return the string value of the system property, or the default value if
	 *		 there is no property with that key.
	 * @since 1.1
	 */
	public static String getSystemProperty(String key, String def) {
		try {
			return System.getProperty(key, def);
		} catch (Throwable e) { // MS-Java throws
								// com.ms.security.SecurityExceptionEx
			LogLog.debug("Was not allowed to read system property \"" + key + "\".");
			return def;
		}
	}

	public static String getStringProperty(final String name, final String defaultValue){
		final String propValue = System.getProperty(name);
		return propValue != null ? propValue : defaultValue;
	}

	public static boolean getBooleanProperty(final String name, final boolean defaultValue){
		final String propValue = System.getProperty(name);
		if (propValue != null) {
			try {
				return Boolean.parseBoolean(propValue);
			} catch (Throwable e){
				// nothing
			}
		}
		return defaultValue;
	}

	public static int getIntProperty(final String name, final int defaultValue){
		final String propValue = System.getProperty(name);
		if (propValue != null) {
			try {
				return Integer.parseInt(propValue);
			} catch (Throwable e){
				// nothing
			}
		}
		return defaultValue;
	}

	/**
	 * If <code>value</code> is "true", then <code>true</code> is returned. If
	 * <code>value</code> is "false", then <code>true</code> is returned.
	 * Otherwise, <code>default</code> is returned.
	 *
	 * <p>
	 * Case of value is unimportant.
	 */
	public static boolean toBoolean(String value, boolean dEfault) {
		if (value == null)
			return dEfault;
		String trimmedVal = value.trim();
		if ("true".equalsIgnoreCase(trimmedVal))
			return true;
		if ("false".equalsIgnoreCase(trimmedVal))
			return false;
		return dEfault;
	}

	public static int toInt(String value, int dEfault) {
		if (value != null) {
			String s = value.trim();
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				LogLog.error("[" + s + "] is not in proper int form.");
				e.printStackTrace();
			}
		}
		return dEfault;
	}

	public static long toFileSize(String value, long dEfault) {
		if (value == null)
			return dEfault;

		String s = value.trim().toUpperCase();
		long multiplier = 1;
		int index;

		if ((index = s.indexOf("KB")) != -1) {
			multiplier = 1024;
			s = s.substring(0, index);
		} else if ((index = s.indexOf("MB")) != -1) {
			multiplier = 1024 * 1024;
			s = s.substring(0, index);
		} else if ((index = s.indexOf("GB")) != -1) {
			multiplier = 1024 * 1024 * 1024;
			s = s.substring(0, index);
		}
		if (s != null) {
			try {
				return Long.parseLong(s) * multiplier;
			} catch (NumberFormatException e) {
				LogLog.error("[" + s + "] is not in proper int form.");
				LogLog.error("[" + value + "] not in expected format.", e);
			}
		}
		return dEfault;
	}

	/**
	 * Perform variable substitution in string <code>val</code> from the values
	 * of keys found in the system propeties.
	 *
	 * <p>
	 * The variable substitution delimeters are <b>${</b> and <b>}</b>.
	 *
	 * <p>
	 * For example, if the System properties contains "key=value", then the call
	 *
	 * <pre>
	 * String s = OptionConverter.substituteVars(&quot;Value of key is ${key}.&quot;);
	 * </pre>
	 *
	 * will set the variable <code>s</code> to "Value of key is value.".
	 *
	 * <p>
	 * If no value could be found for the specified key, then the
	 * <code>props</code> parameter is searched, if the value could not be found
	 * there, then substitution defaults to the empty string.
	 *
	 * <p>
	 * For example, if system propeties contains no value for the key
	 * "inexistentKey", then the call
	 *
	 * <pre>
	 * String s = OptionConverter.subsVars(&quot;Value of inexistentKey is [${inexistentKey}]&quot;);
	 * </pre>
	 *
	 * will set <code>s</code> to "Value of inexistentKey is []"
	 *
	 * <p>
	 * An {@link java.lang.IllegalArgumentException} is thrown if
	 * <code>val</code> contains a start delimeter "${" which is not balanced by
	 * a stop delimeter "}".
	 * </p>
	 *
	 * <p>
	 * <b>Author</b> Avy Sharell</a>
	 * </p>
	 *
	 * @param val
	 *			The string on which variable substitution is performed.
	 * @throws IllegalArgumentException
	 *			 if <code>val</code> is malformed.
	 */
	public static String substVars(String val, Properties props) throws IllegalArgumentException {
		if (val == null) return val;
		StringBuilder sbuf = new StringBuilder();

		int i = 0;
		int j, k;

		while (true) {
			j = val.indexOf(DELIM_START, i);
			if (j == -1) {
				// no more variables
				if (i == 0) { // this is a simple string
					return val;
				}
				// add the tail string which contails no variables and
				// return the result.
				sbuf.append(val.substring(i, val.length()));
				return sbuf.toString();
			}
			sbuf.append(val.substring(i, j));
			k = val.indexOf(DELIM_STOP, j);
			if (k == -1) {
				throw new IllegalArgumentException('"' + val
						+ "\" has no closing brace. Opening brace at position " + j + '.');
			}
			j += DELIM_START_LEN;
			String key = val.substring(j, k);
			// first try in System properties
			String replacement = getSystemProperty(key, null);
			// then try props parameter
			if (replacement == null && props != null) {
				replacement = props.getProperty(key);
			}

			if (replacement != null) {
				// Do variable substitution on the replacement string
				// such that we can solve "Hello ${x2}" as "Hello p1"
				// the where the properties are
				// x1=p1
				// x2=${x1}
				String recursiveReplacement = substVars(replacement, props);
				sbuf.append(recursiveReplacement);
			}
			i = k + DELIM_STOP_LEN;
		}
	}

}
