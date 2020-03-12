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
import analyse.resources.Messages;

public class RestartAction extends Action {
	
	public RestartAction() {
		setText(Messages.getString("RestartAction.Title"));
	}
	
	@Override
	public void run() {
		AnalyseApplication.restart = true;
		AnalyseApplication.withLog = false;
		AnalyseApplication.getAnalyseApplicationWindow().close();
	}

}
