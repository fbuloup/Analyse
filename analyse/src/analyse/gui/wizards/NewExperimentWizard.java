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
import analyse.model.Experiments;
import analyse.resources.Messages;

public final class NewExperimentWizard extends Wizard {
	
	private String experimentName;
	private String experimentType;
	private Hashtable<String, String[]> subjectsAndDataFiles;
	private ExperimentPage experimentPage;
	
	public NewExperimentWizard() {
		super();
		setWindowTitle(Messages.getString("NewExperimentWizard.Title"));
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public void addPages() {
		experimentPage = new ExperimentPage(true, null);
		addPage(experimentPage);
	}
	
	@Override
	public boolean performFinish() {
		experimentName = ((ExperimentPage)getPage(ExperimentPage.PAGE_NAME)).getExperimentName();
		experimentType  = ((ExperimentPage)getPage(ExperimentPage.PAGE_NAME)).getExperimentType();
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
					
					progressMonitor.beginTask(Messages.getString("NewExperimentWizard.ProgressText"),2 + nbFiles);// 2*subjectsAndDataFiles.size());
					Experiments.createNewExperiment(experimentName, experimentType, subjectsAndDataFiles, progressMonitor);
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
