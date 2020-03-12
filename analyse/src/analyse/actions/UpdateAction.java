/*******************************************************************************
 * Universit� d�Aix Marseille (AMU) - Centre National de la Recherche Scientifique (CNRS)
 * Copyright 2014 AMU-CNRS All Rights Reserved.
 * 
 * These computer program listings and specifications, herein, are
 * the property of Universit� d�Aix Marseille and CNRS
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.xml.sax.SAXException;

import analyse.Log;
import analyse.UpdateAnalyse;
import analyse.gui.dialogs.UpdateDialog;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class UpdateAction extends Action {
	
	private String repositoryPath;
	private String workingCopyPath;
	public static UpdateAnalyse updateAnalyse;
	
	public UpdateAction() {
		setText(Messages.getString("AutoUpdateAction.Title")); //$NON-NLS-1$
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.UPDATE_ICON));
		repositoryPath = AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.REPOSITORY_PATH);
		workingCopyPath = (new File(".")).getAbsolutePath().replaceAll(".$", "");
	}
	
	@Override
	public void run() {
//		repositoryPath = "file:///C:\\Documents and Settings\\frank\\Mes documents\\PROJETS\\Eclipse3.5Workspaces\\Analyse\\WINDOWS_ANALYSE_RELEASE\\";
//		repositoryPath = "file:///Users/frank/Documents/Projets/EclipseJEEIndigoWorkspaces/AnalyseWorkspace/MACOSX_INTEL_ANALYSE_RELEASE/";
		String distantXMLUpdateFilesListPath = repositoryPath + "updateFilesList.xml";
//		String distantLibraryPath = repositoryPath + "library.xml";
		String localPath = "file://" +  UpdateAnalyse.prefix + workingCopyPath;
		try {
			UpdateDialog updateDialog = new UpdateDialog(Display.getDefault().getActiveShell());
			updateAnalyse = new UpdateAnalyse(workingCopyPath, new URL(distantXMLUpdateFilesListPath), new URL(localPath), updateDialog);
			updateDialog.open();
		} catch (MalformedURLException e) {
			Log.logErrorMessage(e);
		} catch (ParserConfigurationException e) {
			Log.logErrorMessage(e);
		} catch (IOException e) {
			Log.logErrorMessage(e);
		} catch (SAXException e) {
			Log.logErrorMessage(e);
		}
	}
}
