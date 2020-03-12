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
import org.eclipse.jface.window.Window;

import analyse.Log;
import analyse.gui.dialogs.SaveSubjectsDialog;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Subject;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class StartStopMathEngineAction extends Action {
	
	public StartStopMathEngineAction() {
		super(Messages.getString("StartStopMathEngineAction.StartTitle"),AS_PUSH_BUTTON);
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.RUN_ICON));
		setEnabled(true);
	}
	
	public void run() {	
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		if(mathEngine != null && mathEngine.isStarted()) {
			int response = saveModifiedSubjects();
			if(response == Window.OK) {
				mathEngine.stop();
				setText(Messages.getString("StartStopMathEngineAction.StartTitle"));
				setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.RUN_ICON));
				
				IResource[] resources = Experiments.getInstance().getChildren();
				for (int i = 0; i < resources.length; i++) {
					IResource[] resources2 = resources[i].getChildren();
					for (int j = 0; j < resources2.length; j++) 
						if(resources2[j] instanceof Subject) ((Subject)resources2[j]).setLoaded(false);
				}
				
				Experiments.notifyObservers(IResourceObserver.MATH_ENGINE_STOPPED, new IResource[]{null});
			}
		} else {
			if(mathEngine != null) {
				Log.logMessage(Messages.getString("StartStopMathEngineAction.Starting"));
				if(mathEngine.start(AnalysePreferences.matlabScriptsPath, AnalysePreferences.getCurrentWorkspace())) {
					Log.logMessage(Messages.getString("StartStopMathEngineAction.Done"));
					setText(Messages.getString("StartStopMathEngineAction.StopTitle"));
					setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.STOP_ICON));
				}
			}
		}
	}
	
	
	private int saveModifiedSubjects() {
		ArrayList<Subject> modifiedSubject = new ArrayList<Subject>(0);
		IResource[] resources = Experiments.getInstance().getChildren();
		for (int i = 0; i < resources.length; i++) {
			IResource[] resources2 = resources[i].getChildren();
			for (int j = 0; j < resources2.length; j++) 
				if(resources2[j] instanceof Subject) if(((Subject)resources2[j]).isModified()) modifiedSubject.add((Subject) resources2[j]);
		}
		
		int response = Window.OK;
		if(modifiedSubject.size() > 0) {
			response = new SaveSubjectsDialog(null, modifiedSubject.toArray(new Subject[modifiedSubject.size()])).open();
		}
		return response;
	}

	public void update() {
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		if(mathEngine.isStarted()) {
			setText(Messages.getString("StartStopMathEngineAction.StopTitle"));
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.STOP_ICON));
		} else {
			setText(Messages.getString("StartStopMathEngineAction.StartTitle"));
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.RUN_ICON));
		}
	}

}
