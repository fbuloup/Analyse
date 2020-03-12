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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

import analyse.AnalyseApplication;
import analyse.gui.multicharts.ChartElement;
import analyse.gui.multicharts.DropChartsContainerDelegate;
import analyse.model.ChartsTypes;
import analyse.model.DataChart;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.MultiCharts;
import analyse.model.MultiDataCharts;
import analyse.resources.ColorsUtils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class MultiChartsEditor extends ChartEditor implements IResourceObserver, ISelectionChangedListener {
	
	private MultiCharts multiCharts;
	private ToolBarManager toolBarManager;
	protected ActionContributionItem chartsOptionsAction;
	private Composite chartsContainer;
	
	private class AddColumnAction extends Action {
		public AddColumnAction() {
			super(Messages.getString("AddColumnAction.Title"),Action.AS_PUSH_BUTTON); 
			setToolTipText(Messages.getString("AddColumnAction.Title")); 
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.ADD_COLUMN_ICON));
		}
		@Override
		public void run() {
			multiCharts.getData().setNumberOfColumns(multiCharts.getData().getNumberOfColumns() + 1);
			chartsContainer.setLayout(new GridLayout(multiCharts.getData().getNumberOfColumns(), true));
			chartsContainer.layout();
			multiCharts.saveChart();
		}
	}
	
	private class DeleteColumnAction extends Action {
		public DeleteColumnAction() {
			super(Messages.getString("DeleteColumnAction.Title"),Action.AS_PUSH_BUTTON); 
			setToolTipText(Messages.getString("DeleteColumnAction.Title")); 
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.DELETE_COLUMN_ICON));
		}
		@Override
		public void run() {
			if(multiCharts.getData().getNumberOfColumns() > 1) {
				multiCharts.getData().setNumberOfColumns(multiCharts.getData().getNumberOfColumns() - 1);
				chartsContainer.setLayout(new GridLayout(multiCharts.getData().getNumberOfColumns(), true));
				chartsContainer.layout();
				multiCharts.saveChart();
			}
		}
	}
	
	private final class MultiChartsOptionsAction extends Action {
		
		private Menu chartOptionsMenu;
		
		class AddTimeChartAction extends Action {
			public AddTimeChartAction() {
				super(Messages.getString("AddTimeChartAction.Title"),Action.AS_PUSH_BUTTON); //$NON-NLS-1$
			}
			@Override
			public void run() {
				addTimeChart();
			}
		}
		class AddXYChartAction extends Action {	
			public AddXYChartAction() {
				super(Messages.getString("AddXYChartAction.Title"),Action.AS_PUSH_BUTTON); //$NON-NLS-1$
			}		
			@Override
			public void run() {
				addXYChart();
			}
		}
		class AddXYZChartAction extends Action {	
			public AddXYZChartAction() {
				super(Messages.getString("AddXYZChartAction.Title"),Action.AS_PUSH_BUTTON); //$NON-NLS-1$
			}		
			@Override
			public void run() {
				addXYZChart();
			}
		}
		
		public MultiChartsOptionsAction() {
			super(Messages.getString("MultiChartsOptionsAction.Title"),Action.AS_DROP_DOWN_MENU);
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.CHART_OPTIONS_ICON));
			setMenuCreator(new IMenuCreator() {
				public Menu getMenu(Menu parent) {
					return null;
				}
				public Menu getMenu(Control parent) {
					if (chartOptionsMenu != null) chartOptionsMenu.dispose(); 
					chartOptionsMenu = new Menu(parent);
					AddTimeChartAction addTimeChartAction = new AddTimeChartAction();
					addActionToMenu(chartOptionsMenu, addTimeChartAction); 
					AddXYChartAction addXYChartAction = new AddXYChartAction();
					addActionToMenu(chartOptionsMenu, addXYChartAction); 
					AddXYZChartAction addXYZChartAction = new AddXYZChartAction();
					addActionToMenu(chartOptionsMenu, addXYZChartAction); 
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
		
		public void addTimeChart() {
			DataChart dataChart = new DataChart(ChartsTypes.TIME_CHART_ID_STRING);
			multiCharts.getData().addDataChart(dataChart);
			multiCharts.saveChart();
			addSWTChart(dataChart);
		}
		
		public void addXYChart() {
			DataChart dataChart = new DataChart(ChartsTypes.XY_CHART_ID_STRING);
			multiCharts.getData().addDataChart(dataChart);
			multiCharts.saveChart();
			addSWTChart(dataChart);
		}
		
		public void addXYZChart() {
			DataChart dataChart = new DataChart(ChartsTypes.XYZ_CHART_ID_STRING);
			multiCharts.getData().addDataChart(dataChart);
			multiCharts.saveChart();
			addSWTChart(dataChart);
		}

		@Override
		public void run() {
			Control[] controls = chartsContainer.getChildren();
			for (int i = 0; i < controls.length; i++) if(controls[i] instanceof ChartElement) ((ChartElement)controls[i]).toggleToolBarVisibility();
			chartsContainer.layout();
		}
	}
	
	public MultiChartsEditor(CTabFolder parent, int style, MultiCharts multiCharts) {
		super(parent, style);
		this.multiCharts = multiCharts;
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Experiments.getInstance().removeExperimentObserver(MultiChartsEditor.this);
				AnalyseApplication.getAnalyseApplicationWindow().removeSelectionChangedListenerFromExperimentsView(MultiChartsEditor.this);
				if(chartsOptionsAction != null) chartsOptionsAction.dispose();
				disposeToolBar();
			}
		});
		createContent();
	}

	private void addSWTChart(DataChart dataChart) {
		boolean toolbarVisible = false;
		Control[] controls = chartsContainer.getChildren();
		if(controls.length > 0) toolbarVisible = ((ChartElement)controls[0]).isToolbarVisible();
		ChartElement chartElement = new ChartElement(chartsContainer, SWT.NONE, this, dataChart);
		chartElement.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, dataChart.getHorizontalSpan(), dataChart.getVerticalSpan()));
		if(toolbarVisible) chartElement.toggleToolBarVisibility();
		chartsContainer.layout();
	}

	private void createContent() {
		SashForm sashForm = new SashForm(getParent(), SWT.NONE);
		sashForm.addFocusListener((FocusListener) getParent());
		
		chartsContainer = new Composite(sashForm, SWT.BORDER);
		chartsContainer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				chartsContainer.getParent().setFocus();
			}
		});
		DropChartsContainerDelegate.install(chartsContainer);
		
		chartsContainer.setLayout(new GridLayout(multiCharts.getData().getNumberOfColumns(), true));
		
		MultiDataCharts multiDataCharts = multiCharts.getData();
		DataChart[] dataCharts = multiDataCharts.getDataCharts();
		for (int i = 0; i < dataCharts.length; i++) {
			DataChart dataChart = dataCharts[i];
			addSWTChart(dataChart);
		}
		
		Composite trialsListContainer = new Composite(sashForm, SWT.BORDER);
		trialsListContainer.setLayout(new FillLayout());
		chartOptionsTabFolder = new CTabFolder(trialsListContainer, SWT.NONE);
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
		
		
		CTabItem trialsListTabItem = new CTabItem(chartOptionsTabFolder, SWT.NONE);
		trialsListTabItem.setText("fdqfdqsf");
		List trialsList = new List(chartOptionsTabFolder, SWT.NONE);
		trialsList.add("fdfdsq");
		
		trialsListTabItem.setControl(trialsList);
//		trialsListTabFolder.setSelection(0);
		
		sashForm.setWeights(new int[]{75,25});
		
		setControl(sashForm);
	}

	public void update(int message, IResource[] resources) {
		if(message == IResourceObserver.DELETED) {
			for (int i = 0; i < resources.length; i++) {
				if(multiCharts == resources[i] || multiCharts.hasParent(resources[i])) {
					dispose();
					break;
				} 
			}
		}
		
		if(message == IResourceObserver.RENAMED) {
			if(multiCharts == resources[0] || multiCharts.hasParent(resources[0])) {
				setText(multiCharts.getNameWithoutExtension());
				setToolTipText(multiCharts.getLocalPath());
			}
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		// TODO Auto-generated method stub
		
	}

	public Object getChart() {
		return multiCharts;
	}
	
	protected ToolBar getToolBar() {
		toolBarManager = new ToolBarManager(SWT.FLAT | SWT.WRAP);
		toolBarManager.add(AnalyseApplicationWindow.clearActiveEditorAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(new AddColumnAction());
		toolBarManager.add(new DeleteColumnAction());
		toolBarManager.add(new Separator());
		chartsOptionsAction = new ActionContributionItem((IAction)(new MultiChartsOptionsAction()));
		toolBarManager.add(chartsOptionsAction);
		ToolBar toolBar = toolBarManager.createControl(getParent());
		toolBar.addFocusListener((FocusListener) getParent());
		return toolBar;
	}
	
	protected void disposeToolBar() {
		if(toolBarManager != null) {
			toolBarManager.dispose();
			toolBarManager = null;
		}
		if(chartsOptionsAction != null) {
			chartsOptionsAction.dispose();
			chartsOptionsAction = null;
		}
	}

	public void clearCharts() {
		Control[] controls = chartsContainer.getChildren();
		for (int i = 0; i < controls.length; i++) {
			if(controls[i] instanceof ChartElement) ((ChartElement)controls[i]).remove();
		}
	}

	public void remove(DataChart dataChart) {
		multiCharts.getData().remove(dataChart);
		multiCharts.saveChart();
		chartsContainer.layout();
	}

	public void setHVSpan(DataChart dataChart, int hs, int vs) {
		dataChart.setHorizontalSpan(hs);
		dataChart.setVerticalSpan(vs);
		multiCharts.saveChart();
	}

}
