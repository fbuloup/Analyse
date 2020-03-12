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

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.jface.action.Action;

import analyse.Log;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Subject;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class SaveAllAction extends Action implements /*ISelectionChangedListener,*/ IResourceObserver {
	
	public SaveAllAction() {
		super(Messages.getString("SaveAllAction.Title"),Action.AS_PUSH_BUTTON); //$NON-NLS-1$
		setToolTipText(Messages.getString("SaveAllAction.Tooltip")); //$NON-NLS-1$
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.SAVE_ALL_ICON));
		setEnabled(false);
		Experiments.getInstance().addExperimentObserver(this);
	}
	
	@Override
	public void run() {
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		IResource[] experiments = Experiments.getInstance().getChildren();
		for (int i = 0; i < experiments.length; i++) {
			Experiment experiment = (Experiment) experiments[i];
			Subject[] subjects = experiment.getSubjects();
			for (int j = 0; j < subjects.length; j++) {
				if(subjects[j].isModified()) {
					boolean saved = mathEngine.saveSubject(subjects[j].getLocalPath());
					if(saved) {
						subjects[j].setModified(false);
						Experiments.notifyObservers(IResourceObserver.SAVED, new IResource[]{subjects[j]});
						Log.logMessage(Messages.getString("MathEngine.Done"));
					} else Log.logErrorMessage(Messages.getString("SaveAction.Impossible") + subjects[j].getLocalPath());
				}
			}
		}
		setEnabled(false);
		for (int i = 0; i < experiments.length; i++) {
			Experiment experiment = (Experiment) experiments[i];
			Subject[] subjects = experiment.getSubjects();
			for (int j = 0; j < subjects.length; j++) {
				setEnabled(isEnabled() || subjects[j].isModified());
			}
		}
	}

	public void update(int message, IResource[] resources) {
		setEnabled(false);
		IResource[] experiments = Experiments.getInstance().getChildren();
		for (int i = 0; i < experiments.length; i++) {
			Experiment experiment = (Experiment) experiments[i];
			Subject[] subjects = experiment.getSubjects();
			for (int j = 0; j < subjects.length; j++) {
				if(subjects[j].isModified()) {
					setEnabled(true);
					break;
				}
			}
		}
	}

}
