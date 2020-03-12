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
package analyse.gui.wizards;

import java.util.Hashtable;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import analyse.gui.dialogs.MultipleSubjectsImportDialog;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.preferences.DataFilesPreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public final class ExperimentPage extends WizardPage {

	public static final String PAGE_NAME = "NEW_PROJECT_PAGE"; 
	private Text experimentNameText;
	private Combo experimentTypeCombo;
	private String defaultMessage = Messages.getString("ExperimentPage.Text"); 
	private boolean error;
	private List subjectList;
	private boolean createNewExperiment;
	private Combo experimentNameCombo;
	private Experiment selectedExperiment;
	
	public ExperimentPage(boolean createNewExperiment, Experiment selectedExperiment) {
		super(PAGE_NAME,Messages.getString("ExperimentPage.Title"), ImagesUtils.getImageDescriptor(IImagesKeys.NEW_EXPERIMENT_BANNER)); 
		setMessage(defaultMessage);
		if(!createNewExperiment) {
			//Change default title and message
			setTitle(Messages.getString("ExperimentPage.NewSubjectTitle"));
			setMessage(Messages.getString("ExperimentPage.NewSubjectMessage"));
			//Change default icon
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.NEW_SUBJECT_BANNER));
		}
		
		this.createNewExperiment = createNewExperiment;
		this.selectedExperiment = selectedExperiment;
	}
	
	public void createControl(Composite parent) {
		Composite container = new Composite(parent,SWT.NONE);
		container.setLayout(new GridLayout(2,false));
		if(createNewExperiment) {
			Label label1 = (new Label(container,SWT.NONE));
			label1.setText(Messages.getString("ExperimentPage.ExperimentNameLabelTitle")); //$NON-NLS-1$
			label1.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER, false, false));
			experimentNameText = (new Text(container,SWT.BORDER));
			experimentNameText.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true, false));
			experimentNameText.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e) {				
					checkForErrors();				
				}			
			});
			
			Label label2 = (new Label(container,SWT.NONE));
			label2.setText(Messages.getString("ExperimentPage.SelectExpTypeLabelTitle")); //$NON-NLS-1$
			label2.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER, false, false));	
			experimentTypeCombo = new Combo(container,SWT.READ_ONLY);		
			experimentTypeCombo.setItems(DataFilesPreferences.getSystemList());
			experimentTypeCombo.setVisibleItemCount(experimentTypeCombo.getItemCount());
			experimentTypeCombo.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true, false));
		} else {
			Label label1 = (new Label(container,SWT.NONE));
			label1.setText(Messages.getString("ExperimentPage.ExperimentNameLabelTitle")); //$NON-NLS-1$
			label1.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER, false, false));
			experimentNameCombo = (new Combo(container,SWT.BORDER | SWT.READ_ONLY));
			experimentNameCombo.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true, false));
			experimentNameCombo.setItems(Experiments.getInstance().getExperimentsNamesList().toArray(new String[Experiments.getInstance().getExperimentsCount()]));
			if(selectedExperiment != null) experimentNameCombo.setText(selectedExperiment.getName());
			experimentNameCombo.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e) {				
					selectedExperiment = Experiments.getInstance().getExperimentByName(experimentNameCombo.getText());
					experimentTypeCombo.setText(selectedExperiment.getType());
				}			
			});
			
			Label label2 = (new Label(container,SWT.NONE));
			label2.setText(Messages.getString("ExperimentPage.SelectExpTypeLabelAddSubjectTitle")); //$NON-NLS-1$
			label2.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER, false, false));	
			experimentTypeCombo = new Combo(container,SWT.READ_ONLY);		
			experimentTypeCombo.setItems(DataFilesPreferences.getSystemList());
			experimentTypeCombo.setVisibleItemCount(experimentTypeCombo.getItemCount());
			experimentTypeCombo.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true, false));
			if(selectedExperiment != null) experimentTypeCombo.setText(selectedExperiment.getType());
			experimentTypeCombo.setEnabled(false);
		}
		
		Label label3 = (new Label(container,SWT.NONE));
		label3.setText(Messages.getString("ExperimentPage.SubjectsListLabelTitle")); //$NON-NLS-1$
		label3.setLayoutData(new GridData(SWT.LEFT,SWT.FILL, true, false, 2, 1));
		
		Composite subjectsContainer = new Composite(container,SWT.NONE);
		subjectsContainer.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
		subjectsContainer.setLayout(new GridLayout(2,false));
		
		subjectList = new List(subjectsContainer,SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		subjectList.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,1,1));
		
		Composite buttonsContainer = new Composite(subjectsContainer,SWT.NONE);
		buttonsContainer.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,true,1,1));
		buttonsContainer.setLayout(new GridLayout(1,false));
		
		final Button addSubjectsButton = new Button(buttonsContainer,SWT.PUSH);
		addSubjectsButton.setText(Messages.getString("SubjectsPage.AddButtonTitle"));
		addSubjectsButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		addSubjectsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Rectangle bounds = ExperimentPage.this.getWizard().getContainer().getShell().getBounds();
				MultipleSubjectsImportDialog multipleSubjectsImportDialog = new MultipleSubjectsImportDialog(null,createNewExperiment,subjectList.getItems(),bounds,experimentTypeCombo.getText(),selectedExperiment);
				if(multipleSubjectsImportDialog.open() == Window.OK) {
					String[] subjects = multipleSubjectsImportDialog.getSubjectsList();
					for (int i = 0; i < subjects.length; i++) {
						subjectList.add(subjects[i]);
						String[] dataFiles = multipleSubjectsImportDialog.getDataFileForSubject(subjects[i]);
						subjectList.setData(subjects[i], dataFiles);
					}
				}
			}
		});
		addSubjectsButton.setEnabled(false);
		if(!createNewExperiment && selectedExperiment != null) addSubjectsButton.setEnabled(true);
		
		experimentTypeCombo.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				checkForErrors();
				addSubjectsButton.setEnabled(true);
			}			
		});

		Button removeSubjectsButton = new Button(buttonsContainer,SWT.PUSH);
		removeSubjectsButton.setText(Messages.getString("SubjectsPage.RemoveButtonTitle"));
		removeSubjectsButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));		
		removeSubjectsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				subjectList.remove(subjectList.getSelectionIndices());
			}
		});
		setControl(container);
		setPageComplete(false);
		if(!createNewExperiment && selectedExperiment != null) setPageComplete(true);

	}

	protected void experimentTypeChecker() {
		String experimentType = experimentTypeCombo.getText();
		if(experimentType.equals("")) {
			error = true;			
			setMessage(Messages.getString("ExperimentPage.ErrorMessage1"), IMessageProvider.ERROR); 
		}
	}

	protected void experimentNameChecker() {
		String experimentName = "";
		if(createNewExperiment) experimentName = experimentNameText.getText();
		else experimentName = experimentNameCombo.getText();
		String errorMessage = "";	 			
		if(experimentName.equals("")) errorMessage = Messages.getString("ExperimentPage.ErrorMessage2"); 
		else if(!experimentName.matches("\\w+")) errorMessage = Messages.getString("ExperimentPage.ErrorMessage3"); 
		else if(experimentName.length() > 31) errorMessage = Messages.getString("ExperimentPage.ErrorMessage4"); 
		else if(experimentName.matches("^[0-9]+\\w*")) errorMessage = Messages.getString("ExperimentPage.ErrorMessage5");		  
		else if(createNewExperiment) if(Experiments.getInstance().getExperimentsNamesList().contains(experimentName)) errorMessage = Messages.getString("ExperimentPage.ErrorMessage6"); 
		if(!errorMessage.equals("")) {
			error = true;			
			setMessage(errorMessage, IMessageProvider.ERROR);
		}
	}
	
	private void checkForErrors(){
		error = false;
		setMessage(null);	
		experimentTypeChecker();
		experimentNameChecker();
		if(!error) {
			setMessage(defaultMessage);
			if(!createNewExperiment) setMessage(Messages.getString("ExperimentPage.NewSubjectMessage"));
			//((SubjectsPage)this.getWizard().getPage(SubjectsPage.PAGE_NAME)).updateExperimentName(experimentNameText.getText());
			setPageComplete(true);
		} else setPageComplete(false);
	}
	
	
	public String getExperimentName(){
		if(createNewExperiment) return experimentNameText.getText();
		return experimentNameCombo.getText();
	}
	
	public String getExperimentType(){
		return experimentTypeCombo.getText();
	}
	
	public Hashtable<String, String[]> getSubjectsAndDataFiles() {
		Hashtable<String, String[]> hashMap = new Hashtable<String, String[]>(0);
		for (int i = 0; i < subjectList.getItemCount(); i++) {
			if(subjectList.getData(subjectList.getItem(i)) != null)
			hashMap.put(subjectList.getItem(i), (String[]) subjectList.getData(subjectList.getItem(i)));
		}
		return hashMap;
	}
}
