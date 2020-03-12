/*******************************************************************************
 * Université d’Aix Marseille (AMU) - Centre National de la Recherche Scientifique (CNRS)
 * Copyright 2014 AMU-CNRS All Rights Reserved.
 * 
 * These computer program listings and specifications, herein, are
 * the property of Université d’Aix Marseille and CNRS
 * shall not be reproduced or copied or used in whole or in part as
 * the basis for manufacture or sale of items without written permission.
 * For a license agreement, please contact:
 * <mailto: licensing@sattse.com> 
 * 
 * Author : Frank BULOUP
 * Institut des Sciences du Mouvement - franck.buloup@univ-amu.fr
 ******************************************************************************/
package analyse;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public final class ApplyAnalyseUpdate {

	private static String updateDirectory = "UPDATE" ;//+ File.separator;
	private static ArrayList<String> errorsArrayList = new ArrayList<String>(0);
	private static String currentDirectory;
	
	public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) >= 0) out.write(buffer, 0, len);
		in.close();
		out.close();
	}

	@SuppressWarnings("rawtypes")
	public static void doUnzip(String destinationDirectory, String fileAbsolutePath, String extractDirectoryPrefix) throws IOException {
		ZipFile zipFile = new ZipFile(fileAbsolutePath);
		Enumeration entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			if (entry.isDirectory()) {
				(new File(destinationDirectory + extractDirectoryPrefix + entry.getName())).mkdirs();
				continue;
			}
			copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(destinationDirectory + extractDirectoryPrefix + entry.getName())));
		}
		zipFile.close();
	}
	
	public static void main(String[] args) {
		currentDirectory = (new File(".")).getAbsolutePath().replaceAll("\\.$", "");
		File errorFile = new File(currentDirectory + "applyUpdateError.txt");
		if(errorFile.exists()) errorFile.delete();
		updateDirectory = currentDirectory + updateDirectory;
		if(CPlatform.isWindows()) updateDirectory = updateDirectory.replace("\\", "\\\\");
		File updateDirectoryFile = new File(updateDirectory);
		System.out.println("currentDirectory : " + currentDirectory);
		System.out.println("updateDirectory : " + updateDirectory);
		System.out.println("updateDirectory exists : " + updateDirectoryFile.exists());
		if(updateDirectoryFile.exists()) {
			//get zip files list from update folder
			String[] zipFilesList = updateDirectoryFile.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".zip");
				}
			});
			//Move everything in parent directory
			moveUpdatesToParentDirectory(updateDirectoryFile);
			//Unzip zip files in base directory
			System.out.println("zipFilesList length : " + zipFilesList.length);
			for (int i = 0; i < zipFilesList.length; i++) {
				try {
					System.out.println("Unzip : " + zipFilesList[i]);
					doUnzip(currentDirectory, currentDirectory + zipFilesList[i], zipFilesList[i].replaceAll("\\.\\w+$", "") + File.separator);
				} catch (IOException e) {
					e.printStackTrace();
					errorsArrayList.add("Unabled to unzip : " + updateDirectory);
				}
			}
		} else errorsArrayList.add("updateDirectory does not exist. Am I supposed to do an update ?");
		if(errorsArrayList.size() > 0) {
			try {
				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(currentDirectory + "applyUpdateError.txt"));
				String[] lines = errorsArrayList.toArray(new String[errorsArrayList.size()]);
				for (int i = 0; i < lines.length; i++) {
					bufferedWriter.write(lines[i]);
					bufferedWriter.newLine();
				}
				bufferedWriter.close();
				System.out.println("Error while trying to update analyse. See error file (applyUpdateError.txt).");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else System.out.println("Update applied with success");
	}

	private static void moveUpdatesToParentDirectory(File updateDirectoryFile) {
		String[] filesList = updateDirectoryFile.list();
		System.out.println("filesList length : " + filesList.length);
		for (int i = 0; i < filesList.length; i++) {
			File fileSrc = new File(updateDirectoryFile.getAbsolutePath() + File.separator + filesList[i]);
			if(fileSrc.isFile()) {
				System.out.println("Moving : " + fileSrc.getAbsolutePath());
				String prefix = currentDirectory + updateDirectoryFile.getAbsolutePath().replaceAll("^" + updateDirectory, "");
				File fileDest = new File(prefix + File.separator + filesList[i]);
				boolean success = true;
				if(!fileDest.exists() && !(new File(fileDest.getParent())).exists()) success = (new File(fileDest.getParent())).mkdirs();
				if(success) {
					System.out.println("rename file : " + fileSrc.getAbsolutePath());
					System.out.println("to : " + fileDest.getAbsolutePath());
					if(fileDest.exists()) success = fileDest.delete();
					if(success) success = fileSrc.renameTo(fileDest);
					else errorsArrayList.add("Unabled to delete : " + fileDest.getName());
					if (!success) errorsArrayList.add("Unabled to move : " + fileSrc.getName());
				} else errorsArrayList.add("Unabled to create dirs : " + fileDest.getParent());
				
			} else moveUpdatesToParentDirectory(fileSrc);
		}
		boolean success = updateDirectoryFile.delete();
		if (!success)  errorsArrayList.add("Unabled to remove : " + updateDirectoryFile.getAbsolutePath());
	}

}
