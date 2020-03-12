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
package ftpsharing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;
import org.eclipse.core.runtime.IProgressMonitor;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.model.Chart;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.Folder;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Note;
import analyse.model.Processing;
import analyse.resources.Messages;

public class FTPShareExperimentsClient {
	
	private static FTPClient ftpClient;
	private static File ftpFileTemp;
	private static String[] sharedExperimentsNames;
	
	public static void connect(String ip, String login, String pwd, IProgressMonitor monitor) throws SocketException, IOException {
		monitor.beginTask(Messages.getString("SharingView.ConnectingToServer"), IProgressMonitor.UNKNOWN);
		ftpClient = new FTPClient();
		ftpClient.connect(ip, 2221);

		int reply = ftpClient.getReplyCode();

		if (!FTPReply.isPositiveCompletion(reply)) {
			ftpClient.disconnect();
			Log.logErrorMessage("FTP server refused connection.");
		}

		if(!ftpClient.login(login, pwd)) {
			ftpClient.logout();
			Log.logErrorMessage("Login or password incorrect.");
		}

		ftpClient.enterLocalPassiveMode();
	}
	
	public static void disconnect() throws IOException {
		if(!ftpClient.logout()) throw new IOException("Logout impossible...");
		ftpClient.disconnect();
	}
	
	public static boolean isConnected() {
		if(ftpClient == null) return false;
		return ftpClient.isConnected();
	}
	
	public static FTPFile[] getSharedExperiments(IProgressMonitor monitor) throws IOException {
		monitor.subTask(Messages.getString("SharingView.GettingExperimentsFileList"));
		FTPFile[] files = ftpClient.listFiles();
		sharedExperimentsNames = new String[0];
		//Get shared experiment names
		for (int i = 0; i < files.length; i++) {
			FTPFile ftpFile = files[i];
			if(ftpFile.getName().equals(FTPShareExperimentsServer.SHARED_EXPERIMENTS_FILE_NAME)) {
				FileOutputStream fileOutputStream = new FileOutputStream(AnalyseApplication.analyseDirectory + ftpFile.getName());
				if(!ftpClient.retrieveFile(ftpFile.getName(), fileOutputStream)) throw new IOException(Messages.getString("SharingView.ImpossibleToRetrieve") + ftpFile.getName());
				fileOutputStream.close();
				Properties sharedExperimentsProperties = new Properties();
				FileInputStream fileInputStream = new FileInputStream(AnalyseApplication.analyseDirectory + ftpFile.getName());
				sharedExperimentsProperties.load(fileInputStream);
				fileInputStream.close();
				sharedExperimentsNames = sharedExperimentsProperties.getProperty("EXPERIMENTS").split(";");
				
			}
		}
		monitor.subTask(Messages.getString("SharingView.GettingExperimentsFiles"));
		//Filter FTPFile : shared experiment only
		files = ftpClient.listFiles("/", new FTPFileFilter() {
			public boolean accept(FTPFile file) {
				if(file.isDirectory()) 
					for (int j = 0; j < sharedExperimentsNames.length; j++)
						if(sharedExperimentsNames[j].equals(file.getName())) return true;
				return false;
			}
		});
		monitor.subTask(Messages.getString("SharingView.GettingExperimentsTypes"));
		//For each of these shared experiments, retrieve experiment type
		for (int i = 0; i < files.length; i++) {
			FTPFile ftpFile = files[i];
			//Go into experiment folder
			ftpClient.changeWorkingDirectory(ftpFile.getName());
			//Get properties experimentFile
			FileOutputStream fileOutputStream = new FileOutputStream(AnalyseApplication.analyseDirectory + ftpFile.getName() + ".properties");
			if(!ftpClient.retrieveFile(ftpFile.getName() + ".properties", fileOutputStream)) throw new IOException(Messages.getString("SharingView.ImpossibleToRetrieve") + ftpFile.getName());
			fileOutputStream.close();
			//Open this file to get "TYPE" property
			Properties experimentsProperties = new Properties();
			File propertiesFile = new File(AnalyseApplication.analyseDirectory + ftpFile.getName()+ ".properties");
			FileInputStream fileInputStream = new FileInputStream(propertiesFile);
			experimentsProperties.load(fileInputStream);
			fileInputStream.close();
			String experimentType = experimentsProperties.getProperty("TYPE");
			ftpFile.setGroup(experimentType);
			//Delete properties file
			if(propertiesFile.exists()) if(!propertiesFile.delete()) throw new IOException(Messages.getString("DeleteAction.ImpossibleToDelete") + propertiesFile.getAbsolutePath());
			//Go up
			ftpClient.changeToParentDirectory();
		}
		File sharedExperimentsPropertiesFile = new File(AnalyseApplication.analyseDirectory + FTPShareExperimentsServer.SHARED_EXPERIMENTS_FILE_NAME);
		if(sharedExperimentsPropertiesFile.exists()) if(!sharedExperimentsPropertiesFile.delete()) throw new IOException(Messages.getString("DeleteAction.ImpossibleToDelete") + FTPShareExperimentsServer.SHARED_EXPERIMENTS_FILE_NAME);
		return files;
	}

	public static void changeWorkingDirectory(String pathname) throws IOException {
		if(pathname.equals("..")) ftpClient.changeToParentDirectory();
		else ftpClient.changeWorkingDirectory(pathname);
	}
	
	public static Object[] getChildren(FTPFile parentElement) throws IOException {
		FTPFile[] files = ftpClient.listFiles();
		ArrayList<FTPFile> filesArrayList = new ArrayList<FTPFile>(0);
		for (int i = 0; i < files.length; i++) {
			if(!files[i].getName().endsWith(".properties"))filesArrayList.add(files[i]);
		}
		return filesArrayList.toArray(new FTPFile[filesArrayList.size()]);
		
	}

	public static String getCurentLocation() throws IOException {
		return ftpClient.printWorkingDirectory(); 
	}

	public static void importFiles(String experimentName, FTPFile[] files, IProgressMonitor monitor) throws IOException, InterruptedException {
		monitor.beginTask(Messages.getString("SharingView.ImportFiles"), IProgressMonitor.UNKNOWN);
		ftpFileTemp = new File(AnalyseApplication.analyseDirectory + File.separator + "ftpTempFiles");
		if(!ftpFileTemp.exists()) if(!ftpFileTemp.mkdirs()) throw new InterruptedException("Impossible to create " + ftpFileTemp.getAbsolutePath());
		Experiment experiment = Experiments.getInstance().getExperimentByName(experimentName);
		try {
			importFiles(experiment, files, null, monitor);
		} catch (IOException e) {
			Log.logErrorMessage(e);
			throw new InterruptedException(e.getMessage());
		}
		monitor.subTask(Messages.getString("SharingView.DeleteFiles"));
		File[] filesTemp = ftpFileTemp.listFiles();
		for (int i = 0; i < filesTemp.length; i++) {
			if(!filesTemp[i].delete()) throw new InterruptedException(Messages.getString("DeleteAction.ImpossibleToDelete") + filesTemp[i].getAbsolutePath());
		}
		if(!ftpFileTemp.delete()) throw new InterruptedException(Messages.getString("DeleteAction.ImpossibleToDelete") + ftpFileTemp.getAbsolutePath());
		monitor.done();
	}

	private static void importFiles(IResource parentResource, FTPFile[] files, FTPFile parentFile, IProgressMonitor monitor) throws IOException, InterruptedException {
		String[] ftpWorkingDirectorySegments = ftpClient.printWorkingDirectory().replaceAll("^/","").split("/");
		String[] localPathWorkingDirectorySegments = parentResource.getLocalPath().split("\\.");
		
		if(ftpWorkingDirectorySegments.length > localPathWorkingDirectorySegments.length) {
			//We are not at same level, we must construct or retrieve intermediates folders before to continue.
			//We are sure that it can only be folders as we can't navigate inside subjects folders.
			//First segment is always experiment name, the others are folders
			for (int i = 1; i < ftpWorkingDirectorySegments.length; i++) {
				StringBuffer fullSegmentName = new StringBuffer(ftpWorkingDirectorySegments[0]);
				for (int j = 1; j <= i; j++) fullSegmentName.append("." + ftpWorkingDirectorySegments[j].replaceAll("\\.\\w+", ""));
				IResource resource = parentResource.getFirstResourceByName(fullSegmentName.toString());
				if(resource == null) {
					try {
						resource = new Folder(parentResource, ftpWorkingDirectorySegments[i].replaceAll(Folder.EXTENSION, ""));
						parentResource.addResources(new IResource[]{resource});
						Experiments.notifyObservers(IResourceObserver.FOLDER_CREATED, new IResource[]{resource});
					} catch (InterruptedException e) {
						Log.logErrorMessage(e);
					}
				}
				parentResource = resource;
			}
		}
		
		for (int i = 0; i < files.length; i++) {
			FTPFile file = files[i];
			if(file.isDirectory()) {
				//This is folder or a subject
				String folderName = file.getName();
				IResource localResource = parentResource.getFirstResourceByName(parentResource.getLocalPath() + "." + folderName);
				if(folderName.endsWith(Folder.EXTENSION)) {
					//This is a folder
					if(localResource ==null) {
						//This a new folder, so create it
						try {
							localResource = new Folder(parentResource, folderName.replaceAll("\\.folder$", ""));
							parentResource.addResources(new IResource[]{localResource});
							Experiments.notifyObservers(IResourceObserver.FOLDER_CREATED, new IResource[]{localResource});
						} catch (InterruptedException e) {
							Log.logErrorMessage(e);
						}
					}
					//Go inside distant folder, to get files if exist
					ftpClient.changeWorkingDirectory(file.getName());
					importFiles(localResource, ftpClient.listFiles(), file, monitor);
					//Go up
					ftpClient.changeToParentDirectory();
				}
				else {
					//This is a subject folder
					//Download data files in temporary folder
					//Go inside distant folder, to get data files
					ftpClient.changeWorkingDirectory(file.getName());
					FTPFile[] dataFiles = ftpClient.listFiles();
					final ArrayList<String> dataFilesList = new ArrayList<String>(0);
					for (int j = 0; j < dataFiles.length; j++) {
						monitor.subTask(Messages.getString("SharingView.DownloadingFile") + ftpClient.printWorkingDirectory() + "/" + dataFiles[j].getName());
						downloadFile(ftpFileTemp.getAbsolutePath(), dataFiles[j].getName());
						dataFilesList.add(ftpFileTemp.getAbsolutePath() + File.separator + dataFiles[j].getName());
					}
					//Go up
					ftpClient.changeToParentDirectory();
					//Create HashMap of subject and data files
					final Hashtable<String, String[]> subjectsAndDataFiles = new Hashtable<String, String[]>(0);
					final IResource parentResourceBis = parentResource;
					subjectsAndDataFiles.put(file.getName(), dataFilesList.toArray(new String[dataFilesList.size()]));
					monitor.subTask(Messages.getString("SharingView.ImportSubject") + parentResourceBis.getName());
					Experiments.createNewSubjectsFolders(parentResourceBis.getName(), subjectsAndDataFiles, monitor);
					((Experiment)parentResourceBis).addNewSubjectsByNames(subjectsAndDataFiles.keySet().toArray(new String[subjectsAndDataFiles.size()]), monitor);
				}
			} else {
				//This a chart, a note, a process or a data file
				IResource localResource;
				if(file.getName().endsWith(Chart.EXTENSION) || file.getName().endsWith(Processing.EXTENSION) || file.getName().endsWith(Note.EXTENSION)) 
					localResource = parentResource.getFirstResourceByName(parentResource.getLocalPath() + "." + file.getName().replaceAll("\\.\\w+$", ""));
				else localResource = parentResource.getFirstResourceByName(parentResource.getLocalPath() + "." + file.getName());
				if(localResource == null) {
					try {
						if(file.getName().endsWith(Chart.EXTENSION)) {
							monitor.subTask(Messages.getString("SharingView.DownloadingFile") + ftpClient.printWorkingDirectory() + "/" + file.getName());
							downloadFile(parentResource.getAbsolutePath(),file.getName());
							Chart chart = new Chart(parentResource, file.getName().replaceAll("\\.\\w+$", ""));
							parentResource.addResources(new IResource[]{chart});
							Experiments.notifyObservers(IResourceObserver.FOLDER_CREATED, new IResource[]{chart});
						}
						if(file.getName().endsWith(Processing.EXTENSION)) {
							monitor.subTask(Messages.getString("SharingView.DownloadingFile") + ftpClient.printWorkingDirectory() + "/" + file.getName());
							downloadFile(parentResource.getAbsolutePath(),file.getName());
							Processing process = new Processing(parentResource, file.getName().replaceAll("\\.\\w+$", ""));
							parentResource.addResources(new IResource[]{process});
							Experiments.notifyObservers(IResourceObserver.FOLDER_CREATED, new IResource[]{process});
						}
						if(file.getName().endsWith(Note.EXTENSION)) {
							monitor.subTask(Messages.getString("SharingView.DownloadingFile") + ftpClient.printWorkingDirectory() + "/" + file.getName());
							downloadFile(parentResource.getAbsolutePath(),file.getName());
							Note note = new Note(parentResource, file.getName().replaceAll("\\.\\w+$", ""));
							parentResource.addResources(new IResource[]{note});
							Experiments.notifyObservers(IResourceObserver.NOTE_CREATED, new IResource[]{note});
						}
						//We do not check for data files as they are downloaded with their subject
					} catch (Exception e) {
						Log.logErrorMessage(e);
					}
				}
				
			}
		}
	}
	
	private static void downloadFile(String localPath, String distantPath) throws IOException {
		//Download file
		FileOutputStream fileOutputStream = new FileOutputStream(localPath + File.separator + distantPath);
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		if(!ftpClient.retrieveFile(distantPath, fileOutputStream)) throw new IOException(Messages.getString("SharingView.ImpossibleToRetrieve") + distantPath);
		fileOutputStream.close();
		ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
	}

	public static void resetConnection() {
		ftpClient = null;
		
	}

	
}
