#!/bin/bash

JAVA="java -Xmx250M"
HOME_REPRO="/home/repro"

### inputs to this experiment
XML_DOCS=${HOME_REPRO}/data/xml
NUM_DOCS=200

RESULT_DIR=${HOME_REPRO}/results
if [ -d $RESULT_DIR ]; then
 echo "$RESULT_DIR already exists"
else
 mkdir $RESULT_DIR
fi

touch table.txt
touch stat.txt

cat temp2 >> table.txt

### vary the number of nested paths in this experiment
for NESTEDPATH in 1 2 3 ; do

	echo -e "\nNESTEDPATH = ${NESTEDPATH}:"

	# echo -n "$NESTEDPATH " > temp2

	QUERY_FILE=${HOME_REPRO}/data/queries/queries_${NESTEDPATH}.txt

	### vary the number of queries in this experiment
	for QNUMBER in 1000 10000 50000 100000 150000 200000 ; do

		echo -e "\nQNUMBER = ${QNUMBER}:"

		${JAVA} edu.berkeley.cs.db.yfilter.filter.EXfilter ${XML_DOCS} ${QUERY_FILE} --num_docs=${NUM_DOCS}  --num_queries=${QNUMBER}

		echo -n "$NESTEDPATH $QNUMBER " > temp2
		cat temp2 >> table.txt
		$YFILTER_HOME/tests/scripts/collect.sh > temp2
		cat temp2 >> table.txt

		echo "NESTEDPATH = $NESTEDPATH QNUMBER = $QNUMBER" > temp2
		cat temp2 >> stat.txt
		tail -n 4 result.txt > temp2
		cat temp2 >> stat.txt

		mv performance.txt $RESULT_DIR/performance-${NESTEDPATH}-${QNUMBER}.txt

	done;

done;

mv table.txt $RESULT_DIR/
mv stat.txt $RESULT_DIR/
rm result.txt
rm temp
rm temp2
