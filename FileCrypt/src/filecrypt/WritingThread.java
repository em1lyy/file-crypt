package filecrypt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.security.KeyPair;

import javax.swing.JOptionPane;

public class WritingThread extends Thread {
	
	// Some variables
	String[] input;
	BufferedWriter out;
	KeyPair key;
	int keyLength;
	boolean decryptable;
	int[] failedLines;
	
	// Constructor with all information about the File
	public WritingThread(String[] input, BufferedWriter out, boolean highPriority, KeyPair key, int keyLength, boolean decryptable, int[] failedLines) {
		this.input = input;
		this.out = out;
		this.key = key;
		this.keyLength = keyLength;
		this.decryptable = decryptable;
		this.failedLines = failedLines;
		if(highPriority)
			this.setPriority(MAX_PRIORITY);
		else
			this.setPriority(NORM_PRIORITY);
	}
	
	// Void to start saving the File
	@Override
	public void run() {
		try {
			out.write("#FILETYPE FCRSA");
			out.newLine();
			out.write("#KEYLENGTH " + this.keyLength);
			out.newLine();
			out.write("#PUBLIC " + key.getPublic().toString());
			out.newLine();
			if (decryptable) {
				out.write("#PRIVATE " + key.getPrivate().toString());
				out.newLine();
			} else {
				out.write("#PRIVATE NULL");
				out.newLine();
			}
			if(!failedLines.toString().equalsIgnoreCase("[]")) {
				out.write("#FAILEDLINES " + failedLines.toString());
				out.newLine();
			} else {
				out.write("#FAILEDLINES NULL");
				out.newLine();
			}
			out.write("###TEXT###");
			out.newLine();
			for (String line : input) {
				out.write(line);
				out.newLine();
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "FileCrypt", JOptionPane.ERROR_MESSAGE);
		}
	}
	
}
