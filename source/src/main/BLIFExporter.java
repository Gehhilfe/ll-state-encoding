package main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lowlevel.ParsedFile;
import lowlevel.State;
 
public class BLIFExporter {

	private ParsedFile fsm;
	private List<Long> encoding;
	private int stateBits;
	
	public BLIFExporter(ParsedFile fsm, List<Long> encoding, int stateBits) {
		this.fsm = fsm;
		this.encoding = encoding;
		this.stateBits = stateBits;
		System.out.println("stateBits: " + this.stateBits);
	}
	
	private String getInputs() {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<fsm.getNumInputs();i++) {
			sb.append("I"+i+" ");
		}
		return sb.toString();
	}
	
	private String getOutputs() {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<fsm.getNumOutputs();i++) {
			sb.append("O"+i+" ");
		}
		return sb.toString();
	}
	
	private String getStateBits() {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<stateBits;i++) {
			sb.append("S"+i+" ");
		}
		return sb.toString();
	}
	
	private String getNumBits(long value, int bits) {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<bits;i++) {
			if((value&0x1)==1)
				sb.append("1");
			else
				sb.append("0");
			value>>=1;
		}
		return sb.toString();
	}
	
	private String getBitsPosCube(long value, int bits) {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<bits;i++) {
			int masked = (int) (value&0x3);
			switch (masked) {
			case 1:
				sb.append("0");
				break;
				
			case 2:
				sb.append("1");
				break;
				
			case 3:
				sb.append("-");
				break;

			default:
				throw new UnsupportedOperationException();
			}
			value>>=2;
		}
		return sb.toString();
	}
	
	public void writeToFile(String path) {
		List<State> stateList = Arrays.asList(fsm.getStates());
		int num = 0;
		for (State state : stateList) {
			state.setCode(encoding.get(num));
			num++;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(".model ");
		sb.append(fsm.getFileName());
		sb.append("\n.inputs ");
		sb.append(getInputs());
		sb.append("\n.outputs ");
		sb.append(getOutputs());
		sb.append("\n.clock clk");
		
		int i = 0;
		for(State s:stateList) {
			sb.append("\n# "+s.getName()+" ");
			i++;
			sb.append(getNumBits(s.getCode(), stateBits));
		}
		
		/// State transition
		for(int x=0; x<stateBits;x++) {
			sb.append("\n.names ");
			sb.append(getStateBits());
			sb.append(getInputs());
			sb.append("next_S"+x);

			
			//for(int currentStateNum=0;currentStateNum<fsm.getNum_states();currentStateNum++) {
			for(State s:stateList) {
				long l = s.getCode();
				//long l = encoding.get(currentStateNum);
				
				// Find all next states
				long[][] transistions = s.getTransitions();
				for(long[] entry:transistions) {
					long targetEncoding = entry[2];
					if((targetEncoding&(1L<<x)) != 0) {
						sb.append("\n");
						sb.append(getNumBits(l, stateBits));
						sb.append(getBitsPosCube(entry[1], fsm.getNumInputs()));
						sb.append(" 1");
					}
				}
			}
			sb.append("\n.latch next_S"+x+" S"+x+" re clk 0");
			sb.append("\n");
			sb.append("\n");
		}
		
		
		/// Output transition
		for(int x=0; x<fsm.getNumOutputs();x++) {
			sb.append("\n.names ");
			sb.append(getStateBits());
			sb.append(getInputs());
			sb.append("next_O"+x);

			
			for(State s:stateList) {
				long l = s.getCode();
				
				// Find all next states
				long[][] transistions = s.getOutputs();
				for(long[] entry:transistions) {
					long targetEncoding = entry[2];
					if((targetEncoding&(3L<<2*x)) == 0x2L<<2*x) {
						sb.append("\n");
						sb.append(getNumBits(l, stateBits));
						sb.append(getBitsPosCube(entry[1], fsm.getNumInputs()));
						sb.append(" 1");
					}
				}
			}
			sb.append("\n.latch next_O"+x+" O"+x+" re clk 0");
			sb.append("\n");
			sb.append("\n");
		}
		
		
		FileWriter fw;
		try {
			fw = new FileWriter(path);
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
