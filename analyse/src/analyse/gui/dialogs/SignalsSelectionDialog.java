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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import analyse.gui.multicharts.ChartElement;
import analyse.model.ChartsTypes;
import analyse.model.Signal;
import analyse.model.Subject;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

@SuppressWarnings("deprecation")
public class SignalsSelectionDialog extends TitleAreaDialog {

	private HashSet<Signal> selectedSignalsList = new HashSet<Signal>(0);
	private CheckboxTableViewer signalsTableViewer;
	private String[] signalsNames;
	private ChartElement chartElement;
	
//	private class SignalsFilter extends ViewerFilter {
//		private String names;
//		public SignalsFilter(String names) {
//			this.names = names;
//		}
//		@Override
//		public boolean select(Viewer viewer, Object parentElement,Object element) {			
//			if (names.equals("")) return true; //$NON-NLS-1$
//			return ((Subject)element).getLocalPath().contains(names);
//		}
//	}
	
	public SignalsSelectionDialog(Shell parentShell, Subject[] loadedSubjects, ChartElement chartElement) {
		super(parentShell);
		setShellStyle(SWT.MAX | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		ArrayList<String> signalsNamesList = new ArrayList<String>(0);
		for (int i = 0; i < loadedSubjects.length; i++) {
			Collections.addAll(signalsNamesList, loadedSubjects[i].getFullSignalsNames());
		}
		signalsNames = signalsNamesList.toArray(new String[signalsNamesList.size()]);
		this.chartElement = chartElement;
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);	
		
		String label2 = Messages.getString("SignalsSelectionDialog.Text"); //$NON-NLS-1$
		String label1 = Messages.getString("SignalsSelectionDialog.Title"); //$NON-NLS-1$
		
		parent.getShell().setText(label1);
		
		getShell().setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		
		setTitle(label2);
		setMessage(Messages.getString("SignalsSelectionDialog.Message"));		 //$NON-NLS-1$
			
		int monitorNumber = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.MONITOR_NUMBER);		
		Monitor monitor = parent.getDisplay().getMonitors()[monitorNumber];
		
		Rectangle bounds = monitor.getBounds ();
		Rectangle rect = parent.getShell().getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		parent.getShell().setLocation (x, y);
						
		return contents;
		
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Control dialogArea =  super.createDialogArea(parent);	
		
		if(chartElement.getDataChart().getChartType().equals(ChartsTypes.TIME_CHART_ID_STRING)) {
			createDialogAreaForTimeChart(dialogArea);
		}
		
		if(chartElement.getDataChart().getChartType().equals(ChartsTypes.XY_CHART_ID_STRING)) {
			createDialogAreaForXYChart(dialogArea);
			
		}
		
		if(chartElement.getDataChart().getChartType().equals(ChartsTypes.XYZ_CHART_ID_STRING)) {
			createDialogAreaForXYZChart(dialogArea);
			
		}
		
		Table signalsTable = new Table((Composite) dialogArea,SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.CHECK);
		signalsTableViewer = new CheckboxTableViewer(signalsTable);
		signalsTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		signalsTableViewer.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(Object inputElement) {	
				return (Object[]) inputElement;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		signalsTableViewer.setLabelProvider(new ILabelProvider(){
			public Image getImage(Object element) {
				return null;
			}
			public String getText(Object element) {
				return (String) element;
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public void dispose() {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener listener) {
			}
		});		
		signalsTableViewer.setSorter(new ViewerSorter());
		signalsTableViewer.setInput(signalsNames);
		
		return dialogArea;
	}
	
	private void createDialogAreaForXYZChart(Control dialogArea) {
		// TODO Auto-generated method stub
		
	}

	private void createDialogAreaForXYChart(Control dialogArea) {
		// TODO Auto-generated method stub
		
	}

	private void createDialogAreaForTimeChart(Control dialogArea) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void okPressed() {
		Object[] signals = signalsTableViewer.getCheckedElements();
		for (int i = 0; i < signals.length; i++) selectedSignalsList.add((Signal)signals[i]);
		super.okPressed();
	}
	
	public Signal[] getSelectedSignals() {
		return selectedSignalsList.toArray(new Signal[selectedSignalsList.size()]);
	}

}
