package org.gflogger;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.gflogger.appender.AbstractAppenderFactory;
import org.gflogger.appender.AbstractAsyncAppender;
import org.gflogger.appender.AppenderFactory;
import org.gflogger.appender.ConsoleAppender;
import org.gflogger.appender.ConsoleAppenderFactory;
import org.gflogger.formatter.BytesOverflow;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
	                                                     final GFLoggerBuilder[] loggers,
	                                                     final AppenderFactory ... factories);

	protected LoggerService createLoggerService(final int maxMessageSize,
	                                            final ObjectFormatterFactory objectFormatterFactory,
	                                            final GFLoggerBuilder logger,
	                                            final AppenderFactory ... factories){
		return createLoggerService(maxMessageSize, objectFormatterFactory, new GFLoggerBuilder[]{logger}, factories);
	}

	protected LoggerService createLoggerService(final int maxMessageSize,
	                                            final GFLoggerBuilder[] loggers,
	                                            final AppenderFactory ... factories){
		return createLoggerService(maxMessageSize, null, loggers, factories);
	}

	protected LoggerService createLoggerService(final int maxMessageSize,
	                                            final GFLoggerBuilder logger,
	                                            final AppenderFactory ... factories){
		return createLoggerService(maxMessageSize, null, new GFLoggerBuilder[]{logger}, factories);
	}

	@Test
	public void testCommit() throws Exception {
		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		final int maxMessageSize = 32;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		factory.setMultibyte(false);
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService =
				createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		log.info().append("commited").commit();

		GFLogFactory.stop();

		assertEquals("commited", buffer.toString());
	}

	@Test
	public void testStartAndStop() throws Exception {
		final AtomicInteger startCalled = new AtomicInteger();
		final AtomicInteger stopCalled = new AtomicInteger();
		final AbstractAppenderFactory factory = new AbstractAppenderFactory<Appender>(){
			@Override
			public Appender createAppender( final Class<? extends LoggerService> loggerServiceClass ) {
				return new Appender<LogEntryItemImpl>() {
					@Override
					public boolean isMultibyte() {
						return false;
					}

					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public LogLevel getLogLevel() {
						return LogLevel.FATAL;
					}

					@Override
					public String getName() {
						return "";
					}

					@Override
					public int getIndex() {
						return 0;
					}

					@Override
					public void flush() {}

					@Override
					public void flush( final boolean force ) {}

					@Override
					public void process( final LogEntryItemImpl entry ) {}

					@Override
					public void onUncatchException( final Throwable e ) {}

					@Override
					public void start() {
						startCalled.incrementAndGet();
					}

					@Override
					public void stop() {
						stopCalled.incrementAndGet();
					}
				};
			}
		};

		final LoggerService loggerService = createLoggerService(
				1,
				new GFLoggerBuilder("com.db", factory),
				factory
		);

		GFLogFactory.init( loggerService );

		GFLogFactory.stop();

		assertEquals(
				".start() called once",
				1,
				startCalled.get()
		);

		assertEquals(
				".stop() called once",
				1,
				stopCalled.get()
		);
	}

	@Test
	public void everyMessageDeliveredToAppenderEvenIfAppenderThrowsExceptions() throws Exception {
		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		final String message = "anything";
		final int maxMessageSize = message.length();

		final CountingAppenderFactory factory = new CountingAppenderFactory( maxMessageSize );
		factory.setLogLevel(LogLevel.INFO);
		factory.setImmediateFlush( true );

		final LoggerService loggerService = createLoggerService(
				maxMessageSize,
				new GFLoggerBuilder("com.db", factory),
				factory
		);

		GFLogFactory.init( loggerService );

		final int messagesLogged = 1 << 10;
		for(int i = 0; i < messagesLogged; i++) {
			log.info().append( message ).commit();
		}

		GFLogFactory.stop();

		assertEquals(
				"Every message was delivered to appender",
				messagesLogged,
				factory.getMessagesProcessed()
		);
	}

	@Test
	public void everyExceptionThrownByAppenderIsDeliveredToOnUncatchException() throws Exception {
		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		final String message = "anything";
		final int maxMessageSize = message.length();

		final CountingAppenderFactory factory = new CountingAppenderFactory( maxMessageSize );
		factory.setLogLevel(LogLevel.INFO);
		factory.setImmediateFlush(true);

		final LoggerService loggerService = createLoggerService(
				maxMessageSize,
				new GFLoggerBuilder("com.db", factory),
				factory
		);

		GFLogFactory.init( loggerService );

		final int messagesLogged = 1 << 10;
		for(int i = 0; i < messagesLogged; i++) {
			log.info().append( message ).commit();
		}

		GFLogFactory.stop();

		assertEquals(
				"Every exception was delivered to appender.onUncatchException()",
				messagesLogged,
				factory.getUncatchExceptionsProcessed()
		);
	}

	@Test
	public void testCommitOnFailedProcessing() throws Exception {
		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		final int maxMessageSize = 32;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		factory.setMultibyte(false);
		final int limit = "commited".length() * 2;
		final CountDownLatch latch = new CountDownLatch(limit);
		final Appendable buffer = new Appendable() {

			final StringBuilder builder = new StringBuilder();
			int count = 0;
			@Override
			public Appendable append(CharSequence charSequence) throws IOException {
				t();
				builder.append(charSequence);

				return this;
			}

			private void t() {
				latch.countDown();
				if (count++ >= limit) {
					throw new RuntimeException("(Expected): count="+count+" > limit="+limit);
				}
			}

			@Override
			public Appendable append(CharSequence charSequence, int i, int i1) throws IOException {
				t();
				builder.append(charSequence, i, i1);

				return this;
			}

			@Override
			public Appendable append(char c) throws IOException {
				t();
				builder.append(c);

				return this;
			}

			@Override
			public String toString() {
				return builder.toString();
			}
		};
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		factory.setImmediateFlush(true);
		final LoggerService loggerService =
				createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		log.info().append("commited").commit();
		log.info().append("commited").commit();

		latch.await();

		Thread.sleep(100);

		for(int i = 0; i < (1 << 10); i++) {
			log.info().append("ignored").commit();
		}

		GFLogFactory.stop();

		assertEquals("commitedcommited", buffer.toString());
	}

	@Test
	public void testAppendDouble() throws Exception {
		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		final int maxMessageSize = 200;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		factory.setMultibyte(false);
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService =
				createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		log.info().
				append(6.0).
				append(6E1).
				append(6E100).
				append(6E-1).
				append(6E-10).
				append(6E-100).
				commit();

		//System.in.read();
		GFLogFactory.stop();

		assertEquals("6.000000000060.00000000006.0E1000.60000000000.00000000066.0E-100", buffer.toString());
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
					createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

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

		final GFLoggerBuilder comDbLogger = new GFLoggerBuilder(LogLevel.INFO, "com.db", factory);
		final GFLoggerBuilder rootLogger = new GFLoggerBuilder(LogLevel.DEBUG, null, factory);
		final LoggerService loggerService = createLoggerService(maxMessageSize,
				new GFLoggerBuilder[]{rootLogger, comDbLogger},
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

		final GFLoggerBuilder comDbLogger = new GFLoggerBuilder(LogLevel.INFO, "com.db", factory);
		final GFLoggerBuilder rootLogger = new GFLoggerBuilder(LogLevel.WARN, null, factory);
		final LoggerService loggerService = createLoggerService(maxMessageSize,
				new GFLoggerBuilder[]{rootLogger, comDbLogger},
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

		final GFLoggerBuilder logger = new GFLoggerBuilder(LogLevel.INFO, "com.db.", factory);
		final GFLoggerBuilder logger2 = new GFLoggerBuilder(LogLevel.ERROR, "com.db.messaging", factory);
		final GFLoggerBuilder rootLogger = new GFLoggerBuilder(LogLevel.WARN, null, factory);

		final LoggerService loggerService =
				createLoggerService(maxMessageSize,
						new GFLoggerBuilder[]{logger, logger2, rootLogger}, factory);

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

		final ConsoleAppenderFactory factory2 = new ConsoleAppenderFactory();
		factory2.setLayoutPattern("%m");
		final StringBuffer buffer2 = new StringBuffer();
		factory2.setOutputStream(buffer2);
		factory2.setLogLevel(LogLevel.INFO);

		final LoggerService loggerService = createLoggerService(maxMessageSize,
				new GFLoggerBuilder[]{new GFLoggerBuilder(factory2),
						new GFLoggerBuilder("com.db.", factory)},
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
	public void testHigherLoggerLevelAppender() throws Exception {
		final int maxMessageSize = 32;

		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);

		final ConsoleAppenderFactory factory2 = new ConsoleAppenderFactory();
		factory2.setLayoutPattern("%m");
		final StringBuffer buffer2 = new StringBuffer();
		factory2.setOutputStream(buffer2);
		factory2.setLogLevel(LogLevel.FATAL);

		final LoggerService loggerService = createLoggerService(maxMessageSize,
				new GFLoggerBuilder[]{new GFLoggerBuilder(factory)},
				factory2);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		log.info().append("com.db.fxpricing.Logger.info").commit();

		GFLogFactory.stop();

		//assertEquals("com.db.fxpricing.Logger.info", buffer.toString());
		assertEquals("", buffer2.toString());
	}

	@Test
	@Ignore
	public void testAppendTruncatedMessage() throws Exception {
		// TODO
		final String placeholder = ">>>";
		System.setProperty("gflogger.errorMessage", placeholder);

		final String tooLongMessage = "too long message!";
		final int maxMessageSize = tooLongMessage.length() - 1;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		{
			final GFLogEntry info = log.info().append(tooLongMessage);
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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		{
			final FormattedGFLogEntry info = log.info(tooLongMessage);
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
	@Ignore
	public void testAppendLatinCharsFullMessageSize() throws Exception {
		// TODO
		// abcdefghijklmnopqrstuvwxyz{|}
		final int maxMessageSize = 29;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		{
			final GFLogEntry info = log.info();
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
		// TODO
		final int maxMessageSize = 30;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setMultibyte(true);
		factory.setLayoutPattern("%m");
		factory.setLogLevel(LogLevel.INFO);
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);

		final GFLoggerBuilder logger = new GFLoggerBuilder("com.db.", factory);

		final LoggerService loggerService = createLoggerService(maxMessageSize, logger, factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		{
			final GFLogEntry info = log.info();
			for(int i = 0; i < maxMessageSize; i++){
				// Russian 'a'
				char c = (char) ('\u0430' + i);
				info.append(c);
			}

			assertTrue(info instanceof LocalLogEntry);

			final LocalLogEntry localLogEntry = (LocalLogEntry)info;

			assertNull(localLogEntry.getError());
			// there is no enough space for one more Russian 'b'
			info.append('\u0431');
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
			char c = (char) ('\u0430' + i);
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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

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
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
		log.info("say hello world");

		GFLogFactory.stop();

		final String string = buffer.toString();
		assertEquals("say hello world", string);
	}

	@Test
	public void testAppendFormattedWithArrayPlaceholder() throws Exception {
		final int maxMessageSize = 64;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
		log.info("value: %s;").withLast(new String[0], ", ");
		log.info("value: %s;").withLast(new String[]{"a"}, ", ");
		log.info("value: %s;").withLast(new String[]{"b", "a"}, ", ");
		log.info("value: %s;").withLast(new String[]{null, "q", null}, ", ");

		GFLogFactory.stop();

		final String string = buffer.toString();
		assertEquals("value: [];value: [a];value: [b, a];value: [null, q, null];", string);
	}

	@Test
	public void testAppendFormattedWithIterablePlaceholder() throws Exception {
		final int maxMessageSize = 64;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

		GFLogFactory.init(loggerService);


		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
		log.info("value: %s;").withLast(Arrays.asList(), ", ");
		log.info("value: %s;").withLast(Arrays.asList("a"), ", ");
		log.info("value: %s;").withLast(Arrays.asList("b", "a"), ", ");
		log.info("value: %s;").withLast(Arrays.asList(null, "q", null), ", ");

		GFLogFactory.stop();

		final String string = buffer.toString();
		assertEquals("value: [];value: [a];value: [b, a];value: [null, q, null];", string);
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
				new GFLoggerBuilder("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
		log.info("say hello %s world").withLast(new Foo(5));

		GFLogFactory.stop();

		final String string = buffer.toString();
		assertEquals("say hello v:5 world", string);
	}

	@Test
	public void testUseLoggerAfterCommit() throws Exception {
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
				new GFLoggerBuilder("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");
		final GFLogEntry info = log.info();
		info.append("test");
		info.commit();

		try {
			info.append("test");
			fail();
		} catch (IllegalStateException e){
			// ok
		}

		try {
			info.append(true);
			fail();
		} catch (IllegalStateException e){
			// ok
		}

		try {
			info.append(0);
			fail();
		} catch (IllegalStateException e){
			// ok
		}

		try {
			info.append(Long.MAX_VALUE - 100000L);
			fail();
		} catch (IllegalStateException e){
			// ok
		}

		try {
			info.commit();
			fail();
		} catch (IllegalStateException e){
			// ok
		}

		GFLogFactory.stop();

		final String string = buffer.toString();
		assertEquals("test", string);
	}

	@Test
	public void testAppenderIsAboutToFinish() throws Exception {
		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		final int maxMessageSize = 32;
		final StringBuffer buffer = new StringBuffer();
		final AtomicInteger workerIsAboutToFinish = new AtomicInteger(0);
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory(){
			@Override
			protected ConsoleAppender createAppender() {
				return new ConsoleAppender(bufferSize, multibyte, outputStream){
					@Override
					public void workerIsAboutToFinish() {
						workerIsAboutToFinish.incrementAndGet();
						super.workerIsAboutToFinish();
					}
				};
			}
		};
		factory.setLayoutPattern("%m");
		factory.setMultibyte(false);
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService =
				createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		log.info().append("commited").commit();

		GFLogFactory.stop();

		assertEquals("commited", buffer.toString());
		assertEquals(1, workerIsAboutToFinish.get());
	}

	@Ignore
	@Test
	public void testMemoryConsumption() throws Exception {
		for(int i = 0; i < 10000; i++){
			final int maxMessageSize = 64;
			final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
			factory.setLayoutPattern("%m");
			final StringBuffer buffer = new StringBuffer();
			factory.setOutputStream(buffer);
			factory.setLogLevel(LogLevel.INFO);
			factory.setMultibyte(true);
			final LoggerService loggerService = createLoggerService(maxMessageSize, new GFLoggerBuilder("com.db", factory), factory);

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
		public void append(Foo obj, GFLogEntry entry) {
			entry.append("v:").append(obj.v);
		}
	}

	private static class CountingAppenderFactory extends AbstractAppenderFactory<Appender> {
		private final int maxMessageSize;

		private final AtomicLong messagesProcessed = new AtomicLong( 0 );
		private final AtomicLong uncatchExceptionsProcessed = new AtomicLong( 0 );

		public CountingAppenderFactory( final int maxMessageSize ) {
			this.maxMessageSize = maxMessageSize;
		}

		@Override
		public Appender createAppender( final Class<? extends LoggerService> loggerServiceClass ) {
			return new AbstractAsyncAppender( maxMessageSize, false) {
				@Override
				public String getName() {
					return "CountingAppender";
				}

				@Override
				public void process( final LogEntryItemImpl entry ) {
					messagesProcessed.incrementAndGet();
					throw new RuntimeException( "Intentionally (!) thrown exception" );
				}

				@Override
				public void onUncatchException( final Throwable e ) {
					uncatchExceptionsProcessed.incrementAndGet();
				}

				@Override
				public void flush( final boolean force ) {}
			};
		}

		public long getMessagesProcessed() {
			return messagesProcessed.get();
		}

		public long getUncatchExceptionsProcessed() {
			return uncatchExceptionsProcessed.get();
		}
	}
}
