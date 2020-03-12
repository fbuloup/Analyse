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
package analyse.gui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;

import analyse.AnalyseApplication;
import analyse.model.Chart;
import analyse.model.DataFile;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.Folder;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.MultiCharts;
import analyse.model.Note;
import analyse.model.Processing;
import analyse.model.Subject;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

@SuppressWarnings("deprecation")
public final class ExperimentsView extends View implements IResourceObserver {

	private CTabItem experimentExplorer;
	private TreeViewer experimentsTreeViewer;
	
	private boolean removeChartsFromView;
	private boolean removeProcessingsFromView;
	private boolean removeDataFilesFromView;
	private boolean removeSubjectsFromView;
	private boolean removeFoldersFromView;
	private boolean removeUnloadedSubjectsFromView;
	private ToolBarManager toolBarManager;
	
	private final class CollapseAllAction extends Action {
		public CollapseAllAction() {
			super(Messages.getString("CollapseAllAction"),Action.AS_PUSH_BUTTON); //$NON-NLS-1$
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.COLLAPSE_ALL_ICON));
		}
		@Override
		public void run() {
			experimentsTreeViewer.collapseAll();
		}
	}
	
	private final class ExpandAllAction extends Action {
		public ExpandAllAction() {
			super(Messages.getString("ExpandAllAction"),Action.AS_PUSH_BUTTON); //$NON-NLS-1$
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.EXPAND_ALL_ICON));
		}
		@Override
		public void run() {
			experimentsTreeViewer.expandAll();
		}
	}
	
	private final class ExperimentsViewMenuAction extends Action {
		
		private Menu experimentsViewMenu;
		private RemoveChartsFromViewAction removeChartsFromViewAction;
		private RemoveDataFilesFromViewAction removeDataFilesFromViewAction;
		private RemoveFoldersFromViewAction removeFoldersFromViewAction;
		private RemoveProcessingsFromViewAction removeProcessingsFromViewAction;
		private RemoveSubjectsFromViewAction removeSubjectsFromViewAction;
		private RemoveUnloadedSubjectsFromViewAction removeUnloadedSubjectsFromViewAction;
		
		class ShowAllAction extends Action {
			public ShowAllAction() {
				super(Messages.getString("ShowAllAction"),Action.AS_PUSH_BUTTON); //$NON-NLS-1$
			}
			@Override
			public void run() {
				removeChartsFromView = false;
				removeProcessingsFromView = false;
				removeDataFilesFromView = false;
				removeSubjectsFromView = false;
				removeFoldersFromView = false;
				removeUnloadedSubjectsFromView = false;
				removeChartsFromViewAction.setChecked(removeChartsFromView);
				removeDataFilesFromViewAction.setChecked(removeDataFilesFromView);
				removeFoldersFromViewAction.setChecked(removeFoldersFromView);
				removeProcessingsFromViewAction.setChecked(removeProcessingsFromView);
				removeSubjectsFromViewAction.setChecked(removeSubjectsFromView);
				removeUnloadedSubjectsFromViewAction.setChecked(removeUnloadedSubjectsFromView);
				experimentsTreeViewer.refresh();
			}
		}
		class RemoveUnloadedSubjectsFromViewAction extends Action {
			public RemoveUnloadedSubjectsFromViewAction() {
				super(Messages.getString("RemoveUnloadedSubjectsFromViewAction"),Action.AS_CHECK_BOX); //$NON-NLS-1$
			}
			@Override
			public void run() {
				removeUnloadedSubjectsFromView = !removeUnloadedSubjectsFromView;
				experimentsTreeViewer.refresh();
			}
		}
		class RemoveSubjectsFromViewAction extends Action {
			public RemoveSubjectsFromViewAction() {
				super(Messages.getString("RemoveSubjectsFromViewAction"),Action.AS_CHECK_BOX); //$NON-NLS-1$
			}
			@Override
			public void run() {
				removeSubjectsFromView = !removeSubjectsFromView;
				experimentsTreeViewer.refresh();
			}
		}
		class RemoveDataFilesFromViewAction extends Action {	
			public RemoveDataFilesFromViewAction() {
				super(Messages.getString("RemoveDataFilesFromViewAction"),Action.AS_CHECK_BOX); //$NON-NLS-1$
			}		
			@Override
			public void run() {
				removeDataFilesFromView = !removeDataFilesFromView;
				experimentsTreeViewer.refresh();
			}
		}
		class RemoveChartsFromViewAction extends Action {		
			public RemoveChartsFromViewAction() {
				super(Messages.getString("RemoveChartsFromViewAction"),Action.AS_CHECK_BOX); //$NON-NLS-1$
			}		
			@Override
			public void run() {		
				removeChartsFromView = !removeChartsFromView;
				experimentsTreeViewer.refresh();
			}
		}
		class RemoveProcessingsFromViewAction extends Action {		
			public RemoveProcessingsFromViewAction() {
				super(Messages.getString("RemoveProcessingsFromViewAction"),Action.AS_CHECK_BOX); //$NON-NLS-1$
			}		
			@Override
			public void run() {
				removeProcessingsFromView = !removeProcessingsFromView;
				experimentsTreeViewer.refresh();
			}
		}
		class RemoveFoldersFromViewAction extends Action {		
			public RemoveFoldersFromViewAction() {
				super(Messages.getString("RemoveFoldersFromViewAction"),Action.AS_CHECK_BOX); //$NON-NLS-1$
			}		
			@Override
			public void run() {
				removeFoldersFromView = !removeFoldersFromView;
				experimentsTreeViewer.refresh();
			}
		}
		
		public ExperimentsViewMenuAction() {
			super("",Action.AS_PUSH_BUTTON);
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.SHOW_EXPERIMENTS_MENU_ICON));
			
			experimentsViewMenu = new Menu(getShell());
//			addActionToMenu(chartOptionsMenu, new PropertiesAction()); 
//			new MenuItem(chartOptionsMenu, SWT.SEPARATOR);
			removeChartsFromViewAction = new RemoveChartsFromViewAction();
			removeDataFilesFromViewAction = new RemoveDataFilesFromViewAction();
			removeFoldersFromViewAction = new RemoveFoldersFromViewAction();
			removeProcessingsFromViewAction = new RemoveProcessingsFromViewAction();
			removeSubjectsFromViewAction = new RemoveSubjectsFromViewAction();
			removeUnloadedSubjectsFromViewAction = new RemoveUnloadedSubjectsFromViewAction();
			ShowAllAction showAllAction = new ShowAllAction();
			addActionToMenu(experimentsViewMenu, removeUnloadedSubjectsFromViewAction);
			addActionToMenu(experimentsViewMenu, removeSubjectsFromViewAction);
			new MenuItem(experimentsViewMenu, SWT.SEPARATOR);
			addActionToMenu(experimentsViewMenu, removeDataFilesFromViewAction);
			addActionToMenu(experimentsViewMenu, removeFoldersFromViewAction);
			addActionToMenu(experimentsViewMenu, removeChartsFromViewAction);
			addActionToMenu(experimentsViewMenu, removeProcessingsFromViewAction);
			new MenuItem(experimentsViewMenu, SWT.SEPARATOR);
			addActionToMenu(experimentsViewMenu, showAllAction);
			removeChartsFromViewAction.setChecked(removeChartsFromView);
			removeDataFilesFromViewAction.setChecked(removeDataFilesFromView);
			removeFoldersFromViewAction.setChecked(removeFoldersFromView);
			removeProcessingsFromViewAction.setChecked(removeProcessingsFromView);
			removeSubjectsFromViewAction.setChecked(removeSubjectsFromView);
			removeUnloadedSubjectsFromViewAction.setChecked(removeUnloadedSubjectsFromView);
//			setMenu(experimentsViewMenu);
		}
		
		protected void addActionToMenu(Menu parent, Action action) { 
			ActionContributionItem item = new ActionContributionItem(action);
			item.fill(parent, -1);
		}
		
		@Override
		public void run() {
			ToolBar toolBar = (ToolBar) getTopRight();
			Point p = Display.getDefault().map(toolBar.getParent(), null, toolBar.getLocation());
			p.y = p.y + toolBar.getBounds().height;
			experimentsViewMenu.setLocation(p);
			experimentsViewMenu.setVisible(true);
		}
	}
	
	private class ExperimentsFilter extends ViewerFilter {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if(removeChartsFromView && element instanceof Chart) return false;
			if(removeChartsFromView && element instanceof MultiCharts) return false;
			if(removeDataFilesFromView && element instanceof DataFile) return false;
			if(removeFoldersFromView && element instanceof Folder) return false;
			if(removeProcessingsFromView && element instanceof Processing) return false;
			if(removeSubjectsFromView && element instanceof Subject) return false;
			if(removeUnloadedSubjectsFromView && element instanceof Subject) return ((Subject)element).isLoaded();
			return true;
		}
	}
	
	public ExperimentsView(Composite parent, int style) {
		super(parent, style);
		initView(0);
	}
	
	protected void initView(int showMode) {
		
		createToolBar();
		
		boolean init = experimentExplorer == null;
		if(!init) init = experimentExplorer.isDisposed();
		if(init) {
			experimentExplorer = createTabItem();
			experimentExplorer.setText(Messages.getString("ExperimentsView.Title"));
			experimentExplorer.setImage(ImagesUtils.getImage(IImagesKeys.EXPERIMENTS_VIEW_ICON));
			
			experimentsTreeViewer = new TreeViewer(this, SWT.BORDER | SWT.MULTI);
			experimentExplorer.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					experimentsTreeViewer.getTree().dispose();
				}
			});
			experimentExplorer.setControl(experimentsTreeViewer.getControl());
			experimentsTreeViewer.setContentProvider(new ITreeContentProvider() {
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}
				public void dispose() {
				}
				public Object[] getElements(Object inputElement) {
					return (IResource[])((Experiments)inputElement).getChildren();
				}
				public boolean hasChildren(Object element) {
					return ((IResource)element).hasChildren();
				}
				public Object getParent(Object element) {
					return ((IResource)element).getParent();
				}
				public Object[] getChildren(Object parentElement) {
					return ((IResource)parentElement).getChildren();
				}
			});
			ILabelProvider experimentsViewLabelProvider = new ExperimentsViewLabelProvider();
			ILabelDecorator experimentsViewLabelDecorator = new ExperimentsViewLabelDecorator();
			experimentsTreeViewer.setLabelProvider(new DecoratingLabelProvider(experimentsViewLabelProvider, experimentsViewLabelDecorator));

			experimentsTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					IStructuredSelection selection = (IStructuredSelection) experimentsTreeViewer.getSelection();
					Object[] selectedResources = selection.toArray();
					for (int i = 0; i < selectedResources.length; i++) {
						if(selectedResources[i] instanceof Experiment || selectedResources[i] instanceof Folder || selectedResources[i] instanceof Subject)
						experimentsTreeViewer.setExpandedState(selectedResources[i], !experimentsTreeViewer.getExpandedState(selectedResources[i]));
						else if(!(selectedResources[i] instanceof DataFile)){
							AnalyseApplication.getAnalyseApplicationWindow().openEditor((IResource) selectedResources[i]);
						}
					}
				}
			});
			experimentsTreeViewer.setSorter(new ViewerSorter(){
				@Override
				public int category(Object object) {
					if(object instanceof Experiment) return 1;
					if(object instanceof Chart) return 2;
					if(object instanceof MultiCharts) return 2;
					if(object instanceof Processing) return 3;
					if(object instanceof Note) return 4;
					if(object instanceof Folder) return 5;
					if(object instanceof Subject) return 6;
					return 6;
				}
			});
			experimentsTreeViewer.addFilter(new ExperimentsFilter());
			experimentsTreeViewer.setInput(Experiments.getInstance());
			
			experimentsTreeViewer.getControl().addFocusListener(this);
			Experiments.getInstance().addExperimentObserver(this);
			
			experimentExplorer.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					Experiments.getInstance().removeExperimentObserver(ExperimentsView.this);
				}
			});
			
			MenuManager popupMenuManager = new MenuManager("popupMenuManagerExperiments");

			MenuManager newMenuManager = new MenuManager(Messages.getString("New"));
			popupMenuManager.add(newMenuManager);
			newMenuManager.add(AnalyseApplicationWindow.newExperimentAction);
			newMenuManager.add(AnalyseApplicationWindow.newSubjectAction);
			newMenuManager.add(new Separator());
			newMenuManager.add(AnalyseApplicationWindow.newFolderAction);
			newMenuManager.add(AnalyseApplicationWindow.newChartAction);
			newMenuManager.add(AnalyseApplicationWindow.newProcessAction);
			newMenuManager.add(AnalyseApplicationWindow.newNoteAction);

			popupMenuManager.add(new Separator());
			popupMenuManager.add(AnalyseApplicationWindow.loadUnloadSubjectsAction);
			popupMenuManager.add(AnalyseApplicationWindow.runProcessingAction);
			popupMenuManager.add(AnalyseApplicationWindow.runBatchProcessingAction);
			popupMenuManager.add(AnalyseApplicationWindow.refactorAction);
			popupMenuManager.add(new Separator());
			popupMenuManager.add(AnalyseApplicationWindow.saveAction);
			popupMenuManager.add(AnalyseApplicationWindow.saveAllAction);
			popupMenuManager.add(new Separator());
			popupMenuManager.add(AnalyseApplicationWindow.openAction);
			popupMenuManager.add(new Separator());
			popupMenuManager.add(AnalyseApplicationWindow.copyAction);
			popupMenuManager.add(AnalyseApplicationWindow.pasteAction);
			popupMenuManager.add(new Separator());
			popupMenuManager.add(AnalyseApplicationWindow.renameAction);
			popupMenuManager.add(new Separator());
			popupMenuManager.add(AnalyseApplicationWindow.deleteAction);

			experimentsTreeViewer.getTree().setMenu(popupMenuManager.createContextMenu(experimentsTreeViewer.getTree()));
			
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.newSubjectAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.renameAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.deleteAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.newFolderAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.copyAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.pasteAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.newChartAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.newProcessAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.newNoteAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.openAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.loadUnloadSubjectsAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.saveAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.refactorAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.runProcessingAction);
			experimentsTreeViewer.addSelectionChangedListener(AnalyseApplicationWindow.runBatchProcessingAction);
		}
	}

	public void addSelectionChangedListener(ISelectionChangedListener selectionChangedListener) {
		experimentsTreeViewer.addSelectionChangedListener(selectionChangedListener);
	}
	
	public void removeSelectionChangedListener(ISelectionChangedListener selectionChangedListener) {
		experimentsTreeViewer.removeSelectionChangedListener(selectionChangedListener);
	}
	
	public void update(int message, IResource[] resources) {
		if(message == IResourceObserver.PROCESS_RUN) {
			for (int i = 0; i < resources.length; i++) {
				Processing processing = (Processing) resources[i];
				String[] newChannels = processing.getLastCreatedChannels();
				for (int j = 0; j < newChannels.length; j++) {
					String subjectLocalPath = newChannels[j].split("\\.")[0] + "." + newChannels[j].split("\\.")[1];
					Subject subject = Experiments.getInstance().getSubjectByName(subjectLocalPath);
					subject.setModified(true);
				}
				String[] modifiedSignals = processing.getLastModifiedSignals();
				for (int j = 0; j < modifiedSignals.length; j++) {
					String subjectLocalPath = modifiedSignals[j].split("\\.")[0] + "." + modifiedSignals[j].split("\\.")[1];
					Subject subject = Experiments.getInstance().getSubjectByName(subjectLocalPath);
					subject.setModified(true);
				}
			}
		}
		experimentsTreeViewer.refresh();
		if(message == IResourceObserver.EXPERIMENT_CREATED || message == IResourceObserver.SUBJECT_CREATED || message == IResourceObserver.FOLDER_CREATED
				|| message == IResourceObserver.CHART_CREATED || message == IResourceObserver.PROCESSING_CREATED || message == IResourceObserver.NOTE_CREATED) {
			experimentsTreeViewer.reveal(resources[0]);
			experimentsTreeViewer.setSelection(new StructuredSelection(resources[0]));
		}
	}

	@Override
	protected void createToolBar() {
		if(toolBarManager == null) {
			toolBarManager = new ToolBarManager(SWT.FLAT | SWT.WRAP);
			toolBarManager.add(AnalyseApplicationWindow.loadUnloadSubjectsAction);
			toolBarManager.add(new Separator());
			toolBarManager.add(new ExpandAllAction());
			toolBarManager.add(new CollapseAllAction());
			toolBarManager.add(new Separator());
			toolBarManager.add(new ExperimentsViewMenuAction());
			setTopRight(toolBarManager.createControl(this), SWT.RIGHT);
			toolBarManager.getControl().addFocusListener(this);
		}
	}

	private class ExperimentsViewLabelProvider implements ILabelProvider {
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
			if(element instanceof Experiment) return ((Experiment)element).getName();
			if(element instanceof Folder) return ((Folder)element).getName().replaceAll(Folder.EXTENSION + "$", "");
			if(element instanceof Chart) return ((Chart)element).getName().replaceAll(Chart.EXTENSION + "$", "") + " [" + ((Chart)element).getData().getChartType() + "]";
			if(element instanceof MultiCharts) return ((IResource)element).getName().replaceAll(Chart.EXTENSION + "$", "") + " [Multi Chart]";
			if(element instanceof Processing) return ((Processing)element).getName().replaceAll(Processing.EXTENSION + "$", "");
			if(element instanceof Note) return ((Note)element).getName().replaceAll(Note.EXTENSION + "$", "");
			return ((IResource)element).getName();
		}
		public Image getImage(Object element) {
			return  ((IResource)element).getImage();
		}
	}
	
	private class ExperimentsViewLabelDecorator implements ILabelDecorator {
		public Image decorateImage(Image image, Object element) {	
			IResource resource = (IResource)element;
			if(resource instanceof Subject) {
				Subject subject = (Subject) resource;
				if(subject.isLoaded() | subject.isModified() )
				return (new OverlayImage(image,subject.isModified(),subject.isLoaded(),false, false )).createImage();
			}
			if(resource instanceof Processing) {
				Processing processing = (Processing) resource;
				if(!processing.validate(false)) return (new OverlayImage(image,false,false,processing.hasError(), processing.hasWarning() )).createImage();
			}
			if(resource instanceof Folder) {
				Folder folder = (Folder) resource;
				if(!folder.validate(false)) return (new OverlayImage(image,false,false,folder.hasError(), folder.hasWarning() )).createImage();
			}
			if(resource instanceof Experiment) {
				Experiment experiment = (Experiment) resource;
				boolean hasError = false;
				boolean hasWarning = false;
				if(!experiment.validate(false)) {
					hasError = experiment.hasError();
					hasWarning = experiment.hasWarning();
				}
				boolean hasLoadedSubject = false;
				boolean hasModifiedSubject = false;
				IResource[] chidldren = experiment.getChildren();
				for (int i = 0; i < chidldren.length; i++) {
					IResource childResource = chidldren[i];
					if(childResource instanceof Subject) {
						hasLoadedSubject = hasLoadedSubject || ((Subject)childResource).isLoaded();
						hasModifiedSubject = hasModifiedSubject || ((Subject)childResource).isModified();
					}
				}
				if(hasError || hasWarning || hasLoadedSubject || hasModifiedSubject)
				return (new OverlayImage(image, hasModifiedSubject, hasLoadedSubject, hasError, hasWarning)).createImage();
			}
			return null;
		}
		public String decorateText(String text, Object element) {			
			if(((IResource)element instanceof Experiment))			
			return text + " - [" + ((Experiment)element).getType() + "]";
			else return null;
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
	
	private class OverlayImage extends CompositeImageDescriptor {
		private Image baseImage;
		private boolean modified;
		private boolean isLoadedInMatlab;
		private boolean error;
		private boolean warning;
		public OverlayImage(Image baseImage, boolean modified, boolean isLoadedInMalab, boolean error, boolean warning) {
			this.baseImage = baseImage;
			this.modified = modified;
			this.isLoadedInMatlab = isLoadedInMalab;
			this.error = error;
			this.warning = warning;
		}
		protected void drawCompositeImage(int width, int height) {
			// To draw a composite image, the base image should be 
			// drawn first (first layer) and then the overlay image 
			// (second layer) 
			// Draw the base image using the base image's image data 
			drawImage(baseImage.getImageData(), 0, 0);			
			// Method to create the overlay image data 
			// Get the image data from the Image store or by other means ImageData overlayImageData = overlayImageDescriptor.getImageData();
			// Overlaying the icon in the top left corner i.e. x and y 
			// coordinates are both zero 
			if(isLoadedInMatlab)
			drawImage(ImagesUtils.getImageDescriptor(IImagesKeys.LOADED_SUBJECT_ICON_DECORATOR).getImageData(), 10, 8);
			if(modified)
			drawImage(ImagesUtils.getImageDescriptor(IImagesKeys.MODIFIED_SUBJECT_ICON_DECORATOR).getImageData(), 0, -1);
			if(error)
			drawImage(ImagesUtils.getImageDescriptor(IImagesKeys.ERROR_ICON_DECORATOR).getImageData(), 8, 0);
			if(warning)
			drawImage(ImagesUtils.getImageDescriptor(IImagesKeys.WARNING_ICON_DECORATOR).getImageData(), -1, 6);
		}
		protected Point getSize() {
			return  new Point(baseImage.getBounds().width , baseImage.getBounds().height );
		}		
	}

	public void freeze() {
		experimentsTreeViewer.getTree().setEnabled(false);
	}
	
	public void unFreeze() {
		experimentsTreeViewer.getTree().setEnabled(true);
	}

	public void refresh() {
		experimentsTreeViewer.refresh();
	}
	
}
