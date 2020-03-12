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

import mathengine.ILog;

import analyse.gui.AnalyseApplicationWindow;
import analyse.gui.ConsolesView;

public final class Log implements ILog {
	
	private static BufferedWriter logFile;

	private static Log log = new Log();
	
	private Log() {
	}
	
	public static ILog getInstance() {
		return log;
	}

	public static void logMessage(String message) {
		System.out.println(message);
		ConsolesView consolesView = AnalyseApplicationWindow.getConsolesView();
		if(consolesView != null) consolesView.logMessage(message);
		if(logFile != null)
			try {
				logFile.write(message);
				logFile.newLine();
				logFile.flush();
			} catch (IOException e) {
				logFile = null;
				logErrorMessage(e);
			}
	}
	
	public static void logWarningMessage(String message) {
		System.out.println(message);
		ConsolesView consolesView = AnalyseApplicationWindow.getConsolesView();
		if(consolesView != null) consolesView.logWarningMessage(message);
		if(logFile != null)
			try {
				logFile.write(message);
				logFile.newLine();
				logFile.flush();
			} catch (IOException e) {
				logFile = null;
				logErrorMessage(e);
			}
	}
	
	public static void logErrorMessage(String errorMessage) {
		System.err.println(errorMessage);
		ConsolesView consolesView = AnalyseApplicationWindow.getConsolesView();
		if(consolesView != null) consolesView.logErrorMessage(errorMessage);
		if(logFile != null)
			try {
				logFile.write(errorMessage);
				logFile.newLine();
				logFile.flush();
			} catch (IOException e) {
				logFile = null;
				logErrorMessage(e);
			}
	}

	public static void logErrorMessage(StackTraceElement[] stackTrace) {
		for (int i = 0; i < stackTrace.length; i++) {
			logErrorMessage("\t" + stackTrace[i].toString());
		}
	}

	public static void logErrorMessage(Exception e) {
		logErrorMessage(e.getClass().getCanonicalName() + (e.getLocalizedMessage()!=null?" : " + e.getLocalizedMessage():""));
		logErrorMessage(e.getStackTrace());
		if(e.getCause() != null) logErrorCause(e.getCause());
	}
	
	private static void logErrorCause(Throwable e) {
		logErrorMessage("Caused by : " + e.getClass().getCanonicalName() + (e.getLocalizedMessage()!=null?" : " + e.getLocalizedMessage():""));
		logErrorMessage(e.getStackTrace());
		if(e.getCause() != null) logErrorCause(e.getCause());
	}
	
	public static void initLogFile(String fileName) {
		try {
			 logFile = new BufferedWriter(new FileWriter(new File(fileName)));
		} catch (IOException e) {
			logFile = null;
			logErrorMessage(e);
		}
	}
	
	public static void closeLogFile() {
		try {
			logFile.close();
		} catch (IOException e) {
			logFile = null;
			logErrorMessage(e);
		}
	}

	public void IlogErrorMessage(String errorMessage) {
		Log.logErrorMessage(errorMessage);
	}
	
	public void IlogErrorMessage(Exception e) {
		Log.logErrorMessage(e);
	}
	
	public void IlogMessage(String message) {
		Log.logMessage(message);
	}

	public static void logErrorsMessages(String[] errorsMessages) {
		for (int i = 0; i < errorsMessages.length; i++) {
			logErrorMessage(errorsMessages[i]);
		}
	}
	
	public static void logWarningsMessages(String[] warningsMessages) {
		for (int i = 0; i < warningsMessages.length; i++) {
			logWarningMessage(warningsMessages[i]);
		}
	}
}
