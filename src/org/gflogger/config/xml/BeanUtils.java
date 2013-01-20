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

package org.gflogger.config.xml;

import java.beans.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.TimeZone;

/**
*
* @author Ruslan Cheremin, cheremin@gmail.com
*/
public final class BeanUtils {
	private static final Class[] PRIMITIVE_MAPPING = new Class[] {
			boolean.class, Boolean.class,

			int.class, Integer.class,
			byte.class, Integer.class,
			short.class, Integer.class,
			long.class, Long.class,

			char.class, Character.class
	};

	private BeanUtils() {
		throw new AssertionError( "Not for instantiation" );
	}

	public static PropertyDescriptor[] classProperties( final Class clazz ) throws IntrospectionException {
		final BeanInfo beanInfo = Introspector.getBeanInfo( clazz );
		return beanInfo.getPropertyDescriptors();
	}

	/*public static Set<String> writeablePropertyNames( final Class clazz ) throws IntrospectionException {
		final HashSet<String> names = new HashSet<String>();
		for( final Method method : clazz.getMethods() ) {
			if(isPublic( method ) && !isStatic( method )){
				final String methodName = method.getName();
				final Class<?>[] parameterTypes = method.getParameterTypes();
				if(methodName.startsWith( "set" ) && parameterTypes.length == 1){
					final String propertyName = Introspector.decapitalize( methodName.substring( 3 ) );
					names.add( propertyName );
				}
			}
		}

		return names;
	}*/

	public static void setPropertyStringValue( final Object bean,
	                                           final PropertyDescriptor property,
	                                           final String value ) throws Exception {
		final PropertyEditor editor = property.createPropertyEditor( bean );
		if( editor != null ) {
			editor.setAsText( value );
		} else {
			final Method writeMethod = property.getWriteMethod();
			if( writeMethod != null ) {
				final Class paramClass = writeMethod.getParameterTypes()[0];
				try {
					final Object aValue = convert( value, paramClass );
					if( aValue != null ) {
						writeMethod.invoke( bean, aValue );
					}
				} catch( Exception e ) {
					//just skip
				}
			}
		}
	}

	public static void setPropertyValue( final Object bean,
	                                     final String propertyName,
	                                     final Object value ) throws Exception {
		final Class beanClass = bean.getClass();
		final Class valueClass = value.getClass();
		for( final Method method : beanClass.getMethods() ) {
			if( isPublic( method ) && !isStatic( method ) ) {
				final String methodName = method.getName();
				final Class<?>[] parameterTypes = method.getParameterTypes();
				if( methodName.startsWith( "set" )
						&& parameterTypes.length == 1
						&& parameterTypes[0].isInstance( value ) ) {
					final String beanPropertyName = Introspector.decapitalize( methodName.substring( 3 ) );
					if(propertyName.equals( beanPropertyName )){
						method.invoke( bean, value );
					}
				}
			}
		}
	}

	/**
	 * Convert String value to targetType. Tryes several conversion methods
	 *
	 * @return value of type targetType, or null, if conversion can't be performed
	 */
	private static Object convert( final String value,
	                               final Class targetType ) {
		//direct conversion
		if( targetType.isInstance( value ) ) {
			return value;
		}
		Class actualTargetType = targetType;

		//"normalize" primitive type to appropriate wrapper
		if( targetType.isPrimitive() ) {
			for( int i = 0; i < PRIMITIVE_MAPPING.length; i += 2 ) {
				if( PRIMITIVE_MAPPING[i].equals( targetType ) ) {
					actualTargetType = PRIMITIVE_MAPPING[i + 1];
					break;
				}
			}
		}

		// 1. targetType has static .valueOf(String) method
		try {
			final Method valueOfMethod =
					actualTargetType.getMethod( "valueOf", new Class[] { String.class } );
			if( isStatic( valueOfMethod ) && isPublic( valueOfMethod ) ) {
				return valueOfMethod.invoke( null, value );
			}
		} catch( Exception e ) {
			//just try next method
		}

		// 2. targetType has constructor(String)
		try {
			final Constructor ctor = actualTargetType.getConstructor( String.class );
			if( isPublic( ctor ) ) {
				return ctor.newInstance( value );
			}
		} catch( Exception e ) {
		}

		//special cases

		if( actualTargetType.equals( Character.class ) ) {
			return Character.valueOf( value.charAt( 0 ) );
		} else if( actualTargetType.equals( TimeZone.class ) ) {
			return TimeZone.getTimeZone( value );
		}
		//nothing found -> return null
		return null;
	}

	private static boolean isPublic( final Member m ) {
		return ( m.getModifiers() & Modifier.PUBLIC ) != 0;
	}

	private static boolean isStatic( final Member m ) {
		return ( m.getModifiers() & Modifier.STATIC ) != 0;
	}
}
