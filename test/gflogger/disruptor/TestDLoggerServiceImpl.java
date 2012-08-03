package gflogger.disruptor;

import static org.junit.Assert.*;
import gflogger.LocalLogEntry;
import gflogger.LogEntry;
import gflogger.LogFactory;
import gflogger.LogLevel;
import gflogger.Logger;
import gflogger.LoggerService;
import gflogger.appender.ConsoleAppenderFactory;

import java.nio.BufferOverflowException;

import org.junit.AfterClass;
import org.junit.Test;

/**
 * TestDefaultLoggerServiceImpl
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestDLoggerServiceImpl {

	@AfterClass
	public static void shutdown(){
//		LogFactory.stop();
	}

	@Test
	public void testLogLevels() throws Exception {
		final int maxMessageSize = 20;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = new DLoggerServiceImpl(4, maxMessageSize, factory);

		LogFactory.init("com.db", loggerService);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");

		logger.debug().append("debug").commit();
		logger.info().append("info").commit();
		logger.error().append("error").commit();

		LogFactory.stop();

		final String string = buffer.toString();
		assertEquals("infoerror", string);
	}

	@Test
    public void testAppendLatinCharsFullMessageSize() throws Exception {
		final int maxMessageSize = 20;
	    final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
	    factory.setLayoutPattern("%m");
	    final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
	    factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = new DLoggerServiceImpl(4, maxMessageSize, factory);

		LogFactory.init("com.db", loggerService);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");

		{
			final LogEntry info = logger.info();
			for(int i = 0; i < maxMessageSize; i++){
				// latin 'a'
				char c = (char) ('a' + i);
				info.append(c);
			}

			assertTrue(info instanceof LocalLogEntry);

			final LocalLogEntry localLogEntry = (LocalLogEntry)info;

			assertNull(localLogEntry.getError());
			// there is no enough space for one more latin 'z'
			// it doesn't matter latin, greek or chinese char - it's 2 bytes
			info.append('z');
			{
				assertNotNull(localLogEntry.getError());
				assertEquals(BufferOverflowException.class, localLogEntry.getError().getClass());
			}


			info.commit();
		}

		LogFactory.stop();

		final String string = buffer.toString();
		assertEquals(maxMessageSize, string.length());

		for(int i = 0; i < maxMessageSize; i++){
			char c = (char) ('a' + i);
			assertEquals(c, string.charAt(i));
		}

    }

	@Test
	public void testAppendCyrillicCharsFullMessageSize() throws Exception {
		final int maxMessageSize = 20;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		factory.setMultibyte(true);
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = new DLoggerServiceImpl(4, maxMessageSize, factory);

		LogFactory.init("com.db", loggerService);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");

		{
			final LogEntry info = logger.info();
			for(int i = 0; i < maxMessageSize; i++){
				// Russian 'a'
				char c = (char) ('а' + i);
				info.append(c);
			}

			assertTrue(info instanceof LocalLogEntry);

			final LocalLogEntry localLogEntry = (LocalLogEntry)info;

			assertNull(localLogEntry.getError());
//			// there is no enough space for one more Russian 'b'
//			info.append('б');
//			{
//				assertNotNull(localLogEntry.getError());
//				assertEquals(BufferOverflowException.class, localLogEntry.getError().getClass());
//			}

			info.commit();
		}

		LogFactory.stop();

		final String string = buffer.toString();
		assertEquals(maxMessageSize, string.length());
		assertEquals(maxMessageSize << 1, string.getBytes().length);
		for(int i = 0; i < maxMessageSize; i++){
			// Russian 'a'
			char c = (char) ('а' + i);
			assertEquals(c, string.charAt(i));
		}

	}
}
