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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;

import analyse.resources.ColorsUtils;

public class SharingContainer extends Composite {
	
	private CTabFolder sharingTabFolder;
	private CTabItem serverTabItem;
	private CTabItem clientTabItem;
	
	private static StyleRange serverMessageStyle;
	private static StyleRange serverWarningMessageStyle;
	private static StyleRange serverErrorMessageStyle;

	public SharingContainer(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		
		sharingTabFolder = new CTabFolder(this, SWT.BORDER);
		sharingTabFolder.setBorderVisible(false);
		sharingTabFolder.setTabPosition(SWT.BOTTOM);
		sharingTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
		sharingTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);
		
		sharingTabFolder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				sharingTabFolder.getParent().setFocus();
			}
		});
		
		serverTabItem = ServerSharingContainer.createContainer(sharingTabFolder, (View) getParent());
		clientTabItem = ClientSharingContainer.createContainer(sharingTabFolder, (View) getParent());
		
		sharingTabFolder.setSelection(0);
		
		serverMessageStyle = new StyleRange();
		serverMessageStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);		
		serverMessageStyle.fontStyle = SWT.BOLD;
				
		serverWarningMessageStyle = new StyleRange();
		serverWarningMessageStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW);		
		serverWarningMessageStyle.fontStyle = SWT.BOLD;
		
		serverErrorMessageStyle = new StyleRange();
		serverErrorMessageStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);		
		serverErrorMessageStyle.fontStyle = SWT.BOLD;
		
	}

	public void clearMessages() {
		if(sharingTabFolder.getSelection() == serverTabItem) ServerSharingContainer.clearMessages();
		if(sharingTabFolder.getSelection() == clientTabItem) ClientSharingContainer.clearMessages();
	}

	public void updateSelection(boolean setFocus) {
		if(sharingTabFolder != null && !sharingTabFolder.isDisposed())
			if(setFocus) {
				sharingTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				sharingTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);
				
			} else {
				sharingTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				sharingTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);
			}
	}
}
