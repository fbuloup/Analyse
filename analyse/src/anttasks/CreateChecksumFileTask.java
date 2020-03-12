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
package anttasks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import analyse.Log;

public class CreateChecksumFileTask extends Task {
	
	private String fromFile;
	private String shaFileName;
	
	public void setFromFile(String fromFile) {
		this.fromFile = fromFile;
	}
	public void setShaFileName(String shaFileName) {
		this.shaFileName = shaFileName;
	}
	
	@Override
	public void execute() throws BuildException {
		try {
			byte[] cs = computeCheckSum(new FileInputStream(fromFile));
			String csString = convertChecksum2String(cs);
			FileWriter fileWriter = new FileWriter(shaFileName);
			fileWriter.write(csString);
			fileWriter.close();
			System.out.println("Checksum for file '" + fromFile + "' is " + csString);
			System.out.println("SHA file '" + shaFileName + "' created");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static byte[] computeCheckSum(String url) {
		try {
			URL fileURL = new URL(url);
			InputStream inputStream = fileURL.openConnection().getInputStream();
			byte[] bytes = computeCheckSum(inputStream);
			inputStream.close();
			return bytes;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}
	
	public static byte[] computeCheckSum(InputStream inputStream) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			int bytesRead = 0; 
			byte[] dataBytes = new byte[1024];
		    while ((bytesRead = inputStream.read(dataBytes)) != -1) {
		    	messageDigest.update(dataBytes, 0, bytesRead);
		    };
		    return messageDigest.digest();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.logErrorMessage(e);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.logErrorMessage(e);
		} catch (IOException e) {
			e.printStackTrace();
			Log.logErrorMessage(e);
		}
		return new byte[0];
	}
	
	public static String convertChecksum2String(byte[] bytes) {
		StringBuffer sb = new StringBuffer("");
	    for (int i = 0; i < bytes.length; i++) {
	    	sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return sb.toString();
	}
	

}
