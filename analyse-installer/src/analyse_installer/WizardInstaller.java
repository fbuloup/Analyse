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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import resources.CPlatform;

public class WizardInstaller extends Wizard {
	
	public static String linuxLauncherName = "startAnalyse.run";
	public static String macLauncherName = "startAnalyse.app/Contents/MacOS/startAnalyse";
	public static String macBaseLauncherName = "startAnalyse.app";
	public static String linuxBashLauncherName = "LINUXAnalyseLauncher.sh";
	public static String macBashLauncherName = "OSXAnalyseLauncher.sh";
	public static String windowsLauncherName = "startAnalyse.exe";
	
	private RepositoryAndInstallDirectoryPage repositoryAndInstallDirectoryPage; 
	private String[] filesListPath;
	private String releaseDirectory;
	
	private EULAPage licencePage;
	
	public static String installDirectory;
	public static String launcherName;
	public static String shell;
	public static String shellOption;
	
	public WizardInstaller() {
		super();
		setWindowTitle("Analyse Installer"); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public void addPages() {
		licencePage = new EULAPage();
		addPage(licencePage);
		repositoryAndInstallDirectoryPage = new RepositoryAndInstallDirectoryPage();
		addPage(repositoryAndInstallDirectoryPage);
	}
	
	@Override
	public boolean performFinish() {
		installDirectory = repositoryAndInstallDirectoryPage.getInstallDirectory();
		releaseDirectory =  WizardInstallerDialog.getRepositoryURL() + repositoryAndInstallDirectoryPage.getReleaseDirectory();
		final boolean createShortcut = repositoryAndInstallDirectoryPage.createShortcutOnLocalDesktop();
		try {
			getContainer().run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Getting files list to download from : " + releaseDirectory, IProgressMonitor.UNKNOWN);
					filesListPath = getFilesListToDownload();
					monitor.done();
					
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			repositoryAndInstallDirectoryPage.addMessage(e.toString());
			repositoryAndInstallDirectoryPage.setError(e.toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
			repositoryAndInstallDirectoryPage.addMessage(e.toString());
			repositoryAndInstallDirectoryPage.setError(e.toString());
		}
		
		try {
			getContainer().run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Installing files from " + releaseDirectory  + " to " + installDirectory , (filesListPath.length + 1)*100);
					for (int i = 0; i < filesListPath.length; i++) {
						String localFilePath = installDirectory + File.separator + filesListPath[i];
						String distantFileURL = releaseDirectory + "/" + filesListPath[i];
						repositoryAndInstallDirectoryPage.addMessage("Getting " + filesListPath[i]);
						downloadFile(distantFileURL, localFilePath,monitor);
					}

					monitor.subTask("Changing Analyse launcher file system modes");
					if(CPlatform.isLinux()){
						String[] cmd = new String[]{ "/bin/sh", "-c", "chmod 755 " + installDirectory + File.separator + linuxLauncherName};
						runBashCmd(cmd);
						cmd = new String[]{ "/bin/sh", "-c", "chmod 755 " + installDirectory + File.separator + linuxBashLauncherName};
						runBashCmd(cmd);
					}
					if(CPlatform.isMacOSX()){
						String[] cmd = new String[]{ "/bin/sh", "-c", "chmod 755 " + installDirectory + File.separator + macLauncherName};
						runBashCmd(cmd);
						cmd = new String[]{ "/bin/sh", "-c", "chmod 755 " + installDirectory + File.separator + macBashLauncherName};
						runBashCmd(cmd);
					}
					if(createShortcut) {
						//Create VB script
						try {
							File vbScriptFile = new File(installDirectory + File.separator + "createAnalyseShortCut.vbs");
							FileWriter vbScriptFileWriter = new FileWriter(vbScriptFile);
							BufferedWriter vbScriptBufferedWriter = new BufferedWriter(vbScriptFileWriter);
							vbScriptBufferedWriter.write("Set windowsScript = WScript.CreateObject(\"WScript.Shell\")");
							vbScriptBufferedWriter.newLine();
							vbScriptBufferedWriter.write("desktopPath = windowsScript.SpecialFolders(\"Desktop\")");
							vbScriptBufferedWriter.newLine();
							vbScriptBufferedWriter.write("sLinkFile = desktopPath + \"\\startAnalyse.lnk\"");  
							vbScriptBufferedWriter.newLine();
							vbScriptBufferedWriter.write("Set oLink = windowsScript.CreateShortcut(sLinkFile)");
							vbScriptBufferedWriter.newLine();
							vbScriptBufferedWriter.write("oLink.TargetPath = \"" + installDirectory + File.separator + windowsLauncherName + "\"");
							vbScriptBufferedWriter.newLine();
							vbScriptBufferedWriter.write("oLink.WorkingDirectory = \"" + installDirectory + File.separator + "\"");
							vbScriptBufferedWriter.newLine();
							vbScriptBufferedWriter.write("oLink.Save");
							vbScriptBufferedWriter.flush();
							vbScriptBufferedWriter.close();
							//run it using System
							Process process = Runtime.getRuntime().exec("wscript.exe \"" + vbScriptFile.getAbsolutePath() + "\"");
							process.waitFor();
							if(process.exitValue() == 0) vbScriptFile.delete();
						} catch (IOException e) {
							e.printStackTrace();
							repositoryAndInstallDirectoryPage.addMessage(e.toString());
							repositoryAndInstallDirectoryPage.setError(e.toString());
						}
						
						
					}
					monitor.worked(100);
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			repositoryAndInstallDirectoryPage.addMessage(e.toString());
			repositoryAndInstallDirectoryPage.setError(e.toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
			repositoryAndInstallDirectoryPage.addMessage(e.toString());
			repositoryAndInstallDirectoryPage.setError(e.toString());
		}

		if(getPages()[0].getErrorMessage() == null) {
			repositoryAndInstallDirectoryPage.addMessage("Installation complete");
			if(MessageDialog.openQuestion(getShell(), "Analyse Installer", "Installation complete sucessfull. Do you want to run analyse now ?")) {
				WizardInstallerDialog.startAnalyse = true;
				return true;
			}
		} else MessageDialog.openError(getShell(), "Installation Error !", getPages()[0].getErrorMessage());	
		return false;
	}

	protected void runBashCmd(String[] cmd) {
		try {
			Runtime.getRuntime().exec(cmd).waitFor();
		} catch (IOException e) {
			e.printStackTrace();
			repositoryAndInstallDirectoryPage.addMessage(e.toString());
			repositoryAndInstallDirectoryPage.setError(e.toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
			repositoryAndInstallDirectoryPage.addMessage(e.toString());
			repositoryAndInstallDirectoryPage.setError(e.toString());
		}
	}

	private String[] getFilesListToDownload() {
		ArrayList<String> filesListArrayList = new ArrayList<String>(0);
		try {
			//Retrieve releaseFilesList.xml
			URL releaseFilesListURL = new URL(releaseDirectory + "/releaseFilesList.xml");
			InputStream xmlInputStream = releaseFilesListURL.openStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document filesListDocument = builder.parse(xmlInputStream);
			Node filesListNode = filesListDocument.getFirstChild();
			NodeList nodes = filesListNode.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
					if(nodes.item(i) instanceof Element) {
					NamedNodeMap attribute = nodes.item(i).getAttributes();
					filesListArrayList.add(attribute.item(0).getNodeValue());
				}
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			repositoryAndInstallDirectoryPage.addMessage(e.toString());
			repositoryAndInstallDirectoryPage.setError(e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			repositoryAndInstallDirectoryPage.addMessage(e.toString());
			repositoryAndInstallDirectoryPage.setError(e.toString());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			repositoryAndInstallDirectoryPage.addMessage(e.toString());
			repositoryAndInstallDirectoryPage.setError(e.toString());
		} catch (SAXException e) {
			e.printStackTrace();
			repositoryAndInstallDirectoryPage.addMessage(e.toString());
			repositoryAndInstallDirectoryPage.setError(e.toString());
		}
		return filesListArrayList.toArray(new String[filesListArrayList.size()]);
	}

	
	private boolean downloadFile(String fromFileName, String toFileName, IProgressMonitor monitor) {
		try {
			URL fileURL = new URL(fromFileName);
			URLConnection fileURLConnection = fileURL.openConnection();
			int fileSize = fileURLConnection.getContentLength();
			InputStream fileInputStream = fileURLConnection.getInputStream();
			if(CPlatform.isWindows()) toFileName = toFileName.replace("/","\\");
			String dir = toFileName.replaceAll("\\w+\\.\\w+$$", "");
			if(CPlatform.isMacOSX()) dir = dir.replaceAll("/\\w+$","");
			(new File(dir)).mkdirs();
			FileOutputStream fileOutputStream = new FileOutputStream(toFileName);
			byte[] bytesBuffer = new byte[1024];
			int bytesRead;
			int totalBytesRead = 0;
			int previousWork = 0;
			monitor.subTask("Downloading : " + fromFileName);
			while((bytesRead = fileInputStream.read(bytesBuffer)) > -1) {
				fileOutputStream.write(bytesBuffer, 0, bytesRead);
				totalBytesRead += bytesRead;
				int work = (int) ((100.0*totalBytesRead) /fileSize);
				monitor.worked(work - previousWork);
				previousWork = work;
			}
			fileInputStream.close();
			fileOutputStream.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
