#!/bin/bash
cd bin/
(java server.Server <../input/s1.txt >../output/s1.txt &) 
(java server.Server <../input/s2.txt >../output/s2.txt &)
#java server.Server <../input/s3.txt >../output/s3.txt &
java server.ServerDriver