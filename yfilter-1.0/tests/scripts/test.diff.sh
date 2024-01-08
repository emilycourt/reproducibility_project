#!/bin/bash

# A test suite for the YFilter engine

YFILTER_TESTS=${YFILTER_HOME}/tests

# create the output directory and sub-directories

OUTPUT_DIR="${YFILTER_TESTS}/diff.out"
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

# diff the results
for queries in tsp tspr tnp tes tmq
do
  cd ${YFILTER_TESTS}/correct.out/$queries
# diff each output file (one per document)

  echo "operating in dir: " 
  pwd

  FILES=`ls o*.*`

  for file in $FILES
    do
    ls "$file"
    diff $file ${YFILTER_TESTS}/yfilter.out/$queries/$file > ${YFILTER_TESTS}/diff.out/$queries/$file.diff
  done
done

echo


