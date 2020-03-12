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
package analyse.gui.multicharts;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.swtchart.ext.InteractiveChart;

import analyse.gui.MultiChartsEditor;
import analyse.model.DataChart;

public class ChartElement extends Composite {
	
	private InteractiveChart swtChart;
	private ToolBar toolBar = null;
	private MultiChartsEditor multiChartsEditor;
	private DataChart dataChart;

	public ChartElement(Composite parent, int style,  MultiChartsEditor multiChartsEditor, DataChart dataChart) {
		super(parent, style);
		setLayout(new GridLayout());
		this.dataChart = dataChart;
		this.multiChartsEditor = multiChartsEditor;
		
		swtChart = new InteractiveChart(this, SWT.NONE, true);
		swtChart.getPlotArea().addFocusListener((FocusListener) getParent().getParent().getParent());
		swtChart.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		swtChart.getTitle().setVisible(false);
		swtChart.getPlotArea().setForeground(JFaceColors.getBannerBackground(getDisplay()));
		
		DragDropChartElementDelegate.install(this, DND.DROP_MOVE);
		
	}
	
	public void toggleToolBarVisibility() {
		if(toolBar == null) installToolbar();
		else removeToolbar();
		layout();
	}
	
	public boolean isToolbarVisible() {
		return toolBar != null;
	}
	
	private void removeToolbar() {
		if(toolBar != null) {
			Control[] controls = toolBar.getChildren();
			for (int i = 0; i < controls.length; i++) controls[i].dispose();
			toolBar.dispose();
			toolBar = null;
		}
	}

	private void installToolbar() {
		if(toolBar != null) remove();
		toolBar = new ToolBar(this, SWT.NONE);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true ,false));
		ToolBarChartElementDelegate.install(this, toolBar);
		toolBar.moveAbove(swtChart);
		toolBar.addFocusListener((FocusListener) getParent().getParent().getParent());
	}

	public void remove() {
		removeToolbar();
		dispose();
		multiChartsEditor.remove(dataChart);
	}

	public void saveLayout() {
		GridData gridData = (GridData)getLayoutData();
		multiChartsEditor.setHVSpan(dataChart, gridData.horizontalSpan, gridData.verticalSpan);
	}
	
	public DataChart getDataChart() {
		return dataChart;
	}

}
