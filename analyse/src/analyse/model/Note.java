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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.swt.graphics.Image;

import analyse.Log;
import analyse.Utils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;

public class Note implements IResource, IResourceObserver {

	public static final String EXTENSION = ".note";
	
	private String name;
	private String oldName;
	private IResource parent;
	private DataNote dataNote;
	
	public Note(IResource selectedResource, String noteName) {
		noteName = noteName.replaceAll(EXTENSION + "$", "");
		name = noteName + EXTENSION;
		oldName = name;
		parent = selectedResource;
		File noteFile = new File(getAbsolutePath());
		if(noteFile.exists()) readNote(noteFile);
		else {
			dataNote = new DataNote();
			saveNote();
		}
	}
	
	private void readNote(File noteFile) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(noteFile));
			dataNote = (DataNote) in.readObject();
			in.close();
		} catch (FileNotFoundException e) {
			Log.logErrorMessage(e);
		} catch (IOException e) {
			Log.logErrorMessage(e);
		} catch (ClassNotFoundException e) {
			Log.logErrorMessage(e);
		}
	}
	
	public void saveNote() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getAbsolutePath()));
			out.writeObject(dataNote);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			Log.logErrorMessage(e);
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}
	}
	
	public DataNote getDataNote() {
		return dataNote;
	}
	
	public void addResources(IResource[] resources) throws InterruptedException {
	}

	public void copyTo(IResource destResource) throws IOException, InterruptedException {
		File srcFile = new File(getAbsolutePath());
		File destFile = new File(destResource.getAbsolutePath() + File.separator + name);
		Utils.copyFile(srcFile, destFile);
		Note note = new Note(destResource, name);
		destResource.addResources(new IResource[]{note});
	}

	public boolean delete() {
		boolean succeed = (new File(getAbsolutePath())).delete();
		if(succeed) getParent().remove(this);
		Experiments.getInstance().removeExperimentObserver(this);
		return succeed;
	}

	public String getAbsolutePath() {
		return parent.getAbsolutePath() + File.separator + name;
	}

	public IResource[] getChildren() {
		return null;
	}

	public IResource getFirstResourceByName(String resourceName) {
		if(getLocalPath().equals(resourceName)) return this;
		return null;
	}

	public Image getImage() {
		return ImagesUtils.getImage(IImagesKeys.NOTE_ICON);
	}

	public String getLocalPath() {
		return parent.getLocalPath() + "." + getNameWithoutExtension();
	}

	public String getName() {
		return name;
	}

	public String getNameWithoutExtension() {
		return name.replaceAll(EXTENSION + "$", "");
	}

	public String getOldName() {
		return oldName;
	}

	public IResource getParent() {
		return parent;
	}

	public Subject getSubjectByName(String resourceName) {
		return null;
	}

	public boolean hasChildren() {
		return false;
	}

	public boolean hasParent(IResource parentResource) {
		IResource parent = getParent();
		while(parent != null) {
			if(parent == parentResource) return true;
			parent = parent.getParent();
		}
		return false;
	}

	public void registerToExperimentsObservers() {
		Experiments.getInstance().addExperimentObserver(this);
	}

	public void remove(IResource resource) {

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

	public void update(int message, IResource[] resources) {
	}

}
