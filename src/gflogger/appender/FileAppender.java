package gflogger.appender;

import gflogger.LogEntryItem;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;


public class FileAppender extends AbstractAsyncAppender implements Runnable {

    private final ByteBuffer buffer;

    private String fileName;
    private String codepage = "UTF-8";

    private CharsetEncoder encoder;
    private FileChannel channel;

    private boolean append = true;

    private int maxBytesPerChar;

    public FileAppender() {
        // 4M
        buffer = ByteBuffer.allocateDirect(1 << 22);
        autoFlush = false;
    }

    public synchronized void setCodepage(final String codepage) {
        this.codepage = codepage;
    }

    public synchronized void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public void setAppend(final boolean append) {
        this.append = append;
    }

    @Override
    protected void processCharBuffer() {
        final int remaining = buffer.remaining();
        final int sizeOfBuffer = maxBytesPerChar * charBuffer.position();
        
        // store buffer if there it could be no enough space for message
        if (remaining < sizeOfBuffer){
            store("remaining < sizeOfBuffer");
        }

        CoderResult result;
        charBuffer.flip();
        do{
            result = encoder.encode(charBuffer, buffer, true);
            //*/
            if (result.isOverflow()){
                store("result.isOverflow()");
            }
            /*/
            store("force");
            //*/
        } while(result.isOverflow());
        charBuffer.clear();
    }

    @Override
    protected void flushCharBuffer() {
        store("flushCharBuffer");
    }

    @Override
    protected void workerIsAboutFinish() {
        store("workerIsAboutFinish");
        try {
            channel.close();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void start(final LogEntryItem entryItem) {
        try {
            encoder = Charset.forName(codepage).newEncoder();
            final FileOutputStream fout = new FileOutputStream(fileName, append);
            channel = fout.getChannel();
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        maxBytesPerChar = (int) Math.floor(encoder.maxBytesPerChar());

        super.start(entryItem);
    }

    protected boolean store(final String cause) {
        if (buffer.position() == 0) return false;
        buffer.flip();
        try {
            final int limit = buffer.limit();
            final long start = System.nanoTime();
            channel.write(buffer);
            final long end = System.nanoTime();

            //LogLog.debug(cause + ":" + limit + " bytes stored in " + ((end - start) / 1000 / 1e3) + " ms");
            //System.out.println(cause + ":" + limit + " bytes stored in " + ((end - start) / 1000 / 1e3) + " ms");
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            buffer.clear();
        }
        return true;
    }

    @Override
    protected String name() {
        return "file";
    }
}
