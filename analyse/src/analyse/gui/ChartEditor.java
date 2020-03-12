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

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.ToolBar;
import org.swtchart.ISeries;
import org.swtchart.ext.InteractiveChart;

import analyse.model.Chart;

public abstract class ChartEditor extends CTabItem {

	protected Chart chart;
	protected SashForm sashForm;
	protected CTabFolder chartOptionsTabFolder;
	protected InteractiveChart swtChart;
	protected ToolBarManager toolBarManager;
	protected ActionContributionItem chartOptionsAction;
	
	public ChartEditor(CTabFolder parent, int style) {
		super(parent, style);
	}
	
	public InteractiveChart getSWTChart() {
		return swtChart;
	}
	
	public void clearSWTChart() {
		ISeries[] series = swtChart.getSeriesSet().getSeries();
		for (int i = 0; i < series.length; i++) swtChart.deleteSerie(series[i].getId());
	}
	
	public void clearChart() {
		clearSWTChart();
		chart.getData().clear();
		chart.saveChart();
	}
	
	public Object getChart() {
		return chart;
	}
	
	protected void disposeToolBar() {
		if(toolBarManager != null) {
			toolBarManager.dispose();
			toolBarManager = null;
		}
		if(chartOptionsAction != null) {
			chartOptionsAction.dispose();
			chartOptionsAction = null;
		}
	}

	protected abstract ToolBar getToolBar();

}
