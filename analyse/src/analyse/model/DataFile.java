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

import org.eclipse.swt.graphics.Image;

import analyse.Log;
import analyse.Utils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class DataFile implements IResource {

	private String name;
	private String oldName;
	private Subject subject;
	
	public DataFile(Subject subject, String fileName) {
		this.name = fileName;
		oldName = name;
		this.subject = subject;
	}

	public IResource[] getChildren() {
		return null;
	}

	public Image getImage() {
		return ImagesUtils.getImage(IImagesKeys.DATAFILE_ICON);
	}

	public String getName() {
		return name;
	}

	public String getOldName() {
		return name;
	}

	public IResource getParent() {
		return subject;
	}

	public boolean hasChildren() {
		return false;
	}

	public String getAbsolutePath() {
		return subject.getAbsolutePath() + File.separator + name;
	}

	public boolean delete() {
		boolean succeed = (new File(getAbsolutePath())).delete();
		if(succeed) getParent().remove(this);
		return succeed;
	}

	public void remove(IResource resource) {
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
			Log.logErrorMessage(Messages.getString("DataFile.ImpossibleRename") + getAbsolutePath());
		}
	}

	public void addResources(IResource[] resources) {
	}

	public void copyTo(IResource destResource) throws IOException, InterruptedException {
		DataFile dataFile = new DataFile((Subject) destResource, name);
		File srcFile = new File(getAbsolutePath());
		File destFile = new File(dataFile.getAbsolutePath());
		Utils.copyFile(srcFile, destFile);
		destResource.addResources(new IResource[]{dataFile});
	}

	public String getLocalPath() {
		return ((IResource)subject).getLocalPath() + "." + getNameWithoutExtension();
	}

	public String getNameWithoutExtension() {
		return name;
	}

	public void registerToExperimentsObservers() {
	}

	public IResource getFirstResourceByName(String resourceName) {
		if(getLocalPath().equals(resourceName)) return this;
		return null;
	}
	
	public Subject getSubjectByName(String resourceName) {
		return null;
	}
	
}
