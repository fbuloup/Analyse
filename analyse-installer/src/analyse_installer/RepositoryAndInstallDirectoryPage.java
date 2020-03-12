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
package analyse_installer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import resources.IImagesKeys;
import resources.ImagesUtils;
import resources.CPlatform;

public class RepositoryAndInstallDirectoryPage extends WizardPage {
	
	private final static String[] availableReleases = new String[]{"macintel","macppc","linuxgtk","windows","windows_64"}; 
	private static String targetRelease;
	
	public static final String PAGE_NAME = "REPOSITORY_AND_INSTALL_DIRECTORY_PAGE";
	
	private static String titleMessage = "Welcome in Analyse Installer !";
	private static String message1 = "Please select a directory and a release to install Analyse.";

	private Text installDirectoryText;

	private Combo releaseCombo;

	private Text messageText;
	private Button createShortcutButton;

	protected RepositoryAndInstallDirectoryPage() {
		super(PAGE_NAME,"",ImagesUtils.getImageDescriptor(IImagesKeys.UPDATE_BANNER));
		setTitle(titleMessage);
		if(CPlatform.isMacOSX()) if(CPlatform.isPPCArch()) targetRelease = availableReleases[1]; else targetRelease = availableReleases[0];
		if(CPlatform.isLinux()) targetRelease = availableReleases[2];
		if(CPlatform.isWindows() && CPlatform.isX86Arch()) targetRelease = availableReleases[3];
		if(CPlatform.isWindows() && !CPlatform.isX86Arch()) targetRelease = availableReleases[4];
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent,SWT.NONE);
		container.setLayout(new GridLayout(3,false));
		
		Label installDirectoryLabel = new Label(container,SWT.NONE);
		installDirectoryLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
		installDirectoryLabel.setText("Install directory :");
		
		installDirectoryText = new Text(container,SWT.BORDER | SWT.READ_ONLY);
		installDirectoryText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		Button browseButton = new Button(container, SWT.PUSH);
		browseButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialogDirectoryDialog = new DirectoryDialog(RepositoryAndInstallDirectoryPage.this.getShell(),SWT.NONE);
				String dir = dialogDirectoryDialog.open();
				if(dir != null) {
					installDirectoryText.setText(dir);
					checkForErrors();
				}
			}
			
		});
		
		Label repositoryPathLabel = new Label(container,SWT.NONE);
		repositoryPathLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
		repositoryPathLabel.setText("Release :");
		
		releaseCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
		releaseCombo.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		releaseCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				checkForErrors();
			}
		});
		
		StringBuffer releasesString = new StringBuffer();
		
		try {
			URL repositoryURL = new URL(WizardInstallerDialog.getRepositoryURL() + "releases.txt");
			BufferedReader bufferedReader = new BufferedReader(new java.io.InputStreamReader(repositoryURL.openStream()));
			String release;
			ArrayList<String> releasesArrayList = new ArrayList<String>(0);
			while ((release = bufferedReader.readLine()) != null) {
				releasesArrayList.add(release.replaceAll("_release$", ""));
				releasesString.append(release + " - ");
			}
			String[] releases = releasesArrayList.toArray(new String[releasesArrayList.size()]);
			Arrays.sort(releases);
			releaseCombo.setItems(releases);
			releaseCombo.select(releaseCombo.indexOf(targetRelease));
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			setError(e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			setError(e.toString());
		}
		
		setErrorMessage(null);
		
		messageText = new Text(container, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY | SWT.BORDER);
		messageText.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,3,1));
		
		createShortcutButton = new Button(container, SWT.CHECK);
		createShortcutButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,3,1));
		createShortcutButton.setText("Create Shortcut on Desktop (local machine)");
		createShortcutButton.setVisible(CPlatform.isWindows() && (releaseCombo.getText().equals(availableReleases[3]) || releaseCombo.getText().equals(availableReleases[4])));
		
		setControl(container);
		
		setMessage(message1,IMessageProvider.INFORMATION);
		setPageComplete(false);
		addMessage("Target platform : " + targetRelease);
		addMessage("Available releases : " + releasesString.toString().replaceAll("- $", ""));
		addMessage("Repository URL : " + WizardInstallerDialog.getRepositoryURL());

	}
	
	protected void checkForErrors() {
		createShortcutButton.setVisible(CPlatform.isWindows() && releaseCombo.getText().equals(availableReleases[3]));
		setErrorMessage(null);
		setMessage(null, IMessageProvider.WARNING);
		setMessage(message1,IMessageProvider.INFORMATION);
		boolean error = installDirectoryText.getText().equals("") || releaseCombo.getText().equals("");
		File installDir = new File(installDirectoryText.getText());
		error = error || !installDir.exists();
		if(!installDir.exists()) setErrorMessage("Install directory doesn't exist !"); //$NON-NLS-1$ //$NON-NLS-2$
		if(!targetRelease.equals(releaseCombo.getText())) setMessage("You are trying to install a release that doesn't target your platform !", IMessageProvider.WARNING);
		setPageComplete(!error);
	}
	
	protected String getInstallDirectory() {
		return installDirectoryText.getText();
	}
	
	protected String getReleaseDirectory() {
		return releaseCombo.getText() + "_release";
	}

	protected boolean createShortcutOnLocalDesktop() {
		return createShortcutButton.getVisible() && createShortcutButton.getSelection();
	}
	
	public void addMessage(final String message) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				messageText.append(message + "\n");
			}
		});
	}
	
	public void setError(final String message) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				setErrorMessage(message);
			}
		});
	}
	
}
