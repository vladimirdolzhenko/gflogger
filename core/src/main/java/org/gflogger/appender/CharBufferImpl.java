package org.gflogger.appender;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.gflogger.Appender;
import org.gflogger.Layout;
import org.gflogger.LogEntryItemImpl;
import org.gflogger.formatter.BufferFormatter;

import static org.gflogger.formatter.BufferFormatter.allocate;
import static org.gflogger.formatter.BufferFormatter.size;

/**
 * @author vdolzhenko
 */
public class CharBufferImpl implements Buffer {

	// inner thread buffer
	protected final CharBuffer  charBuffer;
	protected final ByteBuffer  byteBuffer;
	protected final Appender    appender;

	protected Layout            layout;
	protected String            codepage = "UTF-8";
	protected CharsetEncoder    encoder;
	protected int               maxBytesPerChar;

	public CharBufferImpl(final int bufferSize, final Appender appender) {
		final int size = size( bufferSize, true );
		this.byteBuffer = allocate( size );
		this.charBuffer = allocate( size ).asCharBuffer();
		this.appender = appender;
	}

	public void setCodepage(String codepage) {
		this.codepage = codepage;
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
		return true;
	}

	@Override
	public void process(LogEntryItemImpl entry) {
		final CharBuffer entryBuffer = entry.getCharBuffer();
		final int entryPosition = entryBuffer.position();
		final int entryLimit = entryBuffer.limit();

		final int position = charBuffer.position();
		final int limit = charBuffer.limit();
		final int size = layout.size( entry );

		if (position + size >= limit){
			appender.flush();
			charBuffer.clear();
		}

		entryBuffer.flip();

		layout.format( charBuffer, entry );

		entryBuffer.limit( entryLimit ).position( entryPosition );

		final int remaining = byteBuffer.remaining();
		final int sizeOfBuffer = maxBytesPerChar * charBuffer.position();

		// store buffer if there it could be no enough space for message
		if (remaining < sizeOfBuffer){
			appender.flush();
		}

		charBuffer.flip();
		encoder.encode( charBuffer, byteBuffer, true );
		// there is no reason to check encoding result
		// as it has been already checked that buffer has enough space
		charBuffer.clear();
	}

	@Override
	public void start() {
		encoder = Charset.forName(codepage).newEncoder();
		maxBytesPerChar = (int) Math.floor(encoder.maxBytesPerChar());
	}

	@Override
	public void stop() {
		appender.flush();
		BufferFormatter.purge(byteBuffer);
		BufferFormatter.purge(charBuffer);
	}
}
