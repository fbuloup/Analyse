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

import java.util.HashMap;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IMessage;

import analyse.resources.Messages;

public class FunctionValidator {

	private String TRIALS_LIST_ERROR = "TRIALS_LIST_ERROR";
	private String INPUTS_SIGNALS_UPLET_ERROR = "INPUTS_SIGNALS_UPLET_ERROR";
	private String INPUTS_MARKERS_UPLET_ERROR = "INPUTS_MARKERS_UPLET_ERROR";
	private String INPUTS_FIELDS_UPLET_ERROR = "INPUTS_FIELDS_UPLET_ERROR";
	private String OUTPUTS_SIGNALS_ERROR = "OUTPUTS_SIGNALS_ERROR";
	private String OUTPUTS_MARKERS_ERROR = "OUTPUTS_MARKERS_ERROR";
	private String OUTPUTS_FIELDS_ERROR = "OUTPUTS_FIELDS_ERROR";
	private String PARAMS_ERROR = "PARAMS_ERROR";
	
	private String TRIALS_LIST_WARNING = "TRIALS_LIST_WARNING";
	private String OUTPUTS_SIGNALS_WARNING = "OUTPUTS_SIGNALS_WARNING";
	private String OUTPUTS_MARKERS_WARNING = "OUTPUTS_MARKERS_WARNING";
	private String OUTPUTS_FIELDS_WARNING = "OUTPUTS_FIELDS_WARNING";
	private String PARAMS_WARNING = "PARAMS_WARNING";
	
	private Function function;
	private HashMap<String,IMessage> errorsMessages;
	private HashMap<String,IMessage> warningsMessages;
	
	private class Message implements IMessage {
		private int type;
		private String message;
		private String prefix;
		private Object key;
		public Message(Object key, int type, String message, String prefix) {
			this.key = key;
			this.type = type;
			this.message = message;
			this.prefix = prefix;
		}
		public Control getControl() {
			return null;
		}
		public Object getData() {
			return null;
		}
		public Object getKey() {
			return key;
		}
		public String getPrefix() {
			return prefix;
		}
		public String getMessage() {
			return message;
		}
		public int getMessageType() {
			return type;
		}
	}
	
	public FunctionValidator(Function function) {
		this.function = function;
	}
	
	public Function getFunction() {
		return function;
	}
	
	public boolean validate() {
		if(errorsMessages == null) errorsMessages = new HashMap<String,IMessage>(0);
		if(warningsMessages == null) warningsMessages = new HashMap<String, IMessage>(0);
		errorsMessages.clear();
		warningsMessages.clear();
		function.setError(false);
		function.setWarning(false);
		//Trials list
		if(!function.getTrialsList().matches("(\\d+[\\s,:]*)+")) {
			if(function.getTrialsList().equals("")) {
				IMessage message = new Message(TRIALS_LIST_WARNING, IMessageProvider.WARNING, "\t\t--> " + Messages.getString("ProcessEditor.WarningMessageTrialsListEmpty"), function.getGUIFunctionName());
				warningsMessages.put(TRIALS_LIST_WARNING,message);
				function.setWarning(true);
			} else {
				IMessage message = new Message(TRIALS_LIST_ERROR, IMessageProvider.ERROR, "\t\t--> " + Messages.getString("ProcessEditor.ErrorMessageTrialsList"), function.getGUIFunctionName());
				errorsMessages.put(TRIALS_LIST_ERROR,message);
				function.setError(true);
			}
		}
		//Signals Inputs uplets errors
		int nbUplets = function.getSignalsNbUplets();
		int nbSignals = function.getSignalsUsedNumber();
		if(nbUplets == 0 && nbSignals > 0) {
			IMessage message = new Message(INPUTS_SIGNALS_UPLET_ERROR, IMessageProvider.ERROR, "\t\t--> " + Messages.getString("ProcessEditor.ErrorMessageInputSignalsUplets"), function.getGUIFunctionName());
			errorsMessages.put(INPUTS_SIGNALS_UPLET_ERROR,message);
			function.setError(true);
		}
		for (int i = 0; i < nbUplets; i++) {
			String signalsNames = function.getSignalsNamesList(i);
			String[] splitted = signalsNames.split(",");
			boolean error = false;
			for (int j = 0; j < splitted.length; j++) {
				if(splitted[j].equals("")) error = true;
			}
			if(splitted.length != nbSignals || error) {
				IMessage message = new Message(INPUTS_SIGNALS_UPLET_ERROR, IMessageProvider.ERROR, "\t\t--> " + Messages.getString("ProcessEditor.ErrorMessageInputSignalsUplets"), function.getGUIFunctionName());
				errorsMessages.put(INPUTS_SIGNALS_UPLET_ERROR,message);
				function.setError(true);
			}
		}
		//Markers Inputs uplets errors
		nbUplets = function.getMarkersNbUplets();
		int nbMarkers = function.getMarkersUsedNumber();
		if(nbUplets == 0 && nbMarkers > 0) {
			IMessage message = new Message(INPUTS_MARKERS_UPLET_ERROR, IMessageProvider.ERROR, "\t\t--> " + Messages.getString("ProcessEditor.ErrorMessageInputMarkersUplets"), function.getGUIFunctionName());
			errorsMessages.put(INPUTS_MARKERS_UPLET_ERROR,message);
			function.setError(true);
		}
		for (int i = 0; i < nbUplets; i++) {
			String markersNames = function.getMarkersNamesList(i);
			String[] splitted = markersNames.split(",");
			boolean error = false;
			for (int j = 0; j < splitted.length; j++) {
				if(splitted[j].equals("")) error = true;
			}
			if(splitted.length != nbMarkers || error) {
				IMessage message = new Message(INPUTS_MARKERS_UPLET_ERROR, IMessageProvider.ERROR, "\t\t--> " + Messages.getString("ProcessEditor.ErrorMessageInputMarkersUplets"), function.getGUIFunctionName());
				errorsMessages.put(INPUTS_MARKERS_UPLET_ERROR,message);
				function.setError(true);
			}
		}
		//Fields Inputs uplets errors
		nbUplets = function.getFieldsNbUplets();
		int nbFields = function.getFieldsUsedNumber();
		if(nbUplets == 0 && nbFields > 0) {
			IMessage message = new Message(INPUTS_FIELDS_UPLET_ERROR, IMessageProvider.ERROR, "\t\t--> " + Messages.getString("ProcessEditor.ErrorMessageInputFieldsUplets"), function.getGUIFunctionName());
			errorsMessages.put(INPUTS_FIELDS_UPLET_ERROR,message);
			function.setError(true);
		}
		for (int i = 0; i < nbUplets; i++) {
			String fieldsNames = function.getFieldsNamesList(i);
			String[] splitted = fieldsNames.split(",");
			boolean error = false;
			for (int j = 0; j < splitted.length; j++) {
				if(splitted[j].equals("")) error = true;
			}
			if(splitted.length != nbFields || error) {
				IMessage message = new Message(INPUTS_FIELDS_UPLET_ERROR, IMessageProvider.ERROR, "\t\t--> " + Messages.getString("ProcessEditor.ErrorMessageInputFieldsUplets"), function.getGUIFunctionName());
				errorsMessages.put(INPUTS_FIELDS_UPLET_ERROR,message);
				function.setError(true);
			}
		}
		//Outputs signals
		int nbCreatedSignals = function.getSignalsCreatedNumber();
		if (nbCreatedSignals > 0) {
			String[] suffixes = function.getNewSignalsNamesSuffix().split(",");
			if(suffixes.length != function.getSignalsCreatedNumber()) {
				IMessage message = new Message(OUTPUTS_SIGNALS_ERROR, IMessageProvider.ERROR, "\t\t--> " + Messages.getString("ProcessEditor.ErrorMessageOutputSignals"), function.getGUIFunctionName());
				errorsMessages.put(OUTPUTS_SIGNALS_ERROR,message);
				function.setError(true);
			}
			for (int i = 0; i < suffixes.length; i++) {
				if(!suffixes[i].matches("^[a-zA-Z]+\\w*")) {
					if(suffixes[i].equals("")) {
						IMessage message = new Message(OUTPUTS_SIGNALS_WARNING, IMessageProvider.WARNING, "\t\t--> " + Messages.getString("ProcessEditor.WarningMessageOutputSignalsEmpty"), function.getGUIFunctionName());
						warningsMessages.put(OUTPUTS_SIGNALS_WARNING,message);
						function.setWarning(true);
					} else {
						IMessage message = new Message(OUTPUTS_SIGNALS_ERROR, IMessageProvider.ERROR, "\t\t--> " + Messages.getString("ProcessEditor.ErrorMessageOutputSignals"), function.getGUIFunctionName());
						errorsMessages.put(OUTPUTS_SIGNALS_ERROR,message);
						function.setError(true);
					}
				}
			}
			
		}
		//Outputs markers
		int nbCreatedMarkers = function.getMarkersCreatedNumber();
		if (nbCreatedMarkers > 0) {
			String[] markersValuesUplets = function.getNewMarkersGroupLabels().split(":");
			for (int i = 0; i < markersValuesUplets.length; i++) {
				String[] markersValuesUpletsSplitted = markersValuesUplets[i].split(",");
				for (int j = 0; j < markersValuesUpletsSplitted.length; j++) {
					if(!markersValuesUpletsSplitted[j].matches("^[a-zA-Z]+\\w*")) {
						if(markersValuesUpletsSplitted[j].equals("")) {
							IMessage message = new Message(OUTPUTS_MARKERS_WARNING, IMessageProvider.WARNING, "\t\t--> " + Messages.getString("ProcessEditor.WarningMessageOutputMarkersEmpty"), function.getGUIFunctionName());
							warningsMessages.put(OUTPUTS_SIGNALS_WARNING,message);
							function.setWarning(true);
						} else {
							IMessage message = new Message(OUTPUTS_MARKERS_ERROR, IMessageProvider.ERROR, "\t\t--> " + Messages.getString("ProcessEditor.ErrorMessageOutputMarkers"), function.getGUIFunctionName());
							errorsMessages.put(OUTPUTS_MARKERS_ERROR,message);
							function.setError(true);
						}
					}
					
				}
			}
		}
		//Outputs fields
		int nbCreatedFields = function.getFieldsCreatedNumber();
		if (nbCreatedFields > 0) {
			String[] fieldsValuesUplets = function.getNewFieldsNamesList().split(":");
			for (int i = 0; i < fieldsValuesUplets.length; i++) {
				String[] fieldsValuesUpletsSplitted = fieldsValuesUplets[i].split(",");
				for (int j = 0; j < fieldsValuesUpletsSplitted.length; j++) {
					if(!fieldsValuesUpletsSplitted[j].matches("^[a-zA-Z]+\\w*")) {
						if(fieldsValuesUpletsSplitted[j].equals("")) {
							IMessage message = new Message(OUTPUTS_FIELDS_WARNING, IMessageProvider.WARNING, "\t\t--> " + Messages.getString("ProcessEditor.WarningMessageOutputFieldsEmpty"), function.getGUIFunctionName());
							warningsMessages.put(OUTPUTS_FIELDS_WARNING,message);
							function.setWarning(true);
						} else {
							IMessage message = new Message(OUTPUTS_FIELDS_ERROR, IMessageProvider.ERROR, "\t\t--> " + Messages.getString("ProcessEditor.ErrorMessageOutputFields"), function.getGUIFunctionName());
							errorsMessages.put(OUTPUTS_FIELDS_ERROR,message);
							function.setError(true);
						}
					}
					
				}
			}
		}
		//Params
		for(int i=0; i < function.getParametersCount(); i++) {
			String value = (String) function.getParameterValue(i);
			if(!value.matches(function.getParametersRegExp(i))) {
				if(value.equals("")) {
					IMessage message = new Message(PARAMS_WARNING, IMessageProvider.WARNING, "\t\t--> " + Messages.getString("ProcessEditor.WarningMessageParamsEmpty"), function.getGUIFunctionName());
					warningsMessages.put(PARAMS_WARNING,message);
					function.setWarning(true);
				} else {
					IMessage message = new Message(PARAMS_ERROR, IMessageProvider.ERROR, "\t\t--> " + Messages.getString("ProcessEditor.ErrorMessageParams"), function.getGUIFunctionName());
					errorsMessages.put(PARAMS_ERROR,message);
					function.setError(true);
				}
				
			}
		}
		return errorsMessages.size() == 0 && warningsMessages.size() == 0;
	}

	public boolean hasErrors() {
		return errorsMessages.size() > 0;
	}

	public IMessage getFirstErrorMessage() {
		return errorsMessages.values().toArray(new IMessage[errorsMessages.size()])[0];
	}

	public String[] getErrorsMessages() {
		if(errorsMessages.values().size() == 0) return new String[0];
		IMessage[] errors = errorsMessages.values().toArray(new IMessage[errorsMessages.size()]);
		String[] errorsString = new String[errors.length];
		for (int i = 0; i < errorsString.length; i++) errorsString[i] = errors[i].getMessage();
		return errorsString;
	}

	public boolean hasWarnings() {
		return warningsMessages.size() > 0;
	}

	public IMessage getFirstWarningMessage() {
		return warningsMessages.values().toArray(new IMessage[warningsMessages.size()])[0];
	}

	public String[] getWarningsMessages() {
		if(warningsMessages.values().size() == 0) return new String[0];
		IMessage[] warnings = warningsMessages.values().toArray(new IMessage[warningsMessages.size()]);
		String[] warningsString = new String[warnings.length];
		for (int i = 0; i < warningsString.length; i++) warningsString[i] = warnings[i].getMessage();
		return warningsString;
	}
	
}
