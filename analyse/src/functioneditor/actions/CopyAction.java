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
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

import functioneditor.windows.FunctionsEditorComposite;

public class CopyAction extends Action {

	private FunctionsEditorComposite functionsEditorComposite;
	
	public CopyAction() {
		super(Messages.getString("matlabfunctioneditor.CopyAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setAccelerator(SWT.MOD1 | 'C');
		setToolTipText(Messages.getString("matlabfunctioneditor.CopyAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.COPY_ICON));
		setEnabled(true);
	}
	
	@Override
	public void run() {
		Control focusedControl = Display.getDefault().getFocusControl();
		if(functionsEditorComposite != null && focusedControl instanceof StyledText)
			functionsEditorComposite.doCopy();
		else {
			String plainText = null;
			if(focusedControl instanceof Text) plainText = ((Text)focusedControl).getSelectionText();
			if(focusedControl instanceof Spinner) plainText = ((Spinner)focusedControl).getText();
			if(plainText != null) {
				Clipboard clipboard = new Clipboard(Display.getDefault());
		        TextTransfer textTransfer = TextTransfer.getInstance();
		        clipboard.setContents(new String[]{plainText}, new Transfer[]{textTransfer});
		        clipboard.dispose();
			}
		}
	}
	
	public void setFunctionsEditorComposite(FunctionsEditorComposite functionsEditorComposite) {
		this.functionsEditorComposite = functionsEditorComposite;
	}

}
