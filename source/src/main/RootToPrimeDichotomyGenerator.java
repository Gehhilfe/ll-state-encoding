package main;

import java.util.ArrayList;
import java.util.List;

public class RootToPrimeDichotomyGenerator {

	private Dichotomy rootDichotomy;
	private int numStates;
	
	public RootToPrimeDichotomyGenerator(Dichotomy rootDichotomy, int numStates) {
		this.rootDichotomy = rootDichotomy;
		this.numStates = numStates;
	}
	
	public List<Dichotomy> generate() {
		ArrayList<Dichotomy> dichotomies = new ArrayList<Dichotomy>();
		
		long invMask = 0xFFFFFFFF_FFFFFFFFL;
		long mask = 0xFFFFFFFF_FFFFFFFFL;		
		int shift = 64-numStates;
		mask >>>= shift;
		
		long notSetBits = (rootDichotomy.rMask ^ invMask)&mask& ~(rootDichotomy.lMask);
		int bits = Main.countBitsToPos(notSetBits, numStates);
		
		for(long i=0;i<=(long)(1L<<(bits-1));i++) {
			long rMask = notSetBits;
			for(int j=0;j<bits;j++) {
				if((i & (1L<<j)) != 0) {
					rMask = Main.unsetBitNum(rMask, j);
				}
			}
			rMask |= rootDichotomy.rMask;
			long lMask = rootDichotomy.lMask;
			lMask |= ((rMask^invMask)&mask);
			dichotomies.add(new Dichotomy(lMask, rMask));
		}
		
		/*
		int rBits = Main.countBitsToPos(rMask, numStates);
		
		LongBitPermutationGenerator gen = new LongBitPermutationGenerator(rMask);
		List<Long> permutations = gen.generate();
		for(Long e:permutations) {
			dichotomies.add(new Dichotomy(lMask, e));
		}
		*/
		
		return dichotomies;
	}
}
