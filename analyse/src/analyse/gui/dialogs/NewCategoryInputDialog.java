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

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import analyse.gui.CInputDialog;
import analyse.model.Subject;
import analyse.resources.Messages;

public class NewCategoryInputDialog extends CInputDialog {

	private String trialsList;
	private String criteria;
	private Text categoryTrialsListValueText; 
	private Subject subject;
	private String[] categoryName;
	
	private final class Validator implements IInputValidator {
		public String isValid(String newText) {
			String errorMessage = null;
			if(newText.equals("")) errorMessage = Messages.getString("NewCategoryDialog.ErrorMessage1"); 
			else if(!newText.matches("\\w+")) errorMessage = Messages.getString("NewCategoryDialog.ErrorMessage2"); 
			else if(newText.length() > 31) errorMessage = Messages.getString("NewCategoryDialog.ErrorMessage3"); 
			else if(newText.matches("^[0-9]+\\w*")) errorMessage = Messages.getString("NewCategoryDialog.ErrorMessage4");		  
			IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
			if(mathEngine.isStarted()) {
				if(subject != null)
					if(subject.isLoaded()) {
						for (int i = 0; i < categoryName.length; i++) {
							if(categoryName[i].equals(newText)) {
								errorMessage =  Messages.getString("NewCategoryDialog.ErrorMessage5");
								break;
							}
						}
					}
			}
			if(errorMessage == null) {
				trialsList = categoryTrialsListValueText.getText();
				if(trialsList.equals("")) errorMessage = Messages.getString("NewCategoryDialog.ErrorMessage6"); //$NON-NLS-1$ //$NON-NLS-2$
				else if(!trialsList.matches("^[1-9]+(\\d*:\\d+|\\s|\\d)*")) errorMessage = Messages.getString("NewCategoryDialog.ErrorMessage7"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return errorMessage;
		}
	}
	
	public NewCategoryInputDialog(Shell parentShell, Subject subject) {
		super(parentShell, Messages.getString("NewCategoryDialog.ShellTitle"), Messages.getString("NewCategoryDialog.nameLabelTitle"), "", null);
		setValidator(new Validator());
		this.subject = subject;
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		categoryName = mathEngine.getCategoriesNames(subject.getLocalPath());
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Control dialogArea =  super.createDialogArea(parent);
		
		Composite newCategoryContainer = new Composite((Composite)dialogArea, SWT.NONE);
		newCategoryContainer.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		newCategoryContainer.setLayout(new GridLayout(2,false));
		
		Label categoryNameLabel = new Label(newCategoryContainer, SWT.NONE);
		categoryNameLabel.setText(Messages.getString("NewCategoryDialog.trialsListLabelTitle"));
		categoryNameLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		categoryTrialsListValueText = new Text(newCategoryContainer, SWT.BORDER);
		categoryTrialsListValueText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		categoryTrialsListValueText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		
		Label criteriaNameLabel = new Label(newCategoryContainer, SWT.NONE);
		criteriaNameLabel.setText(Messages.getString("NewCategoryDialog.criteriaLabelTitle"));
		criteriaNameLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		final Text criteriaValueText = new Text(newCategoryContainer, SWT.BORDER);
		criteriaValueText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		criteriaValueText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				criteria = criteriaValueText.getText();
			}
		});
		
		return dialogArea;
	}
	
	public String getName() {
		return getValue();
	}

	public String getTrialsList() {
		return trialsList;
	}

	public String getCriteria() {
		return criteria;
	}

}
