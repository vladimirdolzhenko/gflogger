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

import static org.gflogger.DefaultObjectFormatter.DEFAULT_OBJECT_FORMATTER;

import java.util.Map;

/**
 * @author Ruslan Cheremin, cheremin@gmail.com
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DefaultObjectFormatterFactory implements ObjectFormatterFactory {

	private final TypeEntityRegistry<ObjectFormatter> formatters = new TypeEntityRegistry<ObjectFormatter>(
			DEFAULT_OBJECT_FORMATTER
	);

	public DefaultObjectFormatterFactory() {
	}

	public <T> void registerObjectFormatter( final Class<T> clazz,
	                                         final ObjectFormatter<T> formatter ) {
		formatters.register( clazz, formatter );
	}

	public void setExtraObjectFormatters( final Map<Class, ObjectFormatter> formatters ) {
		for( final Map.Entry<Class, ObjectFormatter> entry : formatters.entrySet() ) {
			registerObjectFormatter( entry.getKey(), entry.getValue() );
		}
	}

	@Override
	public ObjectFormatter getObjectFormatter( final Object obj ) {
		if( obj == null ) {
			return DEFAULT_OBJECT_FORMATTER;
		}

		final Class type = obj.getClass();
		return formatters.forType( type );
	}
}
