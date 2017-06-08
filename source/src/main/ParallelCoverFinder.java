package main;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ParallelCoverFinder {

	class WorkerStatus {
		Object countMutex;
		int finishedCount;
		int workerCount;
		boolean solutionFound;
		int[] solution;
		CountDownLatch exitSignal;
		
		Object rowMutex;
		int row;
		int maxRow;

		public WorkerStatus(int workerCount, int rows) {
			this.workerCount = workerCount;
			countMutex = new Object();
			exitSignal = new CountDownLatch(1);
			this.solution = null;
			this.row = 0;
			this.maxRow = rows;
			this.rowMutex = new Object();
		}

		public void finished() {
			synchronized (countMutex) {
				finishedCount++;

				if(finishedCount == workerCount) {
					exitSignal.countDown();
				}
			}
		}
		
		public int getRow() {
			int r;
			
			synchronized (rowMutex) {
				if(row < maxRow) {
					r = row;
					row++;
				} else {
					r = -1;
				}
			}
			
			return r;
		}

		public void solutionFound(int[] solution) {
			this.solution = solution.clone();
			exitSignal.countDown();
		}

		public int[] waitForThreads() {
			try {
				exitSignal.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return solution;
		}
	}

	class Worker extends Thread {
		int id;
		int width;
		int size;
		List<long[]> table;
		WorkerStatus status;

		public Worker(int id, List<long[]> table, WorkerStatus status, int width, int size) {
			this.table = table;
			this.status = status;
			this.width = width;
			this.id = id;
			this.size = size;
		}

		@Override
		public void run() {
			int[] resultVec = null;
			long[] searchMask = new long[width/64 + 1];
			boolean found = false;
			int row;

			for(int i = 0; i < width; i += 64) {
				searchMask[i/64] = -1L;
			}
			searchMask[searchMask.length-1] = (1L << (width % 64)) - 1;

			while((row = status.getRow()) != -1) {
				LimitedCombinationGenerator combGen = new LimitedCombinationGenerator(table.size(), size, row, row+1);
				while((resultVec = combGen.generate()) != null) {
					long[] res = new long[searchMask.length];
					for (int i = 0; i < resultVec.length; i++) {
						long[] entry = table.get(resultVec[i]);
						for(int ii = 0; ii < searchMask.length; ii++) {
							res[ii] |= entry[ii];
						}
					}
					boolean equal = true;
					for(int i = 0; i < searchMask.length; i++) {
						if(res[i] != searchMask[i]) {
							equal = false;
							break;
						}
					}
					if(equal) {
						found = true;
						break;
					}
				}
				if(found) {
					break;
				}
			}

			if(found) {
				status.solutionFound(resultVec);
			} else {
				status.finished();
			}
		}
	}

	class LimitedCombinationGenerator {
		int n, k;
		int min, max;
		int[] vector;
		boolean initialized;

		public LimitedCombinationGenerator(int n, int k, int min, int max) {
			this.n = n;
			this.k = k;
			this.min = min;
			this.max = max;
			this.initialized = false;
		}

		public int[] generate() {
			if(!initialized) {
				if(min + k >= n) {
					return null;
				}
				vector = new int[k];
				for (int i = 0; i < k; i++) {
					vector[i] = min + i;
				}

				initialized = true;
				return vector;
			}

			int j;

			//easy case, increase rightmost element
			if(vector[k-1] < n -1) {
				vector[k - 1]++;
				return vector;
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

			// Check for upper bound.
			if(vector[0] >= max) {
				return null;
			}

			return vector;
		}
	}

	List<long[]> table;
	int threadCount;
	int width;

	public ParallelCoverFinder(List<long[]> table, int threadCount, int width) {
		this.table = table;
		this.threadCount = threadCount;
		this.width = width;
	}

	public int[] run() {
		int size = 1;

		while(true) {
			int tableSize = table.size();
			WorkerStatus status = new WorkerStatus(threadCount, tableSize);

			//int step = tableSize / threadCount;
			int id = 0;
			
			System.out.println("Search size: " + size);

			for(int i = 0; i < threadCount; i++) {
				new Worker(id++, table, status, width, size).start();
			}

			int[] result = status.waitForThreads();
			if(result != null) {
				return result;
			}

			size++;
		}
	}
}
