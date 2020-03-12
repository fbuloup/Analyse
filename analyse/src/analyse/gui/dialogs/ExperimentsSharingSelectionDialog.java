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

import java.util.Arrays;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class ExperimentsSharingSelectionDialog extends TitleAreaDialog {

	private String[] experiments;
	private String[] selection = new String[0];

	public ExperimentsSharingSelectionDialog(Shell parentShell, String[] experiments) {
		super(parentShell);
		setShellStyle(SWT.MAX | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		Arrays.sort(experiments);
		this.experiments = experiments;
	}
	
	
	@Override
	public void create() {
		super.create();
		getShell().setText(Messages.getString("ExperimentsSharingSelectionDialog.ShellTitle"));
		getShell().setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		setTitle(Messages.getString("ExperimentsSharingSelectionDialog.Title"));
		int monitorNumber = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.MONITOR_NUMBER);		
		Monitor monitor = getShell().getDisplay().getMonitors()[monitorNumber];
		
		Rectangle bounds = monitor.getBounds ();
		Rectangle rect = getShell().getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		getShell().setLocation (x, y);
		setTitleImage(ImagesUtils.getImage(IImagesKeys.EXPERIMENTS_SHARING_BANNER));				
		
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea =  (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(dialogArea, SWT.NONE);			
		container.setLayout(new GridLayout(1,false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final CheckboxTableViewer experimentsTableViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
		experimentsTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		experimentsTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}
		});	
		experimentsTableViewer.setLabelProvider(new ILabelProvider() {
			public void removeListener(ILabelProviderListener listener) {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void dispose() {
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public String getText(Object element) {
				return (String) element;
			}
			
			public Image getImage(Object element) {
				return ImagesUtils.getImage(IImagesKeys.EXPERIMENT_ICON);
			}
		});
		experimentsTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object[] objects = experimentsTableViewer.getCheckedElements();
				selection = new String[objects.length];
				for (int i = 0; i < objects.length; i++) selection[i] = (String) objects[i];
			}
		});		
		
		
		Button selectAllButton = new Button(container, SWT.PUSH);
		selectAllButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false));
		selectAllButton.setText(Messages.getString("ExperimentsSharingSelectionDialog.SelectAll"));
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				experimentsTableViewer.setAllChecked(true);
				Object[] objects = experimentsTableViewer.getCheckedElements();
				selection = new String[objects.length];
				for (int i = 0; i < objects.length; i++) selection[i] = (String) objects[i];
			}
		});
		
		experimentsTableViewer.setInput(experiments);
		
		return dialogArea;
	}


	public String[] getSelection() {
		return selection;
	}
	
}
