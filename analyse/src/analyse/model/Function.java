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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import analyse.Log;
import analyse.preferences.LibraryPreferences;
import analyse.resources.CPlatform;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class Function implements Serializable {

	private static final long serialVersionUID = 1L;
	
	transient public static final String GUI_MATLAB_TYPE_INTEGER = "integer"; //$NON-NLS-1$
	transient public static final String GUI_MATLAB_TYPE_FLOAT = "float"; //$NON-NLS-1$
	transient public static final String GUI_MATLAB_TYPE_STRING = "string"; //$NON-NLS-1$
		
	/**
	 * The GUI component will be a simple text field
	 */
	transient public final static int GUI_TYPE_TEXT = 1;
	transient public final static String GUI_TYPE_TEXT_LABEL = "text";  //$NON-NLS-1$
	/**
	 * The GUI component will be a check box
	 */
	transient public final static int GUI_TYPE_CHECK_BUTTON = 2;
	transient public final static String GUI_TYPE_CHECK_BUTTON_LABEL = "checkBox";  //$NON-NLS-1$
	/**
	 * The GUI component will be a combobox
	 */
	transient public final static int GUI_TYPE_COMBOBOX = 3;
	transient public final static String GUI_TYPE_COMBOBOX_LABEL = "combobox"; //$NON-NLS-1$
	/**  
	* The GUI component will be a list
	 */
	transient public final static int GUI_TYPE_LIST = 4;
	transient public final static String GUI_TYPE_LIST_LABEL = "list";  //$NON-NLS-1$
		
	transient public static String TOOL_TIP_TRIALS = "1 3 5:10";  //$NON-NLS-1$
		
	private String trialsList = ""; //$NON-NLS-1$
	/*
	 * These lists holds the n-uplets channels :
	 *  n = 0 -> P.S1.S1,P.S1.S2
	 *  n = 1 -> P.S2.S1,P.S2.S2 etc.
	 *  You can form matlab argument by concatening these uplets with ":" char.
	 */
	private List<String> signalsNamesList = new ArrayList<String>(0);
	private List<String> markersNamesList = new ArrayList<String>(0);
	private List<String> fieldsNamesList = new ArrayList<String>(0);
	/*
	 * These lists holds the suffix or labels for created channels for each outputs uplets:
	 *  n = 0 -> magFFT,phaseFFT:mag2FFT,phase2FFT
	 */
	private String newSignalsNamesSuffix = "";	 //$NON-NLS-1$
	private String newMarkersGroupsLabels = ""; //$NON-NLS-1$
	private String newFieldsNamesLabels = ""; //$NON-NLS-1$
	
	private List<String> parametersListLabel = new ArrayList<String>(0);
	private List<Integer> parametersListComponent = new ArrayList<Integer>(0);
	private List<Object> parametersDefaultValue = new ArrayList<Object>(0);
	private List<String[]> availableListValues = new ArrayList<String[]>(0);
	private List<String> parametersListToolTip = new ArrayList<String>(0);
	private List<String> parametersMatlabType = new ArrayList<String>(0);
	private List<String> parametersRegExp = new ArrayList<String>(0);
	private List<Boolean> signalsAvailableBool = new ArrayList<Boolean>(0);
	private List<Boolean> markersAvailableBool = new ArrayList<Boolean>(0);
	private List<Boolean> fieldAvailableBool = new ArrayList<Boolean>(0);
	
	
	transient private String matlabComments = ""; //$NON-NLS-1$
	transient private String matlabFunctionBody = ""; //$NON-NLS-1$
	transient private String matlabFunctionPreBody = ""; //$NON-NLS-1$
	transient private String matlabFunctionPostBody = ""; //$NON-NLS-1$

	transient private boolean isDirty = false;
	
	private String longDescription;
	private String shortDescription;
	
	private String matlabFunctionName;
	transient private String oldMatlabFunctionName;
	
	private String GUIFunctionName;
	private int signalsUsedNumber = 0;
	private int markersUsedNumber = 0;
	private int fieldsUsedNumber = 0;
	private int signalsCreatedNumber = 0;
	private int markersCreatedNumber = 0;
	private int fieldsCreatedNumber = 0;
	private int signalsModifiedNumber = 0;
	
	transient private HashSet<IFunctionObserver> functionObservers;

	private String matlabPathFile;

	private boolean error;

	private boolean warning;
	
	public Function() {
		this.matlabPathFile = "./matlabscripts/functionTemplate.m"; //$NON-NLS-1$
		this.matlabFunctionName = "functionTemplate";
		oldMatlabFunctionName = matlabFunctionName;
	}
	
	public Function(String matlabFunctionName) {
		this.matlabPathFile = LibraryPreferences.getRootDirectory() + matlabFunctionName + ".m"; //$NON-NLS-1$
		this.matlabFunctionName = matlabFunctionName;
		oldMatlabFunctionName = matlabFunctionName;
		functionObservers = new HashSet<IFunctionObserver>(0);
	}

	public void addFunctionObservers(IFunctionObserver functionObserver) {
		if(functionObservers == null) functionObservers = new HashSet<IFunctionObserver>(0);
		functionObservers.add(functionObserver);				
	}
	
	/**
	 * This method prints informations to the console 
	 */
	public void printInfos() {
		System.out.println(longDescription);
		System.out.println(shortDescription);
		System.out.println(matlabFunctionName);
		System.out.println(GUIFunctionName);
		System.out.println(signalsUsedNumber);
		System.out.println(markersUsedNumber);
		System.out.println(fieldsUsedNumber);
		System.out.println(signalsCreatedNumber);
		System.out.println(markersCreatedNumber);
		System.out.println(fieldsCreatedNumber);
		System.out.println(signalsModifiedNumber);
		for (int i = 0; i < parametersListComponent.size(); i++) {
			System.out.println(Messages.getString("Function.PrintInfosLabelLabel") + parametersListLabel.get(i) + Messages.getString("Function.PrintInfosTypeLabel") + parametersListComponent.get(i) + Messages.getString("Function.PrintInfosValueLabel") + parametersDefaultValue.get(i)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	/**
	 * This method adds a new feature to this function library that will be displayed as a user 
	 * interface component in analyse front end. It takes several parameters. The type parameter 
	 * determines directly the kind of component interface that will be used to display the feature. 
	 * You have the choice between the static types TYPE_**** of the Library class : text, 
	 * check or radio button and combobox. The label is a short description of this feature while
	 * the default value parameter is the initial value of the feature, which might be a literal string or
	 * a boolean. The available values are only necessary if the type of the component is COMBOBOX, 
	 * these values will be then presented to the user as a list of options, the default value representing
	 * the default selected option
	 * The toolTipText parameter is a hint for the user. 
	 * @param label , for instance : "Enter the trial list"
	 * @param value , for instance : "1,3,5,10..20" or true
	 * @param type , for instance : Library.TYPE_TEXT
	 * @param availableValues , for instance  : new String[]{"Butterworth","Bessel","Chebytchev"} or possibly null
	 * @param toolTipText , just a value example that can be entered in the component or possibly null 
	 */	
	protected void addGUIFeature(String matlabType, int component, String label, String toolTipText, String regExp, String[] availableValues, Object defaultValue, boolean signalsAvailable, boolean markersAvailable, boolean fieldsAvailable) {
		parametersMatlabType.add(matlabType);
		parametersListComponent.add(component);
		parametersListLabel.add(label);
		parametersListToolTip.add(toolTipText);
		parametersRegExp.add(regExp);
		parametersDefaultValue.add(defaultValue);
		if((availableValues.length == 1) && (availableValues[0].equals(""))) //$NON-NLS-1$
			availableListValues.add(null);
		else availableListValues.add(availableValues);
		signalsAvailableBool.add(signalsAvailable);
		markersAvailableBool.add(markersAvailable);
		fieldAvailableBool.add(fieldsAvailable);
	}
	
	
	private String[] readFile() throws IOException
	  {
	    BufferedReader bufferedReader = null;
	    String line;
	    String[] lines;
	    Vector<String> text = new Vector<String>();
	    boolean beginReplace = false;
	    boolean endReplace = false;
	    
	    try {
	    	bufferedReader = new BufferedReader(new FileReader(matlabPathFile));
	    	while ((line = bufferedReader.readLine()) != null) {
	    		if (line.equals("%beginAnalyseHeader")) beginReplace = true; //$NON-NLS-1$
		    	if (line.equals("%endAnalyseHeader")){ //$NON-NLS-1$
		    		endReplace = true;
		    		line = line.replaceAll("%\\s*", ""); //$NON-NLS-1$ //$NON-NLS-2$
		    	}
		    	if (!endReplace && beginReplace){
		    		line = line.replaceAll("%\\s*", ""); //$NON-NLS-1$ //$NON-NLS-2$
		    	}
		    	text.add(line);
		    }		    
		    bufferedReader.close();		    
		    lines = new String[text.toArray().length];		    
		    lines =  text.toArray(lines);
		    
		    return lines;
	    }
	    catch(FileNotFoundException exc) {
	    	Log.logErrorMessage(Messages.getString("Function.ConsoleErrorOpeningLabel") + matlabFunctionName); //$NON-NLS-1$
	    }
	    
	    return new String[]{};
	}
	
	private void parseFile(){
		try {
			String[] lines = readFile();
			int i=0;
			int k=1;
			int j=0;
			int component=0;
			boolean beginAnalyseHeaderDetected = false;
			boolean parameterDetected = false;
			boolean functionDetected = false;
			String[] parameterObjectValue = new String[10];
			String[] line;
			String[] parameterObjectAvailableListValue;
			
			if(lines.length > 0) {
				while (!"endAnalyseHeader".equals(lines[i])) {					 //$NON-NLS-1$
					if (!beginAnalyseHeaderDetected) {
						
						if(lines[i].startsWith("function")) functionDetected = true; //$NON-NLS-1$
						if (!functionDetected && !lines[i].equals("")) {
							lines[i] = lines[i].replaceAll("^(%)*",""); //$NON-NLS-1$ //$NON-NLS-2$
							matlabComments = matlabComments + lines[i] + CPlatform.getEOLCharacter();
//							if(CPlatform.isLinux())
//								matlabComments = matlabComments + lines[i] + "\n"; //$NON-NLS-1$
//							if(CPlatform.isMacOSX())
//								matlabComments = matlabComments + lines[i] + "\r"; //$NON-NLS-1$
//							if(CPlatform.isWindows())
//								matlabComments = matlabComments + lines[i] + "\r\n"; //$NON-NLS-1$
						}
						beginAnalyseHeaderDetected = lines[i].equals("beginAnalyseHeader"); //$NON-NLS-1$
						
					} else {
						
						parameterDetected = lines[i].startsWith("param" + k); //$NON-NLS-1$
						
						if (!parameterDetected) {
							line = lines[i].split("s*//"); //$NON-NLS-1$
							line = line[0].split("=\\s*"); //$NON-NLS-1$
							GUIFunctionName = line[1];
							line = lines[i+1].split("s*//"); //$NON-NLS-1$
							line = line[0].split("=\\s*"); //$NON-NLS-1$
							matlabFunctionName = line[1];
							line = lines[i+2].split("=\\s*"); //$NON-NLS-1$
							setShortDescription(line[1]);
							line = lines[i+3].split("=\\s*"); //$NON-NLS-1$
							setLongDescription(line[1]);
							line = lines[i+4].split("s*//"); //$NON-NLS-1$
							line = line[0].split("=\\s*"); //$NON-NLS-1$
							line[1] = line[1].replaceAll("\\s*", ""); //$NON-NLS-1$ //$NON-NLS-2$
							signalsUsedNumber = Integer.parseInt(line[1]);
							line = lines[i+5].split("s*//"); //$NON-NLS-1$
							line = line[0].split("=\\s*"); //$NON-NLS-1$
							line[1] = line[1].replaceAll("\\s*", ""); //$NON-NLS-1$ //$NON-NLS-2$
							markersUsedNumber = Integer.parseInt(line[1]);
							line = lines[i+6].split("s*//"); //$NON-NLS-1$
							line = line[0].split("=\\s*"); //$NON-NLS-1$
							line[1] = line[1].replaceAll("\\s*", ""); //$NON-NLS-1$ //$NON-NLS-2$
							fieldsUsedNumber = Integer.parseInt(line[1]);
							line = lines[i+7].split("s*//"); //$NON-NLS-1$
							line = line[0].split("=\\s*"); //$NON-NLS-1$
							line[1] = line[1].replaceAll("\\s*", ""); //$NON-NLS-1$ //$NON-NLS-2$
							signalsCreatedNumber = Integer.parseInt(line[1]);
							line = lines[i+8].split("s*//"); //$NON-NLS-1$
							line = line[0].split("=\\s*"); //$NON-NLS-1$
							line[1] = line[1].replaceAll("\\s*", ""); //$NON-NLS-1$ //$NON-NLS-2$
							markersCreatedNumber = Integer.parseInt(line[1]);
							line = lines[i+9].split("s*//"); //$NON-NLS-1$
							line = line[0].split("=\\s*"); //$NON-NLS-1$
							line[1] = line[1].replaceAll("\\s*", ""); //$NON-NLS-1$ //$NON-NLS-2$
							fieldsCreatedNumber = Integer.parseInt(line[1]);
							line = lines[i+10].split("s*//"); //$NON-NLS-1$
							line = line[0].split("=\\s*"); //$NON-NLS-1$
							line[1] = line[1].replaceAll("\\s*", ""); //$NON-NLS-1$ //$NON-NLS-2$
							signalsModifiedNumber = Integer.parseInt(line[1]);
							i=i+10;
							
							initialize();
							
						} else {
							if (lines[i].startsWith("param"+k)){ //$NON-NLS-1$
								line = lines[i].split("\\s*//"); //$NON-NLS-1$
								line = line[0].split("=\\s*"); //$NON-NLS-1$
								if (line.length>1) parameterObjectValue[j] = line[1];
								else parameterObjectValue[j] = ""; //$NON-NLS-1$
								j++;
								if (j == 10){
									component = Integer.parseInt(parameterObjectValue[1]);
									parameterObjectAvailableListValue = parameterObjectValue[5].split(":"); //$NON-NLS-1$
									addGUIFeature(parameterObjectValue[0], component, parameterObjectValue[2], parameterObjectValue[3], parameterObjectValue[4], parameterObjectAvailableListValue, parameterObjectValue[6], Boolean.parseBoolean(parameterObjectValue[7]), Boolean.parseBoolean(parameterObjectValue[8]), Boolean.parseBoolean(parameterObjectValue[9]));
									k++;
									j=0;
								}
							}
						}
					}
					i++;
				}
				i++;
				boolean beginFunctionDetected = false;
				boolean endFunctionDetected = false;
				while(lines.length > i){					
					if(lines[i].equals("%endFunction")) endFunctionDetected = true; //$NON-NLS-1$
					if(!beginFunctionDetected) matlabFunctionPreBody = matlabFunctionPreBody  + lines[i] + "\n"; //$NON-NLS-1$
					if(beginFunctionDetected && !endFunctionDetected)	matlabFunctionBody = matlabFunctionBody  + lines[i] + "\n"; //$NON-NLS-1$
					if(beginFunctionDetected && endFunctionDetected) matlabFunctionPostBody = matlabFunctionPostBody  + lines[i] + "\n"; //$NON-NLS-1$
					if(lines[i].equals("%beginFunction")) beginFunctionDetected = true; //$NON-NLS-1$
					i++;
				}
			}
			
			
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}	
	}
	
	private void initialize() {
		for(int i = 0; i < signalsUsedNumber; i++) signalsNamesList.add(""); //$NON-NLS-1$
		for(int i = 0; i < markersUsedNumber; i++) markersNamesList.add(""); //$NON-NLS-1$
		for(int i = 0; i < fieldsUsedNumber; i++) fieldsNamesList.add(""); //$NON-NLS-1$
		newSignalsNamesSuffix = "";		 //$NON-NLS-1$
		newMarkersGroupsLabels = ""; //$NON-NLS-1$
		newFieldsNamesLabels = ""; //$NON-NLS-1$
	}

	public void initializeFunction() {
		parseFile();	
		//printInfos();
	}	
	
	public String getTrialsList() {
		return trialsList == null ? "" : trialsList; //$NON-NLS-1$
	}
	
	public String getShortDescription(){
		return shortDescription;
	}
	
	public String getLongDescription(){
		return longDescription;
	}

	public int getSignalsUsedNumber() {
		return signalsUsedNumber;
	}

	public String getMatlabComments() {
		return matlabComments;
	}

	public String getMatlabFunctionBody() {	
		return matlabFunctionBody;
	}
	
	public void setMatlabFunctionPreBody(String body) {
		matlabFunctionPreBody = body;
	}
	
	public String getMatlabFunctionPreBody() {
		return matlabFunctionPreBody;
	}
	
	public void setMatlabFunctionPostBody(String body) {
		matlabFunctionPostBody = body;
	}
	
	public String getMatlabFunctionPostBody() {
		return matlabFunctionPostBody;
	}
	
	public int getMarkersUsedNumber() {
		return markersUsedNumber;
	}

	public int getFieldsUsedNumber() {
		return fieldsUsedNumber;
	}

	public int getSignalsCreatedNumber() {
		return signalsCreatedNumber;
	}

	public int getMarkersCreatedNumber() {
		return markersCreatedNumber;
	}

	public int getFieldsCreatedNumber() {
		return fieldsCreatedNumber;
	}

	public int getSignalsModifiedNumber() {
		return signalsModifiedNumber;
	}
	
	public String getMatlabFunctionName() {
		return matlabFunctionName;
	}
	
	public String getOldMatlabFunctionName() {
		return oldMatlabFunctionName;
	}

	public String getGUIFunctionName() {
		return GUIFunctionName;
	}

	/**
	 * 
	 * @return the total number of parameters
	 */
	public int getParametersCount() {
		return parametersListLabel.size();
	}
	
	public void createParameter() {
		addGUIFeature(GUI_MATLAB_TYPE_INTEGER, GUI_TYPE_TEXT, "ParameterName", "tooltip", "", new String[]{""}, "", false, false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	public void removeParameter(int i) {
		parametersMatlabType.remove(i);
		parametersListComponent.remove(i);
		parametersListLabel.remove(i);
		parametersListToolTip.remove(i);
		parametersRegExp.remove(i);
		parametersDefaultValue.remove(i);
		availableListValues.remove(i);
		signalsAvailableBool.remove(i);
		markersAvailableBool.remove(i);
		fieldAvailableBool.remove(i);
		isDirty = true;
		notifyFunctionObservers();		
	}	

	public String getParametersMatlabType(int parameterIndex) {
		return parametersMatlabType.get(parameterIndex);
	}

	public String getParametersRegExp(int parameterIndex) {
		return parametersRegExp.get(parameterIndex);
	}

	public Boolean getSignalsAvailableBool(int parameterIndex) {
		return signalsAvailableBool.get(parameterIndex);
	}

	public Boolean getMarkersAvailableBool(int parameterIndex) {
		return markersAvailableBool.get(parameterIndex);
	}

	public Boolean getFieldAvailableBool(int parameterIndex) {
		return fieldAvailableBool.get(parameterIndex);
	}
		
	/**
	 * Return the type of the parameter number "parameterIndex", beginning at 0. 
	 * Example : functionInstance.getParameterType(0)
	 * @param parameterNumber
	 * @return the type of the parameter
	 */
	public int getParameterType(int parameterIndex) {
		return parametersListComponent.get(parameterIndex);
	}
	
	/**
	 * Return the label of the parameter number "parameterIndex", beginning at 1. 
	 * Example : functionInstance.getParameterLabel(1).
	 * The label is the very short sentance that describe the parameter. For instance : 
	 * Select the kind of filter
	 * Selected the input channel
	 * Enter the trials list
	 * @param parameterIndex
	 * @return the label of the parameter
	 */
	public String getParameterLabel(int parameterIndex) {
		return parametersListLabel.get(parameterIndex);
	}
	
	/**
	 * Return the value of the parameter number "parameterIndex", beginning at 1. 
	 * Example : functionInstance.getParameterValue(1)
	 * @param parameterIndex
	 * @return the value of the parameter
	 */
	public Object getParameterValue(int parameterIndex) {
		return parametersDefaultValue.get(parameterIndex);
	}
	
	/**
	 * Return the value  list of the parameter number "parameterIndex", beginning at 1. 
	 * Used when the parameter GUI is a combobox.
	 * Example : functionInstance.getAvailablesValues(1)
	 * @param parameterIndex
	 * @return the list availabels values
	 */
	public String[] getAvailablesValues(int parameterIndex) {
		return availableListValues.get(parameterIndex);
	}
	
	/**
	 * Return the tooltip of the parameter number "parameterIndex", beginning at 1. 
	 * Example : functionInstance.getToolTip(1)
	 * @param parameterIndex
	 * @return the tooltip value
	 */
	public String getToolTip(int parameterIndex) {
		return parametersListToolTip.get(parameterIndex);
	}
	
	public void setTrialsList(String trialsList) {
		this.trialsList = trialsList;
		notifyFunctionObservers();
	}
	
	/**
	 * The inherited class <b>MUST</b> implements this method to return the full description of
	 * the function. This description will be displayed to the user. It is possible to use xhtml
	 * tags to structure the text. Examples :
	 * <p>paragraph (p tag)</p> 
	 * <b>bold (b tag)</b><br/>
	 * <i>italic (i tag)</i><br/>
	 * list (ul and li tags):
	 * <ul>
	 * <li>Coffee</li>
	 * <li>Milk</li>
	 * </ul>
	 * etc..
	 */
	public void setShortDescription(String shortDescription){
		this.shortDescription = shortDescription;
		isDirty = true;
		notifyFunctionObservers();
		LibraryPreferences.updateShortDescription(getMatlabFunctionName(),shortDescription);
	}
	
	public void setLongDescription(String longDescription){
		this.longDescription = longDescription;
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setMatlabComments(String comments) {
		//comments = "%" + comments; //$NON-NLS-1$
		comments = comments.replaceAll("\\r\\n","\n"); //$NON-NLS-1$ //$NON-NLS-2$
		comments = comments.replaceAll("\\r","\n"); //$NON-NLS-1$ //$NON-NLS-2$
		String[] commentsArray = comments.split("\n");
		comments = "";
		for (int i = 0; i < commentsArray.length; i++) {
			if(!commentsArray[i].equals("")) comments = comments + (i==0?"%":"\n%") + commentsArray[i];
		}
//		comments = comments.replaceAll("\\n","\n%"); //$NON-NLS-1$ //$NON-NLS-2$
//		if(CPlatform.isLinux() || CPlatform.isWindows()) {
//			comments = comments.replaceAll("\\n","\n%"); //$NON-NLS-1$ //$NON-NLS-2$
//		}
//		if(CPlatform.isMacOSX()) {
//			comments = comments.replaceAll("\\r","\n%"); //$NON-NLS-1$ //$NON-NLS-2$
//		}
		matlabComments = comments;
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setMatlabGUIFunctionName(String GUIFunctionName) {
		this.GUIFunctionName = GUIFunctionName;
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setMatlabFunctionName(String functionName) {
		matlabFunctionName = functionName;
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setSignalsUsedNumber(int signalsUsedNumber) {
		this.signalsUsedNumber = signalsUsedNumber;
		isDirty = true;		
//		if(channelsNamesList.size() > channelUsedNumber)
//			 for (int i = channelsNamesList.size() - 1; i >= channelUsedNumber ; i--) channelsNamesList.remove(i);
//		else for (int i = channelsNamesList.size(); i < channelUsedNumber ; i++) channelsNamesList.add("");					
		notifyFunctionObservers();
	}

	public void setMarkersUsedNumber(int markerUsedNumber) {
		markersUsedNumber = markerUsedNumber;
		isDirty = true;
//		if(markersNamesList.size() > markerUsedNumber)
//			 for (int i = markersNamesList.size() - 1; i >= markerUsedNumber ; i--) markersNamesList.remove(i);
//		else for (int i = markersNamesList.size(); i < markerUsedNumber ; i++) markersNamesList.add("");					
		notifyFunctionObservers();
	}

	public void setFieldsUsedNumber(int fieldUsedNumber) {
		fieldsUsedNumber = fieldUsedNumber;
		isDirty = true;
//		if(fieldsNamesList.size() > fieldUsedNumber)
//			 for (int i = fieldsNamesList.size() - 1; i >= fieldUsedNumber ; i--) fieldsNamesList.remove(i);
//		else for (int i = fieldsNamesList.size(); i < fieldUsedNumber ; i++) fieldsNamesList.add("");
		notifyFunctionObservers();
	}

	public void setSignalsCreatedNumber(int signalsCreatedNumber) {
		this.signalsCreatedNumber = signalsCreatedNumber;
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setMarkersCreatedNumber(int markerCreatedNumber) {
		markersCreatedNumber = markerCreatedNumber;
		isDirty = true;
//		if(fieldsNamesList.size() > markerCreatedNumber)
//			 for (int i = newMarkersGroupsLabels.size() - 1; i >= markerCreatedNumber ; i--) newMarkersGroupsLabels.remove(i);
//		else for (int i = newMarkersGroupsLabels.size(); i < markerCreatedNumber ; i++) newMarkersGroupsLabels.add("");
		notifyFunctionObservers();
	}

	public void setFieldsCreatedNumber(int fieldCreatedNumber) {
		fieldsCreatedNumber = fieldCreatedNumber;
		isDirty = true;
//		if(fieldsNamesList.size() > fieldCreatedNumber)
//			 for (int i = newFieldsNamesList.size() - 1; i >= fieldCreatedNumber ; i--) newFieldsNamesList.remove(i);
//		else for (int i = newFieldsNamesList.size(); i < fieldCreatedNumber ; i++) newFieldsNamesList.add("");
		notifyFunctionObservers();
	}
	
	public void setSignalsModifiedNumber(int signalsModifiedNumber) {
		this.signalsModifiedNumber = signalsModifiedNumber;
		isDirty = true;
//		if(fieldsNamesList.size() > fieldCreatedNumber)
//			 for (int i = newFieldsNamesList.size() - 1; i >= fieldCreatedNumber ; i--) newFieldsNamesList.remove(i);
//		else for (int i = newFieldsNamesList.size(); i < fieldCreatedNumber ; i++) newFieldsNamesList.add("");
		notifyFunctionObservers();
	}

	public void setParameterMatlabType(String parameterMatlabType, int parameterIndex) {
		parametersMatlabType.set(parameterIndex, parameterMatlabType);
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setParameterComponent(int parameterComponentNumber, int parameterIndex) {
		parametersListComponent.set(parameterIndex, parameterComponentNumber);
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setParameterLabel(String labelText, int parameterIndex) {
		parametersListLabel.set(parameterIndex, labelText);
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setParameterToolTip(String toolTipText, int parameterIndex) {
		parametersListToolTip.set(parameterIndex, toolTipText);
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setParameterRegExp(String regExpText, int parameterIndex) {
		parametersRegExp.set(parameterIndex, regExpText);
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setParameterDefaultValue(String defaultValueText, int parameterIndex) {
		parametersDefaultValue.set(parameterIndex, defaultValueText);
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setSignalsAvailableBool(int parameterIndex, boolean availableChanelsBool) {
		signalsAvailableBool.set(parameterIndex, availableChanelsBool);
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setMarkersAvailableBool(int parameterIndex, boolean availableMarkersBool) {
		markersAvailableBool.set(parameterIndex, availableMarkersBool);
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setFieldsAvailableBool(int parameterIndex, boolean availableFieldsBool) {
		fieldAvailableBool.set(parameterIndex, availableFieldsBool);
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setAvailablesValues(int parameterIndex, String[] elementList) {
		availableListValues.set(parameterIndex, elementList);
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setMatlabFunctionCorpse(String matlabFunctionBody) {
		this.matlabFunctionBody = matlabFunctionBody;
		isDirty = true;
		notifyFunctionObservers();
	}

	public String getScriptCode() {
		String completeInformation = ""; //$NON-NLS-1$

		String matlabComment = getMatlabComments();
		matlabComment = matlabComment.replaceAll("^(" + CPlatform.getEOLCharacter() + ")+", "");
		matlabComment = matlabComment.replaceAll("(" + CPlatform.getEOLCharacter() + ")+$", "");
		String[] matlabComments = matlabComment.split(CPlatform.getEOLCharacter());
		matlabComment = "";
		for (int i = 0; i < matlabComments.length; i++) {
			if(!matlabComments[i].startsWith("%")) matlabComment = matlabComment + "%";
			matlabComment = matlabComment + matlabComments[i] + CPlatform.getEOLCharacter();
		}
		
		completeInformation = matlabComment + "function " + getMatlabFunctionName() + " (TrialsList , signalsNamesList, markersNamesList, fieldsNamesList, signalsNamesSuffix, newMarkersNamesList, newFieldsNamesList, signalsModifiedNumber, ";  //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < getParametersCount(); i++) {
			completeInformation = completeInformation + getParameterLabel(i) + ", "; //$NON-NLS-1$
		}
		completeInformation = completeInformation.replaceAll(", $", "") + ")\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		completeInformation = completeInformation + "%beginAnalyseHeader\n" + "%GUIFunctionName = " + getGUIFunctionName() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		completeInformation = completeInformation + "%MatlabFunctionName = " + getMatlabFunctionName() + "\n" + "%ShortDescritpion = " + getShortDescription() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		completeInformation = completeInformation + "%LongDescription = " + getLongDescription() + "\n" + "%SignalsUsedNumber = " + getSignalsUsedNumber() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		completeInformation = completeInformation + "%MarkersUsedNumber = " + getMarkersUsedNumber() + "\n" + "%FieldsUsedNumber = " + getFieldsUsedNumber() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		completeInformation = completeInformation + "%SignalsCreatedNumber = " + getSignalsCreatedNumber() + "\n" + "%MarkersCreatedNumber = " + getMarkersCreatedNumber() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		completeInformation = completeInformation + "%fieldsCreatedNumber = " + getFieldsCreatedNumber() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		completeInformation = completeInformation + "%signalsModifiedNumber = " + getSignalsModifiedNumber() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < getParametersCount(); i++) {
			String listValues = ""; //$NON-NLS-1$
			if (getAvailablesValues(i)!= null) {


				for (int j = 0; j < getAvailablesValues(i).length; j++) {
					listValues = listValues + getAvailablesValues(i)[j] + ":"; //$NON-NLS-1$
				}
				listValues = listValues.replaceAll(":$", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}else{
				listValues = ""; //$NON-NLS-1$
			}
			completeInformation = completeInformation + "%param" + (i+1) + "MatlabType = " + getParametersMatlabType(i) + "\n" + "%param" + (i+1) + "Component = " + getParameterType(i) + "\n" + "%param" + (i+1) + "Label = " + getParameterLabel(i) + "\n" + "%param" + (i+1) + "ToolTip = " + getToolTip(i) + "\n" + "%param" + (i+1) + "RegExp = " + getParametersRegExp(i) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$
			completeInformation = completeInformation + "%param" + (i+1) + "AvailableListValue = " + listValues + "\n" + "%param" + (i+1) + "DefaultValue = " + getParameterValue(i) + "\n" + "%param" + (i+1) + "SignalsAvailable = " + getSignalsAvailableBool(i) + "\n" + "%param" + (i+1) + "MarkersAvailable = " + getMarkersAvailableBool(i) + "\n" + "%param" + (i+1) + "FieldsAvailable = " + getFieldAvailableBool(i) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$
		}
		completeInformation = completeInformation + "%endAnalyseHeader" + "\n" + getMatlabFunctionPreBody() + getMatlabFunctionBody() + "\n" + getMatlabFunctionPostBody(); //$NON-NLS-1$ //$NON-NLS-2$
		return completeInformation;
	}
	
	public boolean isDirty() {
		return isDirty ;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public void notifyFunctionObservers() {
		if(functionObservers == null) functionObservers = new HashSet<IFunctionObserver>(0);
		IFunctionObserver[] observers =  functionObservers.toArray(new IFunctionObserver[functionObservers.size()]);
		for (int i = 0; i < observers.length; i++) {
			observers[i].update(this);
		}
	}

	public void resetOldMatlabFunctionName() {
		oldMatlabFunctionName = matlabFunctionName;		
	}

	public String getSignalsNamesList(int nupletNumber) {
		return signalsNamesList.get(nupletNumber);
	}

	public int getSignalsNbUplets() {
		return signalsNamesList.size();
	}
	
	public String getMarkersNamesList(int nupletNumber) {		
		return markersNamesList.get(nupletNumber);
	}

	public int getMarkersNbUplets() {
		return markersNamesList.size();
	}
	
	public String getFieldsNamesList(int nupletNumber) {
		return fieldsNamesList.get(nupletNumber);
	}
	
	public int getFieldsNbUplets() {
		return fieldsNamesList.size();
	}

	public String getNewSignalsNamesSuffix() {
		return newSignalsNamesSuffix;
	}

	public String getNewMarkersGroupLabels() {
		return newMarkersGroupsLabels;
	}
	public String getNewMarkersGroupLabels(int nupletNumber) {
		return newMarkersGroupsLabels.split(":")[--nupletNumber]; //$NON-NLS-1$
	}

	public String getNewFieldsNamesList(){
		return newFieldsNamesLabels;
	}
	
	public String getNewFieldsNamesList(int nupletNumber) {		
		return newFieldsNamesLabels.split(":")[--nupletNumber]; //$NON-NLS-1$
	}

	public void setSignalsNamesList(String[] text) {
		signalsNamesList.clear();
		for (int i = 0; i < text.length; i++) signalsNamesList.add(text[i]);
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setMarkersNamesList(String[] text) {
		markersNamesList.clear();
		for (int i = 0; i < text.length; i++) markersNamesList.add(text[i]);
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setFieldsNameslist(String[] text) { 
		fieldsNamesList.clear();
		for (int i = 0; i < text.length; i++) fieldsNamesList.add(text[i]);
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setNewSignalsNamesSuffix(String text) {
		newSignalsNamesSuffix = text;		
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setNewMarkersGroupLabels(String text) {
		newMarkersGroupsLabels = text;
		isDirty = true;
		notifyFunctionObservers();
	}

	public void setNewFieldsNamesList(String text) {
		newFieldsNamesLabels = text;
		isDirty = true;
		notifyFunctionObservers();
	}

	protected String getProcessingCode() {
		
		if(getTrialsList().equals("")) { //$NON-NLS-1$
			MessageDialog messageDialog = new MessageDialog(Display.getCurrent().getActiveShell(), Messages.getString("Function.DialogErrorEmptyTrialsTitle"), ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON),  //$NON-NLS-1$
					Messages.getString("Function.DialogErrorEmptyTrialsText1") + getGUIFunctionName() + Messages.getString("Function.DialogErrorEmptyTrialsText2"), //$NON-NLS-1$ //$NON-NLS-2$
					MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL}, 0);
			messageDialog.open();
			return ""; //$NON-NLS-1$
		}
		
		String trialsList = "[" + getTrialsList() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		
		String signalsString = "'"; //$NON-NLS-1$
		if(getSignalsUsedNumber() > 0) {
			for (int i = 0; i < getSignalsNbUplets(); i++) signalsString = signalsString + getSignalsNamesList(i) + ":" ; //$NON-NLS-1$
			signalsString = signalsString.replaceAll(":$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		signalsString = signalsString + "'"; //$NON-NLS-1$
		
		String markersString = "'"; //$NON-NLS-1$
		if(getMarkersUsedNumber() > 0) {
			for (int i = 0; i < getMarkersNbUplets(); i++) {
				String value = getMarkersNamesList(i);
				value = value.replaceAll("_Values - '?[A-Za-z0-9_]*'?", "");//$NON-NLS-1$ //$NON-NLS-2$ 
				value = value.replaceAll("0 - [A-Za-z0-9\\s]*", "0");//$NON-NLS-1$ //$NON-NLS-2$ 
				value = value.replaceAll("Inf - [A-Za-z0-9\\s]*", "Inf");//$NON-NLS-1$ //$NON-NLS-2$ 
				markersString = markersString + value + ":"; //$NON-NLS-1$
			}
			markersString = markersString.replaceAll(":$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		} 
		markersString = markersString + "'"; //$NON-NLS-1$
		 		
		String fieldsString = "'"; //$NON-NLS-1$
		if(getFieldsUsedNumber() > 0) {
			for (int i = 0; i < getFieldsNbUplets(); i++) fieldsString = fieldsString + getFieldsNamesList(i).replaceAll("_Values - '?[A-Za-z0-9]*'?$", "") + ":" ; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			fieldsString = fieldsString.replaceAll(":$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		fieldsString = fieldsString + "'"; //$NON-NLS-1$
		
		String newSignalsNamesSuffix = "'" + getNewSignalsNamesSuffix() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
		String newMarkersNameList = "'" + getNewMarkersGroupLabels() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
		String newFieldsNameList = "'" + getNewFieldsNamesList() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
		String parameters = ""; //$NON-NLS-1$
		
		String matlabCommand = getMatlabFunctionName() + "("; //$NON-NLS-1$
		matlabCommand = matlabCommand + trialsList + "," + signalsString; //$NON-NLS-1$
		matlabCommand = matlabCommand + "," + markersString; //$NON-NLS-1$
		matlabCommand = matlabCommand + "," + fieldsString; //$NON-NLS-1$
		matlabCommand = matlabCommand + "," + newSignalsNamesSuffix; //$NON-NLS-1$
		matlabCommand = matlabCommand + "," + newMarkersNameList; //$NON-NLS-1$
		matlabCommand = matlabCommand + "," + newFieldsNameList; //$NON-NLS-1$
		matlabCommand = matlabCommand + "," + getSignalsModifiedNumber(); //$NON-NLS-1$
		
		for (int i = 0; i < getParametersCount(); i++) {
			if(getParametersMatlabType(i).equals(GUI_MATLAB_TYPE_STRING)) parameters = parameters + "'" + getParameterValue(i) + "'" + ","; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			else parameters = parameters + getParameterValue(i) + ","; //$NON-NLS-1$
		}
		parameters = parameters.replaceAll(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if(parameters.equals("")) //$NON-NLS-1$
		matlabCommand = matlabCommand + ")"; //$NON-NLS-1$
		else matlabCommand = matlabCommand + "," + parameters + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		return matlabCommand;
	}

//	public void initializeFunctionObservers() {
//		functionObservers = new ArrayList<IFunctionObserver>(0);
//	}

	public void updateChannelsNames(String oldFullName, String fullName) {
		//update signals names list
		for (int i = 0; i < signalsNamesList.size(); i++) {
			 String uplet = signalsNamesList.get(i);
			 uplet = uplet.replaceAll(oldFullName, fullName);
			 signalsNamesList.set(i, uplet);
		}
		//update markers names list
		for (int i = 0; i < markersNamesList.size(); i++) {
			 String uplet = markersNamesList.get(i);
			 uplet = uplet.replaceAll(oldFullName, fullName);
			 markersNamesList.set(i, uplet);
		}
		//update fields names list 
		for (int i = 0; i < fieldsNamesList.size(); i++) {
			 String uplet = fieldsNamesList.get(i);
			 uplet = uplet.replaceAll(oldFullName, fullName);
			 fieldsNamesList.set(i, uplet);
		}
		
	}

	public void setError(boolean error) {
		this.error = error; 
	}

	public void setWarning(boolean warning) {
		this.warning = warning;
	}
	
	public boolean hasErrors() {
		return error;
	}
	
	public boolean hasWarnings() {
		return warning;
	}

	public void replace(String replace, String by) {
		for (int i = 0; i < signalsNamesList.size(); i++) {
			if(signalsNamesList.get(i).contains(replace)) {
				String replaced = signalsNamesList.get(i);
				replaced = replaced.replaceAll(replace, by);
				signalsNamesList.set(i, replaced);
			}
		}
		for (int i = 0; i < markersNamesList.size(); i++) {
			if(markersNamesList.get(i).contains(replace)) {
				String replaced = markersNamesList.get(i);
				replaced = replaced.replaceAll(replace, by);
				markersNamesList.set(i, replaced);
			}
		}
		for (int i = 0; i < fieldsNamesList.size(); i++) {
			if(fieldsNamesList.get(i).contains(replace)) {
				String replaced = fieldsNamesList.get(i);
				replaced = replaced.replaceAll(replace, by);
				fieldsNamesList.set(i, replaced);
			}
		}
		if(getSignalsCreatedNumber() > 0) newSignalsNamesSuffix = newSignalsNamesSuffix.replaceAll(replace, by);
		if(getMarkersCreatedNumber() > 0) newMarkersGroupsLabels = newMarkersGroupsLabels.replaceAll(replace, by);
		if(getFieldsCreatedNumber() > 0) newFieldsNamesLabels = newFieldsNamesLabels.replaceAll(replace, by);
	}

}

