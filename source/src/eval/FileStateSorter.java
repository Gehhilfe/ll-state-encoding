package eval;

import io.Parser;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStateSorter {

	public static void main(String[] args) throws IOException {
		Path folder = FileSystems.getDefault().getPath(args[0]);
		Path outFolder = FileSystems.getDefault().getPath(args[1]);
		DirectoryStream<Path> stream = Files.newDirectoryStream(folder);
		for(Path p : stream) {
			String fileName = p.getFileName().toString();
			if(fileName.endsWith(".kiss2")) {
				Parser fsm = new Parser();
				fsm.parseFile(p.toString());
				int states = fsm.getParsedFile().getNum_states();
				String stateString = Integer.toString(states);
				for(int i = stateString.length(); i<3; i++) {
					stateString = "0"+stateString;
				}
				Files.copy(p, outFolder.resolve(stateString+"_"+p.getFileName()));
			}
		}
	}

}
