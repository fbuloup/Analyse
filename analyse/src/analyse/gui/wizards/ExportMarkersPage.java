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

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

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
import analyse.model.Marker;
import analyse.model.MarkersGroup;
import analyse.model.Signal;
import analyse.model.Subject;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

@SuppressWarnings("deprecation")
public class ExportMarkersPage extends WizardPage {

	public static final String PAGE_NAME = "EXPORT_MARKERS_PAGE"; //$NON-NLS-1$
	private CheckboxTreeViewer exportMarkersTreeViewer;
	private HashMap<Subject, HashSet<Signal>> signalsSubjects = new HashMap<Subject, HashSet<Signal>>(0);
	private HashMap<Signal, HashSet<MarkersGroup>> markersGroupsSignal = new HashMap<Signal, HashSet<MarkersGroup>>(0);
	private HashMap<MarkersGroup, HashSet<Marker>> markersMarkersGroup = new HashMap<MarkersGroup, HashSet<Marker>>(0);
	
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
			if(element instanceof MarkersGroup) {
				return ((MarkersGroup)element).getLabel().matches(namesTemp);
			}
			return true;
		}
	}
	
	protected ExportMarkersPage() {
		super(PAGE_NAME,Messages.getString("ExportMarkersPage.Title"),ImagesUtils.getImageDescriptor(IImagesKeys.EXPORT_WIZARD_BANNER)); //$NON-NLS-1$
		setMessage(Messages.getString("ExportMarkersPage.Text")); //$NON-NLS-1$
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
				ViewerFilter[] filters = exportMarkersTreeViewer.getFilters();
				for (int i = 0; i < filters.length; i++) exportMarkersTreeViewer.removeFilter(filters[i]);
				String filterString = ((Text)e.widget).getText();
				if(!filterString.equals("")) exportMarkersTreeViewer.addFilter(new ChannelsFilter(filterString));				 //$NON-NLS-1$
				exportMarkersTreeViewer.expandToLevel(4);
			}			
		});
		
		exportMarkersTreeViewer = new CheckboxTreeViewer(container);
		exportMarkersTreeViewer.getTree().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		exportMarkersTreeViewer.setSorter(new ViewerSorter());
		exportMarkersTreeViewer.setContentProvider(new ExperimentTreeContentProvider());
		exportMarkersTreeViewer.setLabelProvider(new ExperimentTreeLabelProvider());
		
		exportMarkersTreeViewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				Object element = event.getElement();
				exportMarkersTreeViewer.setSubtreeChecked(element, event.getChecked());
				exportMarkersTreeViewer.setGrayed(element, false);
				Object parentElement = null;
				if(element instanceof Subject) parentElement = ((Subject)element).getParent();
				if(element instanceof Signal) parentElement = ((Signal)element).getParent();
				if(element instanceof MarkersGroup) parentElement = ((MarkersGroup)element).getSignal();
				if(element instanceof Marker) parentElement = ((Marker)element).getMarkersGroup();
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
							checked = checked || exportMarkersTreeViewer.getChecked(subject);
							allChecked = allChecked && exportMarkersTreeViewer.getChecked(subject);
							grayed = grayed || exportMarkersTreeViewer.getGrayed(subject);
						}
					}					
				}
				if(element instanceof Subject) {
					Object[] objects = signalsSubjects.get(element).toArray();
					for (int i = 0; i < objects.length; i++) {
						Signal signal = (Signal)objects[i];
						checked = checked || exportMarkersTreeViewer.getChecked(signal);
						allChecked = allChecked && exportMarkersTreeViewer.getChecked(signal);
						grayed = grayed || exportMarkersTreeViewer.getGrayed(signal);
					}
				}
				if(element instanceof Signal) {
					Object[] objects = markersGroupsSignal.get(element).toArray();
					for (int i = 0; i < objects.length; i++) {
						MarkersGroup markersGroup = (MarkersGroup)objects[i];
						checked = checked || exportMarkersTreeViewer.getChecked(markersGroup);
						allChecked = allChecked && exportMarkersTreeViewer.getChecked(markersGroup);
						grayed = grayed || exportMarkersTreeViewer.getGrayed(markersGroup);
					}
				}
				if(element instanceof MarkersGroup) {
					Object[] objects = markersMarkersGroup.get(element).toArray();
					for (int i = 0; i < objects.length; i++) {
						Marker marker = (Marker)objects[i];
						checked = checked || exportMarkersTreeViewer.getChecked(marker);
						allChecked = allChecked && exportMarkersTreeViewer.getChecked(marker);
						grayed = grayed || exportMarkersTreeViewer.getGrayed(marker);
					}
				}
				exportMarkersTreeViewer.setChecked(element, false);
				exportMarkersTreeViewer.setGrayed(element, false);
				if(allChecked) {
					exportMarkersTreeViewer.setChecked(element, allChecked);
					exportMarkersTreeViewer.setGrayed(element, grayed);
				} else {
					if(!checked) {
						exportMarkersTreeViewer.setChecked(element, false);
						exportMarkersTreeViewer.setGrayed(element, false);
					} else {
						exportMarkersTreeViewer.setChecked(element, true);
						exportMarkersTreeViewer.setGrayed(element, true);
					}
				}
				if(element instanceof Subject) setParentState(((Subject)element).getParent());
				if(element instanceof Signal) setParentState(((Signal)element).getParent());
				if(element instanceof MarkersGroup) setParentState(((MarkersGroup)element).getSignal());
			}
		});
		
		exportMarkersTreeViewer.setInput(Experiments.getInstance().getChildren());
		exportMarkersTreeViewer.expandToLevel(4);
		
		setControl(container);
	}

	private class ExperimentTreeContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof Experiment) {				
				//Return only loaded subject(s) that have signal(s) that have marker(s)
				HashSet<Subject> validSubjects = new HashSet<Subject>(0); 
				Experiment experiment = (Experiment)parentElement;
				IResource[] subjects = experiment.getSubjects();
				for (int i = 0; i < subjects.length; i++) if(((Subject)subjects[i]).isLoaded()) {
					IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
					String[] signalsNames = ((Subject)subjects[i]).getSignalsNames();//mathEngine.getSignalsNames(subjects[i].getLocalPath());
					for (int j = 0; j < signalsNames.length; j++) {
						String fullSignalName = subjects[i].getLocalPath() + "." + signalsNames[j];
						if(mathEngine.getNbMarkersGroups(fullSignalName) > 0) validSubjects.add((Subject) subjects[i]);
					}
				}
				Object[] objects = validSubjects.toArray();
				return objects;
			}
			if(parentElement instanceof Subject) {
				//return only signals with markers 
				Subject subject = (Subject)parentElement;
				HashSet<Signal> signals = new HashSet<Signal>(0); 
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				String[] signalsNames = subject.getSignalsNames();//mathEngine.getSignalsNames(subject.getLocalPath());
				for (int i = 0; i < signalsNames.length; i++) {
					String fullSignalName = subject.getLocalPath() + "." + signalsNames[i];
					int nbMarkersGroup = mathEngine.getNbMarkersGroups(fullSignalName);
					if(nbMarkersGroup > 0) signals.add(new Signal(signalsNames[i], subject));
				}
				signalsSubjects.put(subject, signals);
				Object[] objects = signals.toArray();
				return objects;
			}
			if(parentElement instanceof Signal) {
				Signal signal = (Signal)parentElement;
				HashSet<MarkersGroup> markersGroups = new HashSet<MarkersGroup>();
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				String fullSignalName = signal.getParent().getLocalPath() + "." + signal.getName();
				int nbMarkersGroup = mathEngine.getNbMarkersGroups(fullSignalName);
				for (int i = 0; i < nbMarkersGroup; i++) {
					String label = mathEngine.getMarkersGroupLabel(i+1, fullSignalName);
					MarkersGroup markersGroup = new MarkersGroup(signal, label, i+1);
					markersGroups.add(markersGroup);
				}
				markersGroupsSignal.put(signal,markersGroups);
				return markersGroups.toArray();
			}
			if(parentElement instanceof MarkersGroup) {
				MarkersGroup markersGroup = (MarkersGroup)parentElement;
				HashSet<Marker> markers = new HashSet<Marker>();
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				String fullSignalName = markersGroup.getSignal().getParent().getLocalPath() + "." + markersGroup.getSignal().getName();
				int[] trials = mathEngine.getTrialsListForMarkersGroup(markersGroup.getGroupNumber(), fullSignalName);
				double[] xValues = mathEngine.getXValuesForMarkersGroup(markersGroup.getGroupNumber(), fullSignalName);
				double[] yValues = mathEngine.getYValuesForMarkersGroup(markersGroup.getGroupNumber(), fullSignalName);
				for (int i = 0; i < yValues.length; i++) {
					Marker marker = new Marker(markersGroup, trials[i], xValues[i], yValues[i]);
					markers.add(marker);
				}
				markersMarkersGroup.put(markersGroup, markers);
				return markers.toArray();
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
			if(element instanceof MarkersGroup) {
			MarkersGroup markersGroup = (MarkersGroup)element;
			return markersGroup.getSignal();
			}
			if(element instanceof Marker) {
				Marker marker = (Marker)element;
				return marker.getMarkersGroup();
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
			if(element instanceof Signal) return true;
			if(element instanceof MarkersGroup) return true;
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

	private class ExperimentTreeLabelProvider implements ILabelProvider {
		public Image getImage(Object element) {
			if(element instanceof Experiment) return ImagesUtils.getImage(IImagesKeys.EXPERIMENT_ICON);
			if(element instanceof Subject) return ImagesUtils.getImage(IImagesKeys.SUBJECT_ICON);
			if(element instanceof Signal) return ImagesUtils.getImage(IImagesKeys.SIGNALS_ICON);
			if(element instanceof MarkersGroup) return ImagesUtils.getImage(IImagesKeys.MARKER_ICON);
			return null;
		}
		public String getText(Object element) {
			if(element instanceof IResource) return ((IResource)element).getName();
			if(element instanceof Signal) return ((Signal)element).getName();
			if(element instanceof MarkersGroup) return ((MarkersGroup)element).getLabel();
			if(element instanceof Marker) return ((Marker)element).getLabel();
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
		Object[] objects = exportMarkersTreeViewer.getCheckedElements();
		if(objects.length > 0)
		return Arrays.asList(objects);
		else return new ArrayList<Object>(0);
	}
	
}
