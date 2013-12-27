package org.gflogger.perftest;

import org.gflogger.GFLog;
import org.gflogger.GFLogFactory;

/**
 * AbstractSimpleTest
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public abstract class AbstractSimpleTest {

	public void test() throws Throwable{
		final GFLog log = GFLogFactory.getLog("com.db.fxpricing.Logger");

		log.info().append("test1").commit();

		log.debug().append("testD").commit();

		log.error().append("testE").commit();

		log.info().append("test2").commit();

		log.info().append("test3").commit();

		final GFLog log2 = GFLogFactory.getLog("org.spring");

		log2.info().append("org.spring.info").commit();

		log2.debug().append("org.spring.debug").commit();

		log2.error().append("org.spring.error").commit();

		Thread.sleep(2000);
	}
}
