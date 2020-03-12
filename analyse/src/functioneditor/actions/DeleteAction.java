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

import java.io.File;
import java.util.Iterator;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import functioneditor.windows.FunctionsEditorComposite;

import analyse.preferences.LibraryPreferences;

import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class DeleteAction extends Action implements ISelectionChangedListener {

	private IStructuredSelection selection;
	private FunctionsEditorComposite functionsEditorComposite;

	public DeleteAction() {
		super(Messages.getString("matlabFunctionEditor.DeleteAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setAccelerator(SWT.MOD1 | SWT.DEL);
		setToolTipText(Messages.getString("matlabFunctionEditor.DeleteAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.DELETE_ICON));
		setEnabled(false);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		if(event.getSelection().isEmpty()) {
			setEnabled(false);
			return;
		}

		if(event.getSelection() instanceof IStructuredSelection) {
			selection  = (IStructuredSelection) event.getSelection();
			if(selection.size() == 0) {
				setEnabled(false);
				return;
			}
			if(((Node)selection.getFirstElement()).getNodeName() == "root")  //$NON-NLS-1$
			setEnabled(false);
			else {
				Node editableNode = ((Node)selection.getFirstElement()).getAttributes().getNamedItem(LibraryPreferences.editableAttribute);
				boolean editable = false;
				if(editableNode != null) editable = editableNode.getNodeValue().equals(LibraryPreferences.trueAttributeValue);
				setEnabled(editable);			
			}
		}
	}
	
	private void deleteNode(Node node, boolean deleteFile) {
		if(node.hasChildNodes()) {
			NodeList nodeList = node.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				deleteNode(nodeList.item(i),deleteFile);
			}
		}
		
		boolean isFunction =  (node.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute) != null);
		
		if(isFunction && deleteFile) {					
			String filePath = node.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute).getNodeValue() + ".m"; //$NON-NLS-1$
			filePath = LibraryPreferences.getRootDirectory() + filePath;
			File file = new File(filePath);
			if(file.exists()) file.delete();
			if(functionsEditorComposite != null)
			functionsEditorComposite.removeFunctionEditorCTabItem(node.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute).getNodeValue());
		}
		if(node.getParentNode() != null)
		node.getParentNode().removeChild(node);
	}
	
	public void run() {		
		MessageDialog messageDialog = new MessageDialog(Display.getCurrent().getActiveShell(),Messages.getString("matlabFunctionEditor.DeleteAction.DeletingFunctionsFoldersTitle"),null,Messages.getString("matlabFunctionEditor.DeleteAction.DeletingFunctionsFoldersText"),MessageDialog.QUESTION, new String[] {Messages.getString("matlabFunctionEditor.DeleteAction.CancelButtonTitle"),Messages.getString("matlabFunctionEditor.DeleteAction.DeleteLibrariesButtonTitle"),Messages.getString("matlabFunctionEditor.DeleteAction.DeleteAllButtonTitle")},0 ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		int response = messageDialog.open();
		if(response > 0) {
			for (@SuppressWarnings("rawtypes")
			Iterator iterator = selection.iterator(); iterator.hasNext();) {
				Node node = (Node) iterator.next();				
				deleteNode(node,response == 2);			
			}	
			LibraryPreferences.notifyObservers(LibraryPreferences.getLibrary(),null);	
			LibraryPreferences.save();
			
		}		
	}

	public void setFunctionsEditorComposite(FunctionsEditorComposite functionsEditorComposite) {
		this.functionsEditorComposite = functionsEditorComposite;
		
	}
}
