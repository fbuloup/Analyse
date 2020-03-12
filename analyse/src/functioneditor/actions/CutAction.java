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
import org.eclipse.swt.SWT;

import functioneditor.windows.FunctionsEditorComposite;

import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;


public class CutAction extends Action{

	private FunctionsEditorComposite functionsEditorComposite;

	
	public CutAction() {
		super(Messages.getString("matlabfunctioneditor.CutAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setAccelerator(SWT.MOD1 | 'X');
		setToolTipText(Messages.getString("matlabfunctioneditor.CutAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.CUT_ICON));
	}
	
	@Override
	public void run() {
		if(functionsEditorComposite != null)
			functionsEditorComposite.doCut();
	}
	
	public void setFunctionsEditorComposite(FunctionsEditorComposite functionsEditorComposite) {
		this.functionsEditorComposite = functionsEditorComposite;
	}
}
