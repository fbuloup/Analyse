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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Subject;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class SaveSubjectsDialog extends TitleAreaDialog {

	private TableViewer subjectsModifiedListViewer;
	private Subject[] subjects;

	public SaveSubjectsDialog(Shell parentShell, Subject[] subjects) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.subjects = subjects;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("SaveWizard.Title"));
		newShell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);		
		setTitle(Messages.getString("SavePage.Title"));
		setMessage(Messages.getString("SavePage.Text"));
		setTitleImage(ImagesUtils.getImage(IImagesKeys.SAVE_BANNER));
		return contents;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea =  (Composite) super.createDialogArea(parent);
		subjectsModifiedListViewer = new TableViewer(dialogArea,SWT.CHECK | SWT.BORDER);
		subjectsModifiedListViewer.getTable().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		subjectsModifiedListViewer.setContentProvider(new IStructuredContentProvider () {
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		subjectsModifiedListViewer.setLabelProvider(new ILabelProvider() {
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
				return ((Subject)element).getLocalPath();
			}
			public Image getImage(Object element) {
				return null;
			}
		});
		subjectsModifiedListViewer.setInput(subjects);
		
		Button selectAllButton = new Button(dialogArea,SWT.PUSH);
		selectAllButton.setText(Messages.getString("SavePage.InvertSelectionButtonTitle")); //$NON-NLS-1$
		selectAllButton.setLayoutData(new GridData(SWT.FILL,SWT.NONE,false,false));
		selectAllButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = subjectsModifiedListViewer.getTable().getItems();
				for (int i = 0; i < items.length; i++) {
					TableItem tableItem = items[i];
					tableItem.setChecked(!tableItem.getChecked());
				}
			}
		});
		return dialogArea;
	}
	
	
	
	@Override
	protected void okPressed() {
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		TableItem[] items = subjectsModifiedListViewer.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			TableItem tableItem = items[i];
			if(tableItem.getChecked()) {
				Subject subject = (Subject) subjectsModifiedListViewer.getElementAt(i);
				mathEngine.saveSubject(subject.getLocalPath());
				subject.setModified(false);
				Experiments.notifyObservers(IResourceObserver.SAVED, new IResource[]{subject});
			}
			
		}
		super.okPressed();
	}

}
