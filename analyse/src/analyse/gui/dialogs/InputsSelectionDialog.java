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
package analyse.gui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import analyse.gui.ProcessingEditor.InputsOutputsKinds;
import analyse.model.DataProcessing;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.Function;
import analyse.model.IResource;
import analyse.model.Subject;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

@SuppressWarnings("deprecation")
public class InputsSelectionDialog extends TitleAreaDialog {

	//ArrayList<String> channelsList = new ArrayList<String>(0);
	private HashSet<String> newChannels = new HashSet<String>(0);
	private TableViewer inputsTableViewer;
	private Function function;
	private InputsOutputsKinds inputsKind;
	private int inputNumber;
	private String[] selectedChannels;
	private DataProcessing dataProcessing;
	
	private class ChannelsFilter extends ViewerFilter {
		private String names;
		public ChannelsFilter(String names) {
			this.names = names;
		}
		@Override
		public boolean select(Viewer viewer, Object parentElement,Object element) {			
			if (names.equals("")) return true; //$NON-NLS-1$
			if(names.matches("^\\*$")) return true;
			String namesTemp = names.replaceAll("\\*", "\\\\w*");
			namesTemp = namesTemp.replaceAll("\\.", "\\\\.");
			return ((String)element).matches(namesTemp);
		}
	}
	
	public InputsSelectionDialog(Shell parentShell, DataProcessing dataProcessing, Function function, int n, InputsOutputsKinds inputsKind) {
		super(parentShell);
		setShellStyle(SWT.MAX | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		this.function = function;
		this.inputNumber = n;
		this.inputsKind = inputsKind;
		this.dataProcessing = dataProcessing;
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);		
		
		String label1 = ""; //$NON-NLS-1$
		String label2 = ""; //$NON-NLS-1$
		switch (inputsKind) {
			case SIGNAL:
				label1 = Messages.getString("InputsSelectionDialog.SignalsTitle"); //$NON-NLS-1$
				label2 = Messages.getString("InputsSelectionDialog.SignalsText"); //$NON-NLS-1$
				break;
			case MARKER:
				label1 = Messages.getString("InputsSelectionDialog.MarkersTitle"); //$NON-NLS-1$
				label2 = Messages.getString("InputsSelectionDialog.MarkersText"); //$NON-NLS-1$
				break;
			case FIELD:
				label1 = Messages.getString("InputsSelectionDialog.FieldsTitle"); //$NON-NLS-1$
				label2 = Messages.getString("InputsSelectionDialog.FieldsText"); //$NON-NLS-1$
				break;
			default:
				break;
		}
		
		parent.getShell().setText(label1);
		
		getShell().setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		
		setTitle(label2);
		setMessage(Messages.getString("InputsSelectionDialog.FilterArea"));		 //$NON-NLS-1$
			
		int monitorNumber = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.MONITOR_NUMBER);		
		Monitor monitor = parent.getDisplay().getMonitors()[monitorNumber];
		
		Rectangle bounds = monitor.getBounds ();
		Rectangle rect = parent.getShell().getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		parent.getShell().setLocation (x, y);
						
		return contents;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea =  (Composite) super.createDialogArea(parent);
				
		Composite container = new Composite(dialogArea, SWT.NONE);			
		container.setLayout(new GridLayout(1,false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label channelNamelabel = new Label(container,SWT.NONE);
		channelNamelabel.setText(Messages.getString("InputsSelectionDialog.ChooseName")); //$NON-NLS-1$
		channelNamelabel.setLayoutData(new GridData());
			
		Text channelNameText= new Text(container, SWT.SEARCH | SWT.CANCEL);
		channelNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));				
		
		Label matchingChannelNamelabel = new Label(container,SWT.NONE);
		matchingChannelNamelabel.setText(Messages.getString("InputsSelectionDialog.MatchingNames")); //$NON-NLS-1$
		matchingChannelNamelabel.setLayoutData(new GridData());
		
		inputsTableViewer = new TableViewer(container,SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.MULTI);
		inputsTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		inputsTableViewer.setContentProvider(new IStructuredContentProvider(){
			@SuppressWarnings("rawtypes")
			public Object[] getElements(Object inputElement) {				
				String[] emptyArray = new String[0];
				if(inputElement instanceof ArrayList) {
					String[] names = new String[((ArrayList)inputElement).size()];
					for (int i = 0; i < names.length; i++) names[i] = (String) ((ArrayList)inputElement).get(i);
					return names;
				}
				return emptyArray;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
		});
		inputsTableViewer.setLabelProvider(new ILabelProvider(){
			public Image getImage(Object element) {
				if(newChannels.contains((String)element)) return ImagesUtils.getImage(IImagesKeys.NEW_SIGNAL_ICON);
				return null;
			}
			public String getText(Object element) {
				return (String)element;
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public void dispose() {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener listener) {
			}
		});		
		inputsTableViewer.setSorter(new ViewerSorter());
		
		channelNameText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				ViewerFilter[] filters = inputsTableViewer.getFilters();
				for (int i = 0; i < filters.length; i++) inputsTableViewer.removeFilter(filters[i]);
				String filterString = ((Text)e.widget).getText();
				if(!filterString.equals("")) inputsTableViewer.addFilter(new ChannelsFilter(filterString));				 //$NON-NLS-1$
			}			
		});
		
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		IResource[] experiments = Experiments.getInstance().getChildren(); 
		ArrayList<String> namesList = new ArrayList<String>(0);
		ArrayList<String> strings = new ArrayList<String>(0) ;
		ArrayList<String> alreadyUsedInputs = new ArrayList<String>(0);
		switch (inputsKind) {
			case SIGNAL:
				for (int k = 0; k < function.getSignalsNbUplets(); k++) {
					String signals = function.getSignalsNamesList(k);
					if(!signals.equals(""))	{ //$NON-NLS-1$
						String[] signalsSplitted = signals.split(","); //$NON-NLS-1$
						if(signalsSplitted.length > inputNumber)
							if(!signalsSplitted[inputNumber].equals(""))
								alreadyUsedInputs.add(signalsSplitted[inputNumber]);		 //$NON-NLS-1$
					}
				}
				break;
			case MARKER:
				for (int k = 0; k < function.getMarkersNbUplets(); k++) {
					String markers = function.getMarkersNamesList(k);
					if(!markers.equals(""))	{ //$NON-NLS-1$
						String[] markersSplitted = markers.split(","); //$NON-NLS-1$
						if(markersSplitted.length > inputNumber)
							if(!markersSplitted[inputNumber].equals(""))
								alreadyUsedInputs.add(markersSplitted[inputNumber]);		 //$NON-NLS-1$
					}
				}
				break;
			case FIELD:
				for (int k = 0; k < function.getFieldsNbUplets(); k++) {
					String fields = function.getFieldsNamesList(k);
					if(!fields.equals(""))	{ //$NON-NLS-1$
						String[] fieldsSplitted = fields.split(","); //$NON-NLS-1$
						if(fieldsSplitted.length > inputNumber)
							if(!fieldsSplitted[inputNumber].equals(""))
								alreadyUsedInputs.add(fieldsSplitted[inputNumber]);		 //$NON-NLS-1$
					}
				}
				break;
			default:
				break;
		}
		for (int i = 0; i < experiments.length; i++) {	
			strings.clear();
			IResource[] subjects = ((Experiment)experiments[i]).getSubjects();
			for (int j = 0; j < subjects.length; j++) {
				if(((Subject)subjects[j]).isLoaded()) 
					switch (inputsKind) {
					case SIGNAL:
							String[] localStrings = ((Subject)subjects[j]).getSignalsNames();//mathEngine.getSignalsNames(subjects[j].getLocalPath());
							for (int k = 0; k < localStrings.length; k++) {
								localStrings[k] = subjects[j].getLocalPath() + "." + localStrings[k];
							}
							strings.addAll(Arrays.asList(localStrings));
						break;
					case MARKER:
							ArrayList<String> labels = new ArrayList<String>(0);
							String[] signalsNames = ((Subject)subjects[j]).getSignalsNames();//mathEngine.getSignalsNames(subjects[j].getLocalPath());
							for (int k = 0; k < signalsNames.length; k++) {
								int nbMarkersGroups = mathEngine.getNbMarkersGroups(subjects[j].getLocalPath() + "." + signalsNames[k]);
								for (int l = 0; l < nbMarkersGroups; l++) {
									String markersGroupLabel = mathEngine.getMarkersGroupLabel(l+1, subjects[j].getLocalPath() + "." + signalsNames[k]);
									labels.add(subjects[j].getLocalPath() + "." + signalsNames[k] + IMathEngine.Marker + (l+1)  + IMathEngine._Values + " - " + markersGroupLabel);
								}
							}
							strings.addAll(labels);
						break;
					case FIELD:
							ArrayList<String> labels2 = new ArrayList<String>(0);
							String[] signalsNames2 = ((Subject)subjects[j]).getSignalsNames();//mathEngine.getSignalsNames(subjects[j].getLocalPath());
							for (int k = 0; k < signalsNames2.length; k++) {
								int nbFields = mathEngine.getNbFields(subjects[j].getLocalPath() + "." + signalsNames2[k]);
								for (int l = 0; l < nbFields; l++) {
									String fieldLabel = mathEngine.getFieldLabel(l+1, subjects[j].getLocalPath() + "." + signalsNames2[k]);
									labels2.add(subjects[j].getLocalPath() + "." + signalsNames2[k] + IMathEngine.Field +(l+1)  + IMathEngine._Values + " - " + fieldLabel);
								}
							}
							strings.addAll(labels2);
						break;
					default:
						break;
					}
			}
			for (int k = 0; k < strings.size(); k++) {
				boolean found = false;
				for (int l = 0; l < alreadyUsedInputs.size(); l++) {
					if(strings.get(k).startsWith(alreadyUsedInputs.get(l))) found = true;
				}
				if(!found) namesList.add(strings.get(k));
			}
		}
		
		newChannels.clear();
		Function[] functions = dataProcessing.getFunctions();
		for (int i = 0; i < functions.length; i++) {
			if(functions[i] != function) {
				Function currentFunction = functions[i];
				switch (inputsKind) {
				case SIGNAL:
					if(currentFunction.getSignalsCreatedNumber() > 0) {
						HashSet<String> signalsPrefix = new HashSet<String>(0);
						for (int j = 0; j < currentFunction.getSignalsNbUplets(); j++) {
							if(!currentFunction.getSignalsNamesList(j).equals(""))
							signalsPrefix.add(currentFunction.getSignalsNamesList(j).split(",")[0]);
						}
						String[] names = currentFunction.getNewSignalsNamesSuffix().split(",");
						for (int k = 0; k < names.length; k++) {
							for (Iterator<String> iterator = signalsPrefix.iterator(); iterator.hasNext();) {
								String prefix = iterator.next();
								String signalName = prefix + names[k];
								boolean found = false;
								for (int l = 0; l < alreadyUsedInputs.size(); l++) {
									if(signalName.startsWith(alreadyUsedInputs.get(l))) found = true;
								}
								if(!found) {
									namesList.add(signalName);
									newChannels.add(signalName);
									alreadyUsedInputs.add(signalName);
								}
							}
						}
					}
					break;
				case MARKER:
					if(currentFunction.getMarkersCreatedNumber() > 0) {
						HashSet<String> markersLabel = new HashSet<String>(0);
						for (int j = 0; j < currentFunction.getMarkersCreatedNumber(); j++) {
							String markersGroupLabel = currentFunction.getNewMarkersGroupLabels(j+1);
							for (int k = 0; k < currentFunction.getSignalsNbUplets(); k++) {
								if(!currentFunction.getSignalsNamesList(k).equals(""))
									markersLabel.add(currentFunction.getSignalsNamesList(k).split(",")[0] + IMathEngine.Marker + "." + IMathEngine._Values + " - " + markersGroupLabel);
							}
						}
//						String[] names = currentFunction.getNewMarkersNamesSuffix().split(",");
//						for (int k = 0; k < names.length; k++) {
//							for (Iterator<String> iterator = signalsPrefix.iterator(); iterator.hasNext();) {
//								String prefix = iterator.next();
//								String signalName = prefix + names[k];
//								boolean found = false;
//								for (int l = 0; l < alreadyUsedInputs.size(); l++) {
//									if(signalName.startsWith(alreadyUsedInputs.get(l))) found = true;
//								}
//								if(!found) {
//									namesList.add(signalName);
//									newSignals.add(signalName);
//								}
//							}
//						}
					}
					break;
				case FIELD:
					if(currentFunction.getFieldsCreatedNumber() > 0) {
						HashSet<String> fieldsLabel = new HashSet<String>(0);
						for (int j = 0; j < currentFunction.getFieldsCreatedNumber(); j++) {
							String fieldLabel = currentFunction.getNewFieldsNamesList(j+1);
							for (int k = 0; k < currentFunction.getSignalsNbUplets(); k++) {
								if(!currentFunction.getSignalsNamesList(k).equals(""))
									fieldsLabel.add(currentFunction.getSignalsNamesList(k).split(",")[0]+ IMathEngine.Field + "N" + IMathEngine._Values + " - " + fieldLabel);
							}
						}
						for (Iterator<String> iterator = fieldsLabel.iterator(); iterator.hasNext();) {
							String prefix = iterator.next();
							String prefixMatch = prefix.replaceAll("N_Values", "\\\\d+_Values");
							boolean found = false;
							for (int k = 0; k < alreadyUsedInputs.size(); k++) {
								if(alreadyUsedInputs.get(k).matches(prefixMatch)) found = true;
							}
							for (int k = 0; k < namesList.size(); k++) {
								if(namesList.get(k).matches(prefixMatch)) found = true;
							}
							if(!found) {
								namesList.add(prefix);
								newChannels.add(prefix);
								alreadyUsedInputs.add(prefix);
							}
						}
					}
					break;
				default:
					break;
				}
			}
		}
		
		
		if(inputsKind == InputsOutputsKinds.MARKER) {
			namesList.add("0 - From begining");
			namesList.add("Inf - To end");
		}
		inputsTableViewer.setInput(namesList);
		
		return dialogArea;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// TODO Auto-generated method stub
		super.createButtonsForButtonBar(parent);
		createButton(parent, IDialogConstants.SELECT_ALL_ID,IAnalyseDialogConstants.SELECT_ALL_LABEL, false);
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		if (IDialogConstants.SELECT_ALL_ID == buttonId) {
			inputsTableViewer.getTable().setFocus();
			inputsTableViewer.getTable().selectAll(); 
		}
	}
	
	@Override
	protected void okPressed() {
		TableItem[] tabItems = inputsTableViewer.getTable().getSelection();
		HashSet<String> signals = new HashSet<String>(0);
		for (int i = 0; i < tabItems.length; i++) {
			signals.add(tabItems[i].getText());
		}
		selectedChannels = signals.toArray(new String[signals.size()]);
		Arrays.sort(selectedChannels);
		super.okPressed();
	}
	
	public String[] getSelectedChannels() {
		return selectedChannels;
	}
	
}
