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
package mathengine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import analyse.Utils;
import analyse.model.Subject;
import analyse.preferences.AnalysePreferences;
import analyse.resources.CPlatform;
import analyse.resources.Messages;

public class OctaveEngine implements IMathEngine {
	
	private static String[] cmdArray ;
	private ILog logger;
	private Process process;
	private BufferedReader inputBuffer;
	private BufferedWriter outputBuffer;
	private BufferedReader errorBuffer;
	private StringBuffer buffer = new StringBuffer();
	private boolean started = false;
	private String version;
	private String workspace;
	private boolean continueReceive = true;
	private HashSet<String> runProcessingErrors = new HashSet<String>(0);
	private static HashSet<String> newSignals = new HashSet<String>(0);
	private static HashSet<String> newMarkers = new HashSet<String>(0);
	private static HashSet<String> newFields = new HashSet<String>(0);
	private static HashSet<String> modifiedSignals = new HashSet<String>(0);
	
	static {
		if(CPlatform.isWindows()) cmdArray = new String[] {"cmd","/C","octave","--no-history", "--no-init-file", "--no-line-editing","--no-site-file", "--silent"};
		else cmdArray = new String[] {"/bin/sh","-c","octave", "--no-history", "--no-init-file", "--no-line-editing","--no-site-file", "--silent", "--interactive"};
	}
	
	protected OctaveEngine(ILog logger) {
		this.logger = logger;
	}

	public boolean addMarker(int markersGroupNumber, int trialNumber, double XValue, double YValue, String fullSignalName) {
		String varName = "nbMarkersInGroup_" + (new Date()).getTime();
		sendCommand(varName + "=size(" + fullSignalName + Marker + markersGroupNumber + _Values + ");");
		String response = sendCommand(varName + "(1)");
		response = clearStringValue(response);
		sendCommand("clear " + varName + ";");
		int nbMarkersInGroup = Integer.parseInt(response);
		String cmd = fullSignalName + Marker + markersGroupNumber + _Values;
		if(nbMarkersInGroup == 0) cmd = cmd + " = [" + trialNumber  + " , " + XValue + " , " + YValue + "]";
		else cmd = cmd + " = [" + cmd + " ; " + trialNumber  + " , " + XValue + " , " + YValue + "]";
		sendCommand(cmd);
		return true;
	}

	public boolean createNewCategory(String fullSubjectName, String categoryName, String criteria, String trialsList) {
		String fullCategoryName = fullSubjectName + "." + categoryName; 
		String cmdLine = fullCategoryName + ".isCategory=1;"; 
		sendCommand(cmdLine);
		cmdLine = fullCategoryName + ".isSignal=0;"; 
		sendCommand(cmdLine);
		cmdLine = fullCategoryName + ".isEvent=0;"; 
		sendCommand(cmdLine);
		cmdLine = fullCategoryName + ".Criteria='" + criteria + "';"; 
		sendCommand(cmdLine);
		cmdLine = fullCategoryName + ".TrialsList=[" + trialsList + "];";
		sendCommand(cmdLine);
		return true;
	}

	public boolean createNewMarkersGroup(String markersGroupLabel, String fullSignalName) {
		int nbMarkersGroups = getNbMarkersGroups(fullSignalName) + 1;
		String cmd = fullSignalName + Marker + nbMarkersGroups + _Label; 
		cmd = cmd + " = '" + markersGroupLabel + "';";				
		sendCommand(cmd);
		cmd = fullSignalName + NbMarkers  + " = " + nbMarkersGroups + ";";
		sendCommand(cmd);
		cmd = fullSignalName + Marker + nbMarkersGroups + _Values + "=[];"; 
		sendCommand(cmd);
		return true;
	}

	public boolean deleteChannel(String fullSubjectName, String channelName) {
		String cmdLine = fullSubjectName + " = rmfield(" + fullSubjectName + ",'" + channelName + "');";
		sendCommand(cmdLine);
		String[] fieldsNames = getOctaveFieldsNames(fullSubjectName);
		for (int i = 0; i < fieldsNames.length; i++) if(fieldsNames[i].equals(channelName)) return false;
		return true;
	}

	public boolean deleteField(int fieldNumber, String fullSignalName) {
		String cmdLine = fullSignalName + " = rmfield(" + fullSignalName + ",'" + Field_No_Point + fieldNumber + _Values + "');";
		sendCommand(cmdLine);
		cmdLine = fullSignalName + " = rmfield(" + fullSignalName + ",'" + Field_No_Point + fieldNumber + _Label + "');";
		sendCommand(cmdLine);
		for (int i = (fieldNumber+1); i <= getNbFields(fullSignalName); i++) {
			cmdLine = fullSignalName + " = rnfield(" + fullSignalName + ",'" + Field_No_Point + (i) + _Values + "','" + Field_No_Point + (i-1) + _Values + "');"; 
			sendCommand(cmdLine);
			cmdLine = fullSignalName + " = rnfield(" + fullSignalName + ",'" + Field_No_Point + (i) + _Label + "','" + Field_No_Point + (i-1) + _Label + "');"; 
			sendCommand(cmdLine);
		}
		cmdLine = fullSignalName + NbFields + " = " + fullSignalName + NbFields + " - 1;";
		sendCommand(cmdLine);
		String[] fieldsNames = getOctaveFieldsNames(fullSignalName);
		String fieldName1ToDelete = Field_No_Point + (getNbFields(fullSignalName) + 1) + _Values;
		String fieldName2ToDelete = Field_No_Point + (getNbFields(fullSignalName) + 1) + _Label;
		for (int i = 0; i < fieldsNames.length; i++) {
			if(fieldsNames[i].equals(fieldName1ToDelete)) return false;
			if(fieldsNames[i].equals(fieldName2ToDelete)) return false;
		}
		return true;
	}

	public boolean deleteMarker(int markersGroupNumber, int trialNumber, String fullSignalName) {
		int[] trialsList = getTrialsListForMarkersGroup(markersGroupNumber, fullSignalName);
		if(trialsList.length == 1) return deleteMarkersGroup(markersGroupNumber, fullSignalName);
		String cmd = fullSignalName + Marker + markersGroupNumber + _Values + "(" + trialNumber + ",:) = [];";
		sendCommand(cmd);
		trialsList = getTrialsListForMarkersGroup(markersGroupNumber, fullSignalName);
		if(trialsList.length == 0) return deleteMarkersGroup(markersGroupNumber, fullSignalName);
		return true;
	}
	
	public boolean deleteEvent(int lineNumber, String fullEventName) {
		String cmd = fullEventName + Values + "("+ lineNumber + ",:) = [];";
		sendCommand(cmd);
		double[][] values = getEventsGroupValues(fullEventName);
		if(values.length == 0) {
			String fullSubjectName = fullEventName.replaceAll("\\.\\w+$", "");
			String eventName = fullEventName.split("\\.")[2];
			deleteChannel(fullSubjectName, eventName);
		}
		return true;
	}

	public boolean deleteMarkersGroup(int markersGroupNumber, String fullSignalName) {
		String cmdLine = fullSignalName + " = rmfield(" + fullSignalName + ",'" + Marker_No_Point + markersGroupNumber + _Values + "');";
		sendCommand(cmdLine);
		cmdLine = fullSignalName + " = rmfield(" + fullSignalName + ",'" + Marker_No_Point + markersGroupNumber + _Label + "');";
		sendCommand(cmdLine);
		for (int i = (markersGroupNumber+1); i <= getNbMarkersGroups(fullSignalName); i++) {
			cmdLine = fullSignalName + " = rnfield(" + fullSignalName + ",'" + Marker_No_Point + (i) + _Values + "','" + Marker_No_Point + (i-1) + _Values + "');"; 
			sendCommand(cmdLine);
			cmdLine = fullSignalName + " = rnfield(" + fullSignalName + ",'" + Marker_No_Point + (i) + _Label + "','" + Marker_No_Point + (i-1) + _Label + "');"; 
			sendCommand(cmdLine);
		}
		cmdLine = fullSignalName + NbMarkers + " = " + fullSignalName + NbMarkers + " - 1;";
		sendCommand(cmdLine);
		String[] fieldsNames = getOctaveFieldsNames(fullSignalName);
		String fieldName1ToDelete = Marker_No_Point + (getNbMarkersGroups(fullSignalName) + 1) + _Values;
		String fieldName2ToDelete = Marker_No_Point + (getNbMarkersGroups(fullSignalName) + 1) + _Label;
		for (int i = 0; i < fieldsNames.length; i++) {
			if(fieldsNames[i].equals(fieldName1ToDelete)) return false;
			if(fieldsNames[i].equals(fieldName2ToDelete)) return false;
		}
		return true;
	}

	public String[] getCategoriesNames(String fullSubjectName) {
		String[] channelsNames = getChannelsNames(fullSubjectName);
		ArrayList<String> categoriesNames = new ArrayList<String>(0);
		for (int i = 0; i < channelsNames.length; i++) 
			if(isCategory(fullSubjectName + "." + channelsNames[i])) categoriesNames.add(channelsNames[i]);
		return categoriesNames.toArray(new String[categoriesNames.size()]);
	}
	
	public String[] getEventsGroupsNames(String fullSubjectName) {
		String[] channelsNames = getChannelsNames(fullSubjectName);
		ArrayList<String> eventsNames = new ArrayList<String>(0);
		for (int i = 0; i < channelsNames.length; i++) 
			if(isEventsGroup(fullSubjectName + "." + channelsNames[i])) eventsNames.add(channelsNames[i]);
		return eventsNames.toArray(new String[eventsNames.size()]);
	}

	public String[] getChannelsNames(String fullSubjectName) {
		return getOctaveFieldsNames(fullSubjectName);
	}

	public String getCriteriaForCategory(String fullCategoryName) {
		String response = sendCommand(fullCategoryName + Criteria);
		response = response.replaceAll("(\\n)*", "");
		response = response.replaceAll("^ans\\s*=\\s*", "");
		response = response.replaceAll("ans\\s*=\\s*>>\\s*$", "");
		return response;
	}
	
	public String getCriteriaForEventsGroup(String fullEventName) {
		String response = sendCommand(fullEventName + Criteria);
		response = response.replaceAll("(\\n)*", "");
		response = response.replaceAll("^ans\\s*=\\s*", "");
		response = response.replaceAll(">>\\s*$", "");
		response = response.replaceAll("ans\\s*=\\s*$", "");
		return response;
	}

	public int[] getEndCut(String fullSignalName) {
		String response = sendCommand(fullSignalName + EndCut);
		response = clearStringValue2(response);
		String[] valuesString = response.split(":");
		Integer[] dataTemp = new Integer[valuesString.length];
		int[] data = new int[valuesString.length];
		for (int i = 0; i < data.length; i++) {
			dataTemp[i] = Integer.valueOf(valuesString[i]);
			data[i] = dataTemp[i];
		}
		return data;
//		long timeStamp = (new Date()).getTime();
//		String fileName = "data_" + timeStamp + ".bin";
//		String varName = "EndCut_" + timeStamp;
//		sendCommand(varName + "=" + fullSignalName + EndCut + ";"); 
//		sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
//		sendCommand("clear " + varName + ";");
//		String absoluteFilePath = workspace + File.separator + fileName;
////		File dataFile = (new File(absoluteFilePath));
////		while (!dataFile.exists());
//		int[] data = Utils.readIntegerBinaryDataFile(absoluteFilePath);
//		(new File(absoluteFilePath)).delete();
//		return data;
	}

	public int getEndCut(String fullSignalName, int trialNumber) {
		String response = sendCommand(fullSignalName + EndCut + "(" + trialNumber + ")");
		response = clearStringValue(response);
		return Integer.parseInt(response);
	}

	public String getFieldLabel(int fieldNumber, String fullSignalName) {
		String response = sendCommand(fullSignalName + Field + fieldNumber + _Label);
		response = clearStringValue(response);
		return response;
	}

	public double getFieldValue(int fieldNumber, int trialNumber, String fullSignalName) {
		String response = sendCommand(fullSignalName + Field + fieldNumber + _Values + "(" + trialNumber + ")");
		response = clearStringValue(response);
		return Double.parseDouble(response);
	}

	public double[] getFieldValues(int fieldNumber, String fullSignalName) {
		String response = sendCommand(fullSignalName + Field + fieldNumber + _Values + "'");
		response = clearStringValue2(response);
		String[] valuesString = response.split(":");
		double[] data = new double[valuesString.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = Double.parseDouble(valuesString[i]);
		}
		return data;
//		long timeStamp = (new Date()).getTime();
//		String fileName = "data_" + timeStamp + ".bin";
//		String varName = "FieldValues_" + timeStamp;
//		sendCommand(varName + "=" + fullSignalName + Field + fieldNumber + _Values + ";"); 
//		sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
//		sendCommand("clear " + varName + ";");
//		String absoluteFilePath = workspace + File.separator + fileName;
////		File dataFile = (new File(absoluteFilePath));
////		while (!dataFile.exists());
//		double[] data = Utils.readDoubleBinaryDataFile(absoluteFilePath);
//		(new File(absoluteFilePath)).delete();
//		return data;
	}

	public String[] getFieldsLabels(String fullSignalName) {
		String[] fieldsLabels = new String[getNbFields(fullSignalName)];
		for (int i = 0; i < fieldsLabels.length; i++) fieldsLabels[i] = getFieldLabel(i+1, fullSignalName);
		return fieldsLabels;
	}

	public int[] getFrontCut(String fullSignalName) {
		String response = sendCommand(fullSignalName + FrontCut);
		response = clearStringValue2(response);
		String[] valuesString = response.split(":");
		Integer[] dataTemp = new Integer[valuesString.length];
		int[] data = new int[valuesString.length];
		for (int i = 0; i < data.length; i++) {
			dataTemp[i] = Integer.valueOf(valuesString[i]);
			data[i] = dataTemp[i];
		}
		for (int i = 0; i < data.length; i++) if(data[i] == 0) data[i]++;
		return data;
//		long timeStamp = (new Date()).getTime();
//		String fileName = "data_" + timeStamp + ".bin";
//		String varName = "FrontCut_" + timeStamp;
//		sendCommand(varName + "=" + fullSignalName + FrontCut + ";"); 
//		sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
//		sendCommand("clear " + varName + ";");
//		String absoluteFilePath = workspace + File.separator + fileName;
////		File dataFile = (new File(absoluteFilePath));
////		while (!dataFile.exists());
//		int[] data = Utils.readIntegerBinaryDataFile(absoluteFilePath);
//		(new File(absoluteFilePath)).delete();
//		return data;
	}

	public int getFrontCut(String fullSignalName, int trialNumber) {
		String response = sendCommand(fullSignalName + FrontCut + "(" + trialNumber + ")");
		response = clearStringValue(response);
		int value = Integer.parseInt(response);
		if(value == 0) value++;
		return value;
	}

	public String getLastErrorMessage() {
		if(isStarted())
			try {
				long timeStamp = (new Date()).getTime();
				String varName = "Error_" + timeStamp;
				String cmd = varName + "=lasterror;";
				evalString(cmd);
				cmd = "isstruct(" + varName + "),'>>'";
				evalString(cmd);
				String response = receive();
				response = clearStringValue(response);
				if(response.equals("1")) {
					cmd = varName + ".message,'>>'";
					evalString(cmd);
					String message = receive();
					message = message.replaceAll("\\n", "");
					message = message.replaceAll("ans(\\s)*=(\\s)*", "");
					message = message.replaceAll(">", "");
					if(!message.equals("")) logger.IlogErrorMessage(message);
					evalString("clear " + varName + ";");
					evalString("lasterr('');");
					return message;
				}
			} catch (Exception e) {
				logger.IlogErrorMessage(e);
			}
		else logger.IlogErrorMessage(Messages.getString("MathEngine.NotStarted"));
		return "";
	}

	public String getMarkersGroupLabel(int markersGroupNumber, String fullSignalName) {
		String response = sendCommand(fullSignalName + Marker + markersGroupNumber + _Label);
		response = clearStringValue(response);
		return response;
	}

	public String[] getMarkersGroupsLabels(String fullSignalName) {
		String[] markersGroupsLabels = new String[getNbMarkersGroups(fullSignalName)];
		for (int i = 0; i < markersGroupsLabels.length; i++) {
			markersGroupsLabels[i] = getMarkersGroupLabel(i+1, fullSignalName);
		}
		return markersGroupsLabels;
	}

	public int getNbFields(String fullSignalName) {
		String response = sendCommand(fullSignalName + NbFields);
		response = clearStringValue(response);
		return Integer.parseInt(response);
	}

	public int getNbMarkersGroups(String fullSignalName) {
		String response = sendCommand(fullSignalName + NbMarkers);
		response = clearStringValue(response);
		return Integer.parseInt(response);
	}

	public int[] getNbSamples(String fullSignalName) {
		String response = sendCommand(fullSignalName + NbSamples);
		response = clearStringValue2(response);
		String[] valuesString = response.split(":");
		Integer[] dataTemp = new Integer[valuesString.length];
		int[] data = new int[valuesString.length];
		for (int i = 0; i < data.length; i++) {
			dataTemp[i] = Integer.valueOf(valuesString[i]);
			data[i] = dataTemp[i];
		}
		return data;
//		long timeStamp = (new Date()).getTime();
//		String fileName = "data_" + timeStamp + ".bin";
//		String varName = "NbSamples_" + timeStamp;
//		sendCommand(varName + "=" + fullSignalName + NbSamples + ";"); 
//		sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
//		sendCommand("clear " + varName + ";");
//		String absoluteFilePath = workspace + File.separator + fileName;
////		File dataFile = (new File(absoluteFilePath));
////		while (!dataFile.exists());
//		int[] data = Utils.readIntegerBinaryDataFile(absoluteFilePath);
//		(new File(absoluteFilePath)).delete();
//		return data;
	}

	public int getNbSamples(String fullSignalName, int trialNumber) {
		String response = sendCommand(fullSignalName + NbSamples + "(" + trialNumber + ")");
		response = clearStringValue(response);
		return Integer.parseInt(response);
	}

	public int getNbTrials(String fullSignalName) {
		String varName = "nbTrials_" + (new Date()).getTime();
		sendCommand(varName + "=size(" + fullSignalName + Values + ");");
		String response = sendCommand(varName + "(1)");
		response = clearStringValue(response);
		sendCommand("clear " + varName + ";");
		return Integer.parseInt(response);
	}

	public double getSampleFrequency(String fullSignalName) {
		String response = sendCommand(fullSignalName + SampleFrequence);
		response = clearStringValue(response);
		return Double.parseDouble(response);
	}

	public boolean isSignal(String fullSignalName) {
		String response = sendCommand(fullSignalName + isSignal);
		response = clearStringValue(response);
		return response.equals("1");
	}
	
	public boolean isCategory(String fullCategoryName) {
		String response = sendCommand(fullCategoryName + isCategory);
		response = clearStringValue(response);
		return response.equals("1");
	}
	
	public boolean isEventsGroup(String fullEventName) {
		String response = sendCommand(fullEventName + isEvent);
		response = clearStringValue(response);
		return response.equals("1");
	}
	
	public String[] getSignalsNames(String fullSubjectName) {
		String[] channelsNames = getChannelsNames(fullSubjectName);
		ArrayList<String> signalsNames = new ArrayList<String>(0);
		for (int i = 0; i < channelsNames.length; i++) 
			if(isSignal(fullSubjectName + "." + channelsNames[i])) signalsNames.add(channelsNames[i]);
		return signalsNames.toArray(new String[signalsNames.size()]);
	}

	public int[] getTrialsListForCategory(String fullCategoryName) {
		String response = sendCommand(fullCategoryName + TrialsList);
		response = clearStringValue2(response);
		String[] valuesString = response.split(":");
		Integer[] dataTemp = new Integer[valuesString.length];
		int[] data = new int[valuesString.length];
		for (int i = 0; i < data.length; i++) {
			dataTemp[i] = Integer.valueOf(valuesString[i]);
			data[i] = dataTemp[i];
		}
		return data;
//		long timeStamp = (new Date()).getTime();
//		String fileName = "data_" + timeStamp + ".bin";
//		String varName = "TrialsList_" + timeStamp;
//		sendCommand(varName + "=" + fullCategoryName + TrialsList + ";"); 
//		sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
//		sendCommand("clear " + varName + ";");
//		String absoluteFilePath = workspace + File.separator + fileName;
////		File dataFile = (new File(absoluteFilePath));
////		while (!dataFile.exists());
//		int[] data = Utils.readIntegerBinaryDataFile(absoluteFilePath);
//		(new File(absoluteFilePath)).delete();
//		return data;
	}
	
	public String getCategoryNameFromTrial(String fullSubjectName, int trialNumber) {
		ArrayList<String> fieldNames  = new ArrayList<String>(0) ;
		fieldNames.addAll(Arrays.asList(getOctaveFieldsNames(fullSubjectName)));
		if(fieldNames.indexOf(Categories_No_Point) > -1) {
			fieldNames.clear();
			fieldNames.addAll(Arrays.asList(getOctaveFieldsNames(fullSubjectName+ Categories)));
			if(fieldNames.indexOf(Names_No_Point) > -1) {
				String response = sendCommand(fullSubjectName + Categories + Names + "(" + trialNumber + ")");
				response = clearStringValue(response);
				response = response.replaceAll("^'", "");
				response = response.replaceAll("'$", "");
				return response;
			}
		}
		return "";
	}

	public int[] getTrialsListForMarkersGroup(int markersGroupNumber, String fullSignalName) {
		String response = sendCommand(fullSignalName + Marker + markersGroupNumber + _Values + "(:,1)'");
		response = clearStringValue2(response);
		String[] valuesString = response.split(":");
		Integer[] dataTemp = new Integer[valuesString.length];
		int[] data = new int[valuesString.length];
		for (int i = 0; i < data.length; i++) {
			dataTemp[i] = Integer.valueOf(valuesString[i]);
			data[i] = dataTemp[i];
		}
//		long timeStamp = (new Date()).getTime();
//		String fileName = "data_" + timeStamp + ".bin";
//		String varName = "TrialsMkrsGrp_" + timeStamp;
//		sendCommand(varName + "=" + fullSignalName + Marker + markersGroupNumber + _Values + "(:,1)';");
//		sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
//		sendCommand("clear " + varName + ";");
//		String absoluteFilePath = workspace + File.separator + fileName;
////		File dataFile = (new File(absoluteFilePath));
////		while (!dataFile.exists());
//		int[] data = Utils.readIntegerBinaryDataFile(absoluteFilePath);
//		(new File(absoluteFilePath)).delete();
		return data;
	}

	public double[] getValuesForTrialNumber(int trialNumber, String fullSignalName) {
		long timeStamp = (new Date()).getTime();
		String fileName = "data_" + timeStamp + ".bin";
		String varName = "Values_" + timeStamp;
		int frontCut = getFrontCut(fullSignalName, trialNumber);
		int endCut = getEndCut(fullSignalName, trialNumber);
		if(endCut == 0) return new double[0];
		sendCommand(varName + "=" + fullSignalName + Values + "(" + trialNumber +"," + frontCut + ":" + endCut + ");");
		sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
		sendCommand("clear " + varName + ";");
		String absoluteFilePath = workspace + File.separator + fileName;
//		File dataFile = (new File(absoluteFilePath));
//		while (!dataFile.exists());
		double[] data = Utils.readDoubleBinaryDataFile(absoluteFilePath);
		(new File(absoluteFilePath)).delete();
		return data;
	}
	
	public int[] getEventsGroupTrialsList(String fullEventName) {
		long timeStamp = (new Date()).getTime();
		String fileName = "data_" + timeStamp + ".bin";
		String varName = "Values_" + timeStamp;
		String cmd = varName + " = " + fullEventName + Values + "(:,1);";
		sendCommand(cmd);
		sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
		sendCommand("clear " + varName + ";");
		String absoluteFilePath = workspace + File.separator + fileName;
//		File dataFile = (new File(absoluteFilePath));
//		while (!dataFile.exists());
		int[] data = Utils.readIntegerBinaryDataFile(absoluteFilePath);
		(new File(absoluteFilePath)).delete();
		return data;
	}
	
	public int[] getEventsGroupUnduplicatedTrialsList(String fullEventName) {
		int[] trialsList = getEventsGroupTrialsList(fullEventName);
		
		HashSet<Integer> trialsListHashSet = new HashSet<Integer>(0);
		for (int i = 0; i < trialsList.length; i++) trialsListHashSet.add(Integer.valueOf(trialsList[i]));
		Integer[] unduplicatedTrialsListIntegers = trialsListHashSet.toArray(new Integer[trialsListHashSet.size()]);
		
		int[] unduplicatedTrialsList = new int[unduplicatedTrialsListIntegers.length];
		for (int i = 0; i < unduplicatedTrialsListIntegers.length; i++) unduplicatedTrialsList[i] = unduplicatedTrialsListIntegers[i];
		return unduplicatedTrialsList;
	}
	
	
	public double[][] getEventsGroupValues(String fullEventName) {
		long timeStamp = (new Date()).getTime();
		String fileName = "data_" + timeStamp + ".bin";
		String varName = "Values_" + timeStamp;
		
		String cmd = varName + " = " + fullEventName + Values + ";";
		sendCommand(cmd);
		cmd = varName + " = " + varName + "(:)";
		sendCommand(cmd);
		
		sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
		sendCommand("clear " + varName + ";");
		String absoluteFilePath = workspace + File.separator + fileName;
//		File dataFile = (new File(absoluteFilePath));
//		while (!dataFile.exists());
		double[] dataTemp = Utils.readDoubleBinaryDataFile(absoluteFilePath);
		(new File(absoluteFilePath)).delete();
		int nbLines = dataTemp.length / 4;
		double[][] data = new double[nbLines][4];
		int j = -1;
		for (int i = 0; i < dataTemp.length; i++) {
			if(i%nbLines == 0) j++;
			data[i%nbLines][j] = dataTemp[i];
			
		}
		return data;
	}
	
	public double[][] getEventsGroupValuesForTrialNumber(int trialNumber, String fullEventName) {
		long timeStamp = (new Date()).getTime();
		String fileName = "data_" + timeStamp + ".bin";
		String varName = "Values_" + timeStamp;
		
		String cmd = varName + " = find(" + fullEventName + Values + "(:,1) == " + trialNumber + ");";
		sendCommand(cmd);
		
		cmd = "isempty(" + varName + ");";
		String response = sendCommand(cmd);
		response = clearStringValue(response);
		if(response.equals("1")) {
			sendCommand("clear " + varName + ";");
			return new double[0][0];
		}
		
		cmd = varName + " = " + fullEventName  + Values + "(" + varName + ", [2:4])";
		sendCommand(cmd);
		cmd = varName + " = " + varName + "(:)";
		sendCommand(cmd);
		
		sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
		sendCommand("clear " + varName + ";");
		String absoluteFilePath = workspace + File.separator + fileName;
//		File dataFile = (new File(absoluteFilePath));
//		while (!dataFile.exists());
		double[] dataTemp = Utils.readDoubleBinaryDataFile(absoluteFilePath);
		(new File(absoluteFilePath)).delete();
		int nbLines = dataTemp.length / 3;
		double[][] data = new double[nbLines][3];
		int j = -1;
		for (int i = 0; i < dataTemp.length; i++) {
			if(i%nbLines == 0) j++;
			data[i%nbLines][j] = dataTemp[i];
		}
		return data;
	}

	public double getXValueForMarkersGroup(int markersGroupNumber, int arrayIndex, String fullSignalName) {
		double[] xValues = getXValuesForMarkersGroup(markersGroupNumber, fullSignalName);
		return xValues[arrayIndex];
	}

	public double[] getXValuesForMarkersGroup(int markersGroupNumber, String fullSignalName) {
		String response = sendCommand(fullSignalName + Marker + markersGroupNumber + _Values + "(:,2)'");
		response = clearStringValue2(response);
		String[] valuesString = response.split(":");
		double[] data = new double[valuesString.length];
		for (int i = 0; i < data.length; i++) data[i] = Double.valueOf(valuesString[i]);
//		long timeStamp = (new Date()).getTime();
//		String fileName = "data_" + timeStamp + ".bin";
//		String varName = "XValues_" + timeStamp;
//		sendCommand(varName + "=" + fullSignalName + Marker + markersGroupNumber + _Values + "(:,2)';");
//		sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
//		sendCommand("clear " + varName + ";");
//		String absoluteFilePath = workspace + File.separator + fileName;
////		File dataFile = (new File(absoluteFilePath));
////		while (!dataFile.exists());
//		double[] data = Utils.readDoubleBinaryDataFile(absoluteFilePath);
//		(new File(absoluteFilePath)).delete();
		return data;
	}

	public double getYValueForMarkersGroup(int markersGroupNumber, int arrayIndex, String fullSignalName) {
		double[] yValues = getYValuesForMarkersGroup(markersGroupNumber, fullSignalName);
		return yValues[arrayIndex];
	}

	public double[] getYValuesForMarkersGroup(int markersGroupNumber, String fullSignalName) {
		String response = sendCommand(fullSignalName + Marker + markersGroupNumber + _Values + "(:,3)'");
		response = clearStringValue2(response);
		String[] valuesString = response.split(":");
		double[] data = new double[valuesString.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = Double.parseDouble(valuesString[i]);
		}
		return data;
//		long timeStamp = (new Date()).getTime();
//		String fileName = "data_" + timeStamp + ".bin";
//		String varName = "YValues_" + timeStamp;
//		sendCommand(varName + "=" + fullSignalName + Marker + markersGroupNumber + _Values + "(:,3)';");
//		sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
//		sendCommand("clear " + varName + ";");
//		String absoluteFilePath = workspace + File.separator + fileName;
////		File dataFile = (new File(absoluteFilePath));
////		while (!dataFile.exists());
//		double[] data = Utils.readDoubleBinaryDataFile(absoluteFilePath);
//		(new File(absoluteFilePath)).delete();
//		return data;
	}

	public boolean isStarted() {
		return started;
	}
	
	private String[] getOctaveFieldsNames(String structName) {
		String response = sendCommand("fieldnames(" + structName + ")");
		response = clearStringValue(response);
		response = response.replaceAll("^\\{", "");
		response = response.replaceAll("\\}$", "");
		response = response.replaceAll("\\[\\d+,\\d+\\]", ":");
		response = response.replaceAll("^:", "");
		return response.split(":");
	}
	
	private boolean isstruct(String var) {
		String response = sendCommand("isstruct(" + var + ")");
		response = clearStringValue(response);
		return response.equals("1");
	}
	
	private boolean exist(String var) {
		String response = sendCommand("exist('" + var + "','var')");
		response = clearStringValue(response);
		return response.equals("1");
	}

	public boolean isSubjectLoaded(String fullSubjectName) {
		String experiment = fullSubjectName.split("\\.")[0];
		if(exist(experiment))
		  if(isstruct(experiment)) {
			  String[] fieldsNames = getOctaveFieldsNames(experiment);
  			  String subject = fullSubjectName.split("\\.")[1];
			  for (int i = 0; i < fieldsNames.length; i++) if(fieldsNames[i].equals(subject)) return true;
		  }
		return false;
	}

	public boolean loadSubject(String fullSubjectName, String experimentType, String[] dataFiles) {
		if(isStarted()) {
//			logger.IlogMessage("Loading subject '" + fullSubjectName + "' from data file(s) :");
//			String[] subjectNameParts = fullSubjectName.split("\\.");
//			String dataFilesAbsolutePathString = "";	
//			for (int i = 0; i < dataFiles.length; i++) {
//				logger.IlogMessage(">> " + dataFiles[i]);
//				dataFilesAbsolutePathString = dataFilesAbsolutePathString +  dataFiles[i]+  ",";	
//			}
//			dataFilesAbsolutePathString = dataFilesAbsolutePathString.replaceAll(",$", "");
//			String cmdLine = "loadData('" + experimentType + "','" + subjectNameParts[0] + "','" + subjectNameParts[1] + "','" + dataFilesAbsolutePathString + "');";
//			sendCommand(cmdLine);		
//			boolean loaded = isSubjectLoaded(fullSubjectName);
//			if(loaded) logger.IlogMessage("Done");
//			else logger.IlogErrorMessage("Error loading subject '" + fullSubjectName + "'");
//			return loaded;
			if(dataFiles.length == 1 && dataFiles[0].endsWith(Subject.SAVING_FILE_NAME)) {
				logger.IlogMessage(Messages.getString("MathEngine.LoadingSubject") + fullSubjectName + Messages.getString("MathEngine.FromDataFile"));
				String experimentName = fullSubjectName.split("\\.")[0];
				String subjectName = fullSubjectName.split("\\.")[1];
				String fileName = workspace + File.separator + experimentName + File.separator + subjectName + File.separator + Subject.SAVING_FILE_NAME;
				if(CPlatform.isWindows()) fileName = fileName.replace("\\", "\\\\");
				logger.IlogMessage(">> " + fileName);
				String cmd = "load '" + fileName + "';";
				sendCommand(cmd);
				cmd = experimentName + "." + subjectName + " = subjectName;";
				sendCommand(cmd);
				cmd = "clear subjectName;"; 
				sendCommand(cmd);
				ArrayList<String> fieldNames  = new ArrayList<String>(0) ;
				fieldNames.addAll(Arrays.asList(getOctaveFieldsNames(fullSubjectName)));
				if(fieldNames.indexOf(Categories_No_Point) == -1) {
					sendCommand("buildCategories('" + fullSubjectName + "');");
				}
			} else {
				logger.IlogMessage(Messages.getString("MathEngine.LoadingSubject") + fullSubjectName + Messages.getString("MathEngine.FromDataFile"));
				String[] subjectNameParts = fullSubjectName.split("\\.");
				String dataFilesAbsolutePathString = "";	
				for (int i = 0; i < dataFiles.length && i < 20; i++) logger.IlogMessage(">> " + dataFiles[i]);
				for (int i = 0; i < dataFiles.length; i++) {
					if(CPlatform.isWindows()) dataFiles[i] = dataFiles[i].replace("\\", "\\\\");
					dataFilesAbsolutePathString = dataFilesAbsolutePathString +  dataFiles[i]+  ",";	
				}
				if(dataFiles.length > 20) logger.IlogMessage(">> etc.");
				dataFilesAbsolutePathString = dataFilesAbsolutePathString.replaceAll(",$", "");
				String cmdLine = "loadData('" + experimentType + "','" + subjectNameParts[0] + "','" + subjectNameParts[1] + "','" + dataFilesAbsolutePathString + "');";
				sendCommand(cmdLine);
				cmdLine = "exist('errorMessage','var')";
				String response  = sendCommand(cmdLine);
				response = clearStringValue(response);
				if(response.equals("1")) { 
					cmdLine = "errorMessage";				 
					String errorMessage = sendCommand(cmdLine);	
					errorMessage = errorMessage.split("=")[1];
					errorMessage = errorMessage.replaceAll("(\\n)*", "");
					errorMessage = errorMessage.replaceAll("=", "");
					errorMessage = errorMessage.replaceAll(">{2,}", "");
					logger.IlogErrorMessage(errorMessage);
					cmdLine = "clear errorMessage;";	 //$NON-NLS-1$
					sendCommand(cmdLine);
				}
				ArrayList<String> fieldNames  = new ArrayList<String>(0) ;
				fieldNames.addAll(Arrays.asList(getOctaveFieldsNames(fullSubjectName)));
				if(fieldNames.indexOf(Categories_No_Point) == -1) {
					sendCommand("buildCategories('" + fullSubjectName + "');");
				}
			}
			boolean loaded = isSubjectLoaded(fullSubjectName);
			if(loaded) logger.IlogMessage(Messages.getString("MathEngine.Done"));
			else logger.IlogErrorMessage(Messages.getString("MathEngine.ErrorLoading")  + fullSubjectName + "'");
			return loaded;
		}
		else logger.IlogErrorMessage(Messages.getString("MathEngine.NotStarted"));
		return false;
	}

	public boolean renameExperiment(String oldName, String newName) {
		sendCommand(newName + " = " + oldName + ";");
		sendCommand("clear " + oldName + ";");
		
		String cmd = "exist('" + oldName + "','var')";
		String response1  = sendCommand(cmd);
		response1 = clearStringValue(response1);
		
		cmd = "exist('" + newName + "','var')";
		String response2  = sendCommand(cmd);
		response2 = clearStringValue(response2);
		
		return response1.equals("0") && response2.equals("1");
	}
	
	public boolean renameChannel(String subjectName, String oldChannelName, String newChannelName) {
		String cmd = subjectName + " = " + "rnfield(" + subjectName + ",'" + oldChannelName +"','" + newChannelName + "');";
		sendCommand(cmd);
		
		boolean oldNameFound = false;
		boolean newNameFound = false;
		String[] subjectsNames = getOctaveFieldsNames(subjectName);
		for (int i = 0; i < subjectsNames.length; i++) {
			if(subjectsNames[i].equals(newChannelName)) newNameFound = true;
			if(subjectsNames[i].equals(oldChannelName)) oldNameFound = true;
		}
		
		return newNameFound && !oldNameFound;
	}

	public boolean renameSubject(String experimentName, String oldSubjectName, String newSubjectName) {
		String cmd = experimentName + " = " + "rnfield(" + experimentName + ",'" + oldSubjectName +"','" + newSubjectName + "');";
		sendCommand(cmd);
		
		boolean oldNameFound = false;
		boolean newNameFound = false;
		String[] subjectsNames = getOctaveFieldsNames(experimentName);
		for (int i = 0; i < subjectsNames.length; i++) {
			if(subjectsNames[i].equals(newSubjectName)) newNameFound = true;
			if(subjectsNames[i].equals(oldSubjectName)) oldNameFound = true;
		}
		
		return newNameFound && !oldNameFound;
	}

	public boolean saveSubject(String fullSubjectName) {
		logger.IlogMessage(Messages.getString("MatlabController.SavingSubject") + fullSubjectName); 
		String experimentName = fullSubjectName.split("\\.")[0]; 
		String subjectName = fullSubjectName.split("\\.")[1]; 
		String cmd = "subjectName = " + fullSubjectName + ";;";//Double ';' is very important ! If not present, Octave will output the whole structure : much more the 65535 chars ...
		sendCommand(cmd);
		String fileName = workspace + File.separator + experimentName + File.separator + subjectName + File.separator + Subject.SAVING_FILE_NAME;
		fileName = fileName.replace("\\", "\\\\");
		cmd = "save -v6 '" + fileName + "' subjectName;";
		sendCommand(cmd);
		cmd = "clear subjectName;";
		sendCommand(cmd);
		return (new File(fileName)).exists();
	}

	public String sendCommand(String cmd) {
		if(isStarted())
			try {
				if(cmd.endsWith(";")) cmd = cmd.replaceAll(";$", "");
				cmd = cmd + ",'>>'";
				evalString(cmd);
				String response = receive();
				getLastErrorMessage();
				try {
					String line = "";
					while(errorBuffer.ready())
						if((line = errorBuffer.readLine()) != null) logger.IlogErrorMessage(line);
				} catch(IOException e) {
					logger.IlogErrorMessage(e);
				}
				return response;
			} catch (Exception e) {
				getLastErrorMessage();
				logger.IlogErrorMessage(e);
			}
		else logger.IlogErrorMessage(Messages.getString("MathEngine.NotStarted"));
		return "";
		
	}
	
	private String clearStringValue(String response) {
		response = response.replaceAll("(\\n)*", "");
		response = response.replaceAll("(\\s)*", "");
		response = response.replaceAll("^ans\\s*=\\s*", "");
		response = response.replaceAll("=", "");
		response = response.replaceAll(">", "");
		return response;
	}
	
	private String clearStringValue2(String response) {
		response = response.replaceAll("(\\n)*", "");
		response = response.replaceAll("(\\s)+", ":");
		response = response.replaceAll("Columns:(\\d)+:", "");
		response = response.replaceAll("through:(\\d)+:", "");
		response = response.replaceAll("Column:(\\d)+:", "");
		response = response.replaceAll("and:(\\d)+:", "");
		response = response.replaceAll("^ans\\s*=\\s*", "");
		response = response.replaceAll("^ans\\s*:\\s*", "");
		response = response.replaceAll("=", "");
		response = response.replaceAll(">", "");
		response = response.replaceAll("(:)+", ":");
		response = response.replaceAll(":$", "");
		response = response.replaceAll("^:", "");
		return response;
	}
	
	private String receive() {
		
		long t1;
		long t2;
		boolean readComplete = false;
//		int total = 0;
		if(continueReceive) {
			int charRead = 0;
			synchronized (this) {
				try {
					char[] localBuffer = new char[65535];
					Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
					t1 = System.currentTimeMillis();
					while(started && !readComplete) {
						while(inputBuffer.ready()) {
							charRead = inputBuffer.read(localBuffer, 0, 65535);
							if(charRead > 0) {
								buffer.append(localBuffer,0,charRead);
//								total += charRead;
								t1 = System.currentTimeMillis();
							}
							if((buffer.length() > 2) && (buffer.charAt(buffer.length() - 3) == '>') && (buffer.charAt(buffer.length() - 2) == '>')) readComplete = true;
						}
						t2 = System.currentTimeMillis();
						if(t2 - t1 > 10000) {
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									continueReceive = MessageDialog.openQuestion(Display.getDefault().getActiveShell(), Messages.getString("MathEngine.TimeOut"), Messages.getString("MathEngine.Continue"));
								}
							});
							if(!continueReceive) {
								Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
								return "";
							} else t1 = System.currentTimeMillis();
						}
					}
				} catch (IOException e) {
					logger.IlogErrorMessage(e);
				} finally {
					Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
				}
			}
		} else {
			int returnValue = process.exitValue();
			if(returnValue != 0) {
				stop();
				logger.IlogErrorMessage("Math engine crashed. Return value : " + returnValue);
			}
			continueReceive = true;
		}
		String value = buffer.toString();
		buffer.delete(0, buffer.length());
		return value;
	}
	
	private void evalString(String cmd) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("eval(\"");
		stringBuffer.append(cmd);
		stringBuffer.append("\",\"'>>'\")\n");
		cmd = stringBuffer.toString();
		synchronized (this) {
			try {
				outputBuffer.write(cmd,0,cmd.length());
				outputBuffer.flush();
			} catch (IOException e) {
				logger.IlogErrorMessage(e);
			}
		}
	}

	public boolean start(String[] pathsToAdd, String workingDirectory) {
		try {
			logger.IlogMessage(Messages.getString("MathEngine.Starting"));
			
			if(!AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.MATH_ENGINE_EXECUTABLE_PATH).equals(""))  {
				if(new File(AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.MATH_ENGINE_EXECUTABLE_PATH)).exists())
				cmdArray[2] = AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.MATH_ENGINE_EXECUTABLE_PATH);
				else {
					logger.IlogErrorMessage(Messages.getString("MathEngine.ErrorStarting")  + AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.MATH_ENGINE_EXECUTABLE_PATH) +
							Messages.getString("MathEngine.PathNotValid"));
					return started;
				}
			}
			process = Runtime.getRuntime().exec(cmdArray);
			inputBuffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
			outputBuffer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			errorBuffer = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			
			receive();
			try {
				String line = "";
				while(errorBuffer.ready())
					if((line = errorBuffer.readLine()) != null) logger.IlogErrorMessage(line);
			} catch(IOException e) {
				logger.IlogErrorMessage(e);
			}
			
			started = true;
			logger.IlogMessage(Messages.getString("MathEngine.Done"));
			
			version = clearStringValue(sendCommand("version"));
			logger.IlogMessage(Messages.getString("MathEngine.Version") + version);
			
			logger.IlogMessage(Messages.getString("MathEngine.AddPath"));
			for (int i = 0; i < pathsToAdd.length; i++) {
				if(CPlatform.isWindows()) pathsToAdd[i] = pathsToAdd[i].replace("\\", "\\\\");
				sendCommand("addpath('" + pathsToAdd[i] + "');");
				logger.IlogMessage(">> " + pathsToAdd[i]);
			}
			logger.IlogMessage(Messages.getString("MathEngine.ChangeWorkingDirectory"));
			workspace = workingDirectory;
			if(CPlatform.isWindows()) workingDirectory = workingDirectory.replace("\\", "\\\\");
			sendCommand("cd '" + workingDirectory + "';");
			logger.IlogMessage(">> " + workingDirectory);
			
		} catch (IOException e) {
			logger.IlogErrorMessage(e);
		}
		return started;
	}

	public boolean stop() {
		if(isStarted()) {
			try {
				logger.IlogMessage(Messages.getString("MathEngine.Stop"));
				inputBuffer.close();
				outputBuffer.close();
				errorBuffer.close();
				process.destroy();
				logger.IlogMessage(Messages.getString("MathEngine.Done"));
				started = false;
				return true;
			} catch (IOException e) {
				logger.IlogErrorMessage(e);
			}
		}
		return !started;
	}

	public boolean unloadSubject(String fullSubjectName) {
		String experiment = fullSubjectName.split("\\.")[0];
		String subject = fullSubjectName.split("\\.")[1];
		logger.IlogMessage(Messages.getString("MathEngine.UnLoadingSubject")  + fullSubjectName + "'");
		String cmdLine = experiment + " = rmfield(" + experiment + ",'" + subject + "');";
		sendCommand(cmdLine);
		boolean loaded = isSubjectLoaded(fullSubjectName);
		if(!loaded) logger.IlogMessage(Messages.getString("MathEngine.Done"));
		else logger.IlogErrorMessage(Messages.getString("MathEngine.ErrorUnLoading") + fullSubjectName + "'");
		return loaded;
	}
	
	public boolean isChannelExists(String fullSignalName) {
		String fullSubjectName = fullSignalName.replaceAll("\\.\\w+$", "");
		String channelName = fullSignalName.split("\\.")[2];
		String[] channelsNames = getChannelsNames(fullSubjectName);
		for (int i = 0; i < channelsNames.length; i++) if(channelName.equals(channelsNames[i])) return true;
		return false;
	}
	
	public void rehash() {
		sendCommand("rehash;");
	}
	
	public boolean runProcessing() {
		rehash();
		runProcessingErrors.clear();
		String cmdLine = "process;";
		sendCommand(cmdLine);
		getLastErrorMessage();
		cmdLine = "exist('errorMessage','var')";
		String response  = sendCommand(cmdLine);
		response = clearStringValue(response);
		if(response.equals("1")) { 
			cmdLine = "errorMessage";				 
			String errorMessage = sendCommand(cmdLine);	
			errorMessage = errorMessage.split("=")[1];
			runProcessingErrors.addAll(Arrays.asList(errorMessage.split(":")));
			cmdLine = "clear errorMessage;";	 //$NON-NLS-1$
			sendCommand(cmdLine);
		} else {
			newSignals.clear();
			cmdLine = "createdSignalsNames"; //$NON-NLS-1$
			String createdSignalsNames = ""; //$NON-NLS-1$
			createdSignalsNames = sendCommand(cmdLine);
			if(createdSignalsNames.split("=").length > 1) { //$NON-NLS-1$
				createdSignalsNames = createdSignalsNames.split("=")[1];			 //$NON-NLS-1$
				createdSignalsNames = clearStringValue(createdSignalsNames);
				createdSignalsNames = createdSignalsNames.replaceAll("^:", "");		 //$NON-NLS-1$ //$NON-NLS-2$
				createdSignalsNames = createdSignalsNames.replaceAll("''", ""); //$NON-NLS-1$ //$NON-NLS-2$
				cmdLine = "clear createdSignalsNames;";	 //$NON-NLS-1$
				sendCommand(cmdLine);
				if(!createdSignalsNames.equals(""))
				newSignals.addAll(Arrays.asList(createdSignalsNames.split(":")));
			}
			newMarkers.clear();
			cmdLine = "createdMarkersNames";		 //$NON-NLS-1$
			String createdMarkersNames = ""; //$NON-NLS-1$
			createdMarkersNames = sendCommand(cmdLine);		
			if(createdMarkersNames.split("=").length > 1) { //$NON-NLS-1$
				createdMarkersNames = createdMarkersNames.split("=")[1];	 //$NON-NLS-1$
				createdMarkersNames = clearStringValue(createdMarkersNames);
				createdMarkersNames = createdMarkersNames.replaceAll("^:", "");	 //$NON-NLS-1$ //$NON-NLS-2$
				createdMarkersNames = createdMarkersNames.replaceAll("''", ""); //$NON-NLS-1$ //$NON-NLS-2$
				cmdLine = "clear createdMarkersNames;";	 //$NON-NLS-1$
				sendCommand(cmdLine);
				if(!createdMarkersNames.equals(""))
				newMarkers.addAll(Arrays.asList(createdMarkersNames.split(":")));
			}
			newFields.clear();
			cmdLine = "createdFieldsNames";			 //$NON-NLS-1$
			String createdFieldsNames = ""; //$NON-NLS-1$
			createdFieldsNames = sendCommand(cmdLine);		
			if(createdFieldsNames.split("=").length > 1) { //$NON-NLS-1$
				createdFieldsNames = createdFieldsNames.split("=")[1];			 //$NON-NLS-1$
				createdFieldsNames = clearStringValue(createdFieldsNames);
				createdFieldsNames = createdFieldsNames.replaceAll("^:", "");		 //$NON-NLS-1$ //$NON-NLS-2$
				createdFieldsNames = createdFieldsNames.replaceAll("''", ""); //$NON-NLS-1$ //$NON-NLS-2$
				cmdLine = "clear createdFieldsNames;";	 //$NON-NLS-1$
				sendCommand(cmdLine);
				if(!createdFieldsNames.equals(""))
				newFields.addAll(Arrays.asList(createdFieldsNames.split(":")));
			}
			modifiedSignals.clear();
			cmdLine = "modifiedSignalsNames";			 //$NON-NLS-1$
			String modifiedSignalsNames = ""; //$NON-NLS-1$
			modifiedSignalsNames = sendCommand(cmdLine);		
			if(modifiedSignalsNames.split("=").length > 1) { //$NON-NLS-1$
				modifiedSignalsNames = modifiedSignalsNames.split("=")[1];			 //$NON-NLS-1$
				modifiedSignalsNames = clearStringValue(modifiedSignalsNames);
				modifiedSignalsNames = modifiedSignalsNames.replaceAll("^:", "");		 //$NON-NLS-1$ //$NON-NLS-2$
				modifiedSignalsNames = modifiedSignalsNames.replaceAll("''", ""); //$NON-NLS-1$ //$NON-NLS-2$
				cmdLine = "clear modifiedSignalsNames;";	 //$NON-NLS-1$
				sendCommand(cmdLine);
				if(!modifiedSignalsNames.equals(""))
					modifiedSignals.addAll(Arrays.asList(modifiedSignalsNames.split(":")));
			}
		}
		return runProcessingErrors.size() == 0;
	}
	
	public String[] getNewFieldsNames() {
		return newFields.toArray(new String[newFields.size()]);
	}

	public String[] getNewMarkersNames() {
		return newMarkers.toArray(new String[newMarkers.size()]);
	}

	public String[] getNewSignalsNames() {
		return newSignals.toArray(new String[newSignals.size()]);
	}
	
	public String[] getModifiedSignalsNames() {
		return modifiedSignals.toArray(new String[modifiedSignals.size()]);
	}

	public String[] getProcessingErrors() {
		return runProcessingErrors.toArray(new String[runProcessingErrors.size()]);
	}
	
	public String getMarkersGroupGraphicalSymbol(int markersGroupNumber, String fullSignalName) {
		String cmd = "isfield(" + fullSignalName + ",'" +  Marker_No_Point + markersGroupNumber + _Symbol + "')";
		String response = sendCommand(cmd);
		response = clearStringValue(response);
		if(response.equals("1")) {
			response = sendCommand(fullSignalName + Marker + markersGroupNumber + _Symbol);
			response = clearStringValue(response);
			return response;
		}
		return null;
	}

	public void setMarkersGroupGraphicalSymbol(int markersGroupNumber, String fullSignalName, String symbolName) {
		sendCommand(fullSignalName + Marker + markersGroupNumber + _Symbol + " = '" + symbolName + "';");
	}

//	public static void main(String[] args) {
//		ILog logger = Log.getInstance();
//		UnixOctaveEngine unixOctaveEngine = new UnixOctaveEngine(logger);
//		Log.logMessage("START MATH ENGINE");
//		//unixMatlabEngine.start(new String[]{"/Users/frank/Documents/PROJETS/EclipseWorkspaces/Analyse/analyse2/matlabscripts","/Users/frank/Documents/PROJETS/EclipseWorkspaces/Analyse/analyse2/matlabscripts/library"}, "/Users/frank/Desktop/ANALYSE2/W");
////		unixMatlabEngine.start(new String[]{"//home/frank/Documents/PROJETS/eclipseWorkspaces/Analyse/analyse2/matlabscripts","/home/frank/Documents/PROJETS/eclipseWorkspaces/Analyse/analyse2/matlabscripts/library"}, "/home/frank/Bureau/ANALYSE2/W");
//		unixOctaveEngine.start(new String[]{}, "/");
//		logger.IlogMessage("START MATH ENGINE END");
//		
//		
//		String response = unixOctaveEngine.sendCommand("help butter");
//		
//		System.out.println(response);
		
//		logger.IlogMessage("LOAD SUBJECT");
//		unixMatlabEngine.loadSubject("exp3.a", "OLD_ANALYSE", new String[]{"/home/frank/Bureau/ANALYSE2/W/exp3/a/save.mat"});
//		logger.IlogMessage("LOAD SUBJECT END");
//		logger.IlogMessage("IS SUBJECT LOADED");
//		System.out.println(unixMatlabEngine.isSubjectLoaded("exp3.a"));
//		logger.IlogMessage("IS SUBJECT LOADED END");
//		logger.IlogMessage("GET SIGNALS NAMES");
//		String[] signals = unixMatlabEngine.getSignalsNames("exp3.a");
//		for (int i = 0; i < signals.length; i++) {
//			System.out.println(">> " + signals[i]);
//		}
//		logger.IlogMessage("GET SIGNALS NAMES END");
//		logger.IlogMessage("SAVE SUBJECT");
//		System.out.println(unixMatlabEngine.saveSubject("exp3.a"));
//		logger.IlogMessage("SAVE SUBJECT END");
//		logger.IlogMessage("UNLOAD SUBJECT");
//		unixMatlabEngine.unloadSubject("exp3.a");
//		logger.IlogMessage("UNLOAD SUBJECT END");
//		logger.IlogMessage("IS SUBJECT LOADED");
//		System.out.println(unixMatlabEngine.isSubjectLoaded("exp3.a"));
//		logger.IlogMessage("IS SUBJECT LOADED END");
//		logger.IlogMessage("STOP");
//		unixOctaveEngine.stop();
//		logger.IlogMessage("STOP END");
//	}
}
