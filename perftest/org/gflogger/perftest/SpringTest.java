package org.gflogger.perftest;


import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * SpringTest
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class SpringTest extends AbstractSimpleTest {

	public static void main(String[] args) throws Throwable {
		final SpringTest springTest = new SpringTest();
		final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:app-context.xml");
		ctx.start();

		springTest.test();

		ctx.stop();
	}
}
