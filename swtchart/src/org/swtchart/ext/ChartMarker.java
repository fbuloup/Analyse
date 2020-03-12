package org.swtchart.ext;

public class ChartMarker {
	
	public static enum MARKER_GRAPHIC_SYMBOL {
		LINE, SQUARE, DIAMOND, TRIANGLE, CERCLE, STAR, CROSS;
		public static MARKER_GRAPHIC_SYMBOL getDefault() {
			return LINE;
		}
	}

	public InteractiveChart chart;
	public int groupNumber;
	public int index;
	public String serieID;
	public boolean selected;
	public String label;
	public MARKER_GRAPHIC_SYMBOL graphicSymbol = MARKER_GRAPHIC_SYMBOL.LINE;

	public ChartMarker(int groupNumber, int index, String serieID, String label, String symbolName, InteractiveChart chart) {
		this.groupNumber = groupNumber;
		this.index = index;
		this.serieID = serieID;
		this.label = label;
		this.chart = chart;
		if(symbolName == null) {
			graphicSymbol = MARKER_GRAPHIC_SYMBOL.getDefault();
		} else {
			MARKER_GRAPHIC_SYMBOL[] values = MARKER_GRAPHIC_SYMBOL.values();
			for (int i = 0; i < values.length; i++) {
				if(symbolName.equals(values[i].toString())) graphicSymbol = values[i];
			}
		}
		
	}

	public String getFullLabel() {
		double xValue = chart.getMarkerXValue(index, serieID);
		double yValue = chart.getMarkerYValue(index, serieID);
		if (Double.compare(xValue, Double.NaN) == 0)
			return label + " [ Not visible ]" + " (" + serieID + ")";
		else
			return label + " [" + xValue + " - " + yValue + "]" + " ("
					+ serieID + ")";
	}

	public String getLabel() {
		return label;
	}
	
	public MARKER_GRAPHIC_SYMBOL getGraphicSymbol() {
		return graphicSymbol;
	}
	
	public void setGraphicSymbol(String symbol) {
		MARKER_GRAPHIC_SYMBOL[] values = MARKER_GRAPHIC_SYMBOL.values();
		for (int i = 0; i < values.length; i++) {
			if(symbol.equals(values[i].toString())) graphicSymbol = values[i];
		}
	}
}