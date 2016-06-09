

//This is a program to count the page number in pdf files

import com.lowagie.text.pdf.PdfReader;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class PageCounter extends JFrame {

	JFrame frame = new JFrame("Pdf Page Counter");
	JLabel browseLabel = new JLabel("Click Browse or press Alt+B to select the target folder");
	JTextArea area = new JTextArea();
	JLabel startLabel = new JLabel("Click Start or press Alt+S to begin counting ");
	JButton start = new JButton("Start");
	JPanel panel = new JPanel();
	LineNumberReader reader;
	String actualDirName = "";
	String previousDirName = "";
	String fileListing = "";
	String actualFile = "";
	HashMap<Integer, String> hashPagesInDir = new HashMap<>();
	JButton targetBrowse = new JButton("Browse");
	File[] myFiles = null;
	int noPages = 0;
	int moduleCount = 1;

	public PageCounter() {

		GridBagLayout gridBagLayout = new GridBagLayout();
		GridBagConstraints gc = new GridBagConstraints();

		frame.setSize(600, 300);
		frame.setVisible(true);
		panel.setBackground((SystemColor.inactiveCaption));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel.setLayout(gridBagLayout);
		frame.add(panel);

		// adding the browse label
		gc.gridwidth = 11;
		gc.insets = new Insets(5, 5, 5, 5);
		gc.gridx = 12;
		gc.gridy = 3;
		panel.add(browseLabel, gc);
		// adding the browse button
		gc.insets = new Insets(0, 0, 5, 5);
		gc.gridx = 13;
		gc.gridy = 5;
		panel.add(targetBrowse, gc);
		// adding the start label
		gc.insets = new Insets(0, 0, 5, 5);
		gc.gridx = 13;
		gc.gridy = 7;
		panel.add(startLabel, gc);
		// adding the start button
		gc.insets = new Insets(0, 0, 5, 5);
		gc.gridx = 13;
		gc.gridy = 8;
		panel.add(start, gc);

		// adding the text area and the jscrollpane
		gc.anchor = GridBagConstraints.CENTER;
		gc.gridx = 5;
		gc.gridy = 9;
		gc.gridwidth = 30;
		gc.gridheight = 15;
		gc.insets = new Insets(10, 10, 15, 15);
		gc.weightx = 0.1;
		gc.weighty = 0.1;
		gc.fill = GridBagConstraints.BOTH;
		area.setEditable(false);
		area.setBackground(new Color(216, 191, 216));
		panel.add(new JScrollPane(area), gc);

		// set up mnemonics
		targetBrowse.setMnemonic(KeyEvent.VK_B);
		start.setMnemonic(KeyEvent.VK_S);
		targetBrowse.setBackground(new Color(123, 104, 238));
		//adding action listener
		targetBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				// For Directory
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				// For File
				// fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.setMultiSelectionEnabled(true);
				int rVal = fileChooser.showOpenDialog(null);
				myFiles = fileChooser.getSelectedFiles();
				area.setText("---------------------------------------------\n");
				if (rVal == JFileChooser.APPROVE_OPTION) {
					area.append("These folders were selected:\n\n");
					int myCount = 1;
					for (File myFile : myFiles) {
						actualDirName = myFile.getName();
						area.append(actualDirName.toString() + "\n");
						hashPagesInDir.put(myCount, actualDirName.toString());
						myCount++;
					}
				}
				area.append("---------------------------------------------\n\n");
			}

		});
		start.setBackground(new Color(123, 104, 238));
		//adding action listener to start button
		start.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				moduleCount = 1;
				if (area.getText().toString().equals("")
						|| area.getText().toString().equals("Please use the Browse button to select a folder")) {
					area.setText("Please use the Browse button to select a folder");

				} else {
					// looping through the selected directories
					for (File myFile : myFiles) {
						countPDFPagesInDir(myFile);

					}
				}
			}
		});
	}

	// summing up the page numbers and counting the pdf files
	public void countPDFPagesInDir(File dir) {

		int sum = 0;
		int fileCount = 0;

		try {
			area.append("Getting all files in " + dir.getCanonicalPath() + " including those in subdirectories\n");
		} catch (IOException e1) {
			area.append(e1.getMessage().toString());

		}

		// checking subdirectories
		List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		// listing through directories and searching for .pdf files

		for (File fileName : files) {

			if (fileName.toString().endsWith(".pdf")) {
				try {
					PdfReader document = new PdfReader(fileName.toString());
					noPages = document.getNumberOfPages();
					sum += noPages; // summing up pages
					fileCount++; // counting the pdf files

					area.append("Number of Pages in the PDF document " + fileName + " " + "is " + +noPages + "\n");
					// actualDirName = fileName.getParentFile().getName(); //
					// this was used to get the module names, moduleCount used
					// instead
				} catch (IOException e1) {
					area.append(e1.getMessage().toString());

				}
			}
		}
		// moduleCount shows which m is selected. also needed if the start
		// button is clicked more than once
		area.append("TOTAL NUMBER OF PAGES IN  " + hashPagesInDir.get(moduleCount).toUpperCase() + ": " + sum + "\n");
		area.append("THE NUMBER OF PDF FILES IN " + hashPagesInDir.get(moduleCount).toUpperCase() + ": " + fileCount
				+ "\n");
		moduleCount++;
	}

}
