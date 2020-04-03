/*******************************************************************************
 * Université d'Aix Marseille (AMU) - Centre National de la Recherche Scientifique (CNRS)
 * Copyright 2014 AMU-CNRS All Rights Reserved.
 * 
 * These computer program listings and specifications, herein, are
 * the property of Université d'Aix Marseille and CNRS
 * shall not be reproduced or copied or used in whole or in part as
 * the basis for manufacture or sale of items without written permission.
 * For a license agreement, please contact:
 * <mailto: licensing@sattse.com> 
 * 
 * Author : Frank BULOUP
 * Institut des Sciences du Mouvement - frank.buloup@univ-amu.fr
 ******************************************************************************/
package mathengine;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.eclipse.swt.widgets.Display;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.SafeArray;
import com.jacob.com.Variant;

import analyse.Utils;
import analyse.model.Subject;
import analyse.resources.Messages;

public final class WindowsMatlabEngine implements IMathEngine {
	
	private static String version;
	private static int mainVersion;

	private ILog logger;
	private static ActiveXComponent matlabAutomationServer;
	private boolean started = false;
	private String workspace;
	private HashSet<String> runProcessingErrors = new HashSet<String>(0);
	private static HashSet<String> newSignals = new HashSet<String>(0);
	private static HashSet<String> newMarkers = new HashSet<String>(0);
	private static HashSet<String> newFields = new HashSet<String>(0);
	private static HashSet<String> modifiedSignals = new HashSet<String>(0);

	protected WindowsMatlabEngine(ILog logger) {
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
		String[] fieldsNames = getMatlabFieldsNames(fullSignalName);
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
		String[] fieldsNames = getMatlabFieldsNames(fullSignalName);
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

	public String[] getChannelsNames(String fullSubjectName) {
		return getMatlabFieldsNames(fullSubjectName);
	}

	public String getCriteriaForCategory(String fullCategoryName) {
		String response = sendCommand(fullCategoryName + Criteria);
		response = response.replaceAll("(\\n)*", "");
		response = response.replaceAll("^ans\\s*=\\s*", "");
		response = response.replaceAll(">>\\s*$", "");
		return response;
	}
	
	public String getCriteriaForEventsGroup(String fullEventName) {
		String response = sendCommand(fullEventName + Criteria);
		response = response.replaceAll("(\\n)*", "");
		response = response.replaceAll("^ans\\s*=\\s*", "");
		response = response.replaceAll(">>\\s*$", "");
		return response;
	}
	
	private Variant getVariable(String varName, String workspaceLocation) {
		Variant variant = matlabAutomationServer.invoke("GetVariable", new Variant(varName), new Variant(workspaceLocation));
		return variant;
	}
	
	private double[] getVariableAsDoubleArray(String varName, String workspaceLocation) {
		Variant variant = getVariable(varName, workspaceLocation);
		if(variant.isNull()) return new double[0];
		double[] data = new double[0];
		if(variant.toJavaObject() instanceof SafeArray) {
			data = variant.toSafeArray().toDoubleArray();
		} else if(variant.toJavaObject() instanceof Double) {
			double dataTemp = ((Double)variant.toJavaObject());
			data = new double[]{dataTemp};
		}
		return data;
	}
	
	private int[] getVariableAsIntArray(String varName, String workspaceLocation) {
		Variant variant = getVariable(varName, workspaceLocation);
		if(variant.isNull()) return new int[0];
		int[] data = new int[0];
		if(variant.toJavaObject() instanceof SafeArray) {
			double[] dataTemp = variant.toSafeArray().toDoubleArray();
			data = new int[dataTemp.length];
			for (int i = 0; i < dataTemp.length; i++) data[i] = (int)dataTemp[i];
		} else if(variant.toJavaObject() instanceof Double) {
			int dataTemp = ((Double)variant.toJavaObject()).intValue();
			data = new int[]{dataTemp};
		}
		return data;
	}

	public int[] getEndCut(String fullSignalName) {
		if(mainVersion < 7) {
			long timeStamp = (new Date()).getTime();
			String fileName = "data_" + timeStamp + ".bin";
			String varName = "EndCut_" + timeStamp;
			sendCommand(varName + "=" + fullSignalName + EndCut + ";"); 
			sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
			sendCommand("clear " + varName + ";");
			String absoluteFilePath = workspace + File.separator + fileName;
			int[] data = Utils.readIntegerBinaryDataFile(absoluteFilePath);
			(new File(absoluteFilePath)).delete();
			return data;
		}
		long timeStamp = (new Date()).getTime();
		String varName = "EndCut_" + timeStamp;
		sendCommand(varName + "=" + fullSignalName + EndCut + ";"); 
//		int[] data = new int[getNbTrials(fullSignalName)];
//		try {
//			double[] dataTemp = new double[getNbTrials(fullSignalName)];
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", varName, "base");
//			Variant variant1 = new Variant(varName);
//			Variant variant2 = new Variant("base");
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", variant1, variant2);
//			if(dataTempObject instanceof Double)
//			dataTemp = new double[]{(Double) dataTempObject};
//			else if(dataTempObject instanceof double[]) dataTemp = (double[]) dataTempObject;
//			data = new int[dataTemp.length];
//			for (int i = 0; i < dataTemp.length; i++) data[i] = (int)dataTemp[i];
			
		int[] data = getVariableAsIntArray(varName, "base");
			
			
//		} catch (COMException e) {
//			logger.IlogErrorMessage(e);
//		}
		sendCommand("clear " + varName + ";");
		return data;
	}
	
	public String[] getFieldsLabels(String fullSignalName) {
		String[] fieldsLabels = new String[getNbFields(fullSignalName)];
		for (int i = 0; i < fieldsLabels.length; i++) fieldsLabels[i] = getFieldLabel(i+1, fullSignalName);
		return fieldsLabels;
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
		if(mainVersion < 7) {
			long timeStamp = (new Date()).getTime();
			String fileName = "data_" + timeStamp + ".bin";
			String varName = "FieldValues_" + timeStamp;
			sendCommand(varName + "=" + fullSignalName + Field + fieldNumber + _Values + "';"); 
			sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
			sendCommand("clear " + varName + ";");
			String absoluteFilePath = workspace + File.separator + fileName;
			double[] data = Utils.readDoubleBinaryDataFile(absoluteFilePath);
			(new File(absoluteFilePath)).delete();
			return data;
		}
		long timeStamp = (new Date()).getTime();
		String varName = "FieldValues_" + timeStamp;
		sendCommand(varName + "=" + fullSignalName + Field + fieldNumber + _Values + ";"); 
//		double[] data = new double[0];//getNbFields(fullSignalName)];
//		try {
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", varName, "base");
//			Variant variant1 = new Variant(varName);
//			Variant variant2 = new Variant("base");
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", variant1, variant2);
//			if(dataTempObject instanceof Double)
//			data = new double[]{(Double) dataTempObject};
//			else if(dataTempObject instanceof double[]) data = (double[]) dataTempObject;
//		} catch (COMException e) {
//			logger.IlogErrorMessage(e);
//		}
			
		double[] data = getVariableAsDoubleArray(varName, "base");
			
		sendCommand("clear " + varName + ";");
		return data;
	}

	public int[] getFrontCut(String fullSignalName) {
		if(mainVersion < 7) {
			long timeStamp = (new Date()).getTime();
			String fileName = "data_" + timeStamp + ".bin";
			String varName = "FrontCut_" + timeStamp;
			sendCommand(varName + "=" + fullSignalName + FrontCut + ";"); 
			sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
			sendCommand("clear " + varName + ";");
			String absoluteFilePath = workspace + File.separator + fileName;
			int[] data = Utils.readIntegerBinaryDataFile(absoluteFilePath);
			(new File(absoluteFilePath)).delete();
			for (int i = 0; i < data.length; i++) if(data[i] == 0) data[i]++;
			return data;
		}
		long timeStamp = (new Date()).getTime();
		String varName = "FrontCut_" + timeStamp;
		sendCommand(varName + "=" + fullSignalName + FrontCut + ";"); 
//		int[] data = new int[getNbTrials(fullSignalName)];
//		try {
//			double[] dataTemp = new double[getNbTrials(fullSignalName)];
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", varName, "base");
//			Variant variant1 = new Variant(varName);
//			Variant variant2 = new Variant("base");
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", variant1, variant2);
//			if(dataTempObject instanceof Double)
//			dataTemp = new double[]{(Double) dataTempObject};
//			else if(dataTempObject instanceof double[]) dataTemp = (double[]) dataTempObject;
//			data = new int[dataTemp.length];
//			for (int i = 0; i < dataTemp.length; i++) data[i] = (int)dataTemp[i];
//		} catch (COMException e) {
//			logger.IlogErrorMessage(e);
//		}
		int[] data = getVariableAsIntArray(varName, "base");
		sendCommand("clear " + varName + ";");
		for (int i = 0; i < data.length; i++) if(data[i] == 0) data[i]++;
		return data;
	}


	public String[] getMarkersGroupsLabels(String fullSignalName) {
		String[] markersGroupsLabels = new String[getNbMarkersGroups(fullSignalName)];
		for (int i = 0; i < markersGroupsLabels.length; i++) {
			markersGroupsLabels[i] = getMarkersGroupLabel(i+1, fullSignalName);
		}
		return markersGroupsLabels;
	}
	
	public String getMarkersGroupLabel(int markersGroupNumber, String fullSignalName) {
		String response = sendCommand(fullSignalName + Marker + markersGroupNumber + _Label);
		response = clearStringValue(response);
		return response;
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
		if(mainVersion < 7) {
			long timeStamp = (new Date()).getTime();
			String fileName = "data_" + timeStamp + ".bin";
			String varName = "NbSamples_" + timeStamp;
			sendCommand(varName + "=" + fullSignalName + NbSamples + ";"); 
			sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
			sendCommand("clear " + varName + ";");
			String absoluteFilePath = workspace + File.separator + fileName;
			int[] data = Utils.readIntegerBinaryDataFile(absoluteFilePath);
			(new File(absoluteFilePath)).delete();
			return data;
		}
		long timeStamp = (new Date()).getTime();
		String varName = "NbSamples_" + timeStamp;
		sendCommand(varName + "=" + fullSignalName + NbSamples + ";"); 
//		int[] data = new int[getNbTrials(fullSignalName)];
//		try {
//			double[] dataTemp = new double[getNbTrials(fullSignalName)];
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", varName, "base");
//			Variant variant1 = new Variant(varName);
//			Variant variant2 = new Variant("base");
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", variant1, variant2);
//			if(dataTempObject instanceof Double)
//			dataTemp = new double[]{(Double) dataTempObject};
//			else if(dataTempObject instanceof double[]) dataTemp = (double[]) dataTempObject;
//			data = new int[dataTemp.length];
//			for (int i = 0; i < dataTemp.length; i++) data[i] = (int)dataTemp[i];
//		} catch (COMException e) {
//			logger.IlogErrorMessage(e);
//		}
		int[] data = getVariableAsIntArray(varName, "base");
		sendCommand("clear " + varName + ";");
		return data;
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

	public String[] getSignalsNames(String fullSubjectName) {
		String[] channelsNames = getChannelsNames(fullSubjectName);
		ArrayList<String> signalsNames = new ArrayList<String>(0);
		for (int i = 0; i < channelsNames.length; i++) 
			if(isSignal(fullSubjectName + "." + channelsNames[i])) signalsNames.add(channelsNames[i]);
		return signalsNames.toArray(new String[signalsNames.size()]);
		
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

	public int[] getTrialsListForCategory(String fullCategoryName) {
		if(mainVersion < 7) {
			long timeStamp = (new Date()).getTime();
			String fileName = "data_" + timeStamp + ".bin";
			String varName = "TrialsList_" + timeStamp;
			sendCommand(varName + "=" + fullCategoryName + TrialsList + ";"); 
			sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
			sendCommand("clear " + varName + ";");
			String absoluteFilePath = workspace + File.separator + fileName;
			int[] data = Utils.readIntegerBinaryDataFile(absoluteFilePath);
			(new File(absoluteFilePath)).delete();
			return data;
		}
		long timeStamp = (new Date()).getTime();
		String varName = "TrialsList_" + timeStamp;
		sendCommand(varName + "=" + fullCategoryName + TrialsList + "';"); 
//		int[] data = new int[0];
//		try {
//			double[] dataTemp = new double[0];
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", varName, "base");
//			Variant variant1 = new Variant(varName);
//			Variant variant2 = new Variant("base");
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", variant1, variant2);
//			if(dataTempObject instanceof Double)
//			dataTemp = new double[]{(Double) dataTempObject};
//			else if(dataTempObject instanceof double[]) dataTemp = (double[]) dataTempObject;
//			data = new int[dataTemp.length];
//			for (int i = 0; i < dataTemp.length; i++) data[i] = (int)dataTemp[i];
//		} catch (COMException e) {
//			logger.IlogErrorMessage(e);
//		}
		int[] data = getVariableAsIntArray(varName, "base");
		sendCommand("clear " + varName + ";");
		return data;
	}
	
	public String getCategoryNameFromTrial(String fullSubjectName, int trialNumber) {
		ArrayList<String> fieldNames  = new ArrayList<String>(0) ;
		fieldNames.addAll(Arrays.asList(getMatlabFieldsNames(fullSubjectName)));
		if(fieldNames.indexOf(Categories_No_Point) > -1) {
			fieldNames.clear();
			fieldNames.addAll(Arrays.asList(getMatlabFieldsNames(fullSubjectName+ Categories)));
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
		if(mainVersion < 7) {
			long timeStamp = (new Date()).getTime();
			String fileName = "data_" + timeStamp + ".bin";
			String varName = "TrialsMkrsGrp_" + timeStamp;
			sendCommand(varName + "=" + fullSignalName + Marker + markersGroupNumber + _Values + "(:,1)';");
			sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
			sendCommand("clear " + varName + ";");
			String absoluteFilePath = workspace + File.separator + fileName;
			int[] data = Utils.readIntegerBinaryDataFile(absoluteFilePath);
			(new File(absoluteFilePath)).delete();
			return data;
		}
		long timeStamp = (new Date()).getTime();
		String varName = "TrialsMkrsGrp_" + timeStamp;
		sendCommand(varName + "=" + fullSignalName + Marker + markersGroupNumber + _Values + "(:,1);");
//		int[] data = new int[0];
//		try {
//			double[] dataTemp = new double[0];
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", varName, "base");
//			Variant variant1 = new Variant(varName);
//			Variant variant2 = new Variant("base");
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", variant1, variant2);
//			if(dataTempObject instanceof Double)
//			dataTemp = new double[]{(Double) dataTempObject};
//			else if(dataTempObject instanceof double[]) dataTemp = (double[]) dataTempObject;
//			data = new int[dataTemp.length];
//			for (int i = 0; i < dataTemp.length; i++) data[i] = (int)dataTemp[i];
//		} catch (COMException e) {
//			logger.IlogErrorMessage(e);
//		}
		int[] data = getVariableAsIntArray(varName, "base");
		sendCommand("clear " + varName + ";");
		return data;
	}

	public double[] getValuesForTrialNumber(int trialNumber, String fullSignalName) {
		if(mainVersion < 7) {
			long timeStamp = (new Date()).getTime();
			String fileName = "data_" + timeStamp + ".bin";
			String varName = "Values_" + timeStamp;
			int frontCut = getFrontCut(fullSignalName, trialNumber);
			int endCut = getEndCut(fullSignalName, trialNumber);
			if(endCut == 0) return new double[0];
			sendCommand(varName + "=" + fullSignalName + Values + "(" + trialNumber +"," + frontCut + ":" + endCut + ")';");
			sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
			sendCommand("clear " + varName + ";");
			String absoluteFilePath = workspace + File.separator + fileName;
			double[] data = Utils.readDoubleBinaryDataFile(absoluteFilePath);
			(new File(absoluteFilePath)).delete();
			return data;
		}
		long timeStamp = (new Date()).getTime();
		String varName = "Values_" + timeStamp;
		int frontCut = getFrontCut(fullSignalName, trialNumber);
		int endCut = getEndCut(fullSignalName, trialNumber);
		if(endCut == 0) return new double[0];
		sendCommand(varName + "=" + fullSignalName + Values + "(" + trialNumber +"," + frontCut + ":" + endCut + ")';");
//		double[] data = new double[0];
//		try {
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", varName, "base");
//		Variant variant1 = new Variant(varName);
//		Variant variant2 = new Variant("base");
//		Object dataTempObject = matlabAutomationServer.invoke("GetVariable", variant1, variant2);
//			if(dataTempObject instanceof Double)
//			data = new double[]{(Double) dataTempObject};
//			else if(dataTempObject instanceof double[]) data = (double[]) dataTempObject;
//		} catch (COMException e) {
//			logger.IlogErrorMessage(e);
//		}
		double[] data = getVariableAsDoubleArray(varName, "base");
		sendCommand("clear " + varName + ";");
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
		if(mainVersion < 7) {
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
			
			cmd = varName + " = " + fullEventName + Values  + "(" + varName + ", [2:4])";
			sendCommand(cmd);
			cmd = varName + " = " + varName + "(:)";
			sendCommand(cmd);
			
			sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
			sendCommand("clear " + varName + ";");
			String absoluteFilePath = workspace + File.separator + fileName;
//			File dataFile = (new File(absoluteFilePath));
//			while (!dataFile.exists());
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
		long timeStamp = (new Date()).getTime();
		String varName = "Values_" + timeStamp;
		
		String cmd = varName + " = find(" + fullEventName + Values + "(:,1) == " + trialNumber + ");";
		sendCommand(cmd);
		cmd = varName + " = " + fullEventName + Values + "(" + varName + ", [2:4])";
		sendCommand(cmd);
		cmd = varName + " = " + varName + "(:)";
		sendCommand(cmd);
		
//		double[] dataTemp = new double[0];
//		try {
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", varName, "base");
//		Variant variant1 = new Variant(varName);
//		Variant variant2 = new Variant("base");
//		Object dataTempObject = matlabAutomationServer.invoke("GetVariable", variant1, variant2);
//			if(dataTempObject instanceof Double)
//			dataTemp = new double[]{(Double) dataTempObject};
//			else if(dataTempObject instanceof double[]) dataTemp = (double[]) dataTempObject;
//		} catch (COMException e) {
//			logger.IlogErrorMessage(e);
//		}
		double[] dataTemp = getVariableAsDoubleArray(varName, "base");
		sendCommand("clear " + varName + ";");
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
	
	public double getYValueForMarkersGroup(int markersGroupNumber, int arrayIndex, String fullSignalName) {
		double[] yValues = getYValuesForMarkersGroup(markersGroupNumber, fullSignalName);
		return yValues[arrayIndex];
	}

	public double[] getXValuesForMarkersGroup(int markersGroupNumber, String fullSignalName) {
		if(mainVersion < 7) {
			long timeStamp = (new Date()).getTime();
			String fileName = "data_" + timeStamp + ".bin";
			String varName = "XValues_" + timeStamp;
			sendCommand(varName + "=" + fullSignalName + Marker + markersGroupNumber + _Values + "(:,2)';");
			sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
			sendCommand("clear " + varName + ";");
			String absoluteFilePath = workspace + File.separator + fileName;
			double[] data = Utils.readDoubleBinaryDataFile(absoluteFilePath);
			(new File(absoluteFilePath)).delete();
			return data;
		}
		
		long timeStamp = (new Date()).getTime();
		String varName = "XValues_" + timeStamp;
		sendCommand(varName + "=" + fullSignalName + Marker + markersGroupNumber + _Values + "(:,2);");
//		double[] data = new double[0];
//		try {
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", varName, "base");
//		Variant variant1 = new Variant(varName);
//		Variant variant2 = new Variant("base");
//		Object dataTempObject = matlabAutomationServer.invoke("GetVariable", variant1, variant2);
//			if(dataTempObject instanceof Double)
//			data = new double[]{(Double) dataTempObject};
//			else if(dataTempObject instanceof double[]) data = (double[]) dataTempObject;
//		} catch (COMException e) {
//			logger.IlogErrorMessage(e);
//		}
		double[] data = getVariableAsDoubleArray(varName, "base");
		sendCommand("clear " + varName + ";");
		return data;
	}

	public double[] getYValuesForMarkersGroup(int markersGroupNumber, String fullSignalName) {
		if(mainVersion < 7) {
			long timeStamp = (new Date()).getTime();
			String fileName = "data_" + timeStamp + ".bin";
			String varName = "YValues_" + timeStamp;
			sendCommand(varName + "=" + fullSignalName + Marker + markersGroupNumber + _Values + "(:,3)';");
			sendCommand("writeBinaryDataVector('" + fileName +"'," + varName + ");");
			sendCommand("clear " + varName + ";");
			String absoluteFilePath = workspace + File.separator + fileName;
			double[] data = Utils.readDoubleBinaryDataFile(absoluteFilePath);
			(new File(absoluteFilePath)).delete();
			return data;
		}
		
		long timeStamp = (new Date()).getTime();
		String varName = "YValues_" + timeStamp;
		sendCommand(varName + "=" + fullSignalName + Marker + markersGroupNumber + _Values + "(:,3);");
//		double[] data = new double[0];
//		try {
//			Object dataTempObject = matlabAutomationServer.invoke("GetVariable", varName, "base");
//		Variant variant1 = new Variant(varName);
//		Variant variant2 = new Variant("base");
//		Object dataTempObject = matlabAutomationServer.invoke("GetVariable", variant1, variant2);
//			if(dataTempObject instanceof Double)
//			data = new double[]{(Double) dataTempObject};
//			else if(dataTempObject instanceof double[]) data = (double[]) dataTempObject;
//		} catch (COMException e) {
//			logger.IlogErrorMessage(e);
//		}
		double[] data = getVariableAsDoubleArray(varName, "base");
		sendCommand("clear " + varName + ";");
		return data;
	}

	public boolean loadSubject(String fullSubjectName, String experimentType, String[] dataFiles) {
		if(isStarted()) {
			if(dataFiles.length == 1 && dataFiles[0].endsWith(Subject.SAVING_FILE_NAME)) {
				logger.IlogMessage(Messages.getString("MathEngine.LoadingSubject") + fullSubjectName + Messages.getString("MathEngine.FromDataFile"));
				logger.IlogMessage(">> " + dataFiles[0]);
				String experimentName = fullSubjectName.split("\\.")[0];
				String subjectName = fullSubjectName.split("\\.")[1];
				String fileName = workspace + File.separator + experimentName + File.separator + subjectName + File.separator + Subject.SAVING_FILE_NAME;
				String cmd = "load '" + fileName + "';";
				sendCommand(cmd);
				cmd = experimentName + "." + subjectName + " = subjectName;";
				sendCommand(cmd);
				cmd = "clear subjectName;"; 
				sendCommand(cmd);
				ArrayList<String> fieldNames  = new ArrayList<String>(0) ;
				fieldNames.addAll(Arrays.asList(getMatlabFieldsNames(fullSubjectName)));
				if(fieldNames.indexOf(Categories_No_Point) == -1) {
					sendCommand("buildCategories('" + fullSubjectName + "');");
				}
			} else {
				logger.IlogMessage(Messages.getString("MathEngine.LoadingSubject") + fullSubjectName + Messages.getString("MathEngine.FromDataFile"));
				String[] subjectNameParts = fullSubjectName.split("\\.");
				String dataFilesAbsolutePathString = "";	
				for (int i = 0; i < dataFiles.length && i < 20; i++) logger.IlogMessage(">> " + dataFiles[i]);
				for (int i = 0; i < dataFiles.length; i++) dataFilesAbsolutePathString = dataFilesAbsolutePathString +  dataFiles[i]+  ",";	
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
				fieldNames.addAll(Arrays.asList(getMatlabFieldsNames(fullSubjectName)));
				if(fieldNames.indexOf(Categories_No_Point) == -1) {
					sendCommand("buildCategories('" + fullSubjectName + "');");
				}
			}
			boolean loaded = isSubjectLoaded(fullSubjectName);
			if(loaded) logger.IlogMessage(Messages.getString("MathEngine.Done"));
			else logger.IlogErrorMessage(Messages.getString("MathEngine.ErrorLoading") + fullSubjectName + "'");
			return loaded;
		}
		else logger.IlogErrorMessage(Messages.getString("MathEngine.NotStarted"));
		return false;
	}

	public boolean saveSubject(String fullSubjectName) {
		logger.IlogMessage(Messages.getString("MatlabController.SavingSubject") + fullSubjectName); 
		String experimentName = fullSubjectName.split("\\.")[0]; 
		String subjectName = fullSubjectName.split("\\.")[1]; 
		String cmd = "subjectName = " + fullSubjectName + ";"; 
		sendCommand(cmd);
		String fileName = workspace + File.separator + experimentName + File.separator + subjectName + File.separator + Subject.SAVING_FILE_NAME;
		if(mainVersion >= 7) cmd = "save -v6 '" + fileName + "' subjectName;";
		else cmd = "save '" + fileName + "' subjectName;";
		sendCommand(cmd);
		cmd = "clear subjectName;";
		sendCommand(cmd);
		return (new File(fileName)).exists();
	}

	public String sendCommand(String cmd) {
		if(isStarted())
			try {
				String response = matlabAutomationServer.invoke("Execute", cmd).getString();
				getLastErrorMessage();
				return response;
			} catch (Exception e) {
				getLastErrorMessage();
				logger.IlogErrorMessage(e);
			}
		else logger.IlogErrorMessage(Messages.getString("MathEngine.NotStarted"));
		return "";
	}

	public boolean start(final String[] pathsToAdd, final String workingDirectory) {
		//To be sure that starting is done in IHM thread (COM multi-threading)
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if(!isStarted())
//					try {
						logger.IlogMessage(Messages.getString("MathEngine.Starting"));
//						Ole32.CoInitialize();
//						ComThread.doCoUninitialize();
//						ComThread.InitSTA();
//						matlabAutomationServer_ThreadLocal = new DispatchPtr("matlab.application");
//						matlabAutomationServer = (DispatchPtr) matlabAutomationServer_ThreadLocal.createGITRef();
						matlabAutomationServer = new ActiveXComponent("matlab.application");
						matlabAutomationServer.setProperty("Visible",  false);
						started = true;
						logger.IlogMessage(Messages.getString("MathEngine.Done"));
						version = clearStringValue(sendCommand("version"));
						version = version.replaceAll("'", "");
						mainVersion = Integer.parseInt(version.split("\\.")[0]);
						logger.IlogMessage(Messages.getString("MathEngine.Version") + " Matlab " + version);
						logger.IlogMessage(Messages.getString("MathEngine.AddPath"));
						for (int i = 0; i < pathsToAdd.length; i++) {
							sendCommand("addpath('" + pathsToAdd[i] + "');");
							logger.IlogMessage(">> " + pathsToAdd[i]);
						}
						logger.IlogMessage(Messages.getString("MathEngine.ChangeWorkingDirectory"));
						logger.IlogMessage(">> " + workingDirectory);
						sendCommand("cd '" + workingDirectory + "';");
						workspace = workingDirectory;
//					} catch (COMException e) {
//						logger.IlogErrorMessage(e);
//					}
			}
		});
		return started;
	}

	public boolean stop() {
		if(isStarted()) {
//			try {
				logger.IlogMessage(Messages.getString("MathEngine.Stop"));
//				matlabAutomationServer.invoke("Quit");
				matlabAutomationServer.invoke("Quit");
//				matlabAutomationServer.close();
//				matlabAutomationServer_ThreadLocal.close();
//				Ole32.CoUninitialize();
//				ComThread.Release();
//				ComThread.quitMainSTA();
//				ComThread.doCoUninitialize();
				started = false;
				logger.IlogMessage(Messages.getString("MathEngine.Done"));
				return true;
//			} catch (COMException e) {
//				logger.IlogErrorMessage(e);
//			} 
		} else return !started;
	}

	public boolean unloadSubject(String fullSubjectName) {
		String experiment = fullSubjectName.split("\\.")[0];
		String subject = fullSubjectName.split("\\.")[1];
		logger.IlogMessage(Messages.getString("MathEngine.UnLoadingSubject") + fullSubjectName + "'");
		String cmdLine = experiment + " = rmfield(" + experiment + ",'" + subject + "');";
		sendCommand(cmdLine);
		boolean loaded = isSubjectLoaded(fullSubjectName);
		if(!loaded) logger.IlogMessage(Messages.getString("MathEngine.Done"));
		else logger.IlogErrorMessage(Messages.getString("MathEngine.ErrorUnLoading") + fullSubjectName + "'");
		return loaded;
	}
	
	public boolean isStarted() {
		return started;
	}

	public boolean isSubjectLoaded(String fullSubjectName) {
		String experiment = fullSubjectName.split("\\.")[0];
		if(exist(experiment))
		  if(isstruct(experiment)) {
			  String[] fieldsNames = getMatlabFieldsNames(experiment);
  			  String subject = fullSubjectName.split("\\.")[1];
			  for (int i = 0; i < fieldsNames.length; i++) if(fieldsNames[i].equals(subject)) return true;
		  }
		return false;
	}
	
	private String[] getMatlabFieldsNames(String structName) {
		String response = sendCommand("fieldnames(" + structName + ")");
		response = clearStringValue(response);
		response = response.replaceAll("^'", "");
		response = response.replaceAll("'$", "");
		return response.split("''");
	}
	
	public boolean isChannelExists(String fullSignalName) {
		String fullSubjectName = fullSignalName.replaceAll("\\.\\w+$", "");
		String channelName = fullSignalName.split("\\.")[2];
		String[] channelsNames = getChannelsNames(fullSubjectName);
		for (int i = 0; i < channelsNames.length; i++) if(channelName.equals(channelsNames[i])) return true;
		return false;
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

	private String clearStringValue(String response) {
		response = response.replaceAll("(\\n)*", "");
		response = response.replaceAll("logical", "");
		response = response.replaceAll("\\}", "");
		response = response.replaceAll("\\{", "");
		response = response.replaceAll("(\\s)*", "");
		response = response.replaceAll("^ans\\s*=\\s*", "");
		response = response.replaceAll("=", "");
		response = response.replaceAll("\\d+x\\d+cellarray", "");
		response = response.replaceAll("\\d+x\\d+emptychararray", "");
		response = response.replaceAll(">", "");
		return response;
	}

	public int getEndCut(String fullSignalName, int trialNumber) {
		String response = sendCommand(fullSignalName + EndCut + "(" + trialNumber + ")");
		response = clearStringValue(response);
		return Integer.parseInt(response);
	}

	public int getFrontCut(String fullSignalName, int trialNumber) {
		String response = sendCommand(fullSignalName + FrontCut + "(" + trialNumber + ")");
		response = clearStringValue(response);
		int value = Integer.parseInt(response);
		if(value == 0) value++;
		return value;
	}

	public int getNbSamples(String fullSignalName, int trialNumber) {
		String response = sendCommand(fullSignalName + NbSamples + "(" + trialNumber + ")");
		response = clearStringValue(response);
		return Integer.parseInt(response);
	}

	public String getLastErrorMessage() {
		if(isStarted())
			try {
				long timeStamp = (new Date()).getTime();
				String varName = "Error_" + timeStamp;
				String cmd = varName + "=lasterror;";
//				matlabAutomationServer.invoke("Execute", cmd);
				matlabAutomationServer.invoke("Execute", cmd);
//				String response = (String) matlabAutomationServer.invoke("Execute", "isstruct(" + varName + ")");
				String response = matlabAutomationServer.invoke("Execute", "isstruct(" + varName + ")").getString();
				response = clearStringValue(response);
				if(response.equals("1")) {
					cmd = varName + ".message";
//					String message = (String) matlabAutomationServer.invoke("Execute", cmd);
					String message = matlabAutomationServer.invoke("Execute", cmd).getString();
					message = message.replaceAll("\\n", "");
					message = message.replaceAll("^ans(\\s)*=(\\s)*", "");
					message = clearStringValue(message);
					message = message.replaceAll("''", "");
					if(!message.equals("")) logger.IlogErrorMessage(message);
//					matlabAutomationServer.invoke("Execute", "clear " + varName + ";");
					matlabAutomationServer.invoke("Execute","clear " + varName + ";");
					if(mainVersion < 7)
//						matlabAutomationServer.invoke("Execute", "lasterr('');");
						matlabAutomationServer.invoke("Execute", "lasterr('');");
					else //matlabAutomationServer.invoke("Execute", "lasterror('reset');");
						matlabAutomationServer.invoke("Execute","lasterror('reset');");
					return message;
				}
			} catch (Exception e) {
				logger.IlogErrorMessage(e);
			}
		else logger.IlogErrorMessage(Messages.getString("MathEngine.NotStarted"));
		return "";
	}

	public boolean deleteChannel(String fullSubjectName, String channelName) {
		String cmdLine = fullSubjectName + " = rmfield(" + fullSubjectName + ",'" + channelName + "');";
		sendCommand(cmdLine);
		String[] fieldsNames = getMatlabFieldsNames(fullSubjectName);
		for (int i = 0; i < fieldsNames.length; i++) if(fieldsNames[i].equals(channelName)) return false;
		return true;
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
		String[] subjectsNames = getMatlabFieldsNames(subjectName);
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
		String[] subjectsNames = getMatlabFieldsNames(experimentName);
		for (int i = 0; i < subjectsNames.length; i++) {
			if(subjectsNames[i].equals(newSubjectName)) newNameFound = true;
			if(subjectsNames[i].equals(oldSubjectName)) oldNameFound = true;
		}
		
		return newNameFound && !oldNameFound;
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
				createdSignalsNames = createdSignalsNames.replaceAll("^'", "");	
				createdSignalsNames = createdSignalsNames.replaceAll("'$", "");	
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
				createdMarkersNames = createdMarkersNames.replaceAll("^'", "");	
				createdMarkersNames = createdMarkersNames.replaceAll("'$", "");	
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
				createdFieldsNames = createdFieldsNames.replaceAll("^'", "");	
				createdFieldsNames = createdFieldsNames.replaceAll("'$", "");	
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
				modifiedSignalsNames = modifiedSignalsNames.replaceAll("^'", "");	
				modifiedSignalsNames = modifiedSignalsNames.replaceAll("'$", "");	
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

	/*
	 * 
	 * 
	 * String cmdLine = experimentName + " = " + "rnfield(" + experimentName + ",'" + oldSubjectName +"','" + newSubjectName + "');"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			sendCmdLineToMatlab(cmdLine,true,true);
	 * 
	 * 
	 * 
	 */
	
//	public static void main(String[] args) {
//		try {
//			//Initialize COM and create a Dispatcher instance for application
//			Ole32.CoInitialize();
//			DispatchPtr matlabAutomationServer = new DispatchPtr("matlab.application");
//			 matlabAutomationServer.put("Visible", true);
//			 String value = (String) matlabAutomationServer.invoke("Execute", "chararray='hello';");
//			 if(value.equals("")) System.out.println("empty");
//			 
//			 matlabAutomationServer.invoke("Execute", "a = [1 2 3 4; 5 6 7 8];");
//			 Double[] a = new Double[4];
//			 Double[] y= new Double[0];
//			 matlabAutomationServer.invoke("GetFullMatrix", "a", "base", a, y); 
//			 
//			 //??????
//			 value = (String) matlabAutomationServer.invoke("GetCharArray", "chararray", "base");
//			 System.out.println(value);
////			 SAFEARRAY real = new SAFEARRAY();
////			 SAFEARRAY imag = new SAFEARRAY();
////			 matlabAutomationServer.invoke("GetFullMatrix", "b", "base",real,imag);
////			 System.out.println(real.cDims);
////			 System.out.println(real.cbElements);
////			 long t = System.currentTimeMillis();
////			 double[] x = (double[]) matlabAutomationServer.invoke("GetVariable", "a", "base");
////				System.out.println(x.length);	
////				for (int i = 0; i < x.length; i++) {
////					System.out.println(x[i]);
////				}
////				t=System.currentTimeMillis() - t;
////				System.out.println(t);
//				
//			 
////			String S = "Just a string";
////			matlabAutomationServer.invoke("PutWorkspaceData","S","base",S);
////			System.out.println(matlabAutomationServer.invoke("GetVariable","S", "base"));
////			double[] d = new double[1000];
////			matlabAutomationServer.invoke("PutWorkspaceData","P.S.C.V","base",d);
////			System.out.println(matlabAutomationServer.invoke("GetVariable","P.S.C.V", "base"));
////			app.put("Visible", true);
////			Thread.sleep(5000);
//			
//			//Visible property
//			
//			
//			//Execute method
//			
//			
////			matlabAutomationServer.invoke("PutWorkspaceData","x","base",2.25);
////			double x = (Double) matlabAutomationServer.invoke("GetVariable", "x", "base");
////			System.out.println(x);
////			
////			double[][] B = new double[][]{{1,2},{3,4}};
////			matlabAutomationServer.invoke("PutWorkspaceData","B","base",B);
////			
////			double[] C = new double[]{3,4};
////			matlabAutomationServer.invoke("PutWorkspaceData","C","base",C);
//			
////			
//			
//			
////			String S = "Just a string";
////			matlabAutomationServer.invoke("PutWorkspaceData","S","base",S);
//			
////			double y = 0 ; 
////			matlabAutomationServer.invoke("GetWorkspaceData", "x", "base",y);
////			System.out.println(y);
//			
////			Double[] real = new Double[]{new Double(1),new Double(1),new Double(1),new Double(1)};
////			Double[] imag = new Double[]{new Double(1),new Double(1),new Double(1),new Double(1)};
////			matlabAutomationServer.invoke("PutFullMatrix", "E", "base", real, imag);
//			
////			value = (String) matlabAutomationServer.invoke("Execute", "whos");
//			System.out.println(value);
//			
//			Thread.sleep(1000);
//			matlabAutomationServer.invoke("Quit");
//			Ole32.CoUninitialize();
//		} catch (COMException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//	}

	

}
