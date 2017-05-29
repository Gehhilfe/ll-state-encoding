package main;

import java.util.ArrayList;
import java.util.List;

public class LongBitPermutationGenerator {
	
	private long value;

	public LongBitPermutationGenerator(long value) {
		this.value = value;
	}
	
	public List<Long> generate() {
		ArrayList<Long> result = new ArrayList();
		for(int i=0; i<64; i++) {
			long one = 1;
			one <<= i;
			long masked = value&one;
			if(masked != 0)
				result.add(masked);
		}
		return result;
	}
}
