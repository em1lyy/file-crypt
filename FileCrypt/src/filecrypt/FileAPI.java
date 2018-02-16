package filecrypt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

// Simple file reading/writing API
public class FileAPI {
	
	public static BufferedReader in = null;
	
	// Method to read Files
	public static String[] readFile(File file) {
		String currLine;
		ArrayList<String> linesTEMP = new ArrayList<String>();
		try {
			in = new BufferedReader(new FileReader(file));
			while((currLine = in.readLine()) != null) {
				linesTEMP.add(currLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		String[] lines = new String[linesTEMP.size()];
		for (int i = 0; i < linesTEMP.size(); i++) {
			lines[i] = linesTEMP.get(i);
		}
		
		return lines;
	}
	
	// Method to write to Files
	public static void writeFile(File file, String[] msg, BufferedWriter out) {
		
		try {
			for (int i = 0; i < msg.length; i++) {
				if (msg[i] != null) {
					out.write(msg[i]);
					out.newLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
