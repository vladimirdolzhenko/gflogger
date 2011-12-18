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
package gflogger.util;

import java.net.URL;

/**
 * StackTraceUtils
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class StackTraceUtils {

	public static String getCodeLocation(Class type) {
		try {
			if (type != null) {
				// file:/C:/java/maven-2.0.8/repo/com/icegreen/greenmail/1.3/greenmail-1.3.jar
				URL resource = type.getProtectionDomain().getCodeSource().getLocation();
				if (resource != null) {
					String locationStr = resource.toString();
					// now lets remove all but the file name
					String result = getCodeLocation(locationStr, '/');
					if (result != null) {
						return result;
					}
					return getCodeLocation(locationStr, '\\');
				}
			}
		} catch (Exception e) {
			// ignore
		}
		return "na";
	}

	public static String getCodeLocation(String locationStr, char separator) {
		int idx = locationStr.lastIndexOf(separator);
		if (isFolder(idx, locationStr)) {
			idx = locationStr.lastIndexOf(separator, idx - 1);
			return locationStr.substring(idx + 1);
		} else if (idx > 0) {
			return locationStr.substring(idx + 1);
		}
		return null;
	}
	
	
	public static String getImplementationVersion(Class clazz) {
		if (clazz == null) return null;
		
		final Package pkg = clazz.getPackage();
		if (pkg != null) {
			String v = pkg.getImplementationVersion();
			if (v != null) return v;
		}
		return null;
	}
	
	
	private static boolean isFolder(int idx, String text) {
		return (idx != -1 && idx + 1 == text.length());
	}

	public static Class loadClass(String className) {
		return loadClass(Thread.currentThread().getContextClassLoader(), className);
	}
	
	public static Class loadClass(ClassLoader cl, String className) {
		if (cl == null) {
			return null;
		}
		try {
			return cl.loadClass(className);
		} catch (ClassNotFoundException e1) {
			return null;
		} catch (NoClassDefFoundError e1) {
			return null;
		} catch (Exception e) {
			e.printStackTrace(); // this is unexpected
			return null;
		}
	}
}
