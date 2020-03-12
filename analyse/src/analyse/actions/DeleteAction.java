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
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import analyse.AnalyseApplication;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class DeleteAction extends Action  implements ISelectionChangedListener {
	
	private IResource[] resources;
	private IResource[] localResources;

	public DeleteAction() {
		setText(Messages.getString("DeleteAction.Title")); //$NON-NLS-1$
		setAccelerator(SWT.MOD1 | SWT.DEL);
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.DELETE_ICON));
		setEnabled(false);	
	}
	
	public void run() {	
		localResources = resources;//resources may change as viewer is often refreshed
		IResource[] localLocalResource = localResources;
		if(MessageDialog.openConfirm(Display.getDefault().getActiveShell(), Messages.getString("DeleteAction.Delete"), Messages.getString("DeleteAction.DeleteResourceQuestion"))) {
			IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(Messages.getString("DeleteAction.Deleting"), localResources.length);
					while(localResources.length > 0) {
						localResources = deleteNonParentResources(localResources, monitor);
					}
					monitor.done();
				}
				private IResource[] deleteNonParentResources(IResource[] resources, IProgressMonitor monitor) throws InterruptedException {
					ArrayList<IResource> isParentResource = new ArrayList<IResource>(0);
					ArrayList<IResource> isNonParentResource = new ArrayList<IResource>(0);
					for (int i = 0; i < resources.length; i++) {
						IResource currentResource = resources[i];
						for (int j = 0; j < resources.length; j++) {
							if(resources[j].hasParent(currentResource)) {
								isParentResource.add(currentResource);
								break;
							}
						}
						if(isParentResource.indexOf(currentResource) == -1) isNonParentResource.add(currentResource);
					}
					for (int i = 0; i < isNonParentResource.size(); i++) {
						monitor.subTask(isNonParentResource.get(i).getName());
						if(!isNonParentResource.get(i).delete()) throw new InterruptedException(Messages.getString("DeleteAction.ImpossibleToDelete") + isNonParentResource.get(i).getName());
						else monitor.worked(1);
						if(monitor.isCanceled()) {
							monitor.done();
							throw new InterruptedException(Messages.getString("DeleteAction.Canceled"));
						}
					}
					return isParentResource.toArray(new IResource[isParentResource.size()]);
				}
			};
			AnalyseApplication.getAnalyseApplicationWindow().run(true, true, false, false, runnableWithProgress); 
			Experiments.notifyObservers(IResourceObserver.DELETED, localLocalResource);
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		Object[] objects = ((TreeSelection)event.getSelection()).toArray();
		resources = new IResource[objects.length];
		for (int i = 0; i < objects.length; i++) resources[i] = (IResource) objects[i];
		setEnabled(resources.length > 0);
	}

}
