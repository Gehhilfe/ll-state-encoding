package main;

public class DichotomyGenerator {

	private int width;
	private long lastMask;
	private long fullMask;
	
	DichotomyGenerator(int width) {
		this.width = width;
		this.lastMask = 1;
		this.fullMask = (1<<(width+1))-1;
	}
	
	public Dichotomy generate() {
		//TODO anschauen!!!
		if(lastMask-1 == fullMask/2)
			return null;
		Dichotomy d = new Dichotomy(lastMask, lastMask^fullMask);
		lastMask++;
		return d;
	}
}
