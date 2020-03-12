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

import java.io.File;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import analyse.Log;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;
import analyse.resources.RegularExpressions;

public class RegExpSelectionDialog extends TitleAreaDialog {

	private int ADD_ID = IDialogConstants.CLIENT_ID + 1;
	private int REMOVE_ID = IDialogConstants.CLIENT_ID + 2;
	private int RESET_ID = IDialogConstants.CLIENT_ID + 3;
	private String ADD_LABEL = Messages.getString("RegExpSelectionDialog.AddButtonTitle"); //$NON-NLS-1$
	private String REMOVE_LABEL = Messages.getString("RegExpSelectionDialog.RemoveButtonTitle"); //$NON-NLS-1$
	private String RESET_LABEL = Messages.getString("RegExpSelectionDialog.ResetButtonTitle"); //$NON-NLS-1$
	
	private ListViewer regularExpressionsListViewer;
	private String regularExpression;
	private RegularExpressions regExp;
	private Text regExpLabelText;
	private Text regExpExpressionText;
	
	public RegExpSelectionDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.MAX | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);		
		
		parent.getShell().setText(Messages.getString("RegExpSelectionDialog.ShellTitle")); //$NON-NLS-1$
		
		getShell().setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		
		setTitle(Messages.getString("RegExpSelectionDialog.Title")); //$NON-NLS-1$
		setMessage(Messages.getString("RegExpSelectionDialog.Text"));		 //$NON-NLS-1$
			
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
		
		regExp = RegularExpressions.readRegularExpressions();
		
		regularExpressionsListViewer = new ListViewer(container,SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);
		regularExpressionsListViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
		regularExpressionsListViewer.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(Object inputElement) {
				if(inputElement instanceof RegularExpressions) return ((RegularExpressions)inputElement).getContent();
				return new Object[0];
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput,Object newInput) {
			}
		});
		regularExpressionsListViewer.setLabelProvider(new ILabelProvider(){
			public Image getImage(Object element) {
				return null;
			}
			public String getText(Object element) {
				if(element instanceof String) return (String)element;
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
		});
		
		regularExpressionsListViewer.setInput(regExp);
		
		Composite newRegularExpressionComposite = new Composite(container, SWT.NONE);
		newRegularExpressionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		newRegularExpressionComposite.setLayout(new GridLayout(4, false));
		Label regExpLabel = new Label(newRegularExpressionComposite, SWT.NONE);
		regExpLabel.setText(Messages.getString("RegExpSelectionDialog.LabelTitle")); //$NON-NLS-1$
		regExpLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		regExpLabelText = new Text(newRegularExpressionComposite, SWT.BORDER);
		regExpLabelText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label regExpExpression = new Label(newRegularExpressionComposite, SWT.NONE);
		regExpExpression.setText(Messages.getString("RegExpSelectionDialog.ExpressionTitle")); //$NON-NLS-1$
		regExpExpression.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		regExpExpressionText = new Text(newRegularExpressionComposite, SWT.BORDER);
		regExpExpressionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		return dialogArea;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button addButton = createButton(parent, ADD_ID, ADD_LABEL,	false);
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String label = regExpLabelText.getText();
				String expression = regExpExpressionText.getText();
				if(label.equals("")) { //$NON-NLS-1$
					MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(), Messages.getString("RegExpSelectionDialog.ErrorDialogTitle"), ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON), Messages.getString("RegExpSelectionDialog.ErrorDialogLabelText"), MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL}, 0);  //$NON-NLS-1$ //$NON-NLS-2$
			        dialog.open();
					return;
				}
				if(expression.equals("")) { //$NON-NLS-1$
					MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(), Messages.getString("RegExpSelectionDialog.ErrorDialog"), ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON), Messages.getString("RegExpSelectionDialog.ErrorDialogExpressionText"), MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL}, 0);  //$NON-NLS-1$ //$NON-NLS-2$
			        dialog.open();
					return;
				}
				regExp.addRegularExpression(expression, label);
				regularExpressionsListViewer.refresh();
				RegularExpressions.saveRegularExpressions(regExp);
			}
		});
		Button removeButton = createButton(parent, REMOVE_ID,	REMOVE_LABEL, false);
		removeButton.addSelectionListener(new SelectionAdapter(){
			@SuppressWarnings("unchecked")
			public void widgetSelected(SelectionEvent e) {
				if(regularExpressionsListViewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) regularExpressionsListViewer.getSelection();
					for (Iterator<String> iterator = selection.iterator(); iterator.hasNext();) {
						String selectionString = (String) iterator.next();
						selectionString = selectionString.replaceAll(" \\[\\w*\\]", ""); //$NON-NLS-1$ //$NON-NLS-2$
						regExp.removeRegularExpression(selectionString);
					}
					regularExpressionsListViewer.refresh();
					RegularExpressions.saveRegularExpressions(regExp);
				}
			}
		});
		Button resetButton = createButton(parent, RESET_ID,	RESET_LABEL, false);
		resetButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(!(new File("regexp.ser")).delete()) 
					Log.logErrorMessage(Messages.getString("RegExpSelectionDialog.ImpossibleDelete") + "regexp.ser");
				regExp = new RegularExpressions();
				regularExpressionsListViewer.setInput(regExp);
				RegularExpressions.saveRegularExpressions(regExp);
			}
		});
	}
	
	@Override
	protected void okPressed() {
		int index = regularExpressionsListViewer.getList().getSelectionIndex();
		if(index > -1) regularExpression = regExp.getRegularExpression(index);
		else regularExpression = ""; //$NON-NLS-1$
		RegularExpressions.saveRegularExpressions(regExp);
		super.okPressed();
	}

	public String getRegularExpression() {
		return regularExpression;
	}

}
