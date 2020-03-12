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
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public final class DragDropChartElementDelegate {
	
	public static Control draggedChartElement;
	
	public static void install(final ChartElement chartElement, int style) {
		final Composite container = chartElement.getParent();
		DragSource dragSource = new DragSource(chartElement, DND.DROP_MOVE);
		dragSource.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		dragSource.addDragListener(new DragSourceListener() {
			public void dragStart(DragSourceEvent event) {
				draggedChartElement = ((DragSource)event.widget).getControl();
			}
			public void dragSetData(DragSourceEvent event) {
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					event.data = event.widget.toString();
				}
			}
			public void dragFinished(DragSourceEvent event) {
			}
		});
		
		DropTarget dropTarget = new DropTarget(chartElement, style);
		dropTarget.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		dropTarget.addDropListener(new DropTargetListener() {
			public void dropAccept(DropTargetEvent event) {
			}
			public void drop(DropTargetEvent event) {
				Control targetChart = ((DropTarget) event.widget).getControl();
				switchCharts(chartElement, targetChart);
			}
			public void dragOver(DropTargetEvent event) {
			}
			public void dragOperationChanged(DropTargetEvent event) {
			}
			public void dragLeave(DropTargetEvent event) {
				Control targetChart = ((DropTarget) event.widget).getControl();
				if(targetChart != draggedChartElement) targetChart.setBackground(container.getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			}
			public void dragEnter(DropTargetEvent event) {
				Control targetChart = ((DropTarget) event.widget).getControl();
				if(targetChart != draggedChartElement) targetChart.setBackground(container.getShell().getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
			}
		});
	}
	
	public static void switchCharts(Control currentChart, Control targetChart) {
		if(draggedChartElement != null) 
			if(draggedChartElement != currentChart) {
				int targetIndex = getIndex(targetChart);
				int selectedIndex = getIndex(draggedChartElement);
				if((targetIndex > -1) && (selectedIndex > -1)) {
					if(targetIndex > selectedIndex) draggedChartElement.moveBelow(targetChart);
					else draggedChartElement.moveAbove(targetChart);
					((Composite)draggedChartElement.getParent()).layout();
				}
			}
	}
	
	private static int getIndex(Control chart) {
		Control[] controls = ((Composite)draggedChartElement.getParent()).getChildren();
		for (int i = 0; i < controls.length; i++) {
			if(controls[i] == chart) return i;
		}
		return -1;
	}

}
