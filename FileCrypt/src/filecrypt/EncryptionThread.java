package filecrypt;

import java.security.KeyPair;
import java.security.PublicKey;

// A class which conatins code for multi-threaded encryption
public class EncryptionThread extends Thread {
	
	// Some variables
	String[] input;
	String[] output;
	int index;
	KeyPair key;
	
	// Constructor
	public EncryptionThread(String[] input, String[] output, int index, boolean highPriority, KeyPair key) {
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
		this.output[this.index] = out;
		
	}
	
	// A very minimalistic encryption method (returns a String)
	private String encrypt(String decryptedMessage, PublicKey pk) {
		String result = new String(EncryptionAPI.encrypt(decryptedMessage, EncryptionAPI.key.getPublic()));
		return result;
	}
	
}
