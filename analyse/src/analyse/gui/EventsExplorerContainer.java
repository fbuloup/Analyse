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

import java.util.Arrays;

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import analyse.Log;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Subject;
import analyse.preferences.AnalysePreferences;
import analyse.resources.CPlatform;
import analyse.resources.ColorsUtils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

@SuppressWarnings("deprecation")
public class EventsExplorerContainer extends SashForm implements ISelectionChangedListener {


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
	
	private class DeleteEventAction extends Action {
		public DeleteEventAction() {
			//setText(Messages.getString("CloseEditorAction.Title")); 
			setToolTipText(Messages.getString("ChannelsView.SignalsItemEventsTabItemDeleteSelectedtEventButtonTitle"));
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.REMOVE_MARKER_TRIAL_ICON));
			setEnabled(false);
		}
		@Override
		public void run() {
			deleteEvent();
		}
	}
	
	
	private CLabel subjectNamelabel;
	private TableViewer eventsListViewer;
	private CTabItem trialsTabItem;
	private CTabFolder trialsEventsTabFolder;
	private ActionContributionItem deleteEventAction;
	private Combo selectedTrialValueCombo;
	private CLabel timeValueLabel;
	private CLabel amplitudeValueLabel;
	private CLabel widthValueLabel;
	private CLabel criteriaValueLabel;


	public EventsExplorerContainer(Composite parent) {
		super(parent, SWT.VERTICAL);
		createContent();
		setSashWidth(5);
		int sashHeight = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.EVENTS_VIEW_SASH_HEIGHT);
		setWeights(new int[]{sashHeight, 100 - sashHeight});
	}

	
	public void nextTrial() {
		if(trialsEventsTabFolder.getSelection() == trialsTabItem) {
			int nbTrials = selectedTrialValueCombo.getItemCount();
			int selectedTrial = selectedTrialValueCombo.getSelectionIndex();
			if(nbTrials > 0 && selectedTrial < nbTrials)  selectedTrialValueCombo.select(++selectedTrial);
		}
	}

	public void previousTrial() {
		if(trialsEventsTabFolder.getSelection() == trialsTabItem) {
			int nbTrials = selectedTrialValueCombo.getItemCount();
			int selectedTrial = selectedTrialValueCombo.getSelectionIndex();
			if(nbTrials > 0 && selectedTrial > 0)  selectedTrialValueCombo.select(--selectedTrial);
		}
	}
	
	public void deleteEvent() {
		if(trialsEventsTabFolder.getSelection() == trialsTabItem) {
			int selectedEvent = eventsListViewer.getTable().getSelectionIndex();
			int selectedLine = selectedTrialValueCombo.getSelectionIndex();
			if(selectedEvent > -1 && selectedLine > -1){
				String eventName  = eventsListViewer.getTable().getItem(selectedEvent).getText();
				String fullEventName = ((Subject)eventsListViewer.getInput()).getLocalPath() + "." + eventName;
				if(MessageDialog.openQuestion(getShell(), Messages.getString("ChannelsView.MessageDialogDeleteEventTitle"), Messages.getString("ChannelsView.MessageDialogDeletEventText"))) {
					if(!MathEngineFactory.getInstance().getMathEngine().deleteEvent(selectedLine + 1, fullEventName))
						Log.logErrorMessage(Messages.getString("EventsExplorerContainer.ImpossibleDeleteEvent"));
					else ((Subject)eventsListViewer.getInput()).setModified(true);
					Experiments.notifyObservers(IResourceObserver.EVENT_DELETED,  new IResource[]{(Subject)eventsListViewer.getInput()});
				}
			}
		}
	}
	
	private void createContent() {
		Composite topContainer = new Composite(this, SWT.BORDER);
		topContainer.addControlListener(new ControlListener() {
			public void controlResized(ControlEvent e) {
				AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.EVENTS_VIEW_SASH_HEIGHT, EventsExplorerContainer.this.getWeights()[0]/10);
			}
			public void controlMoved(ControlEvent e) {
			}
		});
		topContainer.setLayout(new GridLayout(2,false));
		
		CLabel selectedSubjectlabel = new CLabel(topContainer, SWT.NONE);
		selectedSubjectlabel.setText(Messages.getString("SelectedSubject"));
		selectedSubjectlabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
		subjectNamelabel = new CLabel(topContainer, SWT.NONE);
		subjectNamelabel.setText(Messages.getString("NONE"));
		subjectNamelabel.setToolTipText(Messages.getString("SelectedSubjectToolTip"));
		subjectNamelabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		eventsListViewer = new TableViewer(topContainer, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
		eventsListViewer.getTable().addFocusListener((FocusListener) getParent());
		eventsListViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		eventsListViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				if(inputElement instanceof Subject) {
					Subject subject = (Subject)inputElement;
					if(subject.isLoaded()) {
//						long t1 = System.currentTimeMillis();
						String[] eventsNames = subject.getEventsNames();//MathEngineFactory.getInstance().getMathEngine().getSignalsNames(subject.getLocalPath());
//						t1 = System.currentTimeMillis() - t1;
//						System.out.println(">>>>>>>>>>>>>>>>>>>>>> Time to getSignalsNames " + t1);
						return eventsNames;
					}
				}
				return new String[0];
			}
		});
		eventsListViewer.setLabelProvider(new ILabelProvider() {
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
		eventsListViewer.addSelectionChangedListener(this);
		eventsListViewer.addSelectionChangedListener((ISelectionChangedListener)ChannelsView.deleteChannelsAction.getAction());
		eventsListViewer.setSorter(new ViewerSorter());
		
		trialsEventsTabFolder = new CTabFolder(this, SWT.NONE);
		trialsEventsTabFolder.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
		trialsEventsTabFolder.setTabHeight(CPlatform.isLinux()?25:22);
		trialsEventsTabFolder.setBorderVisible(true);
		trialsEventsTabFolder.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				trialsEventsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				trialsEventsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
			}
			public void focusGained(FocusEvent e) {
				trialsEventsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				trialsEventsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
//				((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
//				((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
			}
		});
		trialsEventsTabFolder.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
				trialsEventsTabFolder.setFocus();
			}
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		
		
		deleteEventAction= new ActionContributionItem((IAction)(new DeleteEventAction()));
		deleteEventAction.getAction().setEnabled(false);
		ActionContributionItem previousTrialAction = new ActionContributionItem((IAction)(new PreviousTrialAction()));
		ActionContributionItem nextTrialAction = new ActionContributionItem((IAction)(new NextTrialAction()));
		
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT | SWT.WRAP);
		toolBarManager.add(deleteEventAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(previousTrialAction);
		toolBarManager.add(nextTrialAction);
		trialsEventsTabFolder.setTopRight(toolBarManager.createControl(trialsEventsTabFolder), SWT.RIGHT);
		
		toolBarManager.getControl().addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				trialsEventsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				trialsEventsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
			}
			public void focusGained(FocusEvent e) {
				trialsEventsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				trialsEventsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
			}
		});
		
		trialsTabItem = new CTabItem(trialsEventsTabFolder, SWT.NONE);
		trialsTabItem.setText(Messages.getString("ChannelsView.SignalsItemTrialsTabItemTitle"));

		Composite trialsContainer = new Composite(trialsEventsTabFolder,SWT.NONE);
		trialsContainer.setLayout(new GridLayout(2,false));
		trialsTabItem.setControl(trialsContainer);
		
		CLabel criteriaLabel = new CLabel(trialsContainer, SWT.NONE);
		criteriaLabel.setText(Messages.getString("ChannelsView.EventsTrialsTabItemCriteriaLabelTitle"));
		criteriaLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
		criteriaValueLabel = new CLabel(trialsContainer, SWT.NONE);
		criteriaValueLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		CLabel selectedTrialLabel = new CLabel(trialsContainer, SWT.NONE);
		selectedTrialLabel.setText(Messages.getString("ChannelsView.SignalsItemTrialsTabItemSelectTrialLabelTitle"));
		selectedTrialLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
		selectedTrialValueCombo = new Combo(trialsContainer, SWT.READ_ONLY);
		selectedTrialValueCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		selectedTrialValueCombo.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				trialsEventsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				trialsEventsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
			}
			public void focusGained(FocusEvent e) {
				trialsEventsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				trialsEventsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				((View)getParent()).setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				((View)getParent()).setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
			}
		});
		selectedTrialValueCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(eventsListViewer.getTable().getItemCount() > 0) {
					if(!selectedTrialValueCombo.getText().equals("")) {
						String eventName  = eventsListViewer.getTable().getItem(eventsListViewer.getTable().getSelectionIndex()).getText();
						String fullEventName = ((Subject)eventsListViewer.getInput()).getLocalPath() + "." + eventName;
						IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
						double[][] values = mathEngine.getEventsGroupValues(fullEventName);
						double value = values[selectedTrialValueCombo.getSelectionIndex()][1];
						timeValueLabel.setText(String.valueOf(value));
						value = values[selectedTrialValueCombo.getSelectionIndex()][2];
						amplitudeValueLabel.setText(String.valueOf(value));
						value = values[selectedTrialValueCombo.getSelectionIndex()][3];
						widthValueLabel.setText(String.valueOf(value));
					}
				}
			}
		});
		
		CLabel timeLabel = new CLabel(trialsContainer, SWT.NONE);
		timeLabel.setText(Messages.getString("ChannelsView.EventsTrialsTabItemTimeLabelTitle"));
		timeLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
		timeValueLabel = new CLabel(trialsContainer, SWT.NONE);
		timeValueLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		CLabel amplitudeLabel = new CLabel(trialsContainer, SWT.NONE);
		amplitudeLabel.setText(Messages.getString("ChannelsView.EventsTrialsTabItemAmplitudeLabelTitle"));
		amplitudeLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
		amplitudeValueLabel = new CLabel(trialsContainer, SWT.NONE);
		amplitudeValueLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		CLabel widthLabel = new CLabel(trialsContainer, SWT.NONE);
		widthLabel.setText(Messages.getString("ChannelsView.EventsTrialsTabItemWidthLabelTitle"));
		widthLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
		widthValueLabel = new CLabel(trialsContainer, SWT.NONE);
		widthValueLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		trialsEventsTabFolder.setSelection(trialsTabItem);
		
	}

	public void update(int message, Subject subject) {
		if(message == IResourceObserver.LOADED ||
		   message == IResourceObserver.DELETED ||
		   message == IResourceObserver.SELECTION_CHANGED) {
			
			subjectNamelabel.setText(Messages.getString("NONE"));
			
			selectedTrialValueCombo.removeAll();
			criteriaValueLabel.setText("");
			timeValueLabel.setText("");
			amplitudeValueLabel.setText("");
			widthValueLabel.setText("");
			
			for (int i = 0; i < eventsListViewer.getTable().getItemCount(); i++) {
				TableItem item = eventsListViewer.getTable().getItem(i);
				item.setFont(JFaceResources.getFont("MY_TEXT_FONT"));
			}
				
			if(subject != null)	subjectNamelabel.setText(subject.getLocalPath() + (subject.isLoaded()?"":" (" + Messages.getString("NotLoaded") + ")"));
//			long t1 = System.currentTimeMillis();
			eventsListViewer.setInput(subject);
//			t1 = System.currentTimeMillis() - t1;
//			System.out.println(">>>>>>>>>>>>>>>>>> Time to eventsListViewer.setInput : " + t1);
			
		} else if(message == IResourceObserver.RENAMED) {
			subjectNamelabel.setText(Messages.getString("NONE"));
			if(subject != null)	subjectNamelabel.setText(subject.getLocalPath() + (subject.isLoaded()?"":" (" + Messages.getString("NotLoaded") + ")"));
		} else if(message == IResourceObserver.CHANNEL_DELETED) {
			subjectNamelabel.setText(Messages.getString("NONE"));
			
			selectedTrialValueCombo.removeAll();
			criteriaValueLabel.setText("");
			timeValueLabel.setText("");
			amplitudeValueLabel.setText("");
			widthValueLabel.setText("");
			
			eventsListViewer.refresh();
			eventsListViewer.getTable().select(0);
			selectionChanged(new SelectionChangedEvent(eventsListViewer ,eventsListViewer.getSelection()));
		} else if(message == IResourceObserver.PROCESS_RUN) {
			eventsListViewer.refresh();
			selectionChanged(new SelectionChangedEvent(eventsListViewer ,eventsListViewer.getSelection()));
		} else if(message == IResourceObserver.EVENT_DELETED) {
			int index = eventsListViewer.getTable().getSelectionIndex();
			if(index > -1) {
				String eventName = eventsListViewer.getTable().getItem(index).getText();
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				String[] eventsNames = mathEngine.getEventsGroupsNames(subject.getLocalPath());
				if(Arrays.asList(eventsNames).indexOf(eventName) > -1) 
					selectionChanged(new SelectionChangedEvent(eventsListViewer, eventsListViewer.getSelection()));
			else {
				eventsListViewer.setInput(subject);
				selectedTrialValueCombo.removeAll();
				criteriaValueLabel.setText("");
				timeValueLabel.setText("");
				amplitudeValueLabel.setText("");
				widthValueLabel.setText("");
			}
			}
		}
		
	}

	public void selectionChanged(SelectionChangedEvent event) {
		deleteEventAction.getAction().setEnabled(false);
		if(eventsListViewer.getTable().getSelectionIndex() > -1) {
			deleteEventAction.getAction().setEnabled(true);
			TableItem selectedItem = eventsListViewer.getTable().getItem(eventsListViewer.getTable().getSelectionIndex());
			String eventName  = selectedItem.getText();
			String fullEventName = ((Subject)eventsListViewer.getInput()).getLocalPath() + "." + eventName;
			IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
			criteriaValueLabel.setText(mathEngine.getCriteriaForEventsGroup(fullEventName));
			int[] trialsList = mathEngine.getEventsGroupTrialsList(fullEventName);
			selectedTrialValueCombo.removeAll();
			for (int i = 0; i < trialsList.length; i++) selectedTrialValueCombo.add(Messages.getString("TrialNumber") + String.valueOf(trialsList[i]));
			selectedTrialValueCombo.select(0);
			for (int i = 0; i < eventsListViewer.getTable().getItemCount(); i++) {
				TableItem item = eventsListViewer.getTable().getItem(i);
				if(selectedItem == item) item.setFont(JFaceResources.getFont("MY_SELECTED_TEXT_FONT"));
				else item.setFont(JFaceResources.getFont("MY_TEXT_FONT"));
			}
		}
		
	}
	
}
