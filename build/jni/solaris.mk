#############################################################################
# Copyright (C) 2009 koiroha.org All Right Reserved
#############################################################################
# This module, contains source code, binary and documentation, is in the
# BSD License, and comes with NO WARRANTY.
#
#                                        takami torao <torao@mars.dti.ne.jp>
#                                                     http://www.koiroha.org
# $Id: solaris.mk,v 1.1 2009/05/16 19:28:57 torao Exp $
# 
# GNU gmake and g++
# 

# JDK Home directory, ${JAVA_HOME}/include or else.
JAVA_HOME=/usr/jdk/jdk1.5.0_18

# 
PLATFORM=solaris

INC = \
	-I$(JAVA_HOME)/include/ \
	-I$(JAVA_HOME)/include/$(PLATFORM) \
	-Isrc
LIB = -L$(JAVA_HOME)/jre/lib/i386
SRC = $(wildcard src/*.cpp)
OBJ = $(patsubst src/%.cpp,dest/%.o,$(SRC))
DST = ../../dest/lib/$(PLATFORM)
DEF = -D __DUMP -D _DEBUG

all: $(DST)/liblu04j.so

dest/%.o: src/%.cpp
	g++ -Wall -c $< -o $@ $(INC) $(DEF)

dest/*.o: $(wildcard src/*.h) $(wildcard src/lu0/*.h) $(wildcard src/ou1/*.h)

clean:
	rm dest/*.o

#############################################################################
# libusb 0.1
#############################################################################
LIBUSB01SRC = $(wildcard src/lu0/*.cpp)
LIBUSB01OBJ = $(patsubst src/lu0/%.cpp,dest/%.o,$(LIBUSB01SRC))

$(DST)/liblu04j.so: $(OBJ) $(LIBUSB01OBJ)
	g++ -G -o $@ $(OBJ) $(LIBUSB01OBJ) $(LIB) -lusb -ljvm

dest/%.o: src/lu0/%.cpp
	g++ -Wall -c $< -o $@ $(INC) $(DEF)

#############################################################################
# OpenUSB 1.0
#############################################################################
OPENUSB10SRC = $(wildcard src/ou1/*.cpp)
OPENUSB10OBJ = $(patsubst src/ou1/%.cpp,dest/%.o,$(OPENUSB10SRC))

$(DST)/libou14j.so: $(OBJ) $(OPENUSB10OBJ)
	g++ -shared -o $@ $(OBJ) $(OPENUSB10OBJ) $(LIB) -lopenusb -ljvm

dest/%.o: src/ou1/%.cpp
	g++ -Wall -c $< -o $@ $(INC) $(DEF)
