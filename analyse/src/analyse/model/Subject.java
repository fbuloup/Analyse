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

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.swt.graphics.Image;

import analyse.Log;
import analyse.resources.CPlatform;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;

public final class Subject implements IResource {
	
	public static final String SAVING_FILE_NAME = "save.mat";
	
	private boolean loaded = false;
	private boolean modified = false;
	private String name;
	private String oldName;
	private ArrayList<IResource> resourcesArrayList = new ArrayList<IResource>(0);
	private Experiment experiment;
	
	public Subject(Experiment experiment, String subjectName) {
		name = subjectName;
		oldName = name;
		this.experiment = experiment;
		populateDataFiles();
	}

	private void populateDataFiles() {
		Experiments.deleteDS_Stores(getAbsolutePath());
		File file = new File(getAbsolutePath());	
		File[] filesList = file.listFiles();
		if(filesList != null)
			for (int i = 0; i < filesList.length; i++) 
				if(filesList[i].isFile()) addResources(new IResource[]{new DataFile(this, filesList[i].getName())}) ;
	}

	public void addResources(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) 
			if(resourcesArrayList.indexOf(resources[i]) == -1) resourcesArrayList.add(resources[i]);
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	
	public String getAbsolutePath() {
		return experiment.getAbsolutePath() + File.separator + name;
	}

	public IResource[] getChildren() {
		HashSet<IResource> children = new HashSet<IResource>(0);
		for (int i = 0; i < resourcesArrayList.size(); i++) {
			if(!(resourcesArrayList.get(i) instanceof Signal))
				if(!(resourcesArrayList.get(i) instanceof Category))
					if(!(resourcesArrayList.get(i) instanceof Event))
						children.add(resourcesArrayList.get(i));
		}
		return children.toArray(new IResource[children.size()]);
	}

	public Image getImage() {
		return ImagesUtils.getImage(IImagesKeys.SUBJECT_ICON);
	}

	public String getName() {
		return name;
	}

	public String getOldName() {
		return oldName;
	}

	public IResource getParent() {
		return experiment;
	}

	public boolean hasChildren() {
		return resourcesArrayList.size() > 0;
	}

	public boolean delete() {
		boolean succeed = true;
		while(resourcesArrayList.size() >0) {
			succeed = succeed && resourcesArrayList.get(0).delete();
			if(!succeed) break;
		}
		if(succeed) {
			if(CPlatform.isMacOSX()) succeed = Experiments.deleteDS_Stores(getAbsolutePath());
			if(succeed) {
				succeed = new File(getAbsolutePath()).delete();
				if(succeed)	getParent().remove(this);
				if(isLoaded()) {
					IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
					if(mathEngine.unloadSubject(getLocalPath())) Log.logErrorMessage("Impossible to unload subject " + getLocalPath());
				}
			}
		}
		return succeed;
	}

	public void remove(IResource resource) {
		resourcesArrayList.remove(resource);
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
			Log.logErrorMessage("Impossible to rename '" + getAbsolutePath() + "'");
			return;
		}
		experiment.getPreferences().renameSubject(oldName, name);
		if(isLoaded()) {
			IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
			if(!mathEngine.renameSubject(getParent().getLocalPath(), oldName, name)) Log.logErrorMessage("Impossible to rename Subject in Math Engine : " + getLocalPath() + "(old name : " + oldName + " new name : " + name +") !");
		}
	}

	public void copyTo(IResource destResource) throws IOException, InterruptedException {
		Subject subject = new Subject((Experiment) destResource, name);
		File destFile = new File(subject.getAbsolutePath());
		if(destFile.mkdir()) {
			for (int i = 0; i < resourcesArrayList.size(); i++) {
				IResource resource = resourcesArrayList.get(i);
				resource.copyTo(subject);
			}
			destResource.addResources(new IResource[]{subject});
		} else Log.logErrorMessage("Impossible o create resource : " + destFile.getAbsolutePath());
	}

	public String getLocalPath() {
		return ((IResource)experiment).getLocalPath() + "." + getNameWithoutExtension();
	}
	
	public String getNameWithoutExtension() {
		return name;
	}

	public String[] getDataFiles(boolean savingFiles) {
		if (savingFiles)
			if((new File(getAbsolutePath() + File.separator + SAVING_FILE_NAME)).exists()) return new String[]{getAbsolutePath() + File.separator + SAVING_FILE_NAME};
		ArrayList<String> dataFilesNames = new ArrayList<String>(0);
		for (int i = 0; i < resourcesArrayList.size(); i++) 
			if(resourcesArrayList.get(i) instanceof DataFile) 
				if(!resourcesArrayList.get(i).getAbsolutePath().endsWith(SAVING_FILE_NAME) )
					dataFilesNames.add(((DataFile)resourcesArrayList.get(i)).getAbsolutePath());
		return dataFilesNames.toArray(new String[dataFilesNames.size()]);
	}

	public boolean isModified() {
		return modified;
	}
	
	private void removeChannels() {
		int n = 0;
		while (resourcesArrayList.size() > n) {
			IResource resource = resourcesArrayList.get(n);
			if(resource instanceof Signal) remove(resource);
			else if(resource instanceof Category) remove(resource);
			else if(resource instanceof Event) remove(resource);
			else n++;
		}
	}
	
	private void updateChannels() {
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		String[] signals = mathEngine.getSignalsNames(getLocalPath());
		for (int i = 0; i < signals.length; i++) {
			resourcesArrayList.add(new Signal(signals[i], this));
		}
		String[] categories = mathEngine.getCategoriesNames(getLocalPath());
		for (int i = 0; i < categories.length; i++) {
			resourcesArrayList.add(new Category(categories[i], this));
		}
		String[] events = mathEngine.getEventsGroupsNames(getLocalPath());
		for (int i = 0; i < events.length; i++) {
			resourcesArrayList.add(new Event(events[i], this));
		}
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
		if(loaded) updateChannels();
		else removeChannels();
	}

	public void setModified(boolean modified) {
		if(this.modified && !modified) {
			boolean saveFileFound = false;
			for (int i = 0; i < resourcesArrayList.size(); i++) {
				IResource resource = resourcesArrayList.get(i);
				String localPath = resource.getLocalPath();
				if(localPath.endsWith(SAVING_FILE_NAME)) saveFileFound = true;
			}
			if(!saveFileFound) addResources(new IResource[]{new DataFile(this, SAVING_FILE_NAME)});
		}
		removeChannels();
		updateChannels();
		this.modified = modified; 
	}

	public void registerToExperimentsObservers() {
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
		return null;
	}

	public String[] getSignalsNames() {
		HashSet<String> signals = new HashSet<String>(0);
		for (int i = 0; i < resourcesArrayList.size(); i++) {
			IResource resource = resourcesArrayList.get(i);
			if(resource instanceof Signal) signals.add(((Signal)resource).getName());
		}
		return signals.toArray(new String[signals.size()]);
	}
	
	public String[] getFullSignalsNames() {
		HashSet<String> signals = new HashSet<String>(0);
		for (int i = 0; i < resourcesArrayList.size(); i++) {
			IResource resource = resourcesArrayList.get(i);
			if(resource instanceof Signal) signals.add(getLocalPath() + "." + ((Signal)resource).getName());
		}
		return signals.toArray(new String[signals.size()]);
	}
	
	public String[] getCategoriesNames() {
		HashSet<String> categories = new HashSet<String>(0);
		for (int i = 0; i < resourcesArrayList.size(); i++) {
			IResource resource = resourcesArrayList.get(i);
			if(resource instanceof Category) categories.add(((Category)resource).getName());
		}
		return categories.toArray(new String[categories.size()]);
	}
	
	public String[] getEventsNames() {
		HashSet<String> events = new HashSet<String>(0);
		for (int i = 0; i < resourcesArrayList.size(); i++) {
			IResource resource = resourcesArrayList.get(i);
			if(resource instanceof Event) events.add(((Event)resource).getName());
		}
		return events.toArray(new String[events.size()]);
	}
	
	public void refreshDataFiles() {
		removeDataFiles();
		populateDataFiles();
	}

	private void removeDataFiles() {
		int i=0;
		while (i < resourcesArrayList.size()) {
			if(resourcesArrayList.get(i) instanceof DataFile)
				resourcesArrayList.remove(i);
			else i++;
		}
	}
	
}
