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

import functioneditor.windows.MatlabFunctionEditorWindow;

import analyse.preferences.LibraryPreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class SwitchLibrary extends Action {
	
	private static MatlabFunctionEditorWindow matlabFunctionEditorWindow;
	
	public SwitchLibrary() {
		super(Messages.getString("matlabfunctioneditor.SwitchLibraryAction.Title1"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setToolTipText(Messages.getString("matlabfunctioneditor.SwitchLibraryAction.Title1")); //$NON-NLS-1$
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.SWITCH_LIBRARY_ICON));
		setEnabled(true);
	}
	
	@Override
	public void run() {
		if(LibraryPreferences.isStandardLibrary()) {
			//Switch to extended library
			if(matlabFunctionEditorWindow.doSwitchLibrary()) {
				setText(Messages.getString("matlabfunctioneditor.SwitchLibraryAction.Title2")); //$NON-NLS-1$
				setToolTipText(Messages.getString("matlabfunctioneditor.SwitchLibraryAction.Title2")); //$NON-NLS-1$
			}
		} else {
			//Return to standard library
			if(matlabFunctionEditorWindow.doSwitchLibrary()) {
				setText(Messages.getString("matlabfunctioneditor.SwitchLibraryAction.Title1")); //$NON-NLS-1$
				setToolTipText(Messages.getString("matlabfunctioneditor.SwitchLibraryAction.Title1")); //$NON-NLS-1$
			}
		}
	}
	
	public void setMatlabFunctionEditorWindow(MatlabFunctionEditorWindow matlabFunctionEditorWindow) {
		SwitchLibrary.matlabFunctionEditorWindow = matlabFunctionEditorWindow;
	}

}
