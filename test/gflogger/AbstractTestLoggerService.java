package gflogger;

import static org.junit.Assert.*;
import gflogger.appender.AppenderFactory;
import gflogger.appender.ConsoleAppenderFactory;

import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public abstract class AbstractTestLoggerService {

	@AfterClass
	public static void shutdown(){
//		LogFactory.stop();
	}

	@Before
	public void setUp(){
		System.setProperty("gflogger.errorMessage", "");
	}

	protected abstract LoggerService createLoggerService(final int maxMessageSize,
		final ObjectFormatterFactory objectFormatterFactory,
		final AppenderFactory ... factories);

	protected LoggerService createLoggerService(final int maxMessageSize,
		final AppenderFactory ... factories){
		return createLoggerService(maxMessageSize, null, factories);
	}

	@Test
	public void testLateInit() throws Exception {
		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");
		logger.info().append("info").commit();

		final int maxMessageSize = 32;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

		LogFactory.init("com.db", loggerService);

		logger.error().append("error").commit();

		LogFactory.stop();

		assertEquals("error", buffer.toString());
	}

	@Test
	public void testLogLevels() throws Exception {
		final int maxMessageSize = 32;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

		LogFactory.init("com.db", loggerService);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");

		logger.debug().append("debug").commit();
		logger.info().append("info").commit();
		logger.error().append("error").commit();

		LogFactory.stop();

		assertEquals("infoerror", buffer.toString());
	}

	@Test
	public void testLogHierarchyLevelsRootDebugOthersInfo() throws Exception {
		final int maxMessageSize = 32;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.DEBUG);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

		final Map<String, LoggerService> services = new HashMap<String, LoggerService>();
		services.put("com.db", new LoggerServiceView(loggerService, LogLevel.INFO));
		services.put(null, loggerService);
		LogFactory.init(services);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");

		logger.debug().append("com.db.debug").commit();
		logger.info().append("com.db.info").commit();
		logger.error().append("com.db.error").commit();

		final Logger logger2 = LogFactory.getLog("com");

		logger2.debug().append("com.debug").commit();
		logger2.info().append("com.info").commit();
		logger2.error().append("com.error").commit();

		LogFactory.stop();

		assertEquals("com.db.info" +
				"com.db.error" +
				"com.debug" +
				"com.info" +
				"com.error", buffer.toString());
	}

	@Test
	public void testLogHierarchyLevelsRootWarnOtherInfo() throws Exception {
		final int maxMessageSize = 32;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.DEBUG);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

		final Map<String, LoggerService> services = new HashMap<String, LoggerService>();
		services.put("com.db", new LoggerServiceView(loggerService, LogLevel.INFO));
		services.put(null, new LoggerServiceView(loggerService, LogLevel.WARN));
		LogFactory.init(services);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");

		logger.debug().append("com.db.debug").commit();
		logger.info().append("com.db.info").commit();
		logger.error().append("com.db.error").commit();

		final Logger logger2 = LogFactory.getLog("org");

		logger2.debug().append("org.debug").commit();
		logger2.info().append("org.info").commit();
		logger2.error().append("org.error").commit();

		LogFactory.stop();

		assertEquals("com.db.infocom.db.errororg.error", buffer.toString());
	}

	@Test
	public void testRootAppender() throws Exception {
		final int maxMessageSize = 32;

		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

		final ConsoleAppenderFactory factory2 = new ConsoleAppenderFactory();
		factory2.setLayoutPattern("%m");
		final StringBuffer buffer2 = new StringBuffer();
		factory2.setOutputStream(buffer2);
		factory2.setLogLevel(LogLevel.INFO);
		final LoggerService rootLoggerService = createLoggerService(maxMessageSize, factory2);

		final Map<String, LoggerService> services = new HashMap<String, LoggerService>();
		services.put("com.db", loggerService);
		services.put(null, rootLoggerService);
		LogFactory.init(services);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");

		logger.info().append("com.db.fxpricing.Logger.info").commit();

		final Logger logger2 = LogFactory.getLog("com");
		logger2.info().append("com.info").commit();

		LogFactory.stop();

		assertEquals("com.db.fxpricing.Logger.info", buffer.toString());

		assertEquals("com.info", buffer2.toString());
	}

	@Test
	public void testAppendTruncatedMessage() throws Exception {
		final String placeholder = ">>>";
		System.setProperty("gflogger.errorMessage", placeholder);

		final String tooLongMessage = "too long message!";
		final int maxMessageSize = tooLongMessage.length() - 1;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

		LogFactory.init("com.db", loggerService);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");

		{
			final LogEntry info = logger.info().append(tooLongMessage);
			assertTrue(info instanceof LocalLogEntry);

			final LocalLogEntry localLogEntry = (LocalLogEntry)info;

			assertNotNull(localLogEntry.getError());
			final Class errorClass = localLogEntry.getError().getClass();
			assertTrue("failed on buffer.position:" + errorClass.getName(),
				BufferOverflowException.class.equals(errorClass) ||
				AssertionError.class.equals(errorClass));

			info.commit();
		}

		LogFactory.stop();

		final String string = buffer.toString();
		assertEquals(string, maxMessageSize, string.length());

		final String expected =
			tooLongMessage.substring(0, maxMessageSize - placeholder.length()) + placeholder;
		assertEquals(expected, string);
	}

	@Test
	public void testAppendTruncatedMessageWithDigits() throws Exception {
		final String placeholder = ">";
		System.setProperty("gflogger.errorMessage", placeholder);

		final String tooLongMessage = "value is %s%s";
		final int maxMessageSize = "value is  ".length();
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

		LogFactory.init("com.db", loggerService);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");

		{
			final FormattedLogEntry info = logger.info(tooLongMessage);
			assertTrue(info instanceof LocalLogEntry);

			final LocalLogEntry localLogEntry = (LocalLogEntry)info;
			info.with(1234567890L);

			assertNotNull(localLogEntry.getError());
			assertTrue("failed on buffer.position",
				IllegalArgumentException.class.equals(localLogEntry.getError().getClass()) ||
				AssertionError.class.equals(localLogEntry.getError().getClass()));
			info.withLast("");
		}

		LogFactory.stop();

		final String string = buffer.toString();
		assertEquals(string, maxMessageSize, string.length());

		final String expected = "value is >";
		assertEquals(expected, string);
	}

	@Test
    public void testAppendLatinCharsFullMessageSize() throws Exception {
		// abcdefghijklmnopqrstuvwxyz{|}
		final int maxMessageSize = 29;
	    final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
	    factory.setLayoutPattern("%m");
	    final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
	    factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

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
		System.out.println(string);
		assertEquals(maxMessageSize, string.length());

		for(int i = 0; i < maxMessageSize; i++){
			char c = (char) ('a' + i);
			assertEquals(Character.toString(c), c, string.charAt(i));
		}
    }

	@Test
	public void testAppendCyrillicCharsFullMessageSize() throws Exception {
		final int maxMessageSize = 30;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setMultibyte(true);
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

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
			// there is no enough space for one more Russian 'b'
			info.append('б');
			{
				assertNotNull(localLogEntry.getError());
				assertEquals(BufferOverflowException.class, localLogEntry.getError().getClass());
			}

			info.commit();
		}

		LogFactory.stop();

		final String string = buffer.toString();
		assertEquals(maxMessageSize, string.length());
		// unicode char is two byte char
		assertEquals(maxMessageSize << 1, string.getBytes().length);
		for(int i = 0; i < maxMessageSize; i++){
			// Russian 'a'
			char c = (char) ('а' + i);
			assertEquals(c, string.charAt(i));
		}

	}

	@Test
	public void testAppendFormattedWithWithLastMessage() throws Exception {
		final int maxMessageSize = 64;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

		LogFactory.init("com.db", loggerService);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");
		logger.info("say hello %% %s %").withLast("world");

		LogFactory.stop();

		final String string = buffer.toString();
		assertEquals("say hello % world %", string);
	}

	@Test
	public void testAppendFormattedWithLastMessage() throws Exception {
		final int maxMessageSize = 64;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

		LogFactory.init("com.db", loggerService);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");
		logger.info("say %s hello %% %s %").with("a").withLast("world");

		LogFactory.stop();

		final String string = buffer.toString();
		assertEquals("say a hello % world %", string);
	}

	@Test
	public void testAppendFormattedWithWrongPlaceholder() throws Exception {
		final int maxMessageSize = 64;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

		LogFactory.init("com.db", loggerService);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");
		try {
			logger.info("say hello %d !").withLast("world");
			fail();
		} catch(IllegalArgumentException e){
			// ok
		}
	}

	@Test
	public void testAppendFormattedWithNoMorePlaceholder() throws Exception {
		final int maxMessageSize = 64;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

		LogFactory.init("com.db", loggerService);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");
		try {
			logger.info("say hello %s").with("world").with("world");
			fail();
		} catch(IllegalStateException e){
			// ok
		}
		LogFactory.stop();

		final String string = buffer.toString();
		assertEquals("say hello world", string);
	}

	@Test
	public void testAppendFormattedWithLessPlaceholdersThanRequired() throws Exception {
		final int maxMessageSize = 64;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

		LogFactory.init("com.db", loggerService);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");
		try {
			logger.info("say hello %s %s").withLast("world");

			LogFactory.stop();
			fail(buffer.toString());
		} catch(IllegalStateException e){
			// ok
		}
		LogFactory.stop();

		final String string = buffer.toString();
		assertEquals("", string);
	}

	@Test
	public void testAppendFormattedWithAutoCommit() throws Exception {
		final int maxMessageSize = 64;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

		LogFactory.init("com.db", loggerService);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");
		logger.info("say hello world");

		LogFactory.stop();

		final String string = buffer.toString();
		assertEquals("say hello world", string);
	}

	@Test
	public void testAppendObjectFormatter() throws Exception {
		final int maxMessageSize = 64;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);

		final DefaultObjectFormatterFactory defaultObjectFormatterFactory =
			new DefaultObjectFormatterFactory();
		defaultObjectFormatterFactory.registerObjectFormatter(Foo.class, new FooObjectFormatter());
		final LoggerService loggerService = createLoggerService(maxMessageSize, defaultObjectFormatterFactory, factory);

		LogFactory.init("com.db", loggerService);

		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");
		logger.info("say hello %s world").withLast(new Foo(5));

		LogFactory.stop();

		final String string = buffer.toString();
		assertEquals("say hello v:5 world", string);
	}

	@Test
	public void testMemoryConsumption() throws Exception {
		for(int i = 0; i < 1000; i++){
			final int maxMessageSize = 64;
			final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
			factory.setLayoutPattern("%m");
			final StringBuffer buffer = new StringBuffer();
			factory.setOutputStream(buffer);
			factory.setLogLevel(LogLevel.INFO);
			factory.setMultibyte(true);
			final LoggerService loggerService = createLoggerService(maxMessageSize, factory);

			LogFactory.init("com.db", loggerService);

			final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");
			logger.info("say hello world");

			LogFactory.stop();
		}
	}

	private static class Foo {
		private final long v;

		public Foo(long v) {
			this.v = v;
		}

		@Override
		public String toString() {
			return "[" + v + "]";
		}
	}

	private static class FooObjectFormatter implements ObjectFormatter<Foo>{

		@Override
		public void append(Foo obj, LogEntry entry) {
			entry.append("v:").append(obj.v);
		}
	}
}