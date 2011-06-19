@echo off
rem set "MSVC_HOME=C:\Program Files\Microsoft Visual Studio .NET 2003\Vc7"
set "MSVC_HOME=C:\Program Files\Microsoft Visual Studio 9.0\VC"
if "%MSVCVAR%"=="" call "%MSVC_HOME%\bin\vcvars32.bat"
if "%MSVCVAR%"=="" set MSVCVAR=1
nmake %*
