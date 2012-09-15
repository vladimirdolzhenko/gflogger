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

import static org.gflogger.formatter.BufferFormatter.BDIGITS;
import static org.gflogger.formatter.BufferFormatter.BDIGIT_ONES;
import static org.gflogger.formatter.BufferFormatter.BDIGIT_TENS;
import static org.gflogger.formatter.BufferFormatter.LONG_SIZE_TABLE;
import static org.gflogger.formatter.BufferFormatter.stringSize;
import static org.gflogger.formatter.BytesOverflow.BYTES_OVERFLOW;

import java.nio.ByteBuffer;

import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

import com.lmax.disruptor.util.Util;

/**
 * Bytes
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class Bytes {

	private static final Unsafe UNSAFE = Util.getUnsafe();

	// Cached array base offset
	private static final long arrayBaseOffset = UNSAFE.arrayBaseOffset(byte[].class);

	private final byte[] bs;
	private int pos;

	public Bytes(final int size) {
		bs = new byte[size];
	}

	public int size(){
		return bs.length;
	}

	public int remaining(){
		return bs.length - pos;
	}

	public int position(){
		return pos;
	}

	public void position(int pos){
		if (pos < 0 || pos > bs.length)
			throw new IllegalArgumentException("position " + pos
				+ " is out of range [0 .. " + bs.length + "]");
		this.pos = pos;
	}

	public void clear(){
		pos = 0;
	}

	public void put(boolean b){
		final int remaining = remaining();
		if (b){
			if (remaining < 4) throw BYTES_OVERFLOW;
			bs[pos + 0] = 't';
			bs[pos + 1] = 'r';
			bs[pos + 2] = 'u';
			bs[pos + 3] = 'e';
			pos += 4;
			return;
		}
		if (remaining < 5) throw BYTES_OVERFLOW;
		bs[pos + 0] = 'f';
		bs[pos + 1] = 'a';
		bs[pos + 2] = 'l';
		bs[pos + 3] = 's';
		bs[pos + 4] = 'e';
		pos += 5;
	}

	public void put(char c) {
		final int remaining = remaining();
		if (remaining < 1) throw BYTES_OVERFLOW;
		bs[pos++] = (byte) c;
	}

	private void putNull(final int remaining) {
		if (remaining < 4) throw BYTES_OVERFLOW;
		bs[pos + 0] = 'n';
		bs[pos + 1] = 'u';
		bs[pos + 2] = 'l';
		bs[pos + 3] = 'l';
		pos+=4;
	}

	public void put(CharSequence s){
		final int remaining = remaining();
		if (s == null){
			putNull(remaining);
			return;
		}
		final int len = s.length();
		if (remaining < len) throw BYTES_OVERFLOW;
		for(int i = 0; i < len; i++){
			bs[pos++] = (byte) s.charAt(i);
		}
	}

	public void put(CharSequence s, int start, int end){
		final int remaining = remaining();
		if (s == null){
			putNull(remaining);
			return;
		}
		if (remaining < (end - start)) throw BYTES_OVERFLOW;
		for(int i = start; i < end; i++){
			bs[pos++] = (byte) s.charAt(i);
		}
	}

	public void put(int i) {
		if (i == Integer.MIN_VALUE) {
		 // uses java.lang.Integer string constant of MIN_VALUE
			put(Integer.toString(i));
			return;
		}

		int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);

		if (remaining() < size)  throw BYTES_OVERFLOW;

		if (i < 0) {
			bs[pos++] = '-';
			size--;
			i = -i;
		}

		int q, r;
		int charPos = size;

		int oldPos = pos;

		// Generate two digits per iteration
		while (i >= 65536) {
			q = i / 100;
		// really: r = i - (q * 100);
			r = i - ((q << 6) + (q << 5) + (q << 2));
			i = q;
			bs[oldPos + (--charPos)] = BDIGIT_ONES[r];
			bs[oldPos + (--charPos)] = BDIGIT_TENS[r];
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
			bs[oldPos + (--charPos)] = BDIGITS[r];
			i = q;
			if (i == 0) break;
		}

		pos = oldPos + size;
	}

	public void put(long i) {
		if (i == Long.MIN_VALUE){
			// uses java.lang.Long string constant of MIN_VALUE
			put(Long.toString(i));
			return;
		}
		int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);

		if (remaining() < size)  throw BYTES_OVERFLOW;

		if (i < 0) {
			bs[pos++] = '-';
			size--;
			i = -i;
		}

		long q;
		int r;
		int oldPos = pos;
		int charPos = size;

		// Get 2 digits/iteration using longs until quotient fits into an int
		while (i > Integer.MAX_VALUE) {
			q = i / 100;
			// really: r = i - (q * 100);
			r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
			i = q;
			bs[oldPos + (--charPos)] = BDIGIT_ONES[r];
			bs[oldPos + (--charPos)] = BDIGIT_TENS[r];
		}

		// Get 2 digits/iteration using ints
		int q2;
		int i2 = (int)i;
		while (i2 >= 65536) {
			q2 = i2 / 100;
			// really: r = i2 - (q * 100);
			r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
			i2 = q2;
			bs[oldPos + (--charPos)] = BDIGIT_ONES[r];
			bs[oldPos + (--charPos)] = BDIGIT_TENS[r];
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
			bs[oldPos + (--charPos)] = BDIGITS[r];
			i2 = q2;
			if (i2 == 0) break;
		}
		pos = oldPos + size;
	}

	public void put(double i, int precision) {
		long x = (long)i;
		put(x);
		bs[pos++] = '.';
		x = (long)((i -x) * (precision > 0 ? LONG_SIZE_TABLE[precision - 1] : 1));
		put(x < 0 ? -x : x);
	}

	public void copyTo(ByteBuffer buffer) {
		buffer.clear();
		if (buffer.isDirect()) {
			DirectBuffer db = (DirectBuffer) buffer;

			copyFromArray(bs, arrayBaseOffset, 0 << 0,
				db.address(), pos << 0);

			buffer.position(buffer.position() + pos);
		} else {
			buffer.put(bs, 0, pos);
		}
	}

	public String asString() {
		return new String(bs, 0, pos);
	}

    // -- Bulk get/put acceleration --

    // These numbers represent the point at which we have empirically
    // determined that the average cost of a JNI call exceeds the expense
    // of an element by element copy.  These numbers may change over time.
    static final int JNI_COPY_TO_ARRAY_THRESHOLD   = 6;
    static final int JNI_COPY_FROM_ARRAY_THRESHOLD = 6;

    // This number limits the number of bytes to copy per call to Unsafe's
    // copyMemory method. A limit is imposed to allow for safepoint polling
    // during a large copy
    static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;

    // These methods do no bounds checking.  Verification that the copy will not
    // result in memory corruption should be done prior to invocation.
    // All positions and lengths are specified in bytes.

    /**
     * Copy from given source array to destination address.
     *
     * @param   src
     *          source array
     * @param   srcBaseOffset
     *          offset of first element of storage in source array
     * @param   srcPos
     *          offset within source array of the first element to read
     * @param   dstAddr
     *          destination address
     * @param   length
     *          number of bytes to copy
     */
    static void copyFromArray(Object src, long srcBaseOffset, long srcPos,
                              long dstAddr, long length)
    {
        long offset = srcBaseOffset + srcPos;
        while (length > 0) {
            long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            UNSAFE.copyMemory(src, offset, null, dstAddr, size);
            length -= size;
            offset += size;
            dstAddr += size;
        }
    }

}
