package filecrypt;

import java.security.KeyPair;

import javax.swing.JOptionPane;

// Thread that controls Encryption Threads to start Writing Thread when every Encryption Thread is dead
public class ThreadHostThread extends Thread {
	
	String[] msgDECRYPTED;
	String[] msgENCRYPTED;
	int[] failedLines;
	KeyPair key;
	boolean highPriority;
	int keyLength;
	boolean decryptable;
	int failedLinesIndex;
	
	public ThreadHostThread(String[] msgDECRYPTED, KeyPair key, boolean highPriority, int keyLength, boolean decryptable) {
		this.msgDECRYPTED = msgDECRYPTED;
		this.msgENCRYPTED = new String[this.msgDECRYPTED.length];
		failedLines = new int[this.msgDECRYPTED.length];
		this.key = key;
		this.highPriority = highPriority;
		this.keyLength = keyLength;
		this.decryptable = decryptable;
		this.failedLinesIndex = 0;
		if(this.highPriority)
			this.setPriority(MAX_PRIORITY);
		else
			this.setPriority(NORM_PRIORITY);
	}
	
	// Start everything
	@Override
	public void run() {
		for (int ti = 0; ti < msgDECRYPTED.length; ti++) {
			try {
				EncryptionThread et = new EncryptionThread(msgDECRYPTED, msgENCRYPTED, ti, highPriority, key, failedLines, this);
				et.start();
				et.join();
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "FileCrypt", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	// Void to finalize all the stuff
	public ThreadHostThread __finalize__() {
		if(this.isAlive())
			this.interrupt();
		int[] failedLinesTEMP = new int[failedLinesIndex];
		for (int i = 0; i < failedLinesIndex; i++) {
			failedLinesTEMP[i] = failedLines[i];
		}
		failedLines = failedLinesTEMP;
		failedLinesTEMP = null;
		return this;
	}
	
	// Void to get the final, encrypted message
	public String[] getFinalOutput() {
		return this.msgENCRYPTED;
	}
	
	// Void to get the failed lines array
	public int[] getFailedLines() {
		return this.failedLines;
	}
	
	// Add a failed line at index 'index'
	public void addFailedLine(int line) {
		this.failedLines[failedLinesIndex] = line;
		failedLinesIndex++;
	}
	
}
