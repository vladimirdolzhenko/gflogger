package org.gflogger.util;

import java.lang.reflect.Method;
import java.nio.Buffer;

import org.gflogger.helpers.LogLog;

/**
 * Clearing sequence was taken from jMonkeyEngine codebase:
 * https://code.google.com/p/jmonkeyengine/source/browse/trunk/engine/src/core/com/jme3/util/BufferUtils.java
 * Great thanks to them!
 *
 * @author ruslan
 *         created 03/11/14 at 15:12
 */
public class DirectBufferUtils {

	private static final Method cleanerMethod;
	private static final Method cleanMethod;
	private static final Method viewedBufferMethod;
	private static final Method freeMethod;

	static {
		// Oracle JRE / OpenJDK
		cleanerMethod = loadMethod( "sun.nio.ch.DirectBuffer", "cleaner" );
		cleanMethod = loadMethod( "sun.misc.Cleaner", "clean" );
		Method vbMethod = loadMethod( "sun.nio.ch.DirectBuffer", "viewedBuffer" );
		if( vbMethod == null ) {
			// They changed the name in Java 7 (???)
			vbMethod = loadMethod( "sun.nio.ch.DirectBuffer", "attachment" );
		}
		viewedBufferMethod = vbMethod;

		// Apache Harmony
		freeMethod = loadMethod( "org.apache.harmony.nio.internal.DirectBuffer", "free" );

		// GUN Classpath (not likely)
		//finalizeMethod = loadMethod("java.nio.DirectByteBufferImpl", "finalize");
	}

	private static Method loadMethod( final String className,
	                                  final String methodName ) {
		try {
			final Class<?> clazz = Class.forName( className );
			final Method method = clazz.getMethod( methodName );
			method.setAccessible( true );
			return method;
		} catch( NoSuchMethodException ex ) {
			return null; // the method was not found
		} catch( SecurityException ex ) {
			return null; // setAccessible not allowed by security policy
		} catch( ClassNotFoundException ex ) {
			return null; // the direct buffer implementation was not found
		}
	}

	/**
	 * @param toBeReleased The direct buffer that will be "cleaned". Utilizes reflection.
	 */
	public static void releaseBuffer( final Buffer toBeReleased ) {
		if( toBeReleased==null
				|| !toBeReleased.isDirect() ) {
			return;
		}

		try {
			if( freeMethod != null ) {
				freeMethod.invoke( toBeReleased );
			} else {
				final Object cleaner = cleanerMethod.invoke( toBeReleased );
				if( cleaner != null ) {
					cleanMethod.invoke( cleaner );
				} else {
					// Try the alternate approach of getting the viewed buffer first
					final Object viewedBuffer = viewedBufferMethod.invoke( toBeReleased );
					if( viewedBuffer != null ) {
						releaseBuffer( ( Buffer ) viewedBuffer );
					} else {
						LogLog.warn( "Can't release direct buffer:" + toBeReleased );
					}
				}
			}
		} catch( Exception ex ) {
			LogLog.warn( "Can't release direct buffer: " + toBeReleased, ex );
		}
	}
}
