package org.gflogger.slf4j

import org.gflogger.config.xml.StreamAppenderFactory
import org.junit.BeforeClass
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class TestSlf4jLoggerImpl {
    private static Logger logger
    private static final StringBuffer buffer = new StringBuffer()

    @BeforeClass
    static public void setUpClass() {
        buffer.setLength(0)
        System.setProperty("TZ", "GMT")
        System.setProperty("gflogger.configuration", "gflogger1.xml")
        StreamAppenderFactory.outputStream = buffer
        logger = LoggerFactory.getLogger(TestSlf4jLoggerImpl.class)
    }

    @Test
    public void testTrace() throws InterruptedException {
        test("trace")
    }

    @Test
    public void testDebug() throws InterruptedException {
        test("debug")
    }

    @Test
    public void testInfo() throws InterruptedException {
        test("info")
    }

    @Test
    public void testWarn() throws InterruptedException {
        test("warn")
    }

    @Test
    public void testError() throws InterruptedException {
        test("error")
    }

    private static test(String method) {
        testNoParameters(method)
        testNoParametersWithTrailingThrowable(method)
        testOneParameter(method)
        testOneParameterWithTrailingThrowable(method)
        testThrowableLoggedAsLastPlaceholder(method)
        testThrowable(method)
        testTwoParameters(method)
        testManyParameters(method)
        testManyParametersWithTrailingThrowable(method)
        testNoExceptionOnWrongPattern(method)
    }

    public static void testNoParameters(String method) throws InterruptedException {
        buffer.setLength(0)
        logger."$method"("message")
        Thread.sleep(100)
        assert buffer.toString().equals("message")
    }

    public static void testNoParametersWithTrailingThrowable(String method) throws InterruptedException {
        buffer.setLength(0)
        logger."$method"("message", new RuntimeException("Exception message"))
        Thread.sleep(100)
        assert buffer.toString().startsWith("messagejava.lang.RuntimeException: Exception message")
        assert buffer.toString().contains("TestSlf4jLoggerImpl")

    }

    public static void testOneParameter(String method) throws InterruptedException {
        buffer.setLength(0)
        logger."$method"("message{}", "message")
        Thread.sleep(100)
        assert buffer.toString().equals("messagemessage")
    }

    public static void testOneParameterWithTrailingThrowable(String method) throws InterruptedException {
        buffer.setLength(0)
        logger."$method"("message{}", "message", new RuntimeException("Exception message"))
        Thread.sleep(100)
        assert buffer.toString().startsWith("messagemessagejava.lang.RuntimeException: Exception message")
        assert buffer.toString().contains("TestSlf4jLoggerImpl")
    }

    public static void testThrowableLoggedAsLastPlaceholder(String method) throws InterruptedException {
        buffer.setLength(0)
        logger."$method"("message{}{}log", "message", new RuntimeException("Exception message"))
        Thread.sleep(100)
        assert buffer.toString().equals("messagemessagejava.lang.RuntimeException: Exception messagelog")
    }

    public static void testNoExceptionOnWrongPattern(String method) throws InterruptedException {
        buffer.setLength(0)
        logger."$method"("message{}", "message", "message")
        Thread.sleep(100)
        assert true
        logger."$method"("flush uncommited message")
    }

    public static void testThrowable(String method) throws InterruptedException {
        buffer.setLength(0)
        logger."$method"("message", new RuntimeException("Exception message"))
        Thread.sleep(100)
        assert buffer.toString().startsWith("messagejava.lang.RuntimeException: Exception message")
        assert buffer.toString().contains("TestSlf4jLoggerImpl")
    }

    public static void testTwoParameters(String method) throws InterruptedException {
        buffer.setLength(0)
        logger."$method"("message{}{}", "message", "message")
        Thread.sleep(100)
        assert buffer.toString().equals("messagemessagemessage")
    }

    public static void testManyParameters(String method) throws InterruptedException {
        buffer.setLength(0)
        logger."$method"("message{}{}{}", "message", "message", "message")
        Thread.sleep(100)
        assert buffer.toString().equals("messagemessagemessagemessage")
    }

    public static void testManyParametersWithTrailingThrowable(String method) throws InterruptedException {
        buffer.setLength(0)
        logger."$method"("message{}{}{}", "message", "message", "message", new RuntimeException("Exception message"))
        Thread.sleep(100)
        assert buffer.toString().startsWith("messagemessagemessagemessagejava.lang.RuntimeException: Exception message")
        assert buffer.toString().contains("TestSlf4jLoggerImpl")
    }
}
