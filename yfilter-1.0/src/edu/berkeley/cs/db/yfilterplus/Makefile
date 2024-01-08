#!/usr/bin/make

BIN_DIR=${YFILTER_HOME}/build/classes

all: dtdscanner querygenerator queryparser utility xmltree
	cd dtdscanner; make all;
	cd querygenerator; make all;
	cd queryparser; make all;
	cd xmltree; make all;
	cd utility; make all;

clean: dtdscanner querygenerator queryparser utility xmltree
	cd dtdscanner; make clean;
	cd querygenerator; make clean;
	cd queryparser; make clean;
	cd xmltree; make clean;
	cd utility; make clean;
