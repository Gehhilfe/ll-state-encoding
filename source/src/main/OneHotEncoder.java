package main;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import lowlevel.State;

public class OneHotEncoder {

	private int index;
	private HashMap<State, Long> encodings;
	
	public OneHotEncoder() {
		index = 0;
		encodings = new HashMap<State, Long>();
	}
	
	public long encode(State s) {
		if(encodings.containsKey(s))
			return encodings.get(s);
		long encoding = (1L<<index);
		index += 1;
		if(index >= 63) {
			throw new UnsupportedOperationException();
		}
		encodings.put(s, encoding);
		return encoding;
	}

}
