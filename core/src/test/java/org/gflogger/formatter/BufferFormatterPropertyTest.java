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

package org.gflogger.formatter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Ignore;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.experimental.theories.suppliers.TestedOn;
import org.junit.runner.RunWith;

import static org.gflogger.formatter.BufferFormatterPropertyTest.DoubleCloseTo.closeTo;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

/**
 * @author cheremin@gmail.com
 */
@RunWith( Theories.class )
public class BufferFormatterPropertyTest {

	private static final double DOUBLE_FORMATTING_TOLERANCE = 1e-5;//Math.pow( 10, -BufferFormatter.DOUBLE_DIGITS );

	//@DataPoints
	//TODO RC: this was commented out since produce too much broken samples
	// few samples extracted to IMPORTANT_DOUBLE_SAMPLES
	public static Double[] importantDoubles() {
		//I was thinking about listed all doubles here, but was called to dinner,
		// so forced to cut my dreams off. Feel free to extend this

		//start from few crucial samples, and apply a set of transform
		final double[] seeds = {
				Double.MIN_VALUE,
				0.0,
				Double.MIN_NORMAL,

				1e-200, 1e-20, 1e-10, 1e-5, 1e-3, 0.1,

				0.05,
				0.5,
				1.0 / 3,
				1.0 / 7,
				1.0 / 9,
				1, 2, 5,

				10, 1e2, 1e3, 1e5, 1e10, 1e20, 1e200,

				Double.MAX_VALUE / 2,
				Double.MAX_VALUE - 1,
				Double.MAX_VALUE,
				Double.POSITIVE_INFINITY,

				Double.NaN
		};

		final ArrayList<Double> doubles = new ArrayList<Double>();
		for( final double seed : seeds ) {
			doubles.add( seed );
		}
		for( final Double value : new ArrayList<Double>( doubles ) ) {
			doubles.add( -value );
		}
		for( final Double value : new ArrayList<Double>( doubles ) ) {
			doubles.add( value + Math.ulp( value ) );
			doubles.add( value - Math.ulp( value ) );
		}
		for( final Double value : new ArrayList<Double>( doubles ) ) {
			for( int power = Double.MIN_EXPONENT;
			     power <= Double.MAX_EXPONENT;
			     power += 10 /*enough*/ ) {
				doubles.add( value * Math.pow( 10, power ) );
			}
		}

		final HashSet<Double> unique = new HashSet<Double>( doubles );
		final Double[] array = unique.toArray( new Double[unique.size()] );
		Arrays.sort( array );
		return array;
	}

	@DataPoints
	public static final Double[] IMPORTANT_DOUBLE_SAMPLES = {
//			0.9,
//			0.99,
//			0.999,
//			0.9999,

			-0.9,
			-0.99,
			-0.999,
			-0.9999
	};

	/** Work well with IMPORTANT_DOUBLE_SAMPLES, but fails on importantDoubles() */
	@Theory( nullsAccepted = false )
	public void appendedDoubleParsesAsItself( final Double value ) {
		final ByteBuffer buffer = ByteBuffer.allocate( 200 );
		BufferFormatter.append( buffer, value );
		final String formatted = BufferFormatterTest.toString( buffer );
		final double parsedValue = Double.parseDouble( formatted );

		assertThat(
				".append(" + value + ") -> [" + formatted + "] -> [" + parsedValue + "]",
				parsedValue,
				closeTo( value, DOUBLE_FORMATTING_TOLERANCE )
		);
	}


	@Ignore( "Fails even on IMPORTANT_DOUBLE_SAMPLES" )
	@Theory( nullsAccepted = false )
	public void appendedDoubleWithPrecisionParsesAsItselfWithTolerance( final Double value,
	                                                                    @TestedOn( ints = { 0, 1, 2, 3, 10/*, 19, 20*/ } )
	                                                                    final int digits ) {
		final ByteBuffer buffer = ByteBuffer.allocate( 200 );
		BufferFormatter.append( buffer, value, digits );
		final String formatted = BufferFormatterTest.toString( buffer );

		final double parsedValue = Double.parseDouble( formatted );

		assertThat(
				".append(" + value + "," + digits + ") -> [" + formatted + "] -> [" + parsedValue + "]",
				parsedValue,
				closeTo( value, Math.pow( 10, -digits ) )
		);
	}

	@DataPoints
	public static Integer[] importantIntegers() {
		//start from few crucial samples, and apply a set of transform
		final int[] seeds = {
				Integer.MIN_VALUE,
				Short.MIN_VALUE,
				Byte.MIN_VALUE,
				-1,
				0,
				1,
				Byte.MAX_VALUE,
				Short.MAX_VALUE,
				Integer.MAX_VALUE
		};

		final ArrayList<Integer> integers = new ArrayList<Integer>();
		for( final int seed : seeds ) {
			integers.add( seed );
		}

		for( final Integer integer : new ArrayList<Integer>( integers ) ) {
			for( int i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++ ) {
				integers.add( integer + i );
			}
		}

		return integers.toArray( new Integer[0] );
	}

	@Theory( nullsAccepted = false )
	public void appendedIntegerParsesAsItself( final Integer value ) {
		final ByteBuffer buffer = ByteBuffer.allocate( 20 );
		BufferFormatter.append( buffer, value );
		final String formatted = BufferFormatterTest.toString( buffer );
		final int parsedValue = Integer.parseInt( formatted );

		assertThat(
				".append(" + value + ") -> [" + formatted + "] -> [" + parsedValue + "]",
				parsedValue,
				is( value )
		);
	}

	@DataPoints
	public static Long[] importantLongs() {
		//start from few crucial samples, and apply a set of transform
		final long[] seeds = {
				Long.MIN_VALUE,
				Integer.MIN_VALUE,
				Short.MIN_VALUE,
				Byte.MIN_VALUE,
				-1,
				0,
				1,
				Byte.MAX_VALUE,
				Short.MAX_VALUE,
				Integer.MAX_VALUE,
				Long.MAX_VALUE
		};

		final ArrayList<Long> longs = new ArrayList<Long>();
		for( final long seed : seeds ) {
			longs.add( seed );
		}

		for( final Long l : new ArrayList<Long>( longs ) ) {
			for( int i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++ ) {
				longs.add( l + i );
			}
		}

		return longs.toArray( new Long[0] );
	}

	@Theory( nullsAccepted = false )
	public void appendedLongParsesAsItself( final Long value ) {
		final ByteBuffer buffer = ByteBuffer.allocate( 20 );
		BufferFormatter.append( buffer, value );
		final String formatted = BufferFormatterTest.toString( buffer );
		final long parsedValue = Long.parseLong( formatted );

		assertThat(
				".append(" + value + ") -> [" + formatted + "] -> [" + parsedValue + "]",
				parsedValue,
				is( value )
		);
	}

	@DataPoints
	public static char[] allChars() {
		final char[] chars = new char[0xffff];
		for( int i = 0; i < chars.length; i++ ) {
			chars[i] = ( char ) i;
		}

		return chars;
	}

	@DataPoints
	public static byte[] allBytes() {
		final byte[] bytes = new byte[255];
		for( int i = 0; i < bytes.length; i++ ) {
			bytes[i] = ( byte ) ( i + Byte.MIN_VALUE );
		}
		return bytes;
	}

	@Theory( nullsAccepted = false )
	public void appendedCharParsesAsItself( final Character character ) {
		assumeThat( "Because for ByteBuffer only ASCII chars supported",
		            ( int ) character.charValue(),
		            allOf(
				            greaterThanOrEqualTo( 0 ),
				            lessThanOrEqualTo( ( int ) Byte.MAX_VALUE )
		            )
		);
		final char ascii = character.charValue();
		final ByteBuffer buffer = ByteBuffer.allocate( 10 );
		BufferFormatter.append( buffer, ascii );
		final String formatted = BufferFormatterTest.toString( buffer );
		final char parsedValue = formatted.charAt( 0 );

		assertThat(
				".append(" + ascii + ") -> [" + formatted + "] -> [" + parsedValue + "]",
				parsedValue,
				is( ascii )
		);
	}

	@Theory( nullsAccepted = false )
	public void appendedByteParsesAsItself( final Byte value ) {
		final ByteBuffer buffer = ByteBuffer.allocate( 200 );
		BufferFormatter.append( buffer, value.byteValue() );
		final String formatted = BufferFormatterTest.toString( buffer );
		final byte parsedValue = Byte.parseByte( formatted );

		assertThat(
				".append(" + value + ") -> [" + formatted + "] -> [" + parsedValue + "]",
				parsedValue,
				is( value )
		);
	}


	/** {@linkplain org.hamcrest.number.IsCloseTo} not able to work with NaNs and Inf! */
	public static class DoubleCloseTo extends TypeSafeMatcher<Double> {
		private final double expected;
		private final double delta;

		public static DoubleCloseTo relativelyCloseTo( final double expected,
		                                               final double relativeTolerance ) {
			return new DoubleCloseTo( expected, Math.abs( expected * relativeTolerance ) );
		}

		public static DoubleCloseTo closeTo( final double expected,
		                                     final double tolerance ) {
			return new DoubleCloseTo( expected, tolerance );
		}

		public DoubleCloseTo( final double expected,
		                      final double delta ) {
			this.expected = expected;
			this.delta = delta;
		}

		@Override
		protected boolean matchesSafely( final Double actual ) {
			if( Double.isNaN( actual ) && Double.isNaN( expected ) ) {
				return true;
			}
			if( actual == expected ) {
				return true;//infinity-safe
			}
			return Math.abs( expected - actual ) <= delta;
		}

		@Override
		public void describeMismatchSafely( final Double actual,
		                                    final Description mismatchDescription ) {
			final double actualDelta = Math.abs( Math.abs( expected - actual ) - delta );
			mismatchDescription.appendValue( actual )
					.appendText( " differed by " )
					.appendValue( actualDelta );
		}

		@Override
		public void describeTo( final Description description ) {
			description.appendText( "a numeric value within " )
					.appendValue( delta )
					.appendText( " of " )
					.appendValue( expected );
		}
	}
}
