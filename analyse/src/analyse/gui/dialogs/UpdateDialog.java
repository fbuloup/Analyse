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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.actions.UpdateAction;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class UpdateDialog extends TitleAreaDialog {

	private CLabel messageLabel;
	private Tree changesTree;
	private Text messagesText;
	private SashForm verboseSashForm;
	private boolean verbose;

	public UpdateDialog(Shell parentShell) {
		super(parentShell);
		verbose = AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.VERBOSE_DURING_UPDATE);
		if(verbose) setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		else setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("UpdateWizard.ShellTitle"));
		newShell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);		
		setTitle(Messages.getString("UpdateWizard.Title"));
		setMessage(Messages.getString("UpdateWizard.Text"));
		setTitleImage(ImagesUtils.getImage(IImagesKeys.UPDATE_BANNER));
		getButton(IDialogConstants.OK_ID).setText(Messages.getString("UpdateAnalyse.Update"));
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return contents;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		
		Composite dialogArea =  (Composite) super.createDialogArea(parent);
		Composite container = new Composite(dialogArea, SWT.NONE);			
		container.setLayout(new GridLayout(2,true));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		messageLabel = new CLabel(container, SWT.MULTI);
		messageLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,2,1));
		
		if(verbose) {
			verboseSashForm = new SashForm(container, SWT.VERTICAL);
			verboseSashForm.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
			changesTree = new Tree(verboseSashForm, SWT.BORDER);
		} else {
			changesTree = new Tree(container, SWT.BORDER);
			changesTree.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
		}
		 
		if(verbose) {
			messagesText = new Text(verboseSashForm, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			messagesText.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,2,1));
		}
		
		Button checkButton = new Button(container, SWT.PUSH);
		checkButton.setText(Messages.getString("UpdateWizard.CheckButtonTitle"));
		checkButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		checkButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				messageLabel.setText("");
				changesTree.removeAll();
				if(verbose) messagesText.setText("");
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				setErrorMessage(null);
				checkUpdate();
			}
		});
		Button clearButton = new Button(container, SWT.PUSH);
		clearButton.setText(Messages.getString("UpdateWizard.ClearButtonTitle"));
		clearButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		clearButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				messageLabel.setText("");
				changesTree.removeAll();
				if(verbose) messagesText.setText("");
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				setErrorMessage(null);
			}
		});
		setMessage(Messages.getString("UpdateWizard.Text"),IMessageProvider.INFORMATION);
		
		if(verbose) verboseSashForm.setWeights(new int[]{50,50});
		
		return dialogArea;
	}
	
	
	@Override
	protected void okPressed() {
		try {
			(new ProgressMonitorDialog(getShell())).run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(Messages.getString("UpdateAnalyse.GettingChanges"), UpdateAction.updateAnalyse.getChanges().length*100);
					try {
						if(UpdateAction.updateAnalyse.applyChanges(monitor)) {
							UpdateDialog.this.getShell().getDisplay().syncExec(new Runnable() {
								public void run() {
									MessageDialog.openInformation(getShell(), Messages.getString("UpdateAnalyse.Update"), Messages.getString("UpdateAnalyse.UpdateMessage"));
									AnalyseApplication.applyUpdates = true;
									UpdateDialog.this.close();
								}
							});
						}
					} catch (final IOException e) {
						Log.logErrorMessage(e);
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								UpdateDialog.this.setErrorMessage(e.toString());
								messageLabel.setText("");
								changesTree.removeAll();
								getButton(IDialogConstants.OK_ID).setEnabled(false);
								AnalyseApplication.applyUpdates = false;
							}
						});
					}
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			Log.logErrorMessage(e);
			setErrorMessage(e.toString());
		} catch (InterruptedException e) {
			Log.logErrorMessage(e);
			setErrorMessage(e.toString());
		}
	}
	
	private void checkUpdate() {
		try {
			setErrorMessage(null);
			(new ProgressMonitorDialog(getShell())).run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(Messages.getString("UpdateAnalyse.CheckUpdateMessage"), UpdateAction.updateAnalyse.getNbSteps());
					try {
						UpdateAction.updateAnalyse.doCompare(monitor);
					} catch (final IOException e) {
						Log.logErrorMessage(e);
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								UpdateDialog.this.setErrorMessage(e.toString());
							}
						});
					}
					monitor.done();
				}
			});
			if(UpdateAction.updateAnalyse.needUpdate()) {
				messageLabel.setText(Messages.getString("UpdateAnalyse.ChangesFoundMessage"));
				String[] coreChanges = UpdateAction.updateAnalyse.getCoreChanges();
				if(coreChanges.length > 0) {
					TreeItem coreTreeItem = new TreeItem(changesTree, SWT.NORMAL);
					coreTreeItem.setText(Messages.getString("UpdateAnalyse.AllChanges"));
					coreTreeItem.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON_SMALL));
					coreTreeItem.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					for (int i = 0; i < coreChanges.length; i++) {
						TreeItem item = new TreeItem(coreTreeItem, SWT.NORMAL);
						item.setText(coreChanges[i]);
						item.setImage(ImagesUtils.getImage(IImagesKeys.NEXT_TRIAL_ICON));
					}
					coreTreeItem.setExpanded(true);
				}
				/*String[] libraryChanges = UpdateAction.updateAnalyse.getLibraryChanges();
				if(libraryChanges.length > 0) {
					TreeItem libraryTreeItem = new TreeItem(changesTree, SWT.NORMAL);
					libraryTreeItem.setText(Messages.getString("UpdateAnalyse.LibraryChanges"));
					libraryTreeItem.setImage(ImagesUtils.getImage(IImagesKeys.ADD_FUNCTION_ICON));
					libraryTreeItem.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					for (int i = 0; i < libraryChanges.length; i++) {
						TreeItem item = new TreeItem(libraryTreeItem, SWT.NORMAL);
						item.setText(libraryChanges[i]);
						item.setImage(ImagesUtils.getImage(IImagesKeys.NEXT_TRIAL_ICON));
					}
					libraryTreeItem.setExpanded(true);
				}*/
				getButton(IDialogConstants.OK_ID).setEnabled(true);
			} else {
				messageLabel.setText(Messages.getString("UpdateAnalyse.NoChangesFoundMessage"));
				TreeItem treeItem = new TreeItem(changesTree, SWT.NORMAL);
				treeItem.setText(Messages.getString("UpdateAnalyse.NoChangesFoundMessage"));
			}
		} catch (InvocationTargetException e) {
			Log.logErrorMessage(e);
			setErrorMessage(e.toString());
		} catch (InterruptedException e) {
			Log.logErrorMessage(e);
			setErrorMessage(e.toString());
		}
	}
	
	public void logMessage(final String message) {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				messagesText.append(message + SWT.LF);
				
			}
		});
	}
	
}
