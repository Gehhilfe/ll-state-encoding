package main;

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

		public WorkerStatus(int workerCount) {
			this.workerCount = workerCount;
			countMutex = new Object();
			exitSignal = new CountDownLatch(1);
			this.solution = null;
		}

		public void finished() {
			synchronized (countMutex) {
				finishedCount++;

				if(finishedCount == workerCount) {
					exitSignal.countDown();
				}
			}
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
		List<long[]> table;
		LimitedCombinationGenerator combGen;
		WorkerStatus status;

		public Worker(int id, List<long[]> table, LimitedCombinationGenerator combGen, WorkerStatus status, int width) {
			this.table = table;
			this.combGen = combGen;
			this.status = status;
			this.width = width;
			this.id = id;
		}

		@Override
		public void run() {
			int[] resultVec;
			long[] searchMask = new long[width/64 + 1];
			boolean found = false;

			for(int i = 0; i < width; i += 64) {
				searchMask[i/64] = -1L;
			}
			searchMask[searchMask.length-1] = (1L << (width % 64)) - 1;

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

			System.out.println("Thread " + id + " finished");

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
			WorkerStatus status = new WorkerStatus(threadCount);

			int step = tableSize / threadCount;
			int id = 0;

			for(int i = 0; i < threadCount; i++) {
				int min = i * step;
				int max = (i + 1) * step;
				if(i == (threadCount - 1)) {
					max = tableSize;
				}

				LimitedCombinationGenerator combGen = new LimitedCombinationGenerator(tableSize, size, min, max);

				new Worker(id++, table, combGen, status, width).start();
			}

			int[] result = status.waitForThreads();
			if(result != null) {
				return result;
			}

			size++;
		}
	}
}
