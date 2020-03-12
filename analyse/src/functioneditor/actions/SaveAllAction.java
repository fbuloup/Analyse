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

import analyse.model.Function;
import analyse.model.IFunctionObserver;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import functioneditor.windows.FunctionsEditorComposite;

import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class SaveAllAction extends Action implements IFunctionObserver, SelectionListener {
	
	private FunctionsEditorComposite functionsEditorComposite;
	
	public SaveAllAction() {
		super(Messages.getString("matlabfunctioneditor.SaveAllAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setToolTipText(Messages.getString("matlabfunctioneditor.SaveAllAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.SAVE_ALL_ICON));
		setEnabled(false);
	}
	
	@Override
	public void run() {
		functionsEditorComposite.doSaveAll();
	}
	
	public void setFunctionsEditorComposite(FunctionsEditorComposite functionsEditorComposite) {
		this.functionsEditorComposite = functionsEditorComposite;
	}

	public void update(Function function) {
		setEnabled(functionsEditorComposite.isFunctionsDirty());		
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void widgetSelected(SelectionEvent e) {
		setEnabled(functionsEditorComposite.isFunctionsDirty());		
		
	}
}
