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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import analyse.Log;
import analyse.Utils;
import analyse.preferences.AnalysePreferences;
import analyse.preferences.WorkspacePreferences;
import analyse.resources.Messages;

public final class Experiments implements IResource {
	
	public static final String OPERATION_CANCELED = "Operation canceled";
	private static ArrayList<IResource> resourcesArrayList = new ArrayList<IResource>(0);
	private static HashSet<IResourceObserver> experimentObservers = new HashSet<IResourceObserver>(0);
	private static HashSet<IResourceObserver> experimentObserversToRemove = new HashSet<IResourceObserver>(0);
	private static Experiments experiments;

	public static boolean deleteDS_Stores(String absolutePath) {
		File DS_StoreFile = new File(absolutePath + File.separator + ".DS_Store");
		if(DS_StoreFile.exists()) return DS_StoreFile.delete();
		return true;
	}
	
	private Experiments() {
		String[] experimentsListNames = WorkspacePreferences.getExperimentsListNames();
		for (int i = 0; i < experimentsListNames.length; i++) resourcesArrayList.add(new Experiment(experimentsListNames[i]));
	}
	
	public static Experiments getInstance() {
		if(experiments == null) experiments = new Experiments();
		return experiments;
	}
	
	public HashSet<String> getExperimentsNamesList() {
		HashSet<String> experimentsNamesList = new HashSet<String>(0);
		for (int i = 0; i < resourcesArrayList.size(); i++) experimentsNamesList.add(resourcesArrayList.get(i).getName());
		return experimentsNamesList;
	}
	
	public static void createNewSubjectsFolders(final String experimentName, final Hashtable<String, String[]> subjectsAndDataFiles, final IProgressMonitor progressMonitor) throws InterruptedException {
		final File experimentDir = new File(AnalysePreferences.getCurrentWorkspace() + File.separator + experimentName);
		Iterator<Entry<String, String[]>> iterator = subjectsAndDataFiles.entrySet().iterator();
		while (iterator.hasNext()) {			
			final Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)iterator.next();
			final File subjectDir = new File(experimentDir.getAbsoluteFile() + File.separator + entry.getKey());	
			progressMonitor.subTask("Create subject folder " + subjectDir);
			//create subject folder
			if(!subjectDir.mkdir()){
				progressMonitor.done();
				throw new InterruptedException("Impossible to create Subject folder '" + subjectDir +"' (already exists ?) !");
			}
			if(progressMonitor.isCanceled()) {
				progressMonitor.done();
				throw new InterruptedException(OPERATION_CANCELED);								
			}		
			progressMonitor.worked(1);
			//copy data files	
			String[] filesNames = (String[]) entry.getValue();							
			for (int i = 0; i < filesNames.length; i++) {
				progressMonitor.subTask(Messages.getString("Experiments.MonitorSubTaskCopyDataLabel") + entry.getKey() + " : " + filesNames[i]);
				File srcFile = new File(filesNames[i]);
				File destFile = new File(subjectDir + File.separator + srcFile.getName());
				try {
					Utils.copyFile(srcFile, destFile);
					progressMonitor.worked(1);
				} catch (IOException e) {
					Log.logErrorMessage(e);
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							progressMonitor.setCanceled(true);									
						}
					});	
				}
				if(progressMonitor.isCanceled()) {
					progressMonitor.done();
					throw new InterruptedException(OPERATION_CANCELED);								
				}
			}
		}
	}
	
	public static void createNewExperimentFolder(final String experimentName, final Hashtable<String, String[]> subjectsAndDataFiles, final IProgressMonitor progressMonitor) throws InterruptedException {
		progressMonitor.subTask("Create new experiment Folder");
		final File experimentDir = new File(AnalysePreferences.getCurrentWorkspace() + File.separator + experimentName);
		if(!experimentDir.mkdir()){
			progressMonitor.done();
			throw new InterruptedException("Impossible to create experiment folder '" + experimentDir +"' ! Already exists ?");					
		}		
		if(progressMonitor.isCanceled()) {
			progressMonitor.done();
			throw new InterruptedException(OPERATION_CANCELED);								
		}
		progressMonitor.worked(1);
		createNewSubjectsFolders(experimentName, subjectsAndDataFiles, progressMonitor);
	}
	

	public void addNewExperiment(Experiment experiment) {
		resourcesArrayList.add(experiment);
	}

	public static void createNewExperiment(String name, String type, Hashtable<String, String[]> subjectsAndDataFiles, IProgressMonitor progressMonitor) throws InterruptedException {
		createNewExperimentFolder(name, subjectsAndDataFiles, progressMonitor);
		Experiment experiment = new Experiment(name,type, subjectsAndDataFiles, progressMonitor);
		getInstance().addNewExperiment(experiment);
		IResource[] resources = new IResource[] {experiment}; 
		notifyObservers(IResourceObserver.EXPERIMENT_CREATED, resources);
	}
	
	public static void notifyObservers(final int message, final IResource[] resources) {
		updateExperimentObservers();
		System.out.println("NB observers : " + experimentObservers.size());
		long t1 = System.currentTimeMillis();
		for (Iterator<IResourceObserver> observers = experimentObservers.iterator(); observers.hasNext();) {
			final IResourceObserver observer = (IResourceObserver) observers.next();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
//					long t2 = System.currentTimeMillis();
					observer.update(message, resources);
//					t2 = System.currentTimeMillis() - t2;
//					System.out.println(">>>>> Time to notifyObserver " + observer.getClass().getCanonicalName() + " : " + t2);
				}
			});
		}
		t1 = System.currentTimeMillis() - t1;
		System.out.println("Time to notify all Observers " + t1);
	}

	private static void updateExperimentObservers() {
		for (Iterator<IResourceObserver> observers = experimentObserversToRemove.iterator(); observers.hasNext();) {
			final IResourceObserver observerToRemove = (IResourceObserver) observers.next();
			experimentObservers.remove(observerToRemove);
		}
		experimentObserversToRemove.clear();
	}

	public void addExperimentObserver(IResourceObserver experimentObserver) {
		experimentObservers.add(experimentObserver);
	}
	
	public void removeExperimentObserver(IResourceObserver experimentObserver) {
		experimentObserversToRemove.add(experimentObserver);
	}

	public int getExperimentsCount() {
		return resourcesArrayList.size();
	}

	/**
	 * This method returns the experiment object by its name. 
	 * Return null if not found.
	 * 
	 */
	public Experiment getExperimentByName(String name) {
		for (int i = 0; i < resourcesArrayList.size(); i++) {
			IResource resource = resourcesArrayList.get(i);
			if(resource.getName().equals(name)) return (Experiment)resource;
		}
		return null;
	}

	public boolean delete() {
		return false;
	}

	public String getAbsolutePath() {
		return null;
	}

	public IResource[] getChildren() {
		return resourcesArrayList.toArray(new IResource[resourcesArrayList.size()]);
	}

	public Image getImage() {
		return null;
	}

	public String getName() {
		return null;
	}

	public String getOldName() {
		return null;
	}

	public IResource getParent() {
		return null;
	}

	public boolean hasChildren() {
		return resourcesArrayList.size() > 0;
	}

	public void remove(IResource resource) {
		resourcesArrayList.remove(resource);
	}

	public boolean hasParent(IResource parentResource) {
		return false;
	}

	public void rename(String newName) {
	}

	public void addResources(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			if(resourcesArrayList.indexOf(resources[i]) == -1) resourcesArrayList.add(resources[i]);
		}
	}

	public void copyTo(IResource destResource) throws IOException {
		//Can't copy experiments
	}

	public String getLocalPath() {
		return "";
	}
	
	public String getNameWithoutExtension() {
		return "";
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

	public Subject[] getLoadedSubjects() {
		ArrayList<Subject> loadedSubjects = new ArrayList<Subject>(0);
		String[] experimentsNames = getInstance().getExperimentsNamesList().toArray(new String[getInstance().getExperimentsCount()]);
		for (int i = 0; i < experimentsNames.length; i++) {
			Experiment experiment = getInstance().getExperimentByName(experimentsNames[i]);
			Subject[] subjects = experiment.getSubjects();
			for (int j = 0; j < subjects.length; j++) {
				if(subjects[j].isLoaded()) loadedSubjects.add(subjects[j]);
			}
		}
		return loadedSubjects.toArray(new Subject[loadedSubjects.size()]);
	}
	
}
