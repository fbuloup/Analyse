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
package analyse.gui.dialogs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import analyse.model.Chart;
import analyse.model.DataChart;
import analyse.model.DataProcessing;
import analyse.model.Experiments;
import analyse.model.Function;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Processing;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class RefactorDialog extends TitleAreaDialog {

	private IResource[] resources;
	private String replace = "";
	private String by = "";
	private TableViewer byTableViewer;
	private TableViewer replaceTableViewer;

	public RefactorDialog(Shell parentShell, IResource[] resources) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.resources = resources;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("RefactorDialog.ShellTitle"));
		newShell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);		
		setTitle(Messages.getString("RefactorDialog.Title"));
		if(resources.length == 1) setMessage(Messages.getString("RefactorDialog.Text") + resources[0].getLocalPath());
		else setMessage(Messages.getString("RefactorDialog.Text"));
		setTitleImage(ImagesUtils.getImage(IImagesKeys.REFACTOR_BANNER));
		return contents;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea =  (Composite) super.createDialogArea(parent);

		Composite container = new Composite(dialogArea, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		container.setLayout(new GridLayout(1,false));
		
		Composite replaceByComposite = new Composite(container, SWT.NONE);
		replaceByComposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		replaceByComposite.setLayout(new GridLayout(4,false));
		GridLayout gridLayout2 = (GridLayout) replaceByComposite.getLayout();
		gridLayout2.marginHeight = 0;
		gridLayout2.marginWidth = 0;
		
		CLabel replaceLabel = new CLabel(replaceByComposite, SWT.NONE);
		replaceLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		replaceLabel.setText(Messages.getString("RefactorDialog.replaceNameLabelTitle"));
		final Text replaceText = new Text(replaceByComposite, SWT.BORDER);
		replaceText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		CLabel byLabel = new CLabel(replaceByComposite, SWT.NONE);
		byLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER, false, false));
		byLabel.setText(Messages.getString("RefactorDialog.replaceByLabelTitle"));
		final Text byText = new Text(replaceByComposite, SWT.BORDER);
		byText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		Composite previewComposite = new Composite(container, SWT.NONE);
		previewComposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		previewComposite.setLayout(new GridLayout(2,true));
		GridLayout gridLayout3 = (GridLayout) replaceByComposite.getLayout();
		gridLayout3.marginHeight = 0;
		gridLayout3.marginWidth = 0;
		
		HashSet<String> inputSet = new HashSet<String>(0);		
		String[] input = null;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if(resource instanceof Chart) {
				Chart chart = (Chart)resource;
				inputSet.addAll(Arrays.asList(chart.getData().getSignals()));
			}
			if(resource instanceof Processing) {
				Processing processing = (Processing)resource;
				Vector<Function> functions = processing.getDataProcessing().getFunctionsList();
				HashSet<String> channels = new HashSet<String>(0);
				for (int j = 0; j < functions.size(); j++) {
					Function function = functions.get(j);
					int nbUplets = function.getSignalsNbUplets();
					for (int k = 0; k <nbUplets; k++) {
						String signalsNames = function.getSignalsNamesList(k);
						channels.addAll(Arrays.asList(signalsNames.split(",")));
					}
					nbUplets = function.getMarkersNbUplets();
					for (int k = 0; k <nbUplets; k++) {
						String markersNames = function.getMarkersNamesList(k);
						channels.addAll(Arrays.asList(markersNames.split(",")));
					}
					nbUplets = function.getFieldsNbUplets();
					for (int k = 0; k <nbUplets; k++) {
						String fieldsNames = function.getFieldsNamesList(k);
						channels.addAll(Arrays.asList(fieldsNames.split(",")));
					}
					if(function.getSignalsCreatedNumber() > 0 && !function.getNewSignalsNamesSuffix().equals("")) channels.add(function.getNewSignalsNamesSuffix());
					String[] newFieldsNames = function.getNewFieldsNamesList().split(":");
					for (int k = 0; k < newFieldsNames.length; k++) {
						if(!newFieldsNames[k].equals("")) channels.addAll(Arrays.asList(newFieldsNames[k].split(",")));
					}
					String[] newMarkersNames = function.getNewMarkersGroupLabels().split(":");
					for (int k = 0; k < newMarkersNames.length; k++) {
						if(!newMarkersNames[k].equals("")) channels.addAll(Arrays.asList(newMarkersNames[k].split(",")));
					}
				}
				inputSet.addAll(channels);
			}
		}
		input = inputSet.toArray(new String[inputSet.size()]);
		Arrays.sort(input);
		
		replaceTableViewer = new TableViewer(previewComposite);
		replaceTableViewer.getTable().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		replaceTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}
		});
		replaceTableViewer.setLabelProvider(new ILabelProvider() {
			public void removeListener(ILabelProviderListener listener) {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void dispose() {
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public String getText(Object element) {
				return (String) element;
			}
			public Image getImage(Object element) {
				String text = (String)element;
				boolean modified = (Boolean) byTableViewer.getData(text);
				if(modified) return ImagesUtils.getImage(IImagesKeys.CHANGES_ICON);
				return null;
			}
		});
		TableColumn replaceTableColumn = new TableColumn(replaceTableViewer.getTable(), SWT.NONE);
		
		byTableViewer = new TableViewer(previewComposite);
		byTableViewer.getTable().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		byTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}
		});
		byTableViewer.setLabelProvider(new ILabelProvider() {
			public void removeListener(ILabelProviderListener listener) {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void dispose() {
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public String getText(Object element) {
				String text = (String)element;
				byTableViewer.setData(text, false);
				if(!replace.equals("")) {
					text = text.replaceAll(replace, by);
					byTableViewer.setData((String)element, !text.equals((String)element));
				}
				return text;
			}
			public Image getImage(Object element) {
				String text = (String)element;
				boolean modified = (Boolean) byTableViewer.getData(text);
				if(modified) return ImagesUtils.getImage(IImagesKeys.CHANGES_ICON);
				return null;
			}
		});
		TableColumn byTableColumn = new TableColumn(byTableViewer.getTable(), SWT.NONE);
		byTableViewer.setInput(input);
		byTableColumn.pack();
		
		replaceTableViewer.setInput(input);
		replaceTableColumn.pack();
		
		replaceText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				replace = replaceText.getText();
				byTableViewer.refresh();
				replaceTableViewer.refresh();
				byTableViewer.getTable().getColumn(0).setWidth(byTableViewer.getTable().getClientArea().width);
				replaceTableViewer.getTable().getColumn(0).setWidth(replaceTableViewer.getTable().getClientArea().width);
			}
		});
		
		byText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				by = byText.getText();
				byTableViewer.refresh();
				replaceTableViewer.refresh();
				byTableViewer.getTable().getColumn(0).setWidth(byTableViewer.getTable().getClientArea().width);
				replaceTableViewer.getTable().getColumn(0).setWidth(replaceTableViewer.getTable().getClientArea().width);
			}
		});
		
		return dialogArea;
	}
	
	@Override
	protected void okPressed() {
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if(resource instanceof Chart) {
				Chart chart = (Chart)resource;
				DataChart dataChart = chart.getData();
				TableItem[] tableItems = replaceTableViewer.getTable().getItems();
				for (int j = 0; j < tableItems.length; j++) {
					if(!tableItems[j].getText().equals(byTableViewer.getTable().getItem(j).getText())) {
						if(dataChart.hasSignal(tableItems[j].getText())) {
							dataChart.removeSignal(tableItems[j].getText());
							dataChart.addSignal(byTableViewer.getTable().getItem(j).getText());
						}
					}
				}
				chart.saveChart();
				Experiments.notifyObservers(IResourceObserver.REFACTORED, new IResource[]{resource});
			}
			if(resource instanceof Processing) {
				Processing processing = (Processing)resource;
				DataProcessing dataProcessing = processing.getDataProcessing();
				TableItem[] tableItems = replaceTableViewer.getTable().getItems();
				for (int j = 0; j < tableItems.length; j++) {
					if(!tableItems[j].getText().equals(byTableViewer.getTable().getItem(j).getText())) {
						dataProcessing.replace(tableItems[j].getText(),byTableViewer.getTable().getItem(j).getText());
					}
				}
				processing.saveProcessing();
				Experiments.notifyObservers(IResourceObserver.REFACTORED, new IResource[]{resource});
			}
		}
		super.okPressed();
	}

}
