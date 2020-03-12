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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.w3c.dom.Node;

import functioneditor.windows.FunctionsEditorComposite;

import analyse.preferences.LibraryPreferences;
import analyse.resources.Messages;

public class OpenFileAction extends Action implements ISelectionChangedListener, IDoubleClickListener {

	
	private Node currentNode;
	private FunctionsEditorComposite functionsEditorComposite;
	private boolean isFunction;

	public OpenFileAction() {
		super(Messages.getString("matlabfunctioneditor.OpenFileAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setAccelerator(SWT.MOD1  | 'O');
		setToolTipText(Messages.getString("matlabfunctioneditor.OpenFileAction.Tooltip")); //$NON-NLS-1$
		setEnabled(false);
	}
	
	public void setFunctionsEditorComposite(FunctionsEditorComposite functionsEditorComposite) {
		this.functionsEditorComposite = functionsEditorComposite;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		if(event.getSelection().isEmpty()) {
			currentNode = null;
			setEnabled(false);
			return;
		}
		if(event.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection selection  = (IStructuredSelection) event.getSelection();
			if(selection.size() > 1) {
				setEnabled(false);
				return;
			}
			currentNode = (Node)selection.getFirstElement();		
			isFunction =  (currentNode.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute) != null);
			setEnabled(isFunction);
		}
	}
	
	@Override
	public void run() {
		if(functionsEditorComposite != null) {
			boolean editable = currentNode.getAttributes().getNamedItem(LibraryPreferences.editableAttribute).getNodeValue().equals(LibraryPreferences.trueAttributeValue);
			functionsEditorComposite.addFunctionEditorCTabItem(currentNode.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute).getNodeValue(), editable);
		}
	}

	public void doubleClick(DoubleClickEvent event) {
		if(currentNode != null)
			isFunction =  (currentNode.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute) != null);
			setEnabled(isFunction);
			if(isFunction) run();
	}
}
