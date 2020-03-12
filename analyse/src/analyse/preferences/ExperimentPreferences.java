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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

import analyse.Log;

public final class ExperimentPreferences {

	private static final String EXPERIMENT_TYPE = "TYPE";
	private static final String SUBJECTS_LIST = "SUBJECTS_LIST";

	private IPreferenceStore preferenceStore;
	private HashSet<String> subjectsListNames = new HashSet<String>(0);
	private String experimenPropertiestFilePath;

	public ExperimentPreferences(String experimentName, String experimentType) {
		this(experimentName);
		setExperimentType(experimentType);
	}
	
	public ExperimentPreferences(String experimentName) {
		experimenPropertiestFilePath = AnalysePreferences.getCurrentWorkspace() + File.separator + experimentName + File.separator + experimentName + ".properties";
		preferenceStore = new PreferenceStore(experimenPropertiestFilePath);
		preferenceStore.setDefault(SUBJECTS_LIST, "");
		if ((new File(experimenPropertiestFilePath)).exists()) {
			try {
				((PreferenceStore) preferenceStore).load();
				String[] subjectsList = preferenceStore.getString(SUBJECTS_LIST).split(File.pathSeparator);
				for (int i = 0; i < subjectsList.length; i++) subjectsListNames.add(subjectsList[i]);
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

	public IPreferenceStore getPreferenceStore() {
		return preferenceStore;
	}
	
	public String[] getSubjectsListNames() {
		return subjectsListNames.toArray(new String[subjectsListNames.size()]);
	}

	public void savePreferences() {
		try {
			String[] subjectsListNames = getSubjectsListNames();
			String subjectsListNamesString = "";
			for (int i = 0; i < subjectsListNames.length; i++) subjectsListNamesString = subjectsListNamesString + subjectsListNames[i] + File.pathSeparator;
			subjectsListNamesString = subjectsListNamesString.replaceAll(File.pathSeparator + "$", "");
			preferenceStore.putValue(SUBJECTS_LIST, subjectsListNamesString);
			((PreferenceStore) preferenceStore).save();
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}
	}

	public void renameSubject(String oldName, String newName) {
		subjectsListNames.remove(oldName);
		subjectsListNames.add(newName);
		savePreferences();
	}

	public void setExperimentType(String experimentType) {
		preferenceStore.putValue(EXPERIMENT_TYPE, experimentType);
	}

	public String getExperimentType() {
		return preferenceStore.getString(EXPERIMENT_TYPE);
	}

	public boolean isSubject(String subjectName) {
		return subjectsListNames.contains(subjectName);
	}

	public void addSubject(String subjectName) {
		subjectsListNames.add(subjectName);
	}

	public void removeSubject(String subjectName) {
		subjectsListNames.remove(subjectName);
	}

	public boolean deletePreferenceFile() {
		return new File(experimenPropertiestFilePath).delete();
	}

	public void renamePreferencesFile(String oldName, String name) {
		String experimentType = getExperimentType();
		String oldExperimenPropertiesFilePath = AnalysePreferences.getCurrentWorkspace() + File.separator + name + File.separator + oldName + ".properties";
		String newExperimenPropertiesFilePath = AnalysePreferences.getCurrentWorkspace() + File.separator + name + File.separator + name + ".properties";
		File oldFile = new File(oldExperimenPropertiesFilePath);
		File newFile = new File(newExperimenPropertiesFilePath);
		if(oldFile.renameTo(newFile)) {
			preferenceStore = new PreferenceStore(newExperimenPropertiesFilePath);
			setExperimentType(experimentType);
			savePreferences();
			experimenPropertiestFilePath = newExperimenPropertiesFilePath;
		}
	}

}
