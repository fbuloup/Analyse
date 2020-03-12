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
package analyse.gui;

import mathengine.MathEngineFactory;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Subject;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public final class ChannelsView extends View implements IResourceObserver, ISelectionChangedListener {

	private static CTabItem signalsExplorer;
	private static CTabItem categoriesExplorer;
	private static CTabItem eventsExplorer;
	private static CTabItem previewSignalsExplorer;
	private static Subject selectedSubject;
	protected static ActionContributionItem deleteChannelsAction;
	
	public final static int SHOW_SIGNALS_EXPLORER = 1;
	public final static int SHOW_CATEGORIES_EXPLORER = 2;
	public final static int SHOW_EVENTS_EXPLORER = 2;
	public final static int SHOW_PREVIEW_SIGNAL_EXPLORER = 4;
	public final static int SHOW_ALL = 5;

	private final class DeleteChannelsAction extends Action implements SelectionListener, ISelectionChangedListener {
		
		private String[] selectedSignals = new String[0];
		private String[] selectedCategories = new String[0];
		private String[] selectedEvents = new String[0];
		private CTabItem selectedTabItem;
		private Subject selectedSubject;
		
		public DeleteChannelsAction() {
			setToolTipText(Messages.getString("ChannelsView.DeleteChannelsActionTitle"));
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.DELETE_ICON));
			setEnabled(false);
		}
		@Override
		public void run() {
			CTabItem tabItem = getSelection();
			if(tabItem == signalsExplorer) {
				if(MessageDialog.openQuestion(getShell(), Messages.getString("ChannelsView.MessageDialogDeleteSignalsTitle"), Messages.getString("ChannelsView.MessageDialogDeleteSignalsText1") + selectedSignals.length + Messages.getString("ChannelsView.MessageDialogDeleteSignalsText2")))
					for (int i = 0; i < selectedSignals.length; i++) 
						if(!MathEngineFactory.getInstance().getMathEngine().deleteChannel(selectedSubject.getLocalPath(), selectedSignals[i]))
							Log.logErrorMessage(Messages.getString("ChannelsView.TrialsNumLabelTitle.ImpossibleToDeleteSignal") + selectedSubject.getLocalPath() + "." + selectedSignals[i] + " !");
						else selectedSubject.setModified(true);
			}
			if(tabItem == eventsExplorer) {
				if(MessageDialog.openQuestion(getShell(), Messages.getString("ChannelsView.MessageDialogDeleteEventTitle1"), Messages.getString("ChannelsView.MessageDialogDeleteEventText1") + selectedEvents.length + Messages.getString("ChannelsView.MessageDialogDeletEventText2")))
					for (int i = 0; i < selectedEvents.length; i++) 
						if(!MathEngineFactory.getInstance().getMathEngine().deleteChannel(selectedSubject.getLocalPath(), selectedEvents[i]))
							Log.logErrorMessage(Messages.getString("ChannelsView.TrialsNumLabelTitle.ImpossibleToDeleteEvent") + selectedSubject.getLocalPath() + "." + selectedEvents[i] + " !");
						else selectedSubject.setModified(true);
			}
			if(tabItem == categoriesExplorer) {
				if(MessageDialog.openQuestion(getShell(), Messages.getString("ChannelsView.MessageDialogDeleteCategoryTitle1"), Messages.getString("ChannelsView.MessageDialogDeleteCategoryText1") + selectedCategories.length + Messages.getString("ChannelsView.MessageDialogDeleteCategoryText2")))
					for (int i = 0; i < selectedCategories.length; i++) 
						if(!MathEngineFactory.getInstance().getMathEngine().deleteChannel(selectedSubject.getLocalPath(), selectedCategories[i]))
							Log.logErrorMessage(Messages.getString("ChannelsView.TrialsNumLabelTitle.ImpossibleToDeleteCategory") + selectedSubject.getLocalPath() + "." + selectedCategories[i] + " !");
						else selectedSubject.setModified(true);
			}
			Experiments.notifyObservers(IResourceObserver.CHANNEL_DELETED, new IResource[]{selectedSubject});
		}
		public void selectionChanged(SelectionChangedEvent event) {
			if(event.getSource() instanceof TreeViewer) {
				selectedSubject = null;
				Object[] objects = ((TreeSelection)event.getSelection()).toArray();
				for (int i = 0; i < objects.length; i++) {
					if(objects[i] instanceof Subject) {
						if(selectedSubject == null) selectedSubject = (Subject) objects[i];
						if(((Subject)objects[i]).isLoaded()) {
							selectedSubject = (Subject) objects[i];
							break;
						}
					}
				}
				selectedSignals = new String[0];
				selectedCategories = new String[0];
				setEnabled(false);
				selectedTabItem = getSelection();
			} else {
				IStructuredSelection selection = ((IStructuredSelection)event.getSelection());
				if(selectedTabItem == signalsExplorer) {
					selectedSignals = new String[selection.size()];
					Object[] elements = selection.toArray();
					for (int i = 0; i < elements.length; i++) selectedSignals[i] = (String) elements[i];
				}
				if(selectedTabItem == eventsExplorer) {
					selectedEvents = new String[selection.size()];
					Object[] elements = selection.toArray();
					for (int i = 0; i < elements.length; i++) selectedEvents[i] = (String) elements[i];
				}
				if(selectedTabItem == categoriesExplorer) {
					selectedCategories = new String[selection.size()];
					Object[] elements = selection.toArray();
					for (int i = 0; i < elements.length; i++) selectedCategories[i] = (String) elements[i];
				}
				setEnabled(selection.size() > 0);
			}
		}
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		public void widgetSelected(SelectionEvent e) {
			selectedTabItem = getSelection();
			setEnabled(false);
			if(selectedTabItem == signalsExplorer) setEnabled(selectedSignals.length > 0);
			if(selectedTabItem == eventsExplorer) setEnabled(selectedSignals.length > 0);
			if(selectedTabItem == categoriesExplorer) setEnabled(selectedCategories.length > 0);
		}
	}

	private boolean selectionListenerAlreadyRegistered;
	private SignalsExplorerContainer signalsExplorerContainer;
	private EventsExplorerContainer eventsExplorerContainer;
	private CategoriesExplorerContainer categoriesExplorerContainer;
	private PreviewSignalsExplorerContainer previewSignalsExplorerContainer;
	
	public ChannelsView(Composite parent, int style) {
		super(parent, style);
		initView(SHOW_ALL);
		addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				if(getItemCount() == 1)	{
					Experiments.getInstance().removeExperimentObserver(ChannelsView.this);
				}
			}
		});
	}

	@Override
	protected void initView(int showMode) {
		
		createToolBar();
		
		if(!selectionListenerAlreadyRegistered) {
			addSelectionListener((SelectionListener) deleteChannelsAction.getAction());
			selectionListenerAlreadyRegistered = true;
		}
		
		
		Experiments.getInstance().addExperimentObserver(this);
		AnalyseApplication.getAnalyseApplicationWindow().addSelectionChangedListenerToExperimentsView(this);
		
		AnalyseApplication.getAnalyseApplicationWindow().addSelectionChangedListenerToExperimentsView((ISelectionChangedListener) deleteChannelsAction.getAction());
		
		if(showMode == SHOW_SIGNALS_EXPLORER || showMode == SHOW_ALL) {
			boolean init = signalsExplorer == null;
			if(!init) init = signalsExplorer.isDisposed();
			if(init) {
				signalsExplorer = createTabItem();
				signalsExplorer.setText(Messages.getString("SignalsView.Title"));
				signalsExplorer.setImage(ImagesUtils.getImage(IImagesKeys.CHANNELS_VIEW_ICON));
				createSignalsExplorerContent();
				signalsExplorer.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						signalsExplorerContainer.dispose();
					}
				});
			}
		}
		
		if(showMode == SHOW_CATEGORIES_EXPLORER || showMode == SHOW_ALL) {
			boolean init = categoriesExplorer == null;
			if(!init) init = categoriesExplorer.isDisposed();
			if(init) {
				categoriesExplorer = createTabItem();
				categoriesExplorer.setText(Messages.getString("CategoriesView.Title"));
				categoriesExplorer.setImage(ImagesUtils.getImage(IImagesKeys.CATEGORY_VIEW_ICON));
				createCategoriesExplorerContent();
				categoriesExplorer.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						categoriesExplorerContainer.dispose();
					}
				});
			}
		}
		
		if(showMode == SHOW_EVENTS_EXPLORER || showMode == SHOW_ALL) {
			boolean init = eventsExplorer == null;
			if(!init) init = eventsExplorer.isDisposed();
			if(init) {
				eventsExplorer = createTabItem();
				eventsExplorer.setText(Messages.getString("EventsView.Title"));
				eventsExplorer.setImage(ImagesUtils.getImage(IImagesKeys.EVENTS_VIEW_ICON));
				createEventsExplorerContent();
				eventsExplorer.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						eventsExplorer.dispose();
					}
				});
			}
		}
		
		if(showMode == SHOW_PREVIEW_SIGNAL_EXPLORER /*|| showMode == SHOW_ALL*/) {
			boolean init = previewSignalsExplorer == null;
			if(!init) init = previewSignalsExplorer.isDisposed();
			if(init) {
				previewSignalsExplorer = createTabItem();
				previewSignalsExplorer.setText(Messages.getString("SignalsPreviewView.Title"));
				previewSignalsExplorer.setImage(ImagesUtils.getImage(IImagesKeys.PREVIEW_VIEW_ICON));
				createPreviewSignalsExplorer();
				previewSignalsExplorer.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						previewSignalsExplorerContainer.dispose();
					}
				});
			}
		}
		
		Event event = new Event();
		event.doit = true;
		event.display = getDisplay();
		event.widget = this;
		if((showMode == SHOW_SIGNALS_EXPLORER) || showMode == SHOW_ALL) {
			event.item = signalsExplorer;
			setSelection(signalsExplorer);
		}
		if(showMode == SHOW_CATEGORIES_EXPLORER) {
			event.item = categoriesExplorer;
			setSelection(categoriesExplorer);
		}
		if(showMode == SHOW_EVENTS_EXPLORER) {
			event.item = eventsExplorer;
			setSelection(eventsExplorer);
		}
		if(showMode == SHOW_PREVIEW_SIGNAL_EXPLORER) {
			event.item = previewSignalsExplorer;
			setSelection(previewSignalsExplorer);
		}
		notifyListeners(SWT.Selection, event);
	}
	
	private void createSignalsExplorerContent() {
		signalsExplorerContainer = new SignalsExplorerContainer(this);
		signalsExplorer.setControl(signalsExplorerContainer);
		((SignalsExplorerContainer)signalsExplorer.getControl()).update(IResourceObserver.SELECTION_CHANGED, selectedSubject);
	}
	
	private void createEventsExplorerContent() {
		eventsExplorerContainer = new EventsExplorerContainer(this);
		eventsExplorer.setControl(eventsExplorerContainer);
		((EventsExplorerContainer)eventsExplorer.getControl()).update(IResourceObserver.SELECTION_CHANGED, selectedSubject);
	}

	private void createCategoriesExplorerContent() {
		categoriesExplorerContainer = new CategoriesExplorerContainer(this);
		categoriesExplorer.setControl(categoriesExplorerContainer);
		((CategoriesExplorerContainer)categoriesExplorer.getControl()).update(IResourceObserver.SELECTION_CHANGED, selectedSubject);
	}

	private void createPreviewSignalsExplorer() {
		previewSignalsExplorerContainer = new PreviewSignalsExplorerContainer(this);
		previewSignalsExplorer.setControl(previewSignalsExplorerContainer);
		((PreviewSignalsExplorerContainer)previewSignalsExplorer.getControl()).update(IResourceObserver.SELECTION_CHANGED, selectedSubject);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		selectedSubject = null;
		Object[] objects = ((TreeSelection)event.getSelection()).toArray();
		for (int i = 0; i < objects.length; i++) {
			if(objects[i] instanceof Subject) {
				if(selectedSubject == null) selectedSubject = (Subject) objects[i];
				if(((Subject)objects[i]).isLoaded()) {
					selectedSubject = (Subject) objects[i];
					break;
				}
			}
		}
		update(IResourceObserver.SELECTION_CHANGED, new IResource[]{selectedSubject});
	}
	
	public void update(int message, IResource[] resources) {
		boolean update = false;
		for (int i = 0; i < resources.length; i++) {
			if(resources[i] instanceof Subject) {
				if(resources[i] == selectedSubject) {
					update = true;
					if(message == IResourceObserver.DELETED) selectedSubject = null;
					break;
				}
			}
		}
		if(message == IResourceObserver.PROCESS_RUN) update = true;
		update = update || (resources.length == 1 && resources[0] == null);
		if(update) {
				if(signalsExplorer != null)
					if(!signalsExplorer.isDisposed()) {
//						long t1 = System.currentTimeMillis();
						((SignalsExplorerContainer)signalsExplorer.getControl()).update(message, selectedSubject);
//						t1 = System.currentTimeMillis() - t1;
//						System.out.println(">>>>>>>>>> Time to update signalExplorer : " + t1);
					}
				if(eventsExplorer != null)
					if(!eventsExplorer.isDisposed()) {
//						long t1 = System.currentTimeMillis();
						((EventsExplorerContainer)eventsExplorer.getControl()).update(message, selectedSubject);
//						t1 = System.currentTimeMillis() - t1;
//						System.out.println(">>>>>>>>>> Time to update signalExplorer : " + t1);
					}
				if(categoriesExplorer != null)
					if(!categoriesExplorer.isDisposed()) {
//						long t1 = System.currentTimeMillis();
						((CategoriesExplorerContainer)categoriesExplorer.getControl()).update(message, selectedSubject);
//						t1 = System.currentTimeMillis() - t1;
//						System.out.println(">>>>>>>>>> Time to update categoriesExplorer : " + t1);
					}
				if(previewSignalsExplorer != null)
					if(!previewSignalsExplorer.isDisposed()) {
//						long t1 = System.currentTimeMillis();
						((PreviewSignalsExplorerContainer)previewSignalsExplorer.getControl()).update(message, selectedSubject);
//						t1 = System.currentTimeMillis() - t1;
//						System.out.println(">>>>>>>>>> Time to update previewSignalsExplorer : " + t1);
					}
		} 
	}
	
	@Override
	protected void createToolBar() {
		if(deleteChannelsAction == null) {
			deleteChannelsAction = new ActionContributionItem((IAction)(new DeleteChannelsAction()));
			ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT | SWT.WRAP);
			toolBarManager.add(deleteChannelsAction);
			setTopRight(toolBarManager.createControl(this), SWT.RIGHT);
			toolBarManager.getControl().addFocusListener(this);
		}
	}

	public void freeze() {
		if(signalsExplorer != null && !signalsExplorer.isDisposed()) signalsExplorer.getControl().setEnabled(false);
		if(eventsExplorer != null && !eventsExplorer.isDisposed()) eventsExplorer.getControl().setEnabled(false);
		if(categoriesExplorer != null && !categoriesExplorer.isDisposed()) categoriesExplorer.getControl().setEnabled(false);
		if(previewSignalsExplorer != null && !previewSignalsExplorer.isDisposed()) previewSignalsExplorer.getControl().setEnabled(false);
	}
	
	public void unFreeze() {
		if(signalsExplorer != null && !signalsExplorer.isDisposed()) signalsExplorer.getControl().setEnabled(true);
		if(eventsExplorer != null && !eventsExplorer.isDisposed()) eventsExplorer.getControl().setEnabled(true);
		if(categoriesExplorer != null && !categoriesExplorer.isDisposed()) categoriesExplorer.getControl().setEnabled(true);
		if(previewSignalsExplorer != null && !previewSignalsExplorer.isDisposed()) previewSignalsExplorer.getControl().setEnabled(true);
	}
}
