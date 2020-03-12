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

import java.util.Date;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Note;
import analyse.resources.Messages;

public class NoteEditor extends CTabItem implements IResourceObserver {

	private Note note;
	private ToolBarManager toolBarManager;
	private FormToolkit formToolkit;
	private Text text;
	private ScrolledForm form;

	public NoteEditor(CTabFolder parent, int style, Note note) {
		super(parent, style);
		this.note = note;
		formToolkit = new FormToolkit(getDisplay());
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeToolBar();
				Experiments.getInstance().removeExperimentObserver(NoteEditor.this);
				formToolkit.dispose();
			}
		});
		createContents();
		text.setFocus();
	}
	
	public Note getNote() {
		return note;
	}

	protected void disposeToolBar() {
		if(toolBarManager != null) {
			toolBarManager.dispose();
			toolBarManager = null;
		}
	}
	
	protected ToolBar getToolBar() {
		toolBarManager = new ToolBarManager(SWT.FLAT | SWT.WRAP);
		toolBarManager.add(AnalyseApplicationWindow.clearActiveEditorAction);
		ToolBar toolBar = toolBarManager.createControl(getParent());
		toolBar.addFocusListener((FocusListener) getParent());
		return toolBar;
	}

	private void createContents() {
		Composite container = new Composite(getParent(), SWT.NONE);
//		formToolkit.adapt(container);
//		formToolkit.paintBordersFor(container);
		container.setLayout(new FillLayout());
//		container.addFocusListener((FocusListener) getParent());
		
		
		form = formToolkit.createScrolledForm(container);
		formToolkit.decorateFormHeading(form.getForm());
		form.getForm().getBody().setLayout(new GridLayout(4, true));
		form.getForm().getBody().addFocusListener((FocusListener) getParent());

		
		form.setText(Messages.getString("NoteEditor.CreateDateLabel") + note.getDataNote().getCreationDateString() + " - " + 
					 Messages.getString("NoteEditor.ModifDateLabel") + note.getDataNote().getModificationDateString());
		
//		Label createDatelabel = formToolkit.createLabel(container, Messages.getString("NoteEditor.CreateDateLabel"), SWT.NONE);
//		createDatelabel.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,false));
//		Label createDatelabelValue = formToolkit.createLabel(container, creationDate, SWT.NONE);
//		createDatelabelValue.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));
//		
//		Label modifDatelabel = formToolkit.createLabel(container, Messages.getString("NoteEditor.ModifDateLabel"), SWT.NONE);
//		modifDatelabel.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,false));
//		modifDatelabelValue = formToolkit.createLabel(container, modificationDate, SWT.NONE);
//		modifDatelabelValue.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));
		
		text = formToolkit.createText(form.getForm().getBody(), note.getDataNote().getText(), SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		text.addFocusListener((FocusListener) getParent());
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				note.getDataNote().setModificationDate(new Date());
				note.getDataNote().setText(text.getText());
				note.saveNote();
				form.setText(Messages.getString("NoteEditor.CreateDateLabel") + note.getDataNote().getCreationDateString() + " - " + 
						 Messages.getString("NoteEditor.ModifDateLabel") + note.getDataNote().getModificationDateString());
			}
		});
		
		setControl(container);
		
	}

	public void update(int message, IResource[] resources) {
		if(message == IResourceObserver.RENAMED) {
			if(note == resources[0] || note.hasParent(resources[0])) {
				setText(note.getNameWithoutExtension());
				setToolTipText(note.getLocalPath());
			}
		}
		if(message == IResourceObserver.DELETED) {
			for (int i = 0; i < resources.length; i++) {
				if(note == resources[i] || note.hasParent(resources[i])) {
					dispose();
					break;
				}
			}
		}
	}

	public void clearNote() {
		note.getDataNote().setText("");
		note.getDataNote().setModificationDate(new Date());
		note.saveNote();
		form.setText(Messages.getString("NoteEditor.CreateDateLabel") + note.getDataNote().getCreationDateString() + " - " + 
				 Messages.getString("NoteEditor.ModifDateLabel") + note.getDataNote().getModificationDateString());
		text.setText("");
	}

}
