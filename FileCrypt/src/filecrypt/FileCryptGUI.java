package filecrypt;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class FileCryptGUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6992625420131825111L;
	private JPanel contentPane;
	private JTextField txtFilePath;
	
	// The File that has been selected by the user and the output BufferedWriter
	private File currentFile = null;
	private BufferedWriter out = null;
	
	// Some components that are needed in other Events
	private JLabel lblCurrentOperation;
	private JLabel lblOperations;
	private JCheckBox chckbxDeleteAfterEncrypting;
	private JCheckBox chckbxUseMultithreading;
	private JButton btnEncrypt;
	private JCheckBox chckbxHighpriorityEncrypting;
	private JButton btnDecrypt;
	
	// Possible key lengths and their colors on the button
	private int keyLength = 2048;
	private int[] lengths = { 512, 1024, 2048, 4096 };
	private Color[] lengthColors = { Color.RED, (new Color(255, 255, 102)), (new Color(50, 205, 50)), (new Color(50, 205, 50)) };
	private int lengthIndex = 2;
	
	// Possible save intervals
	private int saveInterval = 1000;
	private int[] intervals = { 500, 750, 1000, 2500 };
	private int intervalIndex = 2;
	
	// Failed lines
	public static int[] failedLines;
	public static int failedLinesArrayIndex = 0;
	private JCheckBox chckbxDecryptable;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// Initialize LookAndFeel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		// Start the Frame's thread
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FileCryptGUI frame = new FileCryptGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FileCryptGUI() {
		// Initializing the basics of the Frame
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(FileCryptGUI.class.getResource("/resources/filecrypt_icon.png")));
		setTitle("FileCrypt");
		setBounds(100, 100, 450, 262);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		// File path LABEL, not the text field
		JLabel lblFilePath = new JLabel("File Path:");
		lblFilePath.setBounds(10, 11, 62, 14);
		contentPane.add(lblFilePath);
		
		// THIS is the text field
		txtFilePath = new JTextField();
		txtFilePath.setToolTipText("If you selected a File, its path will be displayed here");
		txtFilePath.setEditable(false);
		txtFilePath.setText("(no file selected)");
		txtFilePath.setBounds(10, 27, 339, 20);
		contentPane.add(txtFilePath);
		txtFilePath.setColumns(10);
		
		// Browse button
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setToolTipText("Select the File you want to encrypt/delete.");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Initialize a JFileChooser
				JFileChooser fc = new JFileChooser(new File("./"));
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(false);
				int status = fc.showOpenDialog(null);
				// If user clicked 'Open', the current File will be set to the chosen File
				if (status == JFileChooser.APPROVE_OPTION) {
					currentFile = fc.getSelectedFile();
					txtFilePath.setText(currentFile.getAbsolutePath());
				}
			}
		});
		btnBrowse.setBounds(353, 26, 71, 23);
		contentPane.add(btnBrowse);
		
		// Encrypt button
		btnEncrypt = new JButton("ENCRYPT!");
		btnEncrypt.setToolTipText("Start encrypting/deleting the File.");
		btnEncrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnEncrypt.setEnabled(false);
				btnDecrypt.setEnabled(false);
				
				if (chckbxUseMultithreading.isSelected()) {
					encryptMultiThreaded();
				} else {
					encryptSingleThreaded();
				}
				
			}
		});
		btnEncrypt.setFont(new Font("Tahoma", Font.PLAIN, 40));
		btnEncrypt.setBounds(10, 86, 414, 53);
		contentPane.add(btnEncrypt);
		
		// Label that says Current Operation
		lblCurrentOperation = new JLabel("Current Operation:");
		lblCurrentOperation.setToolTipText("If there are running operations, they will show up here.");
		lblCurrentOperation.setBounds(10, 58, 105, 14);
		contentPane.add(lblCurrentOperation);
		
		// The label that displays the running operation
		lblOperations = new JLabel("(no operations)");
		lblOperations.setToolTipText("If there are running operations, they will show up here.");
		lblOperations.setBounds(104, 58, 320, 14);
		contentPane.add(lblOperations);
		
		// Checkbox for deleting after encrypting
		chckbxDeleteAfterEncrypting = new JCheckBox("Delete after encrypting");
		chckbxDeleteAfterEncrypting.setToolTipText("Choose if the File should be deleted after encrypting or not.");
		chckbxDeleteAfterEncrypting.setFont(new Font("Tahoma", Font.PLAIN, 10));
		chckbxDeleteAfterEncrypting.setBounds(317, 0, 127, 23);
		contentPane.add(chckbxDeleteAfterEncrypting);
		
		// Label that says key length
		JLabel lblKeyLength = new JLabel("Key length:");
		lblKeyLength.setBounds(10, 203, 61, 14);
		contentPane.add(lblKeyLength);
		
		// Button to change the key length
		JButton btnBits = new JButton("2048 Bits");
		btnBits.setToolTipText("Longer keys mean more security, but also longer encrypting times. Only use 4096 Bits if you've got a beast PC.");
		btnBits.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lengthIndex++;
				if (lengthIndex == lengths.length) {
					lengthIndex = 0;
				}
				keyLength = lengths[lengthIndex];
				btnBits.setText(keyLength + " Bits");
				btnBits.setForeground(lengthColors[lengthIndex]);
			}
		});
		btnBits.setForeground((new Color(50, 205, 50)));
		btnBits.setBounds(67, 199, 87, 23);
		contentPane.add(btnBits);
		
		// Button to change the saving interval
		JButton btnLines = new JButton("1000 Lines");
		btnLines.setToolTipText("A shorter save interval means less RAM usage, but also longer encrypting times, depending on the speed of your drive where the file you delete/encrypt is located.");
		btnLines.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				intervalIndex++;
				if (intervalIndex == intervals.length) {
					intervalIndex = 0;
				}
				saveInterval = intervals[intervalIndex];
				btnLines.setText(saveInterval + " Lines");
			}
		});
		btnLines.setBounds(334, 199, 89, 23);
		contentPane.add(btnLines);
		
		// Label that says Save interval
		JLabel lblSaveInterval = new JLabel("Save interval:");
		lblSaveInterval.setBounds(266, 203, 71, 14);
		contentPane.add(lblSaveInterval);
		
		// Checkbox for using multi-threading
		chckbxUseMultithreading = new JCheckBox("Use Multi-Threading");
		chckbxUseMultithreading.setToolTipText("Decide if you want to use multi-threaded encryption");
		chckbxUseMultithreading.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				chckbxHighpriorityEncrypting.setEnabled(chckbxUseMultithreading.isSelected());
				
			}
		});
		chckbxUseMultithreading.setFont(new Font("Tahoma", Font.PLAIN, 10));
		chckbxUseMultithreading.setBounds(191, 0, 127, 23);
		contentPane.add(chckbxUseMultithreading);
		
		// Checkbox for High-Priority-Encryption (More efficient) !! Single thread always has highest priority !! 
		chckbxHighpriorityEncrypting = new JCheckBox("High-Priority Encrypting");
		chckbxHighpriorityEncrypting.setToolTipText("Decide if the encryption threads will get high or normal priority");
		chckbxHighpriorityEncrypting.setEnabled(false);
		chckbxHighpriorityEncrypting.setFont(new Font("Tahoma", Font.PLAIN, 10));
		chckbxHighpriorityEncrypting.setBounds(52, 0, 137, 23);
		contentPane.add(chckbxHighpriorityEncrypting);
		
		chckbxDecryptable = new JCheckBox("Decryptable");
		chckbxDecryptable.setToolTipText("Decide if the File is decryptable later using FCRSA");
		chckbxDecryptable.setBounds(160, 199, 97, 23);
		contentPane.add(chckbxDecryptable);
		
		btnDecrypt = new JButton("DECRYPT!");
		btnDecrypt.addActionListener(new ActionListener() {
			@SuppressWarnings("unused")
			public void actionPerformed(ActionEvent arg0) {
				Thread decryptionThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						btnDecrypt.setEnabled(false);
						btnEncrypt.setEnabled(false);
						BufferedReader in = null;
						BufferedWriter out = null;
						try {
							in = new BufferedReader(new FileReader(currentFile));
							lblOperations.setText("Checking Filetype...");
							if (in.readLine().equalsIgnoreCase("#FILETYPE FCRSA")) {
								lblOperations.setText("Reading public key...");
								int keyLength = Integer.parseInt(in.readLine().substring(11));
								String pubKey = in.readLine().substring(8);
								String line = in.readLine();
								lblOperations.setText("Reading private key...");
								if (!line.equalsIgnoreCase("#PRIVATE NULL")) {
									String privKey = line.substring(9);
									line = in.readLine();
									lblOperations.setText("Reading Failed Lines...");
									if (!line.equalsIgnoreCase("#FAILEDLINES NULL")) {
										String failedLinesString = line.substring(13, line.length() - 1);
										String[] failedLinesArrayString = failedLinesString.split(",");
										int[] failedLines = new int[failedLinesArrayString.length];
										int index = 0;
										for (String fLine : failedLinesArrayString) {
											failedLines[index] = Integer.parseInt(fLine);
										}
										lblOperations.setText("Generating private Key...");
										byte[] encodedPrivateKey = privKey.getBytes();
										KeyFactory keyFactory = KeyFactory.getInstance("RSA");
										PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
										PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
										lblOperations.setText("Generating public Key...");
										byte[] encodedPublicKey = pubKey.getBytes();
										keyFactory = KeyFactory.getInstance("RSA");
										X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
										PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
										EncryptionAPI.key = new KeyPair(publicKey, privateKey);
										in.readLine();
										lblOperations.setText("Reading file...");
										ArrayList<String> lines = new ArrayList<String>();
										while((line = in.readLine()) != null) {
											lines.add(line);
										}
										String[] linesDecrypted = new String[lines.size()];
										index = 0;
										for (String s : lines) {
											boolean decrypt = true;
											for (int failLine : failedLines) {
												if (index == failLine) {
													decrypt = false;
													break;
												}
											}
											if (decrypt)
												linesDecrypted[index] = EncryptionAPI.decrypt(s.getBytes(), privateKey);
											else
												linesDecrypted[index] = s;
											index++;
										}
										out = new BufferedWriter(new FileWriter(currentFile));
										for (String str : linesDecrypted) {
											out.write(str);
											out.newLine();
										}
									} else {
										lblOperations.setText("Generating private Key...");
										byte[] encodedPrivateKey = privKey.getBytes();
										KeyFactory keyFactory = KeyFactory.getInstance("RSA");
										PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
										PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
										byte[] encodedPublicKey = pubKey.getBytes();
										keyFactory = KeyFactory.getInstance("RSA");
										lblOperations.setText("Generating public Key...");
										X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
										PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
										EncryptionAPI.key = new KeyPair(publicKey, privateKey);
										in.readLine();
										lblOperations.setText("Reading file...");
										ArrayList<String> lines = new ArrayList<String>();
										while((line = in.readLine()) != null) {
											lines.add(line);
										}
										String[] linesDecrypted = new String[lines.size()];
										int index = 0;
										for (String s : lines) {
											lblOperations.setText("Decrypting line " + (index + 1) + " of " + lines.size() + "...");
											linesDecrypted[index] = EncryptionAPI.decrypt(s.getBytes(), privateKey);
											index++;
										}
										lblOperations.setText("Writing decrypted Data to file...");
										out = new BufferedWriter(new FileWriter(currentFile));
										for (String str : linesDecrypted) {
											out.write(str);
											out.newLine();
										}
									}
									JOptionPane.showMessageDialog(null, "Decrypting finished!", "FileCrypt", JOptionPane.INFORMATION_MESSAGE);
								} else {
									JOptionPane.showMessageDialog(null, "Error: File is not decryptable", "FileCrypt", JOptionPane.ERROR_MESSAGE);
									return;
								}
							} else {
								JOptionPane.showMessageDialog(null, "Error: File is not FCRSA-encoded", "FileCrypt", JOptionPane.ERROR_MESSAGE);
								return;
							}
						} catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
							JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "FileCrypt", JOptionPane.ERROR_MESSAGE);
						} finally {
							if (in != null) {
								try {
									in.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							if (out != null) {
								try {
									out.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
						
					}
				});
				decryptionThread.start();
				
			}
		});
		btnDecrypt.setToolTipText("Start decrypting/restoring the File.");
		btnDecrypt.setFont(new Font("Tahoma", Font.PLAIN, 40));
		btnDecrypt.setBounds(10, 139, 414, 53);
		contentPane.add(btnDecrypt);
	}
	
	// Method to encrypt with a single thread
	private void encryptSingleThreaded() {
		Thread t = new Thread( new Runnable() {
			
			@Override
			public void run() {
				if (currentFile != null) {
					// Generating RSA key
					lblOperations.setText("Generating key...");
					EncryptionAPI.gen(keyLength);
					// Initializing String arrays
					lblOperations.setText("Reading File...");
					String[] msgDECRYPTED = FileAPI.readFile(currentFile);
					failedLines = new int[msgDECRYPTED.length];
					lblOperations.setText("Initializing encryption...");
					String[] msgENCRYPTED = new String[saveInterval];
					// Creating a BufferedWriter
					BufferedWriter out = null;
					try {
						out = new BufferedWriter(new FileWriter(currentFile));
					} catch (IOException e) {
						e.printStackTrace();
					}
					int index = 0;
					// Do some FCRSA stuff
					try {
						out.write("#FILETYPE FCRSA");
						out.newLine();
						out.write("#KEYLENGTH " + keyLength);
						out.newLine();
						out.write("#PUBLIC " + EncryptionAPI.key.getPublic().toString());
						out.newLine();
						if (chckbxDecryptable.isSelected()) {
							out.write("#PRIVATE " + EncryptionAPI.key.getPrivate().toString());
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
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "FileCrypt", JOptionPane.ERROR_MESSAGE);
					}
					for (int i = 0; i < msgDECRYPTED.length; i++) {
						// If save interval is reached, pre-save the file
						if (index >= saveInterval) {
							lblOperations.setText("Writing encrypted data to File...");
							FileAPI.writeFile(currentFile, msgENCRYPTED, out);
							lblOperations.setText("Clearing Cache...");
							msgENCRYPTED = new String[saveInterval];
							index = 0;
						}
						lblOperations.setText("Encrypting line " + (i + 1) + " of " + msgDECRYPTED.length + "...");
						// Encrypt directly from EncryptionAPI class
						msgENCRYPTED[index] = new String(EncryptionAPI.encrypt(msgDECRYPTED[i], EncryptionAPI.key.getPublic(), i, false, null));
						index++;
					}
					lblOperations.setText("Writing encrypted data to File...");
					// Writing last lines
					FileAPI.writeFile(currentFile, msgENCRYPTED, out);
					// If delete checkbox is selected, delete the File
					if (chckbxDeleteAfterEncrypting.isSelected()) {
						lblOperations.setText("Deleting File...");
						boolean deleted = currentFile.delete();
						if (deleted == false) {
							JOptionPane.showMessageDialog(null, "ERROR: Deleting failed!");
						}
					}
					// Close BufferedWriter if it isn't null
					if (out != null) {
						try {
							out.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
					// Reset the ArrayLists (RAM Optimization)
					msgDECRYPTED = null;
					msgENCRYPTED = null;
					lblOperations.setText("Finished!");
					JOptionPane.showMessageDialog(null, "Encrypting finished!");
					btnEncrypt.setEnabled(true);
				} else { // If the user didn't select a file, show him/her a dialog which tells him zo select a file
					JOptionPane.showMessageDialog(null, "Please select a File!");
					btnEncrypt.setEnabled(true);
				}
				btnDecrypt.setEnabled(true);
			}
		});
		// Give thread the maximal possible priority so single threading is'nt completely crap
		t.setPriority(Thread.MAX_PRIORITY);
		// Start encrypting!!
		t.start();
	}
	
	// Method to encrypt with multiple threads
	private void encryptMultiThreaded() {
		Thread t = new Thread( new Runnable() {
			
			@Override
			public void run() {
				if (currentFile != null) {
					// Generating RSA key
					lblOperations.setText("Generating key...");
					EncryptionAPI.gen(keyLength);
					// Initializing String array
					lblOperations.setText("Reading File...");
					String[] msgDECRYPTED = FileAPI.readFile(currentFile);
					lblOperations.setText("Initializing encryption...");
					// Initializing BufferedWriter
					try {
						out = new BufferedWriter(new FileWriter(currentFile));
					} catch (IOException e) {
						e.printStackTrace();
					}
					lblOperations.setText("Starting encryption thread host...");
					// Creating and starting the Encryption thread host.
					ThreadHostThread tht = new ThreadHostThread(msgDECRYPTED, EncryptionAPI.key, chckbxHighpriorityEncrypting.isSelected(), keyLength, false);
					tht.start();
					while(tht.isAlive()) {
						try {
							Thread.sleep(500L);
						} catch (InterruptedException e) {
							JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "FileCrypt", JOptionPane.ERROR_MESSAGE);
						}
					}
					tht.__finalize__();
					failedLines = tht.getFailedLines();
					lblOperations.setText("Writing encrypted data to File...");
					WritingThread wt = new WritingThread(tht.getFinalOutput(), out, chckbxHighpriorityEncrypting.isSelected(), EncryptionAPI.key, keyLength, chckbxDecryptable.isSelected(), failedLines);
					wt.start();
					try {
						wt.join();
					} catch (InterruptedException e) {
						JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "FileCrypt", JOptionPane.ERROR_MESSAGE);
					}
					
				} else { // If the user didn't select a file, show him/her a dialog which tells him to select a file
					JOptionPane.showMessageDialog(null, "Please select a File!");
					btnEncrypt.setEnabled(true);
				}
			}
		});
		// Start encrypting!!
		t.start();
		// Wait for Encryption Host thread to die (Check twice a second)
		while(t.isAlive()) {
			try {
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// If delete checkbox is selected, delete the File
		if (chckbxDeleteAfterEncrypting.isSelected()) {
			lblOperations.setText("Deleting File...");
			boolean deleted = currentFile.delete();
			if (deleted == false) {
				JOptionPane.showMessageDialog(null, "ERROR: Deleting failed!");
			}
		}
		lblOperations.setText("Finished!");
		JOptionPane.showMessageDialog(null, "Encrypting finished!");
		btnEncrypt.setEnabled(true);
		btnDecrypt.setEnabled(true);
		// Close BufferedWriter if it isn't null
		if (out != null) {
			try {
				out.flush();
				out.close();
				out = null;
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
