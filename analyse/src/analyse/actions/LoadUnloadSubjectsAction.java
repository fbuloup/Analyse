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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import mathengine.MathEngineFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.gui.AnalyseApplicationWindow;
import analyse.gui.dialogs.LoadSubjectsDialog;
import analyse.gui.dialogs.SignalsFrequenciesTitleAreaDialog;
import analyse.gui.dialogs.SaveSubjectsDialog;
import analyse.model.DataFile;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Subject;
import analyse.preferences.AnalysePreferences;
import analyse.preferences.DataFilesPreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class LoadUnloadSubjectsAction extends Action  implements ISelectionChangedListener {
	
	private Subject[] selectedSubjects;
	private boolean askSavingFiles;
	private boolean fromSavingFiles;
	private int continueLoadUnLoad;
	
	public LoadUnloadSubjectsAction() {
		super(Messages.getString("LoadUnloadSubjectAction.Title"),AS_PUSH_BUTTON);
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.LOAD_UNLOAD_SUBJECT_ICON));
		setAccelerator(SWT.MOD1 | 'L');			
		setEnabled(false);
	}
	
	public void run() {	
		final Subject[] localSelectedSubjects = selectedSubjects;
		ArrayList<Subject> modifiedSubjects = new ArrayList<Subject>(0);
		for (int i = 0; i < localSelectedSubjects.length; i++) if(localSelectedSubjects[i].isModified()) modifiedSubjects.add(localSelectedSubjects[i]);
		int response = Window.OK;
		if(modifiedSubjects.size() > 0) response = new SaveSubjectsDialog(null, modifiedSubjects.toArray(new Subject[modifiedSubjects.size()])).open();
		if(response == Window.OK) {
			for (int i = 0; i < modifiedSubjects.size(); i++) modifiedSubjects.get(i).setModified(false);
			fromSavingFiles =AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.ALWAYS_LOAD_FROM_SAVING_FILE);
			if(!fromSavingFiles && askSavingFiles) {
				LoadSubjectsDialog loadSubjectsDialog = new LoadSubjectsDialog(Display.getCurrent().getActiveShell());
				response = loadSubjectsDialog.open();
				if(response == Window.CANCEL) return;
				if(response == Window.OK) fromSavingFiles = loadSubjectsDialog.getLoadFromSavingFiles();
			}
			if(MathEngineFactory.getInstance().getMathEngine() != null) {
				
				for (int i = 0; i < localSelectedSubjects.length; i++) {
					boolean sampleFrequenciesFileExist = false;
					boolean sauExist = false;
					final Subject subject = localSelectedSubjects[i];
					final String[] files = subject.getDataFiles(fromSavingFiles);
					for (int j = 0; j < files.length; j++) {
						if (files[j].endsWith("SampleFrequencies.properties")) sampleFrequenciesFileExist = true;
						sauExist = sauExist || files[j].endsWith("sau");
					}
					if(((Experiment)subject.getParent()).getType().equals(DataFilesPreferences.getDocometreTypeString()) && sauExist && !sampleFrequenciesFileExist){
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								SignalsFrequenciesTitleAreaDialog dialog =  new SignalsFrequenciesTitleAreaDialog(Display.getDefault().getActiveShell(), files, subject);
								continueLoadUnLoad = dialog.open();
								if(continueLoadUnLoad == Window.OK) {
									Properties sampleFrequenciesProperties = new Properties();
									HashMap<String, String> sf = dialog.getSamplesFrequencies();
									Set<String> key = sf.keySet();
									for (Iterator<String> iterator = key.iterator(); iterator.hasNext();) {
										String signalName = iterator.next();
										sampleFrequenciesProperties.setProperty(signalName, sf.get(signalName));
									}
									try {
										FileOutputStream fos = new FileOutputStream(subject.getAbsolutePath() + File.separator + "SampleFrequencies.properties");
										sampleFrequenciesProperties.store(fos,"Sample frequencies file");
										fos.close();
										subject.addResources(new IResource[]{new DataFile(subject, "SampleFrequencies.properties")});
										
									} catch (FileNotFoundException e) {
										Log.logErrorMessage(e);
									} catch (IOException e) {
										Log.logErrorMessage(e);
									}
								}
							}
						});
					} 
				}
				
				if(continueLoadUnLoad == Window.CANCEL) return;
				
				if(!MathEngineFactory.getInstance().getMathEngine().isStarted()) {
					Log.logMessage(Messages.getString("LoadUnloadSubjectAction.PleaseWait"));
					MathEngineFactory.getInstance().getMathEngine().start(AnalysePreferences.matlabScriptsPath, AnalysePreferences.getCurrentWorkspace());
					AnalyseApplicationWindow.startStopMathEngineAction.update();
					Log.logMessage(Messages.getString("LoadUnloadSubjectAction.DoneTask"));
				}
				IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						int totalWork = localSelectedSubjects.length>1?localSelectedSubjects.length:IProgressMonitor.UNKNOWN;
						monitor.beginTask(Messages.getString("LoadUnloadSubjectAction.LoadUnloadSubjectsTask"), totalWork);
						long beginTime = System.currentTimeMillis();
						for (int i = 0; i < localSelectedSubjects.length; i++) {
							long beginTimeSubject = System.currentTimeMillis();
							Subject subject = localSelectedSubjects[i];
							boolean loaded = subject.isLoaded();
							monitor.subTask(subject.getLocalPath());
							
							//If not loaded & ((DOCOMETRE & SAU) | ICE)  => refresh datafiles
							boolean sauExist = false;
							boolean refreshDataFiles = false;
							boolean isDocometre = ((Experiment)subject.getParent()).getType().equals(DataFilesPreferences.getDocometreTypeString());
							boolean isICE = ((Experiment)subject.getParent()).getType().equals(DataFilesPreferences.getICETypeString());
							if(!loaded && (isDocometre || isICE)) {
								final String[] files = subject.getDataFiles(fromSavingFiles);
								for (int j = 0; j < files.length; j++) {
									sauExist = sauExist || files[j].endsWith("sau") || files[j].endsWith("txt");
									if(sauExist) {
										refreshDataFiles = true;
										break;
									}
								}
							}
							
							if(loaded) loaded = MathEngineFactory.getInstance().getMathEngine().unloadSubject(subject.getLocalPath());
							else loaded = MathEngineFactory.getInstance().getMathEngine().loadSubject(subject.getLocalPath(), ((Experiment)subject.getParent()).getType(), subject.getDataFiles(fromSavingFiles));
							
							//If loaded & refresh data files
							if(loaded && refreshDataFiles) subject.refreshDataFiles();
							
							subject.setLoaded(loaded);
							Experiments.notifyObservers(IResourceObserver.LOADED, new IResource[]{subject});
							monitor.worked(1);
							Log.logMessage(Messages.getString("LoadUnloadSubjectAction.TimeToLoad") + subject.getLocalPath()  + "' (ms) : " + (System.currentTimeMillis()-beginTimeSubject));
							if(monitor.isCanceled()) {
								monitor.done();
								throw new InterruptedException(Messages.getString("LoadUnloadSubjectAction.OperationCanceled"));
							}
						}
						monitor.done();
						Log.logMessage(Messages.getString("LoadUnloadSubjectAction.TotalTimeToLoad") + (System.currentTimeMillis()-beginTime));
					}
				};
				AnalyseApplication.getAnalyseApplicationWindow().run(true,true, true, false, runnableWithProgress);
				Experiments.notifyObservers(IResourceObserver.SELECTION_CHANGED, localSelectedSubjects);
			} else Log.logErrorMessage(Messages.getString("LoadUnloadSubjectAction.OOOPPPSSS"));
			askSavingFiles = false;
			for (int i = 0; i < localSelectedSubjects.length; i++) askSavingFiles = askSavingFiles || !localSelectedSubjects[i].isLoaded();
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		Object[] objects = ((TreeSelection)event.getSelection()).toArray();
		int nbSubjects = 0;
		for (int i = 0; i < objects.length; i++) if(objects[i] instanceof Subject) nbSubjects++;
		if(nbSubjects > 0) {
			askSavingFiles = false;
			selectedSubjects = new Subject[nbSubjects];
			int subjectIndex = 0;
			for (int i = 0; i < objects.length; i++) 
				if(objects[i] instanceof Subject) {
					selectedSubjects[subjectIndex] = (Subject) objects[i];
					askSavingFiles = askSavingFiles || !selectedSubjects[subjectIndex].isLoaded();
					subjectIndex++;
				}
		}
		setEnabled(nbSubjects > 0);
	}

}
