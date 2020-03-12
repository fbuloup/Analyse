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
package analyse.actions;

import org.eclipse.jface.action.Action;

import analyse.AnalyseApplication;
import analyse.gui.ConsolesView;
import analyse.gui.View;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class ShowSharingViewAction extends Action {

	private static View view;

	public ShowSharingViewAction() {
		setText(Messages.getString("ShowExperimentsSharingViewAction.Title"));
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.SHARING_VIEW_ICON));
	}
	
	public void setView(View experimentsView) {
		view = experimentsView;
	}
	
	public void run() {
		AnalyseApplication.getAnalyseApplicationWindow().showView(view,ConsolesView.SHOW_SHARING_CONSOLE);
	}
	
}
