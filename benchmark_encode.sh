#!/bin/bash
trap "exit" INT

for i in `ls -1 benchmarks/kiss_files_sorted/*.kiss2`; do
	if [[ ! -z $(grep "$i" success.txt) ]]; then
		echo "File already covered: $i"
		continue
	fi
	echo "--------"
	echo $i
	start=$(date +%s)
	timeout 2h java -Xmx50g -cp build/ main.Main $i nonexact
	retcode=$?
	end=$(date +%s)
	time=`expr $end - $start`
	echo $time
	if [ $retcode -eq "0" ]; then
		echo $i $time >> success.txt
	else
		echo $i >> timeout.txt
	fi
	echo "--------"
done;
exit 0
