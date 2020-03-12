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
package analyse.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.swtchart.IEventSeries;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;

import analyse.gui.ChartEditor;
import analyse.gui.EditorsView;
import analyse.gui.MultiChartsEditor;
import analyse.resources.ColorsUtils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class RandomizeSeriesColorsAction extends Action implements SelectionListener {
	
	CTabItem tabItem;
	
	public RandomizeSeriesColorsAction() {
		super(Messages.getString("RandomizeSeriesColorsAction.Title"),AS_PUSH_BUTTON);
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.RANDOMIZE_COLOR_ICON));
		setEnabled(false);
	}
	
	public void run() {	
		if(tabItem instanceof ChartEditor) {
			ChartEditor chartEditor = (ChartEditor)tabItem;
			ISeries[] series = chartEditor.getSWTChart().getSeriesSet().getSeries();
			for (int i = 0; i < series.length; i++) {
				if(series[i] instanceof ILineSeries) ((ILineSeries)series[i]).setLineColor(ColorsUtils.getRandomColor());
				if(series[i] instanceof IEventSeries) ((IEventSeries)series[i]).setLineColor(ColorsUtils.getRandomColor());
			}
			chartEditor.getSWTChart().redraw();
		}
		
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		setEnabled(false);
		if(e.widget instanceof EditorsView) {
			EditorsView editorsView = (EditorsView) e.widget;
			if(editorsView.getSelection() instanceof ChartEditor || editorsView.getSelection() instanceof MultiChartsEditor) {
				setEnabled(true);
				tabItem = editorsView.getSelection();
			}
		}
	}

	public void setChartEditor(CTabItem tabItem) {
		this.tabItem = tabItem;
	}
}
