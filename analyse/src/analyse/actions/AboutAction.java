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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import analyse.resources.Messages;

public class AboutAction extends Action {
	
	public AboutAction() {
		super(Messages.getString("AboutAction.Title"),AS_PUSH_BUTTON);
	}
	
	public void run() {
		MessageDialog.openInformation(Display.getDefault().getActiveShell(), Messages.getString("AboutAction.Title"), "http://www.ism.univmed.fr/buloup/index.php?selectedMenu=Analyse");
	}

}
