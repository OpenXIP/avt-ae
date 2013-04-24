@ECHO %1
@ECHO %2
@ECHO %3
@ECHO %4
for /f %%i in ("%0") do set curpath=%%~dpi
cd /d %curpath%
java -Xmx1024m -cp ..\lib\pixelmed.jar;..\lib\XIPApp.jar;..\lib\DicomUtil.jar;..\lib\mime-util.jar; -jar ..\build\lib\AE.jar %1 %2 %3 %4
PAUSE
REM EXIT