#!/bin/bash
echo "#!/bin/bash" > server_kill.sh
n=2
for i in `seq 1 $n`; do
	(java -cp bin/ server.Server <input/s${i}.txt >output/s${i}.txt &)
	echo "kill $!" >> server_kill.sh;
done

echo "printing server_kill.sh: "
cat server_kill.sh

echo "running server driver: "
java -cp bin/ server.ServerDriver

chmod +x server_kill.sh
./server_kill.sh
rm server_kill.sh
