package org.gflogger.appender;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;

import static org.junit.Assert.assertEquals;

/**
 * @author vladimir.dolzhenko@gmail.com
 **/
public class TestFileAppender extends AbstractFlushingAppenderHelper<FileAppender> {

	protected File tempFile;
	protected boolean multibyte = false;

	@Override
	protected FileAppender createAppender() throws Exception {
		FileAppenderFactory appenderFactory = new FileAppenderFactory();
		tempFile = File.createTempFile( "temp-file-name", ".tmp" );
		appenderFactory.setFileName( tempFile.getAbsolutePath() );
		appenderFactory.setLayoutPattern( LAYOUT_PATTERN );
		appenderFactory.setBufferSize( BUFFER_SIZE );
		appenderFactory.setMultibyte( multibyte );
		final FileAppender appender = appenderFactory.createAppender( null );
		appender.start();

		return appender;
	}

	@After
	public void tearDown() {
		appender.stop();
		tempFile.delete();
	}


	protected void assertOutput( String expected ) throws Exception {
		final byte[] bytes = Files.readAllBytes( Paths.get( tempFile.getAbsolutePath() ) );
		assertEquals( expected, new String( bytes ) );
	}
}
