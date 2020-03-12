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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import analyse.Log;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.preferences.AnalysePreferences;
import analyse.preferences.WorkspacePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class ImportAction extends Action {
	
	public ImportAction() {
		super(Messages.getString("ImportAction.Title"),AS_PUSH_BUTTON); //$NON-NLS-1$
		setAccelerator(SWT.MOD1 | 'I');
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.IMPORT_ICON));
		setEnabled(true);
	}
	
	@Override
	public void run() {
		FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.MULTI);
		fileDialog.setFilterExtensions(new String[]{"*.zip"});
		if(fileDialog.open() != null) {
			String[] filesNames = fileDialog.getFileNames();
			for (int i = 0; i < filesNames.length; i++) {
				String experimentName = filesNames[i].replaceAll(".zip$", "");
				if(Experiments.getInstance().getExperimentByName(experimentName) != null) {
					Log.logErrorMessage(experimentName + Messages.getString("ImportAction.Error1"));
				} else {
					try {
						byte[] data = new byte[2048];
						BufferedOutputStream bufferedOutputStream;
						FileInputStream fileInputStream = new FileInputStream(new File(fileDialog.getFilterPath(), filesNames[i]));
						BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
						ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream);
						ZipEntry entry;
						File folder = new File(AnalysePreferences.getCurrentWorkspace(), experimentName);
						folder.mkdirs();
						if(folder.isFile()) {
							while((entry = zipInputStream.getNextEntry()) != null) {
								Log.logMessage(Messages.getString("ImportAction.Extracting") + entry.getName());
								folder = new File(AnalysePreferences.getCurrentWorkspace(), entry.getName().replaceAll("/\\w*.\\w*$", ""));
								folder.mkdirs();
								FileOutputStream fileOutputStream = new FileOutputStream(new File(AnalysePreferences.getCurrentWorkspace(), entry.getName()));
								bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 2048);
								int count;
								while ((count = zipInputStream.read(data, 0, 2048)) != -1) {
									bufferedOutputStream.write(data, 0, count);
								}
								bufferedOutputStream.flush();
								bufferedOutputStream.close();
							}
						}
						zipInputStream.close();
						Experiment experiment = new Experiment(experimentName);
						Experiments.getInstance().addNewExperiment(experiment);IResource[] resources = new IResource[] {experiment}; 
						WorkspacePreferences.addExperiment(experimentName);
						WorkspacePreferences.savePreferences();
						Experiments.notifyObservers(IResourceObserver.EXPERIMENT_CREATED, resources);
					} catch (FileNotFoundException e) {
						Log.logErrorMessage(e);
					} catch (IOException e) {
						Log.logErrorMessage(e);
					}
				}
				
			}
		}
	}

}
