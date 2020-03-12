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
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;

import functioneditor.windows.FunctionsEditorComposite;

import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class FontAction extends Action {

	private FunctionsEditorComposite functionsEditorComposite;

	public FontAction() {
		super(Messages.getString("FontAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setToolTipText(Messages.getString("FontAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.FONT_ICON));
	}
	
	@Override
	public void run() {
		if(functionsEditorComposite != null) {
			FontDialog fontDialog = new FontDialog(Display.getCurrent().getActiveShell());
			FontData fontData = fontDialog.open();
			if (fontData != null) functionsEditorComposite.setFont(fontData);						
		}
			
	}
	
	public void setFunctionsEditorComposite(FunctionsEditorComposite functionsEditorComposite) {
		this.functionsEditorComposite = functionsEditorComposite;
	}
	
}
