package org.gflogger.formatting;

import org.gflogger.FormattingStrategy;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestSlf4jFormattingStrategy {
	private final FormattingStrategy strategy = new Slf4jFormattingStrategy();

	@Test
	public void testIsPlaceholder() throws Exception {
		assertTrue(strategy.isPlaceholder("{}", 0));
		assertFalse(strategy.isPlaceholder("{", 0));
		assertFalse(strategy.isPlaceholder(" {}", 0));
		assertFalse(strategy.isPlaceholder(" {} ", 3));
		assertFalse(strategy.isPlaceholder("{}", 1));
		assertFalse("not a string placeholder", strategy.isPlaceholder("%s", 0));
	}

	@Test
	public void testIsEscape() throws Exception {
		assertTrue(strategy.isEscape("\\{", 0));
		assertFalse(strategy.isEscape("\\", 0));
		assertFalse(strategy.isEscape("{", 0));
	}
}
