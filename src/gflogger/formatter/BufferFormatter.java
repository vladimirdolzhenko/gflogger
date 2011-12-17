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

package gflogger.formatter;

import java.nio.CharBuffer;

/**
 * BufferFormatter
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class BufferFormatter {

	public static CharBuffer append(final CharBuffer buffer, boolean b){
		if (b){
			return buffer.put('t').put('r').put('u').put('e');
		}
		return buffer.put('f').put('a').put('l').put('s').put('e');
	}
	
	public static CharBuffer append(final CharBuffer buffer, CharSequence s){
		return append(buffer, s, 0, s != null ? s.length() : 0);
	}
	
	public static CharBuffer append(final CharBuffer buffer, CharSequence s, int start, int end){
		if (s != null){
			for(int i = start; i < end; i++){
				buffer.put(s.charAt(i));
			}
			return buffer;
		}
		return buffer.put('n').put('u').put('l').put('l');
	}
	
	public static CharBuffer append(final CharBuffer buffer, byte b) {
		int i = b;
		if (i < 0) {
			buffer.put('-');
			i = -i;
		}
		int j = i;
		// positive byte values is in 0 .. 128
		if (i >= 100){
			buffer.put(DIGIT_ONES[1]);
			i -= 100;
		}
		
		if (i >= 10 || j >= 100){
			buffer.put(DIGIT_TENS[i]);
		}
		
		buffer.put(DIGIT_ONES[i]);
		
		return buffer;
	}
	
	public static CharBuffer append(final CharBuffer buffer, int i) {
		if (i == Integer.MIN_VALUE) {
		 // uses java.lang.Integer string constant of MIN_VALUE
			return append(buffer, Integer.toString(i));
		}
		
		put(buffer, i);
		return buffer;
	}
	
	public static CharBuffer append(final CharBuffer buffer, long i) {
		if (i == Long.MIN_VALUE){
			// uses java.lang.Long string constant of MIN_VALUE
			return append(buffer, Long.toString(i));
		}
		put(buffer, i);
		return buffer;
	}
	
	
	public static CharBuffer append(final CharBuffer buffer, double i, int precision) {
		put(buffer, i, precision < 0 ? 4 : precision);
		return buffer;
	}

	public final static int[] INT_SIZE_TABLE = {
		10,
		100,
		1000,
		10000,
		100000,
		1000000,
		10000000,
		100000000,
		1000000000,
		Integer.MAX_VALUE };
	
	public final static long[] LONG_SIZE_TABLE = { 
		10L,
		100L,
		1000L,
		10000L,
		100000L,
		1000000L,
		10000000L,
		100000000L,
		1000000000L,
		10000000000L,
		100000000000L,
		1000000000000L,
		10000000000000L,
		100000000000000L,
		1000000000000000L,
		10000000000000000L,
		100000000000000000L,
		1000000000000000000L,
		Long.MAX_VALUE};
	
	public final static char [] DIGIT_TENS = {
		'0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
		'1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
		'2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
		'3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
		'4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
		'5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
		'6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
		'7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
		'8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
		'9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
	} ; 

	public final static char [] DIGIT_ONES = { 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	} ;
		
	/**
	 * All possible chars for representing a number as a String
	 */
	public final static char[] DIGITS = {
		'0' , '1' , '2' , '3' , '4' , '5' ,
		'6' , '7' , '8' , '9' , 'a' , 'b' ,
		'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
		'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
		'o' , 'p' , 'q' , 'r' , 's' , 't' ,
		'u' , 'v' , 'w' , 'x' , 'y' , 'z'
	};
	
	// Requires positive x
	public static int stringSize(int x) {
		for (int i = 0; i < INT_SIZE_TABLE.length; i++)
			if (x < INT_SIZE_TABLE[i])
				return i + 1;
		return INT_SIZE_TABLE.length;
	}
	
	// I use the "invariant division by multiplication" trick to
	// accelerate Integer.toString.  In particular we want to
	// avoid division by 10.
	//
	// The "trick" has roughly the same performance characteristics
	// as the "classic" Integer.toString code on a non-JIT VM.
	// The trick avoids .rem and .div calls but has a longer code
	// path and is thus dominated by dispatch overhead.  In the
	// JIT case the dispatch overhead doesn't exist and the
	// "trick" is considerably faster than the classic code.
	//
	// TODO-FIXME: convert (x * 52429) into the equiv shift-add
	// sequence.
	//
	// RE:  Division by Invariant Integers using Multiplication
	//	  T Gralund, P Montgomery
	//	  ACM PLDI 1994
	//
	
	// based on java.lang.Integer.getChars(int i, int index, char[] buf)
	static void put(final CharBuffer buffer, int i) {
		int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
		if (buffer.remaining() < size){
			throw new IllegalStateException("No enough buffer space");
		}
		
		if (i < 0) {
			buffer.put('-');
			size--;
			i = -i;
		}
		
		int q, r;
		int charPos = size;
		
		int oldPos = buffer.position();
		
		// Generate two digits per iteration
		while (i >= 65536) {
			q = i / 100;
		// really: r = i - (q * 100);
			r = i - ((q << 6) + (q << 5) + (q << 2));
			i = q;
			putAt(buffer, oldPos + (--charPos), DIGIT_ONES[r]);
			putAt(buffer, oldPos + (--charPos), DIGIT_TENS[r]);
		}

		// Fall thru to fast mode for smaller numbers
		// assert(i <= 65536, i);
		for (;;) { 
			// 52429 = (1 << 15) + (1 << 14) + (1 << 11) + (1 << 10) + (1 << 7) + (1 << 6) + (1 << 3) + (1 << 2) + 1  
			// 52429 = 32768 + 16384 + 2048 + 1024 + 128 + 64 + 8 + 4 + 1
			/*/
			q = ((i << 15) + (i << 14) + (i << 11) + (i << 10) + (i << 7) + (i << 6) + (i << 3) + (i << 2) + i) >> (16 + 3);
			/*/
			q = (i * 52429) >>> (16+3);
			//*/
			r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
			putAt(buffer, oldPos + (--charPos), DIGITS[r]);
			i = q;
			if (i == 0) break;
		}
		
		buffer.position(oldPos + size);
	}
	
	// Requires positive x
	public static int stringSize(long x) {
		for (int i = 0; i < LONG_SIZE_TABLE.length; i++)
			if (x < LONG_SIZE_TABLE[i])
				return i + 1;
		return LONG_SIZE_TABLE.length;
	}
	
	// based on java.lang.Long.getChars(int i, int index, char[] buf)
	static void put(final CharBuffer buffer, long i) {
		int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
		if (buffer.remaining() < size){
			throw new IllegalStateException("No enough buffer space");
		}
		
		int oldPos = buffer.position();
		
		long q;
		int r;
		int charPos = size;
		char sign = 0;

		if (i < 0) {
			sign = '-';
			i = -i;
		}

		// Get 2 digits/iteration using longs until quotient fits into an int
		while (i > Integer.MAX_VALUE) { 
			q = i / 100;
			// really: r = i - (q * 100);
			r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
			i = q;
			putAt(buffer, oldPos + (--charPos), DIGIT_ONES[r]);
			putAt(buffer, oldPos + (--charPos), DIGIT_TENS[r]);
		}

		// Get 2 digits/iteration using ints
		int q2;
		int i2 = (int)i;
		while (i2 >= 65536) {
			q2 = i2 / 100;
			// really: r = i2 - (q * 100);
			r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
			i2 = q2;
			putAt(buffer, oldPos + (--charPos), DIGIT_ONES[r]);
			putAt(buffer, oldPos + (--charPos), DIGIT_TENS[r]);
		}

		// Fall thru to fast mode for smaller numbers
		// assert(i2 <= 65536, i2);
		for (;;) {
			// 52429 = (1 << 15) + (1 << 14) + (1 << 11) + (1 << 10) + (1 << 7) + (1 << 6) + (1 << 3) + (1 << 2) + 1
			/*/
			q2 = ((i2 << 15) + (i2 << 14) + (i2 << 11) + (i2 << 10) + (i2 << 7) + (i2 << 6) + (i2 << 3) + (i2 << 2) + i2) >> (16 + 3);
			/*/
			q2 = (i2 * 52429) >>> (16+3);
			//*/
			r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
			putAt(buffer, oldPos + (--charPos), DIGITS[r]);
			i2 = q2;
			if (i2 == 0) break;
		}
		if (sign != 0) {
			putAt(buffer, oldPos + (--charPos), sign);
		}
		buffer.position(oldPos + size);
	}
	
	static void putAt(final CharBuffer buffer, int pos, char b){
		buffer.position(pos);
		buffer.append(b);
	}
	
	static void put(final CharBuffer buffer, double i, int precision) {
		long x = (long)i;
		put(buffer, x);
		buffer.put('.');
		x = (long)((i -x) * (precision > 0 ? LONG_SIZE_TABLE[precision - 1] : 1));
		put(buffer, x < 0 ? -x : x);
	}
}
