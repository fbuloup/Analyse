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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.forms.widgets.Twistie;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.preferences.AnalysePreferences;
import analyse.resources.CPlatform;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class CheatSheetsView extends View {
	
	private static String TITLE = "title";
	private static String DESCRIPTION = "description";
	private static String STEP = "step";
	private static HashMap<String, String> KEYS = new HashMap<String, String>(0);
	static {
		String rootDirectory = AnalyseApplication.analyseDirectory.replaceAll("[\\\\$]", "\\\\$0");
		String demoDirectory = AnalyseApplication.analyseDirectory + "help" + File.separator + "Demo";
		demoDirectory = demoDirectory.replaceAll("[\\\\$]", "\\\\$0");
		KEYS.put("ANALYSE_ROOT_DIRECTORY", rootDirectory);
		if(CPlatform.isMacOSX()) {
			if(AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.LANGUAGE).equals("fr_FR")) KEYS.put("CTRL_KEY", "Pomme");
			else KEYS.put("CTRL_KEY", "Apple");
		} else KEYS.put("CTRL_KEY", "Ctrl");
		if(CPlatform.isMacOSX()) {
			if(AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.LANGUAGE).equals("fr_FR")) KEYS.put("PREF_KEY", "Pomme + ','");
			else KEYS.put("PREF_KEY", "Apple + ','");
		} else KEYS.put("PREF_KEY", "Ctrl + 'p'");
		
		KEYS.put("ANALYSE_DEMO_DIRECTORY", demoDirectory);
	}
	
	private static String replaceAll(String string) {
		Set<String> keys  = KEYS.keySet();
		for (String key : keys) {
			String value = KEYS.get(key);
			string= string.replaceAll(key, value);
		}
		return string;
	}
	
	private class Step {
		private String title;
		private String description;
		
		public Step(String title, String description) {
			this.title = title;
			this.description = description;
			this.title = CheatSheetsView.replaceAll(this.title);
			this.description = CheatSheetsView.replaceAll(this.description);
		}

		public String getTitle() {
			return title;
		}

		public String getDescription() {
			return description;
		}
	}
	
	private class CheatSheet {
		private Node rootNode;
		private String introTitle;
		private String introDesciption;
		private ArrayList<Step> steps = new ArrayList<Step>(0);
		private String filePath;
		private CTabItem tabItem;
		
		public CheatSheet(String filePath, CTabItem tabItem, Node rootNode) {
			this.tabItem = tabItem;
			this.filePath = filePath;
			this.rootNode = rootNode;
			introTitle = rootNode.getAttributes().getNamedItem(TITLE).getNodeValue();		
			introDesciption = rootNode.getAttributes().getNamedItem(DESCRIPTION).getNodeValue();	
			this.introTitle = CheatSheetsView.replaceAll(this.introTitle);
			this.introDesciption = CheatSheetsView.replaceAll(this.introDesciption);
			populate(rootNode.getChildNodes());
		}
		
		private void populate(NodeList nodes) {
			ArrayList<Element> objectsList = new ArrayList<Element>(0);			
			for (int i = 0; i < nodes.getLength(); i++)  
				if(nodes.item(i) instanceof Element && nodes.item(i).getParentNode() == rootNode) objectsList.add((Element)nodes.item(i));
			for (int i = 0; i < objectsList.size(); i++) {
				Node node = objectsList.get(i);
				if(node.getNodeName().equals(STEP)) {
					Step step = new Step(node.getAttributes().getNamedItem(TITLE).getNodeValue(), node.getAttributes().getNamedItem(DESCRIPTION).getNodeValue());
					steps.add(step);
				}
				populate(node.getChildNodes());
			}
		}
		
		public CTabItem getTabItem() {
			return tabItem;
		}

		public String getFilePath() {
			return filePath;
		}

		public String getIntroTitle() {
			return introTitle;
		}

		public String getIntroDesciption() {
			return introDesciption;
		}
		
		public Step[] getSteps() {
			return steps.toArray(new Step[steps.size()]);
		}
	}
	
	public final static int DEFAULT = 0;
	private FormToolkit formToolkit;
	private HashSet<CheatSheet> cheatSheets = new HashSet<CheatSheet>(0);
	
	public CheatSheetsView(Composite parent, int style) {
		super(parent, style);
		initView(DEFAULT);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				formToolkit.dispose();
			}
		});
	}

	@Override
	protected void createToolBar() {
	}
	
	@Override
	protected void initView(int showMode) {
		formToolkit = new FormToolkit(getDisplay());
	}
	
	public void showCheatSheet(String filePath) {
		for (Iterator<CheatSheet> cheatSheetsIterator = cheatSheets.iterator(); cheatSheetsIterator.hasNext();) {
			CheatSheet cheatSheet = cheatSheetsIterator.next();
			if(cheatSheet.getFilePath().equals(filePath)) {
				setSelection(cheatSheet.getTabItem());
				return;
			}
		}
		Document helpDocument = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();		
			File cheatSheetFile = new File(filePath);
			InputStream helpInputStream = new FileInputStream(cheatSheetFile); 
			helpDocument = builder.parse(helpInputStream);
		} catch (ParserConfigurationException e) {
			Log.logErrorMessage(e);
		} catch (FileNotFoundException e) {
			Log.logErrorMessage(e);
		} catch (SAXException e) {
			Log.logErrorMessage(e);
		} catch (IOException e) {
			Log.logErrorMessage(e);
		}
		final CTabItem cheatSheetTabItem = createTabItem();
		cheatSheetTabItem.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				for (Iterator<CheatSheet> cheatSheetsIterator = cheatSheets.iterator(); cheatSheetsIterator.hasNext();) {
					CheatSheet cheatSheet = cheatSheetsIterator.next();
					if(cheatSheet.getTabItem() == cheatSheetTabItem) {
						cheatSheets.remove(cheatSheet);
						return;
					}
				}
			}
		});
		cheatSheetTabItem.setImage(ImagesUtils.getImage(IImagesKeys.CHEAT_SHEET_ICON));
		CheatSheet cheatSheet = new CheatSheet(filePath, cheatSheetTabItem, helpDocument.getFirstChild());
		cheatSheetTabItem.setText(Messages.getString("CheatSheetView.Title") + " - " + cheatSheet.getIntroTitle());
		cheatSheetTabItem.setToolTipText(cheatSheetTabItem.getText());
		cheatSheets.add(cheatSheet);
		ScrolledForm cheatSheetScrolledForm = formToolkit.createScrolledForm(this);
		cheatSheetScrolledForm.setText(cheatSheet.getIntroTitle());
		formToolkit.decorateFormHeading(cheatSheetScrolledForm.getForm());
		cheatSheetScrolledForm.getForm().getBody().setLayout(new TableWrapLayout());
		FormText descriptionFormText = formToolkit.createFormText(cheatSheetScrolledForm.getForm().getBody(), false);
		descriptionFormText.setFont("code", JFaceResources.getTextFont());
		populateImages(descriptionFormText);
		descriptionFormText.addFocusListener(this);
		descriptionFormText.setText(cheatSheet.getIntroDesciption(), true, false);
		descriptionFormText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		populateSteps(cheatSheetScrolledForm.getForm().getBody(), cheatSheet.getSteps());
		cheatSheetTabItem.setControl(cheatSheetScrolledForm);
	}

	private void populateSteps(Composite parent, Step[] steps) {
		for (int i = 0; i < steps.length; i++) {
			final Section stepSection = formToolkit.createSection(parent, Section.TWISTIE | Section.SHORT_TITLE_BAR);// | Section.CLIENT_INDENT);
			stepSection.setText(steps[i].getTitle());
			stepSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			FormText descriptionFormText = formToolkit.createFormText(stepSection, false);
			descriptionFormText.addFocusListener(this);
			descriptionFormText.setText(steps[i].getDescription(), true, false);
			descriptionFormText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			descriptionFormText.setFont("code", JFaceResources.getTextFont());
			populateImages(descriptionFormText);
			stepSection.setClient(descriptionFormText);
			Control[] controls = stepSection.getChildren();
			for (int j = 0; j < controls.length; j++) if(controls[j] instanceof Twistie) controls[j].addFocusListener(this);
		}
	}

	private void populateImages(FormText descriptionFormText) {
		descriptionFormText.setImage("RUN_ICON", ImagesUtils.getImage(IImagesKeys.RUN_ICON));
		descriptionFormText.setImage("LOADED_ICON", ImagesUtils.getImage(IImagesKeys.LOADED_SUBJECT_ICON_DECORATOR));
		descriptionFormText.setImage("SUBJECT_ICON", ImagesUtils.getImage(IImagesKeys.SUBJECT_ICON));
		descriptionFormText.setImage("DELETE_ICON", ImagesUtils.getImage(IImagesKeys.DELETE_ICON));
		descriptionFormText.setImage("DELETE2_ICON", ImagesUtils.getImage(IImagesKeys.REMOVE_FIELD_MARKERS_GROUP_ICON));
		descriptionFormText.setImage("DELETE3_ICON", ImagesUtils.getImage(IImagesKeys.REMOVE_MARKER_TRIAL_ICON));
		descriptionFormText.setImage("NEXT_ICON", ImagesUtils.getImage(IImagesKeys.NEXT_TRIAL_ICON));
		descriptionFormText.setImage("PREVIOUS_ICON", ImagesUtils.getImage(IImagesKeys.PREVIOUS_TRIAL_ICON));
		descriptionFormText.setImage("LOAD_ICON", ImagesUtils.getImage(IImagesKeys.LOAD_UNLOAD_SUBJECT_ICON));
		descriptionFormText.setImage("ADD_CATEGORY_ICON", ImagesUtils.getImage(IImagesKeys.ADD_CATEGORY_ICON));
		descriptionFormText.setImage("ADD_FUNCTION_ICON", ImagesUtils.getImage(IImagesKeys.ADD_FUNCTION_ICON));
		descriptionFormText.setImage("RUN_PROCESS_ICON", ImagesUtils.getImage(IImagesKeys.RUN_PROCESS_ICON));
		descriptionFormText.setImage("CHECK_PROCESS_ICON", ImagesUtils.getImage(IImagesKeys.CHECK_PROCESS_ICON));;
		descriptionFormText.setImage("ANALYSE_IMAGE", ImagesUtils.getImage(IImagesKeys.ANALYSE_IMAGE));
		descriptionFormText.setImage("EXPERIMENTS_VIEW_IMAGE", ImagesUtils.getImage(IImagesKeys.EXPERIMENTS_VIEW_IMAGE));
		descriptionFormText.setImage("SIGNALS_VIEW_IMAGE", ImagesUtils.getImage(IImagesKeys.SIGNALS_VIEW_IMAGE));
		descriptionFormText.setImage("CATEGORIES_VIEW_IMAGE", ImagesUtils.getImage(IImagesKeys.CATEGORIES_VIEW_IMAGE));
		descriptionFormText.setImage("PREVIEW_SIGNALS_VIEW_IMAGE", ImagesUtils.getImage(IImagesKeys.PREVIEW_SIGNALS_VIEW_IMAGE));
		descriptionFormText.setImage("MESSAGES_VIEW_IMAGE", ImagesUtils.getImage(IImagesKeys.MESSAGES_VIEW_IMAGE));
		descriptionFormText.setImage("CONSOLE_VIEW_IMAGE", ImagesUtils.getImage(IImagesKeys.CONSOLE_VIEW_IMAGE));
		descriptionFormText.setImage("CHART_EDITOR_VIEW_IMAGE", ImagesUtils.getImage(IImagesKeys.CHART_EDITOR_VIEW_IMAGE));
		descriptionFormText.setImage("PROCESS_EDITOR_VIEW_IMAGE", ImagesUtils.getImage(IImagesKeys.PROCESS_EDITOR_VIEW_IMAGE));
		descriptionFormText.setImage("ADD_ICON", ImagesUtils.getImage(IImagesKeys.ADD_ICON));
		descriptionFormText.setImage("START_SERVER_ICON", ImagesUtils.getImage(IImagesKeys.START_SERVER_ICON));
		descriptionFormText.setImage("CONNECT_SERVER_ICON", ImagesUtils.getImage(IImagesKeys.CONNECT_SERVER_ICON));
		descriptionFormText.setImage("IMPORT_ICON", ImagesUtils.getImage(IImagesKeys.IMPORT_ICON));
		descriptionFormText.setImage("FUNCTION_ICON", ImagesUtils.getImage(IImagesKeys.FUNCTION_ICON));
		descriptionFormText.setImage("FUNCTION_CONFIG", ImagesUtils.getImage(IImagesKeys.FUNCTION_CONFIG));
		descriptionFormText.setImage("FUNCTION_CODE", ImagesUtils.getImage(IImagesKeys.FUNCTION_CODE));
	}
	
}
