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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

import analyse.actions.HelpAction;
import analyse.gui.AnalyseApplicationWindow;
import analyse.gui.dialogs.WorkspaceSelectionDialog;
import analyse.preferences.AnalysePreferences;
import analyse.preferences.DataFilesPreferences;
import analyse.preferences.LibraryPreferences;
import analyse.preferences.WorkspacePreferences;
import analyse.resources.CPlatform;
import analyse.resources.Messages;

public final class AnalyseApplication {
	
	public static String linuxLauncherName = "startAnalyse.run";
	public static String macLauncherName = "startAnalyse.app";
	public static String windowsLauncherName = "startAnalyse.exe";
	
	public static boolean restart;
	public static boolean withLog;
	public static boolean applyUpdates;
	
	public static String analyseDirectory = System.getProperty("user.dir") + File.separator;
	
	private static AnalyseApplicationWindow analyseApplicationWindow;
	private static String updateDirectory = "UPDATE";
	private static String applyUpdateFileName = "applyAnalyseUpdate.jar";
	private static boolean logFile = false;
	
	private static boolean isRunning  = true;
	
	public static AnalyseApplicationWindow getAnalyseApplicationWindow() {
		return analyseApplicationWindow;
	}
	
	private static void startUp(String[] args) {
		ArrayList<String> arguments = new ArrayList<String>(args.length);
		for (int i = 0; i < args.length; i++) arguments.add(args[i]);
		
		if(arguments.indexOf("-log") != -1) {
			logFile = true;
			Log.initLogFile((new File(".")).getAbsolutePath() + File.separator  + "log" + (new Date()).getTime()  + ".txt");
		}
		
		if(arguments.indexOf("-logFTPServer") != -1) {
			try {
				Logger logger = Logger.getRootLogger();
				logger.setLevel(Level.DEBUG);
				FileAppender fileAppender = new FileAppender(new SimpleLayout(), analyseDirectory + "FTP_log.txt");
				logger.addAppender(fileAppender);
			} catch (IOException e) {
				Log.logErrorMessage(e);
			}
		}
		
		Log.logMessage("Analyse startup");
		System.out.println("OS : " + CPlatform.getOsName());
		System.out.println("Arch : " + CPlatform.getOsArch());
	}
	
	private static void shutDown() {
		HelpAction.stopJettyServer();
		saveLauncherParams();
		if(applyUpdates) {
			File applyUpdateFile = new File(analyseDirectory + updateDirectory + File.separator + applyUpdateFileName);
			if(applyUpdateFile.exists()) {
				if(applyUpdateFile.delete()) {
					File copyApplyUpdateFile = new File(analyseDirectory + applyUpdateFileName);
					try {
						Utils.copyFile(applyUpdateFile, copyApplyUpdateFile);
					} catch (IOException e) {
						Log.logErrorMessage(e);
					}
				} else Log.logErrorMessage("Impossible to delete : " + applyUpdateFile.getAbsolutePath());				
			}
		}
		if(restart) {
			String launcherName = "";
			if(CPlatform.isLinux()) launcherName = linuxLauncherName;
			if(CPlatform.isMacOSX()) launcherName = macLauncherName;
			if(CPlatform.isWindows()) launcherName = windowsLauncherName;
			launcherName = analyseDirectory + File.separator + launcherName;
			Program.launch(launcherName);
		}
		Log.logMessage("Analyse ended");
		if(logFile) Log.closeLogFile();
	}
	
	public static void main(final String[] args) {
		Display.setAppName("Analyse");
		Display display = Display.getDefault();
		
		SplashScreen.open(3);
		
		Runnable runnable = new Runnable() {
			public void run() {
				
				startUp(args);
				SplashScreen.work(1);
				
				Log.logMessage("Analyse directory : " + analyseDirectory);
				DataFilesPreferences.initialize();
				AnalysePreferences.initialize();
				LibraryPreferences.initialize();

				SplashScreen.work(1);
				
				boolean workspaceExists = (new File(AnalysePreferences.getCurrentWorkspace())).exists();
				int response = Window.OK;
				if(!AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.USE_DEFAULT_WORKSPACE_DIR) || !workspaceExists) {
					if(!workspaceExists) AnalysePreferences.removeWorkspaceFromWorkspacesList(AnalysePreferences.getCurrentWorkspace());
					WorkspaceSelectionDialog workspaceSelectionDialog = new WorkspaceSelectionDialog(null);
					response = workspaceSelectionDialog.open();
				}
				if(response == Window.OK) {
					SplashScreen.show();
					Log.logMessage("Analyse workspace directory : " + AnalysePreferences.getCurrentWorkspace());
					WorkspacePreferences.initialize(AnalysePreferences.getCurrentWorkspace());
					analyseApplicationWindow = new AnalyseApplicationWindow(null);
					analyseApplicationWindow.setBlockOnOpen(true);
					analyseApplicationWindow.open();
				}
				AnalysePreferences.savePreferences();

				shutDown();
				isRunning = false;
			}
		};
		
		SplashScreen.launch(runnable);
		
		while (isRunning) 
			if (!Display.getDefault().readAndDispatch ()) 
				Display.getDefault().sleep ();
		
		display.dispose();

		
	}
	
	private static void deleteLauncherParamsFile(String launcherParamsName) {
		File file = new File(launcherParamsName);
		if(!file.delete()) Log.logErrorMessage(Messages.getString("AnalyseApplication.ImpossibleDelete") + launcherParamsName);
	}
	
	public static void saveLauncherParams() {
		try {
			int javaHeapSpaceMax = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.JAVA_HEAP_MAX);
			int javaHeapSpaceMin = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.JAVA_HEAP_MIN);
			String launcherParamsName = "analyse.ini";
			launcherParamsName = analyseDirectory + File.separator + launcherParamsName;
			deleteLauncherParamsFile(launcherParamsName);
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(launcherParamsName));
			String params = "" + applyUpdates + " " + javaHeapSpaceMin + " " + javaHeapSpaceMax + " " + withLog;
			bufferedWriter.write(params);
			bufferedWriter.close();
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}
	}

}
