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

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;

import analyse.resources.ColorsUtils;


/**
 * This class scans comment partitions
 */
public class CommentScanner extends RuleBasedScanner {
  /**
   * CommentScanner constructor
   */
  public CommentScanner() {
    // Create the tokens
    IToken other = new Token(new TextAttribute(ColorsUtils.getColor(ColorsUtils.COMMENT)));
    // Use "other" for default
    setDefaultReturnToken(other);
    // This scanner has an easy job--we need no rules. Anything in a comment
    // partition should be scanned as a comment
  }
}
