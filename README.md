# What is it ?

The **g**arbage-**f**ree **logger** IS **NOT** a general purpose logger as log4j, logback or similar. 

 ![gflogger logo](https://lh3.googleusercontent.com/-NDPcHqKxmlU/T3gIULn-x-I/AAAAAAAAJQ8/utfNtvp-Z3c/s300/recycling.png)

The goal is to create *ad-hoc* logger for **low latency** (*latency critical*) applications (to be precise for latency critical execution path) which will affect application explicit and implicit (though gc pauses) as less as it possible. It means that it will **not** be a **general purpose** logger. As well it means that *this* logger should not be a log4j or logback (or whatever you like) killer - that's ok to combine them using e.g. log4j for the initial application phase and *this* logger for *latency critical* execution path.

There are some known **by design** limitation.

*gflogger* **DOES NOT** have :
* garbage overhead on the fly *except initialization and file rolling phases*
* unlimited message length
* ability to track changes of :
    * thread name
    * time zone and locale
* different types of appenders as jms appender, email appender and so on

*gflogger* has :
* **zero object delivery** property on the *normal* fly
* appenders: file, daily rolling file, console (stream)
* compatible with **log4j** message pattern format
** but *MDC*, *NDC*
* compatible with **log4j** log levels: *trace*, *debug*, *info*, *warn*, *error*, *fatal*
* category heirarchy support 
* configurators: xml, java api

Read further a [garbage free logger](http://dolzhenko.blogspot.com/2011/11/garbage-free-logger.html) post in blog.

# How it use
[QuickStart](QuickStart.md)

## gflogger.xml
```xml
<configuration 
    xmlns="http://bitbucket.org/vladimir.dolzhenko/gflogger"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://bitbucket.org/vladimir.dolzhenko/gflogger 
        https://raw.githubusercontent.com/vladimirdolzhenko/gflogger/master/core/src/main/resources/gflogger.xsd">

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

## load configuration
```java
// uses system property gflogger.configuration
// or default gflogger.xml file
 org.gflogger.config.xml.XmlLogFactoryConfigurator.configure();
```

## usage
```java

private static final org.gflogger.GFLog log = 
    org.gflogger.GFLogFactory.getLog(MyClass.class);

// somewhere in method, the modern way:
log.info( "value of %s is %s" ).with( name ).with( value );

// the same with the orthodox approach
log.info().
      append( "value of " ).append( name ).
      append( " is " ).append( value ).
    commit();
```

# How it works

![Ring based design](https://lh3.googleusercontent.com/-8Vs0IHF-0PY/T4prKDmA5PI/AAAAAAAAJYg/Duu7cA7ORio/s800/ring1.png)
