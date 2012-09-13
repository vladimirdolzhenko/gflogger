package org.gflogger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.BufferOverflowException;
import java.util.Arrays;

import org.gflogger.appender.AppenderFactory;
import org.gflogger.appender.ConsoleAppenderFactory;
import org.gflogger.formatter.BytesOverflow;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
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
		final GFLogger[] loggers,
		final AppenderFactory ... factories);

	protected LoggerService createLoggerService(final int maxMessageSize,
			final ObjectFormatterFactory objectFormatterFactory,
			final GFLogger logger,
			final AppenderFactory ... factories){
		return createLoggerService(maxMessageSize, objectFormatterFactory, new GFLogger[]{logger}, factories);
	}

	protected LoggerService createLoggerService(final int maxMessageSize,
		final GFLogger[] loggers,
		final AppenderFactory ... factories){
		return createLoggerService(maxMessageSize, null, loggers, factories);
	}

	protected LoggerService createLoggerService(final int maxMessageSize,
			final GFLogger logger,
			final AppenderFactory ... factories){
		return createLoggerService(maxMessageSize, null, new GFLogger[]{logger}, factories);
	}

	@Test
	public void testCommitUncommited() throws Exception {
		for(boolean multibyte : new boolean[]{false, true}){
			final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

			final int maxMessageSize = 32;
			final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
			factory.setLayoutPattern("%m");
			factory.setMultibyte(multibyte);
			final StringBuffer buffer = new StringBuffer();
			factory.setOutputStream(buffer);
			factory.setLogLevel(LogLevel.INFO);
			final LoggerService loggerService =
				createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

			GFLogFactory.init(loggerService);

			log.info().append("uncommited");
			log.info().append("commited").commit();

			GFLogFactory.stop();

			assertEquals("uncommitedcommited", buffer.toString());
		}
	}

	@Test
	public void testLateInit() throws Exception {
		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
		log.info().append("info").commit();

		final int maxMessageSize = 32;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		log.error().append("error").commit();

		GFLogFactory.stop();

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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		log.debug().append("debug").commit();
		log.info().append("info").commit();
		log.error().append("error").commit();

		GFLogFactory.stop();

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

		final GFLogger comDbLogger = new GFLoggerImpl(LogLevel.INFO, "com.db", factory);
		final GFLogger rootLogger = new GFLoggerImpl(LogLevel.DEBUG, null, factory);
		final LoggerService loggerService = createLoggerService(maxMessageSize,
			new GFLogger[]{rootLogger, comDbLogger},
			factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		log.debug().append("com.db.debug").commit();
		log.info().append("com.db.info").commit();
		log.error().append("com.db.error").commit();

		final GFLog logger2 = GFLogFactory.getLog("com");

		logger2.debug().append("com.debug").commit();
		logger2.info().append("com.info").commit();
		logger2.error().append("com.error").commit();

		GFLogFactory.stop();

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

		final GFLogger comDbLogger = new GFLoggerImpl(LogLevel.INFO, "com.db", factory);
		final GFLogger rootLogger = new GFLoggerImpl(LogLevel.WARN, null, factory);
		final LoggerService loggerService = createLoggerService(maxMessageSize,
			new GFLogger[]{rootLogger, comDbLogger},
			factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		log.debug().append("com.db.debug").commit();
		log.info().append("com.db.info").commit();
		log.error().append("com.db.error").commit();

		final GFLog log2 = GFLogFactory.getLog("org");

		log2.debug().append("org.debug").commit();
		log2.info().append("org.info").commit();
		log2.error().append("org.error").commit();

		GFLogFactory.stop();

		assertEquals("com.db.infocom.db.errororg.error", buffer.toString());
	}

	@Test
	public void testLogHierarchyLevels() throws Exception {
		final int maxMessageSize = 32;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);

		final GFLogger logger = new GFLoggerImpl(LogLevel.INFO, "com.db.", factory);
		final GFLogger logger2 = new GFLoggerImpl(LogLevel.ERROR, "com.db.messaging", factory);
		final GFLogger rootLogger = new GFLoggerImpl(LogLevel.WARN, null, factory);

		final LoggerService loggerService =
			createLoggerService(maxMessageSize,
				new GFLogger[]{logger, logger2, rootLogger}, factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		log.debug().append("com.db.debug").commit();
		log.info().append("com.db.info").commit();
		log.error().append("com.db.error").commit();

		final GFLog rootLog = GFLogFactory.getLog("org");

		rootLog.debug().append("org.debug").commit();
		rootLog.info().append("org.info").commit();
		rootLog.error().append("org.error").commit();

		final GFLog log2 = GFLogFactory.getLog("com.db.messaging.Publisher");
		log2.debug().append("messaging.debug").commit();
		log2.info().append("messaging.info").commit();
		log2.error().append("messaging.error").commit();

		GFLogFactory.stop();

		assertEquals("com.db.infocom.db.errororg.errormessaging.error", buffer.toString());
	}

	@Test
	public void testRootAppender() throws Exception {
		final int maxMessageSize = 32;

		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		factory.setIndex(0);

		final ConsoleAppenderFactory factory2 = new ConsoleAppenderFactory();
		factory2.setLayoutPattern("%m");
		final StringBuffer buffer2 = new StringBuffer();
		factory2.setOutputStream(buffer2);
		factory2.setLogLevel(LogLevel.INFO);
		factory2.setIndex(1);

		final LoggerService loggerService = createLoggerService(maxMessageSize,
			new GFLogger[]{new GFLoggerImpl(factory2),
				new GFLoggerImpl("com.db.", factory)},
			factory, factory2);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		log.info().append("com.db.fxpricing.Logger.info").commit();

		final GFLog log2 = GFLogFactory.getLog("com");
		log2.info().append("com.info").commit();

		GFLogFactory.stop();

		assertEquals("com.db.fxpricing.Logger.info", buffer.toString());

		assertEquals("com.info", buffer2.toString());
	}

	@Test
	@Ignore
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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		{
			final LogEntry info = log.info().append(tooLongMessage);
			assertTrue(info instanceof LocalLogEntry);

			final LocalLogEntry localLogEntry = (LocalLogEntry)info;

			assertNotNull(localLogEntry.getError());
			final Class errorClass = localLogEntry.getError().getClass();
			assertTrue("failed on buffer.position:" + errorClass.getName(),
				BufferOverflowException.class.equals(errorClass) ||
				BytesOverflow.class.equals(errorClass) ||
				AssertionError.class.equals(errorClass));

			info.commit();
		}

		GFLogFactory.stop();

		final String string = buffer.toString();
		assertEquals(string, maxMessageSize, string.length());

		final String expected =
			tooLongMessage.substring(0, maxMessageSize - placeholder.length()) + placeholder;
		assertEquals(expected, string);
	}

	@Test
	@Ignore
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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		{
			final FormattedLogEntry info = log.info(tooLongMessage);
			assertTrue(info instanceof LocalLogEntry);

			final LocalLogEntry localLogEntry = (LocalLogEntry)info;
			info.with(1234567890L);

			assertNotNull(localLogEntry.getError());
			final Class errorClass =
					localLogEntry.getError().getClass();
			assertTrue("failed on buffer.position",
				IllegalArgumentException.class.equals(errorClass) ||
				BytesOverflow.class.equals(errorClass) ||
				AssertionError.class.equals(errorClass));
			info.withLast("");
		}

		GFLogFactory.stop();

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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		{
			final LogEntry info = log.info();
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
				final Class<? extends Throwable> errorClazz = localLogEntry.getError().getClass();

				assertTrue(BufferOverflowException.class.equals(errorClazz) ||
					BytesOverflow.class.equals(errorClazz));
			}


			info.commit();
		}

		GFLogFactory.stop();

		final String string = buffer.toString();
		assertEquals(maxMessageSize, string.length());

		for(int i = 0; i < maxMessageSize; i++){
			char c = (char) ('a' + i);
			assertEquals(Character.toString(c), c, string.charAt(i));
		}
    }

	@Test
	@Ignore
	public void testAppendCyrillicCharsFullMessageSize() throws Exception {
		final int maxMessageSize = 30;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setMultibyte(true);
		factory.setLayoutPattern("%m");
		factory.setLogLevel(LogLevel.INFO);
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);

		final GFLoggerImpl logger = new GFLoggerImpl("com.db.", factory);

		final LoggerService loggerService = createLoggerService(maxMessageSize, logger, factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		{
			final LogEntry info = log.info();
			for(int i = 0; i < maxMessageSize; i++){
				// Russian 'a'
				char c = (char) ('�' + i);
				info.append(c);
			}

			assertTrue(info instanceof LocalLogEntry);

			final LocalLogEntry localLogEntry = (LocalLogEntry)info;

			assertNull(localLogEntry.getError());
			// there is no enough space for one more Russian 'b'
			info.append('�');
			{
				assertNotNull(localLogEntry.getError());
				assertEquals(BufferOverflowException.class, localLogEntry.getError().getClass());
			}

			info.commit();
		}

		GFLogFactory.stop();

		final String string = buffer.toString();
		assertEquals(maxMessageSize, string.length());
		// unicode char is two byte char
		final byte[] bytes = string.getBytes();
		System.out.println(string);
		System.out.println(Arrays.toString(bytes));

		assertEquals(maxMessageSize << 1, bytes.length);
		for(int i = 0; i < maxMessageSize; i++){
			// Russian 'a'
			char c = (char) ('�' + i);
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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
		log.info("say hello %% %s %").withLast("world");

		GFLogFactory.stop();

		final String string = buffer.toString();
		assertEquals("say hello % world %", string);
	}

	@Test
	public void testAppendFormattedWithLimitedAppenderBufferSize() throws Exception {
		final int maxMessageSize = 64;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		final String targetMsg = "say hello % world %";
		factory.setBufferSize(targetMsg.length() + 2);
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		final StringBuilder expected = new StringBuilder();
		for(int i = 0; i < 10; i++){
			log.info("say hello %% %s %").withLast("world");
			expected.append(targetMsg);
		}

		GFLogFactory.stop();

		final String string = buffer.toString();
		assertEquals(expected.toString(), string);
	}

	@Test
	public void testAppendFormattedWithLastMessage() throws Exception {
		final int maxMessageSize = 64;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
		log.info("say %s hello %% %s %").with("a").withLast("world");

		GFLogFactory.stop();

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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
		try {
			log.info("say hello %d !").withLast("world");
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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
		try {
			log.info("say hello %s").with("world").with("world");
			fail();
		} catch(IllegalStateException e){
			// ok
		}
		GFLogFactory.stop();

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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
		try {
			log.info("say hello %s %s").withLast("world");

			GFLogFactory.stop();
			fail(buffer.toString());
		} catch(IllegalStateException e){
			// ok
		}
		GFLogFactory.stop();

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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
		log.info("say hello world");

		GFLogFactory.stop();

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
		final LoggerService loggerService = createLoggerService(maxMessageSize,
			defaultObjectFormatterFactory,
			new GFLoggerImpl("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
		log.info("say hello %s world").withLast(new Foo(5));

		GFLogFactory.stop();

		final String string = buffer.toString();
		assertEquals("say hello v:5 world", string);
	}

	@Test
	@Ignore
	public void testMemoryConsumption() throws Exception {
		for(int i = 0; i < 1000; i++){
			final int maxMessageSize = 64;
			final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
			factory.setLayoutPattern("%m");
			final StringBuffer buffer = new StringBuffer();
			factory.setOutputStream(buffer);
			factory.setLogLevel(LogLevel.INFO);
			factory.setMultibyte(true);
			final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerImpl("com.db", factory), factory);

			GFLogFactory.init(loggerService);

			final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
			log.info("say hello world");

			GFLogFactory.stop();
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
