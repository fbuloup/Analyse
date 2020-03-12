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

import java.io.IOException;

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.swt.graphics.Image;

import analyse.Log;
import analyse.resources.Messages;

public class Event implements IResource {

	private String name;
	private String oldName;
	private Subject subject;
	
	public Event(String name, Subject subject) {
		this.name = name;
		oldName = name;
		this.subject = subject;
	}
	
	public void addResources(IResource[] resources) throws InterruptedException {
	}

	public void copyTo(IResource destResource) throws IOException, InterruptedException {
	}

	public boolean delete() {
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		boolean succeed = mathEngine.deleteChannel(subject.getLocalPath(), name);
		if(!succeed) Log.logErrorMessage(Messages.getString("Event.ImpossibleDelete") + getLocalPath());
		else getParent().remove(this);
		return succeed;
	}

	public String getAbsolutePath() {
		return null;
	}

	public IResource[] getChildren() {
		return null;
	}

	public IResource getFirstResourceByName(String resourceName) {
		return null;
	}

	public Image getImage() {
		return null;
	}

	public String getLocalPath() {
		return subject.getLocalPath() + "." + name;
	}

	public String getName() {
		return name;
	}

	public String getNameWithoutExtension() {
		return name;
	}

	public String getOldName() {
		return oldName;
	}

	public IResource getParent() {
		return subject;
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
	}

	public void remove(IResource resource) {
		//signal does not contain any resource
	}

	public void rename(String newName) {
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		if(!mathEngine.renameChannel(getParent().getLocalPath(), name, newName)) Log.logErrorMessage(Messages.getString("Channel.ImpossibleRename") + getLocalPath() + Messages.getString("Channel.OldName") + oldName + Messages.getString("Channel.NewName") + name +") !");
		else {
			if(!oldName.equals(name)) oldName = name;
			name = newName;
		}
	}

}
