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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.swtchart.IAxis;
import org.swtchart.IEventSeries;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ext.ChartMarker;
import org.swtchart.ext.ChartMarkersSet;
import org.swtchart.ext.ICoordinatesObserver;
import org.swtchart.ext.IRangeObserver;
import org.swtchart.ext.InteractiveChart;
import org.swtchart.internal.series.EventSeries;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.Utils;
import analyse.gui.dialogs.AxisDialog;
import analyse.gui.dialogs.NewMarkerInputDialog;
import analyse.gui.dialogs.RefactorDialog;
import analyse.model.Chart;
import analyse.model.DataChart;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Signal;
import analyse.model.Subject;
import analyse.preferences.AnalysePreferences;
import analyse.resources.ColorsUtils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

@SuppressWarnings("deprecation")
public final class TimeChartEditor extends ChartEditor implements IResourceObserver, ISelectionChangedListener, IRangeObserver, ICoordinatesObserver, Listener {

	private class NextAction extends Action {
		public NextAction() {
			super(Messages.getString("Next"),Action.AS_PUSH_BUTTON); 
			setToolTipText(Messages.getString("Next")); 
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.NEXT_TRIAL_ICON));
		}
		@Override
		public void run() {
			CTabItem selectedTabItem = chartOptionsTabFolder.getSelection();
			boolean nextPreviousOnlySignals = AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.NEXT_PREVIOUS_ONLY_SIGNALS);
			if(selectedTabItem == signalsTabItem || nextPreviousOnlySignals) {
				int[] indices = trialsTableViewer.getTable().getSelectionIndices();
				boolean valid = true;
				for (int i = 0; i < indices.length; i++) {
					indices[i] = indices[i] + 1;
					if(indices[i] > trialsTableViewer.getTable().getItemCount()) {
						valid = false;
						break;
					}
				}
				if(valid) {
					trialsTableViewer.getTable().setSelection(indices);
					ISeries[] series = swtChart.getSeriesSet().getSeries();
					String[] seriesID = new String[series.length];
					for (int i = 0; i < series.length; i++) seriesID[i] = series[i].getId();
					Arrays.sort(seriesID);
					saveSeriesColors = new Color[seriesID.length]; 
					for (int i = 0; i < seriesID.length; i++) saveSeriesColors[i] = ((ILineSeries)swtChart.getSeriesSet().getSeries(seriesID[i])).getLineColor();
				}
				updateChart();
			}
			if(selectedTabItem == categoriesTabItem && !nextPreviousOnlySignals) {
				int[] indices = categoriesListViewer.getList().getSelectionIndices(); 
				boolean valid = true;
				for (int i = 0; i < indices.length; i++) {
					indices[i] = indices[i] + 1;
					if(indices[i] > categoriesListViewer.getList().getItemCount()) {
						valid = false;
						break;
					}
				}
				if(valid) {
					clearSWTChart();
					categoriesListViewer.getList().setSelection(indices);
				}
				updateChart();
			}
			if(selectedTabItem == markersTabItem && !nextPreviousOnlySignals) {
				int[] indices = markersListViewer.getList().getSelectionIndices(); 
				boolean valid = true;
				for (int i = 0; i < indices.length; i++) {
					indices[i] = indices[i] + 1;
					if(indices[i] > markersListViewer.getList().getItemCount()) {
						valid = false;
						break;
					}
				}
				if(valid) {
					swtChart.getChartMarkersSet().resetMarkersSeriesSelected();
					for (int i = 0; i < indices.length; i++) swtChart.getChartMarkersSet().selectedMarker(markersListViewer.getElementAt(indices[i]));
					markersListViewer.getList().setSelection(indices);
				}
				swtChart.redraw();
			}
		}
	}
	
	private class PreviousAction extends Action {
		public PreviousAction() {
			super(Messages.getString("Previous"),Action.AS_PUSH_BUTTON); 
			setToolTipText(Messages.getString("Previous")); 
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.PREVIOUS_TRIAL_ICON));
		}
		@Override
		public void run() {
			CTabItem selectedTabItem = chartOptionsTabFolder.getSelection();
			if(selectedTabItem == signalsTabItem) {
				int[] indices = trialsTableViewer.getTable().getSelectionIndices();
				boolean valid = true;
				for (int i = 0; i < indices.length; i++) {
					indices[i] = indices[i] -1;
					if(indices[i] < 0) {
						valid = false;
						break;
					}
				}
				if(valid){
					trialsTableViewer.getTable().setSelection(indices);
					ISeries[] series = swtChart.getSeriesSet().getSeries();
					String[] seriesID = new String[series.length];
					for (int i = 0; i < series.length; i++) seriesID[i] = series[i].getId();
					Arrays.sort(seriesID);
					saveSeriesColors = new Color[seriesID.length]; 
					for (int i = 0; i < seriesID.length; i++) saveSeriesColors[i] = ((ILineSeries)swtChart.getSeriesSet().getSeries(seriesID[i])).getLineColor();
				}
				updateChart();
			}
			if(selectedTabItem == categoriesTabItem){
				int[] indices = categoriesListViewer.getList().getSelectionIndices(); 
				boolean valid = true;
				for (int i = 0; i < indices.length; i++) {
					indices[i] = indices[i] - 1;
					if(indices[i] < 0) {
						valid = false;
						break;
					}
				}
				if(valid) {
					clearSWTChart();
					categoriesListViewer.getList().setSelection(indices);
				}
				updateChart();
			}
			if(selectedTabItem == markersTabItem) {
				int[] indices = markersListViewer.getList().getSelectionIndices(); 
				boolean valid = true;
				for (int i = 0; i < indices.length; i++) {
					indices[i] = indices[i] - 1;
					if(indices[i] < 0) {
						valid = false;
						break;
					}
				}
				if(valid) {
					swtChart.getChartMarkersSet().resetMarkersSeriesSelected();
					for (int i = 0; i < indices.length; i++) swtChart.getChartMarkersSet().selectedMarker(markersListViewer.getElementAt(indices[i]));
					markersListViewer.getList().setSelection(indices);
				}
				swtChart.redraw();
			}
		}
	}
	
	private class AdjustXYAxisAction extends Action {
		public AdjustXYAxisAction() {
			super(Messages.getString("AdjustXYAxisAction.Title"),Action.AS_PUSH_BUTTON); 
			setToolTipText(Messages.getString("AdjustXYAxisAction.Title")); 
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.ADJUST_XY_AXIS_ICON));
		}
		@Override
		public void run() {
			swtChart.getAxisSet().adjustRange();
			swtChart.redraw();
			updateRange();
		}
	}
	
	private final class ChartOptionsAction extends Action {
		
		private Menu chartOptionsMenu;
		
		class ShowMarkersAction extends Action {
			public ShowMarkersAction() {
				super(Messages.getString("ChartOptionsActions.ShowMarkers"),Action.AS_CHECK_BOX); //$NON-NLS-1$
			}
			@Override
			public void run() {
				chart.getData().setMarkersVisibility(!chart.getData().isMarkersVisible());
				swtChart.setShowMarkers(chart.getData().isMarkersVisible());
				chart.saveChart();
			}
		}
		class ShowCrossHairAction extends Action {	
			public ShowCrossHairAction() {
				super(Messages.getString("ChartOptionsActions.ShowCrossHair"),Action.AS_CHECK_BOX); //$NON-NLS-1$
			}		
			@Override
			public void run() {
				chart.getData().setCrossHairVisibility(!chart.getData().isCrossHairVisible());	
				swtChart.setCrossHairVisibility(chart.getData().isCrossHairVisible());
				chart.saveChart();
			}
		}
		class ShowLegendAction extends Action {		
			public ShowLegendAction() {
				super(Messages.getString("ChartOptionsActions.ShowLegend"),Action.AS_CHECK_BOX); //$NON-NLS-1$
			}		
			@Override
			public void run() {				
				chart.getData().setLegendVisibility(!chart.getData().isLegendVisible());
				swtChart.getLegend().setVisible(chart.getData().isLegendVisible());
				swtChart.redraw();
				chart.saveChart();
			}
		}
		class ShowChannelsPaletteAction extends Action {		
			public ShowChannelsPaletteAction() {
				super(Messages.getString("ChartOptionsActions.ShowChannelsPalette"),Action.AS_CHECK_BOX); //$NON-NLS-1$
			}		
			@Override
			public void run() {
				chart.getData().setChannelsPaletteVisibility(!chart.getData().isChannelsPaletteVisible());
				TimeChartEditor.this.updateChannelsPaletteVisiblility();
				chart.saveChart();
			}
		}
		class AutoAdjustXYAxisAction extends Action {		
			public AutoAdjustXYAxisAction() {
				super(Messages.getString("ChartOptionsActions.AutoAdjustXYAxis"),Action.AS_CHECK_BOX); //$NON-NLS-1$
			}		
			@Override
			public void run() {
				chart.getData().setAutoAdjustAxis(!chart.getData().isAutoAdjustAxis());
				chart.saveChart();
			}
		}
		class ShowEventsAsInfiniteAction extends Action {		
			public ShowEventsAsInfiniteAction() {
				super(Messages.getString("ShowEventsAsInfiniteAction.Title"),Action.AS_CHECK_BOX); //$NON-NLS-1$
			}		
			@Override
			public void run() {
				chart.getData().setShowEventsAsInfinite(!chart.getData().isShowEventsAsInfinite());
				chart.saveChart();
				ISeries[] series = swtChart.getSeriesSet().getSeries();
				boolean refresh = false;
				for (int i = 0; i < series.length; i++) {
					if(series[i] instanceof EventSeries) {
						((EventSeries)series[i]).setShowHeightAsInfinite(chart.getData().isShowEventsAsInfinite());
						refresh = true;
					}
				}
				if(refresh) {
					swtChart.redraw();
					swtChart.getAxisSet().adjustRange();
				}
			}
		}
		
		
		public ChartOptionsAction() {
			super(Messages.getString("Properties"),Action.AS_DROP_DOWN_MENU);
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.CHART_OPTIONS_ICON));
			setMenuCreator(new IMenuCreator() {
				public Menu getMenu(Menu parent) {
					return null;
				}
				public Menu getMenu(Control parent) {
					if (chartOptionsMenu != null) chartOptionsMenu.dispose(); 
					chartOptionsMenu = new Menu(parent);
//					addActionToMenu(chartOptionsMenu, new PropertiesAction()); 
//					new MenuItem(chartOptionsMenu, SWT.SEPARATOR);
					ShowCrossHairAction showCrossHairAction = new ShowCrossHairAction();
					addActionToMenu(chartOptionsMenu, showCrossHairAction); 
					ShowLegendAction showLegendAction = new ShowLegendAction();
					addActionToMenu(chartOptionsMenu, showLegendAction); 
					ShowChannelsPaletteAction showChannelsPaletteAction = new ShowChannelsPaletteAction();
					addActionToMenu(chartOptionsMenu, showChannelsPaletteAction); 				
					AutoAdjustXYAxisAction autoAdjustXYAxisAction = new AutoAdjustXYAxisAction();
					addActionToMenu(chartOptionsMenu, autoAdjustXYAxisAction); 	
					ShowMarkersAction showMarkersAction = new ShowMarkersAction();
					addActionToMenu(chartOptionsMenu, showMarkersAction);
					ShowEventsAsInfiniteAction showEventsAsInfiniteAction = new ShowEventsAsInfiniteAction();
					addActionToMenu(chartOptionsMenu, showEventsAsInfiniteAction);
//					new MenuItem(chartOptionsMenu, SWT.SEPARATOR);
//					addActionToMenu(chartOptionsMenu, AnalyseApplicationWindow.refreshActiveChartAction);
//					addActionToMenu(chartOptionsMenu, AnalyseApplicationWindow.randomizeSeriesColorsAction);
					showLegendAction.setChecked(chart.getData().isLegendVisible());
					showCrossHairAction.setChecked(chart.getData().isCrossHairVisible());
					showMarkersAction.setChecked(chart.getData().isMarkersVisible());
					showChannelsPaletteAction.setChecked(chart.getData().isChannelsPaletteVisible());
					autoAdjustXYAxisAction.setChecked(chart.getData().isAutoAdjustAxis());
					showEventsAsInfiniteAction.setChecked(chart.getData().isShowEventsAsInfinite());
//					new MenuItem(chartOptionsMenu,SWT.SEPARATOR);
//					addActionToMenu(chartOptionsMenu, new SaveAsImage());
					return chartOptionsMenu; 
				}
				public void dispose() {
					if(chartOptionsMenu != null) if(!chartOptionsMenu.isDisposed())	chartOptionsMenu.dispose();				
				}
				protected void addActionToMenu(Menu parent, Action action) { 
					ActionContributionItem item = new ActionContributionItem(action);
					item.fill(parent, -1);
				}
			});
			addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if(chartOptionsMenu != null) if(!chartOptionsMenu.isDisposed())	chartOptionsMenu.dispose();
				}
			});
		}
		
		@Override
		public void run() {
			swtChart.openPropertiesDialog();
		}
	}
	
	private final class RefactorAction extends Action {
		public RefactorAction() {
			super(Messages.getString("RefactorAction.Title"),AS_PUSH_BUTTON);
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.REFACTOR_ICON));
			setEnabled(true);
		}
		public void run() {
			(new RefactorDialog(Display.getDefault().getActiveShell(),new IResource[]{chart})).open();
		}
	}
	
	private final class ManageAxisAction extends Action {
		public ManageAxisAction() {
			super(Messages.getString("ManageAxisAction.Title"),AS_PUSH_BUTTON);
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.AXIS_ICON));
			setEnabled(true);
		}
		public void run() {
			(new AxisDialog(Display.getDefault().getActiveShell(),TimeChartEditor.this)).open();
		}
	}
	
	private Color[] saveSeriesColors = new Color[0];
	private ListViewer markersListViewer;
	private Combo currentMarkersLabelValueCombo;
	private CLabel subjectNameLabelCategories;
	private ListViewer categoriesListViewer;
	private CLabel subjectNameLabelSignals;
	private TableViewer signalsTableViewer;
	private CheckboxTableViewer trialsTableViewer;
	private CTabItem signalsTabItem;
	private CTabItem categoriesTabItem;
	private CTabItem markersTabItem;
	protected CLabel serieLabel;
	private TableViewer signalsFilterTableViewer;
	private MarkerModifierDelegate markerModifier;
	private Button categoryColorButton;

	public TimeChartEditor(CTabFolder parent, int style, final Chart chart) {
		super(parent, style);
		this.chart = chart;
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Experiments.getInstance().removeExperimentObserver(TimeChartEditor.this);
				AnalyseApplication.getAnalyseApplicationWindow().removeSelectionChangedListenerFromExperimentsView(TimeChartEditor.this);
				if(chartOptionsAction != null) chartOptionsAction.dispose();
				disposeToolBar();
				sashForm.dispose();
			}
		});
		createContents();
		swtChart.addRangeObserver(this);
		swtChart.addCoordinatesObserver(this);
		swtChart.enableCompress(false);
		swtChart.getPlotArea().addListener(SWT.MouseDoubleClick, this);
		
		
		markerModifier = new MarkerModifierDelegate(swtChart);
		
		swtChart.addListener(SWT.MouseMove, markerModifier);
		swtChart.addListener(SWT.MouseMove, swtChart);
		swtChart.addListener(SWT.MouseDown, markerModifier);
		swtChart.addListener(SWT.MouseUp, markerModifier);
		
		initChart();
	}
	
	public void handleEvent(Event event) {
		if(event.type == SWT.MouseDoubleClick) {
			MarkerModifierDelegate.addMarker(currentMarkersLabelValueCombo.getText(), swtChart);
		}
	}
	
	public void updateChannelsPaletteVisiblility() {
		if(chart.getData().isChannelsPaletteVisible())	sashForm.setWeights(new int[]{75,25});
		else sashForm.setWeights(new int[]{100,0});
	}
	
	protected ToolBar getToolBar() {
		toolBarManager = new ToolBarManager(SWT.FLAT | SWT.WRAP);
		toolBarManager.add(new RefactorAction());
		toolBarManager.add(new Separator());
		toolBarManager.add(new AdjustXYAxisAction());
		toolBarManager.add(new ManageAxisAction());
		toolBarManager.add(AnalyseApplicationWindow.clearActiveEditorAction);
		toolBarManager.add(AnalyseApplicationWindow.refreshActiveChartAction);
		toolBarManager.add(AnalyseApplicationWindow.randomizeSeriesColorsAction);
		toolBarManager.add(new Separator());
		chartOptionsAction = new ActionContributionItem((IAction)(new ChartOptionsAction()));
		toolBarManager.add(chartOptionsAction);
		ToolBar toolBar = toolBarManager.createControl(getParent());
		toolBar.addFocusListener((FocusListener) getParent());
		return toolBar;
	}

	private void createContents() {
		sashForm = new SashForm(getParent(), SWT.HORIZONTAL);
		sashForm.addFocusListener((FocusListener) getParent());

		Composite chartContainer = new Composite(sashForm, SWT.BORDER);
		chartContainer.addFocusListener((FocusListener) getParent());
		chartContainer.setLayout(new GridLayout(1,false));
		GridLayout chartContainerLayout = (GridLayout) chartContainer.getLayout();
		chartContainerLayout.marginHeight = 0;
		chartContainerLayout.marginWidth = 0;
		serieLabel = new CLabel(chartContainer, SWT.NONE);
		serieLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		serieLabel.setText(Messages.getString("NoSelectedSerie"));
		serieLabel.setAlignment(SWT.CENTER);
		swtChart = new InteractiveChart(chartContainer, SWT.NONE, true);
		swtChart.getPlotArea().addFocusListener((FocusListener) getParent());
		swtChart.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		swtChart.getTitle().setVisible(false);
		swtChart.getPlotArea().setForeground(JFaceColors.getBannerBackground(getDisplay()));
		IAxis[] axis = swtChart.getAxisSet().getAxes();
		for (int i = 0; i < axis.length; i++) {
			axis[i].getTitle().setVisible(false);
			axis[i].getTick().setForeground(JFaceColors.getBannerForeground(getDisplay()));
		}
		
		Composite chartOptionsContainer = new Composite(sashForm, SWT.NONE);
		chartOptionsContainer.addFocusListener((FocusListener) getParent());
		chartOptionsContainer.setLayout(new GridLayout(1,false));
		GridLayout chartOptionsContainerLayout = (GridLayout) chartOptionsContainer.getLayout();
		chartOptionsContainerLayout.marginHeight = 0;
		chartOptionsContainerLayout.marginWidth = 0;
		createChartOptions(chartOptionsContainer);

		if(chart.getData().isChannelsPaletteVisible())	sashForm.setWeights(new int[]{75,25});
		else sashForm.setWeights(new int[]{100,0});
		
		setControl(sashForm);
	}

	private void createChartOptions(Composite chartOptionsContainer) {
		chartOptionsTabFolder = new CTabFolder(chartOptionsContainer, SWT.NONE);
		chartOptionsTabFolder.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		chartOptionsTabFolder.setTabHeight(22);
		chartOptionsTabFolder.setBorderVisible(true);
		chartOptionsTabFolder.addFocusListener((FocusListener) getParent());
		chartOptionsTabFolder.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
				chartOptionsTabFolder.setFocus();
			}
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		chartOptionsTabFolder.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
			}
			public void focusGained(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
			}
		});
		
		
		signalsTabItem = new CTabItem(chartOptionsTabFolder, SWT.NONE);
		signalsTabItem.setText(Messages.getString("ChannelsMarkersPalette.SignalsTabItemTitle"));
		createSignalsContents(signalsTabItem);
		
		categoriesTabItem = new CTabItem(chartOptionsTabFolder, SWT.NONE);
		categoriesTabItem.setText(Messages.getString("ChannelsMarkersPalette.CategoriesTabItemTitle"));
		createCategoriesContents(categoriesTabItem);
		
		markersTabItem = new CTabItem(chartOptionsTabFolder, SWT.NONE);
		markersTabItem.setText(Messages.getString("ChannelsView.SignalsItemMarkersTabItemTitle"));
		createMarkersContents(markersTabItem);
		
		ToolBarManager chartOptionsToolBarManager = new ToolBarManager(SWT.FLAT | SWT.WRAP);
		chartOptionsToolBarManager.add(new PreviousAction());
		chartOptionsToolBarManager.add(new NextAction());
		ToolBar toolBar = chartOptionsToolBarManager.createControl(chartOptionsTabFolder);
		toolBar.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
			}
			public void focusGained(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
			}
		});
		chartOptionsTabFolder.setTopRight(toolBar);
		chartOptionsTabFolder.setSelection(0);
		
	}

	private void createMarkersContents(CTabItem markersTabItem) {
		Composite markersContainer = new Composite(markersTabItem.getParent(), SWT.NONE);
		markersContainer.setLayout(new GridLayout(3,false));
		
		markersListViewer = new ListViewer(markersContainer);
		markersListViewer.getList().addFocusListener((FocusListener) getParent());
		markersListViewer.getList().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
		markersListViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				Object[] elements = (Object[]) ((ChartMarkersSet)inputElement).getChartMarkers();
				return elements;
			}
		});
		markersListViewer.setLabelProvider(new ILabelProvider() {
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
				return swtChart.getChartMarkersSet().getMarkerFullLable(element);
			}
			public Image getImage(Object element) {
				return null;
			}
		});
		markersListViewer.setSorter(new ViewerSorter());
		markersListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				swtChart.getChartMarkersSet().resetMarkersSeriesSelected();
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				Object[] elements = selection.toArray();
				for (int i = 0; i < elements.length; i++) swtChart.getChartMarkersSet().selectedMarker(elements[i]);
				swtChart.redraw();
			}
		});
		
		markersListViewer.getList().addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
			}
			public void focusGained(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
			}
		});
		
		final Button deleteSelectedMarkersButton = new Button(markersContainer, SWT.PUSH | SWT.FLAT);
		deleteSelectedMarkersButton.setLayoutData(new GridData(SWT.FILL,SWT.TOP,false,false));
		deleteSelectedMarkersButton.setImage(ImagesUtils.getImage(IImagesKeys.DELETE_ICON));
		deleteSelectedMarkersButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				chartOptionsTabFolder.setFocus();
				if(((IStructuredSelection)markersListViewer.getSelection()).size() > 0)
					if(MessageDialog.openQuestion(deleteSelectedMarkersButton.getShell(), Messages.getString("DeleteMarkerDialogTitle"), Messages.getString("DeleteMarkerDialogText"))) {
						final HashSet<Subject> modifiedSubjects = new HashSet<Subject>(0);
						final Object[] markers = ((IStructuredSelection)markersListViewer.getSelection()).toArray();
						IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								monitor.beginTask(Messages.getString("DeletingMarkers"), markers.length);
								for (int i = 0; i < markers.length; i++) {
									monitor.subTask("number " + i);
									Subject subject = MarkerModifierDelegate.deleteMarker((ChartMarker) markers[i], swtChart);
									if(subject != null) modifiedSubjects.add(subject);
									monitor.worked(1);
									if(monitor.isCanceled()) {
										monitor.done();
										throw new InterruptedException(Messages.getString("TimeChartEditor.Canceled"));
									}
								}
								monitor.done();
							}
						};
						AnalyseApplication.getAnalyseApplicationWindow().run(true, true, true, false, runnableWithProgress);
						IResource[] subjects = new IResource[modifiedSubjects.size()];
						Object[] objects = modifiedSubjects.toArray();
						for (int i = 0; i < objects.length; i++) subjects[i] = (IResource) objects[i];
						Experiments.notifyObservers(IResourceObserver.MARKER_DELETED, subjects);
					}
			}
		});
		
		CLabel currentMarkersLabelLabel = new CLabel(markersContainer, SWT.NONE);
		currentMarkersLabelLabel.setText(Messages.getString("ChannelsMarkersPalette.InputDialogNewMarkerLabelText"));
		currentMarkersLabelLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		
		currentMarkersLabelValueCombo = new Combo(markersContainer, SWT.READ_ONLY);
		currentMarkersLabelValueCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		currentMarkersLabelValueCombo.setVisibleItemCount(50);
		currentMarkersLabelValueCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				chartOptionsTabFolder.setFocus();
			}
		});
		
		Button addNewMarkerLabelButton = new Button(markersContainer, SWT.PUSH | SWT.FLAT);
		addNewMarkerLabelButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		addNewMarkerLabelButton.setImage(ImagesUtils.getImage(IImagesKeys.ADD_MARKER_ICON));
		addNewMarkerLabelButton.setToolTipText(Messages.getString("addNewMarkerLabelButtonTooltip"));
		addNewMarkerLabelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				chartOptionsTabFolder.setFocus();
				IResource resource = TimeChartEditor.this.chart.getParent();
				while(!(resource instanceof Experiment)) resource = resource.getParent();
				NewMarkerInputDialog inputDialog = new NewMarkerInputDialog(TimeChartEditor.this.getDisplay().getActiveShell(), (Experiment)resource);
				if(inputDialog.open() == Window.OK) {
					currentMarkersLabelValueCombo.add(inputDialog.getValue());
					String[] markersLabels = currentMarkersLabelValueCombo.getItems();
					Arrays.sort(markersLabels);
					currentMarkersLabelValueCombo.setItems(markersLabels);
					currentMarkersLabelValueCombo.select(currentMarkersLabelValueCombo.indexOf(inputDialog.getValue()));
				}
				
			}
		});
		
		markersTabItem.setControl(markersContainer);
		
	}

	private void createCategoriesContents(CTabItem categoriesTabItem) {
		Composite categoriesContainer = new Composite(categoriesTabItem.getParent(), SWT.NONE);
		categoriesContainer.setLayout(new GridLayout(2,false));
		
		CLabel selectedSubjectlabel = new CLabel(categoriesContainer, SWT.NONE);
		selectedSubjectlabel.setText(Messages.getString("SelectedSubject"));
		selectedSubjectlabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		
		subjectNameLabelCategories = new CLabel(categoriesContainer, SWT.NONE);
		subjectNameLabelCategories.setText(Messages.getString("NONE"));
		subjectNameLabelCategories.setToolTipText(Messages.getString("SelectedSubjectToolTip"));
		subjectNameLabelCategories.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		SashForm sashForm = new SashForm(categoriesContainer, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
		
		signalsFilterTableViewer = new TableViewer(sashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		signalsFilterTableViewer.setSorter(new ViewerSorter());
		signalsFilterTableViewer.getTable().addFocusListener((FocusListener) getParent());
		signalsFilterTableViewer.getTable().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,2,1));
		signalsFilterTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				if(inputElement instanceof IResource[]) {
					IResource[] resources = (IResource[]) inputElement;
					ArrayList<ArrayList<String>> allSignals = new ArrayList<ArrayList<String>>(resources.length);
					for (int i = 0; i < resources.length; i++) {
						allSignals.add(new ArrayList<String>(0));
						if(resources[i] instanceof Subject) {
							Subject subject = (Subject)resources[i];
							if(subject.isLoaded()) {
								String[] signalsNames = subject.getSignalsNames();//MathEngineFactory.getInstance().getMathEngine().getSignalsNames(subject.getLocalPath());
								allSignals.get(i).addAll(Arrays.asList(signalsNames));
								String[] eventsNames = subject.getEventsNames();
								allSignals.get(i).addAll(Arrays.asList(eventsNames));
							}
						}
					}
					HashSet<String> signals = new HashSet<String>(0);
					ArrayList<String> subSignals = allSignals.get(0);
					for (int i = 0; i < subSignals.size(); i++) {
						boolean doAdd = true;
						for (int j = 1; j < resources.length; j++) doAdd = doAdd && (allSignals.get(j).indexOf(subSignals.get(i)) > -1);							
						if(doAdd) signals.add(subSignals.get(i));
					}
					return signals.toArray(new String[signals.size()]);
				}
				return new String[0];
			}
		});
		signalsFilterTableViewer.setLabelProvider(new ILabelProvider() {
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
				String name = (String) element;
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				IResource[] resources = (IResource[]) signalsTableViewer.getInput();
				for (int i = 0; i < resources.length; i++) {
					if(resources[i] instanceof Subject) {
						Subject subject = (Subject)resources[i];
						if(mathEngine.isEventsGroup(subject.getLocalPath() + "." + name)) 
						return name + " (" + mathEngine.getCriteriaForEventsGroup(subject.getLocalPath() + "." + name) + ")";
					}
				}
				return (String) element;
			}
			public Image getImage(Object element) {
				String name = (String) element;
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				IResource[] resources = (IResource[]) signalsTableViewer.getInput();
				for (int i = 0; i < resources.length; i++) {
					if(resources[i] instanceof Subject) {
						Subject subject = (Subject)resources[i];
						if(mathEngine.isEventsGroup(subject.getLocalPath() + "." + name)) 
						return ImagesUtils.getImage(IImagesKeys.EVENTS_VIEW_ICON);
					}
				}
				return ImagesUtils.getImage(IImagesKeys.CHANNELS_VIEW_ICON);
			}
		});
		signalsFilterTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateCategoriesHandler();
			}
		});
		signalsFilterTableViewer.getTable().addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
			}
			public void focusGained(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
			}
		});
		
		Composite categoriesListContainer = new Composite(sashForm, SWT.NORMAL);
		categoriesListContainer.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
		categoriesListContainer.setLayout(new GridLayout(2, false));
		GridLayout gl = (GridLayout) categoriesListContainer.getLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		
		categoriesListViewer = new ListViewer(categoriesListContainer, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		categoriesListViewer.setSorter(new ViewerSorter());
		categoriesListViewer.getList().addFocusListener((FocusListener) getParent());
		categoriesListViewer.getList().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
		categoriesListViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			
			public Object[] getElements(Object inputElement) {
				if(inputElement instanceof IResource[]) {
					IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
					IResource[] resources = (IResource[]) inputElement;
					ArrayList<ArrayList<String>> allCategories = new ArrayList<ArrayList<String>>(resources.length);
					ArrayList<ArrayList<int[]>> allTrialsList = new ArrayList<ArrayList<int[]>>(resources.length);
					for (int numSubject = 0; numSubject < resources.length; numSubject++) {
						allCategories.add(new ArrayList<String>(0));
						allTrialsList.add(new ArrayList<int[]>(0));
						Subject subject = (Subject)resources[numSubject];
						if(subject.isLoaded()) {
							String[] categoriesNames = subject.getCategoriesNames();//mathEngine.getCategoriesNames(subject.getLocalPath());
							allCategories.get(numSubject).addAll(Arrays.asList(categoriesNames));
							for (int i = 0; i < categoriesNames.length; i++) {
								int[] trials = mathEngine.getTrialsListForCategory(subject.getLocalPath() + "." + categoriesNames[i]);
								allTrialsList.get(numSubject).add(trials);
							}
						}
					}
					HashSet<String> categories = new HashSet<String>(0);
					ArrayList<String> subCategories = allCategories.get(0);
					for (int i = 0; i < subCategories.size(); i++) {
						Subject subSubject = (Subject) resources[0];
						int[] trials = allTrialsList.get(0).get(i);
						boolean doAdd = true;
						for (int j = 1; j < resources.length; j++) {
							Subject subject = (Subject) resources[j];
							doAdd = doAdd && (allCategories.get(j).indexOf(subCategories.get(i)) > -1);		
							if(doAdd) {
								int[] trials2 = allTrialsList.get(j).get(i);
								for (int k = 0; k < trials2.length; k++) {
									if(trials.length > k) doAdd = doAdd && (trials[k] == trials2[k]);
									else doAdd = false;
								}
								if(doAdd) {
									String categoryName = subCategories.get(i);
									String criteria1 = mathEngine.getCriteriaForCategory(subSubject.getLocalPath() + "." + categoryName);
									String criteria2 = mathEngine.getCriteriaForCategory(subject.getLocalPath() + "." + categoryName);
									doAdd = doAdd && criteria1.equals(criteria2);
								}
							}
						}
						if(doAdd) {
							String categoryName = subCategories.get(i);
							categoryName = categoryName + " - " + mathEngine.getCriteriaForCategory(subSubject.getLocalPath() + "." + categoryName);
							categoryName = categoryName + " [";
							for (int k = 0; k < trials.length; k++) categoryName = categoryName + trials[k] + " - ";
							categoryName = categoryName.replaceAll("\\s-\\s$", "");
							categoryName = categoryName + "]";
							categories.add(categoryName);
						}
					}
					return categories.toArray(new String[categories.size()]);
				}
				return new String[0];
			}
		});
		categoriesListViewer.setLabelProvider(new ILabelProvider() {
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
		categoriesListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateCategoriesHandler();
			}
		});
		categoriesListViewer.getList().addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
			}
			public void focusGained(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
			}
		});
		
		CLabel categoryColorLabel = new CLabel(categoriesListContainer, SWT.NONE);
		categoryColorLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,false,1,1));
		categoryColorLabel.setText("Select category color :");
		
		categoryColorButton = new Button(categoriesListContainer, SWT.NORMAL);
		categoryColorButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,1,1));
		categoryColorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ColorDialog colorDialog = new ColorDialog(getSWTChart().getShell());
				colorDialog.setText("Choose a color : ");
				colorDialog.setRGB(categoryColorButton.getBackground().getRGB());
				RGB rgbColor = colorDialog.open();
				if(rgbColor != null) {
					categoryColorButton.setBackground(new Color(getDisplay(), rgbColor));
					categoriesListViewer.setData(categoriesListViewer.getStructuredSelection().getFirstElement().toString(), rgbColor);
				}
			}
		});
		
		

		sashForm.setWeights(new int[]{25,75});
		
		categoriesTabItem.setControl(categoriesContainer);
	}

	protected void updateCategoriesHandler() {
		chart.getData().clear();
		signalsTableViewer.getTable().deselectAll();
		trialsTableViewer.getTable().deselectAll();
		for (int i = 0; i < trialsTableViewer.getTable().getItems().length; i++) trialsTableViewer.getTable().getItems()[i].setChecked(false);
		 
		if(categoriesListViewer.getStructuredSelection().getFirstElement() != null) {
			String key = categoriesListViewer.getStructuredSelection().getFirstElement().toString();
			RGB rgbColor = (RGB) categoriesListViewer.getData(key);
			categoryColorButton.getBackground().dispose();
			categoryColorButton.setBackground(new Color(getDisplay(), rgbColor));
		}
		
		updateChart();
	}

	private void createSignalsContents(CTabItem signalsTabItem) {
		Composite signalsContainer = new Composite(signalsTabItem.getParent(), SWT.NONE);
		signalsContainer.setLayout(new GridLayout(2,false));
		
		CLabel selectedSubjectlabel = new CLabel(signalsContainer, SWT.NONE);
		selectedSubjectlabel.setText(Messages.getString("SelectedSubject"));
		selectedSubjectlabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		
		subjectNameLabelSignals = new CLabel(signalsContainer, SWT.NONE);
		subjectNameLabelSignals.setText(Messages.getString("NONE"));
		subjectNameLabelSignals.setToolTipText(Messages.getString("SelectedSubjectToolTip"));
		subjectNameLabelSignals.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		SashForm sashForm = new SashForm(signalsContainer, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
		
		signalsTableViewer = new TableViewer(sashForm);
		signalsTableViewer.setSorter(new ViewerSorter());
		signalsTableViewer.getTable().addFocusListener((FocusListener) getParent());
		signalsTableViewer.getTable().addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
			}
			public void focusGained(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
			}
		});
		signalsTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			
			public Object[] getElements(Object inputElement) {
				if(inputElement instanceof IResource[]) {
					trialsTableViewer.getTable().removeAll();
					IResource[] resources = (IResource[]) inputElement;
					ArrayList<ArrayList<String>> allSignals = new ArrayList<ArrayList<String>>(resources.length);
					for (int i = 0; i < resources.length; i++) {
						allSignals.add(new ArrayList<String>(0));
						if(resources[i] instanceof Subject) {
							Subject subject = (Subject)resources[i];
							if(subject.isLoaded()) {
								String[] signalsNames = subject.getSignalsNames();//MathEngineFactory.getInstance().getMathEngine().getSignalsNames(subject.getLocalPath());
								allSignals.get(i).addAll(Arrays.asList(signalsNames));
								String[] eventsNames = subject.getEventsNames();
								allSignals.get(i).addAll(Arrays.asList(eventsNames));
							}
						}
					}
					HashSet<String> signals = new HashSet<String>(0);
					ArrayList<String> subSignals = allSignals.get(0);
					for (int i = 0; i < subSignals.size(); i++) {
						boolean doAdd = true;
						for (int j = 1; j < resources.length; j++) doAdd = doAdd && (allSignals.get(j).indexOf(subSignals.get(i)) > -1);							
						if(doAdd) signals.add(subSignals.get(i));
					}
					return signals.toArray(new String[signals.size()]);
				}
				return new String[0];
			}
		});
		signalsTableViewer.setLabelProvider(new ILabelProvider() {
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
				String name = (String) element;
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				IResource[] resources = (IResource[]) signalsTableViewer.getInput();
				for (int i = 0; i < resources.length; i++) {
					if(resources[i] instanceof Subject) {
						Subject subject = (Subject)resources[i];
						if(mathEngine.isEventsGroup(subject.getLocalPath() + "." + name)) 
						return name + " (" + mathEngine.getCriteriaForEventsGroup(subject.getLocalPath() + "." + name) + ")";
					}
					
				}
				
				return (String) element;
			}
			public Image getImage(Object element) {
				String name = (String) element;
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				IResource[] resources = (IResource[]) signalsTableViewer.getInput();
				for (int i = 0; i < resources.length; i++) {
					if(resources[i] instanceof Subject) {
						Subject subject = (Subject)resources[i];
						if(mathEngine.isEventsGroup(subject.getLocalPath() + "." + name)) 
						return ImagesUtils.getImage(IImagesKeys.EVENTS_VIEW_ICON);
					}
					
				}
				
				return ImagesUtils.getImage(IImagesKeys.CHANNELS_VIEW_ICON);
			}
		});
		signalsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			private int[] selectedIndices;
			private boolean inAllSignals;
			private int nbTrialsMin;
			private Object[] elements;
			private Subject subject;
			public void selectionChanged(final SelectionChangedEvent event) {
				final IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask(Messages.getString("TimeChartEditor.Update") + ((Chart)TimeChartEditor.this.getChart()).getLocalPath() + " : ", 3);
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								categoriesListViewer.getList().deselectAll();
							}
						});
						monitor.subTask(Messages.getString("TimeChartEditor.Compute"));
						IStructuredSelection selection = (IStructuredSelection)event.getSelection();
						nbTrialsMin = -1;
						IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
						IResource[] resources = (IResource[]) signalsTableViewer.getInput();
						elements = selection.toArray();
						for (int i = 0; i < resources.length; i++) {
							Subject subject = (Subject) resources[i];
							for (int j = 0; j < elements.length; j++) {
								String signal = (String) elements[j];
								int nbTrials = 0;
								if(signal.matches("^Event\\d+$")) {
									int[] trialsList = mathEngine.getEventsGroupUnduplicatedTrialsList(subject.getLocalPath() + "." + signal);
									nbTrials = Utils.getMaximum(trialsList);
								} else nbTrials = mathEngine.getNbTrials(subject.getLocalPath() + "." + signal);
								if(nbTrialsMin == -1) nbTrialsMin = nbTrials;
								else if(nbTrials < nbTrialsMin) nbTrialsMin = nbTrials;
							}
						}
						monitor.worked(1);
						monitor.subTask(Messages.getString("TimeChartEditor.UpdateTrials"));
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								selectedIndices = trialsTableViewer.getTable().getSelectionIndices();
								trialsTableViewer.getTable().removeAll();
								trialsTableViewer.setInput(nbTrialsMin==-1?null:nbTrialsMin);
							}
						});
						monitor.worked(1);
						monitor.subTask(Messages.getString("TimeChartEditor.CheckingPersistent"));
						for (int i = 0; i < resources.length; i++) {
							subject = (Subject) resources[i];
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									for (int j = 0; j< nbTrialsMin; j++) {
										inAllSignals = true;
										for (int k = 0; k < elements.length; k++) {
											String fullSignalName = subject.getLocalPath() + "." + (String) elements[k] + "." + (j+1);
											inAllSignals = inAllSignals && chart.getData().hasSignal(fullSignalName); 
										}
										trialsTableViewer.getTable().getItem(j).setChecked(inAllSignals);
									}
								}
							});
						}
						monitor.worked(1);
						monitor.subTask(Messages.getString("TimeChartEditor.CheckingNonPersistent"));
						if(selectedIndices.length > 0) {
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									trialsTableViewer.getTable().setSelection(selectedIndices);
									updateChart();
								}
							});
						}
						monitor.done();
					}
				};
				AnalyseApplication.getAnalyseApplicationWindow().run(true, true, false, true, runnable);
			}
		});
		
		Table table = new Table(sashForm, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		trialsTableViewer = new CheckboxTableViewer(table);
		trialsTableViewer.getTable().addFocusListener((FocusListener) getParent());
		trialsTableViewer.getTable().addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
			}
			public void focusGained(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
			}
		});
		trialsTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				String[] elements = new String[(Integer)inputElement];
				for (int i = 0; i < elements.length; i++) elements[i] = String.valueOf(i+1);
				return elements;
			}
		});
		trialsTableViewer.setLabelProvider(new ILabelProvider() {
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
				IResource[] resources = (IResource[]) signalsTableViewer.getInput();
				Object[] selectedChannels = (Object[]) ((IStructuredSelection) signalsTableViewer.getSelection()).toArray();
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				TreeSet<String> categoriesNames = new TreeSet<String>();
				for (int i = 0; i < resources.length; i++) {
					Subject subject = (Subject) resources[i];
					for (int j = 0; j < selectedChannels.length; j++) {
						String name = mathEngine.getCategoryNameFromTrial(subject.getLocalPath(), Integer.parseInt((String)element));
						if(!name.equals("")) categoriesNames.add(name);
					}
				}
				return Messages.getString("TrialNumber")  + element + ((categoriesNames.size()==1)?" - " + categoriesNames.first():"");
			}
			public Image getImage(Object element) {
				return null;
			}
		});
		trialsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				categoriesListViewer.getList().deselectAll();
				updateChart();
			}
		});
//		trialsTableViewer.addCheckStateListener(new ICheckStateListener() {
//			public void checkStateChanged(CheckStateChangedEvent event) {
//				updateChart();
//			}
//		});
		
		sashForm.setWeights(new int[]{50,50});
		
		signalsTabItem.setControl(signalsContainer);
		
	}

	public void update(int message, final IResource[] resources) {
		if(message == IResourceObserver.RENAMED) {
			if(chart == resources[0] || chart.hasParent(resources[0])) {
				setText(chart.getNameWithoutExtension());
				setToolTipText(chart.getLocalPath());
				subjectNameLabelCategories.setText(resources[0].getLocalPath());
				subjectNameLabelSignals.setText(resources[0].getLocalPath());
			}
			if(resources[0] instanceof Experiment || resources[0] instanceof Subject || resources[0] instanceof Signal) {
				if(resources[0] instanceof Signal) {
					trialsTableViewer.getTable().removeAll();
					signalsTableViewer.refresh();
				}
				String newlocalPath = resources[0].getLocalPath();
				String oldLocalPath = "";
				if(resources[0] instanceof Experiment) oldLocalPath = resources[0].getOldName();
				else oldLocalPath = resources[0].getParent().getLocalPath() + "." + resources[0].getOldName();
				subjectNameLabelSignals.setText(subjectNameLabelSignals.getText().replaceAll(oldLocalPath, newlocalPath));
				subjectNameLabelCategories.setText(subjectNameLabelCategories.getText().replaceAll(oldLocalPath, newlocalPath));
				ISeries[] series = swtChart.getSeriesSet().getSeries();
				for (int i = 0; i < series.length; i++) {
					String serieID = series[i].getId();
					if(serieID.startsWith(oldLocalPath)) {
						String newSerieID = serieID.replaceAll("^" + oldLocalPath, newlocalPath);
						double[] y = series[i].getYSeries();
						double[] x = series[i].getXSeries();
						Object[] markers = swtChart.getChartMarkersSet().getChartMarkers(serieID);
						for (int j = 0; j < markers.length; j++) ((ChartMarker)markers[j]).serieID = newSerieID;
						ILineSeries lineSerie = (ILineSeries) swtChart.createSerie(SeriesType.LINE, newSerieID);
						swtChart.getChartMarkersSet().addChartMarkers(markers);
						lineSerie.setSymbolType(PlotSymbolType.NONE);
						lineSerie.setLineColor(ColorsUtils.getRandomColor());
						lineSerie.setLineWidth(2);
						lineSerie.setYSeries(y);
						lineSerie.setXSeries(x);
						if(AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.CHARTS_ANTIALIASIS))
						lineSerie.setAntialias(SWT.ON);
						else lineSerie.setAntialias(SWT.OFF);
						swtChart.deleteSerie(serieID);
					}
				}
			}
		}
		if(message == IResourceObserver.DELETED) {
			for (int i = 0; i < resources.length; i++) {
				if(chart == resources[i] || chart.hasParent(resources[i])) {
					dispose();
					break;
				} else if(resources[i] == signalsTableViewer.getInput()) {
					signalsTableViewer.setInput(null);
					categoriesListViewer.setInput(null);
					signalsFilterTableViewer.setInput(null);
				}
				if(resources[i] instanceof Subject) {
					String localPath = resources[i].getLocalPath();
					ISeries[] series = swtChart.getSeriesSet().getSeries();
					for (int j = 0; j < series.length; j++) {
						String serieID = series[j].getId();
						if(serieID.startsWith(localPath)) swtChart.deleteSerie(serieID);
					}
					markersListViewer.refresh();
					swtChart.redraw();
				}
			}
		}
		if(message == IResourceObserver.LOADED) {
			if(signalsTableViewer.getInput() == null) {
				message = IResourceObserver.SELECTION_CHANGED;
			} else {
				IResource[] tableViewerResource = (IResource[]) signalsTableViewer.getInput();
				for (int i = 0; i < resources.length; i++) {
					for (int j = 0; j < tableViewerResource.length; j++) {
						if(resources[i] == tableViewerResource[j]) {
							String text = ((Subject)resources[i]).getLocalPath();
							if(!((Subject)resources[i]).isLoaded()) text = text + " (" + Messages.getString("NotLoaded") + ")";
							subjectNameLabelCategories.setText(text);
							subjectNameLabelSignals.setText(text);
							signalsTableViewer.refresh();
							categoriesListViewer.refresh();
							IResource resource = chart.getParent();
							while(!(resource instanceof Experiment)) resource = resource.getParent();
							currentMarkersLabelValueCombo.setItems(((Experiment)resource).getAllMarkersLabels());
						}
					}
					
				}
			}
		}
		if(message == IResourceObserver.PROCESS_RUN) {
			signalsTableViewer.refresh();
			categoriesListViewer.refresh();
			markersListViewer.refresh();
			IResource resource = chart.getParent();
			while(!(resource instanceof Experiment)) resource = resource.getParent();
			currentMarkersLabelValueCombo.setItems(((Experiment)resource).getAllMarkersLabels());
			swtChart.redraw();
		}
		if(message == IResourceObserver.SELECTION_CHANGED) {
			subjectNameLabelSignals.setText(Messages.getString("NONE"));
			subjectNameLabelCategories.setText(Messages.getString("NONE"));
			signalsTableViewer.getTable().removeAll();
			trialsTableViewer.getTable().removeAll();
			categoriesListViewer.getList().removeAll();
			if(resources.length >0) {
				IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
					private String text;
					public void run(IProgressMonitor monitor) throws InvocationTargetException,InterruptedException {
						monitor.beginTask(Messages.getString("TimeChartEditor.Update") + ((Chart)TimeChartEditor.this.getChart()).getLocalPath(), IProgressMonitor.UNKNOWN);
						monitor.subTask(Messages.getString("TimeChartEditor.ComputeText"));
						text = "";
						for (int i = 0; i < resources.length; i++) {
							Subject selectedSubject = (Subject) resources[i];
							text = text + selectedSubject.getLocalPath();
							if(!selectedSubject.isLoaded()) text = text + " (" + Messages.getString("NotLoaded") + ")";
							text = text + ", ";
						}
						text = text.replaceAll(", $", "");
						monitor.subTask(Messages.getString("TimeChartEditor.UpdateViews"));
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								subjectNameLabelCategories.setText(text);
								subjectNameLabelCategories.setToolTipText(text);

								subjectNameLabelSignals.setText(text);
								subjectNameLabelSignals.setToolTipText(text);

								signalsTableViewer.setInput(resources);
								categoriesListViewer.setInput(resources);
								signalsFilterTableViewer.setInput(resources);
							}
						});
						monitor.done();
					}
				};
				AnalyseApplication.getAnalyseApplicationWindow().run(true, true, false, true, runnableWithProgress);
			}
		}
		if(message == IResourceObserver.CHANNEL_DELETED || message == IResourceObserver.EVENT_DELETED) {
			IResource[] tableViewerResource = (IResource[]) signalsTableViewer.getInput();
			for (int i = 0; i < resources.length; i++) {
				for (int j = 0; j < tableViewerResource.length; j++) {
					if(tableViewerResource[j] == resources[i] && message == IResourceObserver.CHANNEL_DELETED) {
						signalsTableViewer.refresh();
						categoriesListViewer.refresh();
						markersListViewer.refresh();
					}
				}
				
			}
			Subject subject = (Subject) resources[0];
			IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
			String[] channelsNames = mathEngine.getChannelsNames(subject.getLocalPath());
			ISeries[] series = swtChart.getSeriesSet().getSeries();
			for (int i = 0; i < series.length; i++) {
				String serieID = series[i].getId();
				String fullChannelName = serieID.replaceAll("\\.\\d+$", "");
				
				boolean serieFound = false;
				for (int j = 0; j < channelsNames.length; j++) {
					serieFound = fullChannelName.equals(subject.getLocalPath() + "." + channelsNames[j]);
					if(serieFound) break;
				}
				if(!serieFound) {
					swtChart.deleteSerie(series[i].getId());
					if(message == IResourceObserver.EVENT_DELETED && fullChannelName.matches("^\\w+\\.\\w+\\.Event\\d+$")) {
						String eventName = fullChannelName.split("\\.")[2];
						String[] eventsNames = mathEngine.getEventsGroupsNames(subject.getLocalPath());
						if(Arrays.asList(eventsNames).indexOf(eventName) == -1) {
							signalsTableViewer.refresh();
							categoriesListViewer.refresh();
							markersListViewer.refresh();
						}
							
					}
				}
				else {
					if(message == IResourceObserver.EVENT_DELETED && fullChannelName.matches("^\\w+\\.\\w+\\.Event\\d+$")) {
						swtChart.deleteSerie(series[i].getId());
						int trialNumber = Integer.parseInt(serieID.replaceAll("\\w+\\.\\w+\\.\\w+\\.", ""));
						createSerie(fullChannelName, trialNumber, serieID);
					}
					swtChart.redraw();
				}
			}
		}
		if(message == IResourceObserver.CATEGORY_CREATED) {
			for (int i = 0; i < resources.length; i++) {
				if(resources[i] == signalsTableViewer.getInput()) {
					categoriesListViewer.refresh();
				}
			}
		}
		
		if(message == IResourceObserver.MARKER_DELETED || message == IResourceObserver.MARKERS_GROUP_DELETED || message == IResourceObserver.MARKER_ADDED
					  || message == IResourceObserver.MARKERS_GROUP_SYMBOL_CHANGED) {
			swtChart.getChartMarkersSet().clear();
			ISeries[] series = swtChart.getSeriesSet().getSeries();
			for (int i = 0; i < series.length; i++) {
				String fullSignalName = series[i].getId().replaceAll("\\.\\d+$", "");
				int trialNumber = Integer.parseInt(series[i].getId().split("\\.")[3]);
				populateMarkers(fullSignalName, trialNumber, series[i].getId());
			}
			markersListViewer.refresh();
			swtChart.redraw();
		}
		
		if(message == IResourceObserver.MARKER_LABEL_ADDED) {
			currentMarkersLabelValueCombo.removeAll();
			IResource resource = chart.getParent();
			while (!(resource instanceof Experiment)) resource = resource.getParent();
			currentMarkersLabelValueCombo.setItems(((Experiment)resource).getAllMarkersLabels());
		}
		
		if(message == IResourceObserver.REFACTORED) {
			if(chart == resources[0]) {
				clearSWTChart();
				initChart();
			}
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		HashSet<Subject> selectedSubjects = new HashSet<Subject>(0);
		Object[] objects = ((TreeSelection)event.getSelection()).toArray();
		for (int i = 0; i < objects.length; i++) {
			if(objects[i] instanceof Subject) {
				//if(((Subject)objects[i]).isLoaded()) {
					selectedSubjects.add((Subject) objects[i]);
				//}
			}
		}
		update(IResourceObserver.SELECTION_CHANGED, selectedSubjects.toArray(new IResource[selectedSubjects.size()]));
	}

	@SuppressWarnings("unchecked")
	private void updateChart() {
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		DataChart dataChart = chart.getData();
		ArrayList<String> transientSignals = new ArrayList<String>(0);
		IResource[] resources = (IResource[]) signalsTableViewer.getInput();
		
		for (int numSubject = 0; numSubject < resources.length; numSubject++) {
			Subject subject = (Subject) resources[numSubject];

			boolean nextPreviousOnlySignals = AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.NEXT_PREVIOUS_ONLY_SIGNALS);
			if(chartOptionsTabFolder.getSelection() == signalsTabItem || nextPreviousOnlySignals) {
				Object[] selectedSignals = ((IStructuredSelection)signalsTableViewer.getSelection()).toArray();
				for (int i = 0; i < selectedSignals.length; i++) {
					String fullSignalName  = subject.getLocalPath() + "." + (String)selectedSignals[i];
					for (int j = 0; j <  trialsTableViewer.getTable().getItemCount(); j++) {
						boolean trialChecked = trialsTableViewer.getTable().getItem(j).getChecked();
						boolean trialSelected = trialsTableViewer.getTable().isSelected(j);
						if(trialChecked) dataChart.addSignal(fullSignalName + "." + (j+1) );
						else if(trialSelected) if(transientSignals.indexOf(fullSignalName + "." + (j+1)) == -1) transientSignals.add(fullSignalName + "." + (j+1) );
						if(!trialChecked && !trialSelected) dataChart.removeSignal(fullSignalName + "." + (j+1));
					}
				}
			}
			
			if(subject != null) {
//				String[] signalsNamesTemp = subject.getSignalsNames();//mathEngine.getSignalsNames(subject.getLocalPath());
//				ArrayList<String> signalsNames = new ArrayList<String>(0);
//				signalsNames.addAll(Arrays.asList(signalsNamesTemp));
				IStructuredSelection signalsSelection = (IStructuredSelection)signalsFilterTableViewer.getSelection();
				if(chartOptionsTabFolder.getSelection() == categoriesTabItem) {
					for (int i = 0; i < categoriesListViewer.getList().getItemCount(); i++) {
						if(categoriesListViewer.getList().isSelected(i)) {
							String categoryName = categoriesListViewer.getList().getItem(i).split("-")[0];
							categoryName = categoryName.replaceAll("(\\s)*$","");
							int[] trialsList = mathEngine.getTrialsListForCategory(subject.getLocalPath() + "." + categoryName);
							for (Iterator<String> iterator = signalsSelection.iterator(); iterator.hasNext();) {
								String signalName = iterator.next();
								for (int k = 0; k < trialsList.length; k++) 
									dataChart.addSignal(subject.getLocalPath() + "." + signalName + "." + trialsList[k]);
							}
						}
					}
				}
			}
		}
		
		//Remove signals from chart
		ISeries[] series = swtChart.getSeriesSet().getSeries();
		for (int i = 0; i < series.length; i++) {
			boolean keepSerie = dataChart.hasSignal(series[i].getId());
			keepSerie = keepSerie || (transientSignals.indexOf(series[i].getId()) != -1);
			if(!keepSerie) swtChart.deleteSerie(series[i].getId());
		}
		
		//Add new transient signals to chart
		String[] signalsString = transientSignals.toArray(new String[transientSignals.size()]);
		for (int i = 0; i < signalsString.length; i++) {
			//Update chart
			if(swtChart.getSeriesSet().getSeries(signalsString[i]) == null) {
				String fullSignalName = signalsString[i].replaceAll("\\.\\d+$", "");
				int trialNumber =  Integer.parseInt(signalsString[i].split("\\.")[3]);
				createSerie(fullSignalName, trialNumber, signalsString[i]);
			}
		}
		
		//Add new signals to chart
		signalsString = dataChart.getSignals();
		for (int i = 0; i < signalsString.length; i++) {
			//Update chart
			if(swtChart.getSeriesSet().getSeries(signalsString[i]) == null) {
				String fullSignalName = signalsString[i].replaceAll("\\.\\d+$", "");
				int trialNumber =  Integer.parseInt(signalsString[i].split("\\.")[3]);
				createSerie(fullSignalName, trialNumber, signalsString[i]);
			}
		}
		
		if(dataChart.isAutoAdjustAxis()) swtChart.getAxisSet().adjustRange();
		updateRange();
		
		if(saveSeriesColors.length > 0) {
			series = swtChart.getSeriesSet().getSeries();
			String[] seriesID = new String[series.length];
			for (int i = 0; i < series.length; i++) seriesID[i] = series[i].getId();
			Arrays.sort(seriesID);
			for (int i = 0; i < seriesID.length; i++) if(i < saveSeriesColors.length) ((ILineSeries)swtChart.getSeriesSet().getSeries(seriesID[i])).setLineColor(saveSeriesColors[i]);
			saveSeriesColors = new Color[0];
		}
		
		swtChart.redraw();
		chart.saveChart();
		markersListViewer.setInput(swtChart.getChartMarkersSet());
	}
	
	public void initChart() {
		DataChart dataChart = chart.getData();
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		String[] signalsAndTrials = dataChart.getSignals();
		for (int i = 0; i < signalsAndTrials.length; i++) {
			String fullSignalName = signalsAndTrials[i].replaceAll("\\.\\d+$", "");
			if(mathEngine.isChannelExists(fullSignalName)) {
				int trialNumber =  Integer.parseInt(signalsAndTrials[i].split("\\.")[3]);
				createSerie(fullSignalName, trialNumber, signalsAndTrials[i]);
			} else Log.logErrorMessage(Messages.getString("TimeChartEditor.DataFor") + signalsAndTrials[i] + Messages.getString("TimeChartEditor.DoesNotExist"));
		}
		updateChannelsPaletteVisiblility();
		serieLabel.setText("");
		swtChart.getAxisSet().getXAxes()[0].setRange(dataChart.getXRange());
		swtChart.getAxisSet().getYAxes()[0].setRange(dataChart.getYRange());
		swtChart.getLegend().setVisible(dataChart.isLegendVisible());
		swtChart.setCrossHairVisibility(dataChart.isCrossHairVisible());
		swtChart.setShowMarkers(dataChart.isMarkersVisible());
		markersListViewer.setInput(swtChart.getChartMarkersSet());
		currentMarkersLabelValueCombo.removeAll();
		IResource resource = chart.getParent();
		while (!(resource instanceof Experiment)) resource = resource.getParent();
		currentMarkersLabelValueCombo.setItems(((Experiment)resource).getAllMarkersLabels());
	}
	
	private void createSerie(String fullSignalName, int trialNumber, String serieID) {
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		if(!fullSignalName.matches("^\\w+\\.\\w+\\.Event\\d+$")) {
			double sf = mathEngine.getSampleFrequency(fullSignalName);
			double[] y = mathEngine.getValuesForTrialNumber(trialNumber,fullSignalName);
			int[] frontCut = mathEngine.getFrontCut(fullSignalName);
			double[] x = new double[y.length];
			for (int j = 0; j < x.length; j++) x[j] = (frontCut[trialNumber-1] - 1 + j)/sf;
			ILineSeries lineSerie = (ILineSeries) swtChart.createSerie(SeriesType.LINE, serieID);
			lineSerie.setSymbolType(PlotSymbolType.NONE);
			
			Color serieColor = ColorsUtils.getRandomColor();
			if(chartOptionsTabFolder.getSelection() == categoriesTabItem) {
				if(!categoriesListViewer.getStructuredSelection().isEmpty()) {
					IStructuredSelection selection = categoriesListViewer.getStructuredSelection();
					for (Object item : selection) {
						Object color = categoriesListViewer.getData(item.toString());
						int[] trials = mathEngine.getTrialsListForCategory(item.toString());
						System.out.println(color);
						System.out.println(trials);
					}
				}
			}
			
			lineSerie.setLineColor(ColorsUtils.getRandomColor());
			lineSerie.setLineWidth(2);
			lineSerie.setYSeries(y);
			lineSerie.setXSeries(x);
			if(AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.CHARTS_ANTIALIASIS))
			lineSerie.setAntialias(SWT.ON);
			else lineSerie.setAntialias(SWT.OFF);
			populateMarkers(fullSignalName, trialNumber, serieID);
			swtChart.setActiveSerieId(serieID);
		} else {
			double[][] values = mathEngine.getEventsGroupValuesForTrialNumber(trialNumber, fullSignalName);
			double[] x = new double[values.length];
			double[] y = new double[values.length];
			double[] h = new double[values.length];
			for (int i = 0; i < values.length; i++) {
				x[i] = values[i][0];
				y[i] = values[i][1];
				h[i] = values[i][2];
				if(Double.isNaN(y[i]) && Double.isInfinite(h[i])) y[i] = 0; 
			}
			IEventSeries eventSerie = (IEventSeries) swtChart.createSerie(SeriesType.EVENT, serieID);
			eventSerie.setSymbolType(PlotSymbolType.NONE);
			eventSerie.setLineColor(ColorsUtils.getRandomColor());
			eventSerie.setLineWidth(2);
			eventSerie.setYSeries(y);
			eventSerie.setXSeries(x);
			eventSerie.setHSeries(h);
			eventSerie.setShowHeightAsInfinite(chart.getData().isShowEventsAsInfinite());
			if(AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.CHARTS_ANTIALIASIS))
			eventSerie.setAntialias(SWT.ON);
			else eventSerie.setAntialias(SWT.OFF);
		}
		
	}
	
	private void populateMarkers(String fullSignalName, int trialNumber, String serieID) {
		if(!fullSignalName.matches("^\\w+\\.\\w+\\.Event\\d+$")) {
			IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
			double sf = mathEngine.getSampleFrequency(fullSignalName);
			int nbMarkersGroups = mathEngine.getNbMarkersGroups(fullSignalName);
			for (int j = 0; j < nbMarkersGroups; j++) {
				String label = mathEngine.getMarkersGroupLabel(j+1, fullSignalName);
				double[] xMarkersValues = mathEngine.getXValuesForMarkersGroup(j+1,fullSignalName);
				int[] trialsMarkersValues = mathEngine.getTrialsListForMarkersGroup(j+1, fullSignalName);
				String symbolName = mathEngine.getMarkersGroupGraphicalSymbol(j+1, fullSignalName);
				for (int k = 0; k < xMarkersValues.length; k++) {
					if(trialsMarkersValues[k] == trialNumber) {
						int index = (int) Math.round(xMarkersValues[k]*sf);
						swtChart.getChartMarkersSet().addChartMarker(j+1, index, serieID, label, symbolName);
					}
				}
			}
		}
	}

	public void updateRange() {
		double xmin = swtChart.getAxisSet().getXAxes()[0].getRange().lower;
		double xmax = swtChart.getAxisSet().getXAxes()[0].getRange().upper;
		double ymin = swtChart.getAxisSet().getYAxes()[0].getRange().lower;
		double ymax = swtChart.getAxisSet().getYAxes()[0].getRange().upper;
		chart.getData().setRange(xmin, xmax, ymin, ymax);
		chart.saveChart();
	}

	public void updateCoordinates(double x, double y, String label) {
		serieLabel.setText(label);
	}

	@Override
	public void clearChart() {
		super.clearChart();
		Table trialsTable = trialsTableViewer.getTable();
		for (int i = 0; i < trialsTable.getItemCount(); i++) trialsTable.getItem(i).setChecked(false);
		trialsTable.setSelection(-1);
		categoriesListViewer.getList().setSelection(new String[0]);
		initChart();
	}
	
}
