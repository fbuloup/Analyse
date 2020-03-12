/*******************************************************************************
 * Universit� d�Aix Marseille (AMU) - Centre National de la Recherche Scientifique (CNRS)
 * Copyright 2014 AMU-CNRS All Rights Reserved.
 * 
 * These computer program listings and specifications, herein, are
 * the property of Universit� d�Aix Marseille and CNRS
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
import org.eclipse.swt.widgets.Display;

import functioneditor.windows.FindReplaceDialog;

import analyse.resources.Messages;

public class FindAndReplaceAction extends Action {
	
	FindReplaceDialog findReplaceDialog = null;
	
	public FindAndReplaceAction() {
		super(Messages.getString("FindAndReplaceAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setAccelerator(SWT.MOD1  | 'F');
		setToolTipText(Messages.getString("FindAndReplaceAction.Tooltip")); //$NON-NLS-1$
	}

	@Override
	public void run() {
		if(findReplaceDialog == null){
			findReplaceDialog = new FindReplaceDialog(Display.getCurrent().getActiveShell());
			findReplaceDialog.open();
			findReplaceDialog = null;
		}
	}
}
