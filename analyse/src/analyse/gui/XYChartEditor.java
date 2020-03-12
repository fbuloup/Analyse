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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ext.ChartMarker.MARKER_GRAPHIC_SYMBOL;
import org.swtchart.ext.ICoordinatesObserver;
import org.swtchart.ext.IRangeObserver;
import org.swtchart.ext.InteractiveChart;

import analyse.AnalyseApplication;
import analyse.gui.dialogs.RefactorDialog;
import analyse.model.Chart;
import analyse.model.DataChart;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Subject;
import analyse.preferences.AnalysePreferences;
import analyse.resources.ColorsUtils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public final class XYChartEditor extends ChartEditor implements IResourceObserver, ISelectionChangedListener, IRangeObserver, ICoordinatesObserver {

	
	private class AddAction extends Action {
		public AddAction() {
			super(Messages.getString("XYChartEditor.Add"),Action.AS_PUSH_BUTTON); 
			setToolTipText(Messages.getString("XYChartEditor.Add")); 
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.ADD_ICON));
		}
		@Override
		public void run() {
			if(xSignalCombo.getData() != null) {
				Subject subject = (Subject) xSignalCombo.getData();
				if(subject.isLoaded()) {
					String xSignal = xSignalCombo.getText();
					String ySignal = ySignalCombo.getText();
					if(!xSignal.equals("") && !ySignal.equals("")) {
						String signal = subject.getLocalPath() + "." + ySignal + ":" + subject.getLocalPath() + "." + xSignal;
						Object[] input = (Object[]) signalsTableViewer.getInput();
						if(input != null) {
							for (int i = 0; i < input.length; i++) if(((String)input[i]).equals(signal)) return; 
							String[] newInput = new String[input.length + 1];
							System.arraycopy(input, 0, newInput, 0, input.length);
							newInput[newInput.length - 1] = signal;
							signalsTableViewer.getTable().deselectAll();
							signalsTableViewer.setInput(newInput);
						} else {
							signalsTableViewer.getTable().deselectAll();
							signalsTableViewer.setInput(new String[]{signal});
						}
					}
				}
			}
		}
	}
	
	private class RemoveAction extends Action {
		public RemoveAction() {
			super(Messages.getString("XYChartEditor.Remove"),Action.AS_PUSH_BUTTON); 
			setToolTipText(Messages.getString("XYChartEditor.Remove")); 
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.DELETE_ICON));
		}
		@Override
		public void run() {
			DataChart dataChart = chart.getData();
			Object[] selectedSignals = ((IStructuredSelection)signalsTableViewer.getSelection()).toArray();
			Object[] input = (Object[]) signalsTableViewer.getInput();
			ArrayList<String> signalsToKeep = new ArrayList<String>(0);
			for (int i = 0; i < input.length; i++) signalsToKeep.add((String) input[i]);
			for (int i = 0; i < selectedSignals.length; i++) {
				for (int j = 0; j <  trialsTableViewer.getTable().getItemCount(); j++) {
					boolean trialChecked = trialsTableViewer.getTable().getItem(j).getChecked();
					if(trialChecked) dataChart.removeSignal((String)selectedSignals[i] + "." + (j+1));
				}
				if(input != null) {
					boolean found = false;
					for (int j = 0; j < input.length; j++) {
						if(selectedSignals[i].equals(((String)input[j]))) {
							found = true;
							break; 
						}
					}
					if(found) signalsToKeep.remove((String) selectedSignals[i]);
				}
			}
			signalsTableViewer.getTable().deselectAll();
			signalsTableViewer.setInput(signalsToKeep.toArray(new String[signalsToKeep.size()]));
			trialsTableViewer.setInput(null);
			updateChart();
		}
	}
	
	private class NextAction extends Action {
		public NextAction() {
			super(Messages.getString("Next"),Action.AS_PUSH_BUTTON); 
			setToolTipText(Messages.getString("Next")); 
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.NEXT_TRIAL_ICON));
		}
		@Override
		public void run() {
			CTabItem selectedTabItem = chartOptionsTabFolder.getSelection();
			if(selectedTabItem == signalsTabItem) {
				int[] indices = trialsTableViewer.getTable().getSelectionIndices();
				boolean valid = true;
				for (int i = 0; i < indices.length; i++) {
					indices[i] = indices[i] + 1;
					if(indices[i] > trialsTableViewer.getTable().getItemCount()) {
						valid = false;
						break;
					}
				}
				if(valid) trialsTableViewer.getTable().setSelection(indices);
				updateChart();
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
				if(valid) trialsTableViewer.getTable().setSelection(indices);
				updateChart();
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
				XYChartEditor.this.updateChannelsPaletteVisiblility();
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
		
		public ChartOptionsAction() {
			super(Messages.getString("ChartOptionsActions.Properties"),Action.AS_DROP_DOWN_MENU);
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.CHART_OPTIONS_ICON));
			setMenuCreator(new IMenuCreator() {
				public Menu getMenu(Menu parent) {
					return null;
				}
				public Menu getMenu(Control parent) {
					if (chartOptionsMenu != null) chartOptionsMenu.dispose(); 
					chartOptionsMenu = new Menu(parent);
					ShowLegendAction showLegendAction = new ShowLegendAction();
					addActionToMenu(chartOptionsMenu, showLegendAction); 
					ShowMarkersAction showMarkersAction = new ShowMarkersAction();
					addActionToMenu(chartOptionsMenu, showMarkersAction);
					ShowChannelsPaletteAction showChannelsPaletteAction = new ShowChannelsPaletteAction();
					addActionToMenu(chartOptionsMenu, showChannelsPaletteAction); 				
					AutoAdjustXYAxisAction autoAdjustXYAxisAction = new AutoAdjustXYAxisAction();
					addActionToMenu(chartOptionsMenu, autoAdjustXYAxisAction); 	
					showLegendAction.setChecked(chart.getData().isLegendVisible());
					showChannelsPaletteAction.setChecked(chart.getData().isChannelsPaletteVisible());
					autoAdjustXYAxisAction.setChecked(chart.getData().isAutoAdjustAxis());
					showMarkersAction.setChecked(chart.getData().isMarkersVisible());
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
	
	private CLabel subjectNameLabelSignals;
	private TableViewer signalsTableViewer;
	private CheckboxTableViewer trialsTableViewer;
	private CTabItem signalsTabItem;
	private ActionContributionItem chartOptionsAction;
	private Combo xSignalCombo;
	private Combo ySignalCombo;

	public XYChartEditor(CTabFolder parent, int style, final Chart chart) {
		super(parent, style);
		this.chart = chart;
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Experiments.getInstance().removeExperimentObserver(XYChartEditor.this);
				AnalyseApplication.getAnalyseApplicationWindow().removeSelectionChangedListenerFromExperimentsView(XYChartEditor.this);
				if(chartOptionsAction != null) chartOptionsAction.dispose();
				disposeToolBar();
				sashForm.dispose();
			}
		});
		createContents();
		swtChart.addRangeObserver(this);
		swtChart.addCoordinatesObserver(this);
		initChart();
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
		chartContainerLayout.marginTop = 2;
		chartContainerLayout.marginHeight = 0;
		chartContainerLayout.marginWidth = 0;
//		serieLabel = new CLabel(chartContainer, SWT.NONE);
//		serieLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
//		serieLabel.setText("No serie selected");
		swtChart = new InteractiveChart(chartContainer, SWT.NONE, false);
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
		signalsTabItem.setText(Messages.getString("ChartOptionsActions.Signals"));
		createSignalsContents(signalsTabItem);
		
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT | SWT.WRAP);
		toolBarManager.add(new AddAction());
		toolBarManager.add(new RemoveAction());
		toolBarManager.add(new Separator());
		toolBarManager.add(new PreviousAction());
		toolBarManager.add(new NextAction());
		ToolBar toolBar = toolBarManager.createControl(chartOptionsTabFolder);
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

	private void createSignalsContents(CTabItem signalsTabItem) {
		Composite signalsContainer = new Composite(signalsTabItem.getParent(), SWT.NONE);
		signalsContainer.setLayout(new GridLayout(2,false));
		
		CLabel selectedSubjectlabel = new CLabel(signalsContainer, SWT.NONE);
		selectedSubjectlabel.setText(Messages.getString("ChartOptionsActions.SelectedSubject"));
		selectedSubjectlabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		
		subjectNameLabelSignals = new CLabel(signalsContainer, SWT.NONE);
		subjectNameLabelSignals.setText(Messages.getString("NONE"));
		subjectNameLabelSignals.setToolTipText(Messages.getString("ChartOptionsActions.SelectSubject"));
		subjectNameLabelSignals.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		
		CLabel explainLabel = new CLabel(signalsContainer, SWT.NONE);
		explainLabel.setText(Messages.getString("XYChartEditor.ExplainLabel"));
		explainLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,2,1));
		
		Composite subContainer = new Composite(signalsContainer, SWT.NONE);
		subContainer.setLayout(new GridLayout(4,false));
		GridLayout gridLayout = (GridLayout) subContainer.getLayout();
		gridLayout.marginHeight = 0;
		subContainer.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,2,1));
		
		Label yLabel = new Label(subContainer, SWT.NONE);
		yLabel.setText("Y : ");
		yLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		ySignalCombo = new Combo(subContainer, SWT.READ_ONLY);
		ySignalCombo.setVisibleItemCount(50);
		ySignalCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		ySignalCombo.addFocusListener((View)getParent());
		ySignalCombo.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
			}
			public void focusGained(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
			}
		});
		
		Label xLabel = new Label(subContainer, SWT.NONE);
		xLabel.setText("X : ");
		xLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		xSignalCombo = new Combo(subContainer, SWT.READ_ONLY);
		xSignalCombo.setVisibleItemCount(50);
		xSignalCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		xSignalCombo.addFocusListener((View)getParent());
		xSignalCombo.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
			}
			public void focusGained(FocusEvent e) {
				chartOptionsTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				chartOptionsTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
			}
		});
				
		SashForm sashForm = new SashForm(signalsContainer, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
		
		signalsTableViewer = new TableViewer(sashForm);
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
				if(inputElement instanceof Object[]) return (Object[]) inputElement;
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
				String[] signalsNames = ((String)element).split(":");
				String subjectName = signalsNames[0].replaceAll("\\.\\w+$", "");
				signalsNames[0] = signalsNames[0].replaceAll("^\\w+\\.\\w+\\.", "");
				signalsNames[1] = signalsNames[1].replaceAll("^\\w+\\.\\w+\\.", "");
				return (String) subjectName + " [" + signalsNames[0] + ":" + signalsNames[1] + "]";
			}
			public Image getImage(Object element) {
				return null;
			}
		});
		signalsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			private int nbTrialsMin = Integer.MAX_VALUE;
			private boolean inAllSignals = true;
			public void selectionChanged(final SelectionChangedEvent event) {
				final IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask(Messages.getString("XYChartEditor.Update") + ((Chart)XYChartEditor.this.getChart()).getLocalPath() + " : ", 3);
						monitor.subTask(Messages.getString("XYChartEditor.Compute"));
						IStructuredSelection selection = (IStructuredSelection)event.getSelection();
						
						IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
						Object[] elements = selection.toArray();
						for (int i = 0; i < elements.length; i++) {
							String[] signals = ((String) elements[i]).split(":");
							Subject subject1 = (Subject) Experiments.getInstance().getSubjectByName(signals[0].replaceAll("\\.\\w+$", ""));
							Subject subject2 = (Subject) Experiments.getInstance().getSubjectByName(signals[1].replaceAll("\\.\\w+$", ""));
							if(subject1.isLoaded() && subject2.isLoaded()) {
								int nbTrials1 = mathEngine.getNbTrials(signals[0]);
								int nbTrials2 = mathEngine.getNbTrials(signals[1]);
								nbTrialsMin = Math.min(nbTrialsMin, Math.min(nbTrials1, nbTrials2));
							}
						}
						monitor.worked(1);
						monitor.subTask(Messages.getString("XYChartEditor.UpdateTrials"));
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								trialsTableViewer.getTable().removeAll();
								trialsTableViewer.setInput(nbTrialsMin==Integer.MAX_VALUE?null:nbTrialsMin);
							}
						});
						monitor.worked(1);
						monitor.subTask(Messages.getString("XYChartEditor.CheckingPersistent"));
						if(nbTrialsMin < Integer.MAX_VALUE)
							for (int i = 0; i < nbTrialsMin; i++) {
								inAllSignals = true;
								for (int j = 0; j < elements.length; j++) {
									String fullSignalName = (String)elements[j] + "." + (i+1);
									inAllSignals = inAllSignals && chart.getData().hasSignal(fullSignalName); 
								}
								final int index = i;
								Display.getDefault().syncExec(new Runnable() {
									public void run() {
										trialsTableViewer.getTable().getItem(index).setChecked(inAllSignals);
									}
								});
								
							}
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
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				Object[] curvesNames = (Object[]) signalsTableViewer.getInput();
				
				TreeSet<String> categoriesNames = new TreeSet<String>();
				
				for (int i = 0; i < curvesNames.length; i++) {
					String xCurveName = ((String)curvesNames[i]).split(":")[1];
					String yCurveName = ((String)curvesNames[i]).split(":")[0];
					String xSubjectName = xCurveName.replaceAll("\\.\\w+$", "");
					String ySubjectName = yCurveName.replaceAll("\\.\\w+$", "");
					if(xSubjectName.equals(ySubjectName)) {
						String name = mathEngine.getCategoryNameFromTrial(xSubjectName, Integer.parseInt((String)element));
						if(!name.equals("")) categoriesNames.add(name);
					} else {
						String xCategoryName = mathEngine.getCategoryNameFromTrial(xSubjectName, Integer.parseInt((String)element));
						String yCategoryName = mathEngine.getCategoryNameFromTrial(xSubjectName, Integer.parseInt((String)element));
						if(xCategoryName.equals(yCategoryName) && !xCategoryName.equals("")) categoriesNames.add(xCategoryName);
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
	
	public void update(int message, IResource[] resources) {
		if(message == IResourceObserver.RENAMED) {
			if(chart == resources[0] || chart.hasParent(resources[0])) {
				setText(chart.getNameWithoutExtension());
				setToolTipText(chart.getLocalPath());
				subjectNameLabelSignals.setText(resources[0].getLocalPath());
			}
			if(resources[0] instanceof Experiment || resources[0] instanceof Subject) {
				String newlocalPath = resources[0].getLocalPath();
				String oldLocalPath = "";
				if(resources[0] instanceof Subject) oldLocalPath = resources[0].getParent().getLocalPath() + "." + resources[0].getOldName();
				else oldLocalPath = resources[0].getOldName();
				subjectNameLabelSignals.setText(subjectNameLabelSignals.getText().replaceAll(oldLocalPath, newlocalPath));
				ISeries[] series = swtChart.getSeriesSet().getSeries();
				for(int i = 0; i < series.length; i++) {
					String serieID = series[i].getId();
					String[] serieIDs = serieID.split(":");
					if(serieIDs[0].startsWith(oldLocalPath) || serieIDs[1].startsWith(oldLocalPath)) {
						String newSerieID = serieIDs[0].replaceAll("^" + oldLocalPath, newlocalPath) + ":" + serieIDs[1].replaceAll("^" + oldLocalPath, newlocalPath);
						double[] y = series[i].getYSeries();
						double[] x = series[i].getXSeries();
						ILineSeries lineSerie = (ILineSeries) swtChart.createSerie(SeriesType.LINE, newSerieID);
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
				populateSignalsTableViewer();
			}
		}
		if(message == IResourceObserver.DELETED) {
			for (int i = 0; i < resources.length; i++) {
				if(chart == resources[i] || chart.hasParent(resources[i])) {
					dispose();
					break;
				} else if(resources[i] == xSignalCombo.getData()) {
					xSignalCombo.setData(null);
					refreshCombos();
					populateSignalsTableViewer();
				}
				if(resources[i] instanceof Subject) {
					String localPath = resources[i].getLocalPath();
					ISeries[] series = swtChart.getSeriesSet().getSeries();
					for (int j = 0; j < series.length; j++) {
						String[] serieIDs = series[j].getId().split(":");
						if(serieIDs[0].startsWith(localPath)) swtChart.deleteSerie(series[j].getId());
						else if(serieIDs[1].startsWith(localPath)) swtChart.deleteSerie(series[j].getId());
					}
					swtChart.redraw();
					populateSignalsTableViewer();
				}
			}
		}
		if(message == IResourceObserver.LOADED) {
			for (int i = 0; i < resources.length; i++) {
				if(resources[i] == xSignalCombo.getData()) {
					String text = ((Subject)resources[i]).getLocalPath();
					if(!((Subject)resources[i]).isLoaded()) text = text + Messages.getString("ChartOptionsActions.NotLoaded");
					subjectNameLabelSignals.setText(text);
					refreshCombos();
				}
			}
			populateSignalsTableViewer();
		}
		
		if(message == IResourceObserver.SELECTION_CHANGED) {
			subjectNameLabelSignals.setText(Messages.getString("NONE"));
			if(resources[0] != null) {
				Subject selectedSubject = (Subject) resources[0];
				String text = selectedSubject.getLocalPath();
				if(!selectedSubject.isLoaded()) text = text + Messages.getString("ChartOptionsActions.NotLoaded");
				subjectNameLabelSignals.setText(text);
			}
			xSignalCombo.setData(resources[0]);
			refreshCombos();
			populateSignalsTableViewer();
		}
		if(message == IResourceObserver.CHANNEL_DELETED) {
			if(resources[0] == xSignalCombo.getData()) {
				refreshCombos();
			}
			Subject subject = (Subject) resources[0];
			IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
			String[] channelsNames = mathEngine.getChannelsNames(subject.getLocalPath());
			ISeries[] series = swtChart.getSeriesSet().getSeries();
			for (int i = 0; i < series.length; i++) {
				String[] serieIDs = series[i].getId().split(":");
				for (int j = 0; j < serieIDs.length; j++) {
					String fullChannelName = serieIDs[j].replaceAll("\\.\\d+$", "");
					boolean serieFound = false;
					for (int k = 0; k < channelsNames.length; k++) {
						serieFound = fullChannelName.equals(subject.getLocalPath() + "." + channelsNames[k]);
						if(serieFound) break;
					}
					if(!serieFound) swtChart.deleteSerie(series[i].getId());
				}
				
			}
			populateSignalsTableViewer();
		}
//		if(message == IResourceObserver.CATEGORY_CREATED) {
//		}
//		
//		if(message == IResourceObserver.MARKER_DELETED || message == IResourceObserver.MARKERS_GROUP_DELETED || message == IResourceObserver.MARKER_ADDED) {
//		}
		
		
		if(message == IResourceObserver.MARKER_DELETED || message == IResourceObserver.MARKERS_GROUP_DELETED || message == IResourceObserver.MARKER_ADDED
				  || message == IResourceObserver.MARKERS_GROUP_SYMBOL_CHANGED) {
			swtChart.getChartMarkersSet().clear();
			ISeries[] series = swtChart.getSeriesSet().getSeries();
			for (int i = 0; i < series.length; i++) {
				String serieID = series[i].getId();
				String fullSignalNames = serieID.replaceAll("\\.\\d+$", "");
				int trialNumber = Integer.parseInt(serieID.split(":")[1].split("\\.")[3]);
				populateMarkers(fullSignalNames.split(":")[0], trialNumber, serieID);
				populateMarkers(fullSignalNames.split(":")[1], trialNumber, serieID);
			}
			swtChart.redraw();
		}
		
//		if(message == IResourceObserver.MARKER_LABEL_ADDED) {
//		}
		
		if(message == IResourceObserver.REFACTORED) {
			if(chart == resources[0]) {
				clearSWTChart();
				initChart();
			}
		}
	}

	private void refreshCombos() {
		if(xSignalCombo.getData() != null) {
			Subject subject = (Subject)xSignalCombo.getData();
			if(subject.isLoaded()) {
				//IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				String[] signalsNames = subject.getSignalsNames();//mathEngine.getSignalsNames(subject.getLocalPath());
				Arrays.sort(signalsNames);
				xSignalCombo.setItems(signalsNames);
				ySignalCombo.setItems(signalsNames);
			} else {
				xSignalCombo.removeAll();
				ySignalCombo.removeAll();
				signalsTableViewer.getTable().removeAll();
				trialsTableViewer.getTable().removeAll();
			}
		} else {
			xSignalCombo.removeAll();
			ySignalCombo.removeAll();
			signalsTableViewer.getTable().removeAll();
			trialsTableViewer.getTable().removeAll();
		}
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
		Subject selectedSubject = null;
		Object[] objects = ((TreeSelection)event.getSelection()).toArray();
		for (int i = 0; i < objects.length; i++) {
			if(objects[i] instanceof Subject) {
				if(selectedSubject == null) selectedSubject = (Subject) objects[i];
				if(((Subject)objects[i]).isLoaded()) {
					selectedSubject = (Subject) objects[i];
					break;
				}
			}
		}
		update(IResourceObserver.SELECTION_CHANGED, new IResource[]{selectedSubject});
	}

	private void updateChart() {
		DataChart dataChart = chart.getData();
		
		ArrayList<String> transientSignals = new ArrayList<String>(0);

		Object[] selectedSignals = ((IStructuredSelection)signalsTableViewer.getSelection()).toArray();
		for (int i = 0; i < selectedSignals.length; i++) {
			for (int j = 0; j <  trialsTableViewer.getTable().getItemCount(); j++) {
				boolean trialChecked = trialsTableViewer.getTable().getItem(j).getChecked();
				boolean trialSelected = trialsTableViewer.getTable().isSelected(j);
				if(trialChecked) dataChart.addSignal((String)selectedSignals[i] + "." + (j+1) );
				else if(trialSelected) if(transientSignals.indexOf((String)selectedSignals[i] + "." + (j+1)) == -1) transientSignals.add((String)selectedSignals[i] + "." + (j+1) );
				if(!trialChecked && !trialSelected) dataChart.removeSignal((String)selectedSignals[i] + "." + (j+1));
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
				String fullSignalNames = signalsString[i].replaceAll("\\.\\d+$", "");
				int trialNumber =  Integer.parseInt(signalsString[i].split(":")[1].split("\\.")[3]);
				createSerie(fullSignalNames, trialNumber, signalsString[i]);
			}
		}
		
		//Add new signals to chart
		signalsString = dataChart.getSignals();
		for (int i = 0; i < signalsString.length; i++) {
			//Update chart
			if(swtChart.getSeriesSet().getSeries(signalsString[i]) == null) {
				String fullSignalNames = signalsString[i].replaceAll("\\.\\d+$", "");
				int trialNumber =  Integer.parseInt(signalsString[i].split(":")[1].split("\\.")[3]);
				createSerie(fullSignalNames, trialNumber, signalsString[i]);
			}
		}
		
		if(dataChart.isAutoAdjustAxis()) swtChart.getAxisSet().adjustRange();
		updateRange();
		swtChart.setActiveSerieId(null);
		swtChart.redraw();
		chart.saveChart();
	}
	
	private void populateSignalsTableViewer() {
		DataChart dataChart = chart.getData();
		String[] signalsAndTrials = dataChart.getSignals();
		HashSet<String> signals = new HashSet<String>(signalsAndTrials.length);
		for (int i = 0; i < signalsAndTrials.length; i++) signals.add(signalsAndTrials[i].replaceAll("\\.\\d+$", ""));
		signalsTableViewer.setInput(signals.toArray());
	}
	
	public void initChart() {
		populateSignalsTableViewer();
		DataChart dataChart = chart.getData();
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		String[] signalsAndTrials = dataChart.getSignals();
		for (int i = 0; i < signalsAndTrials.length; i++) {
			String fullSignalNames = signalsAndTrials[i].replaceAll("\\.\\d+$", "");
			if(mathEngine.isChannelExists(fullSignalNames.split(":")[0]) && mathEngine.isChannelExists(fullSignalNames.split(":")[1])) {
				int trialNumber =  Integer.parseInt(signalsAndTrials[i].split(":")[1].split("\\.")[3]);
				createSerie(fullSignalNames, trialNumber, signalsAndTrials[i]);
			} //else Log.logErrorMessage("Data for " + signalsAndTrials[i] + " does not exist ! (Subject not loaded ?)");
		}
		updateChannelsPaletteVisiblility();
//		serieLabel.setText("");
		swtChart.getAxisSet().getXAxes()[0].setRange(dataChart.getXRange());
		swtChart.getAxisSet().getYAxes()[0].setRange(dataChart.getYRange());
		swtChart.getLegend().setVisible(dataChart.isLegendVisible());
		swtChart.setCrossHairVisibility(dataChart.isCrossHairVisible());
		swtChart.setShowMarkers(dataChart.isMarkersVisible());
	}
	
	private void createSerie(String fullSignalNames, int trialNumber, String serieID) {
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		double[] y = mathEngine.getValuesForTrialNumber(trialNumber,fullSignalNames.split(":")[0]);
		double[] x = mathEngine.getValuesForTrialNumber(trialNumber,fullSignalNames.split(":")[1]);
		ILineSeries lineSerie = (ILineSeries) swtChart.createSerie(SeriesType.LINE, serieID);
		lineSerie.setSymbolType(PlotSymbolType.NONE);
		lineSerie.setLineColor(ColorsUtils.getRandomColor());
		lineSerie.setLineWidth(2);
		lineSerie.setYSeries(y);
		lineSerie.setXSeries(x);
		if(AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.CHARTS_ANTIALIASIS))
		lineSerie.setAntialias(SWT.ON);
		else lineSerie.setAntialias(SWT.OFF);
		populateMarkers(fullSignalNames.split(":")[0], trialNumber, serieID);
		populateMarkers(fullSignalNames.split(":")[1], trialNumber, serieID);
	}
	
	private void populateMarkers(String fullSignalName, int trialNumber, String serieID) {
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		double sf = mathEngine.getSampleFrequency(fullSignalName);
		int nbMarkersGroups = mathEngine.getNbMarkersGroups(fullSignalName);
		for (int j = 0; j < nbMarkersGroups; j++) {
			String label = mathEngine.getMarkersGroupLabel(j+1, fullSignalName);
			double[] xMarkersValues = mathEngine.getXValuesForMarkersGroup(j+1,fullSignalName);
			int[] trialsMarkersValues = mathEngine.getTrialsListForMarkersGroup(j+1, fullSignalName);
			String symbolName = mathEngine.getMarkersGroupGraphicalSymbol(j+1, fullSignalName);
			if(symbolName == null) symbolName = MARKER_GRAPHIC_SYMBOL.SQUARE.toString();
			if(symbolName.equals(MARKER_GRAPHIC_SYMBOL.LINE.toString())) symbolName = MARKER_GRAPHIC_SYMBOL.SQUARE.toString();
			for (int k = 0; k < xMarkersValues.length; k++) {
				if(trialsMarkersValues[k] == trialNumber) {
					int index = (int) Math.round(xMarkersValues[k]*sf);
					swtChart.getChartMarkersSet().addChartMarker(j+1, index, serieID, label, symbolName);
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
//		serieLabel.setText(label);
	}

	public void clearChart() {
		super.clearChart();
		Table trialsTable = trialsTableViewer.getTable();
		for (int i = 0; i < trialsTable.getItemCount(); i++) trialsTable.getItem(i).setChecked(false);
		initChart();
	}

}
