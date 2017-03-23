#!/bin/bash
cd bin/
(java controller.Server <../input/s1.txt >../output/s1.txt &)
(java controller.Server <../input/s2.txt >../output/s2.txt &)
(java controller.Server <../input/s3.txt >../output/s3.txt &)

