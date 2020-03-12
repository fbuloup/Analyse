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

import analyse.gui.wizards.NewExperimentWizard;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class NewExperimentAction extends Action  {

	public NewExperimentAction() {
		super(Messages.getString("NewExperimentAction.Title"), AS_PUSH_BUTTON);
		setAccelerator(SWT.MOD1 | 'N');
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.EXPERIMENT_ICON));	
	}
	
	public void run() {	
		NewExperimentWizard newExperimentWizard = new NewExperimentWizard();
		WizardDialog newExperimentWizardDialog = new WizardDialog(null,newExperimentWizard);
		newExperimentWizardDialog.create();
		newExperimentWizardDialog.getShell().setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		newExperimentWizardDialog.open();
	}

}
