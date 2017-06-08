package main;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
	
	public static void printArray(long[] a) {
		for(int i = a.length - 1; i >= 0; i--) {
			for(int ii = 0; ii < Long.numberOfLeadingZeros(a[i]); ii++) {
				System.out.print("0");
			}
			System.out.print(Long.toBinaryString(a[i]));
		}
		System.out.println("");
	}
	
	public static boolean coversArray(long[] a, long[] b) {
		boolean covers = true;
		
		for(int i = 0; i < a.length; i++) {
			if((a[i] & b[i]) != b[i]) {
				covers = false;
				break;
			}
		}
		
		return covers;
	}

	public static boolean verifyTable(List<long[]> table, int width) {
		int[] counter = new int[width];
		boolean bitSet = false;

		for(int i = 0; i < width; i++) {
			bitSet = false;

			int idx = i / 64;
			int shift = i % 64;

			for(long[] l : table) {
				if((l[idx] & (1L << shift)) != 0) {
					counter[i]++;
					bitSet = true;
				}
			}

			if(bitSet == false) {
				break;
			}
		}

		return bitSet;
	}

	public static boolean arrayZero(long[] arr) {
		boolean zero = true;

		for(int i = 0; i < arr.length; i++) {
			if(arr[i] != 0) {
				zero = false;
				break;
			}
		}

		return zero;
	}

	public static int countBitsInArray(long[] arr) {
		int count = 0;

		for(int i = 0; i < arr.length; i++) {
			for(int ii = 0; ii < 64; ii++) {
				if(((arr[i] >> ii) & 1) == 1) {
					count++;
				}
			}
		}

		return count;
	}

	public static int countBitsToPos(long value, int pos) {
		int count = 0;
		while (pos-- != 0) {
			if ((value & 0x1) != 0)
				count++;
			value >>= 1;
		}
		return count;
	};

	public static long unsetBitNum(long value, int pos) {
		int count = 0;
		int bitPos = 0;
		long retValue = value;
		while (value != 0) {
			if(pos == 0) {
				retValue = retValue& ~(1L<<bitPos);
			}
			if ((value & 0x1) != 0)
				pos--;
			value >>= 1;
			bitPos++;
		}
		return retValue;
	};

	public static int getLastBitPos(long value) {
		int res = 0;
		while (value != 0) {
			value >>=1;
			res++;
		}
		return res;
	}

	private static BigInteger ONE = BigInteger.valueOf(1);

	private static void printLong(long value, int bits) {
		int pos = getLastBitPos(value);
		for(int i=pos;i<bits-1;i++)
			System.out.print('0');
		System.out.print(Long.toBinaryString(value));
	}

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

	private static void longArrayShift(long[] arr, int val) {
		for(int i = arr.length - 1; i > 0; i--) {
			arr[i] <<= 1;
			arr[i] |= arr[i-1] >>> 64;
		}
		arr[0] <<= 1;
		arr[0] |= val;
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
			List<Long> constrains = minCover.parallelStream().map(e -> e[2]).distinct().filter((e) -> {
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

			int coveredcounter = 0;
			List<long[]> table = new ArrayList<>();
			List<Long> primes = new ArrayList<>();
			DichotomyGenerator dg = new DichotomyGenerator(fsm.getNum_states());
			int arraySize = (rootDichotomies.size() / 64) + 1;
			int counter = 0;
			for(Dichotomy d = dg.generate();d!=null;d = dg.generate()) {
				long[] res = new long[arraySize];
				for(Dichotomy root: rootDichotomies) {
					if(d.covers(root)) {
						longArrayShift(res, 1);
					} else {
						longArrayShift(res, 0);
					}
				}
				if(!arrayZero(res)) {
					boolean covered = false;
					for(long[] l : table) {
						if(coversArray(l, res)) {
							covered = true;
							coveredcounter++;
							break;
						}
					}
					
					if(!covered) {
						primes.add(d.lMask);
						table.add(res);
					}
				}
			}
			
			System.out.println("Table size: " + table.size());
			System.out.println("Covered rows removed: " + coveredcounter);

			int range = 1;
			boolean found = false;
			int[] resultVec = null;

			/*
			System.out.println("Sorting list with " + table.size() + " entries...");
			table.sort(new Comparator<long[]>() {
				@Override
				public int compare(long[] o1, long[] o2) {
					// TODO Auto-generated method stub
					return countBitsInArray(o2) - countBitsInArray(o1);
				}
			});
			*/

			System.out.print("Verifying table... ");
			if(verifyTable(table, rootDichotomies.size())) {
				System.out.println("Success!");
			} else {
				System.out.println("Failed!");
			}


			System.out.println("Coverage table computed, starting so search...");
			if(table.size() > 0) {
				ParallelCoverFinder coverFinder = new ParallelCoverFinder(table, 10, rootDichotomies.size());
				resultVec = coverFinder.run();
			}

			System.out.println("Found minimal prime dichtomies:");
			List<Long> resultingPrimes = new ArrayList<Long>();

			if(resultVec != null) {
				for (int i = 0; i < resultVec.length; i++) {
					long prime = primes.get(resultVec[i]);
					resultingPrimes.add(prime);
				}
			}

			//Transpose
			List<Long> resultingPrimesTranspose = new ArrayList();
			Set<Long> lookupSet = new HashSet<Long>();
			int maxM = 0;
			for(int i = 0; i<fsm.getNum_states();i++) {
				long val = 0;
				int j = 0;
				for(Long prime:resultingPrimes) {
					val |= (((prime>>>i)&0x1)<<j);
					j++;
				}

				int m = 0;
				long oldVal = val;
				while(lookupSet.contains(val)) {
					System.out.println("Collision");
					m++;
					val = oldVal | (m<<resultingPrimes.size());
				}

				if(m>maxM)
					maxM=m;

				resultingPrimesTranspose.add(val);
				lookupSet.add(val);
			}

			System.out.println(resultingPrimes.size());
			System.out.println(getLastBitPos(maxM)+1);

			for(Long e:resultingPrimesTranspose) {
				printLong(e, resultingPrimes.size()+getLastBitPos(maxM)+1);
				System.out.print("\n");
			}

			BLIFExporter exporter = new BLIFExporter(fsm, resultingPrimesTranspose, resultingPrimes.size()+getLastBitPos(maxM));
			exporter.writeToFile(fsm.getFileName()+".blif");
			System.out.println("Ende");
		}
		else{
			System.out.println("No input argument given");
		}

		System.exit(0);
	}
}
