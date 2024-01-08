#!/usr/bin/make

BIN_DIR=${YFILTER_HOME}/build/classes

all: xquery
	javac -d ${BIN_DIR} *.java
	cd xquery; make all;

clean:
	\rm ${BIN_DIR}/edu/berkeley/cs/db/yfilterplus/queryparser/xqueryparser/*.class
	cd xquery; make clean;
