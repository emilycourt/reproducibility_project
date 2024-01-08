This is a reproduction package for the report "Evaluating Performance of YFilter's Approach to Nested Path Expression Processing"

This repository contains a Dockerfile. After building the docker image, the experiments described in the report can be reproduced in the docker container.

The docker container includes the datasets used in the experiment, along with the source code.

The docker container also includes the cloned latex report, which can be compiled using the included Makefile.

A smoke test can be run using the script smoke.sh. This test runs the required components of the YFilter software to verify that they run as expected.