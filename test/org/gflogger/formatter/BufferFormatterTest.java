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

import org.gflogger.formatter.BufferFormatter;

import junit.framework.TestCase;

/**
 * BufferFormatterTest
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class BufferFormatterTest extends TestCase {

	public void testStringLength() throws Exception {
		final int[] numbers = new int[]{0, 1, 7, 11, 123, 7895, 100, 101, 
			10007, 1000000, 123456789, 
			987654321,
			Integer.MAX_VALUE};
		for (int i = 0; i < numbers.length; i++) {
			assertEquals(Integer.toString(numbers[i]).length(), BufferFormatter.stringSize(numbers[i]));
		}
	}
	
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
	
	public void testAppendCharBufferBoolean() throws Exception {
		final boolean[] booleans = new boolean[]{true, false};
		final CharBuffer buffer = ByteBuffer.allocateDirect(50).asCharBuffer();
		for (int i = 0; i < booleans.length; i++) {
			BufferFormatter.append(buffer, booleans[i]);
			assertEquals(Boolean.toString(booleans[i]), toString(buffer));
			buffer.clear();
		}
	}
	
	public void testAppendByteBufferBoolean() throws Exception {
		final boolean[] booleans = new boolean[]{true, false};
		final ByteBuffer buffer = ByteBuffer.allocateDirect(50);
		for (int i = 0; i < booleans.length; i++) {
			BufferFormatter.append(buffer, booleans[i]);
			assertEquals(Boolean.toString(booleans[i]), toString(buffer));
			buffer.clear();
		}
	}
	
	public void testAppendByte() throws Exception {
		final CharBuffer buffer = ByteBuffer.allocateDirect(50).asCharBuffer();
		for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++) {
			BufferFormatter.append(buffer, b);
			assertEquals(Byte.toString(b), toString(buffer));
			buffer.clear();
		}
	}
	
	public void testAppendShort() throws Exception {
		final CharBuffer buffer = ByteBuffer.allocateDirect(50).asCharBuffer();
		for (short s = Short.MIN_VALUE; s < Short.MAX_VALUE; s++) {
			BufferFormatter.append(buffer, s);
			assertEquals(Short.toString(s), toString(buffer));
			buffer.clear();
		}
	}

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
	
	public void testAppendDouble() throws Exception {
		final CharBuffer buffer = ByteBuffer.allocateDirect(40).asCharBuffer();
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
		
		final double[] numbers = new double[]{1.4328, -123.9487};
		for (int i = 0; i < numbers.length; i++) {
			BufferFormatter.append(buffer, numbers[i], 6);
			buffer.append(' ');
			assertEquals(String.format(Locale.ENGLISH, "%.6f", numbers[i]) + " ", toString(buffer));
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
