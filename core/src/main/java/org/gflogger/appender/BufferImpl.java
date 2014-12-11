package org.gflogger.appender;

import java.nio.ByteBuffer;

import org.gflogger.Appender;
import org.gflogger.Layout;
import org.gflogger.LogEntryItemImpl;
import org.gflogger.formatter.BufferFormatter;

import static org.gflogger.formatter.BufferFormatter.allocate;

/**
 * @author vdolzhenko
 */
public class BufferImpl implements Buffer {
	protected static final int DEFAULT_BUFFER_SIZE = 1 << 22; /* 4Mb */

	protected final ByteBuffer  byteBuffer;
	protected final Appender    appender;
	protected Layout            layout;

	public BufferImpl(final Appender appender) {
		this(DEFAULT_BUFFER_SIZE, appender);
	}

	public BufferImpl(final int bufferSize, final Appender appender) {
		this.byteBuffer = allocate( bufferSize );
		this.appender = appender;
	}

	@Override
	public void setLayout(Layout layout) {
		this.layout = layout;
	}

	@Override
	public Layout getLayout() {
		return layout;
	}

	@Override
	public ByteBuffer getBuffer() {
		return byteBuffer;
	}

	@Override
	public final boolean isMultibyte() {
		return false;
	}

	@Override
	public void process(LogEntryItemImpl entry) {
		final ByteBuffer buffer = entry.getBuffer();

		final int position0 = buffer.position();
		final int limit0 = buffer.limit();

		final int position = byteBuffer.position();
		final int limit = byteBuffer.limit();
		final int size = layout.size(entry);
		if (position + size >= limit){
			appender.flush();
			byteBuffer.clear();
		}

		buffer.flip();

		layout.format(byteBuffer, entry);

		buffer.limit(limit0).position(position0);
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
		appender.flush();
		BufferFormatter.purge(byteBuffer);
	}
}
