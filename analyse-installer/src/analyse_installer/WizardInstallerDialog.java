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
package analyse_installer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import resources.IImagesKeys;
import resources.ImagesUtils;
import resources.CPlatform;

public class WizardInstallerDialog extends WizardDialog {

	public static final String REPOSITORY_URL = "REPOSITORY_URL"; //$NON-NLS-1$
	
	private static PreferenceStore preferenceStore;
	
	public static boolean startAnalyse = false;

	public WizardInstallerDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}
	
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);		
		
		getShell().setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));

		parent.getShell().setSize(500, 500);
		Monitor monitor = parent.getDisplay().getMonitors()[0];
		
		Rectangle bounds = monitor.getBounds ();
		Rectangle rect = parent.getShell().getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		parent.getShell().setLocation (x, y);							
		
		return contents;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.FINISH_ID).setText("Install");
		getButton(IDialogConstants.CANCEL_ID).setText("Quit");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		preferenceStore = new PreferenceStore(); //$NON-NLS-1$
		Display.setAppName("Analyse Installer");
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setBounds(-1000, -1000, 0, 0);
		shell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		if(CPlatform.isWindows()) {
			shell.setText("Analyse Installer");
			shell.open();
		}
		try {
			((PreferenceStore) preferenceStore).load(display.getClass().getResourceAsStream("/install.properties"));
			WizardInstallerDialog wizardInstallerDialog = new WizardInstallerDialog(shell, new WizardInstaller());
			wizardInstallerDialog.open();
			if(startAnalyse) {
				try {
					String launcherName = "";
					if(CPlatform.isLinux()) launcherName = WizardInstaller.installDirectory + File.separator + WizardInstaller.linuxLauncherName;
					if(CPlatform.isMacOSX()) launcherName = "open -a " + WizardInstaller.installDirectory + File.separator + WizardInstaller.macBaseLauncherName;
					if(CPlatform.isWindows()) launcherName = WizardInstaller.installDirectory + File.separator + WizardInstaller.windowsLauncherName;
					String[] cmd;
					if(CPlatform.isLinux() || CPlatform.isMacOSX()) 
		    			cmd = new String[]{ "/bin/sh", "-c", launcherName};
		    		else cmd = new String[]{ "cmd", "/C", launcherName};
		            //Start analyse
					Process process = Runtime.getRuntime().exec(cmd, null, new File(WizardInstaller.installDirectory));
					BufferedReader bufferedErrorStreamReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					BufferedReader bufferedInputStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = null;
					String totalLines = "";
					if(bufferedErrorStreamReader.ready())
						while ( (line = bufferedErrorStreamReader.readLine()) != null) totalLines = totalLines + line + "/n";
					if(bufferedInputStreamReader.ready())
						while ( (line = bufferedInputStreamReader.readLine()) != null) totalLines = totalLines + line + "/n";
					if(!totalLines.equals("")) MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", totalLines);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			shell.dispose();
			display.dispose();
		} catch (IOException e) {
			MessageDialog.openError(shell, "Analyse Installer", "I've not been able to load install properties file !");
		}
	}
	
	public static String getRepositoryURL() {
		return preferenceStore.getString(REPOSITORY_URL);
	}
	
}


/*new Thread() {
@Override
public void run() {
	String execCmd ;
	if(System.isWindows()) execCmd = "\"" + WizardInstaller.installDirectory + File.separator + WizardInstaller.launcherName + "\"";
	else execCmd = WizardInstaller.installDirectory + File.separator + WizardInstaller.launcherName + " &"; 
	java.lang.System.out.println(execCmd);
	String[] cmd = new String[]{ WizardInstaller.shell, WizardInstaller.shellOption, execCmd};
	try {
		final Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		java.lang.System.exit(0);
		// Consommation de la sortie standard de l'application externe dans un Thread separe
		new Thread() {
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = "";
					try {
						while((line = reader.readLine()) != null) {
							java.lang.System.out.println(line);
						}
					} finally {
						reader.close();
					}
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}.start();

		// Consommation de la sortie d'erreur de l'application externe dans un Thread separe
		new Thread() {
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					String line = "";
					try {
						while((line = reader.readLine()) != null) {
							java.lang.System.out.println(line);
						}
					} finally {
						reader.close();
					}
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}.start();
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

}.start();*/
