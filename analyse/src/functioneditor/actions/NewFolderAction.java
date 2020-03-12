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
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import analyse.preferences.LibraryPreferences;

import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class NewFolderAction extends Action implements ISelectionChangedListener {
	
	Node currentNode = null;
	
	public NewFolderAction() {
		super(Messages.getString("matlabfunctioneditor.NewFolderAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setToolTipText(Messages.getString("matlabfunctioneditor.NewFolderAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.LIBRARY_ICON));
		setEnabled(false);
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
			
			boolean isFunction =  (currentNode.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute) != null);
			boolean editable = currentNode.getAttributes().getNamedItem(LibraryPreferences.editableAttribute).getNodeValue().equals(LibraryPreferences.trueAttributeValue);
			
			setEnabled(editable && !isFunction);			
		}
	}
	
	@Override
	public void run() {
		
		IInputValidator inputValidator = new IInputValidator(){

			public String isValid(String newText) {				
				if(!newText.matches("^[a-zA-Z][a-zA-Z0-9_]{0,31}$")){ //$NON-NLS-1$
					return Messages.getString("matlabfunctioneditor.NewFolderAction.ErrorMessage"); //$NON-NLS-1$
				}	
				if(currentNode.getOwnerDocument().getElementsByTagName(newText).getLength() != 0) return Messages.getString("matlabfunctioneditor.NewFolderAction.ErrorMessage2");
				return null;
			}
			
		};
		InputDialog inputDialog = new InputDialog(Display.getCurrent().getActiveShell(),Messages.getString("matlabfunctioneditor.NewFolderAction.InputDialogText"),Messages.getString("matlabfunctioneditor.NewFolderAction.InputDialogTitle"),Messages.getString("matlabfunctioneditor.NewFolderAction.InputDialogLabelTitle"),inputValidator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if(inputDialog.open() == Window.OK) {
			Document libraryDocument = LibraryPreferences.getLibraryDocument();
			Element element = libraryDocument.createElement(inputDialog.getValue());
			element.setAttribute(LibraryPreferences.editableAttribute, LibraryPreferences.trueAttributeValue);
			currentNode.appendChild(element);
			LibraryPreferences.notifyObservers(currentNode,element);
			LibraryPreferences.save();
		}
	}
}
