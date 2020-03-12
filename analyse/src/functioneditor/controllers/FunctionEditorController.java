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
package functioneditor.controllers;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import analyse.preferences.AnalysePreferences;

import functioneditor.utils.AnalyseCodeScanner;
import functioneditor.windows.MatlabFunctionEditorWindow;

public class FunctionEditorController implements ControlListener {
	
	public static final String MATLAB_PARTITIONING = "matlab_partitionning"; //$NON-NLS-1$
	public static AnalyseCodeScanner matlabCodeScanner;
	public static boolean functionEditorOpened = false;
	
	public FunctionEditorController() {
		matlabCodeScanner = new AnalyseCodeScanner();
		/*Shell shell = null;
		if(Display.getDefault().getActiveShell() == null) {
			ImagesUtils.initialize(Display.getDefault());
			shell = new Shell(Display.getDefault(),SWT.SHELL_TRIM);
			shell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		}
		else {
			shell = new Shell(Display.getDefault().getActiveShell(), SWT.SHELL_TRIM);
		}*/
		MatlabFunctionEditorWindow matlabFunctionEditorWindow = new MatlabFunctionEditorWindow(this, Display.getDefault().getActiveShell());
		matlabFunctionEditorWindow.setBlockOnOpen(true);
		functionEditorOpened = true;
		matlabFunctionEditorWindow.open();
		functionEditorOpened = false;
	}

	public void controlMoved(ControlEvent e) {
		if (e.widget instanceof Shell) {
			Shell theShell = (Shell)e.widget;	
			if(!theShell.getMaximized()) {
				AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.FUNCTION_EDITOR_WINDOW_POSITION_LEFT, theShell.getBounds().x);
				AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.FUNCTION_EDITOR_WINDOW_POSITION_TOP, theShell.getBounds().y);
			}
		}	
	}

	public void controlResized(ControlEvent e) {
		if (e.widget instanceof Shell) {
			Shell theShell = (Shell)e.widget;									
			if(!theShell.getMaximized()) {
				AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.FUNCTION_EDITOR_WINDOW_HEIGHT, theShell.getBounds().height);
				AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.FUNCTION_EDITOR_WINDOW_WIDTH, theShell.getBounds().width);
			}
			
			AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.FUNCTION_EDITOR_WINDOW_MAXIMIZED, theShell.getMaximized());			
		}
	}
}
