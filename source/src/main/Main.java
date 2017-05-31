package main;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.Parser;
import lowlevel.ParsedFile;
import lowlevel.State;

/**
 * Main class
 * 
 * @author Wolf & Gottschling
 *
 */
public class Main {

	public static int countBitsToPos(long value, int pos) {
		int count = 0;
		while (pos-- != 0) {
			if ((value & 0x1) != 0)
				count++;
			value >>= 1;
		}
		return count;
	};

	private static BigInteger ONE = BigInteger.valueOf(1);

	public static int binKoeff(int im, int ik) {
		BigInteger res = ONE;
		BigInteger m = BigInteger.valueOf(im);
		BigInteger k = BigInteger.valueOf(ik);
		for (BigInteger i = ONE; i.compareTo(k) <= 0; i = i.add(ONE)) {
			res = res.multiply(m).divide(i);
			m = m.subtract(ONE);
		}
		return res.intValue();
	}

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
			List<Dichotomy> rootDichotomies = new ArrayList<Dichotomy>();
			for(long constrain: constrains) {
				RootDichotomyGenerator generator = new RootDichotomyGenerator(constrain, fsm.getNum_states());
				rootDichotomies.addAll(generator.generate());
			}
			
			System.out.print("\n");
			System.out.print("\n");
			
			List<Long> table = new ArrayList<>();
			List<Long> primes = new ArrayList<>();
			DichotomyGenerator dg = new DichotomyGenerator(fsm.getNum_states());
			for(Dichotomy d = dg.generate();d!=null;d = dg.generate()) {
				primes.add(d.lMask);
				long res = 0;
				for(Dichotomy root: rootDichotomies) {
					res <<= 1;
					if(d.covers(root)) {
						res |= 1; 
					}
				}
				table.add(res);
			}
		
			int range = 1;
			boolean found = false;
			int[] resultVec = null;
			long searchMask = (1<<rootDichotomies.size())-1;
			do {
				CompNoRepGenerator gen = new CompNoRepGenerator(range, table.size());
				int limit = binKoeff(table.size(), range);
				System.out.println(table.size());
				System.out.println(range);
				System.out.println(limit);
				Optional<int[]> result = Stream.generate(gen).parallel().limit(binKoeff(table.size(), range)).filter((vec) -> {
					long res = 0;
					for (int i = 0; i < vec.length; i++) {
						res |= table.get(vec[i]);
					}
					if(res == searchMask) {
						return true;
					}
					return false;
				}).findAny();
				
				/*
				for(int[] vec=gen.generate();vec!=null;vec=gen.generate()) {
					long res = 0;
					for (int i = 0; i < vec.length; i++) {
						res |= table.get(vec[i]);
					}
					if(res == searchMask) {
						resultVec = vec;
						found = true;
						break;
					}
				}*/
				
				if(result.isPresent()){
					found = true;
					resultVec = result.get().clone();
				}
				range++;
			} while(!found);
			System.out.println("Ende");
		}
		else{
			System.out.println("No input argument given");
		}
	}
}
