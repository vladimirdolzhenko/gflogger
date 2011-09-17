package gflogger.formatter;

import gflogger.formatter.FastDateFormat;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

public class FastDateFormatTest extends TestCase {

    public void testname() throws Exception {
        final CharBuffer buffer = ByteBuffer.allocateDirect(1 << 10).asCharBuffer();
        
        final String[] patterns = 
            new String[]{
                "HH:mm:ss,SSS", 
                "dd/MM/yyyy zzz HH:mm:ss,SSS", 
                "dd/MM/yyyy zzzz HH:mm:ss,SSS", 
                "dd/MMM/yyyy E HH:mm:ss,SSS" 
           };
        
        for (final String pattern : patterns) {
            final SimpleDateFormat sdf = new SimpleDateFormat(pattern); 
            final FastDateFormat instance = FastDateFormat.getInstance(pattern);
            
            
            final long now = System.currentTimeMillis();
            instance.format(now, buffer);
            
            final String dateString = BufferFormatterTest.toString(buffer);
            
            final Date date = new Date(now);
            assertEquals(sdf.format(date), dateString);
            
            buffer.clear();
            
            instance.format(date, buffer);
            
            final String dateString2 = BufferFormatterTest.toString(buffer);
            assertEquals(dateString, dateString2);
            
            //System.out.println(dateString);
            buffer.clear();
        }
        
    }
}
