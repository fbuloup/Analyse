/*******************************************************************************
 * Universit� d�Aix Marseille (AMU) - Centre National de la Recherche Scientifique (CNRS)
 * Copyright 2014 AMU-CNRS All Rights Reserved.
 * 
 * These computer program listings and specifications, herein, are
 * the property of Universit� d�Aix Marseille and CNRS
 * shall not be reproduced or copied or used in whole or in part as
 * the basis for manufacture or sale of items without written permission.
 * For a license agreement, please contact:
 * <mailto: licensing@sattse.com> 
 * 
 * Author : Frank BULOUP
 * Institut des Sciences du Mouvement - franck.buloup@univ-amu.fr
 ******************************************************************************/
package analyse.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import analyse.Log;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.Folder;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class NewFolderAction extends Action  implements ISelectionChangedListener {
	
	private IResource selectedResource;
	private IResource[] sistersResources;
	
	IInputValidator inputValidator = new IInputValidator(){
		public String isValid(String newText) {				
			if(!newText.matches("^[a-zA-Z][a-zA-Z0-9_]{0,31}$")) return Messages.getString("NewFolderAction.ErrorMessageText1"); 
			for (int i = 0; i < sistersResources.length; i++) {
				if(sistersResources[i].getName().equalsIgnoreCase(newText + Folder.EXTENSION)) return Messages.getString("NewFolderAction.ErrorMessageText2"); 
			}
			return null;
		}
	};

	public NewFolderAction() {
		super(Messages.getString("NewFolderAction.Title"), AS_PUSH_BUTTON);
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.FOLDER_ICON));	
		setEnabled(false);
	}
	
	public void run() {	
		sistersResources = selectedResource.getChildren();
		InputDialog newFolderDialog = new InputDialog(Display.getDefault().getActiveShell(),Messages.getString("NewFolderAction.DialogTitle"),Messages.getString("NewFolderAction.DialogMessage"),null,inputValidator);
		if(newFolderDialog.open() == Window.OK) {
			try {
				final Folder folder = new Folder(selectedResource, newFolderDialog.getValue());
				selectedResource.addResources(new IResource[]{folder});
				Experiments.notifyObservers(IResourceObserver.FOLDER_CREATED, new IResource[]{folder});
			} catch (InterruptedException e) {
				Log.logErrorMessage(e);
			}
		}
		
	}

	public void selectionChanged(SelectionChangedEvent event) {
		setEnabled(false);
		TreeSelection selection = (TreeSelection)event.getSelection();
		if(selection.size() == 1) {
			selectedResource = (IResource) selection.getFirstElement();
			setEnabled(selectedResource != null && (selectedResource instanceof Experiment || selectedResource instanceof Folder));
		}
	}

}
