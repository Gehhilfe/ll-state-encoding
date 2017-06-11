#!/bin/bash

TARGET_PATH=results_nonexact
ABC_PATH=abc/

for i in $(ls -1 $TARGET_PATH/*.blif); do
	result=$(./$ABC_PATH/abc.bin -c "read_lut abc/6-lut-lib; read_blif $i; fpga -v; print_stats")
	res1=$(echo "$result" | grep "Iteration 3A")
	res2=$(echo "$result" | grep "bdd")
	echo $i $res1 $res2
done;
