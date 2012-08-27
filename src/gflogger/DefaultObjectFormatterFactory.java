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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DefaultObjectFormatterFactory implements ObjectFormatterFactory {

	private final ConcurrentMap<Class, ObjectFormatter> formatters =
		new ConcurrentHashMap<Class, ObjectFormatter>();

	public DefaultObjectFormatterFactory() {
		registerObjectFormatter(Object.class,
			new DefaultObjectFormatter());
	}

	public <T> void registerObjectFormatter(final Class<T> clazz, final ObjectFormatter<T> formatter){
		this.formatters.put(clazz, formatter);
	}

	public void setExtraObjectFormatters(final Map<Class, ObjectFormatter> formatters){
		this.formatters.putAll(formatters);
	}

	@Override
	public ObjectFormatter getObjectFormatter(final Object obj){
		final Class origClass = obj != null ? obj.getClass() : Object.class;
		ObjectFormatter formatter = formatters.get(origClass);
		if (formatter != null) return formatter;

		Class classClazz = origClass;
		Class interfaceClazz = origClass;

		ObjectFormatter classFormatter = formatter;
		ObjectFormatter interfaceFormatter = formatter;
		int classLength = 0;
		int intefaceLength = 0;

		// lookup over classes: class - super class - super - super class and so on
		while (classFormatter == null){
			classClazz = classClazz.getSuperclass();
			classFormatter = formatters.get(classClazz);
			classLength++;
		}

		// Object class formatter (default) has a low priority
		classLength += Object.class.equals(classClazz) ? 1 << 10 : 0;

		// after lookup for interfaces
		while (interfaceFormatter == null){
			intefaceLength++;
			interfaceClazz = interfaceClazz != null ? interfaceClazz : Object.class;

			interfaceFormatter = depthSearchOverInterfaces(interfaceClazz.getInterfaces());
			if (interfaceFormatter != null) break;

			interfaceClazz = interfaceClazz.getSuperclass();
		}

		// the nearest class has more priority than interface
		Class clazz = classLength <= intefaceLength ? classClazz : interfaceClazz;
		formatter = classLength <= intefaceLength ? classFormatter : interfaceFormatter;

		if (!origClass.equals(clazz)){
			formatters.put(clazz, formatter);
		}
		return formatter;
	}

	private ObjectFormatter depthSearchOverInterfaces(Class ... interfaces){
		ObjectFormatter formatter = null;
		for (int i = 0; i < interfaces.length; i++) {
			formatter = formatters.get(interfaces[i]);
			if (formatter != null) {
				formatters.put(interfaces[i], formatter);
				return formatter;
			}
			formatter = depthSearchOverInterfaces(interfaces[i].getInterfaces());
			if (formatter != null) break;
		}
		return formatter;
	}

	private static class DefaultObjectFormatter implements ObjectFormatter<Object> {

		@Override
		public void append(Object obj, LogEntry entry) {
			if (obj != null){
				entry.append(obj.toString());
			} else {
				entry.append('n').append('u').append('l').append('l');
			}
		}

		@Override
		public String toString() {
			return "DefaultObjectFormatter";
		}
	}
}
