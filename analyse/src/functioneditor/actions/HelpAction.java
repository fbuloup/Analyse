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

import analyse.resources.Messages;

public class HelpAction extends Action{
	
	public HelpAction() {
		super(Messages.getString("matlabfunctioneditor.HelpAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setAccelerator(SWT.MOD1  | 'H');
		setToolTipText(Messages.getString("matlabfunctioneditor.HelpAction.Tooltip")); //$NON-NLS-1$
	}
}
