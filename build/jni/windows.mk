#############################################################################
# Copyright (C) 2009 koiroha.org All Right Reserved
#############################################################################
# This module, contains source code, binary and documentation, is in the
# BSD License, and comes with NO WARRANTY.
#
#                                        takami torao <torao@mars.dti.ne.jp>
#                                                     http://www.koiroha.org
# $Id: windows.mk,v 1.1 2009/05/13 08:45:40 torao Exp $
# 
# This makefile is written for MSVS nmake. Please refer make.bat to modifiy
# environment variables. Target compilation platform is follows:
# 
#   o Microsoft Windows XP (SP2)
#   o Microsoft Visual C++ 9.0 Express
#   o Windows Platform SDK
# 
JAVA_HOME=C:\Program Files\Java\jdk1.6.0_13
MSVC_HOME=C:\Program Files\Microsoft Visual Studio 9.0\VC
PSDK_HOME=C:\Program Files\Microsoft SDKs\Windows\v6.0A
LUSB_HOME=win32\libusb-win32-0.1

INC = \
	"/I$(JAVA_HOME)\include" \
	"/I$(JAVA_HOME)\include\win32" \
	"/Isrc" \
	"/I..\share" \
	"/I$(MSVC_HOME)\include" \
	"/I$(LUSB_HOME)\include"
LIB = \
	"/LIBPATH:$(LUSB_HOME)\lib\msvc" \
	"/LIBPATH:$(MSVC_HOME)\lib" \
	"/LIBPATH:$(PSDK_HOME)\Lib" \
	"/LIBPATH:$(JAVA_HOME)\lib"
DEF = /D "_WIN32" /D "__DUMP" /D "_DEBUG"
CC  = "$(MSVC_HOME)\bin\cl.exe"
LNK = "$(MSVC_HOME)\bin\link.exe"
OPT = /MT /W4 /RTCc /RTC1 /RTCsu "/Fodest\\" /EHsc /nologo
DST = ..\..\dest\lib\windows

all: $(DST)\lu04j.dll

$(DST)\lu04j.dll: dest\usb4java.obj dest\libusb0_jni_bridge.obj
	$(LNK) /DLL $** libusb.lib jvm.lib $(LIB) /OUT:$@ /NOLOGO

dest\usb4java.obj: src\usb4java.cpp
	$(CC) $(OPT) /c $(DEF) $(INC) $**

dest\libusb0_jni_bridge.obj: src\lu0\libusb0_jni_bridge.cpp
	$(CC) $(OPT) /c $(DEF) $(INC) $**

clean:
	del dest\\* /Q
