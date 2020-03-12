echo off
REM Goto analyse directory
cd /d %~dp0
REM Get analyse params
for /F "tokens=1,2,3,4" %%j in (analyse.ini) do (set doUpdate=%%j)
for /F "tokens=1,2,3,4" %%j in (analyse.ini) do (set minMem=%%k)
for /F "tokens=1,2,3,4" %%j in (analyse.ini) do (set maxMem=%%l)
for /F "tokens=1,2,3,4" %%j in (analyse.ini) do (set withLog=%%m)
IF "%doUpdate%"=="false" GOTO :runAnalyse
java -jar applyAnalyseUpdate.jar
:runAnalyse
REM Run Analyse
IF "%withLog%"=="true" GOTO :runAnalyseWithLog
java -Xms%minMem%m -Xmx%maxMem%m -jar analyse.jar
GOTO :end
:runAnalyseWithLog
java -Xms%minMem%m -Xmx%maxMem%m -jar analyse.jar -log
:end