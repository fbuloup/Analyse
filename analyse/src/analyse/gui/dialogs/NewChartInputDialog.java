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
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import analyse.model.ChartsTypes;
import analyse.resources.Messages;

public class NewChartInputDialog extends InputDialog {

	private String type;

	public NewChartInputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Control dialogArea =  super.createDialogArea(parent);
		
		Composite chartsTypesContainer = new Composite((Composite)dialogArea, SWT.NONE);
		chartsTypesContainer.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		chartsTypesContainer.setLayout(new GridLayout(2,false));
		
		Label chartsTypesLabel = new Label(chartsTypesContainer, SWT.NONE);
		chartsTypesLabel.setText(Messages.getString("NewChartInputDialog.ChartType"));
		chartsTypesLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		
		final Combo chartsTypesCombo = new Combo(chartsTypesContainer, SWT.READ_ONLY);
		chartsTypesCombo.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		chartsTypesCombo.setItems(ChartsTypes.getChartsTypes());
		chartsTypesCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				type = chartsTypesCombo.getText();
			}
		});
		chartsTypesCombo.select(0);
		
		return dialogArea;
	}
	
	public String getName() {
		return getValue();
	}
	
	public String getType() {
		return type;
	}

}
