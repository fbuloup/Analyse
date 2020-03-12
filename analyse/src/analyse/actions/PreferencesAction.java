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
package analyse.actions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.gui.dialogs.NewFTPUserDialog;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public final class PreferencesAction extends Action {
	
	String languageValue = AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.LANGUAGE);
	String mathEngineValue = AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.MATH_ENGINE);
	
	public PreferencesAction() {
		setText(Messages.getString("PreferencesAction.Title")); //$NON-NLS-1$
		setAccelerator(SWT.MOD1 | 'P');
	}
	
	class AnalysePreferenceDialog extends PreferenceDialog {
		public AnalysePreferenceDialog(PreferenceManager preferenceManager) {
			super(null,preferenceManager );
		}
		@Override
		protected void configureShell(Shell newShell) {
			newShell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
			super.configureShell(newShell);
		}
	}
	
	public void run() {
		PreferenceManager preferenceManager = new PreferenceManager();
		GeneralPrefPage generalPrefPage = new GeneralPrefPage();
		ChartPrefPage chartPregPage = new ChartPrefPage();
		UpdatePrefPage updatePrefPage = new UpdatePrefPage();
		SharingPrefPage sharingPrefPage = new SharingPrefPage();
		generalPrefPage.setValid(false);
		chartPregPage.setValid(false);
		updatePrefPage.setValid(false);
		sharingPrefPage.setValid(false);
		PreferenceNode generalPreferences = new PreferenceNode("General", (IPreferencePage) generalPrefPage); //$NON-NLS-1$
	    PreferenceNode chartPreferences = new PreferenceNode("Chart", (IPreferencePage) chartPregPage); //$NON-NLS-1$
	    PreferenceNode updatePreferences = new PreferenceNode("Update", (IPreferencePage) updatePrefPage); //$NON-NLS-1$
	    PreferenceNode sharingPreferences = new PreferenceNode("Sharing", (IPreferencePage) sharingPrefPage); //$NON-NLS-1$
	    preferenceManager.addToRoot(generalPreferences);
		preferenceManager.addToRoot(chartPreferences);
		preferenceManager.addToRoot(updatePreferences);
		preferenceManager.addToRoot(sharingPreferences);
		AnalysePreferenceDialog preferenceDialog = new AnalysePreferenceDialog(preferenceManager);
        preferenceDialog.setPreferenceStore(AnalysePreferences.getPreferenceStore());
        preferenceDialog.open();
        AnalysePreferences.savePreferences();
	}

	class GeneralPrefPage extends FieldEditorPreferencePage { 
		
		private BooleanFieldEditor redirectConsoleMessageField;
		private BooleanFieldEditor loadFromSavingFileField;
		private ComboFieldEditor languageField;
		private ComboFieldEditor mathEngineComboFieldEditor;
		private BooleanFieldEditor useDefaultWorkspaceDirField;
		private IntegerFieldEditor javaHeapSpaceMinFieldEditor;
		private IntegerFieldEditor javaHeapSpaceMaxFieldEditor;
		private FileFieldEditor mathEngineExecutableFileFieldEditor;

		public GeneralPrefPage() {			
			super(Messages.getString("PreferencesAction.GeneralPrefPageTitle"),FieldEditorPreferencePage.FLAT); //$NON-NLS-1$
		}
	
		public boolean performOk() {
			if(isValid()) {
				getPreferenceStore().setValue(AnalysePreferences.LANGUAGE, languageValue);
				getPreferenceStore().setValue(AnalysePreferences.REDIRECT_CONSOLE_MESSAGES, redirectConsoleMessageField.getBooleanValue());
				getPreferenceStore().setValue(AnalysePreferences.ALWAYS_LOAD_FROM_SAVING_FILE, loadFromSavingFileField.getBooleanValue());
				getPreferenceStore().setValue(AnalysePreferences.USE_DEFAULT_WORKSPACE_DIR, useDefaultWorkspaceDirField.getBooleanValue());
				getPreferenceStore().setValue(AnalysePreferences.JAVA_HEAP_MAX, javaHeapSpaceMaxFieldEditor.getIntValue());
				getPreferenceStore().setValue(AnalysePreferences.JAVA_HEAP_MIN, javaHeapSpaceMinFieldEditor.getIntValue());
				getPreferenceStore().setValue(AnalysePreferences.MATH_ENGINE, mathEngineValue);
				getPreferenceStore().setValue(AnalysePreferences.MATH_ENGINE_EXECUTABLE_PATH, mathEngineExecutableFileFieldEditor.getStringValue());
				AnalyseApplication.saveLauncherParams();
			}
			return true;
		}

		@Override
		protected void createFieldEditors() {
			
			addField(new LabelFieldEditor(Messages.getString("PreferencesAction.GeneralPrefPageInfosTitle"),getFieldEditorParent())); //$NON-NLS-1$
			addField(new SpacerFieldEditor(getFieldEditorParent()));
			
			languageField = new ComboFieldEditor(AnalysePreferences.LANGUAGE,Messages.getString("PreferencesAction.GeneralPrefPageDefaultOptionsLanguageLabelTitle"), AnalysePreferences.LANGUAGE_LIST,getFieldEditorParent()); //$NON-NLS-1$
			addField(languageField);
			redirectConsoleMessageField = new BooleanFieldEditor(AnalysePreferences.REDIRECT_CONSOLE_MESSAGES,Messages.getString("PreferencesAction.GeneralPrefPageDefaultOptionsRedirectConoleLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(redirectConsoleMessageField);
			useDefaultWorkspaceDirField = new BooleanFieldEditor(AnalysePreferences.USE_DEFAULT_WORKSPACE_DIR,Messages.getString("PreferencesAction.GeneralPrefPageDefaultWorkspaceDirLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(useDefaultWorkspaceDirField);
			
			mathEngineComboFieldEditor = new ComboFieldEditor(AnalysePreferences.MATH_ENGINE,Messages.getString("PreferencesAction.GeneralPrefPageDefaultMathEngineLabelTitle"), AnalysePreferences.MATH_ENGINE_LIST,getFieldEditorParent()); //$NON-NLS-1$
			addField(mathEngineComboFieldEditor);
			mathEngineExecutableFileFieldEditor = new FileFieldEditor(AnalysePreferences.MATH_ENGINE_EXECUTABLE_PATH,Messages.getString("PreferencesAction.GeneralPrefPageDefaultMathEnginePathLabelTitle"),true,getFieldEditorParent());
			addField(mathEngineExecutableFileFieldEditor);
			
			addField(new SpacerFieldEditor(getFieldEditorParent()));
			
			addField(new LabelFieldEditor(Messages.getString("PreferencesAction.GeneralPrefPageDefaultSubjectOptionsTitle"),getFieldEditorParent())); //$NON-NLS-1$
			loadFromSavingFileField = new BooleanFieldEditor(AnalysePreferences.ALWAYS_LOAD_FROM_SAVING_FILE,Messages.getString("PreferencesAction.GeneralPrefPageDefaultSubjectOptionsLoadFromSavedFilesLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(loadFromSavingFileField);
			addField(new SpacerFieldEditor(getFieldEditorParent()));
			
//			addField(new LabelFieldEditor(Messages.getString("PreferencesAction.GeneralPrefPageDefaultChartOptionsTitle"),getFieldEditorParent())); //$NON-NLS-1$
			

			
			addField(new LabelFieldEditor(Messages.getString("PreferencesAction.GeneralPrefPageDefaultJavaOptionsTitle"),getFieldEditorParent())); //$NON-NLS-1$
			javaHeapSpaceMaxFieldEditor = new IntegerFieldEditor(AnalysePreferences.JAVA_HEAP_MAX, Messages.getString("PreferencesAction.GeneralPrefPageDefaultJavaHeapMaxLabelTitle"), getFieldEditorParent());
			addField(javaHeapSpaceMaxFieldEditor);
			javaHeapSpaceMinFieldEditor = new IntegerFieldEditor(AnalysePreferences.JAVA_HEAP_MIN, Messages.getString("PreferencesAction.GeneralPrefPageDefaultJavaHeapMinLabelTitle"), getFieldEditorParent());
			addField(javaHeapSpaceMinFieldEditor);
			
			setValid(true);
		}
		
		public void propertyChange(PropertyChangeEvent event) {
			super.propertyChange(event);
			if(languageField == event.getSource()) languageValue = (String) event.getNewValue();
			if(mathEngineComboFieldEditor == event.getSource()) mathEngineValue = (String) event.getNewValue();
		}
		
	}
	
	class ChartPrefPage extends FieldEditorPreferencePage { 
		
		private BooleanFieldEditor showCrossHairField;
		private BooleanFieldEditor showChannelsPaletteField;
		private BooleanFieldEditor showMarkersField;
		private BooleanFieldEditor showLegendField;
		private BooleanFieldEditor sortChannelField;
		private BooleanFieldEditor autoAdjustXYAxisField;
		private BooleanFieldEditor chartsAntialiasisField;
		private BooleanFieldEditor showEventsAsInfiniteField;
		private BooleanFieldEditor nextPreviousOnlyOnSignalsField;

		public ChartPrefPage() {			
			super(Messages.getString("PreferencesAction.ChartPrefPageTitle"),FieldEditorPreferencePage.FLAT); //$NON-NLS-1$
		}
	
		public boolean performOk() {
			if(isValid()) {
				getPreferenceStore().setValue(AnalysePreferences.CHARTS_ANTIALIASIS, chartsAntialiasisField.getBooleanValue());
				getPreferenceStore().setValue(AnalysePreferences.SHOW_CROSSHAIR, showCrossHairField.getBooleanValue());
				getPreferenceStore().setValue(AnalysePreferences.SHOW_CHANNELS_PALETTE, showChannelsPaletteField.getBooleanValue());
				getPreferenceStore().setValue(AnalysePreferences.SHOW_MARKERS, showMarkersField.getBooleanValue());
				getPreferenceStore().setValue(AnalysePreferences.SHOW_LEGEND, showLegendField.getBooleanValue());
				getPreferenceStore().setValue(AnalysePreferences.SORT_CHANNELS, sortChannelField.getBooleanValue());
				getPreferenceStore().setValue(AnalysePreferences.AUTO_ADJUST_XY_AXIS, autoAdjustXYAxisField.getBooleanValue());
				getPreferenceStore().setValue(AnalysePreferences.SHOW_EVENTS_AS_INFINITE, showEventsAsInfiniteField.getBooleanValue());
				getPreferenceStore().setValue(AnalysePreferences.NEXT_PREVIOUS_ONLY_SIGNALS, nextPreviousOnlyOnSignalsField.getBooleanValue());
			}
			return true;
		}

		@Override
		protected void createFieldEditors() {
			
////			addField(new LabelFieldEditor(Messages.getString("PreferencesAction.ChartPrefPageInfosTitle"),getFieldEditorParent())); //$NON-NLS-1$
////			addField(new LabelFieldEditor(Messages.getString("PreferencesAction.ChartPrefPageDefaultOptionsTitle"),getFieldEditorParent())); //$NON-NLS-1$
////			addField(new SpacerFieldEditor(getFieldEditorParent()));
////			addField(new LabelFieldEditor(Messages.getString("PreferencesAction.ChartPrefPageDefaultSubjectOptionsTitle"),getFieldEditorParent())); //$NON-NLS-1$
////			addField(new SpacerFieldEditor(getFieldEditorParent()));
//			
//			addField(new LabelFieldEditor(Messages.getString("PreferencesAction.ChartPageDefaultChartOptionsTitle"),getFieldEditorParent())); //$NON-NLS-1$
			
			chartsAntialiasisField = new BooleanFieldEditor(AnalysePreferences.CHARTS_ANTIALIASIS,Messages.getString("PreferencesAction.GeneralPrefPageDefaultChartOptionsAntialiasisTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(chartsAntialiasisField);
			showCrossHairField = new BooleanFieldEditor(AnalysePreferences.SHOW_CROSSHAIR,Messages.getString("PreferencesAction.GeneralPrefPageDefaultChartOptionsShowCrossHairLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(showCrossHairField);
			showChannelsPaletteField = new BooleanFieldEditor(AnalysePreferences.SHOW_CHANNELS_PALETTE,Messages.getString("PreferencesAction.GeneralPrefPageDefaultChartOptionsShowChannelPaletteLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(showChannelsPaletteField);
			showMarkersField = new BooleanFieldEditor(AnalysePreferences.SHOW_MARKERS,Messages.getString("PreferencesAction.GeneralPrefPageDefaultChartOptionsShowMarkersLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(showMarkersField);
			showLegendField = new BooleanFieldEditor(AnalysePreferences.SHOW_LEGEND,Messages.getString("PreferencesAction.GeneralPrefPageDefaultChartOptionsShowLegendLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(showLegendField);
			sortChannelField = new BooleanFieldEditor(AnalysePreferences.SORT_CHANNELS,Messages.getString("PreferencesAction.GeneralPrefPageDefaultChartOptionsSortChannelsNamesLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(sortChannelField);
			autoAdjustXYAxisField = new BooleanFieldEditor(AnalysePreferences.AUTO_ADJUST_XY_AXIS,Messages.getString("PreferencesAction.GeneralPrefPageDefaultChartOptionsAutoAdjustXYLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(autoAdjustXYAxisField);
			showEventsAsInfiniteField = new BooleanFieldEditor(AnalysePreferences.SHOW_EVENTS_AS_INFINITE,Messages.getString("PreferencesAction.GeneralPrefPageDefaultChartOptionsshowEventsAsInfiniteLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(showEventsAsInfiniteField);
			nextPreviousOnlyOnSignalsField = new BooleanFieldEditor(AnalysePreferences.NEXT_PREVIOUS_ONLY_SIGNALS,Messages.getString("PreferencesAction.GeneralPrefPageDefaultChartOptionsnextPreviousOnlyOnSignalsFieldLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(nextPreviousOnlyOnSignalsField);
			
			
			setValid(true);
		}
		
	}
	
	class UpdatePrefPage extends FieldEditorPreferencePage {

		private StringFieldEditor repositoryURLStringField;
		private BooleanFieldEditor compareFileContentField;
		private BooleanFieldEditor verboseUpdateField;

		public UpdatePrefPage() {			
			super(Messages.getString("PreferencesAction.UpdatePrefPageTitle"),FieldEditorPreferencePage.FLAT); //$NON-NLS-1$
		}
	
		public boolean performOk() {
			if(isValid()) {
				getPreferenceStore().setValue(AnalysePreferences.REPOSITORY_PATH, repositoryURLStringField.getStringValue());
				getPreferenceStore().setValue(AnalysePreferences.COMPARE_FILE_CONTENT_DURING_UPDATE, compareFileContentField.getBooleanValue());
				getPreferenceStore().setValue(AnalysePreferences.VERBOSE_DURING_UPDATE, verboseUpdateField.getBooleanValue());
			}
			return true;
		}

		@Override
		protected void createFieldEditors() {
			
			repositoryURLStringField = new StringFieldEditor(AnalysePreferences.REPOSITORY_PATH,Messages.getString("PreferencesAction.svnRepositoryPathLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(repositoryURLStringField);
			compareFileContentField = new BooleanFieldEditor(AnalysePreferences.COMPARE_FILE_CONTENT_DURING_UPDATE,Messages.getString("PreferencesAction.UpdatePrefPageCompareContentFileDuringUpdateTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(compareFileContentField);
			verboseUpdateField = new BooleanFieldEditor(AnalysePreferences.VERBOSE_DURING_UPDATE,Messages.getString("PreferencesAction.UpdatePrefPageVerboseDuringUpdateTitle"),getFieldEditorParent()); //$NON-NLS-1$
			addField(verboseUpdateField);
			
			setValid(true);
		}
		
	}
	
	class SharingPrefPage extends FieldEditorPreferencePage {
		
		UsersListEditor usersListEditor;

		public SharingPrefPage() {
			super(Messages.getString("PreferencesAction.SharingPrefPageTitle"),FieldEditorPreferencePage.FLAT); //$NON-NLS-1$
		}
		
		@Override
		protected void createFieldEditors() {
			usersListEditor = new UsersListEditor("users",Messages.getString("PreferencesAction.SharingPrefPageLabelTitle"),getFieldEditorParent());
			usersListEditor.doLoad();
		}
		
	}
	
	class UsersListEditor extends FieldEditor {

		private List usersList;
		private Properties ftpProperties;
		private String ftpUser = "ftpserver.user." ;
	
		public UsersListEditor(String name, String label, Composite parent) {
			super(name,label,parent);
			ftpProperties = new Properties();
		}
		
		@Override
		protected void adjustForNumColumns(int numColumns) {
			((GridData) usersList.getLayoutData()).horizontalSpan = numColumns;
		}

		@Override
		protected void doFillIntoGrid(Composite parent, int numColumns) {
			Label label = getLabelControl(parent);
			label.setText(getLabelText());
			usersList = new List(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			usersList.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,numColumns,1));
			Composite container = new Composite(parent, SWT.NONE);
			container.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,false,numColumns,1));
			container.setLayout(new GridLayout(2, true));
			Button addButton = new Button(container,SWT.PUSH);
			addButton.setText(Messages.getString("PreferencesAction.SharingPrefPageAddButton"));
			addButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
			addButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					NewFTPUserDialog newFTPUserDialog = new NewFTPUserDialog(Display.getDefault().getActiveShell(), usersList.getItems());
					if(newFTPUserDialog.open() == Window.OK) {
						SaltedPasswordEncryptor encryptor = new SaltedPasswordEncryptor();
						String login = newFTPUserDialog.getLogin();
						String pwd = encryptor.encrypt(newFTPUserDialog.getPassword());
						String homeDir = AnalysePreferences.getCurrentWorkspace();
						ftpProperties.put(ftpUser + login + ".enableflag", "true");
						ftpProperties.put(ftpUser + login + ".writepermission", "false");
						ftpProperties.put(ftpUser + login + ".homedirectory", homeDir);
						ftpProperties.put(ftpUser + login + ".userpassword", pwd);
						ftpProperties.put(ftpUser + login + ".idletime", "300");
						ftpProperties.put(ftpUser + login + ".maxloginnumber", "1");
						doStore();
						doLoad();
					}
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			Button removeButton = new Button(container,SWT.PUSH);
			removeButton.setText(Messages.getString("PreferencesAction.SharingPrefPageRemoveButton"));
			removeButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
			removeButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					String[] selectedUsers = usersList.getSelection();
					for (int i = 0; i < selectedUsers.length; i++) {
						ftpProperties.remove(ftpUser + selectedUsers[i] + ".enableflag");
						ftpProperties.remove(ftpUser + selectedUsers[i] + ".writepermission");
						ftpProperties.remove(ftpUser + selectedUsers[i] + ".homedirectory");
						ftpProperties.remove(ftpUser + selectedUsers[i] + ".userpassword");
						ftpProperties.remove(ftpUser + selectedUsers[i] + ".idletime");
						ftpProperties.remove(ftpUser + selectedUsers[i] + ".maxloginnumber");
						
					}
					doStore();
					doLoad();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		@Override
		protected void doLoad() {
			try {
				usersList.removeAll();
				Set<String> usersSet = new HashSet<String>(0);
				ftpProperties.load(new FileInputStream("./users.properties"));
				Enumeration<Object> keys = ftpProperties.keys();
				while (keys.hasMoreElements()) {
					String key = (String) keys.nextElement();
					if(key.startsWith("ftpserver.user")) {
						usersSet.add(key.split("\\.")[2]);
					}
				}
				String[] usersString = usersSet.toArray(new String[usersSet.size()]);
				Arrays.sort(usersString);
				for (int i = 0; i < usersString.length; i++) usersList.add(usersString[i]);
			} catch (FileNotFoundException e) {
				//File will be created
			} catch (IOException e) {
				Log.logErrorMessage(e);
			}
		}

		@Override
		protected void doLoadDefault() {
		}

		@Override
		protected void doStore() {
			try {
				ftpProperties.store(new FileOutputStream("./users.properties"), "Please : Do not edit this file");
			} catch (FileNotFoundException e) {
				Log.logErrorMessage(e);
			} catch (IOException e) {
				Log.logErrorMessage(e);
			}
		}

		@Override
		public int getNumberOfControls() {
			return 1;
		}
	}

	/**
	 * A field editor for displaying labels not associated with other widgets.
	 */
	class LabelFieldEditor extends FieldEditor {

		private Label label;

		// All labels can use the same preference name since they don't
		// store any preference.
		public LabelFieldEditor(String value, Composite parent) {
			super("label", value, parent); //$NON-NLS-1$
		}

		// Adjusts the field editor to be displayed correctly
		// for the given number of columns.
		protected void adjustForNumColumns(int numColumns) {
			((GridData) label.getLayoutData()).horizontalSpan = numColumns;
		}

		// Fills the field editor's controls into the given parent.
		protected void doFillIntoGrid(Composite parent, int numColumns) {
			label = getLabelControl(parent);
			JFaceResources.getFontRegistry().put(parent.getFont().toString(),  parent.getFont().getFontData());
			label.setFont(JFaceResources.getFontRegistry().getBold(parent.getFont().toString()));
			
			GridData gridData = new GridData();
			gridData.horizontalSpan = numColumns;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = false;
			gridData.verticalAlignment = GridData.CENTER;
			gridData.grabExcessVerticalSpace = false;
			
			label.setLayoutData(gridData);
		}

		// Returns the number of controls in the field editor.
		public int getNumberOfControls() {
			return 1;
		}

		// Labels do not persist any preferences, so these methods are empty.
		protected void doLoad() {
		}
		protected void doLoadDefault() {
		}
		protected void doStore() {
		}
	}

	
	/**
	 * A field editor for adding space to a preference page.
	 */
	public class SpacerFieldEditor extends LabelFieldEditor {
		// Implemented as an empty label field editor.
		public SpacerFieldEditor(Composite parent) {
			super("", parent); //$NON-NLS-1$
		}
	}
	
	
	/*class MatlabPrefPage extends FieldEditorPreferencePage {
	  
	private BooleanFieldEditor launchMatlabAtStartUpField;
	private BooleanFieldEditor analyseVerboseField;
	private BooleanFieldEditor keepQuietOnSubjectLoadingField;
	private BooleanFieldEditor matlabEngineVerboseField;
	private BooleanFieldEditor matlabNativeEngineVerboseField;
	private AnalyseFileFieldEditor matlabStartCmdFileField;
	private DirectoryFieldEditor matlabLibraryPath;
	private ComboFieldEditor matlabVersionField;
	private DirectoryFieldEditor matlabBaseDirectoryField;
			
	public MatlabPrefPage() {			
		super(Messages.getString("PreferencesAction.MatlabPrefPageTitle"),FieldEditorPreferencePage.FLAT); //$NON-NLS-1$
	}

	public boolean performOk() {
		if(isValid()) {
			getPreferenceStore().setValue(AnalysePreferences.LAUNCH_MATLAB_AT_STARTUP, launchMatlabAtStartUpField.getBooleanValue());
			getPreferenceStore().setValue(AnalysePreferences.ANALYSE_VERBOSE_OUTPUT, analyseVerboseField.getBooleanValue());
			getPreferenceStore().setValue(AnalysePreferences.KEEP_QUIET_ON_SUBJECT_LOADING, keepQuietOnSubjectLoadingField.getBooleanValue());
			getPreferenceStore().setValue(AnalysePreferences.MATLAB_ENGINE_VERBOSE_OUTPUT, matlabEngineVerboseField.getBooleanValue());			
			getPreferenceStore().setValue(AnalysePreferences.MATLAB_NATIVE_ENGINE_VERBOSE_OUTPUT, matlabNativeEngineVerboseField.getBooleanValue());			
			analyseWindowController.getMatlabController().setDebugMode(matlabEngineVerboseField.getBooleanValue());
			getPreferenceStore().setValue(AnalysePreferences.MATLAB_START_CMD, matlabStartCmdFileField.getStringValue());	
			getPreferenceStore().setValue(AnalysePreferences.MATLAB_LIBRARY_PATH, matlabLibraryPath.getStringValue());	
			getPreferenceStore().setValue(AnalysePreferences.MATLAB_VERSION, matlabVersionValue);
			getPreferenceStore().setValue(AnalysePreferences.MATLAB_BASE_DIRECTORY, matlabBaseDirectoryField.getStringValue());
			
			try {
				//Construct the launcher full path name
				File file = new File("."); //$NON-NLS-1$
				String launcherName = "";
				if(resources.System.isLinux()) launcherName = linuxLauncherName;
				if(resources.System.isMacOSX()) launcherName = macLauncherName;
				if(resources.System.isWindows()) launcherName = windowsLauncherName;
				String currentDirectory = file.getAbsolutePath().replaceAll(File.separator + ".$", "");
				String fileName = currentDirectory + File.separator + launcherName; //$NON-NLS-1$
				//Read this launcher in ArrayList
				BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(fileName)));
				String line = bufferedReader.readLine();
				ArrayList<String> lines = new ArrayList<String>(0);
				while (line != null) {
					lines.add(line);
					line = bufferedReader.readLine();
				}
				bufferedReader.close();
				//Replace lib path with new value
				for (int i = 0; i < lines.size(); i++) {
					line = lines.get(i);
					if(resources.System.isLinux()) if(line.startsWith("export LD_LIBRARY_PATH=")) lines.set(i, "export LD_LIBRARY_PATH=.:" + matlabLibraryPath.getStringValue()); 
					if(resources.System.isMacOSX()) if(line.startsWith("export DYLD_LIBRARY_PATH=")) lines.set(i, "export DYLD_LIBRARY_PATH=.:" + matlabLibraryPath.getStringValue());
					if(resources.System.isWindows()) if(line.startsWith("PATH=%PATH%;")) lines.set(i, "PATH=%PATH%;" + matlabLibraryPath.getStringValue());
				}
				//Delete launcher and replace
				file = new File(fileName);
				file.delete();
				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
				for (int i = 0; i < lines.size(); i++) {
					bufferedWriter.write(lines.get(i));
					bufferedWriter.newLine();
				}
				bufferedWriter.close();
				//Change launcher to executable if necessary (mac, linux)
				String[] cmd = new String[0];
				if(resources.System.isLinux() || resources.System.isMacOSX()) {
					if(resources.System.isLinux()) cmd = new String[]{ "/bin/sh", "-c", "chmod 777 " + currentDirectory + File.separator + launcherName};
					if(resources.System.isMacOSX()) cmd = new String[]{ "/bin/sh", "-c", "chmod 777 " + currentDirectory + File.separator + launcherName};
					try {
						Runtime.getRuntime().exec(cmd).waitFor();
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e){
              e.printStackTrace();
			}
			
		}
		return true;
	}

	@Override
	protected void createFieldEditors() {
		addField(new LabelFieldEditor(Messages.getString("PreferencesAction.GeneralPrefPageInfosTitle"),getFieldEditorParent())); //$NON-NLS-1$
		
		matlabBaseDirectoryField = new DirectoryFieldEditor(AnalysePreferences.MATLAB_BASE_DIRECTORY,Messages.getString("PreferencesAction.matlabBaseDirectoryLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
		addField(matlabBaseDirectoryField);
		
		matlabLibraryPath = new DirectoryFieldEditor(AnalysePreferences.MATLAB_LIBRARY_PATH,Messages.getString("PreferencesAction.LibPathLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
		addField(matlabLibraryPath);
		
		matlabStartCmdFileField = new AnalyseFileFieldEditor(AnalysePreferences.MATLAB_START_CMD,Messages.getString("PreferencesAction.StartCommandLabelTitle"),getFieldEditorParent(),matlabBaseDirectoryField); //$NON-NLS-1$
		addField(matlabStartCmdFileField);
		
		matlabVersionField = new ComboFieldEditor(AnalysePreferences.MATLAB_VERSION,Messages.getString("PreferencesAction.MatlabVerLabelTitle"), AnalysePreferences.MATLAB_VERSION_LIST,getFieldEditorParent()); //$NON-NLS-1$
		addField(matlabVersionField);
		
		launchMatlabAtStartUpField = new BooleanFieldEditor(AnalysePreferences.LAUNCH_MATLAB_AT_STARTUP,Messages.getString("PreferencesAction.LaunchMatlabAtStartUpLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
		addField(launchMatlabAtStartUpField);
		
		analyseVerboseField = new BooleanFieldEditor(AnalysePreferences.ANALYSE_VERBOSE_OUTPUT,Messages.getString("PreferencesAction.AnalyseVerboseLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
		addField(analyseVerboseField);
		
		keepQuietOnSubjectLoadingField = new BooleanFieldEditor(AnalysePreferences.KEEP_QUIET_ON_SUBJECT_LOADING,Messages.getString("PreferencesAction.KeepQuietSubjectLoadLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
		addField(keepQuietOnSubjectLoadingField);

		matlabEngineVerboseField = new BooleanFieldEditor(AnalysePreferences.MATLAB_ENGINE_VERBOSE_OUTPUT,Messages.getString("PreferencesAction.MatlabVerboseLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
		addField(matlabEngineVerboseField);	
		
		matlabNativeEngineVerboseField = new BooleanFieldEditor(AnalysePreferences.MATLAB_NATIVE_ENGINE_VERBOSE_OUTPUT,Messages.getString("PreferencesAction.MatlabNativeVerboseLabelTitle"),getFieldEditorParent()); //$NON-NLS-1$
		addField(matlabNativeEngineVerboseField);	
		
		setValid(true);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if(matlabVersionField == event.getSource()) matlabVersionValue = (String) event.getNewValue();
	}
}*/


	/**
	 * This class creates a preference page
	 */
	/*
	class PrefPageTwo extends PreferencePage {
	  // Names for preferences
	  private static final String ONE = "two.one";
	  private static final String TWO = "two.two";
	  private static final String THREE = "two.three";

	  // The checkboxes
	  private Button checkOne;
	  private Button checkTwo;
	  private Button checkThree;

	  /**
	   * PrefPageTwo constructor
	   *
	  public PrefPageTwo() {
	    super("Two");
	    setDescription("Check the checks");
	  }

	  /**
	   * Creates the controls for this page
	   *
	  protected Control createContents(Composite parent) {
	    Composite composite = new Composite(parent, SWT.NONE);
	    composite.setLayout(new RowLayout(SWT.VERTICAL));

	    // Get the preference store
	    IPreferenceStore preferenceStore = AnalysePreferences.getPreferenceStore();

	    // Create three checkboxes
	    checkOne = new Button(composite, SWT.CHECK);
	    checkOne.setText("Check One");
	    checkOne.setSelection(preferenceStore.getBoolean(ONE));

	    checkTwo = new Button(composite, SWT.CHECK);
	    checkTwo.setText("Check Two");
	    checkTwo.setSelection(preferenceStore.getBoolean(TWO));

	    checkThree = new Button(composite, SWT.CHECK);
	    checkThree.setText("Check Three");
	    checkThree.setSelection(preferenceStore.getBoolean(THREE));

	    return composite;
	  }

	  /**
	   * Add buttons
	   * 
	   * @param parent the parent composite
	   *
	  protected void contributeButtons(Composite parent) {
	    // Add a select all button
	    Button selectAll = new Button(parent, SWT.PUSH);
	    selectAll.setText("Select All");
	    selectAll.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent event) {
	        checkOne.setSelection(true);
	        checkTwo.setSelection(true);
	        checkThree.setSelection(true);
	      }
	    });

	    // Add a select all button
	    Button clearAll = new Button(parent, SWT.PUSH);
	    clearAll.setText("Clear All");
	    clearAll.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent event) {
	        checkOne.setSelection(false);
	        checkTwo.setSelection(false);
	        checkThree.setSelection(false);
	      }
	    });

	    // Add two columns to the parent's layout
	    ((GridLayout) parent.getLayout()).numColumns += 2;
	  }

	  /**
	   * Change the description label
	   *
	  protected Label createDescriptionLabel(Composite parent) {
	    Label label = null;
	    String description = getDescription();
	    if (description != null) {
	      // Upper case the description
	      description = description.toUpperCase();

	      // Right-align the label
	      label = new Label(parent, SWT.RIGHT);
	      label.setText(description);
	    }
	    return label;
	  }

	  /**
	   * Called when user clicks Restore Defaults
	   *
	  protected void performDefaults() {
	    // Get the preference store
	    IPreferenceStore preferenceStore = AnalysePreferences.getPreferenceStore();

	    // Reset the fields to the defaults
	    checkOne.setSelection(preferenceStore.getDefaultBoolean(ONE));
	    checkTwo.setSelection(preferenceStore.getDefaultBoolean(TWO));
	    checkThree.setSelection(preferenceStore.getDefaultBoolean(THREE));
	  }

	  /**
	   * Called when user clicks Apply or OK
	   * 
	   * @return boolean
	   *
	  public boolean performOk() {
	    // Get the preference store
	    IPreferenceStore preferenceStore = AnalysePreferences.getPreferenceStore();

	    // Set the values from the fields
	    if (checkOne != null) preferenceStore.setValue(ONE, checkOne.getSelection());
	    if (checkTwo != null) preferenceStore.setValue(TWO, checkTwo.getSelection());
	    if (checkThree != null) preferenceStore.setValue(THREE, checkThree.getSelection());

	    // Return true to allow dialog to close
	    return true;
	  }
	}*/
	
}
