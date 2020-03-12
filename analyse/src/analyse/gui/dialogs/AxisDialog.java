/*******************************************************************************
 * Universit� d�Aix Marseille (AMU) - Centre National de la Recherche Scientifique (CNRS)
 * Copyright 2014 AMU-CNRS All Rights Reserved.
 * 
 * These computer program listings and specifications, herein, are
 * the property of Universit� d�Aix Marseille and CNRS
 * shall not be reproduced or copied or used in whole or in part as
 * the basis for manufacture or sale of items without written permission.
 * For a license agreement, please contact:
 * <mailto: licensing@sattse.com> 
 * 
 * Author : Frank BULOUP
 * Institut des Sciences du Mouvement - franck.buloup@univ-amu.fr
 ******************************************************************************/
package analyse.gui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.IAxis;
import org.swtchart.IAxis.Position;

import analyse.Log;
import analyse.gui.ChartEditor;
import analyse.model.Chart;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

@SuppressWarnings("deprecation")
public class AxisDialog extends TitleAreaDialog {

	private ChartEditor chartEditor;
	private ListViewer yAxisIDsListViewer;
	private ListViewer xAxisIDsListViewer;

	public AxisDialog(Shell parentShell, ChartEditor chartEditor) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.chartEditor = chartEditor;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("AxisDialog.ShellTitle"));
		newShell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);		
		setTitle(Messages.getString("AxisDialog.Title"));
		setMessage(Messages.getString("AxisDialog.Text") + ((Chart)chartEditor.getChart()).getLocalPath());
		setTitleImage(ImagesUtils.getImage(IImagesKeys.AXIS_BANNER));
		return contents;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea =  (Composite) super.createDialogArea(parent);
		
		Composite axisGroupsContainer = new Composite(dialogArea, SWT.NONE);
		axisGroupsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		axisGroupsContainer.setLayout(new GridLayout(2, true));
		
		/* Y axis */
		Group yAxisGroup = new Group(axisGroupsContainer, SWT.NONE);
		yAxisGroup.setText(Messages.getString("AxisDialog.YAxisIDsGroupTitle"));
		yAxisGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		yAxisGroup.setLayout(new GridLayout(2, false));
		yAxisIDsListViewer = new ListViewer(yAxisGroup);
		yAxisIDsListViewer.setSorter(new ViewerSorter());
		yAxisIDsListViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite yAxisButtonsContainer = new Composite(yAxisGroup, SWT.NONE);
		yAxisButtonsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		yAxisButtonsContainer.setLayout(new GridLayout(1, true));
		
		Button removeYAxisButton = new Button(yAxisButtonsContainer, SWT.PUSH);
		removeYAxisButton.setText(Messages.getString("AxisDialog.RemoveButtonTitle"));
		removeYAxisButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		removeYAxisButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeYAxis();
			}
		});
		
		Button addYAxisPrimaryButton = new Button(yAxisButtonsContainer, SWT.PUSH);
		addYAxisPrimaryButton.setText(Messages.getString("AxisDialog.AddPrimaryButtonTitle"));
		addYAxisPrimaryButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		addYAxisPrimaryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addYAxis(Position.Primary);
			}
		});
		
		Button addYAxisSecondaryButton = new Button(yAxisButtonsContainer, SWT.PUSH);
		addYAxisSecondaryButton.setText(Messages.getString("AxisDialog.AddSecondaryButtonTitle"));
		addYAxisSecondaryButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		addYAxisSecondaryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addYAxis(Position.Secondary);
			}
		});
		
		/* X axis */
		Group xAxisGroup = new Group(axisGroupsContainer, SWT.NONE);
		xAxisGroup.setText(Messages.getString("AxisDialog.XAxisIDsGroupTitle"));
		xAxisGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		xAxisGroup.setLayout(new GridLayout(2, false));
		
		xAxisIDsListViewer = new ListViewer(xAxisGroup);
		xAxisIDsListViewer.setSorter(new ViewerSorter());
		xAxisIDsListViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite xAxisButtonsContainer = new Composite(xAxisGroup, SWT.NONE);
		xAxisButtonsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		xAxisButtonsContainer.setLayout(new GridLayout(1, true));
		
		Button removeXAxisButton = new Button(xAxisButtonsContainer, SWT.PUSH);
		removeXAxisButton.setText(Messages.getString("AxisDialog.RemoveButtonTitle"));
		removeXAxisButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		removeXAxisButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeXAxis();
			}
		});
		
		Button addXAxisPrimaryButton = new Button(xAxisButtonsContainer, SWT.PUSH);
		addXAxisPrimaryButton.setText(Messages.getString("AxisDialog.AddPrimaryButtonTitle"));
		addXAxisPrimaryButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		addXAxisPrimaryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addXAxis(Position.Primary);
			}
		});
		
		Button addXAxisSecondaryButton = new Button(xAxisButtonsContainer, SWT.PUSH);
		addXAxisSecondaryButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		addXAxisSecondaryButton.setText(Messages.getString("AxisDialog.AddSecondaryButtonTitle"));
		addXAxisSecondaryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addXAxis(Position.Secondary);
			}
		});
		
		updateXAxisIDsListViewer();
		updateYAxisIDsListViewer();
		
		return dialogArea;
	}
	
	private void updateXAxisIDsListViewer() {
		xAxisIDsListViewer.getList().removeAll();
		IAxis[] xAxis = chartEditor.getSWTChart().getAxisSet().getXAxes();
		for (int i = 0; i < xAxis.length; i++) {
			xAxisIDsListViewer.add(String.valueOf(xAxis[i].getId()) + " (" + xAxis[i].getTitle().getText() + ")");
		}
	}
	
	private void updateYAxisIDsListViewer() {
		yAxisIDsListViewer.getList().removeAll();
		IAxis[] yAxis = chartEditor.getSWTChart().getAxisSet().getYAxes();
		for (int i = 0; i < yAxis.length; i++) {
			yAxisIDsListViewer.add(String.valueOf(yAxis[i].getId()) + " (" + yAxis[i].getTitle().getText() + ")");
		}
	}
	
	protected void removeYAxis() {
		Object[] selection = ((IStructuredSelection)yAxisIDsListViewer.getSelection()).toArray();
		for (int i = 0; i < selection.length; i++) {
			String idString = (String) selection[i];
			idString = idString.replaceAll("\\s*\\((.)*\\)$", "");
			int id = Integer.parseInt(idString);
			if(id>0) chartEditor.getSWTChart().getAxisSet().deleteYAxis(id);
			else Log.logMessage(Messages.getString("AxisDialog.DeleteMessage"));
		}
		updateYAxisIDsListViewer();
	}

	protected void removeXAxis() {
		Object[] selection = ((IStructuredSelection)xAxisIDsListViewer.getSelection()).toArray();
		for (int i = 0; i < selection.length; i++) {
			String idString = (String) selection[i];
			idString = idString.replaceAll("\\s*\\((.)*\\)$", "");
			int id = Integer.parseInt(idString);
			if(id>0) chartEditor.getSWTChart().getAxisSet().deleteXAxis(id);
			else Log.logMessage(Messages.getString("AxisDialog.DeleteMessage"));
		}
		updateXAxisIDsListViewer();
	}

	protected void addXAxis(Position position) {
		int axisID = chartEditor.getSWTChart().getAxisSet().createXAxis();
		IAxis axis = chartEditor.getSWTChart().getAxisSet().getXAxis(axisID);
		axis.getTick().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		axis.getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		axis.getTitle().setVisible(false);
		axis.setPosition(position);
		updateXAxisIDsListViewer();
	}

	protected void addYAxis(Position position) {
		int axisID = chartEditor.getSWTChart().getAxisSet().createYAxis();
		IAxis axis = chartEditor.getSWTChart().getAxisSet().getYAxis(axisID);
		axis.getTick().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		axis.getTitle().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		axis.getTitle().setVisible(false);
		axis.setPosition(position);
		updateYAxisIDsListViewer();
	}
	
	
}
