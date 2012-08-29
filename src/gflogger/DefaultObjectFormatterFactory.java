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

import static gflogger.DefaultObjectFormatter.DEFAULT_OBJECT_FORMATTER;

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
			if (classClazz == null) {
				classLength = Integer.MAX_VALUE;
				break;
			}
			classFormatter = formatters.get(classClazz);
			classLength++;
		}

		// after lookup for interfaces
		while (interfaceFormatter == null){

			final int len =
				depthSearchOverInterfacesLength(interfaceClazz.getInterfaces());
			if (len != Integer.MAX_VALUE){
				interfaceFormatter = depthSearchOverInterfaces(interfaceClazz.getInterfaces());
				if (interfaceFormatter != null) {
					intefaceLength += len;
					break;
				}
			}

			interfaceClazz = interfaceClazz.getSuperclass();
			if (interfaceClazz == null) break;
		}

		// the nearest class has more priority than interface
		Class clazz = classLength <= intefaceLength ? classClazz : interfaceClazz;
		formatter = classLength <= intefaceLength ? classFormatter : interfaceFormatter;

		// there is no any registered for this type of object formatter
		// use the default one
		if (clazz == null){
			clazz = Object.class;
			formatter = DEFAULT_OBJECT_FORMATTER;
		}

		if (!origClass.equals(clazz)){
			formatters.put(clazz, formatter);
		}
		return formatter;
	}

	private int depthSearchOverInterfacesLength(Class ... interfaces){
		for (int i = 0; i < interfaces.length; i++) {
			ObjectFormatter formatter = formatters.get(interfaces[i]);
			if (formatter != null) {
				return 1;
			}
			int length = depthSearchOverInterfacesLength(interfaces[i].getInterfaces());
			if (length != Integer.MAX_VALUE) {
				return length + 1;
			}
		}
		return Integer.MAX_VALUE;
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
}
