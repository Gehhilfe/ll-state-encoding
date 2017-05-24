package main;

import io.Parser;
import lowlevel.ParsedFile;

/**
 * Main class
 * @author Wolf & Gottschling
 *
 */
public class Main {
	
	public static void main(String[] args) {
				
		if(args.length>0){
			System.out.println(" Current working directory : " + System.getProperty("user.dir"));
			
			String input_file_name = args[0];
			
			Parser p = new Parser();
			p.parseFile(input_file_name);
			
			// Representation of the FSM
			ParsedFile fsm = p.getParsedFile();
			

			
			// TODO - here you go 

		}
		else{
			System.out.println("No input argument given");
		}
	}
}
