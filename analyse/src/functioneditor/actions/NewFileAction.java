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


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import analyse.Log;
import analyse.preferences.LibraryPreferences;

import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class NewFileAction extends Action implements ISelectionChangedListener {
	
	private Node currentNode;

	public NewFileAction() {
		super(Messages.getString("matlabfunctioneditor.NewFileAction.Title"), AS_PUSH_BUTTON); //$NON-NLS-1$
		setAccelerator(SWT.MOD1  | 'N');
		setToolTipText(Messages.getString("matlabfunctioneditor.NewFileAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.ADD_FUNCTION_ICON));
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
			boolean editable = currentNode.getAttributes().getNamedItem(LibraryPreferences.editableAttribute).getNodeValue().equals(LibraryPreferences.trueAttributeValue);
			editable = editable && (currentNode.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute) == null);
			setEnabled(editable);		
		}
	}
	
	public void run() {
		final String[] functionsNamesList = (new File(LibraryPreferences.getRootDirectory())).list();
		
		for (int i = 0; i < functionsNamesList.length; i++) {
			functionsNamesList[i] = functionsNamesList[i].replaceAll(".m$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		IInputValidator inputValidator = new IInputValidator(){

			public String isValid(String newText) {				
				if(!newText.matches("^[a-zA-Z][a-zA-Z0-9_]{0,31}$")){ //$NON-NLS-1$
					return Messages.getString("matlabfunctioneditor.NewFileAction.ErrorMessageText1"); //$NON-NLS-1$
				}			
				
				for (int i = 0; i < functionsNamesList.length; i++) {
					if(functionsNamesList[i].equalsIgnoreCase(newText)) return Messages.getString("matlabfunctioneditor.NewFileAction.ErrorMessageText2"); //$NON-NLS-1$
				}
				return null;
			}
			
		};
		
		InputDialog inputDialog = new InputDialog(Display.getCurrent().getActiveShell(),Messages.getString("matlabfunctioneditor.NewFileAction.InputDialogText"),Messages.getString("matlabfunctioneditor.NewFileAction.InputDialogTitle"),Messages.getString("matlabfunctioneditor.NewFileAction.InputDialogLabel"),inputValidator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		if(inputDialog.open() == Window.OK) {
			Document libraryDocument = LibraryPreferences.getLibraryDocument();
			Element element = libraryDocument.createElement(inputDialog.getValue());
			element.setAttribute(LibraryPreferences.functionNameAttribute, inputDialog.getValue());
			element.setAttribute(LibraryPreferences.shortDescriptionAttribute, "Short description");			 //$NON-NLS-1$
			element.setAttribute(LibraryPreferences.editableAttribute, LibraryPreferences.trueAttributeValue);
			currentNode.appendChild(element);
			LibraryPreferences.notifyObservers(currentNode,element);
			LibraryPreferences.save();
			try {
				BufferedInputStream is = new BufferedInputStream(new FileInputStream("./matlabscripts/functionTemplate.m")); //$NON-NLS-1$
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(LibraryPreferences.getRootDirectory() + element.getNodeName() + ".m"));			 //$NON-NLS-1$
				int b; // the byte read from the file
				while ((b = is.read( )) != -1) {
					os.write(b);
				}
				is.close( );				
				os.close( );
			} catch (FileNotFoundException e) {
				Log.logErrorMessage(e);
			} catch (IOException e) {
				Log.logErrorMessage(e);
			}
			
			
		}
	}
}
