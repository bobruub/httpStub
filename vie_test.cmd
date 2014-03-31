@echo off

set PROGNAME=vie_test
set HOMELOC=c:\temp\vie
set JAVALOC="%HOMELOC%\jre6\bin"

cd /d %HOMELOC%\command

set CLASSPATH=.;C:\TEMP\java_test\httpStub\vie.jar
set CLASSPATH=%CLASSPATH%;%HOMELOC%\lib\lava3-printf.jar
set CLASSPATH=%CLASSPATH%;%HOMELOC%\lib\lava3-core.jar

%JAVALOC%\java -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -XX:+PerfBypassFileSystemCheck -Xmx512m com.httpStub.core.httpStub c:\temp\vie\xml\vie_test.xml

