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
package analyse.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import analyse.model.Chart;
import analyse.model.Experiments;
import analyse.model.Folder;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.MultiCharts;
import analyse.model.Note;
import analyse.model.Processing;
import analyse.resources.Messages;

public class RenameAction extends Action  implements ISelectionChangedListener {
	
	private IResource selectedResource;
	private IResource[] sistersResources;

	IInputValidator inputValidator = new IInputValidator(){
		public String isValid(String newText) {				
			if(!newText.matches("^[a-zA-Z][a-zA-Z0-9_]{0,31}$")) return Messages.getString("RenameAction.ErrorMessageText1");
			String extension = "";
			if(selectedResource instanceof Folder) extension = Folder.EXTENSION;
			if(selectedResource instanceof Chart) extension = Chart.EXTENSION;
			if(selectedResource instanceof MultiCharts) extension = Chart.EXTENSION;
			if(selectedResource instanceof Processing) extension = Processing.EXTENSION;
			
			for (int i = 0; i < sistersResources.length; i++) {
				if(sistersResources[i] != selectedResource && sistersResources[i].getName().equals(newText + extension)) return Messages.getString("RenameAction.ErrorMessageText2"); 
			}
			return null;
		}
	};
	
	public RenameAction() {
		super(Messages.getString("RenameAction.Title"),AS_PUSH_BUTTON);
		setAccelerator(SWT.MOD1 | 'R');			
		setEnabled(false);
	}
	
	public void run() {	
		sistersResources = selectedResource.getParent().getChildren();
		String initialValue = selectedResource.getName();
		if(selectedResource instanceof Folder) initialValue = initialValue.replaceAll(Folder.EXTENSION + "$", "");
		if(selectedResource instanceof Chart) initialValue = initialValue.replaceAll(Chart.EXTENSION + "$", "");
		if(selectedResource instanceof MultiCharts) initialValue = initialValue.replaceAll(Chart.EXTENSION + "$", "");
		if(selectedResource instanceof Processing) initialValue = initialValue.replaceAll(Processing.EXTENSION + "$", "");
		if(selectedResource instanceof Note) initialValue = initialValue.replaceAll(Note.EXTENSION + "$", "");
		InputDialog renameDialog = new InputDialog(Display.getDefault().getActiveShell(),Messages.getString("RenameAction.DialogTitle"),Messages.getString("RenameAction.Message"),initialValue,inputValidator);
		if(renameDialog.open() == Window.OK) {
			if(!selectedResource.getName().equals(renameDialog.getValue())) {
				selectedResource.rename(renameDialog.getValue());
				Experiments.notifyObservers(IResourceObserver.RENAMED, new IResource[]{selectedResource});
			}
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		setEnabled(false);
		selectedResource = null;
		if(((TreeSelection)event.getSelection()).size() == 1) {
			selectedResource = (IResource) ((TreeSelection)event.getSelection()).getFirstElement();
			setEnabled(true);
		}
	}

}
