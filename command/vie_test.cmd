@echo off

set PROGNAME=vie_test
set HOMELOC=c:\temp\java_test\httpStub
set JAVALOC="C:\TEMP\vif\jre7\bin"

cd /d %HOMELOC%\command

set CLASSPATH=.;%HOMELOC%\dist\vie.jar

%JAVALOC%\java -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -XX:+PerfBypassFileSystemCheck -Xmx512m com.httpStub.core.httpStub

