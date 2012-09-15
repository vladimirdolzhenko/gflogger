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

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.Locale;

import org.junit.Test;

/**
 * BytesTest
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class BytesTest {

	@Test
	public void testPutString() throws Exception {
		final String[] values = new String[]{"true", null, "value"};
		final Bytes bytes = new Bytes(50);
		for (int i = 0; i < values.length; i++) {
			bytes.put(values[i]);
			assertEquals(String.valueOf(values[i]), bytes.asString());
			bytes.clear();
		}
	}

	@Test
	public void testPutBoolean() throws Exception {
		final boolean[] booleans = new boolean[]{true, false};
		final Bytes bytes = new Bytes(50);
		for (int i = 0; i < booleans.length; i++) {
			bytes.put(booleans[i]);
			assertEquals(Boolean.toString(booleans[i]), bytes.asString());
			bytes.clear();
		}
	}

	@Test
	public void testPutByte() throws Exception {
		final Bytes bytes = new Bytes(50);
		for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++) {
			bytes.put(b);
			assertEquals(Byte.toString(b), bytes.asString());
			bytes.clear();
		}
	}

	@Test
	public void testPutShort() throws Exception {
		final Bytes bytes = new Bytes(50);
		for (short s = Short.MIN_VALUE; s < Short.MAX_VALUE; s++) {
			bytes.put(s);
			assertEquals(Short.toString(s), bytes.asString());
			bytes.clear();
		}
	}

	@Test
	public void testPutInt() throws Exception {
		final Bytes bytes = new Bytes(50);

		for (int i = Short.MIN_VALUE - 100; i < Short.MAX_VALUE + 100; i++) {
			bytes.put(i);
			assertEquals(Integer.toString(i), bytes.asString());
			bytes.clear();
		}

		final int[] numbers = new int[]{9123123, Integer.MAX_VALUE, Integer.MIN_VALUE};
		for (int i = 0; i < numbers.length; i++) {
			bytes.put(numbers[i]);
			bytes.put(' ');
			assertEquals(Integer.toString(numbers[i]) + " ", bytes.asString());
			// check
			bytes.clear();
		}
	}

	@Test
	public void testPutLong() throws Exception {
		final Bytes bytes = new Bytes(50);

		for (long i = Short.MIN_VALUE - 100; i < Short.MAX_VALUE + 100; i++) {
			bytes.put(i);
			assertEquals(Long.toString(i), bytes.asString());
			bytes.clear();
		}

		final long[] numbers = new long[]{7123712398L,
				9999999999399L, 99999999999999L,
				10007, 1000000, 123456789,  1234567890L,
				987654321, 9876543210L,
				Integer.MAX_VALUE, Integer.MIN_VALUE,
				Integer.MAX_VALUE + 249, Integer.MIN_VALUE - 100,
				Long.MAX_VALUE, Long.MIN_VALUE};
		for (int i = 0; i < numbers.length; i++) {
			bytes.put(numbers[i]);
			bytes.put(' ');
			assertEquals(Long.toString(numbers[i]) + " ", bytes.asString());
			// check
			bytes.clear();
		}
	}

	@Test
	public void testCopyToByteBuffer() throws Exception {
		final int size = 50;
		final Bytes bytes = new Bytes(size);

		for(ByteBuffer buffer : new ByteBuffer[]{
				ByteBuffer.allocate(size),
				ByteBuffer.allocateDirect(size),
				}){

			for (long i = Short.MIN_VALUE - 100; i < Short.MAX_VALUE + 100; i++) {
				bytes.put(i);
				assertEquals(Long.toString(i), bytes.asString());

				bytes.copyTo(buffer);
				assertEquals("buffer " + (buffer.isDirect() ? "direct" : "heap") + " : " + Long.toString(i),
					Long.toString(i), BufferFormatterTest.toString(buffer));

				bytes.clear();
			}

			final long[] numbers = new long[]{7123712398L,
					9999999999399L, 99999999999999L,
					10007, 1000000, 123456789,  1234567890L,
					987654321, 9876543210L,
					Integer.MAX_VALUE, Integer.MIN_VALUE,
					Integer.MAX_VALUE + 249, Integer.MIN_VALUE - 100,
					Long.MAX_VALUE, Long.MIN_VALUE};
			for (int i = 0; i < numbers.length; i++) {
				bytes.put(numbers[i]);
				bytes.put(' ');
				assertEquals(Long.toString(numbers[i]) + " ", bytes.asString());

				bytes.copyTo(buffer);
				assertEquals(Long.toString(numbers[i]) + " ", BufferFormatterTest.toString(buffer));

				// check
				bytes.clear();
			}
		}
	}

	@Test
	public void testPutDouble() throws Exception {
		final Bytes bytes = new Bytes(50);
		{
			final double[] numbers = new double[]{0, 1, 7, 11, 123, 7895, -100, 101, -10007};
			for (int i = 0; i < numbers.length; i++) {
				bytes.put(numbers[i], 0);
				bytes.put(' ');
				assertEquals(Double.toString(numbers[i]) + " ", bytes.asString());
				// check
				bytes.clear();
			}
		}

		final double[] numbers = new double[]{1.4328, -123.9487};
		for (int i = 0; i < numbers.length; i++) {
			bytes.put(numbers[i], 6);
			bytes.put(' ');
			assertEquals(String.format(Locale.ENGLISH, "%.6f", numbers[i]) + " ", bytes.asString());
			// check
			bytes.clear();
		}
	}

	@Test
	public void testBytesVsBufferFormatter() throws Exception {
		final int size = 1 << 6;
		System.out.println("size:" + size);
		final Bytes bytes = new Bytes(size);
		final ByteBuffer buffer = ByteBuffer.allocateDirect(size);

		final ByteBuffer target = ByteBuffer.allocateDirect(size);

		int n = 100;

		for(int q = 0; q < 5; q++){

			final long s0 = System.nanoTime();
			for(int k = 0; k < n; k++){
				for (long i = Short.MIN_VALUE - 100; i < Short.MAX_VALUE + 100; i++) {
					bytes.put(i);
					bytes.put(i);
					bytes.put(i);
					bytes.put(i);

					target.clear();
					bytes.copyTo(target);

					bytes.clear();
				}
			}
			final long e0 = System.nanoTime();

			final long t0 = e0 - s0;

			final long s1 = System.nanoTime();
			for(int k = 0; k < n; k++){
				for (long i = Short.MIN_VALUE - 100; i < Short.MAX_VALUE + 100; i++) {
					BufferFormatter.append(buffer, i);
					BufferFormatter.append(buffer, i);
					BufferFormatter.append(buffer, i);
					BufferFormatter.append(buffer, i);
					buffer.flip();

					target.clear();
					target.put(buffer);

					buffer.clear();
				}
			}
			final long e1 = System.nanoTime();


			final long t1 = e1 - s1;
			System.out.println("bytes time:" + (t0/1000)/1e3 + " ms, " +
				"direct buffer time: " + (t1/1000)/1e3 + " ms, ration: " +
				((t1 * 100/t0)/1e2));
		}
		System.out.println();
	}

}
