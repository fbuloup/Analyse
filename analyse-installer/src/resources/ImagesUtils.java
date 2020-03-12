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
package resources;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public final class ImagesUtils {

	private static ImageRegistry imagesRegistry = null;
	
	public static void initialize(Display display) {
		if(imagesRegistry == null) {		
			imagesRegistry = JFaceResources.getImageRegistry();				
			ImageDescriptor analyseIconID = null;
			ImageDescriptor  updateID = null;
			
			analyseIconID = ImageDescriptor.createFromURL(Display.getCurrent().getClass().getResource(IImagesKeys.ANALYSE_ICON));
			updateID = ImageDescriptor.createFromURL(Display.getCurrent().getClass().getResource(IImagesKeys.UPDATE_BANNER));
			 
			imagesRegistry.put(IImagesKeys.ANALYSE_ICON, analyseIconID);
			imagesRegistry.put(IImagesKeys.UPDATE_BANNER, updateID);
		}
	}
	
	public static Image getImage(String key) {
		if(imagesRegistry == null) initialize(Display.getCurrent());
		return imagesRegistry.get(key);
	}
	
	public static ImageDescriptor getImageDescriptor(String key) {
		if(imagesRegistry == null) initialize(Display.getCurrent());
		return imagesRegistry.getDescriptor(key);
	}
	
}
