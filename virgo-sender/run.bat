@echo off

if "%1"=="sender" goto sender
if "%1"=="assign" goto assign
goto end
:sender
echo sender
java -cp -Djsse.enableSNIExtension=false "target/virgo-sender-1.0.0-SNAPSHOT.jar;target/dependency/*" worker.assign.SenderClient
goto end
:assign
echo assign
java -cp "target/virgo-sender-1.0.0-SNAPSHOT.jar;target/dependency/*" worker.assign.ZkManager
goto end
:end
 