#!/bin/bash
cd bin/
(java Server <../input/s1.txt >../output/s1.txt &)
(java Server <../input/s2.txt >../output/s2.txt &)
(java Server <../input/s3.txt >../output/s3.txt &)

