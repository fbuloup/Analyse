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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import analyse.gui.dialogs.SignalsSelectionDialog;
import analyse.model.Experiments;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;


public final class ToolBarChartElementDelegate {
	
	public static void install(final ChartElement chartElement, final ToolBar toolBar) {
		
		ToolItem deleteToolItem = new ToolItem(toolBar, SWT.PUSH);
		deleteToolItem.setToolTipText(Messages.getString("deleteToolItem.Title"));
		deleteToolItem.setImage(ImagesUtils.getImage(IImagesKeys.DELETE_ICON));
		deleteToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				chartElement.remove();
			}
		});
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		ToolItem decreaseHorizontalSpanToolItem = new ToolItem(toolBar, SWT.PUSH);
		decreaseHorizontalSpanToolItem.setToolTipText(Messages.getString("decreaseHorizontalSpanToolItem.Title"));
		decreaseHorizontalSpanToolItem.setImage(ImagesUtils.getImage(IImagesKeys.PREVIOUS_TRIAL_ICON));
		decreaseHorizontalSpanToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridData layout = (GridData) chartElement.getLayoutData();
				if(layout.horizontalSpan > 1) {
					chartElement.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, layout.horizontalSpan - 1, layout.verticalSpan));
					chartElement.getParent().layout();
					chartElement.saveLayout();
				}
			}
		});
		
		ToolItem increaseHorizontalSpanToolItem = new ToolItem(toolBar, SWT.PUSH);
		increaseHorizontalSpanToolItem.setToolTipText(Messages.getString("increaseHorizontalSpanToolItem.Title"));
		increaseHorizontalSpanToolItem.setImage(ImagesUtils.getImage(IImagesKeys.NEXT_TRIAL_ICON));
		increaseHorizontalSpanToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridData layout = (GridData) chartElement.getLayoutData();
				if(layout.horizontalSpan == 0) layout.horizontalSpan = 1;
				chartElement.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, layout.horizontalSpan + 1, layout.verticalSpan));
				chartElement.getParent().layout();
				chartElement.saveLayout();
			}
		});
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		ToolItem decreaseVerticalSpanToolItem = new ToolItem(toolBar, SWT.PUSH);
		decreaseVerticalSpanToolItem.setToolTipText(Messages.getString("decreaseVerticalSpanToolItem.Title"));
		decreaseVerticalSpanToolItem.setImage(ImagesUtils.getImage(IImagesKeys.MOVE_FUNCTION_UP));
		decreaseVerticalSpanToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridData layout = (GridData) chartElement.getLayoutData();
				if(layout.verticalSpan == 0) layout.verticalSpan = 1;
				if(layout.verticalSpan > 1) {
					chartElement.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, layout.horizontalSpan, layout.verticalSpan - 1));
					chartElement.getParent().layout();
					chartElement.saveLayout();
				}
			}
		});
		
		ToolItem increaseVerticalSpanToolItem = new ToolItem(toolBar, SWT.PUSH);
		increaseVerticalSpanToolItem.setToolTipText(Messages.getString("increaseVerticalSpanToolItem.Title"));
		increaseVerticalSpanToolItem.setImage(ImagesUtils.getImage(IImagesKeys.MOVE_FUNCTION_DOWN));
		increaseVerticalSpanToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridData layout = (GridData) chartElement.getLayoutData();
				chartElement.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, layout.horizontalSpan, layout.verticalSpan + 1));
				chartElement.getParent().layout();
				chartElement.saveLayout();
			}
		});
		
		new ToolItem(toolBar, SWT.SEPARATOR);

		ToolItem chartConfigurationToolItem = new ToolItem(toolBar, SWT.PUSH);
		chartConfigurationToolItem.setToolTipText(Messages.getString("chartConfigurationToolItem.Title"));
		chartConfigurationToolItem.setImage(ImagesUtils.getImage(IImagesKeys.CHART_CONFIGURATION_ICON));
		chartConfigurationToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new SignalsSelectionDialog(toolBar.getShell(), Experiments.getInstance().getLoadedSubjects(), chartElement).open();
			}
		});
	}

}
