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

public class SpecialCharsDetector implements IWordDetector {

	public boolean isWordPart(char c) {
		for (int i = 0, n = SpecialCharacters.CHAR.length; i < n; i++)
			if(c == SpecialCharacters.CHAR[i] ) return true;
		return false;
	}

	public boolean isWordStart(char c) {
		for (int i = 0, n = SpecialCharacters.CHAR.length; i < n; i++)
			if(c == SpecialCharacters.CHAR[i] ) return true;
		return false;
	}

}
