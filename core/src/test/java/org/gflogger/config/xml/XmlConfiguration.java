package org.gflogger.config.xml;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.gflogger.GFLog;
import org.gflogger.GFLogFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;

public class XmlConfiguration {
	private final StringBuffer buffer = new StringBuffer();

	@Before
	public void setUp(){
		buffer.setLength(0);
		System.setProperty("TZ", "GMT");
		System.setProperty("gflogger.configuration", "gflogger.xml");
		StreamAppenderFactory.outputStream = buffer;
	}

	@Test
	public void testDefault() throws Throwable {
		try {
			XmlLogFactoryConfigurator.configure();
			fail();
		} catch (Throwable e){

		}

		System.setProperty("gflogger.configuration", "gflogger1.xml");

		XmlLogFactoryConfigurator.configure();

		test();
	}

	@Test
	public void testIS() throws Throwable {
		final InputStream is = XmlConfiguration.class.getResourceAsStream("/gflogger1.xml");
		assertNotNull(is);
		XmlLogFactoryConfigurator.configure(is);

		test();
	}

	@Test
	public void testAbsolutePath() throws Throwable {
		final URL resource = XmlConfiguration.class.getResource("/gflogger1.xml");
		final String fileName = resource.getFile();

		final File file = new File(fileName);
		assertTrue(file.exists());
		assertTrue(file.isAbsolute());

		XmlLogFactoryConfigurator.configure(fileName);

		test();
	}

	@Test
	public void testClasspath() throws Throwable {
		XmlLogFactoryConfigurator.configure("classpath:/gflogger1.xml");

		test();
	}

	@Test
	public void testFile() throws Throwable {
		XmlLogFactoryConfigurator.configure("file:/gflogger1.xml");

		test();
	}

	private void test() {
		final GFLog log = GFLogFactory.getLog(XmlConfiguration.class);
		log.error("error");

		GFLogFactory.stop();

		final String output = buffer.toString();
		//Sep 24 11:18:22,140 GMT ERROR - error [xml.XmlConfiguration] [Test worker]
		assertTrue(
				"Output='"+output+"'",
				output.matches( "\\w+ \\d+ \\d{2}:\\d{2}:\\d{2},\\d{3} GMT ERROR - error \\[xml.XmlConfiguration\\] \\[[\\w\\- ]+\\]"
						                + System.getProperty( "line.separator" ) )
		);
	}

}
