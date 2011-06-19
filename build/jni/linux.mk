#############################################################################
# Copyright (C) 2009 koiroha.org All Right Reserved
#############################################################################
# This module, contains source code, binary and documentation, is in the
# BSD License, and comes with NO WARRANTY.
#
#                                        takami torao <torao@mars.dti.ne.jp>
#                                                     http://www.koiroha.org
# $Id: linux.mk,v 1.2 2009/05/13 08:45:40 torao Exp $
# 
INC = \
	-I/usr/lib/jvm/java-1.5.0-sun/include/ \
	-I/usr/lib/jvm/java-1.5.0-sun/include/linux \
	-I/usr/src/linux-headers-2.6.27-11-generic/include \
	-Isrc
SRC = $(wildcard src/*.cpp)
OBJ = $(patsubst src/%.cpp,dest/%.o,$(SRC))
DST = ../../dest/lib/linux
DEF = -D __DUMP -D _DEBUG

all: $(DST)/liblu04j.so $(DST)/libou14j.so

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
	g++ -shared -o $@ $(OBJ) $(LIBUSB01OBJ) -lusb

dest/%.o: src/lu0/%.cpp
	g++ -Wall -c $< -o $@ $(INC) $(DEF)

#############################################################################
# OpenUSB 1.0
#############################################################################
OPENUSB10SRC = $(wildcard src/ou1/*.cpp)
OPENUSB10OBJ = $(patsubst src/ou1/%.cpp,dest/%.o,$(OPENUSB10SRC))

$(DST)/libou14j.so: $(OBJ) $(OPENUSB10OBJ)
	g++ -shared -o $@ $(OBJ) $(OPENUSB10OBJ) -lopenusb

dest/%.o: src/ou1/%.cpp
	g++ -Wall -c $< -o $@ $(INC) $(DEF)
