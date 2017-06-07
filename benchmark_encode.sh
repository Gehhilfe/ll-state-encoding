#!/bin/bash
trap "exit" INT

rm -f success.txt timeout.txt

for i in `ls -1 benchmarks/kiss_files_sorted/*.kiss2`; do
	echo "--------"	
	echo $i
	start=$(date +%s)
	timeout 1m java -cp bin/ main.Main $i > /dev/null
	retcode=$?
	end=$(date +%s)
	time=`expr $end - $start`
	echo $time
	if [ $retcode -eq "0" ]; then
		echo $i >> success.txt
	else
		echo $i >> timeout.txt
	fi
	echo "--------"
done;
exit 0
