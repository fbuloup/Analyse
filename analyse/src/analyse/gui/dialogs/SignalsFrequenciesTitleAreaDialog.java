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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.Combo;

import analyse.Log;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.Subject;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;


@SuppressWarnings("deprecation")
public class SignalsFrequenciesTitleAreaDialog extends TitleAreaDialog {
	
	private class SignalsNamesSorter extends ViewerSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			LineTableSignalFrequency ltsf1 = (LineTableSignalFrequency)e1;
			LineTableSignalFrequency ltsf2 = (LineTableSignalFrequency)e2;
			return super.compare(viewer, ltsf1.getSignalName(), ltsf2.getSignalName());
		}
	}
	
	private class LineTableSignalFrequency {
		private String signalName;
		private String sampleFrequency;
		public LineTableSignalFrequency(String signalName, String sampleFrequency) {
			this.sampleFrequency = sampleFrequency;
			this.signalName = signalName;
		}
		public String getSignalName() {
			return signalName;
		}
		public String getSampleFrequency() {
			return sampleFrequency;
		}
		public void setSampleFrequency(String value) {
			sampleFrequency = value;
		}
		
	}
	
	private TableViewer tableViewer;
	
	private ArrayList<LineTableSignalFrequency> lineTableSignalFrequencies = new ArrayList<LineTableSignalFrequency>(0);
	private ArrayList<LineTableSignalFrequency> lineTableSignalFrequenciesFromDataFiles = new ArrayList<LineTableSignalFrequency>(0);
	private Subject subject;
	
	private HashMap<String,String> sampleFrequencies = new HashMap<String, String>(0);
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param subjectFiles 
	 * @param subject 
	 * @param style
	 */
	public SignalsFrequenciesTitleAreaDialog(Shell parent, String[] subjectFiles, Subject subject) {
		super(parent);
		setHelpAvailable(false);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		HashSet<String> filesList = new HashSet<String>(0);
		for (int i = 0; i < subjectFiles.length; i++) {
			String[] temp = subjectFiles[i].split(Pattern.quote(File.separator));
			temp = temp[temp.length - 1].split("_");
			filesList.add(temp[0]);
		}
		
		for (Iterator<String> iterator = filesList.iterator(); iterator.hasNext();) {
			String signalName = iterator.next();
			LineTableSignalFrequency ltsf = new LineTableSignalFrequency(signalName, "1000");
			lineTableSignalFrequenciesFromDataFiles.add(ltsf);
		}
		this.subject = subject;
	}
	
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("SignalFrequenciesTitleAreaDialog.ShellTitle"));
		newShell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
	}
	
	/**
	 * Create contents of the dialog.
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);	
		setTitle(Messages.getString("SignalFrequenciesTitleAreaDialog.DialogTitle") + subject.getLocalPath());
		setMessage(Messages.getString("SignalFrequenciesTitleAreaDialog.Info"));
		setTitleImage(ImagesUtils.getImage(IImagesKeys.SIGNAL_FREQUENCY_BANNER));
		return contents;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea =  (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(dialogArea, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNomDuCanal = tableViewerColumn.getColumn();
		tblclmnNomDuCanal.setWidth(172);
		tblclmnNomDuCanal.setText(Messages.getString("SignalFrequenciesTitleAreaDialog.ChannelName"));
		
		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnFrquenceDuCanal = tableViewerColumn_1.getColumn();
		tblclmnFrquenceDuCanal.setWidth(237);
		tblclmnFrquenceDuCanal.setText(Messages.getString("SignalFrequenciesTitleAreaDialog.SignalFrequencies"));
		
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
			}
			public void dispose() {
			}
			@SuppressWarnings("unchecked")
			public Object[] getElements(Object input) {
				ArrayList<LineTableSignalFrequency> ltsf = (ArrayList<LineTableSignalFrequency>)input;
				return ltsf.toArray(new LineTableSignalFrequency[ltsf.size()]);
			}
		});
		tableViewer.setLabelProvider(new ITableLabelProvider() {
			public void removeListener(ILabelProviderListener arg0) {
			}
			public boolean isLabelProperty(Object arg0, String arg1) {
				return false;
			}
			public void dispose() {
			}
			public void addListener(ILabelProviderListener arg0) {
			}
			public String getColumnText(Object element, int index) {
				if(index == 0) return ((LineTableSignalFrequency)element).getSignalName();
				return ((LineTableSignalFrequency)element).getSampleFrequency();
			}
			public Image getColumnImage(Object arg0, int arg1) {
				return null;
			}
		});
		tableViewer.setCellModifier(new ICellModifier() {
			public void modify(Object element, String property, Object value) {
				TableItem item = (TableItem) element;
				((LineTableSignalFrequency) item.getData()).setSampleFrequency(value.toString());
				tableViewer.refresh(((LineTableSignalFrequency) item.getData()));
			}
			public Object getValue(Object element, String property) {
				return ((LineTableSignalFrequency)element).getSampleFrequency();
			}
			public boolean canModify(Object element, String property) {
				return property.equals("frequency");
			}
		});
		tableViewer.setSorter(new SignalsNamesSorter());
		
		final TextCellEditor textCellEditor = new TextCellEditor(table);
		textCellEditor.setValue("1000");
		((Text)textCellEditor.getControl()).addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				String textEdit = ((Text) event.widget).getText();
				TableItem[] frequenciesItems = tableViewer.getTable().getItems();
				boolean enabled = true;
				for (int i = 0; i < frequenciesItems.length; i++) {
					if(i != tableViewer.getTable().getSelectionIndex()) {
						String text = frequenciesItems[i].getText(1);
						enabled = enabled && !text.equals("");
					}
				}
				SignalsFrequenciesTitleAreaDialog.this.getButton(TitleAreaDialog.OK).setEnabled(enabled && !textEdit.equals(""));
				SignalsFrequenciesTitleAreaDialog.this.setMessage(Messages.getString("SignalFrequenciesTitleAreaDialog.Info"));
				if(!SignalsFrequenciesTitleAreaDialog.this.getButton(TitleAreaDialog.OK).isEnabled())
					SignalsFrequenciesTitleAreaDialog.this.setMessage(Messages.getString("SignalFrequenciesTitleAreaDialog.ErrorMessage"), IMessageProvider.ERROR);
			}
		});
		((Text)textCellEditor.getControl()).addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent event) {
				event.doit = event.text.matches("\\d");
				String text = ((Text) textCellEditor.getControl()).getText();
				if(event.text.equals(".")) event.doit = !text.contains(".");
				if(event.character == SWT.DEL)	event.doit = true;
				if(event.character == SWT.BS)	event.doit = true;
				SignalsFrequenciesTitleAreaDialog.this.getButton(TitleAreaDialog.OK).setEnabled(!text.equals(""));
				SignalsFrequenciesTitleAreaDialog.this.setMessage(Messages.getString("SignalFrequenciesTitleAreaDialog.Info"));
				if(!SignalsFrequenciesTitleAreaDialog.this.getButton(TitleAreaDialog.OK).isEnabled())
						SignalsFrequenciesTitleAreaDialog.this.setMessage(Messages.getString("SignalFrequenciesTitleAreaDialog.ErrorMessage"), IMessageProvider.ERROR);
				
			}
		});
		
		tableViewer.setCellEditors(new CellEditor[]{null, textCellEditor});
		tableViewer.setColumnProperties(new String[]{"signalName", "frequency"});
		tableViewer.setInput(lineTableSignalFrequenciesFromDataFiles);
		
		final Button useOtherFrequenciesFileButton = new Button(container, SWT.CHECK);
		useOtherFrequenciesFileButton.setEnabled(false);
		useOtherFrequenciesFileButton.setText(Messages.getString("SignalFrequenciesTitleAreaDialog.CheckboxLabel"));
		
		final Combo subjectSelectionCombo = new Combo(container, SWT.READ_ONLY);
		subjectSelectionCombo.setEnabled(false);
		subjectSelectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		subjectSelectionCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				buildLineTableSignalFrequencies(subjectSelectionCombo.getText());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Subject[] subjects = ((Experiment)subject.getParent()).getSubjects();
		for (int i = 0; i < subjects.length; i++) {
			Subject locaSubject = subjects[i];
			String[] dataFiles = locaSubject.getDataFiles(false);
			for (int j = 0; j < dataFiles.length; j++)
				if (dataFiles[j].endsWith("SampleFrequencies.properties")) {
					subjectSelectionCombo.add(locaSubject.getLocalPath());
					useOtherFrequenciesFileButton.setEnabled(true);
				}
		}
		
		useOtherFrequenciesFileButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				subjectSelectionCombo.setEnabled(useOtherFrequenciesFileButton.getSelection());
				tableViewer.getTable().setEnabled(!useOtherFrequenciesFileButton.getSelection());
				if(!useOtherFrequenciesFileButton.getSelection()) tableViewer.setInput(lineTableSignalFrequenciesFromDataFiles);
				else if(!subjectSelectionCombo.getText().equals("")) buildLineTableSignalFrequencies(subjectSelectionCombo.getText());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		return dialogArea;
	}
	
	
	private void buildLineTableSignalFrequencies(String subjectLocalPath) {
		String experimentName = subjectLocalPath.split("\\.")[0];
		Experiment experiment = Experiments.getInstance().getExperimentByName(experimentName);
		Subject localSubject = experiment.getSubjectByName(subjectLocalPath);
		try {
			String sfFileAbsolutePath = localSubject.getAbsolutePath() + File.separator + "SampleFrequencies.properties";
			Properties properties = new Properties();
			FileInputStream fis = new FileInputStream(sfFileAbsolutePath);
			properties.load(fis);
			fis.close();
			Set<Object> keys = properties.keySet();
			lineTableSignalFrequencies.clear();
			for (Iterator<Object> iterator = keys.iterator(); iterator.hasNext();) {
				String signalName = (String) iterator.next();
				String sampleFrequency = (String) properties.get(signalName);
				LineTableSignalFrequency lineTableSignalFrequency = new LineTableSignalFrequency(signalName, sampleFrequency);
				lineTableSignalFrequencies.add(lineTableSignalFrequency);
			}
			tableViewer.setInput(lineTableSignalFrequencies);
		} catch (FileNotFoundException e) {
			Log.logErrorMessage(e);
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}
	}
	
	@Override
	protected void okPressed() {
		TableItem[] items = tableViewer.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			sampleFrequencies.put(items[i].getText(0), items[i].getText(1));
		}
		super.okPressed();
	}
	
	public HashMap<String, String> getSamplesFrequencies() {
		return sampleFrequencies;
	}
	
}
