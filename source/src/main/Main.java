package main;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.Parser;
import lowlevel.ParsedFile;
import lowlevel.State;

/**
 * Main class
 * @author Wolf & Gottschling
 *
 */
public class Main {
	
	public static int countBitsToPos(long value, int pos) {
		int count = 0;
		while(pos-- != 0) {
			if((value&0x1) != 0)
				count++;
			value >>= 1;
		}
		return count;
	};

	public static void main(String[] args) {
		
		if(args.length>0){
			System.out.println(" Current working directory : " + System.getProperty("user.dir"));
			
			String input_file_name = args[0];
			
			Parser p = new Parser();
			p.parseFile(input_file_name);
			
			// Representation of the FSM
			ParsedFile fsm = p.getParsedFile();
			MinimalCoverFinder finder = new MinimalCoverFinder(fsm);
			List<long[]> minCover = finder.compute();
			
			// Find constrain matrix
			List<Long> constrains = minCover.stream().map(e -> e[2]).distinct().filter((e) -> {
				int val = countBitsToPos(e, fsm.getNum_states());
				return val > 1 && val != fsm.getNum_states();
			}).collect(Collectors.toList());
			
			// 
			for(long constrain: constrains) {
				RootDichotomyGenerator generator = new RootDichotomyGenerator(constrain, fsm.getNum_states());
				generator.generate();
			}
			
			// TODO - here you go 

		}
		else{
			System.out.println("No input argument given");
		}
	}
}
