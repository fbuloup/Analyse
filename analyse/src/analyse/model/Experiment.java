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
package analyse.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;

import analyse.Log;
import analyse.preferences.AnalysePreferences;
import analyse.preferences.ExperimentPreferences;
import analyse.preferences.WorkspacePreferences;
import analyse.resources.CPlatform;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class Experiment implements IResource {
	
	private String name;
	private String type;	
	private String oldName;
	private ExperimentPreferences experimentPreferences;
	private ArrayList<IResource> resourcesArrayList = new ArrayList<IResource>(0);

	public Experiment(String experimentName, String experimentType, Hashtable<String, String[]> subjectsAndDataFiles, IProgressMonitor progressMonitor) {
		progressMonitor.subTask(Messages.getString("Experiment.Create") + experimentName);
		name = experimentName;
		oldName = name;
		type = experimentType;
		experimentPreferences = new ExperimentPreferences(experimentName,experimentType);
		experimentPreferences.savePreferences();
		WorkspacePreferences.addExperiment(experimentName);
		WorkspacePreferences.savePreferences();
		for (Enumeration<String> subjectsNames = subjectsAndDataFiles.keys() ; subjectsNames.hasMoreElements() ;)
	         experimentPreferences.addSubject(subjectsNames.nextElement());
		experimentPreferences.savePreferences();
		populateResourcesList();
//		populateSubjectsList();
		progressMonitor.worked(1);
	}
	
	public Experiment(String experimentName) {
		name = experimentName;
		oldName = name;
		experimentPreferences = new ExperimentPreferences(name);
		type = experimentPreferences.getExperimentType();
		populateResourcesList();
//		populateSubjectsList();
//		populateFoldersList();
//		populateChartsList();
//		populateProcessList();
	}
	
	public ExperimentPreferences getPreferences() {
		return experimentPreferences;
	}
	
	private IResource[] populateResourcesList() {
		Experiments.deleteDS_Stores(getAbsolutePath());
		ArrayList<IResource> addedResources = new ArrayList<IResource>(0);
		File file = new File(getAbsolutePath());	
		File[] filesList = file.listFiles();
		for (int i = 0; i < filesList.length; i++) {
			
			if(filesList[i].isDirectory() && experimentPreferences.isSubject(filesList[i].getName())) 
				if(!isResourceNameExist(filesList[i].getName())) {
					Subject subject = new Subject(this, filesList[i].getName()) ;
					resourcesArrayList.add(subject);
					addedResources.add(subject);
				}
			
			if(filesList[i].isDirectory() && filesList[i].getAbsolutePath().endsWith(Folder.EXTENSION)) 
				if(!isResourceNameExist(filesList[i].getName())) {
					Folder folder = new Folder(this, filesList[i].getName()) ;
					resourcesArrayList.add(folder);
					addedResources.add(folder);
				}
			
			if(filesList[i].isFile() && filesList[i].getAbsolutePath().endsWith(Chart.EXTENSION)) 
				if(!isResourceNameExist(filesList[i].getName())) {
					try {
						Chart chart = new Chart(this, filesList[i].getName()) ;
						resourcesArrayList.add(chart);
						addedResources.add(chart);
					} catch (Exception e) {
						MultiCharts multiCharts = new MultiCharts(this, filesList[i].getName()) ;
						resourcesArrayList.add(multiCharts);
						addedResources.add(multiCharts);
					}
					
				}
			
			if(filesList[i].isFile() && filesList[i].getAbsolutePath().endsWith(Processing.EXTENSION)) 
				if(!isResourceNameExist(filesList[i].getName())) {
					Processing processing = new Processing(this, filesList[i].getName()) ;
					resourcesArrayList.add(processing);
					addedResources.add(processing);
				}
			
			if(filesList[i].isFile() && filesList[i].getAbsolutePath().endsWith(Note.EXTENSION)) 
				if(!isResourceNameExist(filesList[i].getName())) {
					Note note = new Note(this, filesList[i].getName()) ;
					resourcesArrayList.add(note);
					addedResources.add(note);
				}
			
		}
		return addedResources.toArray(new IResource[addedResources.size()]);
	}
	
//	private IResource[] populateFoldersList() {
//		ArrayList<IResource> addedResources = new ArrayList<IResource>(0);
//		File file = new File(getAbsolutePath());	
//		File[] filesList = file.listFiles();
//		for (int i = 0; i < filesList.length; i++) {
//			if(filesList[i].isDirectory() && filesList[i].getAbsolutePath().endsWith(Folder.EXTENSION)) 
//				if(!isResourceNameExist(filesList[i].getName())) {
//					Folder folder = new Folder(this, filesList[i].getName()) ;
//					resourcesArrayList.add(folder);
//					addedResources.add(folder);
//				}
//		}
//		return addedResources.toArray(new IResource[addedResources.size()]);
//	}
//	
//	private IResource[] populateSubjectsList() {
//		ArrayList<IResource> addedResources = new ArrayList<IResource>(0);
//		File file = new File(getAbsolutePath());	
//		File[] filesList = file.listFiles();
//		for (int i = 0; i < filesList.length; i++) 
//			if(filesList[i].isDirectory() && experimentPreferences.isSubject(filesList[i].getName())) 
//				if(!isResourceNameExist(filesList[i].getName())) {
//					Subject subject = new Subject(this, filesList[i].getName()) ;
//					resourcesArrayList.add(subject);
//					addedResources.add(subject);
//				}
//		return addedResources.toArray(new IResource[addedResources.size()]);
//	}
	
	public String getAbsolutePath() {
		return AnalysePreferences.getCurrentWorkspace() + File.separator + getName();
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getOldName() {
		return oldName;
	}
	
	public Image getImage() {
		return ImagesUtils.getImage(IImagesKeys.EXPERIMENT_ICON);
	}

	public IResource[] getChildren() {
		return resourcesArrayList.toArray(new IResource[resourcesArrayList.size()]);
	}

	public IResource getParent() {
		return Experiments.getInstance();
	}

	public boolean hasChildren() {
		return resourcesArrayList.size() > 0;
	}

	public void addNewFolder(String folderName) {
		resourcesArrayList.add(new Folder(this,folderName));
	}

	public boolean isResourceNameExist(String name) {
		for (int i = 0; i < resourcesArrayList.size(); i++) if(resourcesArrayList.get(i).getName().equals(name)) return true;
		return false;
	}

	public void addNewSubjectsByNames(String[] subjectsNames, IProgressMonitor progressMonitor) {
		for (int i = 0; i < subjectsNames.length; i++) {
			if(progressMonitor != null )progressMonitor.subTask(Messages.getString("Experiment.AddSubject") + subjectsNames[i]);
			experimentPreferences.addSubject(subjectsNames[i]);
		}
		experimentPreferences.savePreferences();
		Experiments.notifyObservers(IResourceObserver.SUBJECT_CREATED, populateResourcesList());
	}

	public boolean delete() {
		boolean succeed = true;
		while(resourcesArrayList.size() >0) {
			succeed = succeed && resourcesArrayList.get(0).delete();
			if(!succeed) break;
		}
		if(succeed) {
			succeed = experimentPreferences.deletePreferenceFile();
			if(succeed) {
				if(CPlatform.isMacOSX()) succeed = Experiments.deleteDS_Stores(getAbsolutePath());
				if(succeed) {
					succeed = new File(getAbsolutePath()).delete();
					if(succeed) {
						getParent().remove(this);
						WorkspacePreferences.removeExperiment(name);
						WorkspacePreferences.savePreferences();
					}
				}
			}
		}
		return succeed;
	}

	public void remove(IResource resource) {
		resourcesArrayList.remove(resource);
		if(resource instanceof Subject) {
			experimentPreferences.removeSubject(resource.getName());
			experimentPreferences.savePreferences();
		}
	}

	public boolean hasParent(IResource parentResource) {
		IResource parent = getParent();
		while(parent != null) {
			if(parent == parentResource) return true;
			parent = parent.getParent();
		}
		return false;
	}

	public void rename(String newName) {
		String oldOldName = oldName;
		File oldFile = new File(getAbsolutePath());
		if(!oldName.equals(name)) oldName = name;
		name = newName;
		File newFile = new File(getAbsolutePath());
		if(!oldFile.renameTo(newFile)) {
			name = oldName;
			oldName = oldOldName;
			Log.logErrorMessage(Messages.getString("Experiment.ImpossibleRename") + getAbsolutePath());
		} else {
			experimentPreferences.renamePreferencesFile(oldName,name);
			WorkspacePreferences.renameExperiment(oldName,name);
			for (int i = 0; i < resourcesArrayList.size(); i++) 
				if(resourcesArrayList.get(i) instanceof Subject)
					if(((Subject)resourcesArrayList.get(i)).isLoaded()) {
						IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
						if(!mathEngine.renameExperiment(oldName, name)) Log.logErrorMessage(Messages.getString("Channel.ImpossibleRename") + getAbsolutePath() + Messages.getString("Channel.OldName") + oldName + Messages.getString("Channel.NewName") + name + ") !");
						break;
					}
			
		}
	}

	public void addResources(final IResource[] resources) throws InterruptedException {
		for (int i = 0; i < resources.length; i++) {
			if(resourcesArrayList.indexOf(resources[i]) == -1) {
				if(resources[i] instanceof Subject && !experimentPreferences.isSubject(resources[i].getName())) 
					addNewSubjectsByNames(new String[]{resources[i].getName()}, null);
				else resourcesArrayList.add(resources[i]);
			}
		}
	}

	public void copyTo(IResource destResource) throws IOException {
		//Can't copy an experiment
	}

	public String getLocalPath() {
		return name;
	}
	
	public String getNameWithoutExtension() {
		return name;
	}

	public void registerToExperimentsObservers() {
		IResource[] resources = resourcesArrayList.toArray(new IResource[resourcesArrayList.size()]);
		for (int i = 0; i < resources.length; i++) {
			resources[i].registerToExperimentsObservers();
		}
	}
	
	public IResource getFirstResourceByName(String resourceName) {
		for (int i = 0; i < resourcesArrayList.size(); i++) {
			IResource resource = resourcesArrayList.get(i);
			if(resource.getLocalPath().equals(resourceName)) return resource;
			IResource childResource = resource.getFirstResourceByName(resourceName);
			if(childResource != null) return childResource;
		}
		return null;
	}
	
	public Subject getSubjectByName(String resourceName) {
		for (int i = 0; i < resourcesArrayList.size(); i++) {
			IResource resource = resourcesArrayList.get(i);
			if(resource.getLocalPath().equals(resourceName) && resource instanceof Subject) return (Subject) resource;
			Subject childResource = resource.getSubjectByName(resourceName);
			if(childResource != null) return (Subject) childResource;
		}
		return null;
	}

	public String[] getAllMarkersLabels() {
		HashSet<String> labels = new HashSet<String>(0);
		for (int i = 0; i < resourcesArrayList.size(); i++) {
			IResource resource = resourcesArrayList.get(i);
			if(resource instanceof Subject) {
				Subject subject = (Subject)resource;
				if(subject.isLoaded()) {
					IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
					String[] signals = subject.getSignalsNames();//mathEngine.getSignalsNames(subject.getLocalPath());
					for (int j = 0; j < signals.length; j++) {
						String[] labelsString  = mathEngine.getMarkersGroupsLabels(subject.getLocalPath() + "." + signals[j]);
						for (int k = 0; k < labelsString.length; k++) labels.add(labelsString[k]);
					}
				}
			}
		}
		String[] markersLabels = labels.toArray(new String[labels.size()]);
		Arrays.sort(markersLabels);
		return markersLabels;
	}
	
	public Subject[] getSubjects() {
		HashSet<Subject> subjects = new HashSet<Subject>(0);
		for (int i = 0; i < resourcesArrayList.size(); i++) {
			IResource resource = resourcesArrayList.get(i);
			if(resource instanceof Subject) subjects.add((Subject)resource);
		}
		return subjects.toArray(new Subject[subjects.size()]);
	}

	public boolean validate(boolean b) {
		boolean valid = true;
		for (int i = 0; i < resourcesArrayList.size(); i++) {
			IResource resource = resourcesArrayList.get(i);
			if(resource instanceof Processing) valid = valid && ((Processing)resource).validate(false);
			if(resource instanceof Folder) valid = valid && ((Folder)resource).validate(false);
		}
		return valid;
	}

	public boolean hasWarning() {
		boolean hasWarning = false;
		for (int i = 0; i < resourcesArrayList.size(); i++) {
			IResource resource = resourcesArrayList.get(i);
			if(resource instanceof Processing) if(!((Processing)resource).validate(false)) hasWarning = hasWarning || ((Processing)resource).hasWarning();
			if(resource instanceof Folder) if(!((Folder)resource).validate(false)) hasWarning = hasWarning || ((Folder)resource).hasWarning();
		}
		return hasWarning;
	}

	public boolean hasError() {
		boolean hasError = false;
		for (int i = 0; i < resourcesArrayList.size(); i++) {
			IResource resource = resourcesArrayList.get(i);
			if(resource instanceof Processing) if(!((Processing)resource).validate(false)) hasError = hasError || ((Processing)resource).hasError();
			if(resource instanceof Folder) if(!((Folder)resource).validate(false)) hasError = hasError || ((Folder)resource).hasError();
		}
		return hasError;
	}

}


