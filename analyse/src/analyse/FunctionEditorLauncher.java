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
package analyse;

import functioneditor.controllers.FunctionEditorController;

import org.eclipse.swt.widgets.Display;

import analyse.preferences.AnalysePreferences;
import analyse.preferences.LibraryPreferences;


public class FunctionEditorLauncher {
	
	public static void main(String[] args) {
		try {
			Display display = new Display();
			AnalysePreferences.initialize();
			LibraryPreferences.initialize();
			new FunctionEditorController();
			AnalysePreferences.savePreferences();
			display.dispose();
		} catch (Exception e) {
			Log.logErrorMessage(e);
		}
	}
	
}
