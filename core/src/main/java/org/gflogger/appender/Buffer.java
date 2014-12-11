package org.gflogger.appender;

import java.nio.ByteBuffer;

import org.gflogger.Layout;
import org.gflogger.LogEntryItemImpl;

/**
 * @author vdolzhenko
 */
public interface Buffer {

	boolean isMultibyte();

	ByteBuffer getBuffer();

	void process(LogEntryItemImpl entry);

	void start();
	void stop();

	void setLayout(Layout layout);
	Layout getLayout();
}
