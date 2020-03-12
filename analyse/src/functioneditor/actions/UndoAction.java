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

public class UndoAction extends Action {
	
	private FunctionsEditorComposite functionsEditorComposite;
	
	public UndoAction() {
		super(Messages.getString("matlabfunctioneditor.UndoAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setAccelerator(SWT.MOD1 | 'Z');
		setToolTipText(Messages.getString("matlabfunctioneditor.UndoAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.UNDO_ICON));
	}
	
	public void run(){
		if(functionsEditorComposite != null)
		functionsEditorComposite.doUndo();
	}
	
	public void setFunctionsEditorComposite(FunctionsEditorComposite functionsEditorComposite) {
		this.functionsEditorComposite = functionsEditorComposite;
	}
	
}
