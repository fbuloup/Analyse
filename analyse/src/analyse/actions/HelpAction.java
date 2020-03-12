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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.program.Program;

import analyse.Log;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class HelpAction extends Action {
	
	private static Server server;
	private static boolean started = false;
	
	public HelpAction() {
		setText(Messages.getString("HelpAction.Title"));
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.HELP_CONTENT_ICON));
		setEnabled(true);
	}
	
	@Override
	public void run() {
		if(server == null) {
			server = new Server(8181);
			WebAppContext handler = new WebAppContext();
			handler.setContextPath("/analyseHelp");
			handler.setWar("./help/analyseHelp.war");
			server.setHandler(handler);
			try {
				server.start();
				started = true;
			} catch (Exception e) {
				Log.logErrorMessage(e);
			}
		}
//		Shell helpShell = new Shell(Display.getDefault());
//		helpShell.setLayout(new FillLayout());
//		Browser browser = new Browser(helpShell, SWT.NORMAL);
//		helpShell.open();
//		browser.setUrl("http://127.0.0.1:8181/analyseHelp/analyse");
		String language = AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.LANGUAGE);
		if(language.equals(AnalysePreferences.FRENCH_LANGUAGE))	Program.launch("http://127.0.0.1:8181/analyseHelp/application");
		else if(language.equals(AnalysePreferences.ENGLISH_LANGUAGE))	Program.launch("http://127.0.0.1:8181/analyseHelp/application");
		else Program.launch("http://127.0.0.1:8181/analyseHelp/application");
	}
	
	public static void stopJettyServer() {
		if(started)
			try {
				server.stop();
			} catch (Exception e) {
				Log.logErrorMessage(e);
			}
	}

}
