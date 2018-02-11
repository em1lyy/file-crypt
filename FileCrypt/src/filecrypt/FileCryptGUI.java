package filecrypt;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
	
	private File currentFile = null;
	private JLabel lblCurrentOperation;
	private JLabel lblOperations;
	private JCheckBox chckbxDeleteAfterEncrypting;
	
	private int keyLength = 2048;
	private int[] lengths = { 512, 1024, 2048, 4096 };
	private Color[] lengthColors = { Color.RED, (new Color(255, 255, 102)), (new Color(50, 205, 50)), (new Color(50, 205, 50)) };
	private int lengthIndex = 2;
	
	private int saveInterval = 1000;
	private int[] intervals = { 500, 750, 1000, 2500 };
	private int intervalIndex = 2;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
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
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(FileCryptGUI.class.getResource("/filecrypt/filecrypt_icon.png")));
		setTitle("FileCrypt");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 207);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		JLabel lblFilePath = new JLabel("File Path:");
		lblFilePath.setBounds(10, 11, 62, 14);
		contentPane.add(lblFilePath);
		
		txtFilePath = new JTextField();
		txtFilePath.setToolTipText("If you selected a File, its path will be displayed here");
		txtFilePath.setEditable(false);
		txtFilePath.setText("(no file selected)");
		txtFilePath.setBounds(10, 27, 339, 20);
		contentPane.add(txtFilePath);
		txtFilePath.setColumns(10);
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setToolTipText("Select the File you want to encrypt/delete.");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(new File("./"));
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(false);
				int status = fc.showOpenDialog(null);
				if (status == JFileChooser.APPROVE_OPTION) {
					currentFile = fc.getSelectedFile();
					txtFilePath.setText(currentFile.getAbsolutePath());
				}
			}
		});
		btnBrowse.setBounds(353, 26, 71, 23);
		contentPane.add(btnBrowse);
		
		JButton btnEncrypt = new JButton("ENCRYPT!");
		btnEncrypt.setToolTipText("Start encrypting/deleting the File.");
		btnEncrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnEncrypt.setEnabled(false);
				Thread t = new Thread( new Runnable() {
					
					@Override
					public void run() {
						if (currentFile != null) {
							lblOperations.setText("Generating key...");
							EncryptionAPI.gen(keyLength);
							lblOperations.setText("Reading File...");
							String[] msgDECRYPTED = FileAPI.readFile(currentFile);
							lblOperations.setText("Initializing encryption...");
							String[] msgENCRYPTED = new String[saveInterval];
							BufferedWriter out = null;
							try {
								out = new BufferedWriter(new FileWriter(currentFile));
							} catch (IOException e) {
								e.printStackTrace();
							}
							int index = 0;
							for (int i = 0; i < msgDECRYPTED.length; i++) {
								if (index >= saveInterval) {
									lblOperations.setText("Writing encrypted data to File...");
									FileAPI.writeFile(currentFile, msgENCRYPTED, out);
									lblOperations.setText("Clearing Cache...");
									msgENCRYPTED = new String[saveInterval];
									index = 0;
								}
								lblOperations.setText("Encrypting line " + (i + 1) + " of " + msgDECRYPTED.length + "...");
								msgENCRYPTED[index] = new String(EncryptionAPI.encrypt(msgDECRYPTED[i], EncryptionAPI.key.getPublic()));
								index++;
							}
							lblOperations.setText("Writing encrypted data to File...");
							FileAPI.writeFile(currentFile, msgENCRYPTED, out);
							if (chckbxDeleteAfterEncrypting.isSelected()) {
								lblOperations.setText("Deleting File...");
								boolean deleted = currentFile.delete();
								if (deleted == false) {
									JOptionPane.showMessageDialog(null, "ERROR: Deleting failed!");
								}
							}
							if (out != null) {
								try {
									out.close();
								} catch (IOException ex) {
									ex.printStackTrace();
								}
							}
							msgDECRYPTED = null;
							msgENCRYPTED = null;
							lblOperations.setText("Finished!");
							JOptionPane.showMessageDialog(null, "Encrypting finished!");
							btnEncrypt.setEnabled(true);
						} else {
							JOptionPane.showMessageDialog(null, "Please select a File!");
							btnEncrypt.setEnabled(true);
						}
					}
				});
				t.start();
				
			}
		});
		btnEncrypt.setFont(new Font("Tahoma", Font.PLAIN, 40));
		btnEncrypt.setBounds(10, 86, 414, 53);
		contentPane.add(btnEncrypt);
		
		lblCurrentOperation = new JLabel("Current Operation:");
		lblCurrentOperation.setToolTipText("If there are running operations, they will show up here.");
		lblCurrentOperation.setBounds(10, 58, 105, 14);
		contentPane.add(lblCurrentOperation);
		
		lblOperations = new JLabel("(no operations)");
		lblOperations.setToolTipText("If there are running operations, they will show up here.");
		lblOperations.setBounds(104, 58, 320, 14);
		contentPane.add(lblOperations);
		
		chckbxDeleteAfterEncrypting = new JCheckBox("Delete after encrypting");
		chckbxDeleteAfterEncrypting.setToolTipText("Choose if the File should be deleted after encrypting or not.");
		chckbxDeleteAfterEncrypting.setFont(new Font("Tahoma", Font.PLAIN, 10));
		chckbxDeleteAfterEncrypting.setBounds(317, 0, 127, 23);
		contentPane.add(chckbxDeleteAfterEncrypting);
		
		JLabel lblKeyLength = new JLabel("Key length:");
		lblKeyLength.setBounds(11, 150, 61, 14);
		contentPane.add(lblKeyLength);
		
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
		btnBits.setBounds(68, 146, 87, 23);
		contentPane.add(btnBits);
		
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
		btnLines.setBounds(335, 146, 89, 23);
		contentPane.add(btnLines);
		
		JLabel lblSaveInterval = new JLabel("Save interval:");
		lblSaveInterval.setBounds(267, 150, 71, 14);
		contentPane.add(lblSaveInterval);
	}
}
