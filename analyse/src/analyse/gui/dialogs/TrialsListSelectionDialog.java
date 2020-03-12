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

import java.util.ArrayList;

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.Subject;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public final class TrialsListSelectionDialog extends TitleAreaDialog {

	//ArrayList<String> channelsList = new ArrayList<String>(0);
	private ListViewer trialListListViewer;
	public static String selectedTrialsList;
	
	private class SubjectsFilter extends ViewerFilter {
		private String names;
		public SubjectsFilter(String names) {
			this.names = names;
		}
		@Override
		public boolean select(Viewer viewer, Object parentElement,Object element) {			
			if (names.equals("")) return true; //$NON-NLS-1$
			return ((String)element).contains(names);
		}
	}
	
	public TrialsListSelectionDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.MAX | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);		

		String label2 = Messages.getString("TrialsListSelectionDialog.Title"); //$NON-NLS-1$
		String label1 = Messages.getString("TrialsListSelectionDialog.Text"); //$NON-NLS-1$
		
		parent.getShell().setText(label1);
		
		getShell().setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		
		setTitle(label2);
		setMessage(Messages.getString("InputsSelectionDialog.FilterArea"));		 //$NON-NLS-1$
			
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
				
		Composite container = new Composite(dialogArea, SWT.NONE);			
		container.setLayout(new GridLayout(1,false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label channelNamelabel = new Label(container,SWT.NONE);
		channelNamelabel.setText(Messages.getString("InputsSelectionDialog.ChooseName")); //$NON-NLS-1$
		channelNamelabel.setLayoutData(new GridData());
			
		Text channelNameText= new Text(container, SWT.SEARCH | SWT.CANCEL);
		channelNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));				
		
		Label matchingChannelNamelabel = new Label(container,SWT.NONE);
		matchingChannelNamelabel.setText(Messages.getString("InputsSelectionDialog.MatchingNames")); //$NON-NLS-1$
		matchingChannelNamelabel.setLayoutData(new GridData());
		
		trialListListViewer = new ListViewer(container,SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		trialListListViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		trialListListViewer.setContentProvider(new IStructuredContentProvider(){
			@SuppressWarnings("rawtypes")
			public Object[] getElements(Object inputElement) {				
				String[] emptyArray = new String[0];
				if(inputElement instanceof ArrayList) {
					String[] names = new String[((ArrayList)inputElement).size()];
					for (int i = 0; i < names.length; i++) names[i] = (String) ((ArrayList)inputElement).get(i);
					return names;
				}
				return emptyArray;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
		});
		trialListListViewer.setLabelProvider(new ILabelProvider(){
			public Image getImage(Object element) {
				return null;
			}
			public String getText(Object element) {
				return (String)element;
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
		
		channelNameText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				ViewerFilter[] filters = trialListListViewer.getFilters();
				for (int i = 0; i < filters.length; i++) trialListListViewer.removeFilter(filters[i]);
				String filterString = ((Text)e.widget).getText();
				if(!filterString.equals("")) trialListListViewer.addFilter(new SubjectsFilter(filterString));				 //$NON-NLS-1$
			}			
		});
		
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		IResource[] experiments = Experiments.getInstance().getChildren(); 
		ArrayList<String> namesList = new ArrayList<String>(0);
		ArrayList<String> strings = new ArrayList<String>(0) ;
		ArrayList<String> alreadyUsedInputs = new ArrayList<String>(0);
		for (int i = 0; i < experiments.length; i++) {	
			strings.clear();
			IResource[] subjects = ((Experiment)experiments[i]).getSubjects();
			for (int j = 0; j < subjects.length; j++) {
				if(((Subject)subjects[j]).isLoaded()) {
					String[] localStrings = ((Subject)subjects[j]).getSignalsNames();//mathEngine.getSignalsNames(subjects[j].getLocalPath());
					int nbTrials = mathEngine.getNbTrials(subjects[j].getLocalPath() + "." + localStrings[0]);
					strings.add("1:" + nbTrials + " - " + subjects[j].getLocalPath());
				}
			}
			for (int k = 0; k < strings.size(); k++) {
				boolean found = false;
				for (int l = 0; l < alreadyUsedInputs.size(); l++) {
					if(strings.get(k).startsWith(alreadyUsedInputs.get(l))) found = true;
				}
				if(!found) namesList.add(strings.get(k));
			}
		}
		
		trialListListViewer.setInput(namesList);
		
		return dialogArea;
	}

	@Override
	protected void okPressed() {
		selectedTrialsList = trialListListViewer.getList().getItem(trialListListViewer.getList().getSelectionIndex()).replaceAll(" - \\w+\\.\\w+$", "");
		super.okPressed();
	}
	
	public String getTrialsList() {
		return selectedTrialsList;
	}
	
}
