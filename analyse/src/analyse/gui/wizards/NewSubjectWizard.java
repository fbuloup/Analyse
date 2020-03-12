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
package analyse.gui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;

import analyse.Log;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.resources.Messages;

public final class NewSubjectWizard extends Wizard {
	
	private String experimentName;
	private Hashtable<String, String[]> subjectsAndDataFiles;
	private ExperimentPage experimentPage;
	private Experiment selectedExperiment;
	
	public NewSubjectWizard(Experiment selectedResource) {
		super();
		setWindowTitle(Messages.getString("NewSubjectWizard.Title"));
		setNeedsProgressMonitor(true);
		this.selectedExperiment = selectedResource;
	}
	
	@Override
	public void addPages() {
		experimentPage = new ExperimentPage(false, selectedExperiment);
		addPage(experimentPage);
	}
	
	@Override
	public boolean performFinish() {
		experimentName = ((ExperimentPage)getPage(ExperimentPage.PAGE_NAME)).getExperimentName();
		subjectsAndDataFiles = ((ExperimentPage)getPage(ExperimentPage.PAGE_NAME)).getSubjectsAndDataFiles();	
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
					
					int nbFiles = 0;
					Set<String> keys = subjectsAndDataFiles.keySet();
					for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
						String key = iterator.next();
						String[] dataFiles = subjectsAndDataFiles.get(key);
						nbFiles += dataFiles.length;
					}
					
					progressMonitor.beginTask(Messages.getString("NewSubjectWizard.ProgressText"),1 + nbFiles); //3*subjectsAndDataFiles.size());
					Experiments.createNewSubjectsFolders(experimentName, subjectsAndDataFiles, progressMonitor);
					if(selectedExperiment == null) selectedExperiment = Experiments.getInstance().getExperimentByName(experimentName);
					selectedExperiment.addNewSubjectsByNames(subjectsAndDataFiles.keySet().toArray(new String[subjectsAndDataFiles.size()]), progressMonitor);
					progressMonitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			Log.logErrorMessage(e);
		} catch (InterruptedException e) {
			Log.logErrorMessage(e);
		}
		return true;
	}

}
