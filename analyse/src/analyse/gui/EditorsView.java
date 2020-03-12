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
package analyse.gui;

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.model.Chart;
import analyse.model.DataChart;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.MultiCharts;
import analyse.model.MultiDataCharts;
import analyse.model.Note;
import analyse.model.Processing;
import analyse.resources.Messages;

public class EditorsView extends View implements IMenuListener, IResourceObserver {

	private class CloseEditorAction extends Action {
		public CloseEditorAction() {
			setText(Messages.getString("CloseEditorAction.Title")); 
		}
		@Override
		public void run() {
			EditorsView.this.getSelection().dispose();
			if(getItemCount() == 0) setTopRight(null);
			AnalyseApplicationWindow.refreshActiveChartAction.setEnabled(getSelection() instanceof ChartEditor);
			AnalyseApplicationWindow.randomizeSeriesColorsAction.setEnabled(getSelection() instanceof ChartEditor);
			AnalyseApplicationWindow.clearActiveEditorAction.setEnabled(getSelection() != null);
		}
	}
	private class CloseAllEditors extends Action {
		public CloseAllEditors() {
			setText(Messages.getString("CloseAllEditors.Title")); 
		}
		@Override
		public void run() {
			CTabItem[] items = EditorsView.this.getItems();
			for (int i = 0; i < items.length; i++) items[i].dispose();
			if(getItemCount() == 0) setTopRight(null);
			AnalyseApplicationWindow.refreshActiveChartAction.setEnabled(getSelection() instanceof ChartEditor);
			AnalyseApplicationWindow.randomizeSeriesColorsAction.setEnabled(getSelection() instanceof ChartEditor);
			AnalyseApplicationWindow.clearActiveEditorAction.setEnabled(getSelection() != null);
		}
	}
	private class CloseOthersEditorsAction extends Action {
		public CloseOthersEditorsAction() {
			setText(Messages.getString("CloseOthersEditorsAction.Title")); 
		}
		@Override
		public void run() {
			CTabItem selectedItem = EditorsView.this.getSelection();
			CTabItem[] items = EditorsView.this.getItems();
			for (int i = 0; i < items.length; i++)  if(items[i] != selectedItem) items[i].dispose();
		}
	}
	
	private CTabItem previousSelectedItem = null;
	
	public EditorsView(Composite parent, int style) {
		super(parent, style);
		
		MenuManager popupMenuManager = new MenuManager("popupMenuManagerEditors");
		popupMenuManager.setRemoveAllWhenShown(true);
		popupMenuManager.addMenuListener(this);
		setMenu(popupMenuManager.createContextMenu(this));
		
		addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				CTabItem editor = getSelection();
				IResource resource = null;
				if(editor instanceof ChartEditor) if(((ChartEditor)editor).getChart() instanceof Chart) resource = (Chart)((ChartEditor)editor).getChart();
				if(editor instanceof ProcessingEditor) resource = ((ProcessingEditor)editor).getProcessing();
				if(editor instanceof NoteEditor) resource = ((NoteEditor)editor).getNote();
				if(editor instanceof MultiChartsEditor) resource = (MultiCharts) ((MultiChartsEditor)editor).getChart();
				
				updateToolBar(resource);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		addSelectionListener(AnalyseApplicationWindow.refreshActiveChartAction);
		addSelectionListener(AnalyseApplicationWindow.randomizeSeriesColorsAction);
		addSelectionListener(AnalyseApplicationWindow.clearActiveEditorAction);
		
		Experiments.getInstance().addExperimentObserver(this);
	}

	@Override
	protected void initView(int showMode) {
	}
	
	public void openResource(IResource resource) {
		boolean doOpen = true;
		CTabItem tabItem = isOpened(resource);
		if(tabItem == null) {
			CTabItem editor = null;
			
			if(resource instanceof Chart) {
				doOpen = isOpeningPossible(((Chart)resource).getData());
				if(doOpen) {
					editor = createChartEditorTabItem((Chart) resource);
					AnalyseApplication.getAnalyseApplicationWindow().addSelectionChangedListenerToExperimentsView((ISelectionChangedListener) editor);
					AnalyseApplicationWindow.refreshActiveChartAction.setChartEditor((ChartEditor) editor);
					AnalyseApplicationWindow.randomizeSeriesColorsAction.setChartEditor((ChartEditor) editor);
					AnalyseApplicationWindow.clearActiveEditorAction.setEditor(editor);
				}
			}
			
			if(resource instanceof MultiCharts) {
				MultiDataCharts multiDataCharts =((MultiCharts)resource).getData();
				DataChart[] dataCharts = multiDataCharts.getDataCharts();
				for (int numDataCharts = 0; numDataCharts < dataCharts.length; numDataCharts++) {
					DataChart dataChart = dataCharts[numDataCharts];
					doOpen = isOpeningPossible(dataChart);
				}
				if(doOpen) {
					editor = createChartEditorTabItem((MultiCharts) resource);
					AnalyseApplication.getAnalyseApplicationWindow().addSelectionChangedListenerToExperimentsView((ISelectionChangedListener) editor);
					AnalyseApplicationWindow.refreshActiveChartAction.setChartEditor(editor);
					AnalyseApplicationWindow.randomizeSeriesColorsAction.setChartEditor(editor);
					AnalyseApplicationWindow.clearActiveEditorAction.setEditor(editor);
				}
			}
			
			if(resource instanceof Processing) {
				editor = createProcessingEditorTabItem((Processing) resource);
				AnalyseApplicationWindow.clearActiveEditorAction.setEditor(editor);
			}
			
			if(resource instanceof Note) {
				editor = createNoteEditorTabItem((Note) resource);
				AnalyseApplicationWindow.clearActiveEditorAction.setEditor(editor);
			}
			
			if(doOpen) {
				editor.setText(resource.getNameWithoutExtension());
				editor.setToolTipText(resource.getLocalPath());
				Experiments.getInstance().addExperimentObserver((IResourceObserver) editor);
				AnalyseApplicationWindow.refreshActiveChartAction.setEnabled(editor instanceof ChartEditor);
				AnalyseApplicationWindow.randomizeSeriesColorsAction.setEnabled(editor instanceof ChartEditor);
				AnalyseApplicationWindow.clearActiveEditorAction.setEnabled(true);
				updateToolBar(resource);
			}
		} else {
			setSelection(tabItem);
			updateToolBar(resource);
			AnalyseApplicationWindow.refreshActiveChartAction.setEnabled(tabItem instanceof ChartEditor);
			AnalyseApplicationWindow.randomizeSeriesColorsAction.setEnabled(tabItem instanceof ChartEditor);
			AnalyseApplicationWindow.clearActiveEditorAction.setEnabled(true);
		}
	}
	
	private boolean isOpeningPossible(DataChart dataChart) {
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		String[] signalsAndTrials = dataChart.getSignals();
		if(signalsAndTrials.length > 0 && !mathEngine.isStarted()) {
			Log.logErrorMessage(Messages.getString("EditorsView.MathEngineNotLoaded"));
			return false;
		}
		for (int i = 0; i < signalsAndTrials.length; i++) {
			String fullSignalName = signalsAndTrials[i];
			String fullSubjectName = fullSignalName.replaceAll("\\.\\w+$", "").replaceAll("\\.\\w+$", "");
			if(!mathEngine.isSubjectLoaded(fullSubjectName)) {
				Log.logErrorMessage(Messages.getString("EditorsView.DataFor") + signalsAndTrials[i] + Messages.getString("EditorsView.NotLoaded") + fullSignalName + " !");
				return false;
			}
		}
		return true;
	}

	private void updateToolBar(IResource resource) {
		if(previousSelectedItem instanceof ChartEditor) ((ChartEditor)previousSelectedItem).disposeToolBar();
		if(previousSelectedItem instanceof ProcessingEditor) ((ProcessingEditor)previousSelectedItem).disposeToolBar();
		if(previousSelectedItem instanceof NoteEditor) ((NoteEditor)previousSelectedItem).disposeToolBar();
		if(previousSelectedItem instanceof MultiChartsEditor) ((MultiChartsEditor)previousSelectedItem).disposeToolBar();
		ToolBar toolBar = null;
		if(resource instanceof Chart) toolBar = ((ChartEditor)getSelection()).getToolBar();
		if(resource instanceof MultiCharts) toolBar = ((MultiChartsEditor)getSelection()).getToolBar();
		if(resource instanceof Processing) toolBar = ((ProcessingEditor)getSelection()).getToolBar();
		if(resource instanceof Note) toolBar = ((NoteEditor)getSelection()).getToolBar();
		setTopRight(toolBar, SWT.RIGHT);
		previousSelectedItem = getSelection();
	}
	
	public CTabItem isOpened(IResource resource) {
		CTabItem[] items = getItems();
		for (int i = 0; i < items.length; i++) {
			if(items[i] instanceof ChartEditor) {
				if( ( (ChartEditor)items[i]).getChart() instanceof Chart) {
					Chart chart = (Chart)((ChartEditor)items[i]).getChart();
					if(chart == resource) return items[i];
				}
			}
			if(items[i] instanceof MultiChartsEditor) {
				MultiCharts chart = (MultiCharts) ((MultiChartsEditor)items[i]).getChart();
				if(chart == resource) return items[i];
			}
			if(items[i] instanceof ProcessingEditor) {
				Processing processing = ((ProcessingEditor)items[i]).getProcessing();
				if(processing == resource) return items[i];
			}
			if(items[i] instanceof NoteEditor) {
				Note note = ((NoteEditor)items[i]).getNote();
				if(note == resource) return items[i];
			}
		}
		return null;
	}

	public void menuAboutToShow(IMenuManager popupMenuManager) {
		if(getItemCount() > 0) {
			ActionContributionItem closeEditorAction= new ActionContributionItem((IAction)(new CloseEditorAction()));
			popupMenuManager.add(closeEditorAction);
			if(getItemCount() > 1) {
				ActionContributionItem closeAllEditors = new ActionContributionItem((IAction)(new CloseAllEditors()));
				popupMenuManager.add(closeAllEditors);
				ActionContributionItem closeOthersEditorsAction = new ActionContributionItem((IAction)(new CloseOthersEditorsAction()));
				popupMenuManager.add(closeOthersEditorsAction);
			}
		}
	}

	@Override
	protected void createToolBar() {
		// TODO Auto-generated method stub
	}

	public void update(int message, IResource[] resources) {
		if(message == IResourceObserver.DELETED) {
			if(resources[0] instanceof Chart || resources[0] instanceof Processing || resources[0] instanceof Note || resources[0] instanceof MultiCharts) {
//				Event event = new Event();
//				event.display = getDisplay();
//				event.widget = this;
//				event.item = getSelection();
//				event.type = SWT.Close;
				super.close(null);
			}
		}
	}

	public void freeze() {
		CTabItem[] tabItems = getItems();
		for (int i = 0; i < tabItems.length; i++) 
			if(tabItems[i].getControl() != null) tabItems[i].getControl().setEnabled(false);
	}
	
	public void unFreeze() {
		CTabItem[] tabItems = getItems();
		for (int i = 0; i < tabItems.length; i++) 
			if(tabItems[i].getControl() != null) tabItems[i].getControl().setEnabled(true);
	}

}
