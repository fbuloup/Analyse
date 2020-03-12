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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import analyse.model.IResource;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class CopyAction extends Action  implements ISelectionChangedListener, FocusListener {
	
	protected static IResource[] selectedResources = new IResource[0];
	protected static boolean done = false;
	private TreeSelection selection;
	private Widget selectedWidget = null;

	public CopyAction() {
		super(Messages.getString("CopyAction.Title"),AS_PUSH_BUTTON);
		setAccelerator(SWT.MOD1 | 'C');			
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.COPY_ICON));
		setEnabled(true);
	}
	
	public void run() {
		if(selectedWidget == null && selection != null) {
			Object[] elements = selection.toArray();
			if(elements.length > 0) {
				done = true;
				selectedResources = new IResource[elements.length] ;
				for (int i = 0; i < elements.length; i++) {
					selectedResources[i] = (IResource)elements[i]; 
				}
			}
		} else {
			if(selectedWidget instanceof StyledText || selectedWidget instanceof Text) {
				String plainText = null;
				if(selectedWidget instanceof StyledText) plainText = ((StyledText) selectedWidget).getSelectionText();
				if(selectedWidget instanceof Text) plainText = ((Text) selectedWidget).getSelectionText();
				Clipboard clipboard = new Clipboard(Display.getDefault());
				TextTransfer textTransfer = TextTransfer.getInstance();
				clipboard.setContents(new String[]{plainText}, new Transfer[]{textTransfer});
				clipboard.dispose();
			}
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		selection = ((TreeSelection)event.getSelection());
		selectedWidget = null;
	}

	public void focusGained(FocusEvent e) {
		selectedWidget = e.widget;
	}

	public void focusLost(FocusEvent e) {
		selectedWidget = null;
	}

}
