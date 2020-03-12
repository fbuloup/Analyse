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

import analyse.AnalyseApplication;
import analyse.gui.View;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public final class ShowExperimentsViewAction extends Action {
	
	private static View view;

	public ShowExperimentsViewAction() {
		setText(Messages.getString("ShowExperimentsViewAction.Title"));
		setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.EXPERIMENTS_VIEW_ICON));
	}
	
	public void setView(View experimentsView) {
		view = experimentsView;
	}
	
	public void run() {
		AnalyseApplication.getAnalyseApplicationWindow().showView(view,0);
	}
}
