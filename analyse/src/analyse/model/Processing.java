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
package analyse.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.internal.forms.MessageManager;

import analyse.Log;
import analyse.Utils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;

public class Processing implements IResource, IResourceObserver, IFunctionObserver, Comparable<Processing> {

public static final String EXTENSION = ".process";
	
	private String name;
	private String oldName;
	private IResource parent;
	private DataProcessing dataProcessing;
	private boolean error;
	private boolean warning;
	private HashSet<String> lastCreatedChannels = new HashSet<String>(0);
	private HashSet<String> lastModifiedSignals = new HashSet<String>(0);
	
	public Processing(IResource selectedResource, String processName) {
		processName = processName.replaceAll(EXTENSION + "$", "");
		name = processName + EXTENSION;
		oldName = name;
		parent = selectedResource;
		File processingFile = new File(getAbsolutePath());
		if(processingFile.exists()) readProcessing(processingFile);
		else {
			dataProcessing = new DataProcessing();
			saveProcessing();
		}
		
	}
	
	private void readProcessing(File processingFile) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(processingFile));
			dataProcessing = (DataProcessing) in.readObject();
			in.close();
		} catch (FileNotFoundException e) {
			Log.logErrorMessage(e);
		} catch (IOException e) {
			Log.logErrorMessage(e);
		} catch (ClassNotFoundException e) {
			Log.logErrorMessage(e);
		}
	}
	
	public void saveProcessing() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getAbsolutePath()));
			out.writeObject(dataProcessing);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			Log.logErrorMessage(e);
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}
	}
	
	public DataProcessing getDataProcessing() {
		return dataProcessing;
	}

	public void addResources(IResource[] resources) throws InterruptedException {
	}

	public void copyTo(IResource destResource) throws IOException, InterruptedException {
		File srcFile = new File(getAbsolutePath());
		File destFile = new File(destResource.getAbsolutePath() + File.separator + name);
		Utils.copyFile(srcFile, destFile);
		Processing process = new Processing(destResource, name);
		destResource.addResources(new IResource[]{process});
	}

	public boolean delete() {
		boolean succeed = (new File(getAbsolutePath())).delete();
		if(succeed) getParent().remove(this);
		Experiments.getInstance().removeExperimentObserver(this);
		return succeed;
	}

	public String getAbsolutePath() {
		return parent.getAbsolutePath() + File.separator + name;
	}

	public IResource[] getChildren() {
		return null;
	}

	public Image getImage() {
		return ImagesUtils.getImage(IImagesKeys.PROCESS_EDITOR_ICON);
	}

	public String getName() {
		return name;
	}

	public String getOldName() {
		return oldName;
	}

	public IResource getParent() {
		return parent;
	}

	public boolean hasChildren() {
		return false;
	}

	public boolean hasParent(IResource parentResource) {
		IResource parent = getParent();
		while(parent != null) {
			if(parent == parentResource) return true;
			parent = parent.getParent();
		}
		return false;
	}

	public void remove(IResource resource) {
	}

	public void rename(String newName) {
		String oldOldName = oldName;
		File oldFile = new File(getAbsolutePath());
		if(!oldName.equals(name)) oldName = name;
		name = newName + EXTENSION;
		File newFile = new File(getAbsolutePath());
		if(!oldFile.renameTo(newFile)) {
			name = oldName;
			oldName = oldOldName;
			Log.logErrorMessage("Impossible to rename '" + getAbsolutePath() + "'");
		}
	}

	public String getLocalPath() {
		return parent.getLocalPath() + "." + getNameWithoutExtension();
	}
	
	public String getNameWithoutExtension() {
		return name.replaceAll(EXTENSION + "$", "");
	}

	public void registerToExperimentsObservers() {
		Experiments.getInstance().addExperimentObserver(this);
	}

	public void update(int message, IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if(message == IResourceObserver.RENAMED) {
				String oldName = null;
				String newName = null;
				if(resource instanceof Experiment) {
					oldName = resource.getOldName();
					newName = resource.getLocalPath();
				} else {
					oldName = resource.getParent().getLocalPath() + "." + resource.getOldName();
					newName = resource.getLocalPath();
				}
				dataProcessing.replace(oldName, newName);
			}
			if(message == IResourceObserver.CHANNEL_DELETED) {
			}
			if(message == IResourceObserver.DELETED) {
			}
			if(message == IResourceObserver.MARKERS_GROUP_DELETED) {
			}
			if(message == IResourceObserver.FIELD_DELETED) {
			}
			if(message == IResourceObserver.EVENT_DELETED) {
			}
		}
	}
	
	public IResource getFirstResourceByName(String resourceName) {
		if(getLocalPath().equals(resourceName)) return this;
		return null;
	}
	
	public Subject getSubjectByName(String resourceName) {
		return null;
	}

	public void update(Function function) {
		saveProcessing();
	}

	public void run(String replace) {
		try {
			FileWriter mathScriptFile;
			mathScriptFile = new FileWriter("./matlabscripts/process.m");
			BufferedWriter bufferedWriter = new BufferedWriter(mathScriptFile);
			String scriptCode = getProcessingCode();
			if(replace != null) 
				if(!replace.equals("")) scriptCode = scriptCode.replaceAll("\\w+\\.\\w+\\.", replace + ".");
			Log.logMessage("\t\tWith script code : " + scriptCode);
			bufferedWriter.write(scriptCode);
			bufferedWriter.close();
			IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
			if(mathEngine.isStarted()) 
				if(!mathEngine.runProcessing()) Log.logErrorsMessages(mathEngine.getProcessingErrors());
				else {
					lastCreatedChannels.clear();
					lastModifiedSignals.clear();
					String[] channels = mathEngine.getNewSignalsNames();
					if (channels.length > 0) {
						Log.logMessage("New created signals " + ((replace==null)?"":("for " + replace)) + " :");
						lastCreatedChannels.addAll(Arrays.asList(channels));
					}
					for (int i = 0; i < channels.length; i++) Log.logMessage("\t\t" + channels[i]);
					channels = mathEngine.getNewMarkersNames();
					if (channels.length > 0) {
						Log.logMessage("New created markers " + ((replace==null)?"":("for " + replace)) + " :");
						lastCreatedChannels.addAll(Arrays.asList(channels));
					}
					for (int i = 0; i < channels.length; i++) Log.logMessage("\t\t" + channels[i]);
					channels = mathEngine.getNewFieldsNames();
					if (channels.length > 0) {
						Log.logMessage("New created fields " + ((replace==null)?"":("for " + replace)) + " :");
						lastCreatedChannels.addAll(Arrays.asList(channels));
					}
					for (int i = 0; i < channels.length; i++) Log.logMessage("\t\t" + channels[i]);
					
					channels = mathEngine.getModifiedSignalsNames();
					if (channels.length > 0) {
						Log.logMessage("Modified signals " + ((replace==null)?"":("for " + replace)) + " :");
						lastModifiedSignals.addAll(Arrays.asList(channels));
					}
					for (int i = 0; i < channels.length; i++) Log.logMessage("\t\t" + channels[i]);
				}
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}
	}
	
	public void runMulptipleProcesses(Processing[] processes, Subject[] subjects) {
		try {
			FileWriter mathScriptFile;
			mathScriptFile = new FileWriter("./matlabscripts/process.m");
			BufferedWriter bufferedWriter = new BufferedWriter(mathScriptFile);
			String scriptCode = "";
			for (int i = 0; i < subjects.length; i++) {
				for (int j = 0; j < processes.length; j++) {
					String lineScriptCode = processes[j].getProcessingCode();
					lineScriptCode = lineScriptCode.replaceAll("\\w+\\.\\w+\\.", subjects[i].getLocalPath() + ".");
					scriptCode = scriptCode + lineScriptCode + "\n";
				}
			}
			bufferedWriter.write(scriptCode);
			bufferedWriter.close();
			
			IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
			if(mathEngine.isStarted()) 
				if(!mathEngine.runProcessing()) Log.logErrorsMessages(mathEngine.getProcessingErrors());
				else {
					String[] channels = mathEngine.getNewSignalsNames();
					if (channels.length > 0) {
						Log.logMessage("New created signals :");
						lastCreatedChannels.addAll(Arrays.asList(channels));
					}
					for (int i = 0; i < channels.length; i++) Log.logMessage("\t\t" + channels[i]);
					channels = mathEngine.getNewMarkersNames();
					if (channels.length > 0) {
						Log.logMessage("New created markers :");
						lastCreatedChannels.addAll(Arrays.asList(channels));
					}
					for (int i = 0; i < channels.length; i++) Log.logMessage("\t\t" + channels[i]);
					channels = mathEngine.getNewFieldsNames();
					if (channels.length > 0) {
						Log.logMessage("New created fields :");
						lastCreatedChannels.addAll(Arrays.asList(channels));
					}
					for (int i = 0; i < channels.length; i++) Log.logMessage("\t\t" + channels[i]);
					channels = mathEngine.getModifiedSignalsNames();
					if (channels.length > 0) {
						Log.logMessage("New created fields :");
						lastModifiedSignals.addAll(Arrays.asList(channels));
					}
					for (int i = 0; i < channels.length; i++) Log.logMessage("\t\t" + channels[i]);
				}
			
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}
	}
	
	public String[] getLastCreatedChannels() {
		return lastCreatedChannels.toArray(new String[lastCreatedChannels.size()]);
	}
	
	public String[] getLastModifiedSignals() {
		return lastModifiedSignals.toArray(new String[lastModifiedSignals.size()]);
	}
	
	public boolean validate(boolean logMessages) {
		return validate(null, null, logMessages);
	}
	
	public boolean hasError() {
		return error;
	}

	public boolean hasWarning() {
		return warning;
	}

	public boolean validate(MessageManager messageManager, Function selectedFunction, boolean logMessages) {
		if(messageManager != null) messageManager.removeAllMessages();
		Function[] functions = getDataProcessing().getFunctions();
		boolean valid = true;
		error = false;
		warning = false;
		for (int i = 0; i < functions.length; i++) {
			Function currentFunction = functions[i];
			FunctionValidator functionValidator = new FunctionValidator(currentFunction);
			if(!functionValidator.validate()) {
				valid = false;
				if(functionValidator.hasErrors()) {
					error = true;
					if(selectedFunction == currentFunction && messageManager != null) {
						IMessage message = functionValidator.getFirstErrorMessage();
						messageManager.addMessage(message.getKey(), message.getMessage(), null, message.getMessageType());
					}
					if(logMessages) {
						Log.logErrorMessage("Error(s) in '" + functionValidator.getFunction().getGUIFunctionName() + "' function from Process '" + getLocalPath() + "' :");
						Log.logErrorsMessages(functionValidator.getErrorsMessages());
					}
				} 
				if(functionValidator.hasWarnings()) {
					warning = true;
					if(selectedFunction == currentFunction && messageManager != null) {
						IMessage message = functionValidator.getFirstWarningMessage();
						messageManager.addMessage(message.getKey(), message.getMessage(), null, message.getMessageType());
					}
					if(logMessages) {
						Log.logWarningMessage("Warning(s) in '" + functionValidator.getFunction().getGUIFunctionName() + "' function from Process '" + getLocalPath() + "' :");
						Log.logWarningsMessages(functionValidator.getWarningsMessages());
					}
				}
			}
		}
		return valid;
	}
	
	public String getProcessingCode() {
		Function[] functions = dataProcessing.getFunctions();
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < functions.length; i++) {
			Function function = functions[i];
			stringBuffer.append(function.getProcessingCode() + ";\n");
		}
		return stringBuffer.toString();
	}

	public void clear() {
		dataProcessing.clear();
		saveProcessing();
	}

	public int compareTo(Processing processing) {
		String[] processingNames = new String[2];
		processingNames[0] = getLocalPath();
		processingNames[1] = processing.getLocalPath();
		Arrays.sort(processingNames);
		return processingNames[0].equals(getLocalPath())?-1:1;
	}

}
