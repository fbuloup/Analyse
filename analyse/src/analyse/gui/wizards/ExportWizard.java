/*******************************************************************************
 * Universit� d�Aix Marseille (AMU) - Centre National de la Recherche Scientifique (CNRS)
 * Copyright 2014 AMU-CNRS All Rights Reserved.
 * 
 * These computer program listings and specifications, herein, are
 * the property of Universit� d�Aix Marseille and CNRS
 * shall not be reproduced or copied or used in whole or in part as
 * the basis for manufacture or sale of items without written permission.
 * For a license agreement, please contact:
 * <mailto: licensing@sattse.com> 
 * 
 * Author : Frank BULOUP
 * Institut des Sciences du Mouvement - franck.buloup@univ-amu.fr
 ******************************************************************************/
package analyse.gui.wizards;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;

import analyse.Log;
import analyse.model.Experiment;
import analyse.model.Field;
import analyse.model.Folder;
import analyse.model.IResource;
import analyse.model.Marker;
import analyse.model.Signal;
import analyse.model.Subject;
import analyse.resources.Messages;

public class ExportWizard extends Wizard {

	private ExportSelectionPage exportSelectionPage; 
	private ExportMarkersPage exportMarkersPage;
	private ExportSignalsPage exportSignalsPage;
	private ExportFieldsPage exportFieldsPage;
	private ExportExperimentsPage exportExperimentsPage;
	private ExportSubjectsPage exportSubjectsPage;
	
	
	private Hashtable<String, List<Signal>> filesAndSignals = new Hashtable<String, List<Signal>>(0);
	private Hashtable<String, List<Marker>> filesAndMarkers = new Hashtable<String, List<Marker>>(0); 
	private Hashtable<String, List<Field>> filesAndFields = new Hashtable<String, List<Field>>(0); 
	
	private List<Object> selection;
	private String exportDirectory;
	private String charSeparator;
	private String functionName;
	private boolean exportInSingleFile;
	
	public ExportWizard() {
		super();
		setWindowTitle(Messages.getString("ExportWizard.Title")); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public void addPages() {
		exportSelectionPage = new ExportSelectionPage();
		addPage(exportSelectionPage);
		exportSignalsPage = new ExportSignalsPage();
		exportMarkersPage = new ExportMarkersPage();
		exportFieldsPage = new ExportFieldsPage();
		exportExperimentsPage = new ExportExperimentsPage();
		exportSubjectsPage = new ExportSubjectsPage();
		
		
		addPage(exportSignalsPage);
		addPage(exportMarkersPage);
		addPage(exportFieldsPage);
		addPage(exportExperimentsPage);
		addPage(exportSubjectsPage);
		
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		
		if(page instanceof ExportSelectionPage) {
			if(exportSelectionPage.isMarkers()) return exportMarkersPage;
			if(exportSelectionPage.isSignals()) return exportSignalsPage;
			if(exportSelectionPage.isFields()) return exportFieldsPage;
			if(exportSelectionPage.isCustom()) return exportSubjectsPage;
			if(exportSelectionPage.isExperiments()) return exportExperimentsPage;
		}
		return null;
	}
	
	@Override
	public boolean performFinish() {
		if (exportSelectionPage.isSignals()) {
			Log.logMessage(Messages.getString("ExportWizard.AnalyseMessage3")); //$NON-NLS-1$
			try {
				//Force fork to false in order to run with JACOB COM Bridge
				getContainer().run(false, true, new IRunnableWithProgress() {
					@SuppressWarnings("rawtypes")
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask(Messages.getString("ExportWizard.Task2Title"), 1); //$NON-NLS-1$
						monitor.subTask(Messages.getString("ExportWizard.Subtask3Title")); //$NON-NLS-1$
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								selection = exportSignalsPage.getSelection();
								exportDirectory  = exportSelectionPage.getExportDirectory();
								charSeparator  = exportSelectionPage.getCharSeparator();
								exportInSingleFile = exportSelectionPage.getExportInSingleFile();
							}
						});
						filesAndSignals.clear();
						for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
							Object object = (Object) iterator.next();
							if(object instanceof Signal) {
								Signal signal = (Signal) object;
								String fileNameKey = "";
								if(!exportInSingleFile) fileNameKey = ((Subject) signal.getParent()).getLocalPath() + ".signals.txt"; //$NON-NLS-1$
								else fileNameKey = ((Subject) signal.getParent()).getParent().getLocalPath() + ".signals.txt"; //$NON-NLS-1$
								List<Signal> signals = filesAndSignals.get(fileNameKey);
								if(signals == null) signals = new ArrayList<Signal>(0);
								signals.add(signal);
								filesAndSignals.put(fileNameKey, signals);
							}
						}
						monitor.worked(1);
						
						Enumeration<String> filesNamesKeys = filesAndSignals.keys();
						ArrayList<String> createdFilesNames = new ArrayList<String>(0); 
						try {
							while(filesNamesKeys.hasMoreElements()) {
								String fileName = filesNamesKeys.nextElement();
								List<Signal> signals = filesAndSignals.get(fileName);
								try {
									String fullFileName = exportDirectory + File.separator + fileName;
									if(!createdFilesNames.contains(fullFileName)) {
										new File(fullFileName).delete();
										createdFilesNames.add(fullFileName);
									}
									PrintWriter file = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, true)));
									try {
										for (Iterator iterator = signals.iterator(); iterator.hasNext();) {
											Signal signal = (Signal) iterator.next();
											IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
											String fullSignalName = signal.getParent().getLocalPath() + "." + signal.getName();
											double sf = mathEngine.getSampleFrequency(fullSignalName);
											int nbTrials = mathEngine.getNbTrials(fullSignalName);
											monitor.beginTask(Messages.getString("ExportWizard.Task2Title") + fullSignalName,nbTrials); //$NON-NLS-1$
											if(!exportInSingleFile) file.println("Signal name" + charSeparator + signal.getName()); //$NON-NLS-1$
											else file.println("Signal name" + charSeparator + signal.getParent().getName() + "." + signal.getName()); //$NON-NLS-1$
											file.println("Fe" + charSeparator + sf); //$NON-NLS-1$
											file.println("Trial number" + charSeparator + "Category" + charSeparator + "Values"); //$NON-NLS-1$ //$NON-NLS-2$
											for (int i = 0; i < nbTrials; i++) {
												monitor.subTask("Trial " + (i+1) + " / " + nbTrials); //$NON-NLS-1$
												String categoryName = mathEngine.getCategoryNameFromTrial(signal.getParent().getLocalPath(), i+1);
												double[] values = mathEngine.getValuesForTrialNumber(i+1, fullSignalName); 
												StringBuffer stringBuffer = new StringBuffer(String.valueOf(i + 1) + charSeparator + categoryName + charSeparator);
												for (int j = 0; j < values.length; j++) {
													stringBuffer.append(values[j] + charSeparator);
												}
												String line = stringBuffer.toString(); 
												line = line.replaceAll(charSeparator+"$", ""); //$NON-NLS-1$ //$NON-NLS-2$
												file.println(line);
												if(monitor.isCanceled()) throw new InterruptedException();
												monitor.worked(1);
											}
										}
										file.close();
										Log.logMessage(Messages.getString("ExportWizard.AnalyseMessage3b") + exportDirectory + File.separator + fileName); //$NON-NLS-1$
									} catch (InterruptedException e) {
										file.close();
										throw new InterruptedException();
									}
									
							    } catch (IOException e) {
							    	Log.logErrorMessage(e);
									monitor.done();
							    }
							}
							monitor.done();
							Log.logMessage(Messages.getString("ExportWizard.AnalyseMessage4")); //$NON-NLS-1$
						} catch (InterruptedException e) {
							filesNamesKeys = filesAndSignals.keys();
							while(filesNamesKeys.hasMoreElements()) {
								String fileName = filesNamesKeys.nextElement();
								File file = new File(fileName);
								if(file.exists()) 
									if(!file.delete()) Log.logErrorMessage(Messages.getString("ExportWizard.ImpossibleDelete") + fileName)	;
							}
							Log.logMessage(Messages.getString("ExportWizard.AnalyseMessage5")); //$NON-NLS-1$
						}
					}
				});
			} catch (InvocationTargetException e) {
				Log.logErrorMessage(e);
			} catch (InterruptedException e) {
				Log.logErrorMessage(e);
			}
		}
		if (exportSelectionPage.isMarkers()) {
			Log.logMessage(Messages.getString("ExportWizard.AnalyseMessage1")); //$NON-NLS-1$
			try {
				//Force fork to false in order to run with JACOB COM Bridge
				getContainer().run(false, true, new IRunnableWithProgress() {
					@SuppressWarnings("rawtypes")
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask(Messages.getString("ExportWizard.Task1Title"), 1); //$NON-NLS-1$
						monitor.subTask(Messages.getString("ExportWizard.Subtask1Title")); //$NON-NLS-1$
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								selection = exportMarkersPage.getSelection();
								exportDirectory  = exportSelectionPage.getExportDirectory();
								charSeparator  = exportSelectionPage.getCharSeparator();
								exportInSingleFile = exportSelectionPage.getExportInSingleFile();
							}
						});
						filesAndMarkers.clear();
						for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
							Object object = (Object) iterator.next();
							if(object instanceof Marker) {
								Marker marker = (Marker) object;
								String fileNameKey = "";
								if(!exportInSingleFile) fileNameKey = ((Subject) marker.getMarkersGroup().getSignal().getParent()).getLocalPath() + ".markers.txt"; //$NON-NLS-1$
								else fileNameKey = ((Subject) marker.getMarkersGroup().getSignal().getParent()).getParent().getLocalPath() + ".markers.txt"; //$NON-NLS-1$
								List<Marker> markers = filesAndMarkers.get(fileNameKey);
								if(markers == null) markers = new ArrayList<Marker>(0);
								markers.add(marker);
								filesAndMarkers.put(fileNameKey, markers);
							}
						}
						monitor.worked(1);
						
						Enumeration<String> filesNamesKeys = filesAndMarkers.keys();
						monitor.beginTask(Messages.getString("ExportWizard.Task1Title"), filesAndMarkers.size()); //$NON-NLS-1$
						ArrayList<String> createdFilesNames = new ArrayList<String>(0); 
						while(filesNamesKeys.hasMoreElements()) {
							String fileName = filesNamesKeys.nextElement();
							List<Marker> markers = filesAndMarkers.get(fileName);
//							Object[] markersArray = new Object[markers.size()];
//							int i = 0;
							boolean firstIteration = true;
							int minTrialNumber = 1;
							int maxTrialNumber = 1;
							for (Iterator iterator = markers.iterator(); iterator.hasNext();) {
								Marker marker = (Marker) iterator.next();
								if(firstIteration) {
									minTrialNumber = marker.getTrialNumber();
									maxTrialNumber = marker.getTrialNumber();
									firstIteration = false;
								} else {
									if(marker.getTrialNumber() < minTrialNumber) minTrialNumber = marker.getTrialNumber();
									if(marker.getTrialNumber() > maxTrialNumber) maxTrialNumber = marker.getTrialNumber();
								}
//								markersArray[i] = marker;
//								i++;
							}
							
							try {
								monitor.subTask(Messages.getString("ExportWizard.Subtask2Title") + exportDirectory + File.separator + fileName + "'"); //$NON-NLS-1$ //$NON-NLS-2$
								String fullFileName = exportDirectory + File.separator + fileName;
								if(!createdFilesNames.contains(fullFileName)) {
									new File(fullFileName).delete();
									createdFilesNames.add(fullFileName);
								}
								PrintWriter file = new PrintWriter(new BufferedWriter(new FileWriter(exportDirectory+ File.separator + fileName)));
								//file.println("Trial number" + charSeparator + "Marker label" + charSeparator + "xValue" + charSeparator + "yValue");
//								for (Iterator iterator = markers.iterator(); iterator.hasNext();) {
//									Marker marker = (Marker) iterator.next();
//									String line = marker.getTrialNumber() + charSeparator + ((Signal)marker.getMarkersGroup().getSignal()).getName() + "." + marker.getMarkersGroup().getLabel() + charSeparator + marker.getXValue() + charSeparator + marker.getYValue();
//									file.println(line);
//								}
								IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
								for(int trialNumber = minTrialNumber; trialNumber <= maxTrialNumber; trialNumber++) {
									StringBuffer stringBuffer = new StringBuffer("" + trialNumber);
									for (Iterator iterator = markers.iterator(); iterator.hasNext();) {
										Marker marker = (Marker) iterator.next();
										boolean categoryDone = false;
										if(marker.getTrialNumber() == trialNumber) {
											if(!categoryDone) {
												String categoryName = mathEngine.getCategoryNameFromTrial( ((Signal)marker.getMarkersGroup().getSignal()).getParent().getLocalPath(), trialNumber);
												stringBuffer.append(charSeparator + categoryName);
												categoryDone = true;
											}
											if(!exportInSingleFile) stringBuffer.append(charSeparator + ((Signal)marker.getMarkersGroup().getSignal()).getName() + charSeparator + marker.getMarkersGroup().getLabel() + charSeparator + marker.getX() + charSeparator + marker.getY());
											else stringBuffer.append(charSeparator + ((Signal)marker.getMarkersGroup().getSignal()).getParent().getName() + charSeparator + ((Signal)marker.getMarkersGroup().getSignal()).getName() + charSeparator + marker.getMarkersGroup().getLabel() + charSeparator + marker.getX() + charSeparator + marker.getY());
										}
									}
									file.println(stringBuffer.toString());
								}
								file.close();
								monitor.worked(1);
						    } catch (IOException e) {
						    	Log.logErrorMessage(e);
								monitor.done();
						    } 
						}
						monitor.done();
						
					}
				});
			} catch (InvocationTargetException e) {
				Log.logErrorMessage(e);
			} catch (InterruptedException e) {
				Log.logErrorMessage(e);
			}
			Log.logMessage(Messages.getString("ExportWizard.AnalyseMessage2")); //$NON-NLS-1$
		}
		
		if(exportSelectionPage.isFields()) {
			Log.logMessage(Messages.getString("ExportWizard.AnalyseMessage6")); //$NON-NLS-1$
			try {
				//Force fork to false in order to run with JACOB COM Bridge
				getContainer().run(false, true, new IRunnableWithProgress() {
					@SuppressWarnings("rawtypes")
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
						monitor.beginTask(Messages.getString("ExportWizard.Task3Title"), 1); //$NON-NLS-1$
						monitor.subTask(Messages.getString("ExportWizard.Subtask4Title")); //$NON-NLS-1$
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								selection = exportFieldsPage.getSelection();
								exportDirectory  = exportSelectionPage.getExportDirectory();
								charSeparator  = exportSelectionPage.getCharSeparator();
								exportInSingleFile = exportSelectionPage.getExportInSingleFile();
							}
						});
						filesAndFields.clear();
						for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
							Object object = (Object) iterator.next();
							if(object instanceof Field) {
								Field field = (Field) object;
								String fileNameKey = "";
								if(!exportInSingleFile) fileNameKey = ((Subject) field.getSignal().getParent()).getLocalPath() + ".fields.txt"; //$NON-NLS-1$
								else fileNameKey = ((Subject) field.getSignal().getParent()).getParent().getLocalPath() + ".fields.txt"; //$NON-NLS-1$
								List<Field> fields = filesAndFields.get(fileNameKey);
								if(fields == null) fields = new ArrayList<Field>(0);
								fields.add(field);
								filesAndFields.put(fileNameKey, fields);
							}
						}
						monitor.done();
						monitor.beginTask(Messages.getString("ExportWizard.Task3Title"), filesAndFields.size()); //$NON-NLS-1$
						Enumeration<String> filesNamesKeys = filesAndFields.keys();
						ArrayList<String> createdFilesNames = new ArrayList<String>(0); 
						while(filesNamesKeys.hasMoreElements()) {
							String fileName = filesNamesKeys.nextElement();
							List<Field> fields = filesAndFields.get(fileName);
							try {
								monitor.subTask(Messages.getString("ExportWizard.Subtask5Title") + exportDirectory + File.separator + fileName + "'"); //$NON-NLS-1$ //$NON-NLS-2$
								String fullFileName = exportDirectory + File.separator + fileName;
								if(!createdFilesNames.contains(fullFileName)) {
									new File(fullFileName).delete();
									createdFilesNames.add(fullFileName);
								}
								PrintWriter file = new PrintWriter(new BufferedWriter(new FileWriter(exportDirectory+ File.separator + fileName, true)));
								if(!exportInSingleFile) file.println("Trial number" + charSeparator + "Category" + charSeparator + "Signal name" + charSeparator + "Field name" + charSeparator + "Value");	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								else file.println("Trial number" + charSeparator + "Category" + charSeparator + "Subject name" + charSeparator + "Signal name" + charSeparator + "Field name" + charSeparator + "Value");	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								for (int i = 0; i < fields.size(); i++) {
									Field field = fields.get(i);
									double[] values = field.getValues();
									Signal signal = (Signal) field.getSignal();
									StringBuffer stringBuffer = new StringBuffer();
									for (int j = 0; j < values.length; j++) {
										String categoryName = mathEngine.getCategoryNameFromTrial(signal.getParent().getLocalPath(), j+1);
										if(!exportInSingleFile) stringBuffer.append((j+1) + charSeparator  + categoryName + charSeparator + signal.getName() + charSeparator + field.getLabel() + charSeparator + values[j] + "\n");
										else stringBuffer.append((j+1) + charSeparator  + categoryName + charSeparator + signal.getParent().getName() + charSeparator + signal.getName() + charSeparator + field.getLabel() + charSeparator + values[j] + "\n");
									}
									file.print(stringBuffer.toString());	
								}
								file.close();
								monitor.worked(1);
						    } catch (IOException e) {
						    	Log.logErrorMessage(e);
								monitor.done();
						    } 
						}
						monitor.done();
						
					}
				});
			} catch (InvocationTargetException e) {
				Log.logErrorMessage(e);
			} catch (InterruptedException e) {
				Log.logErrorMessage(e);
			}
			Log.logMessage(Messages.getString("ExportWizard.AnalyseMessage7")); //$NON-NLS-1$
		}
		
		if(exportSelectionPage.isCustom()) {
			Log.logMessage(Messages.getString("ExportWizard.AnalyseMessage9")); //$NON-NLS-1$
			try {
				//Force fork to false in order to run with JACOB COM Bridge
				getContainer().run(false, false, new IRunnableWithProgress() {
					@SuppressWarnings("rawtypes")
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask(Messages.getString("ExportWizard.AnalyseMessage9"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								functionName = exportSelectionPage.getCustomFunction().replaceAll(".m$", "");
								selection = exportSubjectsPage.getSelection();
								exportDirectory  = exportSelectionPage.getExportDirectory();
							}
						});
						StringBuffer matlabCommand = new StringBuffer();
						matlabCommand.append(functionName);
						matlabCommand.append("('");
						for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
							final Object object = (Object) iterator.next();
							if(object instanceof Subject) {
								matlabCommand.append(((Subject)object).getLocalPath());
								matlabCommand.append(":");
							}
						}
						matlabCommand.append("','");
						matlabCommand.append(exportDirectory);
						matlabCommand.append("');");
						
						Log.logMessage(Messages.getString("ExportWizard.AnalyseMessage10") + matlabCommand.toString()); //$NON-NLS-1$
						MathEngineFactory.getInstance().getMathEngine().sendCommand(matlabCommand.toString());
						
						monitor.done();
					}
				});
			} catch (InvocationTargetException e) {
				Log.logErrorMessage(e);
			} catch (InterruptedException e) {
				Log.logErrorMessage(e);
			}
			
			
		}
		
		if(exportSelectionPage.isExperiments()) {
			Log.logMessage(Messages.getString("ExportWizard.AnalyseMessage8")); //$NON-NLS-1$
			try {
				//Force fork to false in order to run with JACOB COM Bridge
				getContainer().run(false, true, new IRunnableWithProgress() {
					@SuppressWarnings("rawtypes")
					public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								selection = exportExperimentsPage.getSelection();
								exportDirectory  = exportSelectionPage.getExportDirectory();
							}
						});
						
						monitor.beginTask(Messages.getString("ExportWizard.Task4Title"), selection.size()); //$NON-NLS-1$
						
						for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
							//Retrieve all files for each selected experiment
							final Object object = (Object) iterator.next();
							if(object instanceof Experiment) {
								
								Experiment experiment = (Experiment) object;
								monitor.subTask(Messages.getString("ExportWizard.Subtask6Title") + " " + experiment.getAbsolutePath()); //$NON-NLS-1$
								IResource[] resources = experiment.getChildren();
								FileOutputStream fileOutputStream;
								
								try {
									fileOutputStream = new FileOutputStream(exportDirectory + File.separator + experiment.getName() + ".zip");
									BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
									ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);
									createZipFile(zipOutputStream, resources, experiment.getName());
									zipOutputStream.flush();
									
									Log.logMessage(Messages.getString("ExportWizard.Message9") + " " + experiment.getAbsolutePath() + File.separatorChar + experiment.getName() + ".properties");
									FileInputStream fileInputStream = new FileInputStream(new File(experiment.getAbsolutePath(), experiment.getName() + ".properties"));
									BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
									ZipEntry zipEntry = new ZipEntry(experiment.getName() + File.separatorChar + experiment.getName() + ".properties");
									zipOutputStream.putNextEntry(zipEntry);
									int count;
									byte[] data = new byte[2048];
									while((count = bufferedInputStream.read(data, 0, 2048)) != -1) {
										zipOutputStream.write(data, 0, count);
									}
									bufferedInputStream.close();
									zipOutputStream.flush();

									zipOutputStream.close();
									
									
								} catch (FileNotFoundException e) {
									Log.logErrorMessage(e);
								} catch (IOException e) {
									Log.logErrorMessage(e);
								}
								monitor.worked(1);
							}
						}
					}
				});
			} catch (InvocationTargetException e) {
				Log.logErrorMessage(e);
			} catch (InterruptedException e) {
				Log.logErrorMessage(e);
			}
			Log.logMessage(Messages.getString("ExportWizard.AnalyseMessage7")); //$NON-NLS-1$
		}
		
		
		return true;
	}

	protected void createZipFile(ZipOutputStream zipOutputStream, IResource[] resources, String parentFolder) {
		if(resources == null) return;
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			try {
				if(resource instanceof Subject || resource instanceof Folder) {
					IResource[] children = resource.getChildren();
					createZipFile(zipOutputStream, children, parentFolder + File.separatorChar + resource.getName());
				} else {
					Log.logMessage(Messages.getString("ExportWizard.Message9") + " " + resource.getAbsolutePath());
					FileInputStream fileInputStream = new FileInputStream(resource.getAbsolutePath());
					BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
					ZipEntry zipEntry = new ZipEntry(parentFolder + File.separatorChar + resource.getName());
					zipOutputStream.putNextEntry(zipEntry);
					int count;
					byte[] data = new byte[2048];
					while((count = bufferedInputStream.read(data, 0, 2048)) != -1) {
						zipOutputStream.write(data, 0, count);
					}
					bufferedInputStream.close();
					zipOutputStream.flush();
				}
			} catch (FileNotFoundException e) {
				Log.logErrorMessage(e);
			} catch (IOException e) {
				Log.logErrorMessage(e);
			}
		}
	}

}
