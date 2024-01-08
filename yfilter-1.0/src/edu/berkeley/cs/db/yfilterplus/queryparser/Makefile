#!/usr/bin/make

BIN_DIR=${YFILTER_HOME}/build/classes

all: 
	cd xpathparser; make all;
	javac -d ${BIN_DIR} *.java

clean: 
	\rm ${BIN_DIR}/edu/berkeley/cs/db/yfilterplus/queryparser/*.class
	cd xpathparser; make clean;
