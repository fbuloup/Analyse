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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mathengine.MathEngineFactory;

import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ftpsharing.FTPShareExperimentsServer;
import functioneditor.controllers.FunctionEditorController;

import analyse.Log;
import analyse.SplashScreen;
import analyse.actions.AboutAction;
import analyse.actions.ClearActiveEditorAction;
import analyse.actions.CopyAction;
import analyse.actions.DeleteAction;
import analyse.actions.ExitAction;
import analyse.actions.ExportAction;
import analyse.actions.HelpAction;
import analyse.actions.ImportAction;
import analyse.actions.LoadUnloadSubjectsAction;
import analyse.actions.NewChartAction;
import analyse.actions.NewExperimentAction;
import analyse.actions.NewFolderAction;
import analyse.actions.NewNoteAction;
import analyse.actions.NewProcessAction;
import analyse.actions.NewSubjectAction;
import analyse.actions.OpenAction;
import analyse.actions.OpenFunctionEditorAction;
import analyse.actions.PasteAction;
import analyse.actions.PreferencesAction;
import analyse.actions.OpenCheatSheetAction;
import analyse.actions.RandomizeSeriesColorsAction;
import analyse.actions.RefactorAction;
import analyse.actions.RefreshActiveChartAction;
import analyse.actions.RenameAction;
import analyse.actions.RestartAction;
import analyse.actions.RestartWithLogAction;
import analyse.actions.RunBatchProcessingAction;
import analyse.actions.RunProcessingAction;
import analyse.actions.SaveAction;
import analyse.actions.SaveAllAction;
import analyse.actions.ShowCategoriesViewAction;
import analyse.actions.ShowEventsViewAction;
import analyse.actions.ShowPreviewSignalsViewAction;
import analyse.actions.ShowSharingViewAction;
import analyse.actions.ShowSignalsViewAction;
import analyse.actions.ShowMathConsoleViewAction;
import analyse.actions.ShowMessagesConsoleViewAction;
import analyse.actions.ShowExperimentsViewAction;
import analyse.actions.StartStopMathEngineAction;
import analyse.actions.UpdateAction;
import analyse.gui.dialogs.SaveSubjectsDialog;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.Subject;
import analyse.preferences.AnalysePreferences;
import analyse.resources.CPlatform;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public final class AnalyseApplicationWindow extends CApplicationWindow implements ControlListener {
	
	private static ExperimentsView experimentsView;
	private static ChannelsView channelsView;
	private static EditorsView editorsView;
	private static ConsolesView consolesView;
	private static CheatSheetsView cheatSheetsView;
	private static SashForm sashFormMain;
	private static SashForm sashFormLeft;
	private static SashForm sashFormRight;
	
	private final static class SashFormControlListener implements ControlListener {
		public void controlMoved(ControlEvent e) {
		}
		public void controlResized(ControlEvent e) {
			IPreferenceStore preferenceStore = AnalysePreferences.getPreferenceStore();
			int value = sashFormMain.getWeights()[0]/10;
			preferenceStore.setValue(AnalysePreferences.LEFT_SASH_WIDTH, value);
			value = sashFormLeft.getWeights()[1]/10;
			preferenceStore.setValue(AnalysePreferences.LEFT_SASH_HEIGHT, value);
			value = sashFormRight.getWeights()[0]/10;
			preferenceStore.setValue(AnalysePreferences.EDITORS_VIEW_SASH_HEIGHT, value);
			if(sashFormMain.getWeights().length == 3) {
				value = sashFormMain.getWeights()[2]/10;
				preferenceStore.setValue(AnalysePreferences.CHEATSHEET_VIEW_WIDTH, value);
			}
		}
	}

	private static ExportAction exportAction;
	private static ImportAction importAction;
	
	private static ExitAction exitAction;
	private static PreferencesAction preferencesAction;
	private static RestartAction restartAction;
	private static RestartWithLogAction restartWithLogAction;
	private static AboutAction aboutAction;
	
	protected static NewExperimentAction newExperimentAction;
	protected static NewSubjectAction newSubjectAction;
	protected static NewFolderAction newFolderAction;
	protected static DeleteAction deleteAction;
	protected static RenameAction renameAction;
	protected static CopyAction copyAction;
	protected static PasteAction pasteAction;
	protected static NewChartAction newChartAction;
	protected static NewProcessAction newProcessAction;
	protected static NewNoteAction newNoteAction; 
	
	protected static OpenAction openAction;
	protected static LoadUnloadSubjectsAction loadUnloadSubjectsAction;
	
	private static ShowExperimentsViewAction showExperimentsViewAction;
	private static ShowSignalsViewAction showSignalsViewAction;
	private static ShowCategoriesViewAction showCategoriesViewAction;
	private static ShowEventsViewAction showEventsViewAction;
	private static ShowPreviewSignalsViewAction showPreviewSignalsViewAction;
	private static ShowMessagesConsoleViewAction showMessagesConsoleViewAction;
	private static ShowMathConsoleViewAction showMathConsoleViewAction;
	private static ShowSharingViewAction showSharingViewAction;
	protected static SaveAction saveAction;
	protected static SaveAllAction saveAllAction;
	public static StartStopMathEngineAction startStopMathEngineAction;
	
	protected static RefreshActiveChartAction refreshActiveChartAction;
	protected static RandomizeSeriesColorsAction randomizeSeriesColorsAction;
	protected static ClearActiveEditorAction clearActiveEditorAction;
	
	private static OpenFunctionEditorAction openFunctionEditorAction;
	
	public static RunProcessingAction runProcessingAction;
	public static RunBatchProcessingAction runBatchProcessingAction;

	private static UpdateAction updateAction;
	
	protected static RefactorAction refactorAction;
	private static HelpAction helpAction;
	
	public AnalyseApplicationWindow(Shell parentShell) {
		super(parentShell);
		
		exportAction = new ExportAction();
		importAction = new ImportAction();
		
		preferencesAction = new PreferencesAction();
		restartAction = new RestartAction();
		restartWithLogAction = new RestartWithLogAction();
		exitAction = new ExitAction();
		
		aboutAction = new AboutAction();
		
		newExperimentAction = new NewExperimentAction();
		newSubjectAction = new NewSubjectAction();
		newFolderAction = new NewFolderAction();
		newChartAction = new NewChartAction();
		newProcessAction = new NewProcessAction();
		newNoteAction = new NewNoteAction();
		openAction = new OpenAction();
		deleteAction = new DeleteAction();
		renameAction = new RenameAction();
		copyAction = new CopyAction();
		pasteAction = new PasteAction();
		
		loadUnloadSubjectsAction = new LoadUnloadSubjectsAction(); 
		
		showExperimentsViewAction = new ShowExperimentsViewAction();
		showSignalsViewAction = new ShowSignalsViewAction();
		showCategoriesViewAction = new ShowCategoriesViewAction();
		showPreviewSignalsViewAction = new ShowPreviewSignalsViewAction();
		showMessagesConsoleViewAction = new ShowMessagesConsoleViewAction();
		showEventsViewAction = new ShowEventsViewAction();
		showMathConsoleViewAction = new ShowMathConsoleViewAction();
		showSharingViewAction = new ShowSharingViewAction();
		
		saveAction = new SaveAction();
		saveAllAction = new SaveAllAction();
		startStopMathEngineAction = new StartStopMathEngineAction();
		
		refreshActiveChartAction = new RefreshActiveChartAction();
		randomizeSeriesColorsAction = new RandomizeSeriesColorsAction();
		clearActiveEditorAction = new ClearActiveEditorAction();
		
		openFunctionEditorAction = new OpenFunctionEditorAction();
		
		updateAction = new UpdateAction();
		
		refactorAction = new RefactorAction();
		
		runProcessingAction = new RunProcessingAction();
		runBatchProcessingAction = new RunBatchProcessingAction();
		
		helpAction = new HelpAction();
		
		addMenuBar();
		addCoolBar(SWT.FLAT);
		addStatusLine();
	}
	
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Analyse - " + AnalysePreferences.getCurrentWorkspace());
		shell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		shell.addControlListener(this);
	}
	
	@Override
	protected Control createContents(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1,false));
		GridLayout layout = (GridLayout) container.getLayout();
		layout.marginHeight = 0;
		
		sashFormMain = new SashForm(container,SWT.HORIZONTAL);
		sashFormMain.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		sashFormLeft = new SashForm(sashFormMain,SWT.VERTICAL);
		experimentsView = new ExperimentsView(sashFormLeft,SWT.NONE);
		channelsView = new ChannelsView(sashFormLeft,SWT.NONE);
		SplashScreen.work(1);
		sashFormRight = new SashForm(sashFormMain,SWT.VERTICAL);
		editorsView = new EditorsView(sashFormRight,SWT.NONE);
		consolesView = new ConsolesView(sashFormRight,SWT.NONE);
		
		showExperimentsViewAction.setView(experimentsView);
		showSignalsViewAction.setView(channelsView);
		showCategoriesViewAction.setView(channelsView);
		showEventsViewAction.setView(channelsView);
		showPreviewSignalsViewAction.setView(channelsView);
		showMessagesConsoleViewAction.setView(consolesView);
		showMathConsoleViewAction.setView(consolesView);
		showSharingViewAction.setView(consolesView);
		
		experimentsView.setFocus();
		
		Experiments.getInstance().registerToExperimentsObservers();
		
		SplashScreen.close();
		
		return super.createContents(parent);
	}
	
	@Override
	protected void initializeBounds() {
		IPreferenceStore preferenceStore = AnalysePreferences.getPreferenceStore();
		Rectangle rect = new Rectangle(preferenceStore.getInt(AnalysePreferences.ANALYSE_WINDOW_POSITION_LEFT), 
									   preferenceStore.getInt(AnalysePreferences.ANALYSE_WINDOW_POSITION_TOP),
									   preferenceStore.getInt(AnalysePreferences.ANALYSE_WINDOW_WIDTH),
									   preferenceStore.getInt(AnalysePreferences.ANALYSE_WINDOW_HEIGHT));		
		boolean maximized = preferenceStore.getBoolean(AnalysePreferences.ANALYSE_WINDOW_MAXIMIZED);
		getShell().setBounds(rect);
		getShell().setMaximized(maximized);
		
		int value = preferenceStore.getInt(AnalysePreferences.LEFT_SASH_WIDTH);
		sashFormMain.setWeights(new int[]{value,100 - value});
		value = preferenceStore.getInt(AnalysePreferences.LEFT_SASH_HEIGHT);
		sashFormLeft.setWeights(new int[]{100-value, value});
		value = preferenceStore.getInt(AnalysePreferences.EDITORS_VIEW_SASH_HEIGHT);
		sashFormRight.setWeights(new int[]{value,100 - value});	
		
		SashFormControlListener sashFormControlListener = new SashFormControlListener();
		experimentsView.addControlListener(sashFormControlListener);
		editorsView.addControlListener(sashFormControlListener);

	}
	
	protected static void updateLayout() {
		if(!experimentsView.isVisible() && !channelsView.isVisible())
			sashFormMain.setMaximizedControl(sashFormRight);
		if(!experimentsView.isVisible() && channelsView.isVisible()) sashFormLeft.setMaximizedControl(channelsView);
		if(experimentsView.isVisible() && !channelsView.isVisible()) sashFormLeft.setMaximizedControl(experimentsView);
		if(!consolesView.isVisible()) sashFormRight.setMaximizedControl(editorsView);
		sashFormMain.layout();
		sashFormLeft.layout();
		sashFormRight.layout();
	}

	public static void setMaximizedControl(Control control) {
		if(control == sashFormMain) return;
		if(control == null) {
			boolean visibles = experimentsView.isVisible() || channelsView.isVisible();
			if(visibles) sashFormMain.setMaximizedControl(null);
			return;
		}
		sashFormMain.setMaximizedControl(control);
	}

	public void controlMoved(ControlEvent e) {
		if(!getShell().getMaximized()) {
			AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.ANALYSE_WINDOW_POSITION_LEFT, getShell().getBounds().x);
			AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.ANALYSE_WINDOW_POSITION_TOP, getShell().getBounds().y);
		}
	}

	public void controlResized(ControlEvent e) {
		IPreferenceStore preferenceStore = AnalysePreferences.getPreferenceStore();
		if(!getShell().getMaximized()) {
			preferenceStore.setValue(AnalysePreferences.ANALYSE_WINDOW_HEIGHT, getShell().getBounds().height);
			preferenceStore.setValue(AnalysePreferences.ANALYSE_WINDOW_WIDTH, getShell().getBounds().width);
		}
		preferenceStore.setValue(AnalysePreferences.ANALYSE_WINDOW_MAXIMIZED, getShell().getMaximized());
	}
	
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager();
		if ( CPlatform.isMacOSX() ) {
			Listener quitListener = new Listener() {
				public void handleEvent(Event event) {
					event.doit = close();
				}
			};
			if(CPlatform.isPPCArch()) {
				CarbonUIEnhancer enhancer = new CarbonUIEnhancer( Messages.getString("AnalyseWindow.AboutAnalyseTitle"));
		        enhancer.hookApplicationMenu( Display.getDefault(), quitListener, aboutAction, preferencesAction );
			} else {
				CocoaUIEnhancer enhancer = new CocoaUIEnhancer(Messages.getString("AnalyseWindow.AboutAnalyseTitle"));
		        enhancer.hookApplicationMenu( Display.getDefault(), quitListener, aboutAction, preferencesAction );
			}
	    } else {
	    	MenuManager analyseMenuManager = new MenuManager(Messages.getString("AnalyseWindow.AnalyseMenuTitle"));
			menuManager.add(analyseMenuManager);
			analyseMenuManager.add(preferencesAction);
			analyseMenuManager.add(new Separator());		
			analyseMenuManager.add(restartAction);	
			analyseMenuManager.add(restartWithLogAction);
			analyseMenuManager.add(new Separator());		
			analyseMenuManager.add(exitAction);
	    }
		
		MenuManager experimentsMenu = new MenuManager(Messages.getString("AnalyseWindow.ExpermimentsMenuTitle"));
		menuManager.add(experimentsMenu);		
		experimentsMenu.add(newExperimentAction);
		experimentsMenu.add(newSubjectAction);
		experimentsMenu.add(new Separator());
		experimentsMenu.add(newFolderAction);
		experimentsMenu.add(newChartAction);
		experimentsMenu.add(newProcessAction);
		experimentsMenu.add(newNoteAction);
		experimentsMenu.add(new Separator());
		experimentsMenu.add(loadUnloadSubjectsAction);
		experimentsMenu.add(new Separator());	
		experimentsMenu.add(exportAction);	
		experimentsMenu.add(importAction);	
		experimentsMenu.add(new Separator());	
		experimentsMenu.add(runProcessingAction);
		experimentsMenu.add(runBatchProcessingAction);
		experimentsMenu.add(new Separator());	
		experimentsMenu.add(refactorAction);
		experimentsMenu.add(new Separator());
		experimentsMenu.add(saveAction);
		experimentsMenu.add(saveAllAction);
		experimentsMenu.add(new Separator());
		experimentsMenu.add(openAction);
		experimentsMenu.add(new Separator());
		experimentsMenu.add(copyAction);
		experimentsMenu.add(pasteAction);
		experimentsMenu.add(new Separator());
		experimentsMenu.add(renameAction);
		experimentsMenu.add(new Separator());
		experimentsMenu.add(deleteAction);
		
		MenuManager toolsMenuManager = new MenuManager(Messages.getString("AnalyseWindow.ToolsMenuTitle"));
		menuManager.add(toolsMenuManager);
		toolsMenuManager.add(openFunctionEditorAction);
		toolsMenuManager.add(new Separator());
		toolsMenuManager.add(startStopMathEngineAction);
		toolsMenuManager.add(clearActiveEditorAction);
		toolsMenuManager.add(new Separator());
		toolsMenuManager.add(refreshActiveChartAction);
		toolsMenuManager.add(randomizeSeriesColorsAction);
		if ( CPlatform.isMacOSX() ) {
			toolsMenuManager.add(new Separator());		
			toolsMenuManager.add(restartAction);	
			toolsMenuManager.add(restartWithLogAction);
		}
		
		MenuManager viewsMenuManager = new MenuManager(Messages.getString("AnalyseWindow.ViewMenuTitle"));
		menuManager.add(viewsMenuManager);
		viewsMenuManager.add(showExperimentsViewAction);
		viewsMenuManager.add(new Separator());
		viewsMenuManager.add(showSignalsViewAction);
		viewsMenuManager.add(showCategoriesViewAction);
		viewsMenuManager.add(showEventsViewAction);
		viewsMenuManager.add(showPreviewSignalsViewAction);
		viewsMenuManager.add(new Separator());
		viewsMenuManager.add(showMessagesConsoleViewAction);
		viewsMenuManager.add(showMathConsoleViewAction);
		viewsMenuManager.add(showSharingViewAction);
		
		MenuManager helpMenuManager = new MenuManager(Messages.getString("AnalyseWindow.HelpMenuTitle"));
		menuManager.add(helpMenuManager);
		helpMenuManager.add(helpAction);
		helpMenuManager.add(new Separator());
		helpMenuManager.add(updateAction);
		
		Document helpDocument = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();	
			String termination = "." + AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.LANGUAGE) + ".xml";
			File cheatSheetsListFile = new File("./help/cheatsheets/cheatsheetslist" + termination);//." + ;
			InputStream helpInputStream = new FileInputStream(cheatSheetsListFile); 
			helpDocument = builder.parse(helpInputStream);
		} catch (ParserConfigurationException e) {
			Log.logErrorMessage(e);
		} catch (FileNotFoundException e) {
			Log.logErrorMessage(e);
		} catch (SAXException e) {
			Log.logErrorMessage(e);
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}
		Node rootNode = helpDocument.getFirstChild();
		NodeList nodesList = rootNode.getChildNodes();
		HashMap<String, String> cheatSheetsInfos = new HashMap<String, String>(0);
		for (int i = 0; i < nodesList.getLength(); i++) {
			Node node = nodesList.item(i);
			if(node instanceof Element) {
				File cheatSheetFile = new File("help/cheatsheets/" + node.getAttributes().getNamedItem("filename").getNodeValue());
				cheatSheetsInfos.put(node.getAttributes().getNamedItem("title").getNodeValue(), cheatSheetFile.getAbsolutePath());
			}
		}
		if(cheatSheetsInfos.size() > 0) {
			helpMenuManager.add(new Separator());
			String[] titles = cheatSheetsInfos.keySet().toArray(new String[cheatSheetsInfos.size()]);
			Arrays.sort(titles);
			for (int i = 0; i < titles.length; i++) {
				String fileName = cheatSheetsInfos.get(titles[i]);
				helpMenuManager.add(new OpenCheatSheetAction(titles[i], fileName));
			}
		}
		
		if (!CPlatform.isMacOSX() ) {
			helpMenuManager.add(new Separator());
			helpMenuManager.add(aboutAction);
		}

		return menuManager;
	}
	
	@Override
	protected CoolBarManager createCoolBarManager(int style) {
		CoolBarManager coolBarManager = new CoolBarManager(style);
		
		ToolBarManager toolBarManager1 = new ToolBarManager(style);
		toolBarManager1.add(newExperimentAction);	
		toolBarManager1.add(newSubjectAction);	
		toolBarManager1.add(newFolderAction);	
		toolBarManager1.add(newChartAction);
		toolBarManager1.add(newProcessAction);
		toolBarManager1.add(newNoteAction);

		toolBarManager1.add(new Separator());
		toolBarManager1.add(startStopMathEngineAction);
		toolBarManager1.add(loadUnloadSubjectsAction);

		toolBarManager1.add(new Separator());
		toolBarManager1.add(exportAction);
		toolBarManager1.add(importAction);
		
		toolBarManager1.add(new Separator());
		toolBarManager1.add(runProcessingAction);
		
		toolBarManager1.add(new Separator());
		toolBarManager1.add(refactorAction);
		
		toolBarManager1.add(new Separator());
		toolBarManager1.add(saveAction);
		toolBarManager1.add(saveAllAction);
		
		coolBarManager.add(toolBarManager1);

		return coolBarManager;
	}

	public void showView(View view, int showMode) {
		if(sashFormLeft.getMaximizedControl() != null) {
			View maximizedView = ((View)sashFormLeft.getMaximizedControl());
			if(view != maximizedView) maximizedView.restore(null);
		}
		if(sashFormRight.getMaximizedControl() != null) {
			View maximizedView = ((View)sashFormRight.getMaximizedControl());
			if(view != maximizedView) maximizedView.restore(null);
		}
		if(!view.getVisible()) {
			view.setVisible(true);
			view.restore(null);
			updateLayout();
		}
		view.initView(showMode);
		view.setFocus();
	}

	public static ConsolesView getConsolesView() {
		return consolesView;
	}

	public void openEditor(IResource resource) {
		editorsView.openResource(resource);
		editorsView.setFocus();
	}
	
	private int saveModifiedSubjects() {
		ArrayList<Subject> modifiedSubject = new ArrayList<Subject>(0);
		IResource[] resources = Experiments.getInstance().getChildren();
		for (int i = 0; i < resources.length; i++) {
			IResource[] resources2 = resources[i].getChildren();
			for (int j = 0; j < resources2.length; j++) 
				if(resources2[j] instanceof Subject) if(((Subject)resources2[j]).isModified()) modifiedSubject.add((Subject) resources2[j]);
		}
		
		int response = Window.OK;
		if(modifiedSubject.size() > 0) {
			response = new SaveSubjectsDialog(null, modifiedSubject.toArray(new Subject[modifiedSubject.size()])).open();
		}
		return response;
	}
	
	@SuppressWarnings("finally")
	@Override
	public boolean close() {
		
		if(FunctionEditorController.functionEditorOpened) {
			MessageDialog.openInformation(getShell(), "Information", Messages.getString("MessageDialog.QuitFunctionEditorFirst"));
			return false;
		}
		
		int response = saveModifiedSubjects();
		if(response == Window.OK) {
			try {
				if(MathEngineFactory.getInstance().getMathEngine() != null)	MathEngineFactory.getInstance().getMathEngine().stop();
				if(FTPShareExperimentsServer.isStarted()) FTPShareExperimentsServer.stop();
			} finally {
				return super.close();
			}
		}
		return false;
	}
	
	public void addSelectionChangedListenerToExperimentsView(ISelectionChangedListener selectionChangedListener) {
		if(experimentsView != null) experimentsView.addSelectionChangedListener(selectionChangedListener);
	}
	
	public void removeSelectionChangedListenerFromExperimentsView(ISelectionChangedListener selectionChangedListener) {
		if(experimentsView != null) experimentsView.removeSelectionChangedListener(selectionChangedListener);
	}

	public void run(boolean freezeUI, final boolean fork, boolean cancelable, boolean inProgressDialog, final IRunnableWithProgress runnable) {
		try {
			if(!inProgressDialog) {
				if(freezeUI) freezeUI();
				//Force fork to false in order to run with JACOB COM Bridge
				super.run(false, cancelable, runnable);
			} else {
				ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(getShell());
				//Force fork to false in order to run with JACOB COM Bridge
				progressMonitorDialog.run(false, cancelable, runnable);
			}
		} catch (InvocationTargetException e) {
			Log.logErrorMessage(e);
		} catch (InterruptedException e) {
			Log.logErrorMessage(e);
		} finally {
			if(freezeUI) unFreezeUI();
		}
	}
	
	public void freezeUI() {
		startStopMathEngineAction.setEnabled(false);
		experimentsView.freeze();
		channelsView.freeze();
		consolesView.freeze();
		editorsView.freeze();
	}
	
	public void unFreezeUI() {
		startStopMathEngineAction.setEnabled(true);
		experimentsView.unFreeze();
		channelsView.unFreeze();
		consolesView.unFreeze();
		editorsView.unFreeze();
	}
	
	public static void refreshExperimentsView() {
		experimentsView.refresh();
	}

	public void openCheatSheet(String filePath) {
		boolean create = cheatSheetsView == null;
		if(cheatSheetsView != null) create = cheatSheetsView.isDisposed();
		if(create) cheatSheetsView = new CheatSheetsView(sashFormMain, SWT.NONE);
		cheatSheetsView.setVisible(true);
		int value = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.LEFT_SASH_WIDTH);
		int value2 = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.CHEATSHEET_VIEW_WIDTH);
		sashFormMain.setWeights(new int[]{value,100 - value - value2,value2});
		cheatSheetsView.showCheatSheet(filePath);
		cheatSheetsView.setFocus();
	}
}
