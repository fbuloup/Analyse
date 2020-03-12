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
package functioneditor.actions;


import org.eclipse.jface.action.Action;

import analyse.model.Function;
import analyse.preferences.LibraryPreferences;
import analyse.resources.Messages;

import functioneditor.windows.FunctionsEditorComposite;

public class RefreshFunctionsAction extends Action {

	private FunctionsEditorComposite functionsEditorComposite;
	
	public RefreshFunctionsAction() {
		//super(Messages.getString("matlabfunctioneditor.CopyAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		super(Messages.getString("RefreshFunctionsAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		//setAccelerator(SWT.MOD1 | 'C');
		setToolTipText(Messages.getString("RefreshFunctionsAction.Title"));
		setEnabled(false);
	}
	
	@Override
	public void run() {
		if(functionsEditorComposite != null) {
			Function[] functions = LibraryPreferences.getAllFunctions();
			Function templateFunction = new Function();
			templateFunction.initializeFunction();
			for (int i = 0; i < functions.length; i++) {
				String preBody = templateFunction.getMatlabFunctionPreBody();
				String postBody = templateFunction.getMatlabFunctionPostBody();
				functions[i].setMatlabFunctionPreBody(preBody);
				functions[i].setMatlabFunctionPostBody(postBody);
				functionsEditorComposite.doSave(functions[i]);
			}
		}
	}
	
	public void setFunctionsEditorComposite(FunctionsEditorComposite functionsEditorComposite) {
		this.functionsEditorComposite = functionsEditorComposite;
	}

}
