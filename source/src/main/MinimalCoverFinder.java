package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lowlevel.ParsedFile;
import lowlevel.State;

public class MinimalCoverFinder {
	private ParsedFile fsm;

	public static boolean cover(long a, long b) {
		long mask = 0x3;
		long r = a&b;
		while(mask != 0) {
			if((a&mask) != 0)
			{
				if((r&mask) == 0) {
					return false;
				}
			}
			mask <<= 2;
		}
		return true;
	};
	
	public static int bitCount(long l) {
		int counter = 0;
		for(int i=0;i<64;i++) {
			long val = l&0x01;
			if(val != 0) {
				counter += 1;
			}
			l >>>= 1;
		};
		return counter;
	}
	
	public static boolean isBitCountOne(long l) {
		int counter = 0;
		for(int i=0;i<64;i++) {
			long val = l&0x01;
			if(val != 0) {
				counter += 1;
				if(counter == 2)
					return false;
			}
			l >>>= 1;
		};
		if(counter == 1)
			return true;
		return false;
	};
	
	public MinimalCoverFinder(ParsedFile fsm) {
		this.fsm = fsm;
	}
	
	public List<long[]> compute() {
		OneHotEncoder encoder = new OneHotEncoder();
		List<long[]> lines = new ArrayList<long[]>();
		for(State s:fsm.getStates()) {
			for(Long input:s.getInputs()) {
				
				long[] line = new long[3];
				line[0] = input;
				line[1] = s.output(input);
				line[2] = encoder.encode(s);
				
				lines.add(line);
			}
		}
		
		List<long[]> oldLines;
		List<long[]> newLines = lines;
		do {
			oldLines = newLines;
			newLines = min(oldLines);
		} while(newLines.size() != oldLines.size());
		
		return newLines;
	}
	
	private List<long[]> min(List<long[]> lines) {
		List<long[]> newLines = new ArrayList(lines);
		for(int i=0;i<lines.size();i++) {
			for(int j=i+1;j<lines.size();j++) {
				
				//check cover
				boolean inputCover = cover(lines.get(i)[0],lines.get(j)[0]);
				boolean outputCover = cover(lines.get(i)[1],lines.get(j)[1]);
				
				long xors = lines.get(i)[2] ^ lines.get(j)[2];
				
				int count = bitCount(xors);
				if(count == 2 && inputCover && outputCover) {
					long[] e = new long[3];
					e[0] = lines.get(i)[0]&lines.get(j)[0];
					e[1] = lines.get(i)[1]&lines.get(j)[1];
					e[2] = lines.get(i)[2]|lines.get(j)[2];
					newLines.set(i, e);
					newLines.remove(j);
					return newLines;
				} 
			}
		}
		return newLines;
	}
}
