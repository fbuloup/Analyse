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
package analyse.gui.wizards;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import analyse.Log;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class ExportSelectionPage extends WizardPage {
	
	public static final String PAGE_NAME = "EXPORT_SELECTION_PAGE"; //$NON-NLS-1$
	Button markersButton;
	Button signalsButton;
	Button fieldsButton;
	Button exportInSingleFileButton;
	private Combo charCombo;
	private Text directoryText;
	private String defaultMessage = Messages.getString("ExportSelectionPage.DefaultMessage");  //$NON-NLS-1$
	private Button experimentsButton;
	private Button customButton;
	private Combo customCombo;

	protected ExportSelectionPage() {
		super(PAGE_NAME,Messages.getString("ExportSelectionPage.Title"),ImagesUtils.getImageDescriptor(IImagesKeys.EXPORT_WIZARD_BANNER)); //$NON-NLS-1$
		setMessage(Messages.getString("ExportSelectionPage.Text")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent,SWT.NONE);
		container.setLayout(new GridLayout());
		
		Composite container2 = new Composite(container,SWT.NONE);
		container2.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		container2.setLayout(new GridLayout(3,false));
		
		Label exportDirectoryLabel = new Label(container2,SWT.NONE);
		exportDirectoryLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
		exportDirectoryLabel.setText(Messages.getString("ExportSelectionPage.ExportDirLabelTitle")); //$NON-NLS-1$
		
		directoryText = new Text(container2,SWT.READ_ONLY | SWT.BORDER);
		directoryText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		directoryText.setText(AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.DEFAULT_EXPORT_DIRECTORY));
		
		Button directoryButton = new Button(container2,SWT.PUSH);
		directoryButton.setText(Messages.getString("ExportSelectionPage.BrowseButtonTitle")); //$NON-NLS-1$
		directoryButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		directoryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog (Display.getCurrent().getActiveShell());
				String response = dialog.open();
				if(response != null) {
					directoryText.setText(response);
					AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.DEFAULT_EXPORT_DIRECTORY, directoryText.getText());
				}
				checkForErrors();
			}
			
		});
		
		Group exportDataGroup = new Group(container, SWT.NORMAL);
		exportDataGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		exportDataGroup.setLayout(new GridLayout(2, false));
		exportDataGroup.setText(Messages.getString("ExportSelectionPage.ExportDataGroupTitle"));
		
		
		signalsButton = new Button(exportDataGroup,SWT.RADIO);
		signalsButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false, 2, 1));
		signalsButton.setSelection(true);
		signalsButton.setText(Messages.getString("ExportSelectionPage.SignalsButtonTitle")); //$NON-NLS-1$
		signalsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectionHandler();
			}
		});
		
		markersButton = new Button(exportDataGroup,SWT.RADIO);
		markersButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false, 2, 1));
		markersButton.setText(Messages.getString("ExportSelectionPage.MarkersButtonTitle")); //$NON-NLS-1$
		markersButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectionHandler();
			}
		});
		
		fieldsButton = new Button(exportDataGroup,SWT.RADIO);
		fieldsButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false, 2, 1));
		fieldsButton.setText(Messages.getString("ExportSelectionPage.FieldsButtonTitle")); //$NON-NLS-1$
		fieldsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectionHandler();
			}
		});
		
		Composite container3 = new Composite(exportDataGroup,SWT.NONE);
		container3.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false, 2, 1));
		container3.setLayout(new GridLayout(2,false));
		
		Label separatorCharLabel = new Label(container3,SWT.NONE);
		separatorCharLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		separatorCharLabel.setText(Messages.getString("ExportSelectionPage.SepCharLabelTitle")); //$NON-NLS-1$
		
		charCombo = new Combo(container3,SWT.NONE);
		charCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		charCombo.add(";"); //$NON-NLS-1$
		charCombo.add(","); //$NON-NLS-1$
		charCombo.add(":"); //$NON-NLS-1$
		charCombo.add("\t"); //$NON-NLS-1$
		charCombo.select(0);
		charCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				checkForErrors();
			}
		});
		
		exportInSingleFileButton = new Button(container3,SWT.CHECK);
		exportInSingleFileButton.setText(Messages.getString("ExportSelectionPage.ExportInSingleFileButtonTitle")); //$NON-NLS-1$
		exportInSingleFileButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,2,1));
		
		customButton = new Button(exportDataGroup,SWT.RADIO);
		customButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		customButton.setText(Messages.getString("ExportSelectionPage.CustomButtonTitle")); //$NON-NLS-1$
		customButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectionHandler();
				charCombo.setEnabled(false);
				exportInSingleFileButton.setEnabled(false);
				customCombo.setEnabled(true);
			}
		});
		
		customCombo = new Combo(exportDataGroup,SWT.READ_ONLY);
		customCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		customCombo.setEnabled(false);
		try {
			Properties exportProperties = new Properties();
			exportProperties.load(new FileInputStream("./exportExtension/exportExtension.properties"));
			int i = 1;
			String key = (String) exportProperties.get("file" + i);
			while (key != null) {
				String property = exportProperties.getProperty("description" + i) + " (" + key + ")";
				customCombo.add(property);
				i++;
				key = (String) exportProperties.get("file" + i);
				
			}
			
		} catch (FileNotFoundException e) {
			//File does not exist, user still doesn't need it...
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}
		
		experimentsButton = new Button(container,SWT.RADIO);
		experimentsButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		experimentsButton.setSelection(false);
		experimentsButton.setText(Messages.getString("ExportSelectionPage.ExperimentsButtonTitle")); //$NON-NLS-1$
		experimentsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				signalsButton.setSelection(false);
				markersButton.setSelection(false);
				fieldsButton.setSelection(false);
				customButton.setSelection(false);
				charCombo.setEnabled(false);
				exportInSingleFileButton.setEnabled(false);
				customCombo.setEnabled(false);
			}
		});
		
		setControl(container);
		
		setMessage(defaultMessage);
		setPageComplete(false);
		
		checkForErrors();
		
	}

	protected void selectionHandler() {
		experimentsButton.setSelection(false);
		charCombo.setEnabled(true);
		exportInSingleFileButton.setEnabled(true);
		customCombo.setEnabled(false);
	}

	protected void checkForErrors() {
		setErrorMessage(null);
		setPageComplete(true);
		if(getCharSeparator().equals("")) setErrorMessage(Messages.getString("ExportSelectionPage.ErrorMessage1")); //$NON-NLS-1$ //$NON-NLS-2$
		else if(getExportDirectory().equals("")) setErrorMessage(Messages.getString("ExportSelectionPage.ErrorMessage2")); //$NON-NLS-1$ //$NON-NLS-2$
		setPageComplete(getErrorMessage() == null);
		
	}

	public boolean isMarkers() {
		return markersButton.getSelection();
	}

	public boolean isSignals() {
		return signalsButton.getSelection();
	}
	
	public boolean isFields() {
		return fieldsButton.getSelection();
	}

	public boolean isCustom() {
		return customButton.getSelection();
	}
	
	public boolean isExperiments() {
		return experimentsButton.getSelection();
	}
	
	public String getCharSeparator() {
		return charCombo.getText();
	}

	public String getExportDirectory() {
		return directoryText.getText();
	}
	
	public boolean getExportInSingleFile() {
		return exportInSingleFileButton.getSelection();
	}
	
	public String getCustomFunction() {
		return customCombo.getText().split("\\(")[1].replaceAll("\\)$", "");
	}
	
}
