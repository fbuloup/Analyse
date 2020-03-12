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

import java.util.HashSet;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.Processing;
import analyse.model.Subject;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

@SuppressWarnings("deprecation")
public final class SubjectsSelectionDialog extends TitleAreaDialog {

	private HashSet<Subject> selectedSubjectsList = new HashSet<Subject>(0);
	private CheckboxTableViewer subjectsTableViewer;
	private IResource[] selectedProcesses;
	private TreeViewer processesTreeViewer;
	
	private class SubjectsFilter extends ViewerFilter {
		private String names;
		public SubjectsFilter(String names) {
			this.names = names;
		}
		@Override
		public boolean select(Viewer viewer, Object parentElement,Object element) {			
			if (names.equals("")) return true; //$NON-NLS-1$
			return ((Subject)element).getLocalPath().contains(names);
		}
	}
	
	public SubjectsSelectionDialog(Shell parentShell, IResource[] selectedProcesses) {
		super(parentShell);
		setShellStyle(SWT.MAX | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		this.selectedProcesses = selectedProcesses;
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);		

		String label2 = Messages.getString("SubjectsSelectionDialog.Text"); //$NON-NLS-1$
		String label1 = Messages.getString("SubjectsSelectionDialog.Title"); //$NON-NLS-1$
		
		parent.getShell().setText(label1);
		
		getShell().setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		
		setTitle(label2);
		setMessage(Messages.getString("SubjectsSelectionDialog.FilterArea"));		 //$NON-NLS-1$
			
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
		Composite dialogArea =  (Composite) super.createDialogArea(parent);
			
		SashForm sashForm = new SashForm(dialogArea, SWT.VERTICAL);			
//		container.setLayout(new GridLayout(1,false));
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite treeContainer = new Composite(sashForm, SWT.BORDER);			
		treeContainer.setLayout(new GridLayout(2,false));
		treeContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final Tree processesTree = new Tree(treeContainer, SWT.BORDER);
		processesTree.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		
		processesTreeViewer = new TreeViewer(processesTree);
		processesTreeViewer.setLabelProvider(new ILabelProvider() {
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
				return ((Processing)element).getLocalPath();
			}
			
			public Image getImage(Object element) {
				return ImagesUtils.getImage(IImagesKeys.PROCESS_EDITOR_ICON);
			}
		});
		processesTreeViewer.setContentProvider(new ITreeContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}
			public boolean hasChildren(Object element) {
				return false;
			}
			public Object getParent(Object element) {
				return null;
			}
			public Object[] getChildren(Object parentElement) {
				return null;
			}
		});
		
		Composite buttonContainer = new Composite(treeContainer, SWT.NORMAL);
		buttonContainer.setLayout(new FillLayout(SWT.VERTICAL));
		buttonContainer.setLayoutData(new GridData(SWT.LEFT,SWT.LEFT,false,false));
		
		Button buttonUp = new Button(buttonContainer, SWT.PUSH);
		buttonUp.setImage(ImagesUtils.getImage(IImagesKeys.MOVE_FUNCTION_UP));
		buttonUp.setToolTipText(Messages.getString("ProcessEditor.MoveFunctionUpButtonTooltip"));
		buttonUp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				int index = processesTree.indexOf(processesTree.getSelection()[0]);
				if(index>0) {
					IResource previousProcessing = selectedProcesses[index-1];
					selectedProcesses[index-1] = selectedProcesses[index];
					selectedProcesses[index] = previousProcessing;
					processesTreeViewer.refresh();
				}
			}
		});
		
		Button buttonDown = new Button(buttonContainer, SWT.PUSH);
		buttonDown.setImage(ImagesUtils.getImage(IImagesKeys.MOVE_FUNCTION_DOWN));
		buttonDown.setToolTipText(Messages.getString("ProcessEditor.MoveFunctionDownButtonTooltip"));
		buttonDown.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				int index = processesTree.indexOf(processesTree.getSelection()[0]);
				if(index<processesTree.getItemCount() - 1) {
					IResource nextProcessing = selectedProcesses[index+1];
					selectedProcesses[index+1] = selectedProcesses[index];
					selectedProcesses[index] = nextProcessing;
					processesTreeViewer.refresh();
				}
			}
		});
		
		processesTreeViewer.setInput(selectedProcesses);
		
		Composite container = new Composite(sashForm, SWT.BORDER);			
		container.setLayout(new GridLayout(1,false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label channelNamelabel = new Label(container,SWT.NONE);
		channelNamelabel.setText(Messages.getString("SubjectsSelectionDialog.ChooseName")); //$NON-NLS-1$
		channelNamelabel.setLayoutData(new GridData());
			
		Text channelNameText= new Text(container, SWT.SEARCH | SWT.CANCEL);
		channelNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));				
		
		Label matchingChannelNamelabel = new Label(container,SWT.NONE);
		matchingChannelNamelabel.setText(Messages.getString("SubjectsSelectionDialog.MatchingNames")); //$NON-NLS-1$
		matchingChannelNamelabel.setLayoutData(new GridData());
		
		Table subjectsTable = new Table(container,SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.CHECK);
		subjectsTableViewer = new CheckboxTableViewer(subjectsTable);
		subjectsTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		subjectsTableViewer.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(Object inputElement) {	
				if(inputElement instanceof Experiments) {
					IResource[] experiments = ((Experiments)inputElement).getChildren(); 
					HashSet<Subject> subjects = new HashSet<Subject>(0);
					for (int i = 0; i < experiments.length; i++) {
						Experiment experiment = (Experiment) experiments[i];
						Subject[] subSubjects = experiment.getSubjects();
						for (int j = 0; j < subSubjects.length; j++) if(subSubjects[j].isLoaded()) subjects.add(subSubjects[j]);
					}
					return subjects.toArray();
				}
				return null;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		subjectsTableViewer.setLabelProvider(new ILabelProvider(){
			public Image getImage(Object element) {
				return null;
			}
			public String getText(Object element) {
				return ((Subject)element).getLocalPath();
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
		subjectsTableViewer.setSorter(new ViewerSorter());
		channelNameText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				ViewerFilter[] filters = subjectsTableViewer.getFilters();
				for (int i = 0; i < filters.length; i++) subjectsTableViewer.removeFilter(filters[i]);
				String filterString = ((Text)e.widget).getText();
				if(!filterString.equals("")) subjectsTableViewer.addFilter(new SubjectsFilter(filterString));				 //$NON-NLS-1$
			}			
		});
		
		subjectsTableViewer.setInput(Experiments.getInstance());
		
		Button selectAllButton = new Button(container,SWT.PUSH);
		selectAllButton.setText(Messages.getString("SavePage.InvertSelectionButtonTitle")); //$NON-NLS-1$
		selectAllButton.setLayoutData(new GridData(SWT.FILL,SWT.NONE,false,false));
		selectAllButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = subjectsTableViewer.getTable().getItems();
				for (int i = 0; i < items.length; i++) {
					TableItem tableItem = items[i];
					tableItem.setChecked(!tableItem.getChecked());
				}
			}
		});
		
		sashForm.setWeights(new int[]{50,50});
		
		return dialogArea;
	}

	@Override
	protected void okPressed() {
		Object[] subjects = subjectsTableViewer.getCheckedElements();
		for (int i = 0; i < subjects.length; i++) selectedSubjectsList.add((Subject)subjects[i]);
		super.okPressed();
	}
	
	public Subject[] getSubjectsList() {
		return selectedSubjectsList.toArray(new Subject[selectedSubjectsList.size()]);
	}

	public Processing[] getProcessesList() {
		return (Processing[]) selectedProcesses;
	}
	
}
