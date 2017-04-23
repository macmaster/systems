#!/bin/bash
echo "!/bin/bash" > kill.sh
n=2
for i in `seq 1 $n`; do
	(java -cp bin/ server.Server <input/s${i}.txt >output/s${i}.txt &)
	echo "kill $!" >> kill.sh;
done

echo "printing kill.sh: "
cat kill.sh

echo "running server driver: "
java server.ServerDriver

./kill.sh
rm kill.sh
