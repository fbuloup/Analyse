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
import java.util.Iterator;
import java.util.List;

import analyse.Log;
import analyse.model.Function;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.source.SourceViewer;

import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;

public class AnalyseCompletionProcessor implements IContentAssistProcessor {
	
//	/**
//	 * Simple content assist tip closer. The tip is valid in a range
//	 * of 5 characters around its popup location.
//	 */
//	protected static class Validator implements IContextInformationValidator, IContextInformationPresenter {
//
//		protected int fInstallOffset;
//
//		/*
//		 * @see IContextInformationValidator#isContextInformationValid(int)
//		 */
//		public boolean isContextInformationValid(int offset) {
//			return Math.abs(fInstallOffset - offset) < 5;
//		}
//
//		/*
//		 * @see IContextInformationValidator#install(IContextInformation, ITextViewer, int)
//		 */
//		public void install(IContextInformation info, ITextViewer viewer, int offset) {
//			fInstallOffset= offset;
//		}
//
//		/*
//		 * @see org.eclipse.jface.text.contentassist.IContextInformationPresenter#updatePresentation(int, TextPresentation)
//		 */
//		public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
//			return false;
//		}
//	}
	
	
//	protected final static String[] fgProposals=
//	{ "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default", "do", "double", "else", "extends", "false", "final", "finally", "float", "for", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while" };
	
//	protected IContextInformationValidator fValidator= new Validator();

	
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		List<ICompletionProposal> proposalsList = new ArrayList<ICompletionProposal>(0);
		IDocument document = viewer.getDocument();
		
		String qualifier = getQualifier(document, documentOffset);
		computeStructureProposals(viewer,qualifier, documentOffset, proposalsList);
		ICompletionProposal[] proposals = new ICompletionProposal[proposalsList.size()];
		proposalsList.toArray(proposals);
		
		return proposals;
	}

	private String getQualifier(IDocument document, int documentOffset) {
		StringBuffer stringBuffer = new StringBuffer();
		try {
			char c = document.getChar(--documentOffset);
			while (!Character.isWhitespace(c)) {
				stringBuffer.append(c);
				if(Character.toString(c).equals(".")) break; //$NON-NLS-1$
				c = document.getChar(--documentOffset);				
			}
			return stringBuffer.reverse().toString();
		} catch (BadLocationException e) {
			return ""; //$NON-NLS-1$
		}
	}
	
	private String computeQualifier(String qualifier) {
		for (int index = qualifier.length() - 1; index >= 0 ; index--) {
			for (int i = 0; i < SpecialCharacters.CHAR.length; i++) {
				if (qualifier.charAt(index) == SpecialCharacters.CHAR[i]) {
					if(index + 1 == qualifier.length()) return ""; //$NON-NLS-1$
					else return qualifier.substring(index+1);
				}
			}
		}
		return qualifier;
	}
	
	private void computeStructureProposals(ITextViewer viewer, String qualifier, int documentOffset, List<ICompletionProposal> proposalsList) {
		
		qualifier = computeQualifier(qualifier);
		
		for (int i = 0; i < MatlabSyntax.KEYWORDS.length; i++) {
			String keyword = MatlabSyntax.KEYWORDS[i];
			if(keyword.startsWith(qualifier)) {
				CompletionProposal proposal = new CompletionProposal(keyword,documentOffset - qualifier.length(), qualifier.length(), keyword.length(), ImagesUtils.getImage(IImagesKeys.KEYWORD_ICON),null,null,null);
				proposalsList.add(proposal);
			}
		}
		
		
		for (int i = 0; i < MatlabSyntax.KEYWORDS_COMPLETION_DISPLAY.length; i++) {
			String keywordDisplay = MatlabSyntax.KEYWORDS_COMPLETION_DISPLAY[i];
			String keywordReplacement = MatlabSyntax.KEYWORDS_COMPLETION_REPLACEMENT[i];
			if(keywordDisplay.startsWith(qualifier)) {
				IDocument document = viewer.getDocument();
				int nbTabToInsert = 0;
				try {
					int localDocumentOffset = documentOffset - 1;
					char searchBeginLine = 0;
					while (searchBeginLine != 10 && localDocumentOffset != -1) {
						searchBeginLine = document.getChar(localDocumentOffset);
						if(searchBeginLine == 9) nbTabToInsert++;
						localDocumentOffset--;
					}
				} catch (BadLocationException e) {
					Log.logErrorMessage(e);
				}
				for (int j = 0; j < nbTabToInsert; j++) keywordReplacement = keywordReplacement.replaceAll("\\n", "\n\t"); //$NON-NLS-1$ //$NON-NLS-2$
				CompletionProposal proposal = new CompletionProposal(keywordReplacement,documentOffset - qualifier.length(), qualifier.length(), keywordReplacement.length(), ImagesUtils.getImage(IImagesKeys.KEYWORD_ICON),keywordDisplay,null,null);
				proposalsList.add(proposal);
			}
		}
		
		
		List<String> analyseKeywords = new ArrayList<String>(0);
		List<String> analyseParameters = new ArrayList<String>(0);
		
		if(viewer instanceof SourceViewer) {
			Function function = (Function) ((SourceViewer) viewer).getData("function"); //$NON-NLS-1$
			
			for (int i = 0; i < function.getSignalsUsedNumber(); i++) analyseKeywords.add("InputSignal" + (i+1)); //$NON-NLS-1$
			for (int i = 0; i < function.getFieldsUsedNumber(); i++) analyseKeywords.add("InputField" + (i+1)); //$NON-NLS-1$
			for (int i = 0; i < function.getMarkersUsedNumber(); i++) analyseKeywords.add("InputMarker" + (i+1)); //$NON-NLS-1$
			
			for (int i = 0; i < function.getSignalsCreatedNumber(); i++) analyseKeywords.add("OutputSignal" + (i+1)); //$NON-NLS-1$
			for (int i = 0; i < function.getFieldsCreatedNumber(); i++) analyseKeywords.add("OutputField" + (i+1)); //$NON-NLS-1$
			for (int i = 0; i < function.getMarkersCreatedNumber(); i++) analyseKeywords.add("OutputMarker" + (i+1)); //$NON-NLS-1$
			
			for (int i = 0; i < AnalyseSyntax.KEYWORDS_COMPLETION.length; i++) analyseKeywords.add(AnalyseSyntax.KEYWORDS_COMPLETION[i]);
			
			for (int i = 0; i < function.getParametersCount(); i++) analyseParameters.add(function.getParameterLabel(i));
			
		}
		
		//Analyse keywords
		for (Iterator<String> iterator = analyseKeywords.iterator(); iterator.hasNext();) {
			
			String keyword =  iterator.next();
			//String keywordPoint = "." + keyword;
			
			boolean testQualifier = false;
			boolean testKeywordWithSpecial = false;
			for (int i = 0; i < SpecialCharacters.CHAR.length; i++) {
				String specialChar = String.valueOf(SpecialCharacters.CHAR[i]);
				testQualifier = testQualifier || qualifier.equalsIgnoreCase(specialChar);
				String keywordSpecial = specialChar + keyword;
				testKeywordWithSpecial  = testKeywordWithSpecial || keywordSpecial.toLowerCase().startsWith(qualifier.toLowerCase()); 
			}
			
			boolean testKeyword = keyword.toLowerCase().startsWith(qualifier.toLowerCase()) ;

			boolean test = testKeyword || testQualifier || testKeywordWithSpecial;//keyword.startsWith(qualifier) || qualifier.equals(".") || keywordPoint.startsWith(qualifier);
			if(test) {
				int replacementOffset = documentOffset - qualifier.length();
				int replacementLength = qualifier.length();
				int cursorPosition = keyword.length();
				
				if((testQualifier || testKeywordWithSpecial) && !qualifier.equals("")) { //$NON-NLS-1$
					replacementOffset++;
					replacementLength--;
				}
				
				CompletionProposal proposal = new CompletionProposal(keyword, replacementOffset, replacementLength, cursorPosition, ImagesUtils.getImage(IImagesKeys.CHANNELS_VIEW_ICON),null,null,null);
				proposalsList.add(proposal);
				
			}
		}
		
		//Analyse params
		for (Iterator<String> iterator = analyseParameters.iterator(); iterator.hasNext();) {
			
			String keyword =  iterator.next();
			//String keywordPoint = "." + keyword;
			
			boolean testQualifier = false;
			boolean testKeywordWithSpecial = false;
			for (int i = 0; i < SpecialCharacters.CHAR.length; i++) {
				String specialChar = String.valueOf(SpecialCharacters.CHAR[i]);
				testQualifier = testQualifier || qualifier.equalsIgnoreCase(specialChar);
				String keywordSpecial = specialChar + keyword;
				testKeywordWithSpecial  = testKeywordWithSpecial || keywordSpecial.toLowerCase().startsWith(qualifier.toLowerCase()); 
			}
			
			boolean testKeyword = keyword.toLowerCase().startsWith(qualifier.toLowerCase()) ;

			boolean test = testKeyword || testQualifier || testKeywordWithSpecial;//keyword.startsWith(qualifier) || qualifier.equals(".") || keywordPoint.startsWith(qualifier);
			
			
			if(test) {
				int replacementOffset = documentOffset - qualifier.length();
				int replacementLength = qualifier.length();
				int cursorPosition = keyword.length();
				
				if((testQualifier || testKeywordWithSpecial) && !qualifier.equals("")) { //$NON-NLS-1$
					replacementOffset++;
					replacementLength--;
				}
				
				CompletionProposal proposal = new CompletionProposal(keyword, replacementOffset, replacementLength, cursorPosition, ImagesUtils.getImage(IImagesKeys.PARAMETER_ICON),null,null,null);
				proposalsList.add(proposal);
				
			}
		}
	}

//	public IContextInformation[] computeContextInformation(ITextViewer viewer,int documentOffset) {
//		IContextInformation[] result= new IContextInformation[5];
//		for (int i= 0; i < result.length; i++)
//			result[i]= new ContextInformation(
//					"proposal " + i + "at position " + documentOffset,					
//				//MessageFormat.format(JavaEditorMessages.getString("CompletionProcessor.ContextInfo.display.pattern"), new Object[] { new Integer(i), new Integer(documentOffset) }),  //$NON-NLS-1$
//					"proposal " + i + " valid from " + (documentOffset-5) + " to " + (documentOffset-5));
//				//MessageFormat.format(JavaEditorMessages.getString("CompletionProcessor.ContextInfo.value.pattern"), new Object[] { new Integer(i), new Integer(documentOffset - 5), new Integer(documentOffset + 5)})); //$NON-NLS-1$
//					
//		return result;
//	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.' };
	}

//	public char[] getContextInformationAutoActivationCharacters() {
//		return new char[] { '#' };
//	}

//	public IContextInformationValidator getContextInformationValidator() {
//		return fValidator;
//	}

	public String getErrorMessage() {
		return null;
	}

	public IContextInformation[] computeContextInformation(ITextViewer arg0, int arg1) {
		return null;
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

}
