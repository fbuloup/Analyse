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
import java.io.IOException;
import java.util.HashSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import analyse.Log;
import analyse.resources.Messages;

public class WorkspacePreferences {
	
	//private static final String CLOSED_EXPERIMENTS_LIST = "CLOSED_EXPERIMENTS_LIST";
	private static final String EXPERIMENTS_LIST = "EXPERIMENTS_LIST";

	private static IPreferenceStore preferenceStore;
	private static HashSet<String> experimentsListNames = new HashSet<String>(0);
	//private static HashSet<String> closedExperimentsListNames = new HashSet<String>(0);
	
	//prevent instance creation
	private WorkspacePreferences() {
	}
	
	public static void initialize(String workspacePath) {
		String workspacePropertiesFilePath = workspacePath + File.separator + "workspace.properties";
		
		preferenceStore = new PreferenceStore(workspacePropertiesFilePath); 
		//preferenceStore.setDefault(CLOSED_EXPERIMENTS_LIST, ""); 
		preferenceStore.setDefault(EXPERIMENTS_LIST, "");
		
		if((new File(workspacePropertiesFilePath)).exists()) {
			try {
				 ((PreferenceStore) preferenceStore).load();
				 String[] experimentsListNamesString = preferenceStore.getString(EXPERIMENTS_LIST).split(File.pathSeparator);
				 for (int i = 0; i < experimentsListNamesString.length; i++) 
					 if(!experimentsListNamesString[i].equals(""))
						 if(new File(AnalysePreferences.getCurrentWorkspace() + File.separator + experimentsListNamesString[i]).exists())
							 experimentsListNames.add(experimentsListNamesString[i]);
						 else {
							 StringBuffer message = new StringBuffer();
							 message.append(Messages.getString("WorkspacePreferences.ExperimentFolderErrorMessage1"));
							 message.append(experimentsListNamesString[i]);
							 message.append(Messages.getString("WorkspacePreferences.ExperimentFolderErrorMessage2"));
							 Shell shell = Display.getCurrent().getActiveShell()!=null?Display.getCurrent().getActiveShell():Display.getDefault().getActiveShell();
							 MessageDialog.openError(shell, Messages.getString("WorkspacePreferences.ExperimentFolderErrorTitle"), message.toString());
							 Log.logErrorMessage(message.toString());
						 }
//				 String[] closedExperimentsListNamesString = preferenceStore.getString(CLOSED_EXPERIMENTS_LIST).split(File.pathSeparator);
//				 for (int i = 0; i < closedExperimentsListNamesString.length; i++) closedExperimentsListNames.add(closedExperimentsListNamesString[i]);
			} catch (IOException e) {
				Log.logErrorMessage(e);
			}
		}
	    
	    try {
	    	 ((PreferenceStore) preferenceStore).save();
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}
	}
	
	public static IPreferenceStore getPreferenceStore() {
		return preferenceStore;
	}
	
	public static String[] getExperimentsListNames() {
		return experimentsListNames.toArray(new String[experimentsListNames.size()]);
	}
	
//	public static String[] getClosedExperimentsListNames() {
//		return closedExperimentsListNames.toArray(new String[closedExperimentsListNames.size()]);
//	}
	
	public static void savePreferences() {
		if(preferenceStore != null){
			
			String[] experimentsList = getExperimentsListNames();
			String experimentsListString = "";
			for (int i = 0; i < experimentsList.length; i++) experimentsListString = experimentsListString + experimentsList[i] + File.pathSeparator;
			experimentsListString = experimentsListString.replaceAll(File.pathSeparator + "$", "");
			experimentsListString = experimentsListString.replaceAll("^" + File.pathSeparator, "");
			preferenceStore.putValue(EXPERIMENTS_LIST, experimentsListString);
			
//			String[] closedExperimentsList = getClosedExperimentsListNames();
//			String closedExperimentsListString = "";
//			for (int i = 0; i < closedExperimentsList.length; i++) closedExperimentsListString = closedExperimentsListString + closedExperimentsList[i] + File.pathSeparator;
//			closedExperimentsListString = closedExperimentsListString.replaceAll(File.pathSeparator + "$", "");
//			closedExperimentsListString = closedExperimentsListString.replaceAll("^" + File.pathSeparator, "");
//			preferenceStore.putValue(CLOSED_EXPERIMENTS_LIST, closedExperimentsListString);
			
			try {
				((PreferenceStore) preferenceStore).save();
			} catch (IOException e) {
				Log.logErrorMessage(e);
			}
		}
	}
	
	public static void removeExperiment(String experimentName) {
		experimentsListNames.remove(experimentName);
	}
	
	public static void addExperiment(String experimentName) {
		experimentsListNames.add(experimentName);
	}

//	public static void addExperimentToClosedExperimentsList(String experimentName) {	
//		closedExperimentsListNames.add(experimentName);
//	}
//	
//	public static void removeExperimentFromClosedExperimentsList(String experimentName) {
//		closedExperimentsListNames.remove(experimentName);
//	}
//	
//	public static boolean isExperimentClosed(String experimentName) {
//		return closedExperimentsListNames.contains(experimentName);
//	}

	public static void renameExperiment(String oldName, String name) {
		experimentsListNames.remove(oldName);
		experimentsListNames.add(name);
		savePreferences();
	}
	
}
