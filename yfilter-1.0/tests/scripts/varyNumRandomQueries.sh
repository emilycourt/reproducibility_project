#!/bin/bash

JAVA="java -Xmx250M"

### list of query parameters and their settings
DEPTH=6
WILDCARD=.2
DSLASH=.2
SKEW=0
PREDPROB=0
NESTEDPATH=0

### inputs to this experiment
XML_DOCS=${YFILTER_HOME}/tests/xmldocs-500
NUM_DOCS=500

TEST_DIR=${YFILTER_HOME}/tests/benchmark_simple_paths
QUERY_FILE=${TEST_DIR}/queries-500000.txt

### prepare the work directory
cd ${TEST_DIR}

RESULT_DIR="varyNumRandomQueries"
if [ -d $RESULT_DIR ]; then
 echo "$RESULT_DIR already exists"
else
 mkdir $RESULT_DIR
fi

touch table.txt
touch stat.txt

### vary the number of queries in this experiment

for QNUMBER in 1000 100000 200000 300000 400000 500000 ; do 

echo "QNUMBER = ${QNUMBER} ..."

${JAVA} edu.berkeley.cs.db.yfilter.filter.EXfilter ${XML_DOCS} ${QUERY_FILE} --num_docs=${NUM_DOCS} --num_queries=${QNUMBER}

echo -n "$QNUMBER " >temp2
cat temp2 >> table.txt
$YFILTER_HOME/tests/scripts/collect.sh >temp2
cat temp2 >> table.txt

echo "QNUMBER = $QNUMBER" > temp2
cat temp2 >> stat.txt
tail -n 4 result.txt > temp2
cat temp2 >> stat.txt

mv performance.txt $RESULT_DIR/performance-${QNUMBER}.txt

done;

mv table.txt $RESULT_DIR/
mv stat.txt $RESULT_DIR/



