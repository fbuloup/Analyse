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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.Folder;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Subject;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class PasteAction extends Action  implements ISelectionChangedListener {
	
	private IResource selectedResource;
	private IResource[] sistersResources;

	public PasteAction() {
		super(Messages.getString("PasteAction.Title"),AS_PUSH_BUTTON);
		setAccelerator(SWT.MOD1 | 'V');			
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.PASTE_ICON));
		setEnabled(false);
	}
	
	public void run() {	
		final IResource localSelectedResource = selectedResource;//selectedResource may change as viewer is often refreshed
		sistersResources = localSelectedResource.getChildren();
		final ArrayList<IResource> resourcesToPaste = new ArrayList<IResource>(0);
		for (int i = 0; i < CopyAction.selectedResources.length; i++) {
			IResource currentResource = CopyAction.selectedResources[i];
			boolean hasParent = false;
			for (int j = 0; j < CopyAction.selectedResources.length; j++) {
				hasParent = currentResource.hasParent(CopyAction.selectedResources[j]);
				if(hasParent) break;
			}
			if(!hasParent) {
				boolean hasSisterWithSameName = false;
				for (int j = 0; j < sistersResources.length; j++) {
					hasSisterWithSameName = sistersResources[j].getName().equalsIgnoreCase(currentResource.getName());
					if(hasSisterWithSameName) break;
				}
				
				if(!hasSisterWithSameName) resourcesToPaste.add(currentResource);
				else Log.logMessage(Messages.getString("PasteAction.ResourceExists") + currentResource.getName());
			}
		}
		IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(Messages.getString("PasteAction.CopyTask"), resourcesToPaste.size());
				for (int i = 0; i < resourcesToPaste.size(); i++) {
					IResource resource = resourcesToPaste.get(i);
					if(!(resource instanceof Experiment)) {
						if(resource instanceof Subject && localSelectedResource instanceof Folder) {
							Log.logMessage(Messages.getString("PasteAction.CantCopySubjectInFolder") + resource.getAbsolutePath() + Messages.getString("PasteAction.To") + localSelectedResource.getAbsolutePath() + ") !");
						}
						else {
							if(resource instanceof Subject) {
								String experimentTypeSrc = ((Experiment)resource.getParent()).getType();
								IResource experimentDest = localSelectedResource;
								while (!(experimentDest instanceof Experiment)) experimentDest = experimentDest.getParent();
								String experimentTypeDest = ((Experiment)experimentDest).getType();
								if(!experimentTypeDest.equals(experimentTypeSrc)) {
									Log.logMessage(Messages.getString("PasteAction.CantCopyToDifferentExperiment") + resource.getName() + " " + Messages.getString("PasteAction.From") + experimentTypeSrc + " " + Messages.getString("PasteAction.To") + experimentTypeDest + ")");
									monitor.worked(1);
									continue;
								}
							}
							try {
								monitor.subTask(resource.getAbsolutePath() + Messages.getString("PasteAction.To") + localSelectedResource.getAbsolutePath());
								resource.copyTo(localSelectedResource);
							} catch (IOException e) {
								Log.logErrorMessage(e);
								Display.getDefault().syncExec(new Runnable() {
									public void run() {
										monitor.setCanceled(true);									
									}
								});	
							}
							if(monitor.isCanceled()) {
								monitor.done();
								throw new InterruptedException(Experiments.OPERATION_CANCELED);								
							}
						}
					} else {
						Log.logMessage(Messages.getString("PasteAction.CantCopyExperiment") + resource.getAbsolutePath() + Messages.getString("PasteAction.CreateNewOne") );
					}
					monitor.worked(1);
				}
				monitor.done();
				Experiments.notifyObservers(IResourceObserver.COPIED, new IResource[]{localSelectedResource});
			}
		};
		AnalyseApplication.getAnalyseApplicationWindow().run(true, true, true, false, runnableWithProgress);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		TreeSelection selection = ((TreeSelection)event.getSelection());
		setEnabled(selection.size() == 1 && CopyAction.selectedResources.length > 0 && CopyAction.done);
		if(isEnabled()) {
			selectedResource =(IResource) selection.getFirstElement();
			setEnabled(selectedResource instanceof Folder || selectedResource instanceof Experiment);
		}
	}

}
