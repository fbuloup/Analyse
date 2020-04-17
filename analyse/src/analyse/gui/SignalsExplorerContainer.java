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
package analyse.gui;

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.graphics.Color;
import org.swtchart.ext.ChartMarker;
import org.swtchart.ext.ChartMarker.MARKER_GRAPHIC_SYMBOL;

import analyse.Log;
import analyse.gui.dialogs.ModifySampleFrequencyDialog;
import analyse.model.Experiments;
import analyse.model.IResourceObserver;
import analyse.model.IResource;
import analyse.model.Signal;
import analyse.model.Subject;
import analyse.preferences.AnalysePreferences;
import analyse.resources.CPlatform;
import analyse.resources.ColorsUtils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

@SuppressWarnings("deprecation")
public final class SignalsExplorerContainer extends SashForm implements ISelectionChangedListener {

		private TableViewer signalsListViewer;
		private CLabel subjectNamelabel;
		private CLabel sampleFrequencyValuelabel;
		private CLabel nbTrialsValuelabel;
		private Combo selectedTrialValueCombo;
		private CLabel frontCutValueLabel;
		private CLabel endCutValueLabel;
		private CLabel nbSamplesValueLabel;
		private CLabel durationValueLabel;
		private Combo selectedFieldValueCombo;
		private Combo selectedTrialFieldValueCombo;
		private CLabel selectedTrialFieldValueValueLabel;
		private Combo selectedMarkerGroupValueCombo;
		private Combo selectedMarkerGroupTrialValueCombo;
		private CLabel xMarkerValueLabel;
		private CLabel yMarkerValueLabel;
		private CTabFolder trialsFielsMarkersTabFolder;
		private CTabItem trialsTabItem;
		private CTabItem fieldsTabItem;
		private CTabItem markersTabItem;
		private ActionContributionItem deleteFieldMarkersAction;
		private ActionContributionItem deleteMarkerAction;
		private Combo markersGroupGraphicalSymbolCombo;
		
		private class NextTrialAction extends Action {
			public NextTrialAction() {
				setToolTipText(Messages.getString("ChannelsView.NextTrialActionTooltip"));
				setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.NEXT_TRIAL_ICON));
			}
			@Override
			public void run() {
				nextTrial();
			}
		}
		
		private class PreviousTrialAction extends Action {
			public PreviousTrialAction() {
				setToolTipText(Messages.getString("ChannelsView.PreviousTrialActionTooltip"));
				setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.PREVIOUS_TRIAL_ICON));
			}
			@Override
			public void run() {
				previousTrial();
			}
		}
		
		private class DeleteFieldMarkersAction extends Action {
			public DeleteFieldMarkersAction() {
				//setText(Messages.getString("CloseEditorAction.Title")); 
				setToolTipText(Messages.getString("ChannelsView.SignalsItemFieldsMarkersTabItemDeleteSelectedtFieldMarkersButtonTitle"));
				setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.REMOVE_FIELD_MARKERS_GROUP_ICON));
			}
			@Override
			public void run() {
				deleteFieldMarkers();
			}
		}
		
		private class DeleteMarkerAction extends Action {
			public DeleteMarkerAction() {
				//setText(Messages.getString("CloseEditorAction.Title")); 
				setToolTipText(Messages.getString("ChannelsView.SignalsItemMarkersTabItemDeleteMarkerLabelTitle"));
				setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.REMOVE_MARKER_TRIAL_ICON));
			}
			@Override
			public void run() {
				deleteMarker();
			}
		}

		public SignalsExplorerContainer(Composite parent) {
			super(parent, SWT.VERTICAL);
			createContent();
			setSashWidth(5);
			int sashHeight = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.SIGNALS_VIEW_SASH_HEIGHT);
			setWeights(new int[]{sashHeight, 100 - sashHeight});
		}
		
		public void deleteMarker() {
			if(trialsFielsMarkersTabFolder.getSelection() == markersTabItem) {
				int selectedMarkerGroup = selectedMarkerGroupValueCombo.getSelectionIndex();
				int selectedTrial = selectedMarkerGroupTrialValueCombo.getSelectionIndex();
				if(selectedMarkerGroup > -1 && selectedTrial > -1){
					String signalName  = signalsListViewer.getTable().getItem(signalsListViewer.getTable().getSelectionIndex()).getText();
					String fullSignalName = ((Subject)signalsListViewer.getInput()).getLocalPath() + "." + signalName;
					if(MessageDialog.openQuestion(getShell(), Messages.getString("ChannelsView.MessageDialogDeleteMarkerTitle"), Messages.getString("ChannelsView.MessageDialogDeleteMarkerText"))) {
						if(!MathEngineFactory.getInstance().getMathEngine().deleteMarker(selectedMarkerGroup + 1, selectedTrial + 1, fullSignalName))
							Log.logErrorMessage(Messages.getString("SignalsExplorerContainer.ImpossibleDeleteMarker"));
						else ((Subject)signalsListViewer.getInput()).setModified(true);
						Experiments.notifyObservers(IResourceObserver.MARKER_DELETED,  new IResource[]{(Subject)signalsListViewer.getInput()});
					}
				}
			}
		}

		public void deleteFieldMarkers() {
			if(trialsFielsMarkersTabFolder.getSelection() == fieldsTabItem) {
				int selectedField = selectedFieldValueCombo.getSelectionIndex();
				if(signalsListViewer.getTable().getSelectionIndex() > -1) {
					String signalName  = signalsListViewer.getTable().getItem(signalsListViewer.getTable().getSelectionIndex()).getText();
					String fullSignalName = ((Subject)signalsListViewer.getInput()).getLocalPath() + "." + signalName;
					if(selectedField > -1) {
						if(MessageDialog.openQuestion(getShell(), Messages.getString("ChannelsView.MessageDialogDeleteFieldTitle"), Messages.getString("ChannelsView.MessageDialogDeleteFieldText"))) {
							if(!MathEngineFactory.getInstance().getMathEngine().deleteField(selectedField + 1, fullSignalName))
								Log.logErrorMessage(Messages.getString("SignalsExplorerContainer.ImpossibleDeleteField"));
							else ((Subject)signalsListViewer.getInput()).setModified(true);
							Experiments.notifyObservers(IResourceObserver.FIELD_DELETED,  new IResource[]{(Subject)signalsListViewer.getInput()});
						}
					}
				}
			}
			if(trialsFielsMarkersTabFolder.getSelection() == markersTabItem) {
				int selectedMarker = selectedMarkerGroupValueCombo.getSelectionIndex();
				if(signalsListViewer.getTable().getSelectionIndex() > -1) {
					String signalName  = signalsListViewer.getTable().getItem(signalsListViewer.getTable().getSelectionIndex()).getText();
					String fullSignalName = ((Subject)signalsListViewer.getInput()).getLocalPath() + "." + signalName;
					if(selectedMarker > -1) {
						if(MessageDialog.openQuestion(getShell(), Messages.getString("ChannelsView.MessageDialogDeleteMarkerGroupTitle"), Messages.getString("ChannelsView.MessageDialogDeleteMarkerGroupText"))) {
							if(!MathEngineFactory.getInstance().getMathEngine().deleteMarkersGroup(selectedMarker + 1, fullSignalName))
								Log.logErrorMessage(Messages.getString("SignalsExplorerContainer.ImpossibleDeleteMarkersGroup"));
							else ((Subject)signalsListViewer.getInput()).setModified(true);
							Experiments.notifyObservers(IResourceObserver.MARKERS_GROUP_DELETED,  new IResource[]{(Subject)signalsListViewer.getInput()});
						}
					}
				}
			}
		}

		public void nextTrial() {
			if(trialsFielsMarkersTabFolder.getSelection() == trialsTabItem) {
				int nbTrials = selectedTrialValueCombo.getItemCount();
				int selectedTrial = selectedTrialValueCombo.getSelectionIndex();
				if(nbTrials > 0 && selectedTrial < nbTrials)  selectedTrialValueCombo.select(++selectedTrial);
			}
			if(trialsFielsMarkersTabFolder.getSelection() == fieldsTabItem) {
				int nbTrials = selectedTrialFieldValueCombo.getItemCount();
				int selectedTrial = selectedTrialFieldValueCombo.getSelectionIndex();
				if(nbTrials > 0 && selectedTrial < nbTrials)  selectedTrialFieldValueCombo.select(++selectedTrial);
				
			}
			if(trialsFielsMarkersTabFolder.getSelection() == markersTabItem) {
				int nbTrials = selectedMarkerGroupTrialValueCombo.getItemCount();
				int selectedTrial = selectedMarkerGroupTrialValueCombo.getSelectionIndex();
				if(nbTrials > 0 && selectedTrial < nbTrials)  selectedMarkerGroupTrialValueCombo.select(++selectedTrial);
				
			}
		}

		public void previousTrial() {
			if(trialsFielsMarkersTabFolder.getSelection() == trialsTabItem) {
				int nbTrials = selectedTrialValueCombo.getItemCount();
				int selectedTrial = selectedTrialValueCombo.getSelectionIndex();
				if(nbTrials > 0 && selectedTrial > 0)  selectedTrialValueCombo.select(--selectedTrial);
			}
			if(trialsFielsMarkersTabFolder.getSelection() == fieldsTabItem) {
				int nbTrials = selectedTrialFieldValueCombo.getItemCount();
				int selectedTrial = selectedTrialFieldValueCombo.getSelectionIndex();
				if(nbTrials > 0 && selectedTrial > 0)  selectedTrialFieldValueCombo.select(--selectedTrial);
			}
			if(trialsFielsMarkersTabFolder.getSelection() == markersTabItem) {
				int nbTrials = selectedMarkerGroupTrialValueCombo.getItemCount();
				int selectedTrial = selectedMarkerGroupTrialValueCombo.getSelectionIndex();
				if(nbTrials > 0 && selectedTrial > 0)  selectedMarkerGroupTrialValueCombo.select(--selectedTrial);
			}
		}

		public void update(int message, IResource resource) {
			Subject subject = null;
			if(resource instanceof Subject) subject = (Subject) resource;
			if(message == IResourceObserver.LOADED ||
			   message == IResourceObserver.DELETED ||
			   message == IResourceObserver.SELECTION_CHANGED) {
				
				subjectNamelabel.setText(Messages.getString("NONE"));
				sampleFrequencyValuelabel.setText("");			
				nbTrialsValuelabel.setText("");
				
				selectedTrialValueCombo.removeAll();
				frontCutValueLabel.setText("");
				endCutValueLabel.setText("");
				nbSamplesValueLabel.setText("");
				durationValueLabel.setText("");
				
				selectedFieldValueCombo.removeAll();
				selectedTrialFieldValueCombo.removeAll();
				selectedTrialFieldValueValueLabel.setText("");
				
				selectedMarkerGroupValueCombo.removeAll();
				selectedMarkerGroupTrialValueCombo.removeAll();
				markersGroupGraphicalSymbolCombo.removeAll();
				xMarkerValueLabel.setText("");
				yMarkerValueLabel.setText("");
				
				for (int i = 0; i < signalsListViewer.getTable().getItemCount(); i++) {
					TableItem item = signalsListViewer.getTable().getItem(i);
					item.setFont(JFaceResources.getFont("MY_TEXT_FONT"));
				}
				
				if(subject != null)	subjectNamelabel.setText(subject.getLocalPath() + (subject.isLoaded()?"":" (" + Messages.getString("NotLoaded") + ")"));
//				long t1 = System.currentTimeMillis();
				signalsListViewer.setInput(subject);
//				t1 = System.currentTimeMillis() - t1;
//				System.out.println(">>>>>>>>>>>>>>>>>> Time to signalsListViewer.setInput : " + t1);
				
			} else if(message == IResourceObserver.RENAMED) {
				subjectNamelabel.setText(Messages.getString("NONE"));
				if(subject != null)	subjectNamelabel.setText(subject.getLocalPath() + (subject.isLoaded()?"":" (" + Messages.getString("NotLoaded") + ")"));
			} else if(message == IResourceObserver.CHANNEL_DELETED) {
				subjectNamelabel.setText(Messages.getString("NONE"));
				sampleFrequencyValuelabel.setText("");			
				nbTrialsValuelabel.setText("");
				
				selectedTrialValueCombo.removeAll();
				frontCutValueLabel.setText("");
				endCutValueLabel.setText("");
				nbSamplesValueLabel.setText("");
				durationValueLabel.setText("");
				
				selectedFieldValueCombo.removeAll();
				selectedTrialFieldValueCombo.removeAll();
				selectedTrialFieldValueValueLabel.setText("");
				
				selectedMarkerGroupValueCombo.removeAll();
				selectedMarkerGroupTrialValueCombo.removeAll();
				xMarkerValueLabel.setText("");
				yMarkerValueLabel.setText("");

				signalsListViewer.refresh();
				signalsListViewer.getTable().select(0);
				selectionChanged(new SelectionChangedEvent(signalsListViewer ,signalsListViewer.getSelection()));
			} else if(message == IResourceObserver.MARKER_DELETED ||
					  message == IResourceObserver.MARKER_ADDED ||
					  message == IResourceObserver.MARKERS_GROUP_DELETED ||
					  message == IResourceObserver.MARKERS_GROUP_SYMBOL_CHANGED) {
				resetMarkersInformations();
			} else if(message == IResourceObserver.FIELD_DELETED) {
				resetFieldsInformations();
			}  else if(message == IResourceObserver.PROCESS_RUN) {
				signalsListViewer.refresh();
				selectionChanged(new SelectionChangedEvent(signalsListViewer ,signalsListViewer.getSelection()));
			}
		}

		private void createContent() {
			Composite topContainer = new Composite(this, SWT.BORDER);
			topContainer.addControlListener(new ControlListener() {
				public void controlResized(ControlEvent e) {
					AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.SIGNALS_VIEW_SASH_HEIGHT, SignalsExplorerContainer.this.getWeights()[0]/10);
				}
				public void controlMoved(ControlEvent e) {
				}
			});
			topContainer.setLayout(new GridLayout(3,false));
			
			CLabel selectedSubjectlabel = new CLabel(topContainer, SWT.NONE);
			selectedSubjectlabel.setText(Messages.getString("SelectedSubject"));
			selectedSubjectlabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			subjectNamelabel = new CLabel(topContainer, SWT.NONE);
			subjectNamelabel.setText(Messages.getString("NONE"));
			subjectNamelabel.setToolTipText(Messages.getString("SelectedSubjectToolTip"));
			subjectNamelabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false, 2, 1));
			
			signalsListViewer = new TableViewer(topContainer, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
			signalsListViewer.getTable().addFocusListener((FocusListener) getParent());
			signalsListViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			signalsListViewer.setContentProvider(new IStructuredContentProvider() {
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}
				public void dispose() {
				}
				public Object[] getElements(Object inputElement) {
					if(inputElement instanceof Subject) {
						Subject subject = (Subject)inputElement;
						if(subject.isLoaded()) {
//							long t1 = System.currentTimeMillis();
							String[] signalsNames = subject.getSignalsNames();//MathEngineFactory.getInstance().getMathEngine().getSignalsNames(subject.getLocalPath());
//							t1 = System.currentTimeMillis() - t1;
//							System.out.println(">>>>>>>>>>>>>>>>>>>>>> Time to getSignalsNames " + t1);
							return signalsNames;
						}
					}
					return new String[0];
				}
			});
			signalsListViewer.setLabelProvider(new ILabelProvider() {
				public void removeListener(ILabelProviderListener listener) {
				}
				public boolean isLabelProperty(Object element, String property) {
					return false;
				}
				public void dispose() {
				}
				public void addListener(ILabelProviderListener listener) {
				}
				public String getText(Object element) {
					return (String) element;
				}
				public Image getImage(Object element) {
					return null;
				}
			});
			signalsListViewer.addSelectionChangedListener(this);
			signalsListViewer.addSelectionChangedListener((ISelectionChangedListener)ChannelsView.deleteChannelsAction.getAction());
			signalsListViewer.setSorter(new ViewerSorter());
			signalsListViewer.setCellModifier(new ICellModifier() {
				public void modify(Object element, String property, Object value) {
					TableItem tableItem = (TableItem) element;
					String oldName = tableItem.getText();
					String newName = (String)value;
					if(!oldName.equals(newName)) {
						String message = Messages.getString("ChannelsView.RenameSignalDialogMessage") + " (" + oldName + " -> " + newName + ")";
						if(MessageDialog.openConfirm(signalsListViewer.getTable().getShell(), Messages.getString("ChannelsView.RenameSignalDialogTitle"), message)) {
							if(newName.matches("^[a-zA-Z][a-zA-Z0-9_]{0,31}$")){
								Subject subject = ((Subject)signalsListViewer.getInput());
								Signal signal = (Signal) subject.getFirstResourceByName(subject.getLocalPath() + "." + oldName);
								signal.rename(newName);
								Experiments.notifyObservers(IResourceObserver.RENAMED, new IResource[]{signal});
								signalsListViewer.refresh();
							} else {
								MessageDialog.openError(signalsListViewer.getTable().getShell(), Messages.getString("ChannelsView.RenameSignalErrorDialogTitle"), Messages.getString("ChannelsView.RenameSignalErrorDialogMessage") + newName);
							}
						}
					}
				}
				public Object getValue(Object element, String property) {
					return element;
				}
				public boolean canModify(Object arg0, String arg1) {
					return true;
				}
			});
			signalsListViewer.setCellEditors(new CellEditor[]{new TextCellEditor(signalsListViewer.getTable())});
			signalsListViewer.setColumnProperties(new String[]{"SignalName"});
			
			ColumnViewerEditorActivationStrategy columnViewerEditorActivationStrategy = new ColumnViewerEditorActivationStrategy(signalsListViewer) {
				@Override
				protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
					return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL || 
						   event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION || 
						   event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
				}
			};
			
			TableViewerEditor.create(signalsListViewer, columnViewerEditorActivationStrategy, ColumnViewerEditor.TABBING_HORIZONTAL
					| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL| ColumnViewerEditor.KEYBOARD_ACTIVATION);
			
			CLabel sampleFrequencylabel = new CLabel(topContainer, SWT.NONE);
			sampleFrequencylabel.setText(Messages.getString("ChannelsView.SignalsItemFrequencyLabelTitle"));
			sampleFrequencylabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			sampleFrequencyValuelabel = new CLabel(topContainer, SWT.NONE);
			sampleFrequencyValuelabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			Button button = new Button(topContainer, SWT.PUSH);
			button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			button.setText("Modify...");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ModifySampleFrequencyDialog msfDialog = new ModifySampleFrequencyDialog(getShell());
					if(msfDialog.open() == Dialog.OK) {
						
					}
				}
			});
			
			
			CLabel nbTrialslabel = new CLabel(topContainer, SWT.NONE);
			nbTrialslabel.setText(Messages.getString("ChannelsView.TrialsNumLabelTitle"));
			nbTrialslabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			nbTrialsValuelabel = new CLabel(topContainer, SWT.NONE);
			nbTrialsValuelabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false, 2, 1));
			
			trialsFielsMarkersTabFolder = new CTabFolder(this, SWT.NONE);
			trialsFielsMarkersTabFolder.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
			trialsFielsMarkersTabFolder.setTabHeight(CPlatform.isLinux()?25:22);
			trialsFielsMarkersTabFolder.setBorderVisible(true);
			trialsFielsMarkersTabFolder.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				}
				public void focusGained(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
//					((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
//					((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				}
			});
			trialsFielsMarkersTabFolder.addMouseListener(new MouseListener() {
				public void mouseUp(MouseEvent e) {
				}
				public void mouseDown(MouseEvent e) {
					trialsFielsMarkersTabFolder.setFocus();
				}
				public void mouseDoubleClick(MouseEvent e) {
				}
			});
			trialsFielsMarkersTabFolder.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					CTabItem tabItem = trialsFielsMarkersTabFolder.getSelection();
					deleteFieldMarkersAction.getAction().setEnabled(tabItem == fieldsTabItem || tabItem == markersTabItem);
					deleteMarkerAction.getAction().setEnabled(tabItem == markersTabItem);
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			trialsTabItem = new CTabItem(trialsFielsMarkersTabFolder, SWT.NONE);
			trialsTabItem.setText(Messages.getString("ChannelsView.SignalsItemTrialsTabItemTitle"));
			fieldsTabItem = new CTabItem(trialsFielsMarkersTabFolder, SWT.NONE);
			fieldsTabItem.setText(Messages.getString("ChannelsView.SignalsItemFieldsTabItemTitle"));
			markersTabItem = new CTabItem(trialsFielsMarkersTabFolder, SWT.NONE);
			markersTabItem.setText(Messages.getString("ChannelsView.SignalsItemMarkersTabItemTitle"));
			
			deleteFieldMarkersAction= new ActionContributionItem((IAction)(new DeleteFieldMarkersAction()));
			deleteFieldMarkersAction.getAction().setEnabled(false);
			deleteMarkerAction= new ActionContributionItem((IAction)(new DeleteMarkerAction()));
			deleteMarkerAction.getAction().setEnabled(false);
			ActionContributionItem previousTrialAction = new ActionContributionItem((IAction)(new PreviousTrialAction()));
			ActionContributionItem nextTrialAction = new ActionContributionItem((IAction)(new NextTrialAction()));
			
			ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT | SWT.WRAP);
			toolBarManager.add(deleteFieldMarkersAction);
			toolBarManager.add(deleteMarkerAction);
			toolBarManager.add(new Separator());
			toolBarManager.add(previousTrialAction);
			toolBarManager.add(nextTrialAction);
			trialsFielsMarkersTabFolder.setTopRight(toolBarManager.createControl(trialsFielsMarkersTabFolder), SWT.RIGHT);
			
			toolBarManager.getControl().addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
					((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
					((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				}
				public void focusGained(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
					((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
					((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				}
			});
			
			/*******/
			Composite trialsContainer = new Composite(trialsFielsMarkersTabFolder,SWT.NONE);
			trialsContainer.setLayout(new GridLayout(2,false));
			
			CLabel selectedTrialLabel = new CLabel(trialsContainer, SWT.NONE);
			selectedTrialLabel.setText(Messages.getString("ChannelsView.SignalsItemTrialsTabItemSelectTrialLabelTitle"));
			selectedTrialLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			selectedTrialValueCombo = new Combo(trialsContainer, SWT.READ_ONLY);
			selectedTrialValueCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			selectedTrialValueCombo.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
					((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
					((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				}
				public void focusGained(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
					((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
					((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				}
			});
			selectedTrialValueCombo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if(signalsListViewer.getTable().getItemCount() > 0) {
						if(!selectedTrialValueCombo.getText().equals("")) {
							String signalName  = signalsListViewer.getTable().getItem(signalsListViewer.getTable().getSelectionIndex()).getText();
							String fullSignalName = ((Subject)signalsListViewer.getInput()).getLocalPath() + "." + signalName;
							IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
							int value = mathEngine.getFrontCut(fullSignalName, selectedTrialValueCombo.getSelectionIndex() + 1);
							frontCutValueLabel.setText(String.valueOf(value));
							value = mathEngine.getEndCut(fullSignalName, selectedTrialValueCombo.getSelectionIndex() + 1);
							endCutValueLabel.setText(String.valueOf(value));
							value = mathEngine.getNbSamples(fullSignalName, selectedTrialValueCombo.getSelectionIndex() + 1);
							nbSamplesValueLabel.setText(String.valueOf(value));
							double duration = value / mathEngine.getSampleFrequency(fullSignalName);
							durationValueLabel.setText(String.valueOf(duration) + " s");
						}
					}
				}
			});
			
			CLabel frontCutLabel = new CLabel(trialsContainer, SWT.NONE);
			frontCutLabel.setText(Messages.getString("ChannelsView.SignalsItemTrialsTabItemFrontCutLabelTitle"));
			frontCutLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			frontCutValueLabel = new CLabel(trialsContainer, SWT.NONE);
			frontCutValueLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			
			CLabel endCutLabel = new CLabel(trialsContainer, SWT.NONE);
			endCutLabel.setText(Messages.getString("ChannelsView.SignalsItemTrialsTabItemEndCutLabelTitle"));
			endCutLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			endCutValueLabel = new CLabel(trialsContainer, SWT.NONE);
			endCutValueLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			
			CLabel nbSamplesLabel = new CLabel(trialsContainer, SWT.NONE);
			nbSamplesLabel.setText(Messages.getString("ChannelsView.SignalsItemTrialsTabItemSamplesLabelTitle"));
			nbSamplesLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			nbSamplesValueLabel = new CLabel(trialsContainer, SWT.NONE);
			nbSamplesValueLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			
			CLabel durationLabel = new CLabel(trialsContainer, SWT.NONE);
			durationLabel.setText(Messages.getString("ChannelsView.SignalsItemTrialsTabItemDurationLabelTitle"));
			durationLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			durationValueLabel = new CLabel(trialsContainer, SWT.NONE);
			durationValueLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			
			trialsTabItem.setControl(trialsContainer);
			
			/******/
			Composite fieldsContainer = new Composite(trialsFielsMarkersTabFolder,SWT.NONE);
			fieldsContainer.setLayout(new GridLayout(2,false));
			
			CLabel selectedFieldLabel = new CLabel(fieldsContainer, SWT.NONE);
			selectedFieldLabel.setText(Messages.getString("ChannelsView.SignalsItemFieldsTabItemSelectFieldLabelTitle"));
			selectedFieldLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			selectedFieldValueCombo = new Combo(fieldsContainer, SWT.READ_ONLY);
			selectedFieldValueCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			selectedFieldValueCombo.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
					((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
					((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				}
				public void focusGained(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
					((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
					((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				}
			});
			selectedFieldValueCombo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if(signalsListViewer.getTable().getItemCount() > 0) {
						if(!selectedFieldValueCombo.getText().equals("")) {
							String signalName  = signalsListViewer.getTable().getItem(signalsListViewer.getTable().getSelectionIndex()).getText();
							String fullSignalName = ((Subject)signalsListViewer.getInput()).getLocalPath() + "." + signalName;
							IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
							int nbTrials = mathEngine.getNbTrials(fullSignalName);
							selectedTrialFieldValueCombo.removeAll();
							for (int i = 0; i < nbTrials; i++) selectedTrialFieldValueCombo.add(Messages.getString("TrialNumber") + String.valueOf(i+1));
							selectedTrialFieldValueCombo.select(0);
						}
					}
				}
			});
			
			CLabel selectedTrialFieldLabel = new CLabel(fieldsContainer, SWT.NONE);
			selectedTrialFieldLabel.setText(Messages.getString("ChannelsView.SignalsItemFieldsTabItemSelectTrialLabelTitle"));
			selectedTrialFieldLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			selectedTrialFieldValueCombo = new Combo(fieldsContainer, SWT.READ_ONLY);
			selectedTrialFieldValueCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			selectedTrialFieldValueCombo.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
					((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
					((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				}
				public void focusGained(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
					((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
					((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				}
			});
			selectedTrialFieldValueCombo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if(signalsListViewer.getTable().getItemCount() > 0) {
						if(!selectedTrialFieldValueCombo.getText().equals("")) {
							String signalName  = signalsListViewer.getTable().getItem(signalsListViewer.getTable().getSelectionIndex()).getText();
							String fullSignalName = ((Subject)signalsListViewer.getInput()).getLocalPath() + "." + signalName;
							IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
							int fieldNumber = selectedFieldValueCombo.getSelectionIndex() + 1;
							int trialNumber = selectedTrialFieldValueCombo.getSelectionIndex() + 1;
							double value = mathEngine.getFieldValue(fieldNumber, trialNumber, fullSignalName);
							selectedTrialFieldValueValueLabel.setText(String.valueOf(value));
						}
					}
				}
			});
			
			CLabel selectedTrialFieldValueLabel = new CLabel(fieldsContainer, SWT.NONE);
			selectedTrialFieldValueLabel.setText(Messages.getString("ChannelsView.SignalsItemFieldsTabItemSelectedTrialValueLabelTitle"));
			selectedTrialFieldValueLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			selectedTrialFieldValueValueLabel = new CLabel(fieldsContainer, SWT.NONE);
			selectedTrialFieldValueValueLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			
			fieldsTabItem.setControl(fieldsContainer);
			
			/****/
			Composite markersContainer = new Composite(trialsFielsMarkersTabFolder,SWT.NONE);
			markersContainer.setLayout(new GridLayout(2,false));
			
			CLabel selectedMarkerGroupLabel = new CLabel(markersContainer, SWT.NONE);
			selectedMarkerGroupLabel.setText(Messages.getString("ChannelsView.SignalsItemMarkersTabItemSelectGroupLabelTitle"));
			selectedMarkerGroupLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			selectedMarkerGroupValueCombo = new Combo(markersContainer, SWT.READ_ONLY);
			selectedMarkerGroupValueCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			selectedMarkerGroupValueCombo.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
					((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
					((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				}
				public void focusGained(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
					((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
					((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				}
			});
			selectedMarkerGroupValueCombo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if(signalsListViewer.getTable().getItemCount() > 0) {
						if(!selectedMarkerGroupValueCombo.getText().equals("")) {
							String signalName  = signalsListViewer.getTable().getItem(signalsListViewer.getTable().getSelectionIndex()).getText();
							String fullSignalName = ((Subject)signalsListViewer.getInput()).getLocalPath() + "." + signalName;
							IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
							int markersGroupNumber = selectedMarkerGroupValueCombo.getSelectionIndex() + 1;
							String markersGroupLabel = mathEngine.getMarkersGroupLabel(markersGroupNumber, fullSignalName);
							String symbolName = mathEngine.getMarkersGroupGraphicalSymbol(markersGroupNumber, fullSignalName);
							if(symbolName == null) symbolName = MARKER_GRAPHIC_SYMBOL.getDefault().toString();
							markersGroupGraphicalSymbolCombo.removeAll();
							MARKER_GRAPHIC_SYMBOL[] graphicSymbolsValues = ChartMarker.MARKER_GRAPHIC_SYMBOL.values();
							for (int i = 0; i < graphicSymbolsValues.length; i++) {
								markersGroupGraphicalSymbolCombo.add(graphicSymbolsValues[i].toString());
								markersGroupGraphicalSymbolCombo.setData(graphicSymbolsValues[i].toString(), graphicSymbolsValues[i]);
							}
							markersGroupGraphicalSymbolCombo.select(markersGroupGraphicalSymbolCombo.indexOf(symbolName));
							int[] trialsList = mathEngine.getTrialsListForMarkersGroup(markersGroupNumber, fullSignalName);
							selectedMarkerGroupTrialValueCombo.removeAll();
							for (int i = 0; i < trialsList.length; i++) selectedMarkerGroupTrialValueCombo.add(markersGroupLabel + " - " + Messages.getString("TrialNumber") + trialsList[i]);
							selectedMarkerGroupTrialValueCombo.select(0);
						}
					}
				}
			});
			
			CLabel markersGroupGraphicalSymbolLabel = new CLabel(markersContainer, SWT.NONE);
			markersGroupGraphicalSymbolLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			markersGroupGraphicalSymbolLabel.setText(Messages.getString("ChannelsView.GraphicalSymbolLabelTitle"));
			markersGroupGraphicalSymbolCombo = new Combo(markersContainer, SWT.READ_ONLY);
			markersGroupGraphicalSymbolCombo.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
			markersGroupGraphicalSymbolCombo.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent event) {
					if(selectedMarkerGroupValueCombo.getText() != null) ;
						int markersGroupNumber = selectedMarkerGroupValueCombo.getSelectionIndex() + 1;
						String signalName  = signalsListViewer.getTable().getItem(signalsListViewer.getTable().getSelectionIndex()).getText();
						String fullSignalName = ((Subject)signalsListViewer.getInput()).getLocalPath() + "." + signalName;
						String symbolName = ((MARKER_GRAPHIC_SYMBOL) markersGroupGraphicalSymbolCombo.getData(markersGroupGraphicalSymbolCombo.getText())).toString();
						MathEngineFactory.getInstance().getMathEngine().setMarkersGroupGraphicalSymbol(markersGroupNumber, fullSignalName, symbolName);
						((Subject)signalsListViewer.getInput()).setModified(true);
						Experiments.notifyObservers(IResourceObserver.MARKERS_GROUP_SYMBOL_CHANGED, new IResource[]{(Subject)signalsListViewer.getInput()});
				}
				public void widgetDefaultSelected(SelectionEvent event) {
				}
			});
			
			CLabel selectedMarkerGroupTrialLabel = new CLabel(markersContainer, SWT.NONE);
			selectedMarkerGroupTrialLabel.setText(Messages.getString("ChannelsView.SignalsItemMarkersTabItemSelectTrialLabelTitle"));
			selectedMarkerGroupTrialLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			selectedMarkerGroupTrialValueCombo = new Combo(markersContainer, SWT.READ_ONLY);
			selectedMarkerGroupTrialValueCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			selectedMarkerGroupTrialValueCombo.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
					((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
					((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				}
				public void focusGained(FocusEvent e) {
					trialsFielsMarkersTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
					trialsFielsMarkersTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
					((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
					((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				}
			});
			selectedMarkerGroupTrialValueCombo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if(signalsListViewer.getTable().getItemCount() > 0) {
						if(!selectedMarkerGroupTrialValueCombo.getText().equals("")) {
							String signalName  = signalsListViewer.getTable().getItem(signalsListViewer.getTable().getSelectionIndex()).getText();
							String fullSignalName = ((Subject)signalsListViewer.getInput()).getLocalPath() + "." + signalName;
							IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
							int markersGroupNumber = selectedMarkerGroupValueCombo.getSelectionIndex() + 1;
							double x = mathEngine.getXValueForMarkersGroup(markersGroupNumber, selectedMarkerGroupTrialValueCombo.getSelectionIndex(), fullSignalName);
							double y = mathEngine.getYValueForMarkersGroup(markersGroupNumber, selectedMarkerGroupTrialValueCombo.getSelectionIndex(), fullSignalName);
							xMarkerValueLabel.setText(String.valueOf(x));
							yMarkerValueLabel.setText(String.valueOf(y));
						}
					}
				}
			});
			
			CLabel xMarkerLabel = new CLabel(markersContainer, SWT.NONE);
			xMarkerLabel.setText(Messages.getString("ChannelsView.SignalsItemMarkersTabItemXMarkerValueLabelTitle"));
			xMarkerLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			xMarkerValueLabel = new CLabel(markersContainer, SWT.NONE);
			xMarkerValueLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			
			CLabel yMarkerLabel = new CLabel(markersContainer, SWT.NONE);
			yMarkerLabel.setText(Messages.getString("ChannelsView.SignalsItemMarkersTabItemYMarkerValueLabelTitle"));
			yMarkerLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			yMarkerValueLabel = new CLabel(markersContainer, SWT.NONE);
			yMarkerValueLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			
			markersTabItem.setControl(markersContainer);
			
			/***/
			trialsFielsMarkersTabFolder.setSelection(trialsTabItem);
		}

		public void selectionChanged(SelectionChangedEvent event) {
			if(signalsListViewer.getTable().getSelectionIndex() > -1) {
				TableItem selectedItem = signalsListViewer.getTable().getItem(signalsListViewer.getTable().getSelectionIndex());
				String signalName  = selectedItem.getText();
				String fullSignalName = ((Subject)signalsListViewer.getInput()).getLocalPath() + "." + signalName;
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				double sf = mathEngine.getSampleFrequency(fullSignalName);
				sampleFrequencyValuelabel.setText(String.valueOf(sf) + " Hz");
				int nbTrials = mathEngine.getNbTrials(fullSignalName);
				nbTrialsValuelabel.setText(String.valueOf(nbTrials));
				selectedTrialValueCombo.removeAll();
				for (int i = 0; i < nbTrials; i++) selectedTrialValueCombo.add(Messages.getString("TrialNumber") + String.valueOf(i+1));
				selectedTrialValueCombo.select(0);

				resetFieldsInformations();
				resetMarkersInformations();
				
				for (int i = 0; i < signalsListViewer.getTable().getItemCount(); i++) {
					TableItem item = signalsListViewer.getTable().getItem(i);
					if(selectedItem == item) item.setFont(JFaceResources.getFont("MY_SELECTED_TEXT_FONT"));
					else item.setFont(JFaceResources.getFont("MY_TEXT_FONT"));
				}
			}
		}
		
		private void resetMarkersInformations() {
			if(signalsListViewer.getTable().getSelectionIndex() > -1) {
				TableItem selectedItem = signalsListViewer.getTable().getItem(signalsListViewer.getTable().getSelectionIndex());
				String signalName  = selectedItem.getText();
				String fullSignalName = ((Subject)signalsListViewer.getInput()).getLocalPath() + "." + signalName;
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				selectedMarkerGroupValueCombo.removeAll();
				String[] markersGroupsNames = mathEngine.getMarkersGroupsLabels(fullSignalName);
				for (int i = 0; i < markersGroupsNames.length; i++) selectedMarkerGroupValueCombo.add(markersGroupsNames[i] + " - " + Messages.getString("MarkersGroupNumber") + (i+1));
				selectedMarkerGroupTrialValueCombo.removeAll();
				markersGroupGraphicalSymbolCombo.removeAll();
				xMarkerValueLabel.setText("");
				yMarkerValueLabel.setText("");
				selectedMarkerGroupValueCombo.select(0);
			}
			
		}
		
		private void resetFieldsInformations() {
			if(signalsListViewer.getTable().getSelectionIndex() > -1) {
				TableItem selectedItem = signalsListViewer.getTable().getItem(signalsListViewer.getTable().getSelectionIndex());
				String signalName  = selectedItem.getText();
				String fullSignalName = ((Subject)signalsListViewer.getInput()).getLocalPath() + "." + signalName;
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				selectedFieldValueCombo.removeAll();
				String[] fieldsNames = mathEngine.getFieldsLabels(fullSignalName);
				selectedFieldValueCombo.setItems(fieldsNames);
				selectedTrialFieldValueCombo.removeAll();
				selectedTrialFieldValueValueLabel.setText("");
				selectedFieldValueCombo.select(0);
			}
		}
	}
