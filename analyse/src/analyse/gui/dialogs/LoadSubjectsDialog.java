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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class LoadSubjectsDialog extends MessageDialog {

		private Button savingFileButton;
		private Button rawFileButton;
		private boolean fromSavingFile = true;

		public LoadSubjectsDialog(Shell parentShell) {
			super(parentShell, Messages.getString("LoadSubjectDialog.Title"), 
					ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON), 
					Messages.getString("LoadSubjectDialog.Message"), 
					MessageDialog.QUESTION, new String[] {IDialogConstants.OK_LABEL,IDialogConstants.CANCEL_LABEL }, 0); 
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite container = (Composite) super.createDialogArea(parent);
			
			savingFileButton =  new Button(container,SWT.RADIO);
			savingFileButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
			savingFileButton.setText(Messages.getString("LoadSubjectDialog.FromSavedFiles")); 
			savingFileButton.setSelection(true);
			
			rawFileButton =  new Button(container,SWT.RADIO);
			rawFileButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
			rawFileButton.setText(Messages.getString("LoadSubjectDialog.FromRawFiles")); 
			
			return container; 
		}
		
		@Override
		protected void buttonPressed(int buttonId) {
			fromSavingFile = savingFileButton.getSelection();
			super.buttonPressed(buttonId);
		}
		
		public boolean getLoadFromSavingFiles() {
			return fromSavingFile;
		}

		
	}
