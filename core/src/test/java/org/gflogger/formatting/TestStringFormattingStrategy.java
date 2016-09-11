package org.gflogger.formatting;

import org.gflogger.LoggingStrategy;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestStringFormattingStrategy {
	private final LoggingStrategy strategy = new StringLoggingStrategy();

	@Test
	public void testIsPlaceholder() throws Exception {
		assertTrue(strategy.isPlaceholder("%s", 0));
		assertFalse(strategy.isPlaceholder("%", 0));
		assertFalse(strategy.isPlaceholder(" %s", 0));
		assertFalse(strategy.isPlaceholder(" %s ", 3));
		assertFalse(strategy.isPlaceholder("%s", 1));
		assertFalse("not a sl4j placeholder", strategy.isPlaceholder("{}", 0));
	}

	@Test
	public void testIsEscape() throws Exception {
		assertTrue(strategy.isEscape("%%", 0));
		assertFalse(strategy.isEscape("%%", 1));
		assertFalse(strategy.isEscape("%", 0));
	}
}
