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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;

import analyse.AnalyseApplication;
import analyse.model.Chart;
import analyse.model.IResource;
import analyse.model.MultiCharts;
import analyse.model.Note;
import analyse.model.Processing;
import analyse.resources.Messages;

public class OpenAction extends Action  implements ISelectionChangedListener {
	
	private TreeSelection selection;

	public OpenAction() {
		super(Messages.getString("OpenAction.Title"),AS_PUSH_BUTTON);
		setAccelerator(SWT.MOD1 | 'O');			
		setEnabled(false);
	}
	
	public void run() {
		Object[] selectedResources = selection.toArray();
		for (int i = 0; i < selectedResources.length; i++) 
			if(selectedResources[i] instanceof Chart || selectedResources[i] instanceof Processing || selectedResources[i] instanceof Note || selectedResources[i] instanceof MultiCharts)
				AnalyseApplication.getAnalyseApplicationWindow().openEditor((IResource) selectedResources[i]);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		selection = ((TreeSelection)event.getSelection());
		setEnabled(selection.size() > 0);
	}

}
