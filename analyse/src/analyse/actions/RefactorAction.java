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

import java.util.HashSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import analyse.gui.dialogs.RefactorDialog;
import analyse.model.Chart;
import analyse.model.IResource;
import analyse.model.Processing;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public final class RefactorAction extends Action  implements ISelectionChangedListener {
	
	protected IResource[] selectedResources ;

	public RefactorAction() {
		super(Messages.getString("RefactorAction.Title"),AS_PUSH_BUTTON);
		//setAccelerator(SWT.MOD1 | 'C');			
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.REFACTOR_ICON));
		setEnabled(false);
	}
	
	public void run() {
		RefactorDialog refactorDialog = new RefactorDialog(Display.getDefault().getActiveShell(),selectedResources);
		if(refactorDialog.open() == Window.OK) {
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		TreeSelection selection = ((TreeSelection)event.getSelection());
		Object[] elements = selection.toArray();
		HashSet<IResource> resources = new HashSet<IResource>(0);
		for (int i = 0; i < elements.length; i++)
			if(elements[i] instanceof Chart || elements[i] instanceof Processing) resources.add((IResource) elements[i]);
		setEnabled(resources.size() > 0);
		selectedResources = resources.toArray(new IResource[resources.size()]);
	}

}
