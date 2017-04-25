#!/bin/bash
echo "#!/bin/bash" > skill.sh
(java -cp bin/ server.Server < input/s1.txt > output/s1.txt & echo "kill $!" >> skill.sh) 
(java -cp bin/ server.Server < input/s2.txt > output/s2.txt & echo "kill $!" >> skill.sh)
(java -cp bin/ server.Server < input/s3.txt > output/s3.txt & echo "kill $!" >> skill.sh)
echo "rm ./skill.sh" >> skill.sh
chmod +x skill.sh
