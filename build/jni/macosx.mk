#############################################################################
# Copyright (C) 2009 koiroha.org All Right Reserved
#############################################################################
# This module, contains source code, binary and documentation, is in the
# BSD License, and comes with NO WARRANTY.
#
#                                        takami torao <torao@mars.dti.ne.jp>
#                                                     http://www.koiroha.org
# $Id: macosx.mk,v 1.2 2009/05/13 08:45:39 torao Exp $
# 
# If you want to get usb4j library for 64bit Java VM, you must build 64bit
# libusb library and compile usb4j with -m64 option.
# 
JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.5
INCLUDE=-I$(JAVA_HOME)/Headers -Isrc
SRC=$(wildcard src/*.cpp)
OBJ=$(patsubst src/%.cpp,dest/%.o,$(SRC))
DST=../../dest/lib/macosx

OPT = -m32 -dynamiclib -D __DUMP -D _DEBUG

all: $(DST)/liblu04j.jnilib

dest/%.o: src/%.cpp
	g++ -Wall -c $< -o $@ $(INCLUDE) $(OPT)

dest/*.o: $(wildcard src/*.h)

clean:
	rm dest/*.o

#############################################################################
# libusb 0.1
#############################################################################
LU0SRC = $(wildcard src/lu0/*.cpp)
LU0OBJ = $(patsubst src/lu0/%.cpp,dest/%.o,$(LU0SRC))

$(DST)/liblu04j.jnilib: $(OBJ) $(LU0OBJ)
	g++ $(OPT) -o $@ $(OBJ) $(LU0OBJ) -lusb -framework JavaVM

dest/%.o: src/lu0/%.cpp
	g++ -Wall -c $< -o $@ $(INCLUDE) $(OPT)
