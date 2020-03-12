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

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class MatlabPartitionScanner extends RuleBasedPartitionScanner {

	public static final String COMMENT = "comment"; //$NON-NLS-1$
	  public static final String[] TYPES = new String[]{COMMENT};

	  /**
	   * PerlPartitionScanner constructor
	   */
	  public MatlabPartitionScanner() {
	    super();
	    // Create the token for comment partitions
	    IToken comment = new Token(COMMENT);
	    // Set the rule--anything from % to the end of the line is a comment
	    setPredicateRules(new IPredicateRule[] { new EndOfLineRule("%", comment)}); //$NON-NLS-1$
	  }
	
}
