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
import java.nio.CharBuffer;
import java.util.Locale;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * BufferFormatterTest
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class BufferFormatterTest {

	@Test
	public void testStringLength() throws Exception {
		final int[] numbers = new int[]{0, 1, 7, 11, 123, 7895, 100, 101,
			10007, 1000000, 123456789,
			987654321,
			Integer.MAX_VALUE};
		for (int i = 0; i < numbers.length; i++) {
			assertEquals(Integer.toString(numbers[i]).length(), BufferFormatter.stringSize(numbers[i]));
		}
	}

	@Test
	public void testAppendByteBufferString() throws Exception {
		final String[] values = new String[]{"true", null, "value"};
		final ByteBuffer buffer = ByteBuffer.allocateDirect(50);
		for (int i = 0; i < values.length; i++) {
			BufferFormatter.append(buffer, values[i]);
			final String s = toString(buffer);
			assertEquals(String.valueOf(values[i]), s);
			buffer.clear();
		}
	}

	@Test
	public void testAppendCharBufferBoolean() throws Exception {
		final boolean[] booleans = new boolean[]{true, false};
		final CharBuffer buffer = ByteBuffer.allocateDirect(50).asCharBuffer();
		for (int i = 0; i < booleans.length; i++) {
			BufferFormatter.append(buffer, booleans[i]);
			assertEquals(Boolean.toString(booleans[i]), toString(buffer));
			buffer.clear();
		}
	}

	@Test
	public void testAppendByteBufferBoolean() throws Exception {
		final boolean[] booleans = new boolean[]{true, false};
		final ByteBuffer buffer = ByteBuffer.allocateDirect(50);
		for (int i = 0; i < booleans.length; i++) {
			BufferFormatter.append(buffer, booleans[i]);
			assertEquals(Boolean.toString(booleans[i]), toString(buffer));
			buffer.clear();
		}
	}

	@Test
	public void testAppendByte() throws Exception {
		final CharBuffer buffer = ByteBuffer.allocateDirect(50).asCharBuffer();
		for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++) {
			BufferFormatter.append(buffer, b);
			assertEquals(Byte.toString(b), toString(buffer));
			buffer.clear();
		}
	}

	@Test
	public void testAppendShort() throws Exception {
		final CharBuffer buffer = ByteBuffer.allocateDirect(50).asCharBuffer();
		for (short s = Short.MIN_VALUE; s < Short.MAX_VALUE; s++) {
			BufferFormatter.append(buffer, s);
			assertEquals(Short.toString(s), toString(buffer));
			buffer.clear();
		}
	}

	@Test
	public void testAppendInt() throws Exception {
		final CharBuffer buffer = ByteBuffer.allocateDirect(30).asCharBuffer();

		for (int i = Short.MIN_VALUE - 100; i < Short.MAX_VALUE + 100; i++) {
			BufferFormatter.append(buffer, i);
			assertEquals(Integer.toString(i), toString(buffer));
			buffer.clear();
		}

		final int[] numbers = new int[]{9123123, Integer.MAX_VALUE, Integer.MIN_VALUE};
		for (int i = 0; i < numbers.length; i++) {
			BufferFormatter.append(buffer, numbers[i]);
			buffer.append(' ');
			assertEquals(Integer.toString(numbers[i]) + " ", toString(buffer));
			// check
			buffer.clear();
		}
	}

	@Test
	public void testAppendLong() throws Exception {
		final CharBuffer buffer = ByteBuffer.allocateDirect(50).asCharBuffer();

		for (long i = Short.MIN_VALUE - 100; i < Short.MAX_VALUE + 100; i++) {
			BufferFormatter.append(buffer, i);
			assertEquals(Long.toString(i), toString(buffer));
			buffer.clear();
		}

		final long[] numbers = new long[]{7123712398L,
				9999999999399L, 99999999999999L,
				10007, 1000000, 123456789,  1234567890L,
				987654321, 9876543210L,
				Integer.MAX_VALUE, Integer.MIN_VALUE,
				Integer.MAX_VALUE + 249, Integer.MIN_VALUE - 100,
				Long.MAX_VALUE, Long.MIN_VALUE};
		for (int i = 0; i < numbers.length; i++) {
			BufferFormatter.append(buffer, numbers[i]);
			buffer.append(' ');
			assertEquals(Long.toString(numbers[i]) + " ", toString(buffer));
			// check
			buffer.clear();
		}
	}

	@Test
	public void testAppendDoubleCharBufferWithPrecision() throws Exception {
		final CharBuffer buffer = ByteBuffer.allocateDirect(100).asCharBuffer();
		{
			final double[] numbers = new double[]{0, 1, 7, 11, 123, 7895, -100, 101, -10007};
			for (int i = 0; i < numbers.length; i++) {
				BufferFormatter.append(buffer, numbers[i], 0);
				buffer.append(' ');
				assertEquals(Double.toString(numbers[i]) + " ", toString(buffer));
				// check
				buffer.clear();
			}
		}

		final double[] numbers = new double[]{
			1.025292, 1.0025292, 1.00025292, 1.000025292, 1.0000025292, 1.00000025292,
			10.025292, 10.0025292, 10.00025292, 10.000025292,
			-1.025292, -1.0025292, -1.00025292, -1.000025292, -1.0000025292, -1.00000025292,
			-10.025292, -10.0025292, -10.00025292, -10.000025292,
			1.4328, -123.9487, -0.5};
		for (int i = 0; i < numbers.length; i++) {
			BufferFormatter.append(buffer, numbers[i], 8);
			buffer.append(' ');
			assertEquals(String.format(Locale.ENGLISH, "%.8f", numbers[i]) + " ", toString(buffer));
			// check
			buffer.clear();
		}

		final double[] numbers2 = new double[]{1e10, 1e15, 1e18, 5.074e10};
		for (int i = 0; i < numbers2.length; i++) {
			BufferFormatter.append(buffer, numbers2[i], 6);
			buffer.append(' ');
			assertEquals(String.format(Locale.ENGLISH, "%.6f", numbers2[i]) + " ", toString(buffer));
			// check
			buffer.clear();
		}

		final double[] numbers3 = new double[]{1e-5, 1e-10, 1e-18, 5.074e-10, 0.0035};
		for (int i = 0; i < numbers3.length; i++) {
			BufferFormatter.append(buffer, numbers3[i], 20);
			buffer.append(' ');
			assertEquals(String.format(Locale.ENGLISH, "%.20f", numbers3[i]) + " ", toString(buffer));
			// check
			buffer.clear();
		}

		final double[] numbers4 = new double[]{1e-19, 1e19,
			Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
			0.0, -0.0};
		for (int i = 0; i < numbers4.length; i++) {
			BufferFormatter.append(buffer, numbers4[i], 20);
			buffer.append(' ');
			assertEquals(Double.toString(numbers4[i]) + " ", toString(buffer));
			// check
			buffer.clear();
		}
	}

	@Test
	public void testAppendDoubleByteBufferWithPrecision() throws Exception {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(200);
		{
			final double[] numbers = new double[]{0, 1, 7, 11, 123, 7895, -100, 101, -10007};
			for (int i = 0; i < numbers.length; i++) {
				BufferFormatter.append(buffer, numbers[i], 0);
				buffer.put((byte) ' ');
				assertEquals(Double.toString(numbers[i]) + " ", toString(buffer));
				// check
				buffer.clear();
			}
		}

		final double[] numbers = new double[]{1.4328, -123.9487, -0.5};
		for (int i = 0; i < numbers.length; i++) {
			BufferFormatter.append(buffer, numbers[i], 6);
			buffer.put((byte) ' ');
			assertEquals(String.format(Locale.ENGLISH, "%.6f", numbers[i]) + " ", toString(buffer));
			// check
			buffer.clear();
		}

		final double[] numbers2 = new double[]{1e10, 1e15, 1e18};
		for (int i = 0; i < numbers2.length; i++) {
			BufferFormatter.append(buffer, numbers2[i], 6);
			buffer.put((byte) ' ');
			assertEquals(String.format(Locale.ENGLISH, "%.6f", numbers2[i]) + " ", toString(buffer));
			// check
			buffer.clear();
		}

		final double[] numbers3 = new double[]{1e-5, 1e-10, 1e-18, 0.0035};
		for (int i = 0; i < numbers3.length; i++) {
			BufferFormatter.append(buffer, numbers3[i], 20);
			buffer.put((byte) ' ');
			assertEquals(String.format(Locale.ENGLISH, "%.20f", numbers3[i]) + " ", toString(buffer));
			// check
			buffer.clear();
		}

		final double[] numbers4 = new double[]{1e-19, 1e19,
			Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
			0.0, -0.0};
		for (int i = 0; i < numbers4.length; i++) {
			BufferFormatter.append(buffer, numbers4[i], 20);
			buffer.put((byte) ' ');
			assertEquals(Double.toString(numbers4[i]) + " ", toString(buffer));
			// check
			buffer.clear();
		}
	}

	@Test
	public void testAppendDoubleByteBuffer() throws Exception {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(200);

		final double[] numbers = new double[]{
			0.0035,
			1e-19, 1e19,
			Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
			-0.0, 0.0,
			1235,
			-1235, 0.005, -0.5};

		final String[] strings = new String[]{
			"0.003500000000000",
			Double.toString(1e-19),
			Double.toString(1e19),
			Double.toString(Double.NaN),
			Double.toString(Double.POSITIVE_INFINITY),
			Double.toString(Double.NEGATIVE_INFINITY),
			Double.toString(-0.0),
			Double.toString(0.0),
			"1235.0",
			"-1235.0",
			"0.005000000000000",
			"-0.500000000000000"};
		for (int i = 0; i < numbers.length; i++) {
			BufferFormatter.append(buffer, numbers[i]);
			buffer.put((byte) ' ');
			assertEquals(strings[i] + " ", toString(buffer));
			// check
			buffer.clear();
		}
	}

	@Test
	public void testAppendDoubleCharBuffer() throws Exception {
		final CharBuffer buffer = ByteBuffer.allocateDirect(100).asCharBuffer();

		final double[] numbers = new double[]{
			0.0035,
			1e-19, 1e19,
			Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
			-0.0, 0.0,
			1235,
			-1235, 0.005, -0.5};

		final String[] strings = new String[]{
			"0.003500000000000",
			Double.toString(1e-19),
			Double.toString(1e19),
			Double.toString(Double.NaN),
			Double.toString(Double.POSITIVE_INFINITY),
			Double.toString(Double.NEGATIVE_INFINITY),
			Double.toString(-0.0),
			Double.toString(0.0),
			"1235.0",
			"-1235.0",
			"0.005000000000000",
			"-0.500000000000000"};
		for (int i = 0; i < numbers.length; i++) {
			BufferFormatter.append(buffer, numbers[i]);
			buffer.put(' ');
			assertEquals(strings[i] + " ", toString(buffer));
			// check
			buffer.clear();
		}
	}

	static String toString(final CharBuffer buffer) {
		buffer.flip();
		final char[] chs = new char[buffer.limit()];
		buffer.get(chs);
		return new String(chs);
	}

	static String toString(final ByteBuffer buffer) {
		buffer.flip();
		final byte[] chs = new byte[buffer.limit()];
		buffer.get(chs);
		return new String(chs);
	}

}
