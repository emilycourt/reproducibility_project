#!/bin/bash

HOME_REPRO="/home/repro"
SCRIPTS="${HOME_REPRO}/scripts"
REPORT="${HOME_REPRO}/report"

${SCRIPTS}/experiment.sh

python3 ${SCRIPTS}/create_graph.py 

mv replication_figure.png results_table.tex ${REPORT}/results

make -C ${REPORT}
cp ${REPORT}/report.pdf ${HOME_REPRO}
make -C ${REPORT} clean