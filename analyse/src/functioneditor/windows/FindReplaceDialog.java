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

import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import analyse.preferences.AnalysePreferences;

import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;



/**
 * This class displays a find/replace dialog
 */
public class FindReplaceDialog extends Dialog {

	public static IDocument selectedDocument = null;
	public static ITextViewer selectedSourceViewer = null;

	// The adapter that does the finding/replacing
	private static FindReplaceDocumentAdapter frda;

	// The associated viewer
	//private ITextViewer viewer;

	// The find and replace buttons
	private Button doFind;
	private Button doReplace;
	private Button doReplaceFind;

	/**
	 * FindReplaceDialog constructor
	 * 
	 * @param shell the parent shell
	 * @param document the associated document
	 * @param viewer the associated viewer
	 */
	public FindReplaceDialog(Shell shell)/*, IDocument document, ITextViewer viewer) */{
		super(shell);//, SWT.DIALOG_TRIM | SWT.MODELESS  | SWT.RESIZE);
		/*frda = new FindReplaceDocumentAdapter(document);
    	this.viewer = viewer;*/
	}
/*
	public void setDocument(IDocument document) {
		frda = new FindReplaceDocumentAdapter(document);
	}

	public void setViewer(ITextViewer textViewer) {
		this.viewer = textViewer;
	}
*/
	
	public static void initFindReplaceDocumentAdapter() {
		frda = new FindReplaceDocumentAdapter(selectedDocument);
	}
	
	/**
	 * Opens the dialog box
	 */
	public void open() {
		Shell shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.MODELESS  | SWT.RESIZE);
		shell.setText(Messages.getString("FindReplaceDialog.Title")); //$NON-NLS-1$
		shell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		createContents(shell);
		shell.pack();
		int x = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.FIND_REPLACE_DIALOG_LEFT);
		int y = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.FIND_REPLACE_DIALOG_TOP);
		int w = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.FIND_REPLACE_DIALOG_WIDTH);
		int h = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.FIND_REPLACE_DIALOG_HEIGHT);
		if(y==0) {
			int monitorNumber = AnalysePreferences.getPreferenceStore().getInt(AnalysePreferences.MONITOR_NUMBER);		
			Monitor monitor = getParent().getDisplay().getMonitors()[monitorNumber];
			Rectangle bounds = monitor.getBounds ();
			y = bounds.height / 2 - shell.getBounds().height / 2;
		}
		if(x==0) x = 10;
		if(w>0 && h>0)
			shell.setBounds(x,y,w,h);
		else shell.setBounds(x, y, shell.getBounds().width, shell.getBounds().height);
		shell.open();
		shell.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
				Shell shell = (Shell) e.widget;
				AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.FIND_REPLACE_DIALOG_TOP, shell.getBounds().y);
				AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.FIND_REPLACE_DIALOG_LEFT, shell.getBounds().x);
				AnalysePreferences.savePreferences();
			}
			public void controlResized(ControlEvent e) {
				Shell shell = (Shell) e.widget;
				AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.FIND_REPLACE_DIALOG_WIDTH, shell.getBounds().width);
				AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.FIND_REPLACE_DIALOG_HEIGHT, shell.getBounds().height);
				AnalysePreferences.savePreferences();
			}
		});
		
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Performs a find
	 * 
	 * @param find the find string
	 * @param forward whether to search forward
	 * @param matchCase whether to match case
	 * @param wholeWord whether to search on whole word
	 * @param regexp whether find string is a regular expression
	 */
	protected void doFind(String find, boolean forward, boolean matchCase,boolean wholeWord, boolean regexp) {		
		// You can't mix whole word and regexp
		if (wholeWord && regexp) {
			showError(Messages.getString("FindReplaceDialog.ErrorMessage1")); //$NON-NLS-1$
		} else {
			IRegion region = null;
			try {
				// Get the current offset
				int offset = selectedSourceViewer.getTextWidget().getCaretOffset();

				// If something is currently selected, and they're searching backwards,
				// move offset to beginning of selection. Otherwise, repeated backwards
				// finds will only find the same text
				if (!forward) {
					Point pt = selectedSourceViewer.getSelectedRange();
					if (pt.x != pt.y) {
						offset = pt.x - 1;
					}
				}

				// Make sure we're in the document
				if (offset >= frda.length()) offset = frda.length() - 1;
				if (offset < 0) offset = 0;

				// Perform the find
				region = frda.find(offset, find, forward, matchCase, wholeWord, regexp);

				// Update the viewer with found selection
				if (region != null) {
					selectedSourceViewer.setSelectedRange(region.getOffset(), region.getLength());
				}

				// If find succeeded, enable Replace buttons.
				// Otherwise, disable Replace buttons.
				// We know find succeeded if region is not null
				enableReplaceButtons(region != null);
			} catch (BadLocationException e) {
				// Ignore
			} catch (PatternSyntaxException e) {
				// Show the error to the user
				showError(e.getMessage());
			}
		}
	}

	/**
	 * Performs a replace
	 * @param replaceText the replacement text
	 */
	protected void doReplace(String replaceText) {
		try {
			frda.replace(replaceText, false);
		} catch (BadLocationException e) {
			// Ignore
		}
	}

	/**
	 * Creates the dialog's contents
	 * 
	 * @param shell
	 */
	protected void createContents(final Shell shell) {
		shell.setLayout(new GridLayout(2, false));

		Label label1 = new Label(shell, SWT.NONE);
		label1.setLayoutData(new GridData(GridData.END, GridData.CENTER,false,false));
		label1.setText(Messages.getString("FindReplaceDialog.FindLabelTitle")); //$NON-NLS-1$
		final Text findText = new Text(shell, SWT.BORDER);
		findText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		Label label2 = new Label(shell, SWT.NONE);
		label2.setLayoutData(new GridData(GridData.END, GridData.CENTER,false,false));
		label2.setText(Messages.getString("FindReplaceDialog.ReplaceLabelTitle")); //$NON-NLS-1$
		final Text replaceText = new Text(shell, SWT.BORDER);
		replaceText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		
		Group groupDirection = new Group(shell, SWT.NONE);
		groupDirection.setText(Messages.getString("FindReplaceDialog.DirectionGroupTitle")); //$NON-NLS-1$
		groupDirection.setLayout(new GridLayout(1,false));
		groupDirection.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
		final Button down = new Button(groupDirection, SWT.RADIO);
		down.setText(Messages.getString("FindReplaceDialog.ForwardLabelTitle")); //$NON-NLS-1$
		down.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		final Button up = new Button(groupDirection, SWT.RADIO);
		up.setText(Messages.getString("FindReplaceDialog.BackwardLabelTitle")); //$NON-NLS-1$
		up.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		
		Group groupOptions = new Group(shell, SWT.NONE);
		groupOptions.setText(Messages.getString("FindReplaceDialog.OptionsGroupTitle")); //$NON-NLS-1$
		groupOptions.setLayout(new GridLayout(1,false));
		groupOptions.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
		
		
		// Add the match case checkbox
		final Button match = new Button(groupOptions, SWT.CHECK);
		match.setText(Messages.getString("FindReplaceDialog.CaseSensitiveButtonTitle")); //$NON-NLS-1$
		match.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		// Add the whole word checkbox
		final Button wholeWord = new Button(groupOptions, SWT.CHECK);
		wholeWord.setText(Messages.getString("FindReplaceDialog.WholeWordButtonTitle")); //$NON-NLS-1$
		wholeWord.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		// Add the regular expression checkbox
		final Button regexp = new Button(groupOptions, SWT.CHECK);
		regexp.setText(Messages.getString("FindReplaceDialog.RegExpButtonTitle")); //$NON-NLS-1$
		regexp.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		// Add the buttons
		Composite buttons = new Composite(shell, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.FILL,GridData.FILL, true, true,2,1));
		buttons.setLayout(new GridLayout(2,true));

		// Create the Find button
		doFind = new Button(buttons, SWT.PUSH);
		doFind.setText(Messages.getString("FindReplaceDialog.FindButtonTitle")); //$NON-NLS-1$
		doFind.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Set the initial find operation to FIND_FIRST
//		doFind.setData(FindReplaceOperationCode.FIND_FIRST);

		// Create the Replace button
		doReplace = new Button(buttons, SWT.PUSH);
		doReplace.setText(Messages.getString("FindReplaceDialog.ReplaceButtonTitle")); //$NON-NLS-1$
		doReplace.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the Replace/Find button
		doReplaceFind = new Button(buttons, SWT.PUSH);
		doReplaceFind.setText(Messages.getString("FindReplaceDialog.ReplaceFindButtonTitle")); //$NON-NLS-1$
		doReplaceFind.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		doReplaceFind.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				doReplace(replaceText.getText());
				doFind(findText.getText(), down.getSelection(), match.getSelection(),
						wholeWord.getSelection(), regexp.getSelection());
			}
		});

		// Create the Close button
		Button close = new Button(buttons, SWT.PUSH);
		close.setText(Messages.getString("FindReplaceDialog.CloseButtonTitle")); //$NON-NLS-1$
		close.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		close.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.close();
			}
		});

		// Disable the replace button when find text is modified
		findText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				enableReplaceButtons(false);
			}
		});

		// Do a find
		doFind.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				doFind(findText.getText(), down.getSelection(), match.getSelection(), 
						wholeWord.getSelection(), regexp.getSelection());
			}
		});

		// Replace loses "find" state, so disable buttons
		doReplace.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				doReplace(replaceText.getText());
				enableReplaceButtons(false);
			}
		});

		// Set defaults
		down.setSelection(true);
		findText.setFocus();
		enableReplaceButtons(false);
		shell.setDefaultButton(doFind);
	}

	/**
	 * Enables/disables the Replace and Replace/Find buttons
	 * 
	 * @param enable whether to enable or disable
	 */
	protected void enableReplaceButtons(boolean enable) {
		doReplace.setEnabled(enable);
		doReplaceFind.setEnabled(enable);
	}

	/**
	 * Shows an error
	 * 
	 * @param message the error message
	 */
	protected void showError(String message) {
		MessageDialog.openError(getParent(), Messages.getString("FindReplaceDialog.ErrorMessageDialogTitle"), message); //$NON-NLS-1$
	}
	
	public static void main(String[] args) {
		
		Display display = new Display();
		Shell shell = new Shell(display); 
		
		(new FindReplaceDialog(shell)).open();
		
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
