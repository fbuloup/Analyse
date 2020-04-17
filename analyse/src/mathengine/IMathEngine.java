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

public interface IMathEngine {
	
	String isSignal = ".isSignal";
	String SampleFrequence = ".SampleFrequency";
	String Values = ".Values";
	String FrontCut = ".FrontCut";
	String EndCut = ".EndCut";
	String NbSamples = ".NbSamples";

	String NbFields = ".NbFields";
	String Field = ".Field";
	String Field_No_Point = "Field";

	String NbMarkers = ".NbMarkers";
	String Marker = ".Marker";
	String Marker_No_Point = "Marker";
	
	String _Label = "_Label";
	String _Values = "_Values";
	String _Symbol = "_Symbol";
	
	String isCategory = ".isCategory";
	String Criteria = ".Criteria";
	String TrialsList = ".TrialsList";
	
	String isEvent = ".isEvent";
	
	String Categories = ".Categories";
	String Names = ".Names";
	String Categories_No_Point = "Categories";
	String Names_No_Point = "Names";
	
	boolean start(String[] pathToAdd, String workingDirectory);
	boolean stop();
	
	String getLastErrorMessage();
	
	boolean isStarted();
	
	boolean loadSubject(String fullSubjectName, String experimentType,String[] dataFiles);
	boolean unloadSubject(String fullSubjectName);
	boolean saveSubject(String fullSubjectName);
	boolean isSubjectLoaded(String fullSubjectName);
	
	String[] getChannelsNames(String fullSubjectName);
	boolean deleteChannel(String fullSubjectName, String channelName);
	boolean renameChannel(String subjectName, String oldChannelName, String newChannelName);
	
	String[] getSignalsNames(String fullSubjectName);
	boolean isSignal(String fullSignalName);
	
	String[] getCategoriesNames(String fullSubjectName);
	boolean createNewCategory(String fullSubjectName, String categoryName, String criteria, String trialsList);
	int[] getTrialsListForCategory(String fullCategoryName);
	String getCriteriaForCategory(String fullCategoryName);
//	boolean doesCategoryContainsTrial(String fullCategoryName, int trialNumber);
	String getCategoryNameFromTrial(String fullSubjectName, int trialNumber);
	boolean isCategory(String fullCategoryName);
	
	String[] getEventsGroupsNames(String fullSubjectName);
	boolean isEventsGroup(String fullEventName);
	
	double getSampleFrequency(String fullSignalName);
	void setSampleFrequency(String fullSignalName, double sf);
	
	
	int getNbMarkersGroups(String fullSignalName);
	String getMarkersGroupLabel(int markersGroupNumber, String fullSignalName);
	String getMarkersGroupGraphicalSymbol(int markersGroupNumber, String fullSignalName);
	void setMarkersGroupGraphicalSymbol(int markersGroupNumber, String fullSignalName, String symbolName);
	String[] getMarkersGroupsLabels(String fullSignalName);
	int[] getTrialsListForMarkersGroup(int markersGroupNumber, String fullSignalName);
	double[] getXValuesForMarkersGroup(int markersGroupNumber, String fullSignalName);
	double[] getYValuesForMarkersGroup(int markersGroupNumber, String fullSignalName);
	double getXValueForMarkersGroup(int markersGroupNumber, int arrayIndex, String fullSignalName);
	double getYValueForMarkersGroup(int markersGroupNumber, int arrayIndex, String fullSignalName);
	boolean deleteMarkersGroup(int markersGroupNumber, String fullSignalName);
	boolean deleteMarker(int markersGroupNumber, int lineNumber, String fullSignalName);
	boolean createNewMarkersGroup(String markersGroupLabel, String fullSignalName);
	boolean addMarker(int markersGroupNumber, int trialNumber, double XValue, double YValue, String fullSignalName);
	
	int getNbFields(String fullSignalName);
	String getFieldLabel(int fieldNumber, String fullSignalName);
	String[] getFieldsLabels(String fullSignalName);
	double[] getFieldValues(int fieldNumber, String fullSignalName);
	double getFieldValue(int fieldNumber, int trialNumber, String fullSignalName);
	boolean deleteField(int fieldNumber, String fullSignalName);
	
	int getNbTrials(String fullSignalName);
	int[] getNbSamples(String fullSignalName);
	int getNbSamples(String fullSignalName, int trialNumber);
	int[] getEndCut(String fullSignalName);
	int getEndCut(String fullSignalName, int trialNumber);
	int[] getFrontCut(String fullSignalName);
	int getFrontCut(String fullSignalName, int trialNumber);
	double[] getValuesForTrialNumber(int trialNumber, String fullSignalName);
	
	String getCriteriaForEventsGroup(String fullEventName);
	int[] getEventsGroupTrialsList(String fullEventName);
	int[] getEventsGroupUnduplicatedTrialsList(String fullEventName);
	double[][] getEventsGroupValues(String fullEventName);
	double[][] getEventsGroupValuesForTrialNumber(int trialNumber, String fullEventName);
	boolean deleteEvent(int lineNumber, String fullEventName);
	
	boolean renameExperiment(String oldName, String newName);
	boolean renameSubject(String experimentName, String oldSubjectName, String newSubjectName);
	
	String sendCommand(String cmd);
	boolean isChannelExists(String fullSignalName);
	
	void rehash();
	
	boolean runProcessing();
	String[] getProcessingErrors();
	String[] getNewSignalsNames();
	String[] getNewMarkersNames();
	String[] getNewFieldsNames();
	String[] getModifiedSignalsNames();

}
