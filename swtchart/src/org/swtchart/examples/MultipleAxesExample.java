package org.swtchart.examples;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.IAxis.Position;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ext.InteractiveChart;

/**
 * An example for multiple axes.
 */
public class MultipleAxesExample {

    private static final double[] ySeries1 = { 0.0, 0.38, 0.71, 0.92, 1.0,
            0.92, 0.71, 0.38, 0.0, -0.38, -0.71, -0.92, -1.0, -0.92, -0.71,
            -0.38 };

    private static final double[] ySeries2 = { 2, 11, 19, 23, 18, 15, 18, 26,
            29, 32, 47, 32, 31, 35, 30, 29 };
    
    private static final double[] ySeries3 = { 20, 110, 190, 230, 180, 150, 180, 260,
        290, 302, 407, 302, 301, 350, 300, 290 };
    
    private static final double[] ySeries4 = { -2, -11, -19, -23, -18, -15, -18, -26,
        -29, -32, -47, -32, -31, -35, -30, -29 };

    /**
     * The main method.
     * 
     * @param args
     *            the arguments.
     */
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Multiple Axes Example");
        shell.setSize(500, 400);
        shell.setLayout(new FillLayout());

        // create a chart
        InteractiveChart chart = new InteractiveChart(shell, SWT.NONE, false);

        // set titles
        chart.getTitle().setText("Multiple Axes Example");
        chart.getAxisSet().getXAxis(0).getTitle().setText("Data Points");
        chart.getAxisSet().getYAxis(0).getTitle().setText("Amplitude 1");

        // create second Y axis
        int axisId = chart.getAxisSet().createYAxis();

        // set the properties of second Y axis
        IAxis yAxis2 = chart.getAxisSet().getYAxis(axisId);
        yAxis2.setPosition(Position.Secondary);
        final Color RED = Display.getDefault().getSystemColor(SWT.COLOR_RED);
        yAxis2.getTick().setForeground(RED);
        yAxis2.getTitle().setForeground(RED);
        yAxis2.getTitle().setText("Amplitude 2");
        
        
        
        // create second Y axis
        int axisIdBis = chart.getAxisSet().createYAxis();

        // set the properties of second Y axis
        IAxis yAxis2Bis = chart.getAxisSet().getYAxis(axisIdBis);
        yAxis2Bis.setPosition(Position.Primary);
        final Color GREEN = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
        yAxis2Bis.getTick().setForeground(GREEN);
        yAxis2Bis.getTitle().setForeground(GREEN);
        yAxis2Bis.getTitle().setText("Amplitude 3");
        
        // create second Y axis
        int axisIdTer = chart.getAxisSet().createYAxis();

        // set the properties of second Y axis
        IAxis yAxis2Ter = chart.getAxisSet().getYAxis(axisIdTer);
        yAxis2Ter.setPosition(Position.Secondary);
        final Color CYAN = Display.getDefault().getSystemColor(SWT.COLOR_CYAN);
        yAxis2Ter.getTick().setForeground(CYAN);
        yAxis2Ter.getTitle().setForeground(CYAN);
        yAxis2Ter.getTitle().setText("Amplitude 4");
        
        

        // create line series
        ILineSeries lineSeries1 = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "line series 1");
        lineSeries1.setYSeries(ySeries1);
        
        ILineSeries lineSeries2 = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "line series 2");
        lineSeries2.setYSeries(ySeries2);
        lineSeries2.setLineColor(RED);
        
        ILineSeries lineSeries3 = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "line series 3");
        lineSeries3.setYSeries(ySeries3);
        lineSeries3.setLineColor(GREEN);
        lineSeries3.setYAxisId(axisIdBis);
        
        ILineSeries lineSeries4 = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "line series 4");
        lineSeries4.setYSeries(ySeries4);
        lineSeries4.setLineColor(CYAN);
        lineSeries4.setYAxisId(axisIdTer);

        // assign series to second Y axis
        lineSeries2.setYAxisId(axisId);

        // adjust the axis range
        chart.getAxisSet().adjustRange();

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}