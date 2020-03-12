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
package analyse.gui;

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;

import analyse.model.IResourceObserver;
import analyse.model.Subject;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public final class PreviewSignalsExplorerContainer extends Composite {

		private CLabel subjectNamelabel;
		private Button nextTrialButton;
		private Button previousTrialButton;
		private Chart chart;
		private Combo signalsCombo;
		private static String SERIE_ID = "SERIE";
		private Subject subject;
		private int currentTrial;
		private Combo currentTrialCombo;
		
		PreviewSignalsExplorerContainer(Composite parent) {
			super(parent, SWT.NONE);
			setLayout(new GridLayout(3,false));
			createContent();
		}

		public void update(int message, Subject selectedSubject) {
			
			if(message == IResourceObserver.LOADED ||
					message == IResourceObserver.DELETED ||
					message == IResourceObserver.SELECTION_CHANGED ||
					message == IResourceObserver.CHANNEL_DELETED ||
					message == IResourceObserver.PROCESS_RUN) {

				subjectNamelabel.setText(Messages.getString("NONE"));
				currentTrialCombo.setText("");
				signalsCombo.removeAll();
				currentTrialCombo.removeAll();
				subject = null;
				if(chart.getSeriesSet().getSeries().length == 1) {
					chart.getSeriesSet().deleteSeries(SERIE_ID);
					chart.redraw();
				}
				if(selectedSubject != null)	{
					subjectNamelabel.setText(selectedSubject.getLocalPath() + (selectedSubject.isLoaded()?"":" (" + Messages.getString("NotLoaded") + ")"));
					if(selectedSubject.isLoaded()) {
//						long t1 = System.currentTimeMillis();
						IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
						String[] signalsNames = selectedSubject.getSignalsNames();//mathEngine.getSignalsNames(selectedSubject.getLocalPath());
						signalsCombo.setItems(signalsNames);
						subject = selectedSubject;
						currentTrial = 1;
						signalsCombo.select(0);
						int nbTrials = mathEngine.getNbTrials(selectedSubject.getLocalPath() + "." + signalsCombo.getText());
						for (int i = 0; i < nbTrials; i++) currentTrialCombo.add(Messages.getString("TrialNumber") + (i+1));
						currentTrialCombo.select(0);
//						t1 = System.currentTimeMillis() - t1;
//						System.out.println(">>>>>>>>>>>>>>>>>> Time to update preview : " + t1);
					}
				}
			} else if(message == IResourceObserver.RENAMED) {
				subjectNamelabel.setText(Messages.getString("NONE"));
				if(subject != null)	subjectNamelabel.setText(subject.getLocalPath() + (subject.isLoaded()?"":" (" + Messages.getString("NotLoaded") + ")"));
			}
		}

		private void createContent() {
			CLabel selectedSubjectlabel = new CLabel(this, SWT.NONE);
			selectedSubjectlabel.setText(Messages.getString("SelectedSubject"));
			selectedSubjectlabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			
			subjectNamelabel = new CLabel(this, SWT.NONE);
			subjectNamelabel.setText(Messages.getString("NONE"));
			subjectNamelabel.setToolTipText(Messages.getString("SelectedSubjectToolTip"));
			subjectNamelabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,2,1));
			
			CLabel selectedSignallabel = new CLabel(this, SWT.NONE);
			selectedSignallabel.setText(Messages.getString("SelectedSignal"));
			selectedSignallabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			
			signalsCombo = new Combo(this, SWT.READ_ONLY);
			signalsCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,2,1));
			signalsCombo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					String signalName = signalsCombo.getText();
					if(!signalName.equals("")) {
						updateChart(signalName);
					}
				}
			});
			signalsCombo.addFocusListener((FocusListener) getParent());
			
			CLabel selectedTriallabel = new CLabel(this, SWT.NONE);
			selectedTriallabel.setText(Messages.getString("ChannelsView.SignalsItemFieldsTabItemSelectTrialLabelTitle"));
			selectedTriallabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			
			currentTrialCombo = new Combo(this, SWT.READ_ONLY);
			currentTrialCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,1,1));
			currentTrialCombo.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					String signalName = signalsCombo.getText();
					currentTrial = currentTrialCombo.getSelectionIndex() + 1;
					if(!signalName.equals("")) {
						updateChart(signalName);
					}
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			Composite container = new Composite(this, SWT.NONE);
			container.setLayout(new GridLayout(2,true));
			container.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
			
			previousTrialButton = new Button(container, SWT.PUSH | SWT.FLAT);
			previousTrialButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			previousTrialButton.setToolTipText(Messages.getString("PreviousTrial"));
			previousTrialButton.setImage(ImagesUtils.getImage(IImagesKeys.PREVIOUS_TRIAL_ICON));
			previousTrialButton.addFocusListener((FocusListener) getParent());
			previousTrialButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					String signalName = signalsCombo.getText();
					if(!signalName.equals("")) {
						if(currentTrial > 1) {							
							currentTrial--;
							currentTrialCombo.select(currentTrial-1);//.setText("Trial n¬∞" + String.valueOf(currentTrial));
							currentTrialCombo.setToolTipText(Messages.getString("TrialNumber") + String.valueOf(currentTrial));
							updateChart(signalName);
						}
					}
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			nextTrialButton = new Button(container, SWT.PUSH | SWT.FLAT);
			nextTrialButton.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			nextTrialButton.setToolTipText(Messages.getString("NextTrial"));
			nextTrialButton.setImage(ImagesUtils.getImage(IImagesKeys.NEXT_TRIAL_ICON));
			nextTrialButton.addFocusListener((FocusListener) getParent());
			nextTrialButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					String signalName = signalsCombo.getText();
					if(!signalName.equals("")) {
						IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
						String fullSignalName = subject.getLocalPath() + "." + signalName;
						int nbTrial = mathEngine.getNbTrials(fullSignalName);
						if(nbTrial >= currentTrial + 1) {
							currentTrial++;
							currentTrialCombo.select(currentTrial-1);//  .setText("Trial n¬∞" + String.valueOf(currentTrial));
							currentTrialCombo.setToolTipText(Messages.getString("TrialNumber") + String.valueOf(currentTrial));
							updateChart(signalName);
						}
					}
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			chart = new Chart(this, SWT.NONE);
			chart.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,5,1));
			chart.getTitle().setVisible(false);
			chart.getLegend().setVisible(false);
			chart.getPlotArea().setForeground(JFaceColors.getBannerBackground(getDisplay()));
			IAxis[] axis = chart.getAxisSet().getAxes();
			for (int i = 0; i < axis.length; i++) {
				axis[i].getTitle().setVisible(false);
				axis[i].getTick().setForeground(JFaceColors.getBannerForeground(getDisplay()));
			}
		}

		protected void updateChart(String signalName) {
			IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
			String fullSignalName = subject.getLocalPath() + "." + signalName;
			double sampleFrequency = mathEngine.getSampleFrequency(fullSignalName);
			double[] signalValues = mathEngine.getValuesForTrialNumber(currentTrial, fullSignalName);
			double[] timeValues = new double[signalValues.length];
			for (int i = 0; i < timeValues.length; i++) timeValues[i] = i / sampleFrequency;
			ILineSeries lineSerie = null;
			if(chart.getSeriesSet().getSeries().length == 1) lineSerie = (ILineSeries) chart.getSeriesSet().getSeries()[0];
			else {
				lineSerie = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, SERIE_ID);
				lineSerie.setSymbolType(PlotSymbolType.NONE);
				lineSerie.setLineColor(getForeground());
				lineSerie.setLineWidth(2);
			}
			lineSerie.setYSeries(signalValues);
			lineSerie.setXSeries(timeValues);
			if(AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.CHARTS_ANTIALIASIS))
			lineSerie.setAntialias(SWT.ON);
			else lineSerie.setAntialias(SWT.OFF);
			chart.getAxisSet().adjustRange();
			chart.redraw();
		}
		
	}
