package org.gflogger.helpers;

import org.gflogger.LogEntryItem;
import org.gflogger.LogEntryItemImpl;
import org.gflogger.formatting.StringFormattingStrategy;
import org.junit.Test;

import java.util.Locale;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;

/**
 * @author vdolzhenko
 */
public class TestPatternParser {

	@Test
	public void testMessageSize() throws Exception {
		PatternParser parser = new PatternParser("%m", Locale.getDefault(), TimeZone.getDefault());

		final PatternConverter converter = parser.parse();
		final LogEntryItem item = new LogEntryItemImpl(1 << 10 , new StringFormattingStrategy());
		assertEquals(0, converter.size(item));
		item.getBuffer().put("Hello world!".getBytes());
		assertEquals(12, converter.size(item));
	}
}
