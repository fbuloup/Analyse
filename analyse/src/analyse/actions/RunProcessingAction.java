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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Processing;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public final class RunProcessingAction extends Action  implements ISelectionChangedListener {
	
	protected Processing processing ;

	public RunProcessingAction() {
		super(Messages.getString("RunProcessingAction.Title"),AS_PUSH_BUTTON);
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.RUN_PROCESS_ICON));
		setEnabled(false);
	}
	
	public void run() {
		IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(Messages.getString("RunProcessingAction.Running") + processing.getLocalPath(), IProgressMonitor.UNKNOWN);
				processing.run(null);
				if(monitor.isCanceled()) {
					monitor.done();
					throw new InterruptedException(Messages.getString("RunProcessingAction.Canceled"));
				}
				monitor.done();
			}
		};
		Log.logMessage(Messages.getString("RunProcessingAction.Running") + processing.getLocalPath());
		AnalyseApplication.getAnalyseApplicationWindow().run(true,true, true, true, runnableWithProgress);
		Log.logMessage(Messages.getString("RunProcessingAction.Done"));
		Experiments.notifyObservers(IResourceObserver.PROCESS_RUN, new IResource[]{processing});
	}
	
	public void run(Processing processing) {
		this.processing = processing;
		run();
	}

	public void selectionChanged(SelectionChangedEvent event) {
		TreeSelection selection = ((TreeSelection)event.getSelection());
		setEnabled(false);
		if(selection.size() == 1)
			if(selection.getFirstElement() instanceof Processing) {
				setEnabled(true);
				processing = (Processing) selection.getFirstElement();
			}
				
	}

}
