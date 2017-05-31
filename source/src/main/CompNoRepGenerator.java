package main;

import java.util.function.Supplier;


// Geklaut von http://www.aconnect.de/friends/editions/computer/combinatoricode_g.html
public class CompNoRepGenerator implements Supplier<int[]>{
	
	int[] vector;
	int k;
	int n;
	boolean first;
	
	public CompNoRepGenerator(int k, int n) {
		if(k > n)
			throw new UnsupportedOperationException();
		if(k==0)
			throw new UnsupportedOperationException();
		this.k = k;
		this.n = n;
		this.first = true;
		vector = new int[k];
		for (int i = 0; i < vector.length; i++) {
			vector[i] = i;
		}
	}
	
	public synchronized int[] generate() {
		if(first) {
			first = false;
			return vector.clone();
		}
		
		int j;
		
		//easy case, increase rightmost element
		if(vector[k-1] < n -1) {
			vector[k - 1]++;
			return vector.clone();
		}
		
		for(j = k - 2; j >= 0; j--)
			 if(vector[j] < n - k + j)
			  break;
		
		//terminate if vector[0] == n - k
		if(j < 0)
			return null;
		
		//increase
		vector[j]++;
		
		//set right-hand elements
		while(j < k - 1) {
			vector[j + 1] = vector[j] + 1;
			j++;
		}
		
		return vector.clone();
	}

	@Override
	public int[] get() {
		return generate();
	}

}
