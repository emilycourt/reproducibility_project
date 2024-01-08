#!/bin/bash

#        echo -n "$QNUMBER "
	grep "Avg:" performance.txt >temp
	awk '{printf $2" "}' temp
	echo
