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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
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
import analyse.model.Signal;
import analyse.model.Subject;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

@SuppressWarnings("deprecation")
public class ExportSignalsPage extends WizardPage {
	
	public static final String PAGE_NAME = "EXPORT_SIGNALS_PAGE"; //$NON-NLS-1$
	private CheckboxTreeViewer exportSignalsTreeViewer;
	private HashMap<Subject, HashSet<Signal>> signalsSubjects = new HashMap<Subject, HashSet<Signal>>(0);
	
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
			if(element instanceof Signal) {
				return ((Signal)element).getName().matches(namesTemp);
			}
			return true;
		}
	}
	
	protected ExportSignalsPage() {
		super(PAGE_NAME,Messages.getString("ExportSignalsPage.Title"),ImagesUtils.getImageDescriptor(IImagesKeys.EXPORT_WIZARD_BANNER)); //$NON-NLS-1$
		setMessage(Messages.getString("ExportSignalsPage.Text")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent,SWT.NONE);
		container.setLayout(new GridLayout());
		
		Label channelNamelabel = new Label(container,SWT.NONE);
		channelNamelabel.setText(Messages.getString("InputsSelectionDialog.ChooseName")); //$NON-NLS-1$
		channelNamelabel.setLayoutData(new GridData());
		
		Text channelNameText= new Text(container, SWT.SEARCH | SWT.CANCEL);
		channelNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
		channelNameText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				ViewerFilter[] filters = exportSignalsTreeViewer.getFilters();
				for (int i = 0; i < filters.length; i++) exportSignalsTreeViewer.removeFilter(filters[i]);
				String filterString = ((Text)e.widget).getText();
				if(!filterString.equals("")) exportSignalsTreeViewer.addFilter(new ChannelsFilter(filterString));				 //$NON-NLS-1$
			}			
		});
		
		exportSignalsTreeViewer = new CheckboxTreeViewer(container);
		exportSignalsTreeViewer.getTree().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		exportSignalsTreeViewer.setSorter(new ViewerSorter());
		exportSignalsTreeViewer.setContentProvider(new ExperimentTreeContentProvider());
		exportSignalsTreeViewer.setLabelProvider(new ExperimentTreeLabelProvider());
		
		exportSignalsTreeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object element = event.getElement();
				exportSignalsTreeViewer.setSubtreeChecked(element, event.getChecked());
				exportSignalsTreeViewer.setGrayed(element, false);
				Object parentElement = null;
				if(element instanceof Subject) parentElement = ((Subject)element).getParent();
				if(element instanceof Signal) parentElement = ((Signal)element).getParent();
				if(parentElement != null) setParentState(parentElement);
			}
			private void setParentState(Object element) {
				boolean checked = false;
				boolean allChecked = true;
				boolean grayed = false;
				if(element instanceof Experiment) {
					IResource[] subjects = ((Experiment)element).getSubjects();
					for (int i = 0; i < subjects.length; i++) {
						Subject subject = (Subject) subjects[i];
						if(subject.isLoaded()) {
							checked = checked || exportSignalsTreeViewer.getChecked(subject);
							allChecked = allChecked && exportSignalsTreeViewer.getChecked(subject);
							grayed = grayed || exportSignalsTreeViewer.getGrayed(subject);
						}
					}					
				}
				if(element instanceof Subject) {
					Object[] objects = signalsSubjects.get(element).toArray();
					for (int i = 0; i < objects.length; i++) {
						Signal signal = (Signal)objects[i];
						checked = checked || exportSignalsTreeViewer.getChecked(signal);
						allChecked = allChecked && exportSignalsTreeViewer.getChecked(signal);
						grayed = grayed || exportSignalsTreeViewer.getGrayed(signal);
					}
				}
				exportSignalsTreeViewer.setChecked(element, false);
				exportSignalsTreeViewer.setGrayed(element, false);
				if(allChecked) {
					exportSignalsTreeViewer.setChecked(element, allChecked);
					exportSignalsTreeViewer.setGrayed(element, grayed);
				} else {
					if(!checked) {
						exportSignalsTreeViewer.setChecked(element, false);
						exportSignalsTreeViewer.setGrayed(element, false);
					} else {
						exportSignalsTreeViewer.setChecked(element, true);
						exportSignalsTreeViewer.setGrayed(element, true);
					}
				}
				if(element instanceof Subject) setParentState(((Subject)element).getParent());
			}
		});
		
		exportSignalsTreeViewer.setInput(Experiments.getInstance().getChildren());
		exportSignalsTreeViewer.expandToLevel(3);
		
		setControl(container);
	}
	
	public class ExperimentTreeContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof Experiment) {				
				//Return only loaded subject(s)
				HashSet<Subject> loadedSubjects = new HashSet<Subject>(0); 
				Experiment experiment = (Experiment)parentElement;
				IResource[] subjects = experiment.getSubjects();
				for (int i = 0; i < subjects.length; i++) if(((Subject)subjects[i]).isLoaded()) loadedSubjects.add((Subject) subjects[i]);
				Object[] objects = loadedSubjects.toArray();
				return objects;
			}
			if(parentElement instanceof Subject) {
				Subject subject = (Subject)parentElement;
				HashSet<Signal> signals = new HashSet<Signal>(0); 
				//IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				String[] signalsNames = subject.getSignalsNames();//mathEngine.getSignalsNames(subject.getLocalPath());
				for (int i = 0; i < signalsNames.length; i++) signals.add(new Signal(signalsNames[i], subject));
				signalsSubjects.put(subject, signals);
				Object[] objects = signals.toArray();
				return objects;
			}
			return null;
		}
		public Object getParent(Object element) {
			if(element instanceof IResource) {
				IResource resource = (IResource)element;
				resource.getParent();
			}
			if(element instanceof Signal) {
				Signal signal = (Signal)element;
				return signal.getParent();
			}
			return null;
		}
		public boolean hasChildren(Object element) {
			if(element instanceof Subject) return true;
			if(element instanceof Experiment) {
				IResource resource = (IResource)element;
				Object[] children = resource.getChildren();
				if(children == null) return false;
				return children.length > 0;
			}
			return false;
		}
		public Object[] getElements(Object inputElement) {
			HashSet<Experiment> experiments = new HashSet<Experiment>(0);
			if(inputElement instanceof IResource[]) {
				IResource[] resources = (IResource[]) inputElement;
				for (int i = 0; i < resources.length; i++) {
					if(resources[i] instanceof Experiment) {
						Experiment experiment = (Experiment)resources[i];
						IResource[] subjects = experiment.getSubjects();
						for (int j = 0; j < subjects.length; j++) {
							if(((Subject)subjects[j]).isLoaded()) experiments.add(experiment);
						}
					}
				}
			}
			Object[] objects = experiments.toArray();
			return objects;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public class ExperimentTreeLabelProvider implements ILabelProvider {
		public Image getImage(Object element) {
			if(element instanceof Experiment) return ImagesUtils.getImage(IImagesKeys.EXPERIMENT_ICON);
			if(element instanceof Subject) return ImagesUtils.getImage(IImagesKeys.SUBJECT_ICON);
			if(element instanceof Signal) return ImagesUtils.getImage(IImagesKeys.SIGNALS_ICON);
			return null;
		}
		public String getText(Object element) {
			if(element instanceof IResource) return ((IResource)element).getName();
			if(element instanceof Signal) return ((Signal)element).getName();
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
		Object[] objects = exportSignalsTreeViewer.getCheckedElements();
		if(objects.length > 0)
		return Arrays.asList(objects);
		else return new ArrayList<Object>(0);
	}

}
