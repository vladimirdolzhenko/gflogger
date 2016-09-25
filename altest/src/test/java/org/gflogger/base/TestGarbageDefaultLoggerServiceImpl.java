package org.gflogger.base;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import com.google.monitoring.runtime.instrumentation.Sampler;
import org.apache.log4j.BasicConfigurator;
import org.gflogger.*;
import org.gflogger.appender.ConsoleAppenderFactory;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * TestGarbageDefaultLoggerServiceImpl
 *
 * have to run with jvm option -javaagent:libs/allocation.jar
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestGarbageDefaultLoggerServiceImpl {
	static final AtomicBoolean detailedAllocation = new AtomicBoolean(false);

	static final AtomicBoolean objectCounting = new AtomicBoolean(false);
	static final AtomicInteger objectCount = new AtomicInteger();
	static final AtomicLong objectSize = new AtomicLong();

	private static final int WARMUP_COUNT = 500;
	private static final int TEST_COUNT = 100;

	static void resetObjectCounting(){
		detailedAllocation.set(false);
		objectCounting.set(false);
		objectCount.set(0);
		objectSize.set(0);
	}

	@BeforeClass
	public static void init(){

		final ThreadLocal<StringBuilder> local = new ThreadLocal<StringBuilder>(){
			@Override
			public StringBuilder get() {
				return new StringBuilder(1 << 10);
			}
		};

		final ThreadLocal<String> threadName = new ThreadLocal<String>(){
			@Override
			protected String initialValue() {
				return Thread.currentThread().getName();
			}
		};

		// pre init
		local.get();
		threadName.get();

		AllocationRecorder.addSampler(new Sampler() {

			@Override
			public void sampleAllocation(int count, String desc, Object newObj, long size) {
			  if (!objectCounting.get() ||
					  // bypass for gradle workers thread
					  threadName.get().contains("0.0.0.0:"))
			  	return;

			  objectCount.incrementAndGet();
			  objectSize.addAndGet(size);

			  objectCounting.getAndSet(false);
			  try {

				  final StringBuilder builder = local.get();
				  builder.setLength(0);
				  if (count != -1) {
					  builder.append("an array of ").
						append(newObj.getClass().getComponentType().getName()).
						append("[").append(count).append("]");
				  } else {
					  if (newObj instanceof String){
						  builder.append("allocated the string '").append(newObj).
							  append('\'');
						} else {
							builder.append("allocated the object ").append(newObj).
							append(" of type ").append(desc).append(" whose size is ").append(size);
						}
				  }
				  //builder.append(" [").append(threadName.get()).append(']');

				  if (!detailedAllocation.get()) return;
				  System.out.println(builder);
			  } finally {
				  objectCounting.set(true);
			  }
			  /*/
			  //*/
			}
		  });
		BasicConfigurator.configure();
		org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("com.db.fxpricing.Logger");
	}

	@AfterClass
	public static void shutdown(){
		resetObjectCounting();
	}

	@Before
	public void setUp(){
		resetObjectCounting();

		objectCounting.set(true);

		final StringBuilder builder = new StringBuilder();
		builder.append("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

		assertEquals(74, builder.length());
		Assume.assumeTrue(
				"-javaagent:java-allocation-instrumenter.jar is not enabled",
				objectCount.get() > 0
		);

		resetObjectCounting();
	}

	// TODO
	@Ignore
	@Test
	public void testGFLoggerAppendLongAndDouble() throws Exception {
		final int maxMessageSize = 20;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		// 1k
		final StringBuffer buffer = new StringBuffer(1<<20);
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = new LoggerServiceImpl(4, maxMessageSize,
			new GFLoggerBuilder[]{new GFLoggerBuilder("com.db", factory)},
			factory);

		GFLogFactory.init(loggerService);

		final GFLog logger = GFLogFactory.getLog("com.db.fxpricing.Logger");

		for(long i = -WARMUP_COUNT; i < WARMUP_COUNT; i++)
			logger.info().append("value:").append(i).commit();
		Thread.sleep(1000L);

		objectCounting.set(true);

		for(long i = -TEST_COUNT; i < TEST_COUNT; i++)
			logger.info().append("value:").append(i).commit();

		for(long i = -TEST_COUNT; i < TEST_COUNT; i++)
			logger.info().append("value:").append(i/2.0, 4).commit();

		Thread.sleep(500L);

		objectCounting.set(false);

		GFLogFactory.stop();

		final String string = buffer.toString();

		final StringBuffer buffer2 = new StringBuffer(1<<20);

		for(long i = -WARMUP_COUNT; i < WARMUP_COUNT; i++)
			buffer2.append("value:" + i);
		for(long i = -TEST_COUNT; i < TEST_COUNT; i++)
			buffer2.append("value:" + i);
		for(long i = -TEST_COUNT; i < TEST_COUNT; i++)
			buffer2.append("value:" +
				(i == 0 ?
					String.format(Locale.ENGLISH, "%.1f", (i/2.0)) :
					String.format(Locale.ENGLISH, "%.4f", (i/2.0))
				));

		assertEquals(buffer2.toString(), string);

		assertEquals(0, objectCount.get());
		printState("org.gflogger");
	}

	@Test
	public void testLog4JAppendString() throws Exception {
		BasicConfigurator.configure();
		org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog("com.db.fxpricing.Logger");

		for(long i = 0; i < WARMUP_COUNT; i++)
			log.info("value");
		Thread.sleep(1000L);

		objectCounting.set(true);
		for(long i = 0; i < TEST_COUNT; i++)
			log.info("value");

		Thread.sleep(500L);

		printState("log4j-string");
	}

	@Test
	public void testLog4JAppendLong() throws Exception {
		BasicConfigurator.configure();
		org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog("com.db.fxpricing.Logger");

		for(long i = 0; i < WARMUP_COUNT; i++)
			log.info("value:" + i);
		Thread.sleep(1000L);

		objectCounting.set(true);
		for(long i = 0; i < TEST_COUNT; i++)
			log.info("value:" + i);

		Thread.sleep(500L);

		printState("log4j-long");
	}

	@Test
	public void testMessageFormat() throws Exception {
		detailedAllocation.set(true);
		objectCounting.set(true);

		int l = 0;
		{
			// warm it up
			String s = String.format("value is %d", 100000L);
			l += s.length();
		}

		System.out.println("----------");
		resetObjectCounting();

		for(int i = 0; i < 10; i++){
			if ( i == 9 ){
				detailedAllocation.set(true);
				objectCounting.set(true);
			}
			String s = String.format("value is %d", (100000L + i));
			l += s.length();
		}

		printState("String.format");

		resetObjectCounting();

		for(int i = 0; i < 10; i++){
			if ( i == 9 ){
				detailedAllocation.set(true);
				objectCounting.set(true);
			}
			String s = "value is " + (100000L + i);
			l += s.length();
		}

		printState("StringBuilder");

		resetObjectCounting();

		System.out.println(l);
	}

	@Test
	public void testLog4JAppendLongs() throws Exception {
		BasicConfigurator.configure();
		org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog("com.db.fxpricing.Logger");

		for(long i = 0; i < WARMUP_COUNT; i++)
			log.info("value:" + i + " " + i + " " + i + " " + i + " " + i
					+ i + " " + i + " " + i + " " + i + " " + i);
		Thread.sleep(1000L);

		objectCounting.set(true);
		for(long i = 0; i < TEST_COUNT; i++)
			log.info("value:" + i + " " + i + " " + i + " " + i + " " + i
					+ i + " " + i + " " + i + " " + i + " " + i);

		Thread.sleep(500L);

		printState("log4j-longs");
	}

	@Test
	public void testLogbackAppendString() throws Exception {
		org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("com.db.fxpricing.Logger");

		for(long i = 0; i < WARMUP_COUNT; i++)
			log.info("value");

		Thread.sleep(1000L);

		objectCounting.set(true);
		for(long i = 0; i < TEST_COUNT; i++)
			log.info("value");

		Thread.sleep(500L);

		printState("logback-string");
	}

	@Test
	public void testLogbackAppendLong() throws Exception {
		org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("com.db.fxpricing.Logger");

		for(long i = 0; i < WARMUP_COUNT; i++)
			log.info("value:{}", i);

		Thread.sleep(1000L);

		objectCounting.set(true);
		for(long i = 0; i < TEST_COUNT; i++)
			log.info("value:{}", i);

		Thread.sleep(500L);

		printState("logback-long");
	}

	@Test
	public void testLogbackAppendLongs() throws Exception {
		org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("com.db.fxpricing.Logger");

		for(long i = 0; i < WARMUP_COUNT; i++)
			log.info("value:{} {} {} {} {} {} {} {} {} {} ", new long[]{i, i, i, i, i, i, i, i, i, i});

		Thread.sleep(1000L);

		objectCounting.set(true);
		for(long i = 0; i < TEST_COUNT; i++)
			log.info("value:{} {} {} {} {} {} {} {} {} {} ", new long[]{i, i, i, i, i, i, i, i, i, i});

		Thread.sleep(500L);

		printState("logback-longs");
	}

	@Test
	public void testCommon() throws Exception {
		// Thread.currentThread().getName
		{
			{
				final String name = Thread.currentThread().getName();
			}
			resetObjectCounting();
			objectCounting.set(true);
			detailedAllocation.set(true);
			{
				final String name = Thread.currentThread().getName();
			}
			objectCounting.set(false);
			detailedAllocation.set(false);
			printState("Thread.currentThread().getName");
		}
		// Integer.toString
		{
			{
				final String s = Integer.toString(12345);
			}
			resetObjectCounting();
			objectCounting.set(true);
			detailedAllocation.set(true);
			{
				final String s = Integer.toString(12345);
			}
			objectCounting.set(false);
			detailedAllocation.set(false);
			printState("Integer.toString");
		}

		// condition.await
		{
			final Lock lock = new ReentrantLock();
			final Condition condition = lock.newCondition();
			lock.lock();
			try {
				condition.await(1, TimeUnit.MILLISECONDS);
			} finally {
				lock.unlock();
			}
			resetObjectCounting();
			objectCounting.set(true);
			detailedAllocation.set(true);
			lock.lock();
			try {
				condition.await(1, TimeUnit.MILLISECONDS);
			} finally {
				lock.unlock();
			}
			objectCounting.set(false);
			detailedAllocation.set(false);
			printState("ReentrantLock");
		}
	}

	private void printState(final String name){
		final boolean b1 = objectCounting.getAndSet(false);
		final boolean b2 = detailedAllocation.getAndSet(false);

		System.out.println(name + " count:" + objectCount.get() + " size:" + objectSize.get());

		objectCounting.set(b1);
		detailedAllocation.set(b2);
	}

}
