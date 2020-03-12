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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import analyse.Log;
import analyse.model.Experiment;
import analyse.preferences.DataFilesPreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

@SuppressWarnings("deprecation")
public class MultipleSubjectsImportDialog extends TitleAreaDialog {

	private TableViewer subjectsTableViewer;
	private Text rootFolderText;
	private Button filesMethodButton;
	private String[] extensions;
	private Experiment experiment;
	private boolean previousFilesMethodButtonState = true;
	private ArrayList<String> subjectsList;
	private ArrayList<String[]> dataFileslist;
	private boolean createNewExperiment = false;
	private String[] subjectsListPlanedToImport;
	private Rectangle bounds;
	private String experimentType;
	
	private boolean hasCorrectExtension(String fileName){
		boolean extensionAllowed = false;
		
		for (int i = 0; i < extensions.length; i++) {
			String ext2 = extensions[i].replaceAll("\\*", "");
			extensionAllowed = extensionAllowed | fileName.endsWith(ext2);
		}	
		return extensionAllowed;
	}
	
	private boolean hasCorrectName(String fileName, boolean fileImportMethod){
		boolean nameAllowed = fileName.matches("\\w+");
//		nameAllowed = nameAllowed && (fileName.length() < 31);
		nameAllowed = nameAllowed && (!fileName.matches("^[0-9]+\\w*"));
		TableItem[] items = subjectsTableViewer.getTable().getItems();
		for (int i = 0; i < items.length; i++) nameAllowed = nameAllowed && (!items[i].getText().equalsIgnoreCase(fileName));
		for (int i = 0; i < subjectsListPlanedToImport.length; i++) nameAllowed = nameAllowed && (!subjectsListPlanedToImport[i].equalsIgnoreCase(fileName));
		if(!createNewExperiment) {
			nameAllowed = nameAllowed && (!experiment.isResourceNameExist(fileName));
			if(experiment.isResourceNameExist(fileName) && DataFilesPreferences.isExtensionAllowed(experimentType, fileName, fileImportMethod)) Log.logMessage(Messages.getString("MultipleSubjectsImportDialog.Subject") + fileName + Messages.getString("MultipleSubjectsImportDialog.AlreadyExists"));
		}
		return nameAllowed;
	}
	
	private class FileExtensionFilter implements FileFilter {
		public boolean accept(File file2) {	
			String fileName = file2.getName();
			return file2.isFile() && hasCorrectExtension(fileName);
		}
	}
	
	private class FoldersMethodFilter implements FileFilter {
		public boolean accept(File file) {
			String fileName = file.getName();
			File[] dataFiles = file.listFiles(new FileExtensionFilter());
			return file.isDirectory() && dataFiles.length > 0 && hasCorrectName(fileName, false);
		}
	}
	
	private class FilesMethodFilter implements FileFilter {
		public boolean accept(File file) {
			String fileName = file.getName();
			boolean extensionAllowed = hasCorrectExtension(fileName);
			fileName = fileName.replaceAll("\\.\\w*$", "");
			return file.isFile() && extensionAllowed && hasCorrectName(fileName, true);
		}
	}

	public MultipleSubjectsImportDialog(Shell parentShell, boolean createNewExperiment, String[] subjectsListPlanedToImport, Rectangle bounds, String experimentType, Experiment experiment) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.createNewExperiment = createNewExperiment;
		this.subjectsListPlanedToImport = subjectsListPlanedToImport;
		extensions = DataFilesPreferences.getExtensionForSystem(experimentType);
		this.experimentType = experimentType;
		this.bounds = bounds;
		this.experiment = experiment;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("MultipleSubjectsImportDialog.ShellTitle"));
		newShell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		newShell.setBounds(bounds);
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);		
		setTitle(Messages.getString("MultipleSubjectsImportDialog.Title"));
		setMessage(Messages.getString("MultipleSubjectsImportDialog.Text"));
		setTitleImage(ImagesUtils.getImage(IImagesKeys.IMPORT_SUBJECT_BANNER));
		return contents;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea =  (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(dialogArea, SWT.NONE);			
		container.setLayout(new GridLayout(3,false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite buttonsContainer = new Composite(container, SWT.NONE);			
		buttonsContainer.setLayout(new GridLayout(2,true));
		buttonsContainer.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,3,1));
		
		filesMethodButton = new Button(buttonsContainer,SWT.RADIO);
		filesMethodButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		filesMethodButton.setText(Messages.getString("MultipleSubjectsImportDialog.FilesButtonTitle"));
		filesMethodButton.setSelection(true);
		filesMethodButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				if(filesMethodButton.getSelection() != previousFilesMethodButtonState) {
					populateSubjectTableHandler();
					previousFilesMethodButtonState=!previousFilesMethodButtonState;
				}
			}
		});
		filesMethodButton.setEnabled(DataFilesPreferences.isFileImportMethod(experimentType));
		
		Button foldersMethodButton = new Button(buttonsContainer,SWT.RADIO);
		foldersMethodButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		foldersMethodButton.setText(Messages.getString("MultipleSubjectsImportDialog.FoldersButtonTitle"));
		foldersMethodButton.setEnabled(DataFilesPreferences.isFolderImportMethod(experimentType));
		
		if(!DataFilesPreferences.isFileImportMethod(experimentType)) {
			filesMethodButton.setSelection(false);
			foldersMethodButton.setSelection(true);
		}
		
		
		Label rootFolderLabel = new Label(container,SWT.NONE);
		rootFolderLabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
		rootFolderLabel.setText(Messages.getString("MultipleSubjectsImportDialog.RootDirectoryLabelTitle"));//$NON-NLS-1$
		rootFolderText = new Text(container,SWT.BORDER | SWT.READ_ONLY);
		rootFolderText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		rootFolderText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				populateSubjectTableHandler();
			}
		});
		
		Button browseButton = new Button(container,SWT.PUSH);
		browseButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		browseButton.setText(Messages.getString("MultipleSubjectsImportDialog.BrowseButtonTitle"));
		browseButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(getParentShell());
				String directory = directoryDialog.open();
				rootFolderText.setText((directory != null)?directory:"");
			}
		});
		
		Composite tableComposite = new Composite(container, SWT.NONE);	
		tableComposite.setLayout(new GridLayout(1,false));
		tableComposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,3,1));
		
		subjectsTableViewer = new TableViewer(tableComposite,SWT.CHECK | SWT.BORDER);
		subjectsTableViewer.getTable().setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,true));
		subjectsTableViewer.setSorter(new ViewerSorter());
		subjectsTableViewer.setLabelProvider(new ILabelProvider(){
			public Image getImage(Object element) {
				return null;
			}
			public String getText(Object element) {
				try {
					String subjectName = ((File)element).getName();
					subjectName = subjectName.replaceAll("\\.\\w*$", "");
					if(filesMethodButton.getSelection()) {
						return subjectName + " - " + ((File)element).getCanonicalPath();
					} else {
						File[] dataFiles = ((File)element).listFiles(new FileExtensionFilter());
						File[] allowedDataFiles = DataFilesPreferences.getAllowedDataFiles(dataFiles, experimentType, true);
						return subjectName + " - " + DataFilesPreferences.getLabelFromDataFiles(allowedDataFiles);
					}
				} catch (IOException e) {
					Log.logErrorMessage(e);
				}
				return "ERROR";
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
		subjectsTableViewer.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(Object inputElement) {
				File[] subjectsFiles = (File[]) inputElement;
				if(filesMethodButton.getSelection()) {
					return	DataFilesPreferences.getAllowedDataFiles(subjectsFiles, experimentType, false);
				}
				
				return (Object[]) inputElement;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		
		Button inverseSelectionButton = new Button(tableComposite,SWT.PUSH);
		inverseSelectionButton.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,true,false));
		inverseSelectionButton.setText(Messages.getString("MultipleSubjectsImportDialog.InverseSelectionButtonTitle"));
		inverseSelectionButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = subjectsTableViewer.getTable().getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].setChecked(!items[i].getChecked());
				}
			}
		});
		
		return dialogArea;
	}
	
	public String[] getSubjectsList() {
		return subjectsList.toArray(new String[subjectsList.size()]);
	}
	
	public String[] getDataFileForSubject(String subjectName) {
		int index = subjectsList.indexOf(subjectName);
		return dataFileslist.get(index);
	}

	protected void populateSubjectTableHandler() {
		subjectsTableViewer.getTable().removeAll();
		String rootFolder = rootFolderText.getText();
		if(!rootFolder.equals("")) {
			File rootFolderFile = new File(rootFolder);
			FileFilter fileFilter = filesMethodButton.getSelection() ? new FilesMethodFilter(): new FoldersMethodFilter();
			File[] subjectsFilesNames = rootFolderFile.listFiles(fileFilter);
			subjectsTableViewer.setInput(subjectsFilesNames);
		}
		
	}
	
	@Override
	protected void okPressed() {
		subjectsList = new ArrayList<String>(0);
		dataFileslist = new ArrayList<String[]>(0);
		TableItem[] items = subjectsTableViewer.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			if(items[i].getChecked()) {
				File file = (File) subjectsTableViewer.getElementAt(i);
				String subjectName = file.getName().replaceAll("\\.\\w*$", "");
				subjectsList.add(subjectName);
				dataFileslist.add(items[i].getText().replaceAll("^" + subjectName + " - ", "").split(";"));
			}
		}		
		super.okPressed();
	}

}
