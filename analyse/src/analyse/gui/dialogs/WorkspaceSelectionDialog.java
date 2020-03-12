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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import analyse.Log;
import analyse.SplashScreen;
import analyse.Utils;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;


public class WorkspaceSelectionDialog extends TitleAreaDialog {
	
	private Combo workspaceCombo;
	private Button useAsDefaultButton;

	public WorkspaceSelectionDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText(Messages.getString("WorkspaceSelectionDialog.ShellTitle"));
		newShell.setSize(600, 250);
		
		newShell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		
		int monitorNumber = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.MONITOR_NUMBER);		
		Monitor monitor = newShell.getDisplay().getMonitors()[monitorNumber];
		
		Rectangle bounds = monitor.getBounds();
		Rectangle rect = newShell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		newShell.setLocation (x, y);
		
	}
	
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);		
		setTitle(Messages.getString("WorkspaceSelectionDialog.Title"));
		setMessage(Messages.getString("WorkspaceSelectionDialog.Text"));
		SplashScreen.hide();
		return contents;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		boolean firstLaunch = AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.FIRST_LAUNCH);
		if(firstLaunch) {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			Runnable unzipRunnable = new Runnable() {
				public void run() {
					String currentDirectory = (new File(".")).getAbsolutePath();
					currentDirectory = currentDirectory.replaceAll("\\.", "");
					//Get zip files list
					String[] zipFiles = (new File(".")).list(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".zip");
						}
					});
					//unzip files
					for (int i = 0; i < zipFiles.length; i++) {
						try {
							String[] splitted =  zipFiles[i].split(Pattern.quote("\\"));
							Utils.doUnzip(currentDirectory, currentDirectory + File.separator + zipFiles[i], splitted[splitted.length - 1].replaceAll("\\.\\w*$", "")+ File.separator);
						} catch (IOException e) {
							Log.logErrorMessage(e);
						}
					}
					//update message and button
					WorkspaceSelectionDialog.this.getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							getButton(OK).setEnabled(true);
						}
					});
				}
			};
			Thread unzipThread = new Thread(unzipRunnable);
			unzipThread.start();
			
			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.getString("FirstLaunchDialog.Title"), Messages.getString("FirstLaunchDialog.Text"));
					AnalysePreferences.getPreferenceStore().putValue(AnalysePreferences.FIRST_LAUNCH, "false");
				}
			});
		}
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea =  (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(dialogArea, SWT.NONE);			
		container.setLayout(new GridLayout(3,false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(container, SWT.NONE);
		label.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,false,false));
		label.setText(Messages.getString("WorkspaceSelectionDialog.LabelTitle")); 
		
		workspaceCombo = new Combo(container, SWT.READ_ONLY);
		workspaceCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
		workspaceCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				workspaceCombo.setToolTipText(workspaceCombo.getText());
			}
		});
		workspaceCombo.setItems(AnalysePreferences.getWorkspacesList());
		workspaceCombo.select(workspaceCombo.indexOf(AnalysePreferences.getLastWorkspaceUsed()));
		workspaceCombo.setToolTipText(workspaceCombo.getText());
		
		Button browseButton = new Button(container, SWT.NONE);
		browseButton.setLayoutData(new GridData(SWT.CENTER,SWT.CENTER,false,false));
		browseButton.setText(Messages.getString("WorkspaceSelectionDialog.ButtonTitle")); 
		
		browseButton.addSelectionListener(new SelectionAdapter()  {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(Display.getCurrent().getActiveShell());
				dlg.setText(Messages.getString("WorkspaceSelectionDialog.DirectoryDialogTitle")); 
				dlg.setMessage(Messages.getString("WorkspaceSelectionDialog.DirectoryDialogText")); 
				String workspace = dlg.open();
				if(workspace != null) {
					AnalysePreferences.addWorkspaceToWorkspacesList(workspace);
					workspaceCombo.setItems(AnalysePreferences.getWorkspacesList());
					workspaceCombo.select(workspaceCombo.indexOf(workspace));
					workspaceCombo.setToolTipText(workspace);
				}
			}
		});
		
		useAsDefaultButton = new Button(container, SWT.CHECK);
		useAsDefaultButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,3,1));
		useAsDefaultButton.setText(Messages.getString("WorkspaceSelectionDialog.UseDefaultWorkspaceDirLabelTitle")); 
		useAsDefaultButton.setSelection(AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.USE_DEFAULT_WORKSPACE_DIR));
		
		return dialogArea;
	}
	
	protected void okPressed() {
		if(!workspaceCombo.getText().equals("")) { 
			IPreferenceStore preferenceStore = AnalysePreferences.getPreferenceStore();
			for (int i = 0; i < getShell().getDisplay().getMonitors().length; i++) {
				Monitor monitor = getShell().getDisplay().getMonitors()[i];
				if(monitor == getShell().getMonitor()) preferenceStore.setValue(AnalysePreferences.MONITOR_NUMBER, i);
			}
			preferenceStore.setValue(AnalysePreferences.USE_DEFAULT_WORKSPACE_DIR, useAsDefaultButton.getSelection());
			preferenceStore.setValue(AnalysePreferences.LAST_WORKSPACE_USED, workspaceCombo.getText());
			AnalysePreferences.addWorkspaceToWorkspacesList(workspaceCombo.getText());
			super.okPressed();
		} else {
			MessageDialog messageDlg = new MessageDialog(getShell(), Messages.getString("WorkspaceSelectionDialog.MessageDialogTitle"), null, Messages.getString("WorkspaceSelectionDialog.MessageDialogText"), MessageDialog.INFORMATION,new String[] {"OK"}, 0);
			messageDlg.open();
		}
	}
	
}
