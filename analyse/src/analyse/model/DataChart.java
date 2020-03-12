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
package analyse.model;

import java.io.Serializable;
import java.util.HashSet;

import org.eclipse.jface.preference.IPreferenceStore;
import org.swtchart.Range;

import analyse.preferences.AnalysePreferences;

public final class DataChart implements Serializable {
		
	private static final long serialVersionUID = 1L;
	
	private String chartType;
	//Signals names with trials : 
	//experiment.subject.signal.10 etc... for time chart
	//experiment.subject.signal1:experiment.subject.signal2.10 etc... for XY chart
	//experiment.subject.signal1:experiment.subject.signal2:experiment.subject.signal3.10 etc... for XYZ chart
	private HashSet<String> signals;
	private boolean markersVisibility;
	private boolean crossHairVisibility;
	private boolean legendVisibility;
	private boolean channelsPaletteVisibility;
	private boolean autoAdjustAxis;
	private boolean showEventsAsInfinite;
	private double xMin;
	private double yMin;
	private double xMax;
	private double yMax;

	private int horizontalSpan;
	private int verticalSpan;
	
	public DataChart(String chartType) {
		this.chartType = chartType;
		signals = new HashSet<String>(0);
		xMin = 0;
		xMax = 1;
		yMin = 0;
		yMax = 1;
		IPreferenceStore preferenceStore = AnalysePreferences.getPreferenceStore();
		autoAdjustAxis = preferenceStore.getBoolean(AnalysePreferences.AUTO_ADJUST_XY_AXIS);
		markersVisibility = preferenceStore.getBoolean(AnalysePreferences.SHOW_MARKERS);
		crossHairVisibility = preferenceStore.getBoolean(AnalysePreferences.SHOW_CROSSHAIR);
		legendVisibility = preferenceStore.getBoolean(AnalysePreferences.SHOW_LEGEND);
		channelsPaletteVisibility = preferenceStore.getBoolean(AnalysePreferences.SHOW_CHANNELS_PALETTE);
		showEventsAsInfinite = preferenceStore.getBoolean(AnalysePreferences.SHOW_EVENTS_AS_INFINITE);
		if(chartType.equals(ChartsTypes.XY_CHART_ID_STRING)) crossHairVisibility = false;
	}
	
	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	public String[] getSignals() {
		return signals.toArray(new String[signals.size()]);
	}
	
	public void addSignal(String signal) {
		signals.add(signal);
	}
	
	public void removeSignal(String signal) {
		signals.remove(signal);
	}
	
	public boolean hasSignal(String signal) {
		return signals.contains(signal);
	}

	public void clear() {
		signals.clear();
	}

	public void setRange(double xmin, double xmax, double ymin, double ymax) {
		xMax = xmax;
		yMax = ymax;
		xMin = xmin;
		yMin = ymin;
	}
	
	public Range getXRange() {
		return new Range(xMin, xMax);
	}
	
	public Range getYRange() {
		return new Range(yMin, yMax);
	}

	public boolean isAutoAdjustAxis() {
		return autoAdjustAxis;
	}

	public void setAutoAdjustAxis(boolean autoAdjustAxis) {
		this.autoAdjustAxis = autoAdjustAxis;
	}

	public boolean isMarkersVisible() {
		return markersVisibility;
	}

	public void setMarkersVisibility(boolean markersVisibility) {
		this.markersVisibility = markersVisibility;
	}

	public boolean isCrossHairVisible() {
		return crossHairVisibility;
	}

	public void setCrossHairVisibility(boolean crossHairVisibility) {
		this.crossHairVisibility = crossHairVisibility;
	}

	public boolean isLegendVisible() {
		return legendVisibility;
	}

	public void setLegendVisibility(boolean legendVisibility) {
		this.legendVisibility = legendVisibility;
	}

	public boolean isChannelsPaletteVisible() {
		return channelsPaletteVisibility;
	}

	public void setChannelsPaletteVisibility(boolean channelsPaletteVisibility) {
		this.channelsPaletteVisibility = channelsPaletteVisibility;
	}

	public boolean isShowEventsAsInfinite() {
		return showEventsAsInfinite;
	}

	public void setShowEventsAsInfinite(boolean showEventsAsInfinite) {
		this.showEventsAsInfinite = showEventsAsInfinite;
	}

	public void setHorizontalSpan(int hs) {
		this.horizontalSpan = hs;
	}

	public void setVerticalSpan(int vs) {
		this.verticalSpan = vs;
	}

	public int getHorizontalSpan() {
		return horizontalSpan;
	}

	public int getVerticalSpan() {
		return verticalSpan;
	}
	
	
}
