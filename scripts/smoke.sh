#! /usr/bin/env bash

# smoke.sh runs the required components of the YFilter software
# to verify that they run as expected. 
# The tested components are the EXfilter class from the yfiler package,
# the DTDStat and PathGenerator classes from yfilterplus,
# and the yfilter executable.

yfilter=~/yfilter-1.0

echo "Running EXFilter"
java edu.berkeley.cs.db.yfilter.filter.EXfilter ${yfilter}/tests/xmldocs ${yfilter}/queries.txt > tmp.txt

if [ -f result.txt ] && [ -f performance.txt ]; then
        echo "EXFilter runs successfully"
else
        echo "Error running EXFilter"
fi


echo "Running DTDStat"
java edu.berkeley.cs.db.yfilterplus.dtdscanner.DTDStat ${yfilter}/sample/nitf-2-5.dtd nitf-2-5.stat > tmp.txt

if [ -f nitf-2-5.stat ]; then
        echo "DTDStat runs successfully"
else
        echo "Error running DTDStat"
fi


echo "Running PathGenerator"
java edu.berkeley.cs.db.yfilterplus.querygenerator.PathGenerator nitf-2-5.stat queries1.txt 1 6 0.2 0.2 > tmp.txt

if [ -f queries1.txt ]; then
        echo "PathGenerator runs successfully"
else
        echo "Error running PathGenerator"
fi


echo "Running yfilter"
yfilter ${yfilter}/tests/xmldocs/outFile1.xml queries1.txt > tmp.txt

if grep -q "Processsing ${yfilter}/tests/xmldocs/outFile1.xml" tmp.txt; then
	echo "yfilter runs successfully"
else
	echo "Error running yfilter"
fi

rm result.txt performance.txt nitf-2-5.stat queries1.txt tmp.txt