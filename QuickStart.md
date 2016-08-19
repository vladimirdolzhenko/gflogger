How to  [configure](Configuration.md) _gflogger_

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

# Usage
```java

private static final GFLogger log = 
    GFLogFactory.getLog(MyClass.class);

// in method
log.info("value of %s is %s").with(name).withLast(value);
```

## classic 
```java

private static final GFLog log = 
    GFLogFactory.getLog(MyClass.class);

// in method
log.info().append("value:").append(value).commit();
```
