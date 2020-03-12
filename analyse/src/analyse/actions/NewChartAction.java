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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.gui.dialogs.NewChartInputDialog;
import analyse.model.Chart;
import analyse.model.ChartsTypes;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.Folder;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.MultiCharts;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class NewChartAction extends Action  implements ISelectionChangedListener {
	
	private IResource selectedResource;
	private IResource[] sistersResources;
	
	IInputValidator inputValidator = new IInputValidator(){
		public String isValid(String newText) {				
			if(!newText.matches("^[a-zA-Z][a-zA-Z0-9_]{0,31}$")) return Messages.getString("NewChartAction.ErrorMessageText1"); 
			for (int i = 0; i < sistersResources.length; i++) {
				if(sistersResources[i].getName().equalsIgnoreCase(newText + Chart.EXTENSION)) return Messages.getString("NewChartAction.ErrorMessageText2"); 
			}
			return null;
		}
	};

	public NewChartAction() {
		super(Messages.getString("NewChartAction.Title"), AS_PUSH_BUTTON);
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.CHART_EDITOR_ICON));	
		setEnabled(false);
	}
	
	public void run() {	
		sistersResources = selectedResource.getChildren();
		NewChartInputDialog newChartDialog = new NewChartInputDialog(Display.getDefault().getActiveShell(),Messages.getString("NewChartAction.DialogTitle"),Messages.getString("NewChartAction.DialogMessage"),null,inputValidator);
		if(newChartDialog.open() == Window.OK) {
			try {
				IResource chart = null;
				if(newChartDialog.getType().equals(ChartsTypes.MULTI_CHARTS_ID_STRING)) chart = new MultiCharts(selectedResource, newChartDialog.getName(), newChartDialog.getType());
				else chart = new Chart(selectedResource, newChartDialog.getName(), newChartDialog.getType());
				selectedResource.addResources(new IResource[]{chart});
				Experiments.notifyObservers(IResourceObserver.CHART_CREATED, new IResource[]{chart});
				AnalyseApplication.getAnalyseApplicationWindow().openEditor(chart);
				chart.registerToExperimentsObservers();
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
