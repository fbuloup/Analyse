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
package analyse.preferences;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.resources.CPlatform;

public final class AnalysePreferences  {
	
	public static final String ANALYSE_PROPERTIES_FILE_NAME = "analyse.properties";
	
	public static final String FIRST_LAUNCH = "FIRST_LAUNCH"; 
	
	public static final String CHARTS_ANTIALIASIS = "CHARTS_ANTIALIASIS";	
	public static final String ANALYSE_WINDOW_WIDTH = "ANALYSE_WINDOW_WIDTH";
	public static final String ANALYSE_WINDOW_HEIGHT = "ANALYSE_WINDOW_HEIGHT"; 
	public static final String ANALYSE_WINDOW_MAXIMIZED = "ANALYSE_WINDOW_MAXIMIZED"; 
	public static final String ANALYSE_WINDOW_POSITION_TOP = "ANALYSE_WINDOW_POSITION_TOP"; 
	public static final String ANALYSE_WINDOW_POSITION_LEFT = "ANALYSE_WINDOW_POSITION_LEFT"; 
	public static final String LEFT_SASH_WIDTH = "LEFT_SASH_WIDTH";
	public static final String LEFT_SASH_HEIGHT = "LEFT_SASH_HEIGHT"; 
	public static final String EDITORS_VIEW_SASH_HEIGHT = "EDITORS_VIEW_SASH_HEIGHT"; 
	public static final String SIGNALS_VIEW_SASH_HEIGHT = "SIGNALS_VIEW_SASH_HEIGHT"; 
	public static final String EVENTS_VIEW_SASH_HEIGHT = "EVENTS_VIEW_SASH_HEIGHT"; 
	public static final String CHANNELS_VIEW_VISIBILITY = "CHANNELS_VIEW_VISIBILITY"; 
	public static final String EXPERIMENTS_VIEW_VISIBILITY = "EXPERIMENTS_VIEW_VISIBILITY"; 
	public static final String CONSOLES_VIEW_VISIBILITY = "CONSOLES_VIEW_VISIBILITY"; 
	public static final String MONITOR_NUMBER = "MONITOR_NUMBER"; 
	public static final String CHEATSHEET_VIEW_WIDTH = "CHEATSHEET_VIEW_WIDTH";
	
	public static final String MATH_ENGINE = "MATH_ENGINE";
	public static final String MATH_ENGINE_EXECUTABLE_PATH = "MATH_ENGINE_EXECUTABLE_PATH";
	public static final String MATLAB_ENGINE = "Matlab";
	public static final String OCTAVE_ENGINE = "Octave";
	public static final String[][] MATH_ENGINE_LIST = new String[][]{{MATLAB_ENGINE,MATLAB_ENGINE},{OCTAVE_ENGINE,OCTAVE_ENGINE}};
	
	public static final String USE_DEFAULT_WORKSPACE_DIR = "USE_DEFAULT_WORKSPACE_DIR";
	public static final String WORKSPACES_LIST = "WORKSPACES_LIST"; 
	public static final String LAST_WORKSPACE_USED = "LAST_WORKSPACE_USED";

	public static final String LANGUAGE = "LANGUAGE";
	public static final String FRENCH_LANGUAGE = "fr_FR";
	public static final String ENGLISH_LANGUAGE = "en_US";
	public static final String[][] LANGUAGE_LIST = new String[][]{{"English","en_US"},{"Français","fr_FR"}};
	public static final String REDIRECT_CONSOLE_MESSAGES = "REDIRECT_CONSOLE_MESSAGES";
	
	public static final String ALWAYS_LOAD_FROM_SAVING_FILE = "ALWAYS_LOAD_FROM_SAVING_FILE";
	public static final String SHOW_CROSSHAIR = "SHOW_CROSSHAIR";
	public static final String SHOW_CHANNELS_PALETTE = "SHOW_CHANNELS_PALETTE";
	public static final String SHOW_MARKERS = "SHOW_MARKERS";
	public static final String SHOW_LEGEND = "SHOW_LEGEND";
	public static final String SORT_CHANNELS = "SORT_CHANNELS";
	public static final String AUTO_ADJUST_XY_AXIS = "AUTO_ADJUST_XY_AXIS";

	public static final String JAVA_HEAP_MAX = "HEAP_MAX"; 
	public static final String JAVA_HEAP_MIN = "HEAP_MIN"; 
	
	public static final String REPOSITORY_PATH = "REPOSITORY_PATH"; //$NON-NLS-1$
	
	//Function editor preferences
	public static final String FUNCTION_EDITOR_WINDOW_POSITION_LEFT = "FUNCTION_EDITOR_WINDOW_POSITION_LEFT"; //$NON-NLS-1$
	public static final String FUNCTION_EDITOR_WINDOW_POSITION_TOP = "FUNCTION_EDITOR_WINDOW_POSITION_TOP"; //$NON-NLS-1$
	public static final String FUNCTION_EDITOR_WINDOW_WIDTH = "FUNCTION_EDITOR_WINDOW_WIDTH"; //$NON-NLS-1$
	public static final String FUNCTION_EDITOR_WINDOW_HEIGHT = "FUNCTION_EDITOR_WINDOW_HEIGHT"; //$NON-NLS-1$
	public static final String FUNCTION_EDITOR_WINDOW_MAXIMIZED = "FUNCTION_EDITOR_WINDOW_MAXIMIZED"; //$NON-NLS-1$
	public static final String FIND_REPLACE_DIALOG_TOP = "FIND_REPLACE_DIALOG_TOP"; //$NON-NLS-1$
	public static final String FIND_REPLACE_DIALOG_LEFT = "FIND_REPLACE_DIALOG_LEFT"; //$NON-NLS-1$
	public static final String FIND_REPLACE_DIALOG_WIDTH = "FIND_REPLACE_DIALOG_WIDTH"; //$NON-NLS-1$
	public static final String FIND_REPLACE_DIALOG_HEIGHT = "FIND_REPLACE_DIALOG_HEIGHT"; //$NON-NLS-1$
	public static final String DEFAULT_EXPORT_DIRECTORY = "DEFAULT_EXPORT_DIRECTORY";
	public static final String SHOW_EVENTS_AS_INFINITE = "SHOW_EVENTS_AS_INFINITE";
	public static final String NEXT_PREVIOUS_ONLY_SIGNALS = "NEXT_PREVIOUS_ONLY_SIGNALS";
	public static final String COMPARE_FILE_CONTENT_DURING_UPDATE = "COMPARE_FILE_CONTENT_DURING_UPDATE";
	public static final String VERBOSE_DURING_UPDATE = "VERBOSE_DURING_UPDATE";
	

	public static final String FTP_SHARING_IP = "FTP_SHARING_IP";
	public static final String FTP_SHARING_LOGIN = "FTP_SHARING_LOGIN";

	private static IPreferenceStore preferenceStore;
	private static HashSet<String> workspacesList = new HashSet<String>(0);
	
	public static String[] matlabScriptsPath = 
		new String[]{AnalyseApplication.analyseDirectory + "matlabscripts",
					 AnalyseApplication.analyseDirectory + "matlabscripts" + File.separator + "library",
					 AnalyseApplication.analyseDirectory + "matlabscripts" + File.separator + "library" + File.separator + "extendedLibrary",
					 AnalyseApplication.analyseDirectory + "exportExtension"};
	
	//prevent instance creation
	private AnalysePreferences() {
		
	}
			
	public static void initialize()  {
		
		if(preferenceStore == null) {
			
			preferenceStore = new PreferenceStore(ANALYSE_PROPERTIES_FILE_NAME); //$NON-NLS-1$
			
			preferenceStore.setDefault(FIRST_LAUNCH, true);
			preferenceStore.setDefault(CHARTS_ANTIALIASIS, false);
			preferenceStore.setDefault(ANALYSE_WINDOW_HEIGHT, 800);
			preferenceStore.setDefault(ANALYSE_WINDOW_WIDTH, 800);
			preferenceStore.setDefault(ANALYSE_WINDOW_MAXIMIZED, false);
			preferenceStore.setDefault(ANALYSE_WINDOW_POSITION_TOP, 0);
			preferenceStore.setDefault(ANALYSE_WINDOW_POSITION_LEFT, 0);
			preferenceStore.setDefault(CHEATSHEET_VIEW_WIDTH, 25);
			preferenceStore.setDefault(LEFT_SASH_WIDTH, 25);
			preferenceStore.setDefault(LEFT_SASH_HEIGHT, 25);
			preferenceStore.setDefault(SIGNALS_VIEW_SASH_HEIGHT, 75);
			preferenceStore.setDefault(EVENTS_VIEW_SASH_HEIGHT, 75);
			preferenceStore.setDefault(EDITORS_VIEW_SASH_HEIGHT, 75);
			preferenceStore.setDefault(CHANNELS_VIEW_VISIBILITY, true);
			preferenceStore.setDefault(EXPERIMENTS_VIEW_VISIBILITY, true);
			preferenceStore.setDefault(CONSOLES_VIEW_VISIBILITY, true);
			preferenceStore.setDefault(MONITOR_NUMBER, 0);

			preferenceStore.setDefault(DEFAULT_EXPORT_DIRECTORY,"");
			
			preferenceStore.setDefault(MATH_ENGINE, MATLAB_ENGINE);
			preferenceStore.setDefault(MATH_ENGINE_EXECUTABLE_PATH, "");
			
			preferenceStore.setDefault(JAVA_HEAP_MAX, 128);
			preferenceStore.setDefault(JAVA_HEAP_MIN, 32);
			
			preferenceStore.setDefault(USE_DEFAULT_WORKSPACE_DIR, "");
			preferenceStore.setDefault(WORKSPACES_LIST, "");
			preferenceStore.setDefault(LAST_WORKSPACE_USED, "");
			
			preferenceStore.setDefault(LANGUAGE, "en_US");
			preferenceStore.setDefault(REDIRECT_CONSOLE_MESSAGES,false);

			preferenceStore.setDefault(SHOW_CROSSHAIR,true);
			preferenceStore.setDefault(SHOW_CHANNELS_PALETTE,true);
			preferenceStore.setDefault(SHOW_MARKERS,true);
			preferenceStore.setDefault(SHOW_LEGEND,false);
			preferenceStore.setDefault(SORT_CHANNELS,false);
			preferenceStore.setDefault(AUTO_ADJUST_XY_AXIS,true);
			preferenceStore.setDefault(ALWAYS_LOAD_FROM_SAVING_FILE,true);
			preferenceStore.setDefault(SHOW_EVENTS_AS_INFINITE,false);
			preferenceStore.setDefault(NEXT_PREVIOUS_ONLY_SIGNALS,true);
			
			preferenceStore.setDefault(FUNCTION_EDITOR_WINDOW_HEIGHT, 800);
			preferenceStore.setDefault(FUNCTION_EDITOR_WINDOW_WIDTH, 600);
			preferenceStore.setDefault(FUNCTION_EDITOR_WINDOW_MAXIMIZED, false);
			preferenceStore.setDefault(FUNCTION_EDITOR_WINDOW_POSITION_TOP, 0);
			preferenceStore.setDefault(FUNCTION_EDITOR_WINDOW_POSITION_LEFT, 0);
			preferenceStore.setDefault(FIND_REPLACE_DIALOG_TOP, 0);
			preferenceStore.setDefault(FIND_REPLACE_DIALOG_LEFT, 0);
			preferenceStore.setDefault(FIND_REPLACE_DIALOG_WIDTH, 0);
			preferenceStore.setDefault(FIND_REPLACE_DIALOG_HEIGHT, 0);

			preferenceStore.setDefault(FTP_SHARING_IP, "xxx.xxx.xxx.xxx");
			preferenceStore.setDefault(FTP_SHARING_LOGIN, "");
			
			String release = "windows_release/";
			if(CPlatform.isWindows64Bits()) release = "windows_64_release/";
			if(CPlatform.isLinux()) release = "linuxgtk_release/";
			if(CPlatform.isMacOSX() && CPlatform.isPPCArch()) release = "macppc_release/";
			if(CPlatform.isMacOSX() && CPlatform.isX86Arch()) release = "macintel_release/";
			preferenceStore.setDefault(REPOSITORY_PATH, "http://www.ism.univmed.fr/buloup/documents/Analyse/" + release); //$NON-NLS-1$
			preferenceStore.setDefault(COMPARE_FILE_CONTENT_DURING_UPDATE, false);
			preferenceStore.setDefault(VERBOSE_DURING_UPDATE, false);
			
			if ((new File(AnalyseApplication.analyseDirectory + ANALYSE_PROPERTIES_FILE_NAME)).exists()) {
				try {
					 ((PreferenceStore) preferenceStore).load();
				} catch (IOException e) {
					Log.logErrorMessage(e);
				}
			}
			
		    try {
		    	 ((PreferenceStore) preferenceStore).save();
			} catch (IOException e) {
				Log.logErrorMessage(e);
			}
			
			String languageValue = preferenceStore.getString(LANGUAGE);
			Locale currentLocale = new Locale(languageValue.split("_")[0],languageValue.split("_")[1]);
			Locale.setDefault(currentLocale);
			
			String[] workspaces = preferenceStore.getString(WORKSPACES_LIST).split(File.pathSeparator);
			for (int i = 0; i < workspaces.length; i++) 
				if(!workspaces[i].equals("")) 
					if((new File(workspaces[i])).exists()) workspacesList.add(workspaces[i]);
		}
	}

	public static IPreferenceStore getPreferenceStore() {
		if(preferenceStore == null) initialize();
		return preferenceStore;
	}
	
	public static void savePreferences() {
		try {
			String[] workspaces = getWorkspacesList();
			String workspacesString = "";
			for (int i = 0; i < workspaces.length; i++) workspacesString = workspacesString + workspaces[i] + File.pathSeparator;
			workspacesString = workspacesString.replaceAll(File.pathSeparator + "$", "");
			preferenceStore.putValue(WORKSPACES_LIST, workspacesString);
			((PreferenceStore) preferenceStore).save();
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}
		WorkspacePreferences.savePreferences();
	}
	
	public static void addWorkspaceToWorkspacesList(String workspace) {
		workspacesList.add(workspace);
	}
	
	public static void removeWorkspaceFromWorkspacesList(String workspace) {
		workspacesList.remove(workspace);
	}
	
	public static String[] getWorkspacesList() {
		if(preferenceStore == null) initialize();
		return workspacesList.toArray(new String[workspacesList.size()]);
	}
	
	public static String getLastWorkspaceUsed() {
		if(preferenceStore == null) initialize();
		return preferenceStore.getString(LAST_WORKSPACE_USED);
	}
	
	public static String getCurrentWorkspace() {
		return getLastWorkspaceUsed();
	}
	
	public static void setCurrentWorkspace(String workspace) {
		if(preferenceStore == null) initialize();
		preferenceStore.putValue(LAST_WORKSPACE_USED, workspace);
	}

}
