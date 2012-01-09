package perftest;

import gflogger.LogFactory;
import gflogger.Logger;

import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * SpringTest
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class SpringTest {

	public static void main(String[] args) {
	    final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:app-context.xml");
	    ctx.start();
	    
	    final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");
	    
	    logger.info().append("test1").commit();
	    
	    logger.info().append("test2").commit();
	    
	    logger.info().append("test3").commit();
	    
	    ctx.stop();
    }
}
