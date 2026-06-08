@ECHO OFF
SETLOCAL
set MAVEN_PROJECTBASEDIR=%~dp0
REM Remove trailing backslash to avoid JVM parsing issues with -D options.
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%
set MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties

REM Resolve M2_HOME / JAVA
if not "%JAVA_HOME%"=="" goto checkJavaHome
set JAVA_EXEC=java
goto run

:checkJavaHome
set JAVA_EXEC=%JAVA_HOME%\bin\java

:run
set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar

if exist "%WRAPPER_JAR%" goto wrapperJarReady

REM Download wrapper jar if missing
for /f "usebackq tokens=2 delims==" %%A in (`findstr /i "wrapperUrl" "%MAVEN_WRAPPER_PROPERTIES%"`) do set WRAPPER_URL=%%A
if "%WRAPPER_URL%"=="" set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "try { Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%' -UseBasicParsing } catch { exit 1 }"
if not exist "%WRAPPER_JAR%" (
  echo Failed to download maven-wrapper.jar
  exit /b 1
)

:wrapperJarReady
REM Execute wrapper.
REM NOTE: maven-wrapper.jar in some environments lacks a Main-Class in MANIFEST,
REM so we run MavenWrapperMain via classpath instead of `java -jar`.
"%JAVA_EXEC%" -cp "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR% org.apache.maven.wrapper.MavenWrapperMain %*

ENDLOCAL

