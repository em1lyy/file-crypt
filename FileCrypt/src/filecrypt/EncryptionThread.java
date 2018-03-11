package filecrypt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;

// A class which conatins code for multi-threaded encryption
public class EncryptionThread extends Thread {
	
	// Some variables
	String[] input;
	BufferedWriter output;
	int index;
	KeyPair key;
	
	// Constructor
	public EncryptionThread(String[] input, BufferedWriter output, int index, boolean highPriority, KeyPair key) {
		this.input = input;
		this.output = output;
		this.index = index;
		this.key = key;
		if(highPriority)
			this.setPriority(MAX_PRIORITY);
		else
			this.setPriority(NORM_PRIORITY);
	}
	
	// Tiny undependent encryption method
	public void run() {
		
		String in = this.input[this.index];
		String out = encrypt(in, this.key.getPublic());
		try {
			output.write(out);
			output.newLine();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
	// A very minimalistic encryption method (returns a String)
	private String encrypt(String decryptedMessage, PublicKey pk) {
		String result = new String(EncryptionAPI.encrypt(decryptedMessage, EncryptionAPI.key.getPublic()));
		return result;
	}
	
}
