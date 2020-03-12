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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import analyse.gui.CInputDialog;
import analyse.resources.Messages;

public class NewFTPUserDialog extends CInputDialog {
	
	private final class Validator implements IInputValidator {
		public String isValid(String newText) {
			String errorMessage = null;
			if(newText.equals("")) errorMessage = Messages.getString("NewFTPUserDialog.ErrorMessage1"); 
			else if(!newText.matches("\\w+")) errorMessage = Messages.getString("NewFTPUserDialog.ErrorMessage2"); 
			else if(newText.matches("^[0-9]+\\w*")) errorMessage = Messages.getString("NewFTPUserDialog.ErrorMessage3");		  
			else if(usersArrayList.contains(newText)) errorMessage = Messages.getString("NewFTPUserDialog.ErrorMessage4");	
			else if(pwd.equals("")) errorMessage = Messages.getString("NewFTPUserDialog.ErrorMessage1"); 
			return errorMessage;
		}
	}

	protected String pwd = "";
	private ArrayList<String> usersArrayList = new ArrayList<String>(0);

	public NewFTPUserDialog(Shell parentShell, String[] users) {
		super(parentShell, Messages.getString("NewFTPUserDialog.ShellTitle"), Messages.getString("NewFTPUserDialog.nameLabelTitle"), "", null);
		setValidator(new Validator());
		usersArrayList.addAll(Arrays.asList(users));
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Control dialogArea =  super.createDialogArea(parent);
		
		Label pwdLabel = new Label((Composite) dialogArea, SWT.NONE);
		pwdLabel.setText(Messages.getString("NewFTPUserDialog.pwdLabelTitle"));
		pwdLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		final Text pwdValueText = new Text((Composite) dialogArea, SWT.BORDER | SWT.PASSWORD);
		pwdValueText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		pwdValueText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				pwd = pwdValueText.getText();
				validateInput();
			}
		});
		
		Label pwdCommentLabel = new Label((Composite) dialogArea, SWT.NONE);
		pwdCommentLabel.setText(Messages.getString("NewFTPUserDialog.pwdCommentLabelTitle"));
		pwdCommentLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		return dialogArea;
	}

	public String getLogin() {
		return getValue();
	}

	public String getPassword() {
		return pwd;
	}
	
}
