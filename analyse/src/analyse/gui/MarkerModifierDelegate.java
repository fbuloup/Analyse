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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.swtchart.ext.ChartMarker;
import org.swtchart.ext.ChartMarker.MARKER_GRAPHIC_SYMBOL;
import org.swtchart.ext.InteractiveChart;

import analyse.Log;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Subject;
import analyse.resources.Messages;

public class MarkerModifierDelegate implements Listener {

	private InteractiveChart chart;
	private boolean markerSelected;
	private ChartMarker selectedChartMarker;
	private double[] newMarkerCoordinates = new double[2];

	public MarkerModifierDelegate(InteractiveChart chart) {
		this.chart = chart;
	}

	public void handleEvent(Event event) {
		if(event.type == SWT.MouseDown) {				
			markerSelected = false;
			Object[] markers = chart.getChartMarkersSet().getChartMarkers();
			selectedChartMarker = null;
			for (int i = 0; i < markers.length; i++) {
				if( ((ChartMarker)markers[i]).selected && markerSelected) {
					markerSelected = false;
					selectedChartMarker = null;
					break;
				}
				if( ((ChartMarker)markers[i]).selected && !markerSelected) {
					markerSelected = true;
					selectedChartMarker = (ChartMarker)markers[i];
				}
			}
			if(selectedChartMarker != null) {
				chart.setActiveSerieId(selectedChartMarker.serieID);
				computeNewCoordinates();
			}
		}
		if(event.type == SWT.MouseUp) {			
			if(markerSelected) {
				computeNewCoordinates();
				String markerLabel = selectedChartMarker.getLabel();
				MarkerModifierDelegate.deleteMarker(selectedChartMarker, chart);
				MarkerModifierDelegate.addMarker(markerLabel, chart);
			}
			markerSelected = false;
		}
		if(event.type == SWT.MouseMove && markerSelected) {
			computeNewCoordinates();
		}
	}
	
	private void computeNewCoordinates() {
		double xValue = chart.getSeriesSet().getSeries(chart.getActiveSerieId()).getXSeries()[selectedChartMarker.index];
		double yValue = chart.getSeriesSet().getSeries(chart.getActiveSerieId()).getYSeries()[selectedChartMarker.index];
		double[] markerCoordinate = chart.getCrossHairCoordinates();
		newMarkerCoordinates = chart.getNearestPointCoordinates(markerCoordinate[0]);
		StringBuffer message = new StringBuffer();
		message.append("New Marker Coordinates : (");
		message.append(newMarkerCoordinates[0]);
		message.append(",");
		message.append(newMarkerCoordinates[1]);
		message.append(")");
		message.append(" - ");
		message.append("Old Marker Coordinates : (");
		message.append(xValue);
		message.append(",");
		message.append(yValue);
		message.append(")");
		Log.getInstance().IlogMessage(message.toString());
	}
	
	public static Subject deleteMarker(ChartMarker chartMarker, InteractiveChart chart) {
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		Subject modifiedSubject = null;
		String fullSignalName = chartMarker.serieID.replaceAll("\\.\\d+$", "");
		int trialNumber = Integer.parseInt(chartMarker.serieID.split("\\.")[3]);
		int[] trialsList = mathEngine.getTrialsListForMarkersGroup(chartMarker.groupNumber, fullSignalName);
		double[] xValues = mathEngine.getXValuesForMarkersGroup(chartMarker.groupNumber, fullSignalName);
		int lineNumber = -1;
		for (int j = 0; j < xValues.length; j++) {
			int index = (int) Math.round(xValues[j]*mathEngine.getSampleFrequency(fullSignalName));
			if(trialNumber == trialsList[j] && chartMarker.index == index) {
				lineNumber = j;
				break;
			}
		}
		if(lineNumber > -1) {
			if(mathEngine.deleteMarker(chartMarker.groupNumber, lineNumber+1 , fullSignalName)) {
				boolean result = false;
				if(trialsList.length == 1) result = chart.getChartMarkersSet().deleteMarkersGroup(chartMarker);
				else result = chart.getChartMarkersSet().deleteChartMarkers(new ChartMarker[]{chartMarker}); 
				if(result) {
					String subjectName = fullSignalName.replaceAll("\\.\\w+$", "");
					modifiedSubject = (Subject) Experiments.getInstance().getSubjectByName(subjectName);
					modifiedSubject.setModified(true);
					
				} else Log.logErrorMessage(Messages.getString("TimeChartEditor.MarkerNotDeleted") + chartMarker.getFullLabel());
			} else Log.logErrorMessage(Messages.getString("TimeChartEditor.MarkerNotDeleted") + chartMarker.getFullLabel());
		} else Log.logErrorMessage(Messages.getString("TimeChartEditor.MarkerNotFound") + chartMarker.getFullLabel());
		return modifiedSubject;
	}
	
	public static void addMarker(String markerLabel, InteractiveChart swtChart) {
		String activeSerieID = swtChart.getActiveSerieId();
		if(activeSerieID != null) {
			int trialNumber = Integer.parseInt(activeSerieID.split("\\.")[3]);
			String fullSignalName = activeSerieID.replaceAll("\\.\\d+$", "");
			Subject subject = (Subject) Experiments.getInstance().getSubjectByName(fullSignalName.replaceAll("\\.\\w+$", ""));
			if(subject.isLoaded()) {
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				double sf = mathEngine.getSampleFrequency(fullSignalName);
				String[] markersLabels = mathEngine.getMarkersGroupsLabels(fullSignalName);
				int numGroupMarker = -1;
				for (int i = 0; i < markersLabels.length; i++) {
					if(markerLabel.equals(markersLabels[i])) {
						numGroupMarker = i + 1; 
						break;
					}
				}
				if(numGroupMarker == -1 && !markerLabel.equals("")) if(mathEngine.createNewMarkersGroup(markerLabel, fullSignalName)) numGroupMarker = mathEngine.getNbMarkersGroups(fullSignalName);
				if(numGroupMarker > -1) {
					int index = swtChart.canAddMarkerAtCurrentMousePosition(numGroupMarker,sf);
					if(index > - 1) {
						double[] markerCoordinate = swtChart.getCrossHairCoordinates();
						markerCoordinate = swtChart.getNearestPointCoordinates(markerCoordinate[0]);
						if(mathEngine.addMarker(numGroupMarker, trialNumber, markerCoordinate[0], markerCoordinate[1], fullSignalName)) {
							swtChart.getChartMarkersSet().addChartMarker(numGroupMarker, index, activeSerieID, markerLabel, MARKER_GRAPHIC_SYMBOL.getDefault().toString());
							subject.setModified(true);
						}
						else Log.logErrorMessage(Messages.getString("TimeChartEditor.ImpossibleAddMarker"));
						Experiments.notifyObservers(IResourceObserver.MARKER_ADDED, new IResource[]{subject});
					} else Log.logErrorMessage(Messages.getString("TimeChartEditor.ImpossibleAddMarker"));
				} else Log.logErrorMessage(Messages.getString("TimeChartEditor.ImpossibleAddMarker") + Messages.getString("TimeChartEditor.EmptyLabel"));
			}
		}
	}
	
}
