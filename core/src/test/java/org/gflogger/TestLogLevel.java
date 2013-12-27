package org.gflogger;

import org.junit.Test;

import static org.gflogger.LogLevel.DEBUG;
import static org.gflogger.LogLevel.INFO;
import static org.junit.Assert.assertTrue;

public class TestLogLevel {

	@Test
	public void testName() throws Exception {
		assertTrue(DEBUG.lessThan(INFO));
		assertTrue(INFO.greaterThan(DEBUG));
	}
}
