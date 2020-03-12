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
package functioneditor.windows;

import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import functioneditor.actions.CopyAction;
import functioneditor.actions.CutAction;
import functioneditor.actions.DeleteAction;
import functioneditor.actions.FindAndReplaceAction;
import functioneditor.actions.FontAction;
import functioneditor.actions.NewFileAction;
import functioneditor.actions.NewFolderAction;
import functioneditor.actions.OpenFileAction;
import functioneditor.actions.PasteAction;
import functioneditor.actions.PrintAction;
import functioneditor.actions.QuitAction;
import functioneditor.actions.RedoAction;
import functioneditor.actions.RefreshFunctionsAction;
import functioneditor.actions.SaveAction;
import functioneditor.actions.SaveAllAction;
import functioneditor.actions.SwitchLibrary;
import functioneditor.actions.UndoAction;
import functioneditor.controllers.FunctionEditorController;

import analyse.preferences.AnalysePreferences;
import analyse.preferences.LibraryPreferences;

import analyse.resources.CPlatform;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;


public class MatlabFunctionEditorWindow extends ApplicationWindow {
	
	private SashForm mainSashForm;
	private FunctionsEditorComposite functionsEditorComposite;
	private LibraryComposite libraryComposite;
	private FunctionEditorController matlabFunctionEditorController;

	public static NewFileAction newFileAction = new NewFileAction();
	public static QuitAction quitAction = new QuitAction();
	public static SaveAction saveAction = new SaveAction();
	public static SaveAllAction saveAllAction = new SaveAllAction();
	public static OpenFileAction openFileAction = new OpenFileAction();
	public static PrintAction printAction = new PrintAction();
	public static UndoAction undoAction = new UndoAction();
	public static RedoAction redoAction = new RedoAction();
	public static CutAction cutAction = new CutAction();
	public static CopyAction copyAction = new CopyAction();
	public static PasteAction pasteAction = new PasteAction();
	public static FontAction fontAction = new FontAction();
	public static FindAndReplaceAction findAndReplaceAction;
	//public static HelpAction helpAction = new HelpAction();
	public static NewFolderAction newFolderAction = new NewFolderAction();
	public static DeleteAction deleteAction = new DeleteAction();
	private static RefreshFunctionsAction refreshFunctionsAction = new RefreshFunctionsAction();
	public static SwitchLibrary switchLibrary = new SwitchLibrary();
	
	
	public MatlabFunctionEditorWindow(FunctionEditorController matlabFunctionEditorController, Shell shell) {
		super(shell);
		addMenuBar();
		addCoolBar(SWT.FLAT);
		this.matlabFunctionEditorController = matlabFunctionEditorController;
		addStatusLine();			
		
		setShellStyle(SWT.SHELL_TRIM);
		
		create();
		getMenuBarManager().updateAll(true);
	}
	
	
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
	}
	
	@Override
	protected Control createContents(Composite parent) {
		getShell().setText(Messages.getString("matlabfunctioneditor.MatlabFunctionEditorWindow.Title")); //$NON-NLS-1$
		getShell().setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		
		int monitorNumber = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.MONITOR_NUMBER);		
		Monitor monitor = parent.getDisplay().getMonitors()[monitorNumber];
		
		Rectangle bounds = monitor.getBounds ();
		parent.getShell().setLocation (bounds.x, bounds.y);		

		mainSashForm = new SashForm(parent.getShell(), SWT.NONE);
		mainSashForm.setOrientation(SWT.HORIZONTAL);
		libraryComposite = new LibraryComposite(mainSashForm,SWT.NONE);
		functionsEditorComposite = new FunctionsEditorComposite(mainSashForm,SWT.BORDER);		
	
		mainSashForm.setWeights(new int[] {20,80});
		
		openFileAction.setFunctionsEditorComposite(functionsEditorComposite);
		deleteAction.setFunctionsEditorComposite(functionsEditorComposite);
		undoAction.setFunctionsEditorComposite(functionsEditorComposite);
		redoAction.setFunctionsEditorComposite(functionsEditorComposite);
		copyAction.setFunctionsEditorComposite(functionsEditorComposite);
		pasteAction.setFunctionsEditorComposite(functionsEditorComposite);
		saveAction.setFunctionsEditorComposite(functionsEditorComposite);
		saveAllAction.setFunctionsEditorComposite(functionsEditorComposite);
		cutAction.setFunctionsEditorComposite(functionsEditorComposite);
		fontAction.setFunctionsEditorComposite(functionsEditorComposite);
		refreshFunctionsAction.setFunctionsEditorComposite(functionsEditorComposite);
		switchLibrary = new SwitchLibrary();
		switchLibrary.setMatlabFunctionEditorWindow(this);
		
		quitAction.setMatlabFunctionEditorWindow(this);

		printAction.setFunctionsEditorComposite(functionsEditorComposite);
		
		libraryComposite.addSelectionChangedListener(openFileAction);
		libraryComposite.addSelectionChangedListener(newFolderAction);
		libraryComposite.addSelectionChangedListener(newFileAction);
		libraryComposite.addSelectionChangedListener(deleteAction);
		libraryComposite.addDoubleClickListener(openFileAction);
		
		
		//Retreive size and position preferences
		IPreferenceStore preferenceStore = AnalysePreferences.getPreferenceStore();
		Rectangle rect = new Rectangle(preferenceStore.getInt(AnalysePreferences.FUNCTION_EDITOR_WINDOW_POSITION_LEFT), 
									   preferenceStore.getInt(AnalysePreferences.FUNCTION_EDITOR_WINDOW_POSITION_TOP),
									   preferenceStore.getInt(AnalysePreferences.FUNCTION_EDITOR_WINDOW_WIDTH),
									   preferenceStore.getInt(AnalysePreferences.FUNCTION_EDITOR_WINDOW_HEIGHT));		
		getShell().setBounds(rect);
		
		if(CPlatform.isWindows() || CPlatform.isLinux())
			getShell().setMaximized(preferenceStore.getBoolean(AnalysePreferences.FUNCTION_EDITOR_WINDOW_MAXIMIZED));
			else getShell().setMaximized(false);
			//Add the controller as Control listener
			getShell().addControlListener(matlabFunctionEditorController);
		
		return super.createContents(parent);	
	}
	
	@Override
	protected CoolBarManager createCoolBarManager(int style) {
		CoolBarManager coolBarManager = new CoolBarManager(style);
		
		ToolBarManager toolBarManager = new ToolBarManager(style);
		toolBarManager.add(newFolderAction);
		toolBarManager.add(newFileAction);
		toolBarManager.add(saveAction);
		toolBarManager.add(saveAllAction);
		toolBarManager.add(printAction);
		toolBarManager.add(deleteAction);
		
		ToolBarManager toolBarManager1 = new ToolBarManager(style);
		toolBarManager1.add(undoAction);
		toolBarManager1.add(redoAction);
		toolBarManager1.add(cutAction);
		toolBarManager1.add(copyAction);
		toolBarManager1.add(pasteAction);
		
		ToolBarManager toolBarManager2 = new ToolBarManager(style);
		toolBarManager2.add(fontAction);
				
		coolBarManager.add(toolBarManager);
		coolBarManager.add(toolBarManager1);
		coolBarManager.add(toolBarManager2);
		
		return coolBarManager;
	}
	
	protected MenuManager createMenuManager(){
		MenuManager mainMenu = new MenuManager(null);
		MenuManager menu_files = new MenuManager(Messages.getString("matlabfunctioneditor.MatlabFunctionEditorWindow.FunctionEditorMenuText")); //$NON-NLS-1$
		MenuManager menu_edit = new MenuManager(Messages.getString("matlabfunctioneditor.MatlabFunctionEditorWindow.SubMenuEditText")); //$NON-NLS-1$
		MenuManager menu_search = new MenuManager(Messages.getString("matlabfunctioneditor.MatlabFunctionEditorWindow.SubMenuSearchText")); //$NON-NLS-1$
		//MenuManager menu_help = new MenuManager("Help");
		mainMenu.add(menu_files);
		menu_files.add(newFolderAction);
		menu_files.add(newFileAction);
		menu_files.add(openFileAction);
		menu_files.add(new Separator());
		menu_files.add(refreshFunctionsAction);
		menu_files.add(switchLibrary);
		menu_files.add(new Separator());
		menu_files.add(deleteAction);
		menu_files.add(new Separator());
		menu_files.add(saveAction);
		menu_files.add(saveAllAction);
		menu_files.add(printAction);
		menu_files.add(new Separator());
		menu_files.add(quitAction);		
		mainMenu.add(menu_edit);
		menu_edit.add(undoAction);
		menu_edit.add(redoAction);
		menu_edit.add(new Separator());
		menu_edit.add(copyAction);
		menu_edit.add(cutAction);
		menu_edit.add(pasteAction);
		menu_edit.add(new Separator());
		menu_edit.add(fontAction);
		mainMenu.add(menu_search);
		menu_search.add(findAndReplaceAction = new FindAndReplaceAction());
		//mainMenu.add(menu_help);
		//menu_help.add(helpAction);
		
		return mainMenu;
	}
	
	public void doClose() {		
		close();		
	}
	
	public boolean doSwitchLibrary() {
		functionsEditorComposite.doCloseAll();
//		LibraryPreferences.removeObserver(libraryComposite);
		if(LibraryPreferences.doSwitch()) {
			libraryComposite.refreshInput();
			return true;
		}
		return false;
	}
		
	
	@Override
	public boolean close() {		
		
		if(functionsEditorComposite.isFunctionsDirty()){			
			
			MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(),SWT.YES | SWT.NO | SWT.CANCEL);
			messageBox.setMessage(Messages.getString("matlabfunctioneditor.MatlabFunctionEditorWindow.MessageDialogSaveText"));					 //$NON-NLS-1$
			int response = messageBox.open();
			
			switch (response) {
			case SWT.YES:
				functionsEditorComposite.doSaveAll();
				LibraryPreferences.removeObserver(libraryComposite);
				return super.close();
				//break;
			case SWT.NO:
				LibraryPreferences.removeObserver(libraryComposite);
				return super.close();
				//break;
			default:
				return false;
				//break;
			}
		}		
		LibraryPreferences.removeObserver(libraryComposite);
		return super.close();
	}
}
