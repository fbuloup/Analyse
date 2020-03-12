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
package analyse.gui.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

@SuppressWarnings("deprecation")
public class ExportExperimentsPage extends WizardPage {
	
	public static final String PAGE_NAME = "EXPORT_EXPERIMENTS_PAGE"; //$NON-NLS-1$
	private CheckboxTreeViewer exportExperimentsTreeViewer;

	private class ChannelsFilter extends ViewerFilter {
		private String names;
		public ChannelsFilter(String names) {
			this.names = names;
		}
		@Override
		public boolean select(Viewer viewer, Object parentElement,Object element) {			
			if (names.equals("")) return true; //$NON-NLS-1$
			if(names.matches("^\\*$")) return true;
			String namesTemp = names.replaceAll("\\*", "\\\\w*");
			namesTemp = namesTemp.replaceAll("\\.", "\\\\.");
			if(element instanceof Experiment) {
				return ((Experiment)element).getName().matches(namesTemp);
			}
			return true;
		}
	}
	
	protected ExportExperimentsPage() {
		super(PAGE_NAME,Messages.getString("ExportExperimentsPage.Title"),ImagesUtils.getImageDescriptor(IImagesKeys.EXPORT_WIZARD_BANNER)); //$NON-NLS-1$
		setMessage(Messages.getString("ExportExperimentsPage.Text")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent,SWT.NONE);
		container.setLayout(new GridLayout());
		
		Label channelNamelabel = new Label(container,SWT.NONE);
		channelNamelabel.setText(Messages.getString("InputsSelectionDialog.ChooseName")); //$NON-NLS-1$
//		channelNamelabel.setLayoutData(new GridData());
		
		Text channelNameText= new Text(container, SWT.SEARCH | SWT.CANCEL);
		channelNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		channelNameText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				ViewerFilter[] filters = exportExperimentsTreeViewer.getFilters();
				for (int i = 0; i < filters.length; i++) exportExperimentsTreeViewer.removeFilter(filters[i]);
				String filterString = ((Text)e.widget).getText();
				if(!filterString.equals("")) exportExperimentsTreeViewer.addFilter(new ChannelsFilter(filterString));				 //$NON-NLS-1$
			}			
		});
		
		exportExperimentsTreeViewer = new CheckboxTreeViewer(container);
		exportExperimentsTreeViewer.getTree().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		exportExperimentsTreeViewer.setSorter(new ViewerSorter());
		exportExperimentsTreeViewer.setContentProvider(new ExperimentTreeContentProvider());
		exportExperimentsTreeViewer.setLabelProvider(new ExperimentTreeLabelProvider());
		
		exportExperimentsTreeViewer.setInput(Experiments.getInstance().getChildren());
//		exportExperimentsTreeViewer.expandToLevel(4);
		
		setControl(container);

	}
	
	private class ExperimentTreeContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			return (Object[]) parentElement;
		}
		public Object getParent(Object element) {
			if(element instanceof IResource) {
				IResource resource = (IResource)element;
				resource.getParent();
			}
			return null;
		}
		public boolean hasChildren(Object element) {
			return false;
		}
		public Object[] getElements(Object inputElement) {
			return (Object[]) inputElement;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class ExperimentTreeLabelProvider implements ILabelProvider {
		public Image getImage(Object element) {
			if(element instanceof Experiment) return ImagesUtils.getImage(IImagesKeys.EXPERIMENT_ICON);
			return null;
		}
		public String getText(Object element) {
			if(element instanceof IResource) return ((IResource)element).getName();
			return null;
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
	}
	
	public List<Object> getSelection() {
		Object[] objects = exportExperimentsTreeViewer.getCheckedElements();
		if(objects.length > 0)
		return Arrays.asList(objects);
		else return new ArrayList<Object>(0);
	}

}
