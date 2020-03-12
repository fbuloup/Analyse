/*******************************************************************************
 * Universit� d�Aix Marseille (AMU) - Centre National de la Recherche Scientifique (CNRS)
 * Copyright 2014 AMU-CNRS All Rights Reserved.
 * 
 * These computer program listings and specifications, herein, are
 * the property of Universit� d�Aix Marseille and CNRS
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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import analyse.resources.CPlatform;
import analyse.resources.Messages;
import anttasks.CreateChecksumFileTask;

public final class Utils {
	
	public static final void copyFile(File srcFile, File destFile) throws IOException {
		if(srcFile.isFile()) {
			InputStream inputStream = new FileInputStream(srcFile);
			OutputStream outputStream = new FileOutputStream(destFile);
			byte[] byteBuffer = new byte[1024];
			int length;
			while((length = inputStream.read(byteBuffer)) > 0) outputStream.write(byteBuffer, 0, length);
			inputStream.close();
			outputStream.close();
		}
	}
	
	public static double[] readDoubleBinaryDataFile(String absoluteFileName) {
		double[] dataArray;
		try {
			File file = new File(absoluteFileName);
			InputStream is = new FileInputStream(file);
			DataInputStream dis = new DataInputStream( is );
			long length = file.length();
			dataArray = new double[(int)(length / 4)];
			if (length > Integer.MAX_VALUE) {
				dis.close();
				throw new IOException(Messages.getString("Utils.FileTooLArge") + file.getName());
			}
			else {
				byte[] bytes = new byte[(int)length];
				int offset = 0;
				int numRead = 0;
				while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length-offset) ) >= 0) offset += numRead;
				if (offset < bytes.length) {
					dis.close();
					throw new IOException(Messages.getString("Utils.ReadUncomplete") + file.getName());
				}
				dis.close();
				is.close();
				int index = 0;
				for (int start = 0; start < offset; start = start + 4) {
					dataArray[index] = arr2float(bytes, start);
					index++;
				}
			}
		} catch (Exception e) {
			Log.logErrorMessage(e);
			dataArray = new double[0];
		}
		return dataArray;
	}
	
	public static int[] readIntegerBinaryDataFile(String absoluteFileName) {
		int[] dataArray;
		try {
			File file = new File(absoluteFileName);
			InputStream is = new FileInputStream(file);
			DataInputStream dis = new DataInputStream( is );
			long length = file.length();
			dataArray = new int[(int)(length / 4)];
			if (length > Integer.MAX_VALUE) {
				dis.close();
				throw new IOException(Messages.getString("Utils.FileTooLArge") + file.getName());
			}
			else {
				byte[] bytes = new byte[(int)length];
				int offset = 0;
				int numRead = 0;
				while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length-offset) ) >= 0) offset += numRead;
				if (offset < bytes.length) {
					dis.close();
					throw new IOException(Messages.getString("Utils.ReadUncomplete") + file.getName());
				}
				dis.close();
				is.close();
				int index = 0;
				for (int start = 0; start < offset; start = start + 4) {
					dataArray[index] = (int)arr2float(bytes, start);
					index++;
				}
			}
		} catch (Exception e) {
			Log.logErrorMessage(e);
			dataArray = new int[0];
		}
		return dataArray;
	}
	
	public static float arr2float (byte[] arr, int start) {
		int i = 0;
		int len = 4;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for (i = start; i < (start + len); i++) {
			int index = cnt;
			if(CPlatform.isPPCArch()) index  = len - 1 - index;
			tmp[index] = arr[i];
			cnt++;
		}
		int accum = 0;
		i = 0;
		for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) {
			accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
			i++;
		}
		return Float.intBitsToFloat(accum);
	}
	
	public static int getMaximum(int[] intArray) {
		int maximum = intArray[0];
		for (int i = 1; i < intArray.length; i++) if(intArray[i] > maximum) maximum = intArray[i];
		return maximum;
	}
	
	public static byte[] computeCheckSum(InputStream inputStream) {
		return CreateChecksumFileTask.computeCheckSum(inputStream);
	}
	
	public static byte[] computeCheckSum(String urlName) {
		return CreateChecksumFileTask.computeCheckSum(urlName);
	}
	
	public static String convertChecksum2String(byte[] bytes) {
		return CreateChecksumFileTask.convertChecksum2String(bytes);
	}

	public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) >= 0) out.write(buffer, 0, len);
		in.close();
		out.close();
	}

	public static void doUnzip(String destinationDirectory, String fileAbsolutePath, String extractDirectoryPrefix) throws IOException {
		Log.logMessage("Zip file : " + fileAbsolutePath);
		Log.logMessage("destinationDirectory : " + destinationDirectory);
		Log.logMessage("extractDirectoryPrefix : " + extractDirectoryPrefix);
		ZipFile zipFile = new ZipFile(fileAbsolutePath);
		@SuppressWarnings("rawtypes")
		Enumeration entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			Log.logMessage("Extracting : " + entry.getName());
			if (entry.isDirectory()) {
				if(!(new File(destinationDirectory + extractDirectoryPrefix + entry.getName())).mkdirs())
					Log.logErrorMessage(Messages.getString("Utils.FailedToCreateDirectories") + destinationDirectory + extractDirectoryPrefix + entry.getName());
				continue;
			}
			copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(destinationDirectory + extractDirectoryPrefix + entry.getName())));
		}
		zipFile.close();
	}

	
}
