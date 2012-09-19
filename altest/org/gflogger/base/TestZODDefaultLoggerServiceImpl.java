package org.gflogger.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.gflogger.*;
import org.gflogger.appender.FileAppenderFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import com.google.monitoring.runtime.instrumentation.Sampler;

/**
 * TestZODDefaultLoggerServiceImpl
 *
 * have to run with jvm option -javaagent:libs/allocation.jar
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestZODDefaultLoggerServiceImpl {
	static final AtomicBoolean detailedAllocation = new AtomicBoolean(false);

	static final AtomicBoolean objectCounting = new AtomicBoolean(false);
	static final AtomicInteger objectCount = new AtomicInteger();
	static final AtomicLong objectSize = new AtomicLong();

	private static final int WARMUP_COUNT = 50;
	private static final int TEST_COUNT = 1000;

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
			  if (!objectCounting.get()) return;

			  objectCount.incrementAndGet();
			  objectSize.addAndGet(size);

			  final StringBuilder builder = local.get();
			  builder.setLength(0);
			  if (count != -1) {
				  builder.append("an array of ").
					append(newObj.getClass().getComponentType().getName()).
					append("[").append(count).append("]");
			  } else {
				  if (newObj instanceof String){
					  builder.append("just allocated the string '").append(newObj).
						  append('\'');
					} else {
						builder.append("I just allocated the object ").append(newObj).
						append(" of type ").append(desc).append(" whose size is ").append(size);
					}
			  }
			  builder.append('[').append(threadName.get()).append(']');

			  if (!detailedAllocation.get()) return;
			  System.out.println(builder);
			  /*/
			  //*/
			}
		  });
	}

	@AfterClass
	public static void shutdown(){
	}

	@Before
	public void setUp(){
		resetObjectCounting();
	}

	@Test
	public void testGFLoggerAppendLong() throws Exception {

		resetObjectCounting();
		objectCounting.set(true);

		final int maxMessageSize = 20;
		final FileAppenderFactory factory = new FileAppenderFactory();
		factory.setFileName("./logs/gflogger.log");
		factory.setAppend(false);
		factory.setLayoutPattern("%m%n");
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = new LoggerServiceImpl(1 << 10, maxMessageSize,
			new GFLogger[]{new GFLoggerImpl("com.db", factory)},
			factory);

		GFLogFactory.init(loggerService);

		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		for(long i = 0; i < WARMUP_COUNT; i++)
			log.info().append("warmup:").append(i).commit();
		Thread.sleep(1000L);

		assertTrue("have to run with jvm option -javaagent:libs/allocation.jar",
				objectCount.get() > 0);
		System.out.println(objectCount.get());

		resetObjectCounting();
		objectCounting.set(true);
		//detailedAllocation.set(true);

		for(long v = 0; v < TEST_COUNT; v++)
			log.info().append("value:").append(v).commit();

		for(long v = 0; v < TEST_COUNT; v++)
			log.info("value: %s").withLast(v);

		Thread.sleep(500L);

		objectCounting.set(false);

		assertEquals(0, objectCount.get());

		printState("gflogger");

		GFLogFactory.stop();
	}


	private void printState(final String name){
		System.out.println(name + " count:" + objectCount.get() + " size:" + objectSize.get());
	}

}
