#!/usr/bin/make

BIN_DIR=${YFILTER_HOME}/build/classes

all: filter icdedemo operators querymodule filterinstance
	javac -d ${BIN_DIR} *.java
	cd filter; make all;
	cd icdedemo; make all;
	cd operators; make all;
	cd querymodule; make all;
	cd filterinstance; make all;

clean: filter icdedemo operators querymodule filterinstance
	\rm ${BIN_DIR}/edu/berkeley/cs/db/yfilter/*.class
	cd filter; make clean;
	cd icdedemo; make clean;
	cd operators; make clean;
	cd querymodule; make clean;
	cd filterinstance; make clean;
