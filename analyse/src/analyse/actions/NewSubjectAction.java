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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardDialog;

import analyse.gui.wizards.NewSubjectWizard;
import analyse.model.DataFile;
import analyse.model.Experiment;
import analyse.model.IResource;
import analyse.model.Subject;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class NewSubjectAction extends Action  implements ISelectionChangedListener {
	
	private Experiment selectedExperiment;

	public NewSubjectAction() {
		super(Messages.getString("NewSubjectAction.Title"), AS_PUSH_BUTTON);
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.SUBJECT_ICON));	
	}
	
	public void run() {	
		NewSubjectWizard  newSubjectWizard = new NewSubjectWizard(selectedExperiment);
		WizardDialog newSubjectWizardDialog = new WizardDialog(null,newSubjectWizard);
		newSubjectWizardDialog.create();
		newSubjectWizardDialog.getShell().setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		newSubjectWizardDialog.open();
	}

	public void selectionChanged(SelectionChangedEvent event) {
		selectedExperiment = null;
		IResource selection = (IResource) ((TreeSelection)event.getSelection()).getFirstElement();
		if(selection instanceof Experiment) selectedExperiment = (Experiment) selection;
		if(selection instanceof Subject) selectedExperiment = (Experiment) selection.getParent();
		if(selection instanceof DataFile) selectedExperiment = (Experiment) selection.getParent().getParent();
	}

}
