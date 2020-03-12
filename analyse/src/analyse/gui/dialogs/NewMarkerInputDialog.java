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
package analyse.gui.dialogs;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.widgets.Shell;

import analyse.gui.CInputDialog;
import analyse.model.Experiment;
import analyse.resources.Messages;

public class NewMarkerInputDialog extends CInputDialog {

	private String[] markersLabels;
	
	private final class Validator implements IInputValidator {
		public String isValid(String newText) {
			String errorMessage = null;
			if(newText.equals("")) errorMessage = Messages.getString("NewMarkerDialog.ErrorMessage1"); 
			for (int i = 0; i < markersLabels.length; i++) {
				if(markersLabels[i].equalsIgnoreCase(newText)) {
					errorMessage =  Messages.getString("NewMarkerDialog.ErrorMessage2");
					break;
				}
			}
			return errorMessage;
		}
	}
	
	public NewMarkerInputDialog(Shell parentShell, Experiment experiment) {
		super(parentShell, Messages.getString("NewMarkerDialog.Title"), Messages.getString("NewMarkerDialog.Message"), "", null);
		setValidator(new Validator());
		markersLabels = experiment.getAllMarkersLabels();
	}

}
