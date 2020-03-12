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
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.gui.dialogs.SubjectsSelectionDialog;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Processing;
import analyse.model.Subject;
import analyse.resources.Messages;

public final class RunBatchProcessingAction extends Action  implements ISelectionChangedListener {
	
	protected IResource[] selectedProcesses ;

	public RunBatchProcessingAction() {
		super(Messages.getString("RunBatchProcessingAction.Title"),AS_PUSH_BUTTON);
		setEnabled(false);
	}
	
	public void run() {
		SubjectsSelectionDialog subjectsSelectionDialog = new SubjectsSelectionDialog(null,selectedProcesses);
		if(subjectsSelectionDialog.open() == Window.OK) {
			final Subject[] subjects = subjectsSelectionDialog.getSubjectsList();
			final Processing[] processes = subjectsSelectionDialog.getProcessesList();
			IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(Messages.getString("RunBatchProcessingAction.Running"), IProgressMonitor.UNKNOWN);
					processes[0].runMulptipleProcesses(processes, subjects);
//					for (int i = 0; i < subjects.length; i++) {
//						for (int j = 0; j < processes.length; j++) {
//							monitor.subTask("Subject " + subjects[i].getLocalPath() + " - Process " + processes[j].getLocalPath());
//							Log.logMessage("Running process " + processes[j].getLocalPath() + " on " + subjects[i].getLocalPath());
//							processes[j].run(subjects[i].getLocalPath());
//							monitor.worked(1);
//							if(monitor.isCanceled()) {
//								monitor.done();
//								throw new InterruptedException("Operation Canceled");
//							}
//						}
//					}
					monitor.done();
					Log.logMessage(Messages.getString("RunBatchProcessingAction.UpdateAnalyse"));
					Experiments.notifyObservers(IResourceObserver.PROCESS_RUN, selectedProcesses);
				}
			};
			Log.logMessage(Messages.getString("RunBatchProcessingAction.RunningWait"));
			AnalyseApplication.getAnalyseApplicationWindow().run(true,true, false, true, runnableWithProgress);
			Log.logMessage(Messages.getString("RunBatchProcessingAction.Done"));
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		TreeSelection selection = ((TreeSelection)event.getSelection());
		Object[] elements = selection.toArray();
		HashSet<Processing> processings = new HashSet<Processing>(0);
		for (int i = 0; i < elements.length; i++)
			if(elements[i] instanceof Processing) processings.add((Processing) elements[i]);
		setEnabled(processings.size() > 0);
		selectedProcesses = processings.toArray(new Processing[processings.size()]);
		Arrays.sort(selectedProcesses);
	}

}
