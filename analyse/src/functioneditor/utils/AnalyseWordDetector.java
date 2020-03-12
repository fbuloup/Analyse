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
package functioneditor.utils;

import org.eclipse.jface.text.rules.IWordDetector;

public class AnalyseWordDetector implements IWordDetector {

	public boolean isWordPart(char c) {
		for (int i = 0, n = AnalyseSyntax.KEYWORDS.length; i < n; i++)
		      if (((String) AnalyseSyntax.KEYWORDS[i]).indexOf(c) != -1) 
		    	  return true;
		    return false;
	}

	public boolean isWordStart(char c) {
		for (int i = 0, n = AnalyseSyntax.KEYWORDS.length; i < n; i++)
		      if (c == ((String) AnalyseSyntax.KEYWORDS[i]).charAt(0))
		    	  return true;
		return false;
	}

}
