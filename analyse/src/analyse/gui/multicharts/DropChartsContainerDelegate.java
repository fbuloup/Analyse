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
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;

public final class DropChartsContainerDelegate {
	
	public static void install(final Composite chartsContainer) {
		DropTarget target = new DropTarget(chartsContainer, DND.DROP_MOVE);
		target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		target.addDropListener(new DropTargetListener() {
			public void dropAccept(DropTargetEvent event) {
			}
			public void drop(DropTargetEvent event) {
				int nbControls = chartsContainer.getChildren().length;
				DragDropChartElementDelegate.draggedChartElement.moveBelow(chartsContainer.getChildren()[nbControls - 1]);
				chartsContainer.layout();
			}
			public void dragOver(DropTargetEvent event) {
			}
			public void dragOperationChanged(DropTargetEvent event) {
			}
			public void dragLeave(DropTargetEvent event) {
				chartsContainer.setBackground(chartsContainer.getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			}
			public void dragEnter(DropTargetEvent event) {
				chartsContainer.setBackground(chartsContainer.getShell().getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
			}
		});
	}

}
