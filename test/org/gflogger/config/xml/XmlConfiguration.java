package org.gflogger.config.xml;

import org.gflogger.GFLogFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XmlConfiguration {

	@Test
	public void test() throws Throwable {
		final StringBuffer buffer = new StringBuffer();

		StreamAppenderFactory.outputStream = buffer;
		XmlLogFactoryConfigurator.configure(XmlConfiguration.class.getResourceAsStream("gflogger1.xml"));

		GFLogFactory.getLog(XmlConfiguration.class).error("error");

		GFLogFactory.stop();

		assertEquals("", buffer.toString());
	}

}
