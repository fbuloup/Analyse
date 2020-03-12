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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import analyse.Log;
import analyse.model.Function;
import analyse.model.IFunctionObserver;
import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import functioneditor.controllers.FunctionEditorController;
import functioneditor.utils.AnalyseEditorSourceViewerConfiguration;
import functioneditor.utils.MatlabPartitionScanner;

import analyse.preferences.LibraryPreferences;

import analyse.resources.ColorsUtils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;


public class FunctionsEditorComposite extends CTabFolder implements IFunctionObserver {	
	
	private ArrayList<IDocument> documents = new ArrayList<IDocument>(0);
	private ArrayList<SourceViewer> sourceViewers = new ArrayList<SourceViewer>(0);
	private ArrayList<Function> functions = new ArrayList<Function>(0);
	
	
	private class CloseSelectedEditorAction extends Action {
		public CloseSelectedEditorAction() {
			setText(Messages.getString("matlabfunctioneditor.FunctionsEditorComposite.CloseActionTitle")); //$NON-NLS-1$
		}
		@Override
		public void run() {
			if(closeHandler(FunctionsEditorComposite.this.getSelection())) FunctionsEditorComposite.this.getSelection().dispose();
		}
	}
	
	private class CloseOthersEditorsAction extends Action {
		public CloseOthersEditorsAction() {
			setText(Messages.getString("matlabfunctioneditor.FunctionsEditorComposite.CloseOthersActionTitle")); //$NON-NLS-1$
		}
		@Override
		public void run() {
			CTabItem[] items = FunctionsEditorComposite.this.getItems();
			for (int i = 0; i < items.length; i++) {
				if(items[i] != FunctionsEditorComposite.this.getSelection())
					if(closeHandler(items[i])) items[i].dispose();
			}
		}
	}
	
	private class CloseAllEditorsAction extends Action {
		public CloseAllEditorsAction() {
			setText(Messages.getString("matlabfunctioneditor.FunctionsEditorComposite.CloseAllActionTitle")); //$NON-NLS-1$
		}
		@Override
		public void run() {
			FunctionsEditorComposite.this.doCloseAll();
		}
	}
	
	public FunctionsEditorComposite(Composite parent, int style) {
		super(parent, style);
		
		setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
		setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
		
		setSimple(false);
		setTabHeight(23);
		setMaximizeVisible(true);
		
		addSelectionListener(MatlabFunctionEditorWindow.saveAction);
		addSelectionListener(MatlabFunctionEditorWindow.saveAllAction);
		
		addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {				
			}

			public void widgetSelected(SelectionEvent e) {
				/*
				 * assign the selected document and source viewer in 
				 * FindReplaceDialog class statics members
				 */
				FindReplaceDialog.selectedDocument = documents.get(getSelectionIndex());
				FindReplaceDialog.selectedSourceViewer = sourceViewers.get(getSelectionIndex());	
				FindReplaceDialog.initFindReplaceDocumentAdapter();			
				sourceViewers.get(getSelectionIndex()).getTextWidget().setFocus();
			}
		});
		
		addCTabFolder2Listener(new CTabFolder2Listener(){

			public void close(CTabFolderEvent event) {
				/*
				 * Save the associated function if dirty
				 */
				event.doit = closeHandler((CTabItem) event.item);
			}

			public void maximize(CTabFolderEvent event) {
				maximizeHandler();
			}

			public void minimize(CTabFolderEvent event) {
			}

			public void restore(CTabFolderEvent event) {
				restoreHandler();
			}

			public void showList(CTabFolderEvent event) {
			}
		});
		
		addMouseListener(new MouseAdapter(){
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if(e.widget instanceof FunctionsEditorComposite) {
					mouseDoubleClickHandler();
				}
			}
		});
		
		
		MenuManager popupMenuManager = new MenuManager("popupMenuManagerEditors"); //$NON-NLS-1$
		popupMenuManager.setRemoveAllWhenShown(false);
		
		setMenu(popupMenuManager.createContextMenu(this));
		ActionContributionItem closeEditorAction= new ActionContributionItem(new CloseSelectedEditorAction()); 
		popupMenuManager.add(closeEditorAction);
		closeEditorAction.fill(popupMenuManager.getMenu(),0);
		
		ActionContributionItem closeAllEditors = new ActionContributionItem((IAction)(new CloseAllEditorsAction()));
		popupMenuManager.add(closeAllEditors);
		closeAllEditors.fill(popupMenuManager.getMenu(),1);
//		
		ActionContributionItem closeOthersEditorsAction = new ActionContributionItem((IAction)(new CloseOthersEditorsAction()));
		popupMenuManager.add(closeOthersEditorsAction);
		closeOthersEditorsAction.fill(popupMenuManager.getMenu(),2);	
		
	}

	protected boolean closeHandler(CTabItem tabItem) {
		int index = FunctionsEditorComposite.this.indexOf(tabItem);
		Function function = functions.get(index);
		if(function.isDirty()) {
			
			MessageDialog messageDialog = new MessageDialog(getParent().getShell(), Messages.getString("matlabfunctioneditor.FunctionsEditorComposite.SaveMessageDialogTitle"), ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON), Messages.getString("matlabfunctioneditor.FunctionsEditorComposite.SaveMessageDialogText") + function.getMatlabFunctionName() + " ?",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
															MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL}, 0);
			int response = messageDialog.open();
			
			switch (response) {
			case 0:
				doSave(function);
				function.notifyFunctionObservers();
				/*
				 * remove the document and source viewer from 
				 * statics members of FindReplaceDialog class
				 */						
				documents.remove(index);
				sourceViewers.remove(index);
				functions.remove(index);						
				break;
				
			case 2:
				return false;
			case 1:
				/*
				 * remove the document and source viewer from 
				 * statics members of FindReplaceDialog class
				 */
				function.setDirty(false);
				function.notifyFunctionObservers();
				documents.remove(index);
				sourceViewers.remove(index);
				functions.remove(index);
				break;
			default:
				break;
			}
		} else {
			/*
			 * remove the document and source viewer from 
			 * statics members of FindReplaceDialog class
			 */
			documents.remove(index);
			sourceViewers.remove(index);
			functions.remove(index);
		}
		return true;
	}

	protected void mouseDoubleClickHandler() {
		if(getMaximized()) restoreHandler(); 
		else maximizeHandler();
	}

	protected void restoreHandler() {
		setMaximized(false);		
		((SashForm)getParent()).setMaximizedControl(null);
	}

	protected void maximizeHandler() {
		setMaximized(true);
		((SashForm)getParent()).setMaximizedControl(this);
	}
	
	public void addFunctionEditorCTabItem(String functionName, boolean editable){		
		/*
		 * If this function is already opened, select it in the folder
		 */
		CTabItem[] items = getItems();		
		for (int i = 0; i < items.length; i++) {
			if(items[i].getText().equals(functionName) || items[i].getText().equals("*" + functionName)) { //$NON-NLS-1$
				setSelection(items[i]);				
				FindReplaceDialog.selectedDocument = documents.get(getSelectionIndex());
				FindReplaceDialog.selectedSourceViewer = sourceViewers.get(getSelectionIndex());	
				FindReplaceDialog.initFindReplaceDocumentAdapter();		
				functions.get(getSelectionIndex()).notifyFunctionObservers();
				sourceViewers.get(getSelectionIndex()).getTextWidget().setFocus();
				return;
			}
		}				
		/*
		 * Else, create the tabItem
		 */		
		CTabItem cTabItem = new CTabItem(this,SWT.CLOSE);
		cTabItem.setText(functionName);
		cTabItem.setImage(ImagesUtils.getImage(IImagesKeys.FUNCTION_ICON));
		SashForm cTabItemSash = new SashForm(this, SWT.NONE);
		cTabItemSash.setOrientation(SWT.HORIZONTAL);
		
		
		/*
		 * The function
		 */
		final Function function = new Function(functionName);
		function.initializeFunction();
		function.setMatlabFunctionName(functionName);
		function.addFunctionObservers(MatlabFunctionEditorWindow.saveAction);
		function.addFunctionObservers(MatlabFunctionEditorWindow.saveAllAction);
		
		/*
		 * The document
		 */
		IDocument document;
		if(function.getMatlabFunctionBody()!=null){
			document = new Document(function.getMatlabFunctionBody());
			//sourceViewer.setDocument(document);

		} else {
			document = new Document();
			
		}
		/*
		 * The partitioner scanner
		 */

		MatlabPartitionScanner matlabPartitionScanner = new MatlabPartitionScanner();
		
		/*
		 * The partitioner
		 */
		IDocumentPartitioner partitioner = new FastPartitioner(matlabPartitionScanner,MatlabPartitionScanner.TYPES);
		/*
		 * 
		 */		
		((Document)document).setDocumentPartitioner(FunctionEditorController.MATLAB_PARTITIONING,partitioner);
		partitioner.connect(document);		
		
		/*
		 * The source viewer
		 */
		IVerticalRuler verticalRuler = new VerticalRuler(15);		
		//IOverviewRuler overviewRuler = new OverviewRuler(null,10,null);
		final SourceViewer sourceViewer = new SourceViewer(cTabItemSash, verticalRuler, null,true, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		sourceViewer.setEditable(editable);
		
		AnalyseEditorSourceViewerConfiguration sourceViewerConfiguration = new AnalyseEditorSourceViewerConfiguration();
		
		sourceViewer.configure(sourceViewerConfiguration);
		
		
		
		sourceViewer.setDocument(document);
		
//		for (int i = 0; i < document.getLength(); i++) {
//			ITypedRegion typedRegion = partitioner.getPartition(i);			
//			System.out.println(i + " = " + typedRegion.getType());
//		}
				
//		sourceViewer.configure(new SourceViewerConfiguration(){
//			@Override
//			public int getTabWidth(ISourceViewer sourceViewer) {				
//				return 10;
//			}
//		});
		
		/*
		 * The undo manager
		 */
		IUndoManager undoManager = new TextViewerUndoManager(500);
		undoManager.connect(sourceViewer);
		sourceViewer.setUndoManager(undoManager);	
		sourceViewer.setData("function", function); //$NON-NLS-1$
		
		/*
		 * The FeaturesComposite
		 */
		final FeaturesComposite featuresComposite = new FeaturesComposite(cTabItemSash, SWT.NONE, function, editable);
		
		
		/*
		 * Handle CTRL+SPACE, MOD1+D and TAB behaviours
		 */
		sourceViewer.appendVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent event) {
				// Check for Ctrl+Spacebar
				if (event.stateMask == SWT.CTRL && event.keyCode == 32) {
					// Check if source viewer is able to perform operation
					if (sourceViewer.canDoOperation(SourceViewer.CONTENTASSIST_PROPOSALS)) sourceViewer.doOperation(SourceViewer.CONTENTASSIST_PROPOSALS);
					// Veto this key press to avoid further processing
					event.doit = false;
				}
				//Check for CTRL+d
				boolean testDelete = (event.stateMask == SWT.MOD1 && event.character == 'd');
				testDelete = testDelete || (event.stateMask == SWT.MOD1 && event.character == 'D');
				testDelete = testDelete || (event.stateMask == SWT.MOD1 && event.keyCode == 100);
				if (testDelete) {
					IDocument document = sourceViewer.getDocument();
					Point selection = sourceViewer.getSelectedRange();
					try {
						int lineNumber = document.getLineOfOffset(selection.x);
						int linesNumber = document.getNumberOfLines(selection.x, selection.y);
						for (int i = 0; i < linesNumber; i++) document.replace(document.getLineOffset(lineNumber), document.getLineLength(lineNumber), ""); //$NON-NLS-1$
					} catch (BadLocationException e) {
						Log.logErrorMessage(e);
					}
				}
				//Check for TAB and SHIFT+TAB
				if (event.character == SWT.TAB) {
					IDocument document = sourceViewer.getDocument();
					Point selection = sourceViewer.getSelectedRange();
					try {
						int lineNumber = document.getLineOfOffset(selection.x);
						int linesNumber = document.getNumberOfLines(selection.x, selection.y);
						for (int i = 0; i < linesNumber; i++) {
							if(event.stateMask == SWT.SHIFT) {
								String lineString = document.get(document.getLineOffset(lineNumber + i), document.getLineLength(lineNumber + i));
								if(lineString.startsWith("\t")) { //$NON-NLS-1$
									document.replace(document.getLineOffset(lineNumber + i), 1, ""); //$NON-NLS-1$
								}
							}
							else {
								document.replace(document.getLineOffset(lineNumber + i),0, "\t"); //$NON-NLS-1$
							}
						}
						if(event.stateMask == SWT.SHIFT) {
							if(selection.x == document.getLineOffset(lineNumber))
								sourceViewer.setSelectedRange(selection.x, selection.y - linesNumber + 1);
							else sourceViewer.setSelectedRange(selection.x - 1, selection.y - linesNumber + 1);
						}
						else {
							sourceViewer.setSelectedRange(selection.x + 1, selection.y + linesNumber - 1);
						}
						event.doit = false;
					} catch (BadLocationException e) {
						Log.logErrorMessage(e);
					}
				}
				//Check for CTRL and SHIFT+'C' for toggle comments
				boolean testComment = ((event.stateMask == (SWT.MOD1 | SWT.SHIFT)) && (event.keyCode == 'c'));
				if(testComment) {
					IDocument document = sourceViewer.getDocument();
					Point selection = sourceViewer.getSelectedRange();
					try {
						int lineNumber = document.getLineOfOffset(selection.x);
						int linesNumber = document.getNumberOfLines(selection.x, selection.y);
						int nbChars = 0;
						for (int i = 0; i < linesNumber; i++) {
							String lineString = document.get(document.getLineOffset(lineNumber + i), document.getLineLength(lineNumber + i));
							if(lineString.startsWith("%")) {
								document.replace(document.getLineOffset(lineNumber + i), 1, ""); //$NON-NLS-1$
								nbChars--;
							} else {
								document.replace(document.getLineOffset(lineNumber + i),0, "%"); //$NON-NLS-1$
								nbChars++;
							}
						}
						sourceViewer.setSelectedRange(selection.x, selection.y + nbChars);
					} catch (BadLocationException e) {
						Log.logErrorMessage(e);
					}
				}
				//Check for CTRL and ALT + 'c' : toggle editable
				boolean testSetEditable = ((event.stateMask == (SWT.CTRL | SWT.ALT)) && (event.keyCode == 'c'));
				if(testSetEditable) {
					if(!sourceViewer.isEditable() && LibraryPreferences.isStandardLibrary()) {
						sourceViewer.setEditable(true);
						featuresComposite.setEditable();
						String title = Messages.getString("matlabfunctioneditor.FunctionsEditorComposite.SetEditableMessageBoxTitle");
						String text = Messages.getString("matlabfunctioneditor.FunctionsEditorComposite.SetEditableMessageBoxText");
						MessageDialog.openInformation(getShell(), title, text + function.getMatlabFunctionName());
					}
				}
			}
		});
		
		/*
		 * Assign to the utils arrays
		 */
		functions.add(function);
		documents.add(document);
		sourceViewers.add(sourceViewer);	
		/*
		 * assign the selected document and source viewer in 
		 * statics members of FindReplaceDialog class
		 */
		FindReplaceDialog.selectedDocument = document;
		FindReplaceDialog.selectedSourceViewer = sourceViewer;		
		FindReplaceDialog.initFindReplaceDocumentAdapter();
		
		
		function.setDirty(false);
		function.notifyFunctionObservers();
		cTabItem.setControl(cTabItemSash);
		cTabItemSash.setWeights(new int[] {70,30});
		setSelection(cTabItem);
		
		document.addDocumentListener(new IDocumentListener(){

			public void documentAboutToBeChanged(DocumentEvent event) {
				// TODO Auto-generated method stub
				
			}

			public void documentChanged(DocumentEvent event) {
				Function function = functions.get(getSelectionIndex());
				IDocument document = documents.get(getSelectionIndex());
				function.setMatlabFunctionCorpse(document.get());
			}
			
		});

		function.addFunctionObservers(this);
		
		
		sourceViewers.get(getSelectionIndex()).getTextWidget().setFocus();
	}

	public void doUndo() {	
		if(getSelectionIndex() > -1)
		sourceViewers.get(getSelectionIndex()).getUndoManager().undo();
	}
	
	public void doReDo() {
		if(getSelectionIndex() > -1)
			sourceViewers.get(getSelectionIndex()).getUndoManager().redo();
	}

	public void doCopy() {
		if(getSelectionIndex() > -1)
			sourceViewers.get(getSelectionIndex()).getTextWidget().copy();
	}

	public void doPaste() {
		if(getSelectionIndex() > -1)
			sourceViewers.get(getSelectionIndex()).getTextWidget().paste();		
	}

	public void doCut() {
		if(getSelectionIndex() > -1)
			sourceViewers.get(getSelectionIndex()).getTextWidget().cut();	
	}

	public void doPrint() {
		if(getSelectionIndex() > -1)
			sourceViewers.get(getSelectionIndex()).getTextWidget().print();
	}
		
	public void setFont(FontData fontData) {		
		if(getSelectionIndex() > -1)
		{
			Font font = new Font(Display.getCurrent(),fontData);		
			sourceViewers.get(getSelectionIndex()).getTextWidget().setFont(font);
		}
	}
	
	public boolean isSelectedFunctionDirty() {
		return functions.get(getSelectionIndex()).isDirty();
	}
	
	public boolean isFunctionsDirty() {
		boolean isDirty = false;
		for (int i = 0; i < functions.size(); i++) {
			isDirty = isDirty || functions.get(i).isDirty();
		}
		return isDirty;
	}
	
	public void doSaveAll() {
		for (int i = 0; i < functions.size(); i++) {
			if(functions.get(i).isDirty()) doSave(functions.get(i));
		}
		MathEngineFactory.getInstance().getMathEngine().rehash();
	}
	
	public void doCloseAll() {
		CTabItem[] items = getItems();
		for (int i = 0; i < items.length; i++) {
			if(closeHandler(items[i])) items[i].dispose();
		}
	}
	
	public void doSave(){
		if(getSelectionIndex() > -1) {	
			doSave(functions.get(getSelectionIndex()));
		}
		IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
		if(mathEngine.isStarted()) mathEngine.rehash();
	}
	
	public void doSave(Function function) {		
		String completeFile;
		completeFile = function.getScriptCode();
		Writer output = null;
		try {
			output = new BufferedWriter(new FileWriter(LibraryPreferences.getRootDirectory() + function.getMatlabFunctionName() + ".m")); //$NON-NLS-1$
			output.write( completeFile );
			output.close();
			function.setDirty(false);			
			function.notifyFunctionObservers(); 
						
			CTabItem[] items = getItems();		
			for (int i = 0; i < items.length; i++) {
				if(items[i].getText().equals("*" + function.getOldMatlabFunctionName())) {					 //$NON-NLS-1$
					items[i].setText(function.getMatlabFunctionName());
					break;
				}
			}

			if(!function.getOldMatlabFunctionName().equals(function.getMatlabFunctionName())) {
				File oldFile = new File(LibraryPreferences.getRootDirectory() + function.getOldMatlabFunctionName() + ".m"); //$NON-NLS-1$
				oldFile.delete();
				LibraryPreferences.updateFunction(function.getOldMatlabFunctionName(),function.getMatlabFunctionName(),function.getShortDescription());
				function.resetOldMatlabFunctionName();				
			}
			
			
		} catch (IOException e) {
			Log.logErrorMessage(e);
			try {
				output.close();
			} catch (IOException e1) {
				Log.logErrorMessage(e1);
			}
		}			
		LibraryPreferences.save();
	}

	public void removeFunctionEditorCTabItem(String functionName) {
		/*
		 * If this function is already opened, close its folder
		 */
		CTabItem[] items = getItems();		
		for (int i = 0; i < items.length; i++) {
			if(items[i].getText().equals(functionName) || items[i].getText().equals("*" + functionName)) { //$NON-NLS-1$
				documents.remove(i);
				sourceViewers.remove(i);
				functions.remove(i);
				items[i].dispose();
				return;
			}
		}		
	}

	public void update(Function function) {		
		if(getSelectionIndex() > -1)
		{	
			if(functions.get(getSelectionIndex()).isDirty()) {			
				String title = getSelection().getText();
				title = title.replaceFirst("\\*","");			 //$NON-NLS-1$ //$NON-NLS-2$
				getSelection().setText("*" + title ); //$NON-NLS-1$
			}
		}
						
	}

	
	public Function[] getFunctions() {
		return functions.toArray(new Function[functions.size()]);
	}
	
}
