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

import org.eclipse.swt.graphics.Image;

import analyse.Log;
import analyse.resources.CPlatform;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;

public class Folder implements IResource {

	public static final String EXTENSION = ".folder";
	
	private IResource parent;
	private String name;
	private String oldName;
	private ArrayList<IResource> resourcesArrayList = new ArrayList<IResource>(0);
	
	public Folder(IResource resource, String folderName) {
		folderName = folderName.replaceAll(EXTENSION + "$", "");
		name  = folderName + EXTENSION;
		oldName = name;
		parent = resource;
		File file = new File(getAbsolutePath());
		if(!file.exists()) if(!file.mkdir()) Log.logErrorMessage("Impossible to create Folder '" + name + "' !");
		populateResourcesList();
	}

	private IResource[] populateResourcesList() {
		Experiments.deleteDS_Stores(getAbsolutePath());
		ArrayList<IResource> addedResources = new ArrayList<IResource>(0);
		File file = new File(getAbsolutePath());	
		File[] filesList = file.listFiles();
		for (int i = 0; i < filesList.length; i++) {
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
	
	public boolean isResourceNameExist(String name) {
		for (int i = 0; i < resourcesArrayList.size(); i++) if(resourcesArrayList.get(i).getName().equals(name)) return true;
		return false;
	}
	
	public IResource[] getChildren() {
		return resourcesArrayList.toArray(new IResource[resourcesArrayList.size()]);
	}

	public Image getImage() {
		return ImagesUtils.getImage(IImagesKeys.FOLDER_ICON);
	}

	public String getName() {
		return name;
	}

	public String getOldName() {
		return oldName;
	}

	public IResource getParent() {
		return parent;
	}

	public boolean hasChildren() {
		return resourcesArrayList.size() > 0;
	}

	public String getAbsolutePath() {
		return parent.getAbsolutePath() + File.separator + name;
	}

	public boolean delete() {
		boolean succeed = true;
		while(resourcesArrayList.size() > 0) {
			succeed = succeed && resourcesArrayList.get(0).delete();
			if(!succeed) break;
		}
		if(succeed) {
			if(CPlatform.isMacOSX()) succeed = Experiments.deleteDS_Stores(getAbsolutePath());
			succeed = (new File(getAbsolutePath())).delete();
			if(succeed) getParent().remove(this);
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
		name = newName + EXTENSION;
		File newFile = new File(getAbsolutePath());
		if(!oldFile.renameTo(newFile)) {
			name = oldName;
			oldName = oldOldName;
			Log.logErrorMessage("Impossible to rename '" + getAbsolutePath() + "'");
		}
	}

	public void addResources(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			if(resourcesArrayList.indexOf(resources[i]) == -1) resourcesArrayList.add(resources[i]);
		}
			
	}

	public void copyTo(IResource destResource) throws IOException, InterruptedException {
		Folder folder = new Folder(destResource, name);
		File destFile = new File(folder.getAbsolutePath());
		if(destFile.exists()) {
			for (int i = 0; i < resourcesArrayList.size(); i++) {
				IResource resource = resourcesArrayList.get(i);
				resource.copyTo(folder);
			}
			destResource.addResources(new IResource[]{folder});
		} else Log.logErrorMessage("Impossible o create resource : " + destFile.getAbsolutePath());
	}

	public String getLocalPath() {
		return parent.getLocalPath() + "." + getNameWithoutExtension();
	}
	
	public String getNameWithoutExtension() {
		return name.replaceAll(EXTENSION + "$", "");
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
		return null;
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
