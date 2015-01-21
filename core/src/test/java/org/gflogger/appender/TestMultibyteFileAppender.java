package org.gflogger.appender;

import java.nio.CharBuffer;

import org.gflogger.LogEntryItemImpl;
import org.gflogger.LogLevel;
import org.junit.Before;
import org.junit.Test;

/**
 * @author vladimir.dolzhenko@gmail.com
 **/
public class TestMultibyteFileAppender extends TestFileAppender {

	@Before
	@Override
	public void setUp() throws Exception {
		multibyte = true;
		super.setUp();
	}

	@Override
	protected LogEntryItemImpl createEntry(final String message) throws Exception {
		LogEntryItemImpl entry = new LogEntryItemImpl(message.length(), multibyte);
		entry.setLogLevel( LogLevel.INFO );
		final CharBuffer charBuffer = entry.getCharBuffer();
		for(int i = 0, len = message.length(); i < len; i++){
			charBuffer.put( message.charAt( i ) );
		}
		return entry;
	}

	@Override
	protected char justAChar() {
		return '\u0430';
	}

	@Test
	public void testUnicodeMessage() throws Exception {
		final String msg =
			"\u0411\u0435\u043B\u0435\u0435\u0442 \u043F\u0430\u0440\u0443\u0441 \u043E\u0434\u0438\u043D\u043E\u043A\u0438\u0439";
			//"Белеет парус одинокий";

		processEntryWithMessage( msg );

		final String anotherMessage = stringWithLength( BUFFER_SIZE - msg.length() );
		processEntryWithMessage( anotherMessage );
		assertOutput(msg);
	}
}
