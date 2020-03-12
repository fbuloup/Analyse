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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.apache.ftpserver.ftplet.FtpException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import analyse.Log;
import analyse.gui.dialogs.ExperimentsSharingSelectionDialog;
import analyse.model.Experiments;
import analyse.preferences.AnalysePreferences;
import analyse.resources.ColorsUtils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;
import ftpsharing.FTPShareExperimentsServer;
import ftpsharing.IFTPMessageObserver;

@SuppressWarnings("deprecation")
public class ServerSharingContainer implements IFTPMessageObserver {
	
	private TableViewer sharedExperimentListViewer;
	private static StyledText serverStyledText;
	private TableViewer connectedUsersListViewer;
	private static CTabItem serverTabItem;
	
	private static StyleRange serverMessageStyle;
	private static StyleRange serverWarningMessageStyle;
	private static StyleRange serverErrorMessageStyle;

	public static CTabItem createContainer(final CTabFolder sharingTabFolder, final View view) {
		new ServerSharingContainer(sharingTabFolder, view);
		return serverTabItem;
	}
	
	private ServerSharingContainer(final CTabFolder sharingTabFolder, final View view) {
		
		serverMessageStyle = new StyleRange();
		serverMessageStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);		
		serverMessageStyle.fontStyle = SWT.BOLD;
				
		serverWarningMessageStyle = new StyleRange();
		serverWarningMessageStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW);		
		serverWarningMessageStyle.fontStyle = SWT.BOLD;
		
		serverErrorMessageStyle = new StyleRange();
		serverErrorMessageStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);		
		serverErrorMessageStyle.fontStyle = SWT.BOLD;
		
		BufferedReader buffer = null;
		String ipString = "";
		try {
			URL url = new URL("http://automation.whatismyip.com/n09230945.asp");
			InputStreamReader in = new InputStreamReader(url.openStream());
			buffer = new BufferedReader(in);
			ipString = buffer.readLine() + " ";
		} catch (IOException e) {
			Log.logErrorMessage(e);
		} finally {
			try {
				if (buffer != null)  buffer.close();
			} catch (IOException e) {
					Log.logErrorMessage(e);
			}
		}		
		
		serverTabItem = new CTabItem(sharingTabFolder, SWT.NONE);
		serverTabItem.setText(Messages.getString("SharingView.ServerTabItemTitle") +  ": " + ipString);
		
		SashForm serverSashForm = new SashForm(sharingTabFolder, SWT.NONE);
		serverTabItem.setControl(serverSashForm);
		
		Composite sharedExperimentsContainer = new Composite(serverSashForm, SWT.BORDER);
		sharedExperimentsContainer.setLayout(new GridLayout(1, true));
		
		CLabel sharedExperimentsListLabel = new CLabel(sharedExperimentsContainer, SWT.NONE);
		sharedExperimentsListLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		sharedExperimentsListLabel.setText(Messages.getString("SharingView.SharedExperimentListLabelTitle"));
		
		sharedExperimentListViewer = new TableViewer(sharedExperimentsContainer, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		Table sharedExperimentsList = sharedExperimentListViewer.getTable();
		sharedExperimentsList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));
		sharedExperimentsList.addFocusListener(new FocusListener() {
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
		sharedExperimentListViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}
		});
		sharedExperimentListViewer.setLabelProvider(new ILabelProvider() {
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
				return (String) element;
			}
			public Image getImage(Object element) {
				return ImagesUtils.getImage(IImagesKeys.EXPERIMENT_ICON);
			}
		});
		sharedExperimentListViewer.setSorter(new ViewerSorter());
		loadSharedExperimentsFile();
		
		Composite buttonsContainer = new Composite(sharedExperimentsContainer, SWT.FLAT);
		buttonsContainer.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_buttonsContainer = new GridLayout(3, false);
		gl_buttonsContainer.marginHeight = 0;
		buttonsContainer.setLayout(gl_buttonsContainer);
		
		Button removeButton = new Button(buttonsContainer, SWT.FLAT);
		removeButton.setImage(ImagesUtils.getImage(IImagesKeys.DELETE_ICON));
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		removeButton.setToolTipText(Messages.getString("SharingView.RemoveExperimentsButtonToolTip"));
		removeButton.addFocusListener(view);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] selectionToDeleteTemp = ((IStructuredSelection)sharedExperimentListViewer.getSelection()).toArray();
				String[] selectionToDelete = Arrays.asList(selectionToDeleteTemp).toArray(new String[selectionToDeleteTemp.length]);
				String[] currentSelection = (String[]) sharedExperimentListViewer.getInput();
				ArrayList<String> currentSelectionList = new ArrayList<String>(0);
				for (int i = 0; i < currentSelection.length; i++) currentSelectionList.add(currentSelection[i]);
				for (int i = 0; i < selectionToDelete.length; i++) currentSelectionList.remove(currentSelectionList.indexOf(selectionToDelete[i]));
				sharedExperimentListViewer.setInput(currentSelectionList.toArray(new String[currentSelectionList.size()]));
				saveSharedExperimentsFile();
			}
		});
		
		Button addButton = new Button(buttonsContainer, SWT.FLAT);
		addButton.setImage(ImagesUtils.getImage(IImagesKeys.ADD_ICON));
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		addButton.setToolTipText(Messages.getString("SharingView.AddExperimentsButtonToolTip"));
		addButton.addFocusListener(view);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				HashSet<String> experiments = Experiments.getInstance().getExperimentsNamesList();
				String[] currentSelection = (String[]) sharedExperimentListViewer.getInput();
				if(currentSelection != null)
					for (int i = 0; i < currentSelection.length; i++) experiments.remove(currentSelection[i]);
				String[] allExperimentsString = experiments.toArray(new String[experiments.size()]);
				ExperimentsSharingSelectionDialog experimentsSelectionDialog = new ExperimentsSharingSelectionDialog(null, allExperimentsString);
				if(experimentsSelectionDialog.open() == Window.OK) {
					String[] addSelection = experimentsSelectionDialog.getSelection();
					String[] oldSelection = (String[]) sharedExperimentListViewer.getInput();
					HashSet<String> newSelection = new HashSet<String>(0);
					if(oldSelection != null) newSelection.addAll(Arrays.asList(oldSelection));
					newSelection.addAll(Arrays.asList(addSelection));
					sharedExperimentListViewer.setInput(newSelection.toArray(new String[newSelection.size()]));
					saveSharedExperimentsFile();
				}
			}
		});
		
		final Button startButton = new Button(buttonsContainer, SWT.FLAT);
		startButton.setImage(ImagesUtils.getImage(IImagesKeys.START_SERVER_ICON));
		startButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		startButton.setToolTipText(Messages.getString("SharingView.StartServerButtonToolTip"));
		startButton.setText(Messages.getString("SharingView.StartServerButtonToolTip"));
		startButton.addFocusListener(view);
		startButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				if(!FTPShareExperimentsServer.isStarted()) {
					try {
						FTPShareExperimentsServer.startServer();
						Log.logMessage(Messages.getString("SharingView.ServerStarted"));
						updateMessage(Messages.getString("SharingView.ServerStarted"));
						FTPShareExperimentsServer.addObserver(ServerSharingContainer.this);
						startButton.setImage(ImagesUtils.getImage(IImagesKeys.STOP_SERVER_ICON));
						startButton.setToolTipText(Messages.getString("SharingView.StopServerButtonToolTip"));
						startButton.setText(Messages.getString("SharingView.StopServerButtonToolTip"));
					} catch (FtpException e) {
						Log.logErrorMessage(e);
						updateErrorMessage(e.getMessage());
					}
				} else {
					connectedUsersListViewer.getTable().selectAll();
					IStructuredSelection selection = (IStructuredSelection) connectedUsersListViewer.getSelection();
					ArrayList<String> selectedUsersList = new ArrayList<String>(0);
					for (Iterator<String> iterator = selection.iterator(); iterator.hasNext();) {
						String selectionString = (String) iterator.next();
						selectedUsersList.add(selectionString);
					}
					FTPShareExperimentsServer.disconnectUsers(selectedUsersList.toArray(new String[selectedUsersList.size()]));
					FTPShareExperimentsServer.stop();
					FTPShareExperimentsServer.removeObserver(ServerSharingContainer.this);
					Log.logMessage(Messages.getString("SharingView.ServerStopped"));
					updateMessage(Messages.getString("SharingView.ServerStopped"));
					startButton.setImage(ImagesUtils.getImage(IImagesKeys.START_SERVER_ICON));
					startButton.setToolTipText(Messages.getString("SharingView.StartServerButtonToolTip"));
					startButton.setText(Messages.getString("SharingView.StartServerButtonToolTip"));
				}
			}
			
		});
		
		Composite connectedUsersContainer = new Composite(serverSashForm, SWT.BORDER);
		connectedUsersContainer.setLayout(new GridLayout(1, false));
		
		CLabel connectedUsersListLabel = new CLabel(connectedUsersContainer, SWT.NONE);
		connectedUsersListLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		connectedUsersListLabel.setText(Messages.getString("SharingView.ConnectedUsersLabelTitle"));
		
		connectedUsersListViewer = new TableViewer(connectedUsersContainer, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		Table connectedUsersList = connectedUsersListViewer.getTable();
		connectedUsersList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		connectedUsersList.addFocusListener(new FocusListener() {
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
		connectedUsersListViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}
		});
		connectedUsersListViewer.setLabelProvider(new ILabelProvider() {
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
				return (String) element;
			}
			
			public Image getImage(Object element) {
				return ImagesUtils.getImage(IImagesKeys.FTP_USER_ICON);
			}
		});
		
		
		Button disconnectButton = new Button(connectedUsersContainer, SWT.FLAT);
		disconnectButton.setImage(ImagesUtils.getImage(IImagesKeys.DISCONNECT_USER_ICON));
		disconnectButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		disconnectButton.setToolTipText(Messages.getString("SharingView.DisconnectUsersButtonToolTip"));
		disconnectButton.setText(Messages.getString("SharingView.DisconnectUsersButtonToolTip"));
		disconnectButton.addFocusListener(view);
		disconnectButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent event) {
				IStructuredSelection selection = (IStructuredSelection) connectedUsersListViewer.getSelection();
				ArrayList<String> selectedUsersList = new ArrayList<String>(0);
				for (Iterator<String> iterator = selection.iterator(); iterator.hasNext();) {
					String selectionString = (String) iterator.next();
					selectedUsersList.add(selectionString);
				}
				FTPShareExperimentsServer.disconnectUsers(selectedUsersList.toArray(new String[selectedUsersList.size()]));
			}
		});
		
		Composite serverMessagesContainer = new Composite(serverSashForm, SWT.BORDER);
		serverMessagesContainer.setLayout(new GridLayout(1, false));
		
		CLabel serverMessagesLabel = new CLabel(serverMessagesContainer, SWT.NONE);
		serverMessagesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		serverMessagesLabel.setText(Messages.getString("SharingView.ServerMessagesLabelTitle"));
		
		serverStyledText = new StyledText(serverMessagesContainer, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		serverStyledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		serverStyledText.addFocusListener(new FocusListener() {
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
		
		Button clearButton = new Button(serverMessagesContainer, SWT.FLAT);
		clearButton.setImage(ImagesUtils.getImage(IImagesKeys.CLEAR_MESSAGES_ICON));
		clearButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		clearButton.setToolTipText(Messages.getString("SharingView.ClearServerMessagesButtonToolTip"));
		clearButton.setText(Messages.getString("SharingView.ClearServerMessagesButtonToolTip"));
		clearButton.addFocusListener(view);
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				serverStyledText.setText("");
			}
		});
		serverSashForm.setWeights(new int[] {1, 1, 1});
	}
	
	public static void clearMessages() {
		serverStyledText.setText("");
	}

	private void loadSharedExperimentsFile() {
		String fileName = AnalysePreferences.getCurrentWorkspace() + File.separator + FTPShareExperimentsServer.SHARED_EXPERIMENTS_FILE_NAME;
		File sharedExperimentsFile = new File(fileName);
		if(sharedExperimentsFile.exists())
			try {
				Properties sharedExperimentsProperties = new Properties();
				sharedExperimentsProperties.load(new FileInputStream(sharedExperimentsFile));
				String[] experiments = sharedExperimentsProperties.getProperty("EXPERIMENTS").split(";");
				if(experiments.length > 0 && !experiments[0].equals(""))	sharedExperimentListViewer.setInput(experiments);
			} catch (FileNotFoundException e) {
				Log.logErrorMessage(e);
			} catch (IOException e) {
				Log.logErrorMessage(e);
			}
	}

	protected void saveSharedExperimentsFile() {
		String fileName = AnalysePreferences.getCurrentWorkspace() + File.separator + FTPShareExperimentsServer.SHARED_EXPERIMENTS_FILE_NAME;
		File sharedExperimentsFile = new File(fileName);
		if(sharedExperimentsFile.exists()) sharedExperimentsFile.delete();
		Properties sharedExperimentsProperties = new Properties();
		String[] experiments = (String[]) sharedExperimentListViewer.getInput();
		String expString = "";
		for (int i = 0; i < experiments.length; i++) expString = expString + experiments[i] + ";";
		expString = expString.replaceAll(";$", "");
		sharedExperimentsProperties.put("EXPERIMENTS", expString);
		FileOutputStream sharedExperimentFile;
		try {
			sharedExperimentFile = new FileOutputStream(sharedExperimentsFile);
			sharedExperimentsProperties.store(sharedExperimentFile, "Please, do not edit this file, it is machine generated.");
			sharedExperimentFile.close();
		} catch (FileNotFoundException e) {
			Log.logErrorMessage(e);
		} catch (IOException e) {
			Log.logErrorMessage(e);
		} 
	}
	
	private void logServerMessage(final String message, final StyleRange style) {
		if(!serverStyledText.isDisposed() && message != null) {
			serverStyledText.setCaretOffset(serverStyledText.getText().length());								
			serverStyledText.append(message + "\n");				
			style.start = serverStyledText.getText().length() - message.length() - 1;
			style.length = message.length();
			serverStyledText.setStyleRange(style);
		}
	}
	
	private void updateErrorMessage(String errorMessage) {
		logServerMessage(errorMessage, serverErrorMessageStyle);
	}

	private void updateMessage(String message) {
		logServerMessage(message, serverMessageStyle);			
	}

//	private void updateWarningMessage(String warningMessage) {
//		logServerMessage(warningMessage, serverWarningMessageStyle);
//	}

	public void update(int messageID, String message) {
		if(IFTPMessageObserver.USER_LOGGEDIN == messageID) {
			connectedUsersListViewer.setInput(FTPShareExperimentsServer.getConnectedUser());
			logServerMessage(Messages.getString("SharingView.UserConnection") + message, serverMessageStyle);
		}
		if(IFTPMessageObserver.USER_DISCONNECTED == messageID) {
			if(connectedUsersListViewer.getContentProvider() != null) {
				connectedUsersListViewer.setInput(FTPShareExperimentsServer.getConnectedUser());
				logServerMessage(Messages.getString("SharingView.UserDisconnection") + message, serverMessageStyle);
			}
		}
	}
	
}
