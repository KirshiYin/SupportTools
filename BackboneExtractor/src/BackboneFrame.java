/*
 * A program to extract xml files and ectd structure from submissions
 * @author Kristina Nikolova
 * 02.09.2015
 */

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import org.apache.commons.io.FileUtils;

public class BackboneFrame extends JFrame {

    private JPanel contentPane;
    final JTextArea outputArea = new JTextArea();
    JScrollPane scrollPane = new JScrollPane(outputArea);

    /**
     * Launch the application.
     */
    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager
                            .setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
             
                    BackboneFrame frame = new BackboneFrame();
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
    public BackboneFrame() {
        setFont(new Font("Comic Sans MS", Font.BOLD, 17));
        setTitle("Backbone Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1030, 515);
        contentPane = new JPanel();
        contentPane.setBackground(new Color(216, 191, 216));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        final JLabel sourceLabel = new JLabel(
                "Please provide the source folder path or use the browse button:");
        sourceLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 15));
        sourceLabel.setBounds(53, 56, 482, 16);

        contentPane.add(sourceLabel);
        
        final JTextArea textArea1 = new JTextArea();
        textArea1.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
        textArea1.setBackground(new Color(255, 228, 225));
        textArea1.setBounds(93, 102, 365, 36);
        contentPane.add(textArea1);

        final JLabel targetLabel = new JLabel(
                "Please provide the target folder path or use the browse button:");
        targetLabel.setFont(new Font("Comic Sans MS", Font.PLAIN, 15));
        targetLabel.setBounds(518, 56, 482, 16);
        contentPane.add(targetLabel);

        final JTextArea textArea2 = new JTextArea();
        textArea2.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
        textArea2.setBackground(new Color(255, 228, 225));
        textArea2.setBounds(558, 102, 365, 36);
        contentPane.add(textArea2);

        // final JTextArea outputArea = new JTextArea();
        outputArea.setFont(new Font("Comic Sans MS", Font.PLAIN, 15));
        outputArea.setBackground(new Color(255, 228, 225));
        outputArea.setBounds(201, 240, 542, 209);
        contentPane.add(outputArea);

        JButton sourceBrowse = new JButton("Browse");
        sourceBrowse.setBackground(new Color(123, 104, 238));
        sourceBrowse.setForeground(new Color(0, 0, 0));
        sourceBrowse.setBounds(184, 151, 97, 25);
        contentPane.add(sourceBrowse);
        sourceBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fileChooser = new JFileChooser();
                // For Directory
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                // For File
                // fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setAcceptAllFileFilterUsed(false);
                int rVal = fileChooser.showOpenDialog(null);
                if (rVal == JFileChooser.APPROVE_OPTION) {
                    textArea1.setText(fileChooser.getSelectedFile().toString());
                }
            }
        });

        JButton targetBrowse = new JButton("Browse");
        targetBrowse.setBackground(new Color(123, 104, 238));
        targetBrowse.setBounds(661, 151, 97, 25);
        contentPane.add(targetBrowse);
        targetBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fileChooser = new JFileChooser();
                // For Directory
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                // For File
                // fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setAcceptAllFileFilterUsed(false);
                int rVal = fileChooser.showOpenDialog(null);
                if (rVal == JFileChooser.APPROVE_OPTION) {
                    textArea2.setText(fileChooser.getSelectedFile().toString());
                }
            }
        });

        JButton extractButton = new JButton("Extract");
        extractButton.setFont(new Font("Yu Gothic", Font.BOLD, 13));
        extractButton.setBackground(new Color(123, 104, 238));
        extractButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                outputArea.setText("Extracting...");

                File src = new File(textArea1.getText());
                File dst = new File(textArea2.getText());
                try {
                    copyDirectory(src, dst);
                } catch (IOException e1) {
                    System.out.println("An error has occured: " + e1.getMessage().toString());
                }

            }
        });
        extractButton.setBounds(391, 175, 163, 43);
        contentPane.add(extractButton);

    }
  public void copyDirectory(File srcPath, File dstPath) throws IOException {

        if (srcPath.isDirectory()) {
            if (!dstPath.exists()) {
                dstPath.mkdir();
            }

            String files[] = srcPath.list();
            File xmlFile = null;
            File destxmlFile = null;

            for (int i = 0; i < files.length; i++) {

                if (files[i].endsWith("xml")) {

                    xmlFile = new File(srcPath + "\\" + files[i]);
                    destxmlFile = new File(dstPath + "\\" + files[i]);
                    FileUtils.copyFile(xmlFile, destxmlFile);

                }
//                System.out.println("\n" + files[i]);
//                DefaultCaret caret = (DefaultCaret) outputArea.getCaret();
//                caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
//                outputArea.setEditable(true);

                // scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                
                copyDirectory(new File(srcPath, files[i]), new File(dstPath,
                        files[i]));
 }
            if (xmlFile != null) {
            	
            	outputArea.setText("Extracted");
//                outputArea.append("\n" + xmlFile + " was copied to "
//                        + destxmlFile);
            	System.out.println("\n" + xmlFile + " was copied to "
                       + destxmlFile);
            }
        }

        System.out.println("Directory copied.");

    }

}