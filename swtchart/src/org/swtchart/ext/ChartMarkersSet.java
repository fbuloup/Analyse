package org.swtchart.ext;

import java.util.HashSet;

public class ChartMarkersSet {
	
	private HashSet<ChartMarker> chartMarkers = new HashSet<ChartMarker>(0);
	private InteractiveChart chart;
	
	public ChartMarkersSet(InteractiveChart chart) {
		this.chart = chart;
	}
	
	public String getMarkerFullLable(Object chartMarker) {
    	return ((ChartMarker)chartMarker).getFullLabel();
    }
    
    public Object[] getChartMarkers() {
    	return chartMarkers.toArray(new ChartMarker[chartMarkers.size()]);
    }
    
    public Object[] getChartMarkers(String serieID) {
    	HashSet<ChartMarker> serieIDChartMarkers = new HashSet<ChartMarker>(0);
    	ChartMarker[] markers = chartMarkers.toArray(new ChartMarker[chartMarkers.size()]);
    	for (int i = 0; i < markers.length; i++) {
			ChartMarker chartMarker = markers[i];
			if(chartMarker.serieID.equals(serieID)) serieIDChartMarkers.add(chartMarker);
		}
    	return serieIDChartMarkers.toArray(new ChartMarker[serieIDChartMarkers.size()]);
    }
    
    public boolean addChartMarkers(Object[] chartMarkers) {
    	boolean added = true;
    	for (int i = 0; i < chartMarkers.length; i++) {
    		ChartMarker chartMarker = (ChartMarker) chartMarkers[i];
    		added = added && addChartMarker(chartMarker.groupNumber, chartMarker.index, chartMarker.serieID, chartMarker.label, chartMarker.graphicSymbol.toString());
    	}
    	return added;
    }
    
    public boolean addChartMarker(int markersGroup, int index, String serieID, String label, String symbolName) {
    	ChartMarker[] markers = chartMarkers.toArray(new ChartMarker[chartMarkers.size()]);
    	//Check if marker to add has not been added yet
    	for (int i = 0; i < markers.length; i++) {
			ChartMarker chartMarker = markers[i];
			if(chartMarker.groupNumber == markersGroup && chartMarker.index == index && chartMarker.serieID.equals(serieID)) {
				//Marker has been added yet, check if graphical symbol has to be updated
				if(!chartMarker.graphicSymbol.toString().equals(symbolName)) chartMarker.setGraphicSymbol(symbolName);
				return false;
			}
		}
    	ChartMarker chartMarker = new ChartMarker(markersGroup, index, serieID, label, symbolName, chart);
    	chartMarkers.add(chartMarker);
    	return true;
    }
    
    public boolean deleteChartMarkers(Object[] chartMarkers) {
    	boolean deleted = true;
    	for (int i = 0; i < chartMarkers.length; i++) {
    		ChartMarker chartMarker = (ChartMarker) chartMarkers[i];
    		deleted = deleted && deleteChartMarker(chartMarker.groupNumber, chartMarker.index, chartMarker.serieID);
    	}
    	return deleted;
    }
    
    public boolean deleteChartMarker(int markersGroup, int index, String serieID) {
    	ChartMarker[] markers = chartMarkers.toArray(new ChartMarker[chartMarkers.size()]);
    	ChartMarker chartMarker = null;
    	for (int i = 0; i < markers.length; i++) {
			chartMarker = markers[i];
			if(chartMarker.groupNumber == markersGroup && chartMarker.index == index && chartMarker.serieID.equals(serieID)) break;
		}
    	if(chartMarker != null) return chartMarkers.remove(chartMarker);
    	return false;
    }
    
    public void selectedMarker(Object chartMarker) {
    	((ChartMarker)chartMarker).selected = true;
    }
    
    public void resetMarkersSeriesSelected() {
    	ChartMarker[] markers = chartMarkers.toArray(new ChartMarker[chartMarkers.size()]);
    	for (int i = 0; i < markers.length; i++) markers[i].selected = false;
    }
    
    public String getMarkerLabel(int numGroup, int index, String serieID) {
    	ChartMarker[] markers = chartMarkers.toArray(new ChartMarker[chartMarkers.size()]);
    	ChartMarker chartMarker = null;
    	for (int i = 0; i < markers.length; i++) {
			chartMarker = markers[i];
			if(chartMarker.groupNumber == numGroup && chartMarker.index == index && chartMarker.serieID.equals(serieID)) return chartMarker.label;
    	}
    	return "";
    }

	public void remove(ChartMarker chartMarker) {
		chartMarkers.remove(chartMarker);
	}

	public void clear() {
		chartMarkers.clear();
	}

	public boolean deleteMarkersGroup(ChartMarker marker) {
		ChartMarker[] markers = chartMarkers.toArray(new ChartMarker[chartMarkers.size()]);
		String fullSignalName = marker.serieID.replaceAll("\\.\\d+$", "");
		for (int i = 0; i < markers.length; i++) {
			ChartMarker currentChartMarker = markers[i];
			if(currentChartMarker != marker)
				if(currentChartMarker.serieID.startsWith(fullSignalName) && currentChartMarker.groupNumber > marker.groupNumber)
					currentChartMarker.groupNumber = currentChartMarker.groupNumber - 1;
		}
		deleteChartMarkers(new Object[]{marker});
		return true;
	}

}
