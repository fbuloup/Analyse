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

public class MatlabSyntax {
	public static final String[] KEYWORDS = { 
		"if", //$NON-NLS-1$
		"break", //$NON-NLS-1$
		"case", //$NON-NLS-1$
		"catch", //$NON-NLS-1$
		"classdef", //$NON-NLS-1$
		"continue", //$NON-NLS-1$
		"else", //$NON-NLS-1$
		"elseif", //$NON-NLS-1$
		"end", //$NON-NLS-1$
		"for", //$NON-NLS-1$
		"function", //$NON-NLS-1$
		"global", //$NON-NLS-1$
		"otherwise", //$NON-NLS-1$
		"parfor", //$NON-NLS-1$
		"persistent", //$NON-NLS-1$
		"return", //$NON-NLS-1$
		"spmd", //$NON-NLS-1$
		"switch", //$NON-NLS-1$
		"try", //$NON-NLS-1$
		"while" //$NON-NLS-1$
	};
	
	public static final String[] KEYWORDS_COMPLETION_REPLACEMENT = {
		"if\nend", //$NON-NLS-1$
		"if\nelse\nend", //$NON-NLS-1$
		"if\nelseif\nelse\nend", //$NON-NLS-1$
		"switch\n\tcase\n\totherwise\nend", //$NON-NLS-1$
		"for\nend", //$NON-NLS-1$
		"while\nend", //$NON-NLS-1$
		"try\ncatch\nend" //$NON-NLS-1$
	};
	
	public static final String[] KEYWORDS_COMPLETION_DISPLAY = {
		"if...end", //$NON-NLS-1$
		"if...else...end", //$NON-NLS-1$
		"if...elsif...else...end", //$NON-NLS-1$
		"switch...case...otherwise...end", //$NON-NLS-1$
		"for...end", //$NON-NLS-1$
		"while...end", //$NON-NLS-1$
		"try...catch...end" //$NON-NLS-1$
	};
	  
}
