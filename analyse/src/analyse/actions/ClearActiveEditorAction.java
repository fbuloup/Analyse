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
package analyse.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;

import analyse.gui.ChartEditor;
import analyse.gui.EditorsView;
import analyse.gui.MultiChartsEditor;
import analyse.gui.NoteEditor;
import analyse.gui.ProcessingEditor;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class ClearActiveEditorAction extends Action implements SelectionListener {
	
	CTabItem editor;
	
	public ClearActiveEditorAction() {
		super(Messages.getString("ClearActiveEditorAction.Title"),AS_PUSH_BUTTON);
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.CLEAR_CHART_ICON));
		setEnabled(false);
	}
	
	public void run() {	
		if(MessageDialog.openQuestion(Display.getDefault().getActiveShell(), Messages.getString("ClearActiveEditorAction.DialogTitle"), Messages.getString("ClearActiveEditorAction.DialogMessage"))) {
			if(editor instanceof ChartEditor) ((ChartEditor)editor).clearChart();
			if(editor instanceof ProcessingEditor) ((ProcessingEditor)editor).clearProcessing();
			if(editor instanceof NoteEditor) ((NoteEditor)editor).clearNote();
			if(editor instanceof MultiChartsEditor) ((MultiChartsEditor)editor).clearCharts();
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		setEnabled(false);
		if(e.widget instanceof EditorsView) {
			EditorsView editorsView = (EditorsView) e.widget;
			editor = editorsView.getSelection();
			setEnabled(true);
		}
	}
	
	public void setEditor(CTabItem editor) {
		this.editor = editor;
	}
}
