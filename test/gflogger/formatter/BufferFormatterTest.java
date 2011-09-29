package gflogger.formatter;

import gflogger.formatter.BufferFormatter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * BufferFormatterTest
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class BufferFormatterTest extends TestCase {

    public void testStringLength() throws Exception {
        final int[] numbers = new int[]{0, 1, 7, 11, 123, 7895, 100, 101, 10007, Integer.MAX_VALUE};
        for (int i = 0; i < numbers.length; i++) {
            assertEquals(Integer.toString(numbers[i]).length(), BufferFormatter.stringSize(numbers[i]));
        }
    }
    
    public void testAppendBoolean() throws Exception {
        final boolean[] booleans = new boolean[]{true, false};
        final CharBuffer buffer = ByteBuffer.allocateDirect(50).asCharBuffer();
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
    

    
}