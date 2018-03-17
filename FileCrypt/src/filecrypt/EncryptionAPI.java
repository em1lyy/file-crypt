package filecrypt;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

// Simple API for RSA encryption
public class EncryptionAPI {
	
	public static KeyPair key;
	
	// Method to generate a keypair with a specific length
	public static void gen(int length)
	{
		KeyPairGenerator keygen = null;
		try
		{
			keygen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		keygen.initialize(length);
		key = keygen.generateKeyPair();
	}
	
	// Method to encrypt a message using RSA
	public static byte[] encrypt(String message, PublicKey pk, int lineIndex, boolean useThreadHostThread, ThreadHostThread threadHostThread)
	{
		Cipher cipher = null;
		try
		{
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, pk);
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
		}
		byte[] chiffrat = null;
		try
		{
			chiffrat = cipher.doFinal(message.getBytes());
		} catch (IllegalBlockSizeException | BadPaddingException e)
		{
			e.printStackTrace();
			if (useThreadHostThread) {
				threadHostThread.addFailedLine(lineIndex);
			} else {
				FileCryptGUI.failedLines[FileCryptGUI.failedLinesArrayIndex] = lineIndex;
				FileCryptGUI.failedLinesArrayIndex += 1;
			}
			chiffrat = message.getBytes();
		}
		return chiffrat;
	}
	
	// Method to decrypt a message using RSA
	public static String decrypt(byte[] chiffrat, PrivateKey sk)
	{
		byte[] dec = null;
		Cipher cipher = null;
		try
		{
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, sk);
		} catch (NoSuchAlgorithmException e1)
		{
			e1.printStackTrace();
		} catch (NoSuchPaddingException e1)
		{
			e1.printStackTrace();
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			dec = cipher.doFinal(chiffrat);
		} catch (IllegalBlockSizeException | BadPaddingException e)
		{
			e.printStackTrace();
		}
		return new String(dec);
	}
	
}
