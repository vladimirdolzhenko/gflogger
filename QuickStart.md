# Configuration

```java
// uses system property **gflogger.configuration** 
// or default gflogger.xml file
XmlLogFactoryConfigurator.configure();
```

### system properties

* **gflogger.configuration** - gflogger xml configuration file
    * default value: **/gflogger.xml**
* **gflogger.buffer.size** - buffer size (in bytes) commonly used in appenders
    * default value: **1M**
* **gflogger.multibyte** - use multibyte code pages
    * default value: **false**
* **gflogger.loglevel**
    * default value: **ERROR**
* **gflogger.pattern**
    * default value: **%m%n**
* **gflogger.immediateFlush**
    * default value: **false**
* **gflogger.bufferedIOThreshold**
* **gflogger.awaitTimeout**
* **gflogger.codepage**
    * default value: **UTF-8**
* **gflogger.append**
    * default value: **true**
* **gflogger.rolling.pattern** rolling pattern, see _DailyRollingFileAppenderFactory_
    *  default value: **'.'yyyy-MM-dd**
* **gflogger.timeZoneId** timezone to use for datetime formatters 
    * default value: _none_
* **gflogger.language** language/locale to use for datetime formatters 
    * default value: _none_
* **gflogger.internalQuietMode** turn internal logging (prints to stdout/stderr) in quiet mode
    * default value: _false_
* **gflogger.internalLogLevel** internal log level
    * default value: _INFO_

# gflogger.xml 
```xml

<configuration 
    xmlns="http://bitbucket.org/vladimir.dolzhenko/gflogger"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://bitbucket.org/vladimir.dolzhenko/gflogger 
        https://bitbucket.org/vladimir.dolzhenko/gflogger/raw/0.1.0/src/gflogger.xsd">

    <appender name="fileAppender"
        class="org.gflogger.appender.DailyRollingFileAppenderFactory"
        fileName="${logs.root}/${instanceName}/${instanceName}.log"
        datePattern="'.'yyyy-MM-dd-HH">
            <layout class="org.gflogger.PatternLayout"
                pattern="%d{MMM d HH:mm:ss,SSS zzz} %p - %m [%c{2}] [%t]%n" timeZoneId="GMT"/>
    </appender>
    
    <logger name="com.db" logLevel="INFO">
        <appender-ref ref="fileAppender"/> 
    </logger>
    
    <root logLevel="WARN">
        <appender-ref ref="fileAppender"/> 
    </root>

    <service count="1024" maxMessageSize="4096">
        <object-formatter 
            class="org.gflogger.perftest.SomeObject"
            formatter="org.gflogger.perftest.SomeObjectFormatter"/>
    </service>

</configuration>
```

```java
// uses system property **gflogger.configuration** 
// or default gflogger.xml file
XmlLogFactoryConfigurator.configure();
```
