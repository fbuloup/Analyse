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

import org.eclipse.swt.graphics.Image;

public interface IResource {
	IResource[] getChildren();	
	IResource getParent();
	boolean hasChildren();
	boolean hasParent(IResource parentResource);
	
	String getName();
	String getOldName();
	Image getImage();
	String getAbsolutePath();
	String getLocalPath();
	boolean delete();
	void remove(IResource resource);
	void rename(String newName);
	void addResources(IResource[] resources) throws InterruptedException;
	void copyTo(IResource destResource) throws IOException, InterruptedException;
	String getNameWithoutExtension();
	
	void registerToExperimentsObservers();
	IResource getFirstResourceByName(String resourceName); 
	Subject getSubjectByName(String resourceName);
}
