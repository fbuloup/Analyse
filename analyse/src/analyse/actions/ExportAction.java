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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import analyse.gui.wizards.ExportWizard;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class ExportAction extends Action {
	
	public ExportAction() {
		super(Messages.getString("ExportAction.Title"),AS_PUSH_BUTTON); //$NON-NLS-1$
		setAccelerator(SWT.MOD1 | 'E');
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.EXPORT_WIZARD_ICON));
		setEnabled(true);
		
	}
	
	@Override
	public void run() {
		Shell shell = Display.getCurrent().getActiveShell();
		WizardDialog exportWizardDialog = new WizardDialog(shell,new ExportWizard());
		exportWizardDialog.create();
		exportWizardDialog.getShell().setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		exportWizardDialog.open();
	}
}
