package gflogger.base;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.*;
import gflogger.LogFactory;
import gflogger.LogLevel;
import gflogger.Logger;
import gflogger.LoggerService;
import gflogger.appender.ConsoleAppenderFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import com.google.monitoring.runtime.instrumentation.Sampler;

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
              if (!objectCounting.get()) return;
              
              objectCount.incrementAndGet();
              objectSize.addAndGet(size);
              
              final StringBuilder builder = local.get();
              builder.setLength(0);
              if (count != -1) {
                  builder.append("It's an array of ").
                    append(newObj.getClass().getComponentType().getName()).
                    append("[").append(count).append("]");
              } else {
                  if (newObj instanceof String){
                      builder.append("I just allocated the string '").append(newObj).
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
		final int maxMessageSize = 20;
	    final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
	    factory.setLayoutPattern("%m");
	    // 1k
	    final StringBuffer buffer = new StringBuffer(1<<20);
		factory.setOutputStream(buffer);
	    factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService = new DefaultLoggerServiceImpl(4, maxMessageSize, factory);
		
		LogFactory.init(singletonMap("com.db", loggerService));
		
		final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");

		for(long i = 0; i < WARMUP_COUNT; i++)
			logger.info().append("value:").append(i).commit();
		Thread.sleep(1000L);
		
		objectCounting.set(true);
		
		for(long i = 0; i < TEST_COUNT; i++)
			logger.info().append("value:").append(i).commit();
		
		Thread.sleep(500L);
		
		objectCounting.set(false);
		
		LogFactory.stop();
		
		final String string = buffer.toString();
		
		final StringBuffer buffer2 = new StringBuffer(1<<20);
		
		for(long i = 0; i < WARMUP_COUNT; i++)
			buffer2.append("value:" + i);
		for(long i = 0; i < TEST_COUNT; i++)
			buffer2.append("value:" + i);
		
		assertEquals(buffer2.toString(), string);
		
		assertEquals(0, objectCount.get());
		printState("gflogger");
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
		System.out.println(name + " count:" + objectCount.get() + " size:" + objectSize.get());
	}
	
}
