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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import functioneditor.windows.FunctionsEditorComposite;

import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class PasteAction extends Action{

	private FunctionsEditorComposite functionsEditorComposite;
	
	public PasteAction() {
		super(Messages.getString("matlabfunctioneditor.PasteAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setAccelerator(SWT.MOD1  | 'V');
		setToolTipText(Messages.getString("matlabfunctioneditor.PasteAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.PASTE_ICON));
	}
	
	@Override
	public void run() {
		Control focusedControl = Display.getDefault().getFocusControl();
		if(functionsEditorComposite != null && focusedControl instanceof StyledText)
			functionsEditorComposite.doPaste();
		else {
			Clipboard clipboard = new Clipboard(Display.getDefault());
	        String plainText = (String)clipboard.getContents(TextTransfer.getInstance());
	        if(focusedControl instanceof Text) ((Text)focusedControl).insert(plainText);
	        clipboard.dispose();
		}
	}
	
	public void setFunctionsEditorComposite(FunctionsEditorComposite functionsEditorComposite) {
		this.functionsEditorComposite = functionsEditorComposite;
	}
}
