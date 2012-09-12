package perftest;

import gflogger.LogFactory;
import gflogger.Logger;
import gflogger.config.xml.XmlLogFactoryConfigurator;


/**
 * XmlConfigurationTest
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class XmlConfigurationTest {

	public static void main(String[] args) throws Exception {
		XmlLogFactoryConfigurator.configure();

	    final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");

	    logger.info().
	    	append(new SomeObject(5)).commit();

	    logger.info().append("test1").commit();

	    logger.debug().append("testD").commit();

	    logger.error().append("testE").commit();

	    logger.info().append("test2").commit();

	    logger.info().append("test3").commit();

	    final Logger logger2 = LogFactory.getLog("org.spring");

	    logger2.info().append("org.spring.info").commit();

	    logger2.debug().append("org.spring.debug").commit();

	    logger2.error().append("org.spring.error").commit();

	    Thread.sleep(1000L);
    }
}
