/* The aim of this application is to loop through folders and subfolders on the disk.
 * identifies folders that don't contain a specific file.
* @author Kristina Nikolova 
 * 08.01.2016
 */

package com.test;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

public class ReceiptFinder {

	// Creates the variable investigatedFolder - it is the name of the subfolder
	// that we currently check for receipts
	static String investigatedFolder;

	// Creates the variable foldersWithoutReceipt - this is a list of folders
	// where the receipt file is not present
	// By default, we will add all of our checked folders to this list
	static ArrayList<String> foldersWithoutReceipt = new ArrayList<String>();

	public static void main(String[] args) {
		System.out.println("testing starts..");

		// Defines the files variable, where we will be looking for subfolders
		// and receipt files
		File[] files = new File("c://test").listFiles();

		showFiles(files);
		printResults();
		
	}

	// The code will loop through File objects, meaning files and subdirectories
	// both.
	// If it finds a directory, it will open it and loop through File objects
	// there too.
	// All of the folders, that were verified, will be added to the list:
	// foldersWithoutReceipt
	// As soon as the method finds a receipt file, it will remove the actual
	// folder's name from the list.

	public static void showFiles(File[] files) {
		for (File file : files) {
			if (file.isDirectory()) {
				foldersWithoutReceipt.add(file.getAbsolutePath());
				// System.out.println("Directory: " + file.getAbsolutePath());
				showFiles(file.listFiles());

			} else {
				// System.out.println("File: " + file.getParent());
				investigatedFolder = file.getParent();
				if (file.getName().startsWith("receip")) {
					foldersWithoutReceipt.remove(investigatedFolder);
				}
			}
		}
	}

	private static void printResults() {
		for (String folderWithoutReceipt : foldersWithoutReceipt) {
			// The following regex expression can be used to show only the
			// folder names that contain only 4 or more digits
			if (FilenameUtils.getBaseName(folderWithoutReceipt).matches("\\d+\\d+\\d+\\d+")) {
				System.out.println("This folder does not contain a receipt file: " + folderWithoutReceipt);
			}
		}
	}
}
