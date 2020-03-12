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

import functioneditor.windows.MatlabFunctionEditorWindow;

import analyse.resources.Messages;

public class QuitAction extends Action{
	
	private MatlabFunctionEditorWindow matlabFunctionEditorWindow;
	
	public QuitAction() {
		super(Messages.getString("matlabfunctioneditor.QuitAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setAccelerator(SWT.SHIFT | SWT.MOD1 | 'Q');
		setToolTipText(Messages.getString("matlabfunctioneditor.QuitAction.Tooltip")); //$NON-NLS-1$
	}	

	public void run(){
		matlabFunctionEditorWindow.doClose();
	}
	 
	public void setMatlabFunctionEditorWindow(MatlabFunctionEditorWindow matlabFunctionEditorWindow2){
		this.matlabFunctionEditorWindow = matlabFunctionEditorWindow2;
	}
}
