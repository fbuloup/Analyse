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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import analyse.model.Chart;
import analyse.model.ChartsTypes;
import analyse.model.MultiCharts;
import analyse.model.Note;
import analyse.model.Processing;
import analyse.resources.ColorsUtils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;

public abstract class View extends CTabFolder implements CTabFolder2Listener, FocusListener, MouseListener {
	
	private SashForm sashForm;

	public View(Composite parent, int style) {
		super(parent, style);
		if(parent instanceof SashForm) sashForm = (SashForm) parent;
		setMaximizeVisible(true);
		setSimple(false);
		setTabHeight(25);
		setBorderVisible(true);
		addCTabFolder2Listener(this);
		addFocusListener(this);
		addMouseListener(this);
	}
	
	protected abstract void initView(int showMode);
	
	protected abstract void createToolBar();
	
	protected CTabItem createTabItem() {
		CTabItem tabItem = new CTabItem(this,SWT.NONE);
		tabItem.setShowClose(true);
		tabItem.setText("Tab item");
		setSelection(tabItem);
		return tabItem;
	}
	
	protected CTabItem createChartEditorTabItem(Object object) {
		if(object instanceof Chart) {
			Chart chart = (Chart)object;
			if(chart.getData().getChartType().equals(ChartsTypes.TIME_CHART_ID_STRING)) {
				TimeChartEditor chartEditor = new TimeChartEditor(this, SWT.NONE, chart);
				chartEditor.setShowClose(true);
				chartEditor.setImage(ImagesUtils.getImage(IImagesKeys.CHART_EDITOR_ICON));
				setSelection(chartEditor);
				return chartEditor;
			}
			if(chart.getData().getChartType().equals(ChartsTypes.XY_CHART_ID_STRING)) {
				XYChartEditor chartEditor = new XYChartEditor(this, SWT.NONE, chart);
				chartEditor.setShowClose(true);
				chartEditor.setImage(ImagesUtils.getImage(IImagesKeys.CHART_EDITOR_ICON));
				setSelection(chartEditor);
				return chartEditor;
			}
		}
		if(object instanceof MultiCharts) {
			MultiCharts multiCharts = (MultiCharts)object;
			MultiChartsEditor chartEditor = new MultiChartsEditor(this, SWT.NONE, multiCharts);
			chartEditor.setShowClose(true);
			chartEditor.setImage(ImagesUtils.getImage(IImagesKeys.CHART_EDITOR_ICON));
			setSelection(chartEditor);
			return chartEditor;
		}
		return null;
	}
	
	protected ProcessingEditor createProcessingEditorTabItem(Processing processing) {
		ProcessingEditor processingEditor = new ProcessingEditor(this, SWT.NONE, processing);
		processingEditor.setShowClose(true);
		setSelection(processingEditor);
		return processingEditor;
	}
	
	protected NoteEditor createNoteEditorTabItem(Note note) {
		NoteEditor noteEditor = new NoteEditor(this, SWT.NONE, note);
		noteEditor.setShowClose(true);
		noteEditor.setImage(ImagesUtils.getImage(IImagesKeys.NOTE_ICON));
		setSelection(noteEditor);
		return noteEditor;
	}

	public void close(CTabFolderEvent event) {
		if(getItemCount() == 1 && !(this instanceof EditorsView)) {
			setVisible(false);
			restoreHandler();
			AnalyseApplicationWindow.updateLayout();
		}
		if(this instanceof EditorsView) {
			setTopRight(null);
			AnalyseApplicationWindow.refreshActiveChartAction.setEnabled((getItemCount() > 1));
			AnalyseApplicationWindow.randomizeSeriesColorsAction.setEnabled((getItemCount() > 1));
			AnalyseApplicationWindow.clearActiveEditorAction.setEnabled((getItemCount() > 1));
		}
		if(this instanceof ExperimentsView) event.doit = false;
	}

	public void maximize(CTabFolderEvent event) {
		maximizeHandler();
	}
	
	private void maximizeHandler() {
		sashForm.setMaximizedControl(this);
		AnalyseApplicationWindow.setMaximizedControl(sashForm);
		setMaximized(true);
	}

	public void minimize(CTabFolderEvent event) {
	}

	public void restore(CTabFolderEvent event) {
		restoreHandler();
	}
	
	private void restoreHandler() {
		sashForm.setMaximizedControl(null);
		AnalyseApplicationWindow.setMaximizedControl(null);
		setMaximized(false);
	}

	public void showList(CTabFolderEvent event) {
	}

	public void focusGained(FocusEvent e) {
		setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
		setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
	}

	public void focusLost(FocusEvent e) {
		setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
		setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
	}

	public void mouseDoubleClick(MouseEvent e) {
		if(getMaximized()) restoreHandler();
		else maximizeHandler();
		setFocus();
	}

	public void mouseDown(MouseEvent e) {
		setFocus();
	}

	public void mouseUp(MouseEvent e) {
	}

}
