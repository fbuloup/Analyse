/*******************************************************************************
 * Copyright (c) 2008-2009 SWTChart project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.swtchart.internal.series;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IEventSeries;
import org.swtchart.LineStyle;
import org.swtchart.Range;
import org.swtchart.IAxis.Direction;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.internal.Util;
import org.swtchart.internal.axis.Axis;
import org.swtchart.internal.compress.CompressLineSeries;
import org.swtchart.internal.compress.CompressScatterSeries;

/**
 * Line series.
 */
public class EventSeries extends Series implements IEventSeries {

    /** the symbol size in pixel */
    private int symbolSize;

    /** the symbol color */
    private Color symbolColor;

    /** the symbol colors */
    private Color[] symbolColors;

    /** the symbol type */
    private PlotSymbolType symbolType;

    /** the line style */
    private LineStyle lineStyle;

    /** the line color */
    private Color lineColor;

    /** the line width */
    private int lineWidth;

    /** the anti-aliasing value for drawing line */
    private int antialias;

    /** the height of the events to plot*/
	private double[] hSeries;

    /** the default line style */
    private static final LineStyle DEFAULT_LINE_STYLE = LineStyle.SOLID;

    /** the default line width */
    private static final int DEFAULT_LINE_WIDTH = 1;

    /** the default line color */
    private static final int DEFAULT_LINE_COLOR = SWT.COLOR_BLUE;

    /** the default symbol color */
    private static final int DEFAULT_SYMBOL_COLOR = SWT.COLOR_DARK_GRAY;

    /** the default symbol size */
    private static final int DEFAULT_SIZE = 4;

    /** the default symbol type */
    private static final PlotSymbolType DEFAULT_SYMBOL_TYPE = PlotSymbolType.CIRCLE;

    /** the default anti-aliasing value */
    private static final int DEFAULT_ANTIALIAS = SWT.DEFAULT;

    /** the margin in pixels attached at the minimum/maximum plot */
    private static final int MARGIN_AT_MIN_MAX_PLOT = 6;
    
    /** show events height as infinite*/
    private boolean showHeightAsInfinite = false;

    /**
     * Constructor.
     * 
     * @param chart
     *            the chart
     * @param id
     *            the series id
     */
    protected EventSeries(Chart chart, String id) {
        super(chart, id);

        symbolSize = 4;
        symbolColor = Display.getDefault().getSystemColor(DEFAULT_SYMBOL_COLOR);
        symbolType = DEFAULT_SYMBOL_TYPE;

        lineStyle = DEFAULT_LINE_STYLE;
        lineColor = Display.getDefault().getSystemColor(DEFAULT_LINE_COLOR);

        antialias = DEFAULT_ANTIALIAS;
        lineWidth = DEFAULT_LINE_WIDTH;

        compressor = new CompressLineSeries();
    }

    /*
     * @see ILineSeries#getLineStyle()
     */
    public LineStyle getLineStyle() {
        return lineStyle;
    }

    /*
     * @see ILineSeries#setLineStyle(LineStyle)
     */
    public void setLineStyle(LineStyle style) {
        if (style == null) {
            this.lineStyle = DEFAULT_LINE_STYLE;
            return;
        }

        this.lineStyle = style;
        if (compressor instanceof CompressScatterSeries) {
            ((CompressScatterSeries) compressor)
                    .setLineVisible(style != LineStyle.NONE);
        }
    }

    /*
     * @see ILineSeries#getLineColor()
     */
    public Color getLineColor() {
        if (lineColor.isDisposed()) {
            lineColor = Display.getDefault().getSystemColor(DEFAULT_LINE_COLOR);
        }
        return lineColor;
    }

    /*
     * @see ILineSeries#setLineColor(Color)
     */
    public void setLineColor(Color color) {
        if (color != null && color.isDisposed()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }

        if (color == null) {
            this.lineColor = Display.getDefault().getSystemColor(
                    DEFAULT_LINE_COLOR);
        } else {
            this.lineColor = color;
        }
    }

    /*
     * @see ILineSeries#getLineWidth()
     */
    public int getLineWidth() {
        return lineWidth;
    }

    /*
     * @see ILineSeries#setLineWidth(int)
     */
    public void setLineWidth(int width) {
        if (width <= 0) {
            this.lineWidth = DEFAULT_LINE_WIDTH;
        } else {
            this.lineWidth = width;
        }
    }

    /*
     * @see ILineSeries#getSymbolType()
     */
    public PlotSymbolType getSymbolType() {
        return symbolType;
    }

    /*
     * @see ILineSeries#setSymbolType(PlotSymbolType)
     */
    public void setSymbolType(PlotSymbolType type) {
        if (type == null) {
            this.symbolType = DEFAULT_SYMBOL_TYPE;
        } else {
            this.symbolType = type;
        }
    }

    /*
     * @see ILineSeries#getSymbolSize()
     */
    public int getSymbolSize() {
        return symbolSize;
    }

    /*
     * @see ILineSeries#setSymbolSize(int)
     */
    public void setSymbolSize(int size) {
        if (size <= 0) {
            this.symbolSize = DEFAULT_SIZE;
        } else {
            this.symbolSize = size;
        }
    }

    /*
     * @see ILineSeries#getSymbolColor()
     */
    public Color getSymbolColor() {
        return symbolColor;
    }

    /*
     * @see ILineSeries#setSymbolColor(Color)
     */
    public void setSymbolColor(Color color) {
        if (color != null && color.isDisposed()) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }

        if (color == null) {
            this.symbolColor = Display.getDefault().getSystemColor(
                    DEFAULT_SYMBOL_COLOR);
        } else {
            this.symbolColor = color;
        }
    }

    /*
     * @see ILineSeries#getSymbolColors()
     */
    public Color[] getSymbolColors() {
        if (symbolColors == null) {
            return null;
        }

        Color[] copiedSymbolColors = new Color[symbolColors.length];
        System.arraycopy(symbolColors, 0, copiedSymbolColors, 0,
                symbolColors.length);

        return copiedSymbolColors;
    }

    /*
     * @see ILineSeries#setSymbolColors(Color [])
     */
    public void setSymbolColors(Color[] colors) {
        if (colors == null) {
            symbolColors = null;
            return;
        }

        for (Color color : colors) {
            if (color.isDisposed()) {
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            }
        }

        symbolColors = new Color[colors.length];
        System.arraycopy(colors, 0, symbolColors, 0, colors.length);
    }

    /*
     * @see Series#setCompressor()
     */
    @Override
    protected void setCompressor() {
        if (isXMonotoneIncreasing) {
            compressor = new CompressLineSeries();
        } else {
            compressor = new CompressScatterSeries();
            ((CompressScatterSeries) compressor)
                    .setLineVisible(getLineStyle() != LineStyle.NONE);
        }
    }

    /*
     * @see Series#getAdjustedRange(Axis, int)
     */
    @Override
    public Range getAdjustedRange(Axis axis, int length) {

        Range range;
        if (axis.getDirection() == Direction.X) {
            range = getXRange();
        } else {
            range = getYRange();
        }

        int lowerPlotMargin = getSymbolSize() + MARGIN_AT_MIN_MAX_PLOT;
        int upperPlotMargin = getSymbolSize() + MARGIN_AT_MIN_MAX_PLOT;

        return getRangeWithMargin(lowerPlotMargin, upperPlotMargin, length,
                axis, range);
    }

    /*
     * @see ILineSeries#getAntialias()
     */
    public int getAntialias() {
        return antialias;
    }

    /*
     * @see ILineSeries#setAntialias(int)
     */
    public void setAntialias(int antialias) {
        if (antialias != SWT.DEFAULT && antialias != SWT.ON
                && antialias != SWT.OFF) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.antialias = antialias;
    }

    /*
     * @see Series#draw(GC, int, int, Axis, Axis)
     */
    @Override
    protected void draw(GC gc, int width, int height, Axis xAxis, Axis yAxis) {
        int oldAntialias = gc.getAntialias();
        int oldLineWidth = gc.getLineWidth();
        gc.setAntialias(antialias);
        gc.setLineWidth(lineWidth);

        if (lineStyle != LineStyle.NONE) {
            drawEvents(gc, width, height, xAxis, yAxis);
        }

        if (symbolType != PlotSymbolType.NONE || getLabel().isVisible()
                || getXErrorBar().isVisible() || getYErrorBar().isVisible()) {
            drawSymbolAndLabel(gc, width, height, xAxis, yAxis);
        }

        gc.setAntialias(oldAntialias);
        gc.setLineWidth(oldLineWidth);
    }

    /**
     * Draws the line and area.
     * 
     * @param gc
     *            the graphics context
     * @param width
     *            the width to draw series
     * @param height
     *            the height to draw series
     * @param xAxis
     *            the x axis
     * @param yAxis
     *            the y axis
     */
    private void drawEvents(GC gc, int width, int height, Axis xAxis, Axis yAxis) {

        // get x and y series
        double[] xseries = compressor.getCompressedXSeries();
        double[] yseries = compressor.getCompressedYSeries();
        if (xseries.length == 0 || yseries.length == 0){
            return;
        }
        int[] indexes = compressor.getCompressedIndexes();
        if (xAxis.isValidCategoryAxis()) {
            for (int i = 0; i < xseries.length; i++) {
                xseries[i] = indexes[i];
            }
        }

        gc.setLineStyle(Util.getIndexDefinedInSWT(lineStyle));
        gc.setForeground(getLineColor());

        boolean isHorizontal = xAxis.isHorizontalAxis();
        
        double xLower = xAxis.getRange().lower;
        double xUpper = xAxis.getRange().upper;
        double yLower = yAxis.getRange().lower;
        double yUpper = yAxis.getRange().upper;

        for (int i = 0; i < xseries.length; i++) {
            int x = xAxis.getPixelCoordinate(xseries[i], xLower, xUpper);
            int h1 = chart.getPlotArea().getBounds().height;
        	int h2 = 0;
            if(!Double.isInfinite(getHSeries()[i]) && !showHeightAsInfinite) {
            	h1 = yAxis.getPixelCoordinate(yseries[i] - getHSeries()[i]/2, yLower, yUpper);
                h2 = yAxis.getPixelCoordinate(yseries[i] + getHSeries()[i]/2, yLower, yUpper);
            }
            h1=h1<0?0:h1;
            h1=h1>chart.getPlotArea().getBounds().height?chart.getPlotArea().getBounds().height:h1;
            h2=h2<0?0:h2;
            h2=h2>chart.getPlotArea().getBounds().height?chart.getPlotArea().getBounds().height:h2;
            if (isHorizontal) {
            	gc.drawLine(x, h1, x, h2);
            	
            } else {
            	gc.drawLine(h1, x, h2, x);
            }
        }
    }

    /**
     * Draws series symbol, label and error bars.
     * 
     * @param gc
     *            the graphics context
     * @param width
     *            the width to draw series
     * @param height
     *            the height to draw series
     * @param xAxis
     *            the x axis
     * @param yAxis
     *            the y axis
     */
    private void drawSymbolAndLabel(GC gc, int width, int height, Axis xAxis,
            Axis yAxis) {

        // get x and y series
        double[] xseries = compressor.getCompressedXSeries();
        double[] yseries = compressor.getCompressedYSeries();
        int[] indexes = compressor.getCompressedIndexes();
        if (xAxis.isValidCategoryAxis()) {
            boolean isValidStackSeries = isValidStackSeries();
            for (int i = 0; i < xseries.length; i++) {
                xseries[i] = indexes[i];
                if (isValidStackSeries) {
                    yseries[i] = stackSeries[indexes[i]];
                }
            }
        }

        // draw symbol and label
        for (int i = 0; i < xseries.length; i++) {
            Color color = getSymbolColor();
            if (symbolColors != null && symbolColors.length > i) {
                color = symbolColors[i];
            }
            int h, v;
            if (xAxis.isHorizontalAxis()) {
                h = xAxis.getPixelCoordinate(xseries[i]);
                v = yAxis.getPixelCoordinate(yseries[i]);
            } else {
                v = xAxis.getPixelCoordinate(xseries[i]);
                h = yAxis.getPixelCoordinate(yseries[i]);
            }
            if (getSymbolType() != PlotSymbolType.NONE) {
                drawSeriesSymbol(gc, h, v, color);
            }
            seriesLabel.draw(gc, h, v, yseries[i], indexes[i], SWT.BOTTOM);
            xErrorBar.draw(gc, h, v, xAxis, indexes[i]);
            yErrorBar.draw(gc, h, v, yAxis, indexes[i]);
        }
    }

    /**
     * Draws series symbol.
     * 
     * @param gc
     *            the GC object
     * @param h
     *            the horizontal coordinate to draw symbol
     * @param v
     *            the vertical coordinate to draw symbol
     * @param color
     *            the symbol color
     */
    public void drawSeriesSymbol(GC gc, int h, int v, Color color) {
        int oldAntialias = gc.getAntialias();
        gc.setAntialias(SWT.ON);
        gc.setForeground(color);
        gc.setBackground(color);

        switch (symbolType) {
        case CIRCLE:
            gc.fillOval(h - symbolSize, v - symbolSize, symbolSize * 2,
                    symbolSize * 2);
            break;
        case SQUARE:
            gc.fillRectangle(h - symbolSize, v - symbolSize, symbolSize * 2,
                    symbolSize * 2);
            break;
        case DIAMOND:
            int[] diamondArray = { h, v - symbolSize, h + symbolSize, v, h,
                    v + symbolSize, h - symbolSize, v };
            gc.fillPolygon(diamondArray);
            break;
        case TRIANGLE:
            int[] triangleArray = { h, v - symbolSize, h + symbolSize,
                    v + symbolSize, h - symbolSize, v + symbolSize };
            gc.fillPolygon(triangleArray);
            break;
        case INVERTED_TRIANGLE:
            int[] invertedTriangleArray = { h, v + symbolSize, h + symbolSize,
                    v - symbolSize, h - symbolSize, v - symbolSize };
            gc.fillPolygon(invertedTriangleArray);
            break;
        case CROSS:
            gc.setLineStyle(SWT.LINE_SOLID);
            gc.drawLine(h - symbolSize, v - symbolSize, h + symbolSize, v
                    + symbolSize);
            gc.drawLine(h - symbolSize, v + symbolSize, h + symbolSize, v
                    - symbolSize);
            break;
        case PLUS:
            gc.setLineStyle(SWT.LINE_SOLID);
            gc.drawLine(h, v - symbolSize, h, v + symbolSize);
            gc.drawLine(h - symbolSize, v, h + symbolSize, v);
            break;
        case NONE:
        default:
            break;
        }
        gc.setAntialias(oldAntialias);
    }

	public double[] getHSeries() {
		if (hSeries == null) return null;
        double[] copiedSeries = new double[hSeries.length];
        System.arraycopy(hSeries, 0, copiedSeries, 0, hSeries.length);
        return copiedSeries;
	}

	public void setHSeries(double[] series) {
        if (series == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
            return; // to suppress warning...
        }
        hSeries = new double[series.length];
        System.arraycopy(series, 0, hSeries, 0, series.length);
	}

	public boolean isShowHeightAsInfinite() {
		return showHeightAsInfinite;
	}

	public void setShowHeightAsInfinite(boolean showHeightAsInfinite) {
		this.showHeightAsInfinite = showHeightAsInfinite;
	}
	
	
}
