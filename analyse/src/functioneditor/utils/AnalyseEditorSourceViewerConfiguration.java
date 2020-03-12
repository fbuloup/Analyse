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


import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.*;
import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import functioneditor.controllers.FunctionEditorController;

import analyse.resources.ColorsUtils;

/**
 * This class provides the source viewer configuration
 */
public class AnalyseEditorSourceViewerConfiguration extends SourceViewerConfiguration {
	/**
	 * Gets the presentation reconciler. This will color the code.
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		// Create the presentation reconciler
		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		// Create the damager/repairer for comment partitions
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new CommentScanner());
		reconciler.setDamager(dr, MatlabPartitionScanner.COMMENT);
		reconciler.setRepairer(dr, MatlabPartitionScanner.COMMENT);

		// Create the damager/repairer for default
		dr = new DefaultDamagerRepairer(FunctionEditorController.matlabCodeScanner);
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		return reconciler;
	}

	/**
	 * Gets the configured document partitioning
	 * 
	 * @return String
	 */
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return FunctionEditorController.MATLAB_PARTITIONING;
	}

	/**
	 * Gets the configured partition types
	 * 
	 * @return String[]
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE, MatlabPartitionScanner.COMMENT};
		
	}
  
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();

		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		assistant.setContentAssistProcessor(new AnalyseCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		
		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(100);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);

		assistant.setProposalSelectorBackground(ColorsUtils.getColor(ColorsUtils.WHITE));
		assistant.setProposalSelectorForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
		
		//assistant.setStatusLineVisible(true);
		//assistant.setContextInformationPopupBackground();
		//assistant.setContextInformationPopupForeground();
		
		return assistant;
	}
	
	@Override
	public int getTabWidth(ISourceViewer sourceViewer) {
		return 16;
	}
  
}
