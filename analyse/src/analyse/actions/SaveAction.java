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

import java.util.ArrayList;

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;

import analyse.Log;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Subject;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class SaveAction extends Action implements ISelectionChangedListener, IResourceObserver {
	
	ArrayList<Subject> subjectList = new ArrayList<Subject>(0);
	
	public SaveAction() {
		super(Messages.getString("SaveAction.Title"),Action.AS_PUSH_BUTTON); //$NON-NLS-1$
		setToolTipText(Messages.getString("SaveAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.SAVE_ICON));
		setEnabled(false);
		setAccelerator(SWT.MOD1 | 'S');
		Experiments.getInstance().addExperimentObserver(this);
	}
	
	@Override
	public void run() {
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		for (int i = 0; i < subjectList.size(); i++) {
			Subject subject = subjectList.get(i);
			boolean saved = mathEngine.saveSubject(subject.getLocalPath());
			if(saved) {
				subject.setModified(false);
				Experiments.notifyObservers(IResourceObserver.SAVED, new IResource[]{subject});
				Log.logMessage(Messages.getString("MathEngine.Done"));
			} else Log.logErrorMessage(Messages.getString("SaveAction.Impossible") + subject.getLocalPath());
		}
		setEnabled(false);
		for (int i = 0; i < subjectList.size(); i++) setEnabled(isEnabled() || subjectList.get(i).isModified());
	}

	public void selectionChanged(SelectionChangedEvent event) {
		setEnabled(false);
		Object[] resources = ((TreeSelection)event.getSelection()).toArray();
		subjectList.clear();
	 	for (int i = 0; i < resources.length; i++)
			if(resources[i] instanceof Subject) {
				subjectList.add((Subject)resources[i]);
				setEnabled(isEnabled() || ((Subject)resources[i]).isModified());
			}
	}

	public void update(int message, IResource[] resources) {
		setEnabled(false);
		for (int i = 0; i < resources.length; i++) {
			if(resources[i] instanceof Subject) {
				if(((Subject)resources[i]).isModified())
					if(subjectList.indexOf(resources[i]) > -1) setEnabled(true);
			}
		}
		
	}

}
