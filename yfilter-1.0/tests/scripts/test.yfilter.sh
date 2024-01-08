#!/bin/bash

# A test suite for the YFilter engine

YFILTER_TESTS=${YFILTER_HOME}/tests

# create the output directory and sub-directories

OUTPUT_DIR="${YFILTER_TESTS}/yfilter.out"
if [ -d "$OUTPUT_DIR" ]; then
	echo "$OUTPUT_DIR already exists!"
else
	mkdir $OUTPUT_DIR
fi

for TEST in tsp tspr tnp tes tmq ; do 

TEST_DIR="${OUTPUT_DIR}/$TEST"
if [ -d "$TEST_DIR" ]; then
	echo "$TEST_DIR already exists!"
else
	mkdir $TEST_DIR
fi

done;

# yfilter 
cd ${YFILTER_TESTS}/yfilter.out/tsp
pwd
java edu.berkeley.cs.db.yfilter.filter.EXfilter ${YFILTER_TESTS}/xmldocs/ ${YFILTER_TESTS}/test_simple_paths/queries.txt --result=ALL --output=indiv

cd ${YFILTER_TESTS}/yfilter.out/tspr
pwd
java edu.berkeley.cs.db.yfilter.filter.EXfilter ${YFILTER_TESTS}/xmldocs/ ${YFILTER_TESTS}/test_simple_predicates/queries.txt --result=ALL --output=indiv

cd ${YFILTER_TESTS}/yfilter.out/tnp
pwd
java edu.berkeley.cs.db.yfilter.filter.EXfilter ${YFILTER_TESTS}/xmldocs/ ${YFILTER_TESTS}/test_nested_paths/queries.txt --result=ALL --output=indiv

cd ${YFILTER_TESTS}/yfilter.out/tes
pwd
java edu.berkeley.cs.db.yfilter.filter.EXfilter ${YFILTER_TESTS}/xmldocs/ ${YFILTER_TESTS}/test_extra_selects/queries.txt --result=ALL --output=indiv

cd ${YFILTER_TESTS}/yfilter.out/tmq
pwd
java edu.berkeley.cs.db.yfilter.filter.EXfilter ${YFILTER_TESTS}/xmldocs/ ${YFILTER_TESTS}/test_mixed_queries/queries.txt --result=ALL --output=indiv
