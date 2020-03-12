package org.swtchart.ext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.Range;
import org.swtchart.IAxis.Direction;
import org.swtchart.IAxis.Position;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ext.internal.SelectionRectangle;
import org.swtchart.ext.internal.properties.AxisPage;
import org.swtchart.ext.internal.properties.AxisTickPage;
import org.swtchart.ext.internal.properties.ChartPage;
import org.swtchart.ext.internal.properties.GridPage;
import org.swtchart.ext.internal.properties.LegendPage;
import org.swtchart.ext.internal.properties.PropertiesResources;
import org.swtchart.ext.internal.properties.SeriesLabelPage;
import org.swtchart.ext.internal.properties.SeriesPage;

/**
 * An interactive chart which provides the following abilities.
 * <ul>
 * <li>scroll with arrow keys</li>
 * <li>zoom in and out with ctrl + arrow up/down keys</li>
 * <li>context menus for adjusting axis range and zooming in/out.</li>
 * <li>file selector dialog to save chart to image file.</li>
 * <li>properties dialog to configure the chart settings</li>
 * </ul>
 */
public class InteractiveChart extends Chart implements PaintListener {

	/** the filter extensions */
	private static final String[] EXTENSIONS = new String[] { "*.jpeg", "*.jpg", "*.png" };
	private static final String EXTENSIONS_STRING = "jpeg, jpg or png";

	/** the selection rectangle for zoom in/out */
	protected SelectionRectangle selection;

	/** the clicked time in milliseconds */
	private long clickedTime;

	/** the resources created with properties dialog */
	private PropertiesResources resources;

	private HashSet<IRangeObserver> rangeObservers = new HashSet<IRangeObserver>(0);
	private HashSet<ICoordinatesObserver> coordinatesObservers = new HashSet<ICoordinatesObserver>(0);
	private Image bufferedImageZoom;
	private Image bufferedImage;
	private String activeSerieId = null;
	private boolean showCrossHair;
	private boolean showMarkers;
	private double xCoordinateValue;
	private double yCoordinateValue;
	private int mouseXPosition = -1;

	private ChartMarkersSet chartMarkersSet;
	private boolean isTimeSerie;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            the style
	 */
	public InteractiveChart(Composite parent, int style, boolean isTimeSerie) {
		super(parent, style);
		this.isTimeSerie = isTimeSerie;
		init();
	}

	@Override
	public void redraw() {
		super.redraw();
		 if(bufferedImage != null) bufferedImage.dispose();
		 bufferedImage = null;
	}
	
	/**
	 * Initializes.
	 */
	private void init() {
		selection = new SelectionRectangle();
		resources = new PropertiesResources();
		chartMarkersSet = new ChartMarkersSet(this);

		Composite plot = getPlotArea();
		plot.addListener(SWT.MouseMove, this);
		plot.addListener(SWT.MouseDown, this);
		plot.addListener(SWT.MouseUp, this);
		plot.addListener(SWT.MouseWheel, this);
		plot.addListener(SWT.KeyDown, this);
		plot.addListener(SWT.Resize, this);

		plot.addPaintListener(this);
		
		createMenuItems();
	}

	public void paintControl(PaintEvent e) {
		if(showMarkers) {
			if(isTimeSerie) {
				ISeries[] series = getSeriesSet().getSeries();
				for (int i = 0; i < series.length; i++) {
					double[] xValues  = series[i].getXSeries();
					double[] yValues = series[i].getYSeries();
					long offsetIndex = 0;
					if(xValues.length >= 2) offsetIndex = Math.round(xValues[0]/(xValues[1] - xValues[0]));
					IAxis xAxis = getAxisSet().getXAxis(series[i].getXAxisId());
					IAxis yAxis = getAxisSet().getYAxis(series[i].getYAxisId());
					IAxis[] allYAxis = getAxisSet().getYAxes();
					int W2 = ((GridLayout)getParent().getLayout()).horizontalSpacing;
					for (int j = 0; j < allYAxis.length; j++) 
						if(allYAxis[j].getPosition() == Position.Primary) 
							W2 = W2 + allYAxis[j].getTick().getBounds().width ;
					Object[] markers = chartMarkersSet.getChartMarkers();
					for (int j = 0; j < xValues.length; j++) {
						if(xAxis.getRange().lower <= xValues[j] && xValues[j]<= xAxis.getRange().upper) {
							for (int k = 0; k < markers.length; k++) {
								ChartMarker chartMarker = (ChartMarker) markers[k];
								if(chartMarker.serieID.equals(series[i].getId()) && (j + offsetIndex) == chartMarker.index) {
									int W = xAxis.getPixelCoordinate(xValues[j]);
									int H = yAxis.getPixelCoordinate(yValues[j]);
									Color lineColor = ((ILineSeries)series[i]).getLineColor();
									GC gc = new GC(this);
									gc.setForeground(lineColor);
									gc.setLineWidth(3);
									gc.drawLine(W + W2, getPlotArea().getClientArea().height, W + W2, getPlotArea().getClientArea().height + 15);
									gc.dispose();
									Color color = e.gc.getForeground();
									e.gc.setForeground(lineColor);
									switch (chartMarker.getGraphicSymbol()) {
									case LINE:
										e.gc.setLineWidth(chartMarker.selected?7:3);
										e.gc.drawLine(W, 0, W, getPlotArea().getClientArea().height);
										e.gc.drawText(chartMarker.getLabel(), W + (chartMarker.selected?7:3), getPlotArea().getClientArea().height - 15);
										break;
									case SQUARE:
										e.gc.setLineWidth(chartMarker.selected?3:1);
										e.gc.drawRectangle(W - 6, H - 6, 12, 12);
										e.gc.drawText(chartMarker.getLabel(), W + 3, getPlotArea().getClientArea().height - 15);
										break;
									case DIAMOND:
										e.gc.setLineWidth(chartMarker.selected?3:1);
										e.gc.drawPolyline(new int[] {W - 6, H, W, H - 6, W + 6, H, W, H + 6, W - 6, H});
										e.gc.drawText(chartMarker.getLabel(), W + 3, getPlotArea().getClientArea().height - 15);
										break;
									case CROSS:
										e.gc.setLineWidth(chartMarker.selected?3:1);
										e.gc.drawLine(W-6, H, W+6, H);
										e.gc.drawLine(W, H-6, W, H+6);
										e.gc.drawText(chartMarker.getLabel(), W + 3, getPlotArea().getClientArea().height - 15);
										break;
									case STAR:
										e.gc.setLineWidth(chartMarker.selected?3:1);
										e.gc.drawLine(W-6, H, W+6, H);
										e.gc.drawLine(W, H-6, W, H+6);
										e.gc.drawLine(W-6, H-6, W+6, H+6);
										e.gc.drawLine(W-6, H+6, W+6, H-6);
										e.gc.drawText(chartMarker.getLabel(), W + 3, getPlotArea().getClientArea().height - 15);
										break;
									case TRIANGLE:
										e.gc.setLineWidth(chartMarker.selected?3:1);
										e.gc.drawPolyline(new int[] {W - 6, H+6, W, H - 6, W + 6, H+6, W - 6, H+6});
										e.gc.drawText(chartMarker.getLabel(), W + 3, getPlotArea().getClientArea().height - 15);
										break;
									case CERCLE:
										e.gc.setLineWidth(chartMarker.selected?3:1);
										e.gc.drawOval(W-6, H-6, 12, 12);
										e.gc.drawText(chartMarker.getLabel(), W + 3, getPlotArea().getClientArea().height - 15);
										break;
									default:
										e.gc.setLineWidth(chartMarker.selected?7:3);
										e.gc.drawLine(W, 0, W, getPlotArea().getClientArea().height);
										e.gc.drawText(chartMarker.getLabel(), W + (chartMarker.selected?7:3), getPlotArea().getClientArea().height - 15);
										break;
									}
									e.gc.setForeground(color);
								}
							}
						}
					}
				}
			} else {
				ISeries[] series = getSeriesSet().getSeries();
				for (int i = 0; i < series.length; i++) {
					double[] xValues  = series[i].getXSeries();
					double[] yValues = series[i].getYSeries();
//					long offsetIndex = 0;
//					if(xValues.length >= 2) offsetIndex = Math.round(xValues[0]/(xValues[1] - xValues[0]));
					IAxis xAxis = getAxisSet().getXAxis(series[i].getXAxisId());
					IAxis yAxis = getAxisSet().getYAxis(series[i].getYAxisId());
					IAxis[] allYAxis = getAxisSet().getYAxes();
					int W2 = ((GridLayout)getParent().getLayout()).horizontalSpacing;
					for (int j = 0; j < allYAxis.length; j++) 
						if(allYAxis[j].getPosition() == Position.Primary) 
							W2 = W2 + allYAxis[j].getTick().getBounds().width ;
					Object[] markers = chartMarkersSet.getChartMarkers();
					for (int j = 0; j < xValues.length; j++) {
						if((xAxis.getRange().lower <= xValues[j] && xValues[j]<= xAxis.getRange().upper) && (yAxis.getRange().lower <= yValues[j] && yValues[j]<= yAxis.getRange().upper)) {
							for (int k = 0; k < markers.length; k++) {
								ChartMarker chartMarker = (ChartMarker) markers[k];
								if(chartMarker.serieID.equals(series[i].getId()) && j == chartMarker.index) {
									int W = xAxis.getPixelCoordinate(xValues[j]);
									int H = yAxis.getPixelCoordinate(yValues[j]);
									Color lineColor = ((ILineSeries)series[i]).getLineColor();
									GC gc = new GC(this);
									gc.setForeground(lineColor);
									gc.setLineWidth(3);
									gc.drawLine(W + W2, getPlotArea().getClientArea().height, W + W2, getPlotArea().getClientArea().height + 15);
									gc.dispose();
									Color color = e.gc.getForeground();
									e.gc.setForeground(lineColor);
									switch (chartMarker.getGraphicSymbol()) {
									case LINE:
										e.gc.setLineWidth(chartMarker.selected?7:3);
										e.gc.drawLine(W, 0, W, getPlotArea().getClientArea().height);
										e.gc.drawText(chartMarker.getLabel(), W + (chartMarker.selected?7:3), getPlotArea().getClientArea().height - 15);
										break;
									case SQUARE:
										e.gc.setLineWidth(chartMarker.selected?3:1);
										e.gc.drawRectangle(W - 6, H - 6, 12, 12);
										e.gc.drawText(chartMarker.getLabel(), W + 3, getPlotArea().getClientArea().height - 15);
										break;
									case DIAMOND:
										e.gc.setLineWidth(chartMarker.selected?3:1);
										e.gc.drawPolyline(new int[] {W - 6, H, W, H - 6, W + 6, H, W, H + 6, W - 6, H});
										e.gc.drawText(chartMarker.getLabel(), W + 3, getPlotArea().getClientArea().height - 15);
										break;
									case CROSS:
										e.gc.setLineWidth(chartMarker.selected?3:1);
										e.gc.drawLine(W-6, H, W+6, H);
										e.gc.drawLine(W, H-6, W, H+6);
										e.gc.drawText(chartMarker.getLabel(), W + 3, getPlotArea().getClientArea().height - 15);
										break;
									case STAR:
										e.gc.setLineWidth(chartMarker.selected?3:1);
										e.gc.drawLine(W-6, H, W+6, H);
										e.gc.drawLine(W, H-6, W, H+6);
										e.gc.drawLine(W-6, H-6, W+6, H+6);
										e.gc.drawLine(W-6, H+6, W+6, H-6);
										e.gc.drawText(chartMarker.getLabel(), W + 3, getPlotArea().getClientArea().height - 15);
										break;
									case TRIANGLE:
										e.gc.setLineWidth(chartMarker.selected?3:1);
										e.gc.drawPolyline(new int[] {W - 6, H+6, W, H - 6, W + 6, H+6, W - 6, H+6});
										e.gc.drawText(chartMarker.getLabel(), W + 3, getPlotArea().getClientArea().height - 15);
										break;
									case CERCLE:
										e.gc.setLineWidth(chartMarker.selected?3:1);
										e.gc.drawOval(W-6, H-6, 12, 12);
										e.gc.drawText(chartMarker.getLabel(), W + 3, getPlotArea().getClientArea().height - 15);
										break;
									default:
										e.gc.setLineWidth(chartMarker.selected?7:3);
										e.gc.drawLine(W, 0, W, getPlotArea().getClientArea().height);
										e.gc.drawText(chartMarker.getLabel(), W + (chartMarker.selected?7:3), getPlotArea().getClientArea().height - 15);
										break;
									}
									e.gc.setForeground(color);
								}
							}
						}
					}
				}
			}
		}
	}

	public ISeries createSerie(SeriesType serieType, String newSerieID) {
		ISeries serie = getSeriesSet().createSeries(serieType, newSerieID);
		activeSerieId = newSerieID;
		xCoordinateValue = 0.0;
		yCoordinateValue = 0.0;
		notifyCoordinatesObservers();
		return serie;
	}

	public void deleteSerie(String serieID) {
		getSeriesSet().deleteSeries(serieID);
		Object[] markers = chartMarkersSet.getChartMarkers();
		ChartMarker chartMarker = null;
		for (int i = 0; i < markers.length; i++) {
			chartMarker = (ChartMarker) markers[i];
			if(chartMarker.serieID.equals(serieID)) chartMarkersSet.remove(chartMarker);
		}
	}

	public double getMarkerXValue(int index, String serieID) {
		double[] xValues  = getSeriesSet().getSeries(serieID).getXSeries();
		int offsetIndex = 0;
		if(xValues.length >= 2) offsetIndex = (int)Math.round(xValues[0]/(xValues[1] - xValues[0]));
		if(index > offsetIndex) return getSeriesSet().getSeries(serieID).getXSeries()[index - offsetIndex];
		else return Double.NaN;
	}

	protected double getMarkerYValue(int index, String serieID) {
		double[] xValues  = getSeriesSet().getSeries(serieID).getXSeries();
		int offsetIndex = 0;
		if(xValues.length >= 2) offsetIndex = (int)Math.round(xValues[0]/(xValues[1] - xValues[0]));
		if(index > offsetIndex) return getSeriesSet().getSeries(serieID).getYSeries()[index - offsetIndex];
		else return Double.NaN;
	}

	public ChartMarkersSet getChartMarkersSet() {
		return chartMarkersSet;
	}

	 /*
	  * @see Listener#handleEvent(Event)
	  */
	 @Override
	 public void handleEvent(Event event) {
		 super.handleEvent(event);

		 switch (event.type) {
		 case SWT.MouseMove:
			 handleMouseMoveEvent(event);
			 notifyCoordinatesObservers();
			 break;
		 case SWT.MouseDown:
			 handleMouseDownEvent(event);
			 if(bufferedImage != null) bufferedImage.dispose();
			 bufferedImage = null;
			 break;
		 case SWT.MouseUp:
			 handleMouseUpEvent(event);
			 notifyRangeObservers();
			 break;
		 case SWT.MouseWheel:
			 handleMouseWheel(event);
			 break;
		 case SWT.KeyDown:
			 handleKeyDownEvent(event);
			 break;
		 case SWT.Selection:
			 handleSelectionEvent(event);
			 break;
		 case SWT.Resize:
			 if(bufferedImage != null) bufferedImage.dispose();
			 bufferedImage = null;
			 break;
		 default:
			 break;
		 }
	 }

	 /*
	  * @see Chart#dispose()
	  */
	 @Override
	 public void dispose() {
		 super.dispose();
		 resources.dispose();
		 if(bufferedImageZoom != null) if(!bufferedImageZoom.isDisposed()) bufferedImageZoom.dispose();
		 if(bufferedImage != null) if(!bufferedImage.isDisposed()) bufferedImage.dispose();
	 }

	 /**
	  * Handles mouse move event.
	  * 
	  * @param event
	  *            the mouse move event
	  */
	 private void handleMouseMoveEvent(Event event) {
		 if (!selection.isDisposed()) {
			 GC gc = new GC(getPlotArea());
			 //Clean all plotArea						
			 gc.drawImage(bufferedImageZoom, 0, 0);			
			 //Draw new rectangle
			 selection.setEndPoint(event.x, event.y);			
			 selection.draw(gc);			
			 gc.dispose();	
		 } else if(showCrossHair) {
			 if(activeSerieId == null || getSeriesSet().getSeries(activeSerieId) == null) {
				 activeSerieId = null;
				 ISeries[] series = getSeriesSet().getSeries();
				 if(series.length > 0) activeSerieId = series[0].getId();
			 }
			 if(activeSerieId != null && !activeSerieId.matches("^\\w+\\.\\w+\\.Event\\d+\\.\\w+") ) {
				 getPlotArea().setFocus();
				 //Compute coordinates and draw crosshair
				 Point point = event.display.map((Control) event.widget, getPlotArea(), event.x, event.y);
				 computeCoordinates(point.x, point.y);
				 //Notify observers
				 notifyCoordinatesObservers();
			 }
		 }
	 }

	 private void drawCrossHair(GC gc) {
		 Point plotAreaSize = new Point(getPlotArea().getClientArea().width, getPlotArea().getClientArea().height);
		 ISeries serie = getSeriesSet().getSeries(activeSerieId);
		 Range yRange = getAxisSet().getYAxis(serie.getYAxisId()).getRange();
		 Range xRange = getAxisSet().getXAxis(serie.getXAxisId()).getRange();
		 //Height in screen space
		 int height = (int) ((1.0*plotAreaSize.y)*(yCoordinateValue - yRange.lower)/(yRange.upper - yRange.lower)); 
		 int ovalSize = 8;
		 if(bufferedImage != null) {
			 gc.drawImage(bufferedImage, mouseXPosition - ovalSize, 0);
			 bufferedImage.dispose();
			 bufferedImage = null;
		 }
		 int newMouseXPosition = (int) ((xCoordinateValue - xRange.lower) / (xRange.upper - xRange.lower) * plotAreaSize.x) ;
		 if(mouseXPosition > -1) {
			//save line to image			
			 bufferedImage = new Image(Display.getCurrent(), 2*ovalSize + 1, plotAreaSize.y);
			 gc.copyArea(bufferedImage, newMouseXPosition - ovalSize, 0);
			 //draw line		
			 Color color = gc.getForeground();
			 gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			 gc.drawLine(newMouseXPosition, 0, newMouseXPosition, plotAreaSize.y);
			 gc.drawOval(newMouseXPosition - ovalSize/2, plotAreaSize.y - height - 3*ovalSize/4, ovalSize, ovalSize);
			 gc.setForeground(color);
			 gc.dispose();
		 }
		 mouseXPosition = newMouseXPosition;
	 }

	 private void computeCoordinates(int mouseXPosition, int mouseYPosition) {
		 //retrieve point coordinate in active serie;
		 Point size = new Point(getPlotArea().getClientArea().width, getPlotArea().getClientArea().height);
		 ISeries serie = getSeriesSet().getSeries(activeSerieId);		
		 Range xRange = getAxisSet().getXAxis(serie.getXAxisId()).getRange();
		 double newXCoordinateValue = (1.0*mouseXPosition) / (1.0*size.x) * (xRange.upper - xRange.lower) + xRange.lower;
		 double[] nearestPoints = getNearestPoints(newXCoordinateValue);
		 if(nearestPoints == null) return;
		 xCoordinateValue = newXCoordinateValue;
		 yCoordinateValue = (nearestPoints[3] - nearestPoints[1]) / (nearestPoints[2] - nearestPoints[0]) * (xCoordinateValue - nearestPoints[0]) + nearestPoints[1];
		 //		System.out.println("e.x = " + mouseXPosition);
		 //		System.out.println("e.y = " + mouseYPosition);
		 //		System.out.println("x = " + xCoordinateValue);				
		 //		System.out.println("y = " + yCoordinateValue);
		 //		System.out.println("x1 = " + nearestPoints[0] + " - y1 = " + nearestPoints[1]);
		 //		System.out.println("x2 = " + nearestPoints[2] + " - y2 = " + nearestPoints[3]);		
		 //Draw crosshair
		 GC gc = new GC(getPlotArea());
		 drawCrossHair(gc);
		 gc.dispose();
	 }


	 /**
	  * Handles the mouse down event.
	  * 
	  * @param event
	  *            the mouse down event
	  */
	 private void handleMouseDownEvent(Event event) {
		 if (event.button == 1 && (event.stateMask & SWT.CTRL) == 0) {
			 selection.setStartPoint(event.x, event.y);
			 clickedTime = System.currentTimeMillis();
			 //get snapshot of plotArea in an image buffer
			 bufferedImageZoom = new Image(Display.getCurrent(), getPlotArea().getClientArea().width, getPlotArea().getClientArea().height);
			 GC gc = new GC(getPlotArea());
			 gc.copyArea(bufferedImageZoom, 0, 0);
			 gc.dispose();
		 }
	 }

	 /**
	  * Handles the mouse up event.
	  * 
	  * @param event
	  *            the mouse up event
	  */
	 private void handleMouseUpEvent(Event event) {
		 if (event.button == 1 && System.currentTimeMillis() - clickedTime > 100) {
			 for (IAxis axis : getAxisSet().getAxes()) {
				 Point range = null;
				 if ((getOrientation() == SWT.HORIZONTAL && axis.getDirection() == Direction.X) || (getOrientation() == SWT.VERTICAL && axis.getDirection() == Direction.Y))
					 range = selection.getHorizontalRange();
				 else range = selection.getVerticalRange();
				 if (range != null && range.x != range.y) setRange(range, axis);
			 }
		 }
		 selection.dispose();
		 if(bufferedImageZoom != null) bufferedImageZoom.dispose();
		 bufferedImageZoom = null;
		 if(bufferedImage != null) bufferedImage.dispose();
		 bufferedImage = null;
		 mouseXPosition = -1;
		 redraw();
	 }

	 /**
	  * Handles mouse wheel event.
	  * 
	  * @param event
	  *            the mouse wheel event
	  */
	 private void handleMouseWheel(Event event) {
		 for (IAxis axis : getAxes(SWT.HORIZONTAL)) {
			 double coordinate = axis.getDataCoordinate(event.x);
			 if (event.count > 0) axis.zoomIn(coordinate);
			 else axis.zoomOut(coordinate);
		 }
		 for (IAxis axis : getAxes(SWT.VERTICAL)) {
			 double coordinate = axis.getDataCoordinate(event.y);
			 if (event.count > 0) axis.zoomIn(coordinate);
			 else axis.zoomOut(coordinate);
		 }
		 if(bufferedImage != null) bufferedImage.dispose();
		 bufferedImage = null;
		 mouseXPosition = -1;
		 redraw();
	 }

	 /**
	  * Handles the key down event.
	  * 
	  * @param event
	  *            the key down event
	  */
	 private void handleKeyDownEvent(Event event) {
		 if (event.keyCode == SWT.F5) {
			 redraw();
			 return;
		 }
		 else if (event.keyCode == SWT.ARROW_DOWN) {
			 if (event.stateMask == SWT.CTRL) {
				 getAxisSet().zoomOut();
			 } else {
				 for (IAxis axis : getAxes(SWT.VERTICAL)) {
					 axis.scrollDown();
				 }
			 }
		 } else if (event.keyCode == SWT.ARROW_UP) {
			 if (event.stateMask == SWT.CTRL) {
				 getAxisSet().zoomIn();
			 } else {
				 for (IAxis axis : getAxes(SWT.VERTICAL)) {
					 axis.scrollUp();
				 }
			 }
		 } else if (event.keyCode == SWT.ARROW_LEFT) {
			 for (IAxis axis : getAxes(SWT.HORIZONTAL)) {
				 axis.scrollDown();
			 }
		 } else if (event.keyCode == SWT.ARROW_RIGHT) {
			 for (IAxis axis : getAxes(SWT.HORIZONTAL)) {
				 axis.scrollUp();
			 }
		 } else if(event.keyCode == SWT.TAB) {
			 ISeries[] seriesArray = getSeriesSet().getSeries();
			 if (seriesArray.length == 0) return;
			 String[] seriesIDArray = new String[seriesArray.length];
			 for (int i = 0; i < seriesArray.length; i++) seriesIDArray[i] = seriesArray[i].getId();
			 Arrays.sort(seriesIDArray);
			 int j = 0;
			 if(activeSerieId != null)
				 for (int i = 0; i < seriesIDArray.length; i++)
					 if(activeSerieId.equals(seriesIDArray[i])) {
						 j = i;
						 break;
					 }
			 j = (event.stateMask == SWT.SHIFT) ? j-1:j+1;
			 j = (j<0) ? seriesIDArray.length - 1 : j;
			 activeSerieId = seriesIDArray[ j % seriesIDArray.length ];					
			 Event e1 = new Event();
			 e1.x = mouseXPosition;
			 getPlotArea().notifyListeners(SWT.MouseMove, e1);
			 return;
		 }
		 if(bufferedImage != null) bufferedImage.dispose();
		 bufferedImage = null;
		 mouseXPosition = -1;
		 redraw();
		 notifyRangeObservers();
	 }

	 /**
	  * Gets the axes for given orientation.
	  * 
	  * @param orientation
	  *            the orientation
	  * @return the axes
	  */
	 private IAxis[] getAxes(int orientation) {
		 IAxis[] axes;
		 if (getOrientation() == orientation) {
			 axes = getAxisSet().getXAxes();
		 } else {
			 axes = getAxisSet().getYAxes();
		 }
		 return axes;
	 }

	 /**
	  * Handles the selection event.
	  * 
	  * @param event
	  *            the event
	  */
	 private void handleSelectionEvent(Event event) {

		 if (!(event.widget instanceof MenuItem)) {
			 return;
		 }
		 MenuItem menuItem = (MenuItem) event.widget;

		 if (menuItem.getText().equals(Messages.ADJUST_AXIS_RANGE)) {
			 getAxisSet().adjustRange();
		 } else if (menuItem.getText().equals(Messages.ADJUST_X_AXIS_RANGE)) {
			 for (IAxis axis : getAxisSet().getXAxes()) {
				 axis.adjustRange();
			 }
		 } else if (menuItem.getText().equals(Messages.ADJUST_Y_AXIS_RANGE)) {
			 for (IAxis axis : getAxisSet().getYAxes()) {
				 axis.adjustRange();
			 }
		 } else if (menuItem.getText().equals(Messages.ZOOMIN)) {
			 getAxisSet().zoomIn();
		 } else if (menuItem.getText().equals(Messages.ZOOMIN_X)) {
			 for (IAxis axis : getAxisSet().getXAxes()) {
				 axis.zoomIn();
			 }
		 } else if (menuItem.getText().equals(Messages.ZOOMIN_Y)) {
			 for (IAxis axis : getAxisSet().getYAxes()) {
				 axis.zoomIn();
			 }
		 } else if (menuItem.getText().equals(Messages.ZOOMOUT)) {
			 getAxisSet().zoomOut();
		 } else if (menuItem.getText().equals(Messages.ZOOMOUT_X)) {
			 for (IAxis axis : getAxisSet().getXAxes()) {
				 axis.zoomOut();
			 }
		 } else if (menuItem.getText().equals(Messages.ZOOMOUT_Y)) {
			 for (IAxis axis : getAxisSet().getYAxes()) {
				 axis.zoomOut();
			 }
		 } else if (menuItem.getText().equals(Messages.SAVE_AS)) {
			 openSaveAsDialog();
		 } else if (menuItem.getText().equals(Messages.PROPERTIES)) {
			 openPropertiesDialog();
		 }
		 if(bufferedImage != null) bufferedImage.dispose();
		 bufferedImage = null;
		 mouseXPosition = -1;
		 redraw();
		 notifyRangeObservers();
	 }

	 /**
	  * Opens the Save As dialog.
	  */
	 private void openSaveAsDialog() {
		 FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		 dialog.setText(Messages.SAVE_AS_DIALOG_TITLE);
		 dialog.setFilterExtensions(EXTENSIONS);

		 final String filename = dialog.open();
		 if (filename == null) {
			 return;
		 }

		 final int format;
		 if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
			 format = SWT.IMAGE_JPEG;
		 } else if (filename.endsWith(".png")) {
			 format = SWT.IMAGE_PNG;
		 } else {
			 format = SWT.IMAGE_UNDEFINED;
		 }

		 if (format != SWT.IMAGE_UNDEFINED) {
			 Timer timer = new Timer();
			 TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							InteractiveChart.this.save(filename, format);
							
						}
					});
				}
			};
			timer.schedule(timerTask, 1000);
		 } else MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Image Formats", "Image formats are " + EXTENSIONS_STRING);
	 }

	 /**
	  * Opens the properties dialog.
	  */
	 public void openPropertiesDialog() {
		 PreferenceManager manager = new PreferenceManager();

		 final String chartTitle = "Chart";
		 PreferenceNode chartNode = new PreferenceNode(chartTitle);
		 chartNode.setPage(new ChartPage(this, resources, chartTitle));
		 manager.addToRoot(chartNode);

		 final String legendTitle = "Legend";
		 PreferenceNode legendNode = new PreferenceNode(legendTitle);
		 legendNode.setPage(new LegendPage(this, resources, legendTitle));
		 manager.addTo(chartTitle, legendNode);

		 final String xAxisTitle = "X Axis";
		 PreferenceNode xAxisNode = new PreferenceNode(xAxisTitle);
		 xAxisNode
		 .setPage(new AxisPage(this, resources, Direction.X, xAxisTitle));
		 manager.addTo(chartTitle, xAxisNode);

		 final String gridTitle = "Grid";
		 PreferenceNode xGridNode = new PreferenceNode(gridTitle);
		 xGridNode
		 .setPage(new GridPage(this, resources, Direction.X, gridTitle));
		 manager.addTo(chartTitle + "." + xAxisTitle, xGridNode);

		 final String tickTitle = "Tick";
		 PreferenceNode xTickNode = new PreferenceNode(tickTitle);
		 xTickNode.setPage(new AxisTickPage(this, resources, Direction.X,
				 tickTitle));
		 manager.addTo(chartTitle + "." + xAxisTitle, xTickNode);

		 final String yAxisTitle = "Y Axis";
		 PreferenceNode yAxisNode = new PreferenceNode(yAxisTitle);
		 yAxisNode
		 .setPage(new AxisPage(this, resources, Direction.Y, yAxisTitle));
		 manager.addTo(chartTitle, yAxisNode);

		 PreferenceNode yGridNode = new PreferenceNode(gridTitle);
		 yGridNode
		 .setPage(new GridPage(this, resources, Direction.Y, gridTitle));
		 manager.addTo(chartTitle + "." + yAxisTitle, yGridNode);

		 PreferenceNode yTickNode = new PreferenceNode(tickTitle);
		 yTickNode.setPage(new AxisTickPage(this, resources, Direction.Y,
				 tickTitle));
		 manager.addTo(chartTitle + "." + yAxisTitle, yTickNode);

		 final String seriesTitle = "Series";
		 PreferenceNode plotNode = new PreferenceNode(seriesTitle);
		 plotNode.setPage(new SeriesPage(this, resources, seriesTitle));
		 manager.addTo(chartTitle, plotNode);

		 final String labelTitle = "Label";
		 PreferenceNode labelNode = new PreferenceNode(labelTitle);
		 labelNode.setPage(new SeriesLabelPage(this, resources, labelTitle));
		 manager.addTo(chartTitle + "." + seriesTitle, labelNode);

		 PreferenceDialog dialog = new PreferenceDialog(getShell(), manager);
		 dialog.create();
		 dialog.getShell().setText("Properties");
		 dialog.getTreeViewer().expandAll();
		 dialog.open();
	 }

	 /**
	  * Sets the axis range.
	  * 
	  * @param range
	  *            the axis range in pixels
	  * @param axis
	  *            the axis to set range
	  */
	 private void setRange(Point range, IAxis axis) {
		 if (range == null) {
			 return;
		 }

		 double min = axis.getDataCoordinate(range.x);
		 double max = axis.getDataCoordinate(range.y);

		 axis.setRange(new Range(min, max));
	 }

	 public void addRangeObserver(IRangeObserver zoomObserver) {
		 rangeObservers.add(zoomObserver);
	 }

	 public void addCoordinatesObserver(ICoordinatesObserver coordinatesObserver) {
		 coordinatesObservers.add(coordinatesObserver);
	 }

	 public void notifyRangeObservers() {
		 IRangeObserver[] observers = rangeObservers.toArray(new IRangeObserver[rangeObservers.size()]);
		 for (int i = 0; i < observers.length; i++) {
			 observers[i].updateRange();
		 }
	 }

	 public void notifyCoordinatesObservers() {
		 if(showCrossHair) {
			 ICoordinatesObserver[] observers = coordinatesObservers.toArray(new ICoordinatesObserver[coordinatesObservers.size()]);
			 for (int i = 0; i < observers.length; i++) {
				 double[] c = getCrossHairCoordinates();
				 if(activeSerieId == null)
					 observers[i].updateCoordinates(0, 0, "No selected serie [0.0 ; 0.0]");
				 else observers[i].updateCoordinates(c[0], c[1], activeSerieId + " [" + c[0] + " ; " + c[1] + "]");
			 }
		 }
	 }

	 public void removeRangeObserver(IRangeObserver zoomObserver) {
		 rangeObservers.remove(zoomObserver);
	 }

	 public void setCrossHairVisibility(boolean showCrossHair) {
		 this.showCrossHair = showCrossHair;
		 redraw();
	 }

	 public void setShowMarkers(boolean showMarkers) {
		 this.showMarkers = showMarkers;
		 redraw();
	 }

	 public double[] getNearestPoints(double x) {
		 //if(isCompressEnabled()) {
			 //    		double[] xCompressed = getCompressor().getCompressedXSeries();
			 //        	double[] yCompressed = getCompressor().getCompressedYSeries();
			 //         	double[] point = new double[4];
			 //        	for (int i = 0; i < xCompressed.length; i++) {
			 //    			if(xCompressed[i] > x){	
			 //    				if(i==0) return null;
			 //    				point[0] = xCompressed[i-1];
			 //    				point[1] = yCompressed[i-1];
			 //    				point[2] = xCompressed[i];
			 //    				point[3] = yCompressed[i];
			 //    				return point;
			 //    			} 
			 //    		}
		 //} else {
			 ISeries serie = getSeriesSet().getSeries(activeSerieId);
			 double[] xValues = serie.getXSeries();
			 double[] yValues = serie.getYSeries();
			 double[] point = new double[4];
			 for (int i = 0; i < xValues.length; i++) {
				 if(xValues[i] > x){	
					 if(i==0) return null;
					 point[0] = xValues[i-1];
					 point[1] = yValues[i-1];
					 point[2] = xValues[i];
					 point[3] = yValues[i];
					 return point;
				 } 
			 }
		 //}
		 return null;
	 }

	 public double[] getNearestPointCoordinates(double x) {
		 double[] nearestPoints = getNearestPoints(x);
		 if(Math.abs(x-nearestPoints[0]) > Math.abs(x-nearestPoints[2])) return new double[]{nearestPoints[2],nearestPoints[3]};
		 else return new double[]{nearestPoints[0],nearestPoints[1]};
	 }

	 public double[] getCrossHairCoordinates() {
		 return new double[]{xCoordinateValue,yCoordinateValue};
	 }

	 public void setActiveSerieId(String activeSerieId) {
		 this.activeSerieId = activeSerieId;
	 }

	 public String getActiveSerieId() {
		 return activeSerieId;
	 }

	 /**
		 * Creates menu items.
		 */
		 private void createMenuItems() {
			Menu menu = new Menu(getPlotArea());
			getPlotArea().setMenu(menu);

			// adjust axis range menu group
			MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
			menuItem.setText(Messages.ADJUST_AXIS_RANGE_GROUP);
			Menu adjustAxisRangeMenu = new Menu(menuItem);
			menuItem.setMenu(adjustAxisRangeMenu);

			// adjust axis range
			menuItem = new MenuItem(adjustAxisRangeMenu, SWT.PUSH);
			menuItem.setText(Messages.ADJUST_AXIS_RANGE);
			menuItem.addListener(SWT.Selection, this);

			// adjust X axis range
			menuItem = new MenuItem(adjustAxisRangeMenu, SWT.PUSH);
			menuItem.setText(Messages.ADJUST_X_AXIS_RANGE);
			menuItem.addListener(SWT.Selection, this);

			// adjust Y axis range
			menuItem = new MenuItem(adjustAxisRangeMenu, SWT.PUSH);
			menuItem.setText(Messages.ADJUST_Y_AXIS_RANGE);
			menuItem.addListener(SWT.Selection, this);

			menuItem = new MenuItem(menu, SWT.SEPARATOR);

			// zoom in menu group
			menuItem = new MenuItem(menu, SWT.CASCADE);
			menuItem.setText(Messages.ZOOMIN_GROUP);
			Menu zoomInMenu = new Menu(menuItem);
			menuItem.setMenu(zoomInMenu);

			// zoom in both axes
			menuItem = new MenuItem(zoomInMenu, SWT.PUSH);
			menuItem.setText(Messages.ZOOMIN);
			menuItem.addListener(SWT.Selection, this);

			// zoom in X axis
			menuItem = new MenuItem(zoomInMenu, SWT.PUSH);
			menuItem.setText(Messages.ZOOMIN_X);
			menuItem.addListener(SWT.Selection, this);

			// zoom in Y axis
			menuItem = new MenuItem(zoomInMenu, SWT.PUSH);
			menuItem.setText(Messages.ZOOMIN_Y);
			menuItem.addListener(SWT.Selection, this);

			// zoom out menu group
			menuItem = new MenuItem(menu, SWT.CASCADE);
			menuItem.setText(Messages.ZOOMOUT_GROUP);
			Menu zoomOutMenu = new Menu(menuItem);
			menuItem.setMenu(zoomOutMenu);

			// zoom out both axes
			menuItem = new MenuItem(zoomOutMenu, SWT.PUSH);
			menuItem.setText(Messages.ZOOMOUT);
			menuItem.addListener(SWT.Selection, this);

			// zoom out X axis
			menuItem = new MenuItem(zoomOutMenu, SWT.PUSH);
			menuItem.setText(Messages.ZOOMOUT_X);
			menuItem.addListener(SWT.Selection, this);

			// zoom out Y axis
			menuItem = new MenuItem(zoomOutMenu, SWT.PUSH);
			menuItem.setText(Messages.ZOOMOUT_Y);
			menuItem.addListener(SWT.Selection, this);

			menuItem = new MenuItem(menu, SWT.SEPARATOR);

			// save as
			menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText(Messages.SAVE_AS);
			menuItem.addListener(SWT.Selection, this);

			menuItem = new MenuItem(menu, SWT.SEPARATOR);

			// properties
			menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText(Messages.PROPERTIES);
			menuItem.addListener(SWT.Selection, this);
		 }

	public int canAddMarkerAtCurrentMousePosition(int markerGroup, double sampleFrequency) {
		int index = (int) Math.round(xCoordinateValue*sampleFrequency);
		Object[] markers = getChartMarkersSet().getChartMarkers();
		for (int i = 0; i < markers.length; i++) {
			ChartMarker chartMarker = (ChartMarker) markers[i];
			if(chartMarker.index == index && chartMarker.groupNumber == markerGroup) return -1;
		}
		return index;
	}
	
}
