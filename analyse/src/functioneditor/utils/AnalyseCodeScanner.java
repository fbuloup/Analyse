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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;

import analyse.resources.ColorsUtils;

public class AnalyseCodeScanner extends RuleBasedScanner {

	@SuppressWarnings({ "unchecked"})
	public AnalyseCodeScanner() {
	    // Create the tokens for keywords, strings, and other (everything else)
	    IToken keyword = new Token(new TextAttribute(ColorsUtils.getColor(ColorsUtils.KEYWORD),ColorsUtils.getColor(ColorsUtils.WHITE), SWT.BOLD));
	    IToken other = new Token(new TextAttribute(ColorsUtils.getColor(ColorsUtils.BLACK),ColorsUtils.getColor(ColorsUtils.WHITE), SWT.BOLD));
	    IToken string = new Token(new TextAttribute(ColorsUtils.getColor(ColorsUtils.STRING)));
	    IToken analyseKeyword = new Token(new TextAttribute(ColorsUtils.getColor(ColorsUtils.ANALYSE_KEYWORD)));
	    IToken number = new Token(new TextAttribute(ColorsUtils.getColor(ColorsUtils.ANALYSE_KEYWORD)));
//	    IToken specialCharsKeyword = new Token(new TextAttribute(ColorsUtils.getColor(ColorsUtils.BLACK)));

	    // Use "other" for default
	    setDefaultReturnToken(other);

	    // Create the rules
	    @SuppressWarnings("rawtypes")
		List rules = new ArrayList();
	    
	    // Add rules for strings
	    rules.add(new SingleLineRule("\"", "\"", string, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
	    rules.add(new SingleLineRule("'", "'", string, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
	    
	    // Add rule for whitespace
	    rules.add(new WhitespaceRule(new IWhitespaceDetector() {
	      public boolean isWhitespace(char c) {
	        return Character.isWhitespace(c);
	      }
	    }));
	    

//	    WordRule specialCharsRule = new WordRule(new SpecialCharsDetector(), other);
//	    for (int i = 0, n = SpecialCharacters.CHAR.length; i < n; i++)
//	    	specialCharsRule.addWord( Character.toString(SpecialCharacters.CHAR[i]), specialCharsKeyword);
//	    rules.add(specialCharsRule);
	    
	    
	    WordRule analyseWordRule = new WordRule(new AnalyseWordDetector(), other);
	    for (int i = 0, n = AnalyseSyntax.KEYWORDS.length; i < n; i++)
	    	analyseWordRule.addWord(AnalyseSyntax.KEYWORDS[i], analyseKeyword);
	    rules.add(analyseWordRule);
	    
	    // Add rule for keywords, and add the words to the rule
	    WordRule wordRule = new WordRule(new MatlabWordDetector(), other);
	    for (int i = 0, n = MatlabSyntax.KEYWORDS.length; i < n; i++)
	      wordRule.addWord(MatlabSyntax.KEYWORDS[i], keyword);
	    rules.add(wordRule);
	    
	    NumberRule numberRule = new NumberRule(number);
	    rules.add(numberRule);

	    IRule[] result = new IRule[rules.size()];
	    rules.toArray(result);
	    setRules(result);
	    
	    
	}

}
