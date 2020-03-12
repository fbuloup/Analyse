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
package analyse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import analyse.resources.CPlatform;
import analyse.resources.ImagesUtils;

public class SplashScreen {
	
	private static Shell splash;
	private static ProgressBar bar;
	private static Label progessInfoLabel;

	public static void open(int totalWork) {
		splash = new Shell(SWT.ON_TOP);
		splash.setBounds(0, 0, 460, 280);
		splash.setBackgroundMode(SWT.INHERIT_FORCE);
		splash.setBackgroundImage(ImagesUtils.getSplashImage());
		
		bar = new ProgressBar(splash, SWT.NONE);
		bar.setMaximum(totalWork);
		progessInfoLabel = new Label(splash, SWT.NONE);
		progessInfoLabel.setText("Loading Analyse. Please Wait...");
		
		FormLayout layout = new FormLayout();
		splash.setLayout(layout);
		
		FormData progessInfoLabelData = new FormData ();
		progessInfoLabelData.left = new FormAttachment (0, 5);
		progessInfoLabelData.right = new FormAttachment (100, 0);
		int position = -35;
		if(CPlatform.isWindows()) position = -40;
		progessInfoLabelData.bottom = new FormAttachment (100, position);
		progessInfoLabel.setLayoutData(progessInfoLabelData);
		
		FormData progressData = new FormData ();
		progressData.left = new FormAttachment (0, 5);
		progressData.right = new FormAttachment (100, -5);
		progressData.bottom = new FormAttachment (100, -20);
		bar.setLayoutData(progressData);
		
		Rectangle splashRect = splash.getBounds();
		Rectangle displayRect = Display.getDefault().getBounds();
		int x = (displayRect.width - splashRect.width) / 2;
		int y = (displayRect.height - splashRect.height) / 2;
		splash.setLocation(x, y);
		splash.open();
	}
	
	public static void launch(Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}
	
	public static void close() {
		splash.close();
	}
	
	public static void hide() {
		splash.setVisible(false);
	}
	
	public static void show() {
		splash.setVisible(true);
	}
	
	public static void work(int work) {
		bar.setSelection(bar.getSelection() + work);
	}
}
