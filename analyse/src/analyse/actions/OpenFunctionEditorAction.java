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
package analyse.actions;

import functioneditor.controllers.FunctionEditorController;

import org.eclipse.jface.action.Action;

import analyse.AnalyseApplication;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;


public class OpenFunctionEditorAction extends Action {


	public OpenFunctionEditorAction() {
		super(Messages.getString("OpenFunctionEditorAction.Title"),AS_PUSH_BUTTON);
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.FUNCTION_ICON));
		setEnabled(true);
	}
	
	@Override
	public void run() {
		AnalyseApplication.getAnalyseApplicationWindow().freezeUI();
		setEnabled(false);
		new FunctionEditorController();
		setEnabled(true);
		AnalyseApplication.getAnalyseApplicationWindow().unFreezeUI();
	}
	
}
