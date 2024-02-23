#! /usr/bin/env bash

# Generates query files using the following settings:
#	Maximum depth: 6
# 	Probability of a wildcard: 0.2
#	Probability of doubleslash: 0.2
# 	Queries are distinct
#	Number of predicates: 0
# 	Number of nested paths: 1, 2, 3
# 	Number of generated queries: 1000, 50000, 100000, 150000, 200000
#
# Generated query files are output to the current directory.

yfilter=~/yfilter-1.0

java edu.berkeley.cs.db.yfilterplus.dtdscanner.DTDStat ${yfilter}/sample/nitf-2-5.dtd nitf-2-5.stat

# Generate queries files
for NP in 1 2 3 ; do

	java edu.berkeley.cs.db.yfilterplus.querygenerator.PathGenerator \
	       nitf-2-5.stat \
	       queries${QNUMBER}_${NP}.txt \
	       200000 \
	       6 0.2 0.2 \
	       --num_nestedpaths=${NP} \
	       --distinct=TRUE

done;

rm nitf-2-5.stat
