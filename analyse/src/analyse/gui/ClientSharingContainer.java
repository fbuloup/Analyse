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
package analyse.gui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.model.Chart;
import analyse.model.Experiment;
import analyse.model.Experiments;
import analyse.model.Folder;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Note;
import analyse.model.Processing;
import analyse.preferences.AnalysePreferences;
import analyse.resources.ColorsUtils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;
import ftpsharing.FTPShareExperimentsClient;
import ftpsharing.IFTPMessageObserver;

@SuppressWarnings("deprecation")
public class ClientSharingContainer implements IFTPMessageObserver, IResourceObserver {

	private static StyledText clientMessagesStyledText;

	private TableViewer availableSharedExperimentListViewer;

	private TableViewer filesTableViewer;

	private CLabel selectedExperimentLabel;

	private Combo experimentCombo;

	private static CTabItem clientTabItem;
	
	private static StyleRange clientMessageStyle;
	private static StyleRange clientWarningMessageStyle;
	private static StyleRange clientErrorMessageStyle;
	
	public static CTabItem createContainer(final CTabFolder sharingTabFolder, final View view) {
		new ClientSharingContainer(sharingTabFolder, view);
		return clientTabItem;
	}

	private ClientSharingContainer(final CTabFolder sharingTabFolder, final View view) {
		
		clientMessageStyle = new StyleRange();
		clientMessageStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);		
		clientMessageStyle.fontStyle = SWT.BOLD;
				
		clientWarningMessageStyle = new StyleRange();
		clientWarningMessageStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW);		
		clientWarningMessageStyle.fontStyle = SWT.BOLD;
		
		clientErrorMessageStyle = new StyleRange();
		clientErrorMessageStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);		
		clientErrorMessageStyle.fontStyle = SWT.BOLD;
		
		clientTabItem = new CTabItem(sharingTabFolder, SWT.NONE);
		clientTabItem.setText(Messages.getString("SharingView.ClientTabItemTitle"));
		
		Experiments.getInstance().addExperimentObserver(this);
		clientTabItem.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Experiments.getInstance().removeExperimentObserver(ClientSharingContainer.this);
			}
		});
		
		SashForm clientSashForm = new SashForm(sharingTabFolder, SWT.NONE);
		clientTabItem.setControl(clientSashForm);
		
		Composite availableSharedExperimentComposite = new Composite(clientSashForm, SWT.BORDER);
		availableSharedExperimentComposite.setLayout(new GridLayout(2, false));
		
		CLabel serverIpAdressLabel = new CLabel(availableSharedExperimentComposite, SWT.NONE);
		serverIpAdressLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		serverIpAdressLabel.setText(Messages.getString("SharingView.ServerIPLabelTitle"));
		
		final Text IPText = new Text(availableSharedExperimentComposite, SWT.BORDER);
		
		IPText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		IPText.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				view.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				view.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				sharingTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				sharingTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);
			}
			public void focusGained(FocusEvent e) {
				view.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				view.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				sharingTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				sharingTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);
			}
		});
		
		final Button connectButton = new Button(availableSharedExperimentComposite, SWT.FLAT);
		connectButton.setImage(ImagesUtils.getImage(IImagesKeys.CONNECT_SERVER_ICON));
		connectButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		connectButton.setToolTipText(Messages.getString("SharingView.ConnectButtonToolTip"));
		connectButton.setText(Messages.getString("SharingView.ConnectButtonToolTip"));
		connectButton.setEnabled(false);
		connectButton.addFocusListener(view);
		connectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if(!FTPShareExperimentsClient.isConnected()) {
					final ConnectionDialog connectionDialog = new ConnectionDialog(null, "FTP Connection", AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.FTP_SHARING_LOGIN));
					if(connectionDialog.open() == Window.OK) {
						AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.FTP_SHARING_LOGIN, connectionDialog.getLogin());
						final String ip = IPText.getText();
						IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException,InterruptedException {
								try {
									FTPShareExperimentsClient.connect(ip, connectionDialog.getLogin(), connectionDialog.getPwd(), monitor);
									Log.logMessage(Messages.getString("SharingView.ClientConnected"));
									updateMessage(Messages.getString("SharingView.ClientConnected"));
									getSharedExperimentsList(monitor);
									monitor.done();
								} catch (SocketException e) {
									FTPShareExperimentsClient.resetConnection();
									Log.logErrorMessage(e);
									updateErrorMessage(e.getMessage());
								} catch (IOException e) {
									FTPShareExperimentsClient.resetConnection();
									Log.logErrorMessage(e);
									updateErrorMessage(e.getMessage());
								}
							}
						};
						AnalyseApplication.getAnalyseApplicationWindow().run(true, true, false, true, runnableWithProgress);
//							FTPShareExperimentsClient.addObserver(ClientSharingContainer.this);
						if(FTPShareExperimentsClient.isConnected()) {
							connectButton.setImage(ImagesUtils.getImage(IImagesKeys.DISCONNECT_USER_ICON));
							connectButton.setToolTipText(Messages.getString("SharingView.DisconnectClientButtonToolTip"));
							connectButton.setText(Messages.getString("SharingView.DisconnectClientButtonToolTip"));
						}
					}
				} else {
					try {
						FTPShareExperimentsClient.disconnect();
						Log.logMessage(Messages.getString("SharingView.ClientDisonnected"));
						updateMessage(Messages.getString("SharingView.ClientDisonnected"));
//						FTPShareExperimentsClient.removeObserver(ClientSharingContainer.this);
					} catch (IOException e) {
						Log.logErrorMessage(e);
						updateErrorMessage(e.getMessage());
					} finally {
						FTPShareExperimentsClient.resetConnection();
						connectButton.setImage(ImagesUtils.getImage(IImagesKeys.CONNECT_SERVER_ICON));
						connectButton.setToolTipText(Messages.getString("SharingView.ConnectButtonToolTip"));
						connectButton.setText(Messages.getString("SharingView.ConnectButtonToolTip"));
						availableSharedExperimentListViewer.setInput(null);
						filesTableViewer.setInput(null);
						experimentCombo.removeAll();
					}
				}
			}
		});
		
		IPText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				connectButton.setEnabled(false);
				if(IPText.getText().matches("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b")) {
					connectButton.setEnabled(true);
					AnalysePreferences.getPreferenceStore().setValue(AnalysePreferences.FTP_SHARING_IP, IPText.getText());
				}
			}
		});
		IPText.setText(AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.FTP_SHARING_IP));
		
		CLabel availableSharedExperimentsLabel = new CLabel(availableSharedExperimentComposite, SWT.NONE);
		availableSharedExperimentsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		availableSharedExperimentsLabel.setText(Messages.getString("SharingView.AvailableExperimentsLabelTitle"));
		
		availableSharedExperimentListViewer = new TableViewer(availableSharedExperimentComposite, SWT.BORDER | SWT.V_SCROLL);
		Table availableSharedExperimentList = availableSharedExperimentListViewer.getTable();
		availableSharedExperimentList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		availableSharedExperimentList.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				view.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				view.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				sharingTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				sharingTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);
			}
			public void focusGained(FocusEvent e) {
				view.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				view.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				sharingTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				sharingTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);
			}
		});
		availableSharedExperimentListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				try {
					if(FTPShareExperimentsClient.isConnected())	FTPShareExperimentsClient.changeWorkingDirectory("/");
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					FTPFile element = (FTPFile) selection.getFirstElement();
					if(element != null) {
						FTPShareExperimentsClient.changeWorkingDirectory(element.getName());
						filesTableViewer.setInput(element);
						selectedExperimentLabel.setText(element.getName() + " - [" + element.getGroup() + "]");
					}
					populateCombo();
				} catch (IOException e) {
					Log.logErrorMessage(e);
					updateErrorMessage(e.getMessage());
				}
			}
		});
		availableSharedExperimentListViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}
		});
		availableSharedExperimentListViewer.setLabelProvider(new ILabelProvider() {
			public void removeListener(ILabelProviderListener listener) {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void dispose() {
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public String getText(Object element) {
				FTPFile file = (FTPFile) element;
				return file.getName() + " - [" + file.getGroup() + "]";
			}
			public Image getImage(Object element) {
				return ImagesUtils.getImage(IImagesKeys.EXPERIMENT_ICON);
			}
		});
		
		
		CLabel selectAnExperimentLabel = new CLabel(availableSharedExperimentComposite, SWT.NONE);
		selectAnExperimentLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1));
		selectAnExperimentLabel.setText(Messages.getString("SharingView.SelectExperimentLabelTitle"));
		
		Composite availableFilesComposite = new Composite(clientSashForm, SWT.BORDER);
		availableFilesComposite.setLayout(new GridLayout(3, false));
		
		CLabel contentOfExperimentLabel = new CLabel(availableFilesComposite, SWT.NONE);
		contentOfExperimentLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false,1,1));
		contentOfExperimentLabel.setText(Messages.getString("SharingView.ExperimentContentLabelTitle"));
		
		selectedExperimentLabel = new CLabel(availableFilesComposite, SWT.NONE);
		selectedExperimentLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		selectedExperimentLabel.setText(Messages.getString("SharingView.NoSelectionLabelTitle"));
		
		Table filesTable = new Table(availableFilesComposite, SWT.BORDER | SWT.MULTI);
		filesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		filesTableViewer = new TableViewer(filesTable);
		filesTable.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				view.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				view.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				sharingTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				sharingTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);
			}
			public void focusGained(FocusEvent e) {
				view.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				view.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				sharingTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				sharingTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);
			}
		});
		filesTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				try {
					FTPFile element = (FTPFile) ((IStructuredSelection)event.getSelection()).getFirstElement();
					String experiementLocation = "/" + ((FTPFile) ((IStructuredSelection) availableSharedExperimentListViewer.getSelection()).getFirstElement()).getName();
					String currentLocation = FTPShareExperimentsClient.getCurentLocation();
					if(currentLocation.equals(experiementLocation) && element.getName().equals("..")) return;
					if(element.isDirectory() && !element.getName().endsWith(Folder.EXTENSION)) return;
					FTPShareExperimentsClient.changeWorkingDirectory(element.getName());
					filesTableViewer.setInput(element);
				} catch (IOException e) {
					Log.logErrorMessage(e);
					updateErrorMessage(e.getMessage());
				}
			}
		});
		filesTableViewer.setContentProvider(new IStructuredContentProvider() {
			private Object[] elements;
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(final Object inputElement) {
				elements = null;
				IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							monitor.beginTask(Messages.getString("SharingView.GetChildren") + ((FTPFile)inputElement).getName(), IProgressMonitor.UNKNOWN);
							elements = FTPShareExperimentsClient.getChildren((FTPFile) inputElement);
							monitor.done();
						} catch (IOException e) {
							Log.logErrorMessage(e);
							updateErrorMessage(e.getMessage());
						}
					}
				};
				AnalyseApplication.getAnalyseApplicationWindow().run(true, true, false, true, runnableWithProgress);
				if(elements != null) {
					FTPFile folderUp = new FTPFile();
					folderUp.setName("..");
					ArrayList<Object> list = new ArrayList<Object>(0);
					list.addAll(Arrays.asList(elements));
					list.add(folderUp);
					return list.toArray(new Object[list.size()]);
				}
				return null;
			}
		});
		filesTableViewer.setLabelProvider(new ILabelProvider() {
			public void removeListener(ILabelProviderListener listener) {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void dispose() {
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public String getText(Object element) {
				return ((FTPFile)element).getName().replaceAll("\\.\\w+$", "");
			}
			public Image getImage(Object element) {
				FTPFile file = (FTPFile) element;
				if(file.getName().equals("..")) return ImagesUtils.getImage(IImagesKeys.FOLDER_UP_ICON);
				if(file.getName().endsWith(Folder.EXTENSION)) return ImagesUtils.getImage(IImagesKeys.FOLDER_ICON);
				if(file.getName().endsWith(Chart.EXTENSION)) return ImagesUtils.getImage(IImagesKeys.CHART_EDITOR_ICON);
				if(file.getName().endsWith(Processing.EXTENSION)) return ImagesUtils.getImage(IImagesKeys.PROCESS_EDITOR_ICON);
				if(file.getName().endsWith(Note.EXTENSION)) return ImagesUtils.getImage(IImagesKeys.NOTE_ICON);
				if(file.getName().indexOf(".") == -1) return ImagesUtils.getImage(IImagesKeys.SUBJECT_ICON);
				return ImagesUtils.getImage(IImagesKeys.DATAFILE_ICON);
			}
		});
		filesTableViewer.setSorter(new ViewerSorter());
		
		Button getButton = new Button(availableFilesComposite, SWT.FLAT);
		getButton.setImage(ImagesUtils.getImage(IImagesKeys.IMPORT_ICON));
		getButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL,false,false,1,1));
		getButton.setToolTipText(Messages.getString("SharingView.ImportButtonToolTip"));
		getButton.setText(Messages.getString("SharingView.ImportButtonToolTip"));
		getButton.addFocusListener(view);
		getButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				final String comboValue = experimentCombo.getText();
				IStructuredSelection selection =(IStructuredSelection) filesTableViewer.getSelection();
				Object[] objects = selection.toArray();
				if((objects.length == 1) && (((FTPFile)objects[0]).getName().equals(".."))) objects = new Object[0];
				if(objects.length > 0)
					if(MessageDialog.openConfirm(Display.getDefault().getActiveShell(), Messages.getString("SharingView.ConfirmationTitle"), Messages.getString("SharingView.ConfirmationMessage") + comboValue + " ?")) {
						final ArrayList<FTPFile> filesArrayList = new ArrayList<FTPFile>(0);
						for (int i = 0; i < objects.length; i++) filesArrayList.add((FTPFile) objects[i]);
						IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								if(!comboValue.equals("")) {
									try {
										FTPShareExperimentsClient.importFiles(comboValue, filesArrayList.toArray(new FTPFile[filesArrayList.size()]), monitor);
									} catch (IOException e) {
										Log.logErrorMessage(e);
										updateErrorMessage(e.getMessage());
									}
								}
							}
						};
						AnalyseApplication.getAnalyseApplicationWindow().run(true, true, false, true, runnableWithProgress);
					}
			}
		});
		
		CLabel toLabel = new CLabel(availableFilesComposite, SWT.NONE);
		toLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false,1,1));
		toLabel.setText(Messages.getString("SharingView.ImportToLabel"));
		
		experimentCombo = new Combo(availableFilesComposite, SWT.BORDER | SWT.READ_ONLY);
		experimentCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,1,1));
		experimentCombo.setVisibleItemCount(10);
		populateCombo();
		
		Composite clientMessagesComposite = new Composite(clientSashForm, SWT.BORDER);
		clientMessagesComposite.setLayout(new GridLayout(1, false));
		
		CLabel clientMessagesLabel = new CLabel(clientMessagesComposite, SWT.NONE);
		clientMessagesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		clientMessagesLabel.setText(Messages.getString("SharingView.ClientMessagesLabelTitle"));
		
		clientMessagesStyledText = new StyledText(clientMessagesComposite, SWT.BORDER);
		clientMessagesStyledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		clientMessagesStyledText.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				view.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);		
				view.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				sharingTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.BLACK));
				sharingTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_WIDGET_BACKGROUND)},new int[]{70}, true);
			}
			public void focusGained(FocusEvent e) {
				view.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
				view.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				sharingTabFolder.setSelectionForeground(ColorsUtils.getColor(ColorsUtils.WHITE));
				sharingTabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);
			}
		});
		
		Button clearButton = new Button(clientMessagesComposite, SWT.FLAT);
		clearButton.setImage(ImagesUtils.getImage(IImagesKeys.CLEAR_MESSAGES_ICON));
		clearButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		clearButton.setToolTipText(Messages.getString("SharingView.ClearClientMessagesButtonToolTip"));
		clearButton.setText(Messages.getString("SharingView.ClearClientMessagesButtonToolTip"));
		clearButton.addFocusListener(view);
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clientMessagesStyledText.setText("");
			}
		});
		
		clientSashForm.setWeights(new int[] {1, 1, 1});
	}
	
	public static void clearMessages() {
		clientMessagesStyledText.setText("");
	}

	private void populateCombo() {
		String oldSelection = experimentCombo.getText();
		experimentCombo.removeAll();
		IResource[] experiments = Experiments.getInstance().getChildren();
		String type = "";
		FTPFile file = (FTPFile)((IStructuredSelection)availableSharedExperimentListViewer.getSelection()).getFirstElement();
		if(file != null) type = file.getGroup();
		ArrayList<String> experimentsNamesArrayList = new ArrayList<String>(0);
		for (int i = 0; i < experiments.length; i++) {
			if(type.equals("") || ((Experiment)experiments[i]).getType().equals(type))	experimentsNamesArrayList.add(experiments[i].getLocalPath());
		}
		String[] expStrings = new String[experimentsNamesArrayList.size()];
		expStrings = experimentsNamesArrayList.toArray(new String[experimentsNamesArrayList.size()]);
		Arrays.sort(expStrings);
		for (int i = 0; i < expStrings.length; i++) experimentCombo.add(expStrings[i]);
		if(expStrings.length > 0) {
			experimentCombo.select(0);
			experimentCombo.select(experimentCombo.indexOf(oldSelection));
		}
	}

	protected void getSharedExperimentsList(IProgressMonitor monitor) {
		try {
			final FTPFile[] experiments = FTPShareExperimentsClient.getSharedExperiments(monitor);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					availableSharedExperimentListViewer.setInput(experiments);
				}
			});
		} catch (IOException e) {
			Log.logErrorMessage(e);
			updateErrorMessage(e.getMessage());
		}
	}

	private void logClientMessage(final String message, final StyleRange style) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if(!clientMessagesStyledText.isDisposed() && message != null) {
					clientMessagesStyledText.setCaretOffset(clientMessagesStyledText.getText().length());								
					clientMessagesStyledText.append(message + "\n");				
					style.start = clientMessagesStyledText.getText().length() - message.length() - 1;
					style.length = message.length();
					clientMessagesStyledText.setStyleRange(style);
				}
			}
		});
		
	}

	private void updateErrorMessage(String errorMessage) {
		logClientMessage(errorMessage, clientErrorMessageStyle);
	}

	private void updateMessage(final String message) {
		logClientMessage(message, clientMessageStyle);
	}

//	private void updateWarningMessage(String warningMessage) {
//		logClientMessage(warningMessage, clientWarningMessageStyle);
//	}
	
	private class ConnectionDialog extends Dialog {

		private String shellTitle;
		
		private String login, pwd;

		protected ConnectionDialog(Shell parentShell, String shellTitle, String login) {
			super(parentShell);
			this.shellTitle = shellTitle;
			this.login = login;
		}
		
		@Override
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText(shellTitle);
			shell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite container = (Composite) super.createDialogArea(parent);
			
			GridLayout layout = (GridLayout) container.getLayout();
			layout.numColumns = 2;
			
			Label loginLabel = new Label(container, SWT.NONE);
			loginLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			loginLabel.setText("Login :");

			final Text loginText = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			loginText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			loginText.setText(login);
			
			Label pwdLabel = new Label(container, SWT.NONE);
			pwdLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			pwdLabel.setText("Password :");
			
			final Text pwdText = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER | SWT.PASSWORD);
			pwdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			
			loginText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					login = loginText.getText();
				}
			});
			
			pwdText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					pwd = pwdText.getText();
				}
			});
			
			if(!login.equals("")) pwdText.setFocus();
			
			return container;
		}
		
		public String getLogin() {
			return login;
		}
		
		public String getPwd() {
			return pwd;
		}
	}

	public void update(int message, IResource[] resources) {
		if(message == IResourceObserver.DELETED || message == IResourceObserver.EXPERIMENT_CREATED) {
			populateCombo();
			if(message == IResourceObserver.EXPERIMENT_CREATED) experimentCombo.select(experimentCombo.indexOf(resources[0].getName()));
		}
	}

	public void update(int messageID, String message) {
		// TODO Auto-generated method stub
		
	}


	
}
