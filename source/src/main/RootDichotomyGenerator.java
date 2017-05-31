package main;

import java.util.ArrayList;
import java.util.List;

public class RootDichotomyGenerator {
	
	private long constrain;
	private int numStates;

	public RootDichotomyGenerator(long constrain, int states) {
		this.constrain = constrain;
		this.numStates = states;
	}
	
	public List<Dichotomy> generate() {
		ArrayList<Dichotomy> dichotomies = new ArrayList<Dichotomy>();
		
		long mask = 0xFFFFFFFF_FFFFFFFFL;
		
		long invConstrain = constrain^mask;
		
		int shift = 64-numStates;
		mask >>>= shift;
		
		long lMask = constrain&mask;
		long rMask = invConstrain&mask;
		
		int rBits = Main.countBitsToPos(rMask, numStates);
		
		LongBitPermutationGenerator gen = new LongBitPermutationGenerator(rMask);
		List<Long> permutations = gen.generate();
		for(Long e:permutations) {
			dichotomies.add(new Dichotomy(lMask, e));
		}
		
		return dichotomies;
	}
}
