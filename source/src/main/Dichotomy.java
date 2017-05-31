package main;

public class Dichotomy {
	protected long lMask;
	protected long rMask;
	
	public Dichotomy(long lMask, long rMask) {
		this.lMask = lMask;
		this.rMask = rMask;
	}
	
	@Override
	public String toString() {
		return "L: "+Long.toBinaryString(lMask)+" R: "+Long.toBinaryString(rMask);
	}
	
	public boolean covers(Dichotomy other) {
		boolean flag = (lMask & other.rMask)==other.rMask && (rMask & other.lMask)==other.lMask;
		flag |=  (lMask & other.lMask)==other.lMask && (rMask & other.rMask)==other.rMask;
		return flag;
	}
	
	public boolean compatibility(Dichotomy other) {
		boolean flag = (lMask & other.rMask)==0 && (rMask & other.lMask)==0;
		flag |=  (lMask & other.lMask)==0 && (rMask & other.rMask)==0;
		return flag;
	}
}
