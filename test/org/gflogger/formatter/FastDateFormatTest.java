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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.gflogger.formatter.FastDateFormat;

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
            
            buffer.clear();
        }
        
    }
}
