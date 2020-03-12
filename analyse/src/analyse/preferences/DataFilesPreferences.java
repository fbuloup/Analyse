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
package analyse.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import analyse.AnalyseApplication;
import analyse.Log;


public final class DataFilesPreferences {
	
	private final static HashMap<String, String[]> dataFilesProperties = new HashMap<String, String[]>(0);
	private static ResourceBundle dataFilesResourceBundle = null;
	
	private DataFilesPreferences() {
	}
	
	public static String getDocometreTypeString() {
		return "DOCOMETRE";
	}
	
	public static String getICETypeString() {
		return "ICE";
	}
	
	public static void initialize() {
		dataFilesResourceBundle = ResourceBundle.getBundle("dataFiles");
		Enumeration<String> keys = dataFilesResourceBundle.getKeys();
		for (Enumeration<String> k = keys ; k.hasMoreElements() ;) {
			String element = k.nextElement();
			if(element.startsWith("SYSTEM")) {
				int i = Integer.valueOf(element.replaceFirst("SYSTEM", ""));
				String system = dataFilesResourceBundle.getString(element);
				String extension = dataFilesResourceBundle.getString("EXTENSION" + i); 
			 	String description = dataFilesResourceBundle.getString("DESCRIPTION" + i); 
			 	String importMethods = dataFilesResourceBundle.getString("IMPORT_METHOD" + i); 
			 	dataFilesProperties.put(system, new String[]{extension,description, importMethods});
			}
	     }
		//Check if dataFilesExtensions exists
		File dataFilesExtension = new File(AnalyseApplication.analyseDirectory + "dataFilesExtension.properties");
		if(dataFilesExtension.exists()) {
			try {
				//If yes, add systems if do not already exist
				FileInputStream fileInputStream = new FileInputStream(dataFilesExtension);
				Properties dataFilesExtensionProperties = new Properties();
				dataFilesExtensionProperties.load(fileInputStream);
				Enumeration<Object> extensionsKeys = dataFilesExtensionProperties.keys();
				for (Enumeration<Object> k = extensionsKeys ; k.hasMoreElements() ;) {
					String element = (String) k.nextElement();
					if(element.startsWith("SYSTEM")) {
						String system = dataFilesExtensionProperties.getProperty(element);
						if(dataFilesProperties.get(system) == null) {
							int i = Integer.valueOf(element.replaceFirst("SYSTEM", ""));
							String extension = dataFilesExtensionProperties.getProperty("EXTENSION" + i); 
						 	String description = dataFilesExtensionProperties.getProperty("DESCRIPTION" + i); 
						 	String importMethods = dataFilesExtensionProperties.getProperty("IMPORT_METHOD" + i); 
						 	dataFilesProperties.put(system, new String[]{extension,description, importMethods});
						}
					}
			     }
				
			} catch (FileNotFoundException e) {
				Log.logErrorMessage(e);
			} catch (IOException e) {
				Log.logErrorMessage(e);
			}
			
		}
	}
	
	public static String[] getSystemList() {
		Set<String> systemSet = dataFilesProperties.keySet();
		
		String[] systems = new String[systemSet.size()];
		int i=0;
		for (Iterator<String> iterator = systemSet.iterator(); iterator.hasNext();) {
			systems[i] = iterator.next();
			i++;
		}
		
		return systems;
	}
	
	public static String[] getExtensionForSystem(String system) {
		return ((String[])dataFilesProperties.get(system))[0].split(","); //$NON-NLS-1$
	}
	
	public static String getDesrciptionForSystem(String system) {
		return ((String[])dataFilesProperties.get(system))[1];
	}
	
	public static String getImportMethodsForSystem(String system) {
		return ((String[])dataFilesProperties.get(system))[2];
	}
	
	private static String[] getParamFileImportMethod(String system) {
		String x=dataFilesProperties.get(system)[2];
		x = x.replaceAll(",folder\\((.)*\\)$", "");
		x = x.replaceAll("folder", "");
		x = x.replaceAll("file", "");
		x = x.replaceAll("\\)", "");
		x = x.replaceAll("\\(", "");
		return x.split(",");
	}	
	
	private static String[] getParamFolderImportMethod(String system) {
		String x=dataFilesProperties.get(system)[2];
		x = x.replaceAll("^file\\((.)*\\),", "");
		x = x.replaceAll("^folder\\(", "");
		x = x.replaceAll("folder", "");
		x = x.replaceAll("file", "");
		x = x.replaceAll("\\)", "");
		x = x.replaceAll("\\(", "");
		return x.split(",");
	}
	private static boolean checkExtensionProperty(String system, String extension, boolean folderImportMethod, String property){
		String[] paramMethod;
		if(folderImportMethod) paramMethod= getParamFolderImportMethod(system);
		else paramMethod= getParamFileImportMethod(system);
		for (int i = 0; i < paramMethod.length; i++) {
			if (paramMethod[i].endsWith(extension)) return paramMethod[i].startsWith(property);
		}
		return false;
	}
	
	public static File[] getAllowedDataFiles (File[] dataFiles, String system, boolean folderImportMethod){
		ArrayList<File> allowedDataFiles = new ArrayList<File>(0); 
		if ((folderImportMethod && isParamFolderImportMethod(system)) || (!folderImportMethod && isParamFileImportMethod(system)))
		{
			boolean exclusivity = false;
			boolean unicity = false;
			for (int i = 0; i < dataFiles.length; i++) {
				String[] y = dataFiles[i].toString().split("\\.");
				String extension = y[y.length-1];
				
				if(checkExtensionProperty(system, extension, folderImportMethod, "-")) {
					allowedDataFiles.clear();
					allowedDataFiles.add(dataFiles[i]);
					return allowedDataFiles.toArray(new File[1]);
				}
				
				if(checkExtensionProperty(system, extension, folderImportMethod, "#")) {
					if(!allowedDataFiles.isEmpty()){
						for (int j = 0; j < allowedDataFiles.size(); j++) {
							if(!dataFiles[j].toString().endsWith(extension)){
									allowedDataFiles.remove(j);
							}
						}
					}
					allowedDataFiles.add(dataFiles[i]);
					exclusivity = true;
				}
				
				if(checkExtensionProperty(system, extension, folderImportMethod, "+") && !exclusivity) {
					if (!unicity) {
						allowedDataFiles.add(dataFiles[i]);
						unicity = true;
					}
					
				}
				
				if(checkExtensionProperty(system, extension, folderImportMethod, "*")) {
					if (!exclusivity) {
						allowedDataFiles.add(dataFiles[i]);
					}
				}
			}
			return allowedDataFiles.toArray(new File[allowedDataFiles.size()]);
		}
		return dataFiles;
	}
	
	public static String getLabelFromDataFiles(File[] dataFiles) {
		StringBuffer dataFilesString = new StringBuffer();
		for (int i = 0; i < dataFiles.length; i++) {
			dataFilesString.append(dataFiles[i]+";");
		}
		return dataFilesString.toString().replaceAll(";$", "");
	}
	
	
	public static boolean isExtensionExclusive(String system, String extension, boolean folderImportMethod) {
		return checkExtensionProperty(system, extension, folderImportMethod, "#");
	}
	
	public static boolean isExtensionUnique(String system, String extension, boolean folderImportMethod) {
		return checkExtensionProperty(system, extension, folderImportMethod, "+");	
	}
	
	public static boolean isExtensionExclusiveAndUnique(String system, String extension, boolean folderImportMethod) {
		return checkExtensionProperty(system, extension, folderImportMethod, "-");	
	}
		
	
	public static boolean isParamFileImportMethod(String system) {
		String[] params = getParamFileImportMethod(system);
		if(params.length == 1) if(params[0].equals("")) return false;
		return params.length != 0;
	}
	
	public static boolean isParamFolderImportMethod(String system) {
		String[] params = getParamFolderImportMethod(system);
		if(params.length == 1) if(params[0].equals("")) return false;
		return params.length != 0;
	}
	
	public static boolean isFileImportMethod(String system) {
		return getImportMethodsForSystem(system).contains("file");
	}
	
	public static boolean isFolderImportMethod(String system) {
		return getImportMethodsForSystem(system).contains("folder");
	}
	
	public static boolean isExtensionAllowed(String system, String fileName, boolean fileImportMethod){
		if (!fileImportMethod){
			return true;
		} else {
			if (isParamFileImportMethod(system)){
				String[] y = fileName.split("\\.");
				String extension = y[y.length-1];
				for (int i = 0; i < getParamFileImportMethod(system).length; i++) {
					if (getParamFileImportMethod(system)[i].endsWith(extension)){
						return true;
					}
				}
				return false;
			} else {
				return true;
			}
		}
	}	
}
