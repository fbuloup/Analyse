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
package functioneditor.windows;

import analyse.model.Function;

import java.util.regex.*;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import analyse.gui.dialogs.RegExpSelectionDialog;

import analyse.resources.ColorsUtils;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;


public class FeaturesComposite extends Composite  {
	
	private Text matlabFunctionCommentsText;
	private Text guiMatlabFunctionNameText;
	private Text matlabFunctionNameText;
	private Text matlabFunctionShortDescriptionText;
	private Text matlabFunctionLongDescriptionText;
	private Spinner nbUsedChannelsSpinner;
	private Spinner nbUsedMarkersSpinner;
	private Spinner nbUsedFieldsSpinner;
	private Spinner nbCreatedChannelsSpinner;
	private Spinner nbCreatedMarkersSpinner;
	private Spinner nbCreatedFieldsSpinner;	
	private Spinner nbModifiedSignalsSpinner;
	private Function function;
	private CTabFolder ctabFolder;
	private boolean editable;
	private Composite parameters;
	private Composite options;

	public FeaturesComposite(Composite parent, int style, Function function, boolean editable) {
		super(parent, style);
		
		this.function = function;
		this.editable = editable;
		
		GridLayout gridLayout = new GridLayout(1,true);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		
		this.setLayout(gridLayout);
		
		TabFolder tf = new TabFolder(this, SWT.NONE);
		tf.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TabItem options = new TabItem(tf, SWT.NONE);
		options.setText(Messages.getString("FeaturesComposite.OptionsTabItemTitle")); //$NON-NLS-1$
		
		TabItem parameters = new TabItem(tf, SWT.NONE);
		parameters.setText(Messages.getString("FeaturesComposite.ParametersTabItemTitle")); //$NON-NLS-1$
		
		options.setControl(createOptionComposite(tf));
		parameters.setControl(createParametersComposite(tf));
		
		
	}
	
	public void setEditable() {
		parameters.setEnabled(true);
		options.setEnabled(true);
	}
	
	private Composite createParametersComposite(Composite parent){
		
		parameters = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		parameters.setLayout(layout);
		parameters.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Button addParameters = new Button(parameters,  SWT.PUSH);
		addParameters.setText(Messages.getString("FeaturesComposite.AddParameterButtonTitle")); //$NON-NLS-1$
		addParameters.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		ctabFolder = new CTabFolder(parameters,  SWT.CLOSE | SWT.BORDER);
		ctabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		ctabFolder.setSelectionBackground(new Color[]{ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND),ColorsUtils.getColor(ColorsUtils.COLOR_TITLE_BACKGROUND_GRADIENT)},new int[]{70}, true);		
		ctabFolder.setSelectionForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));	
		ctabFolder.addCTabFolder2Listener(new CTabFolder2Listener(){
			
			public void close(CTabFolderEvent event) {
				boolean found = false;
				for (int i = 0; i < ctabFolder.getItemCount(); i++) {
					if(found){
						ctabFolder.getItem(i).setText(Messages.getString("FeaturesComposite.ParameterTabItemTitle") + i); //$NON-NLS-1$
					}
					if (ctabFolder.getItem(i) == event.item){
						found = true;
						function.removeParameter(i);
					}
				}
			}

			public void maximize(CTabFolderEvent event) {
			}

			public void minimize(CTabFolderEvent event) {
			}

			public void restore(CTabFolderEvent event) {
			}

			public void showList(CTabFolderEvent event) {
			}
		});		
		
		addParameters.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseUp(MouseEvent e) {
				function.createParameter();
				createParameter();
			}
		});
		
		int numberOfParameters = function.getParametersCount();
		for (int i = 1; i <= numberOfParameters; i++) {
			createParameter();
			ctabFolder.setSelection(0);
		}
		
		parameters.setEnabled(editable);

		return parameters;
	}
	
	public void createParameter(){
		CTabItem paramTabItem = new CTabItem(ctabFolder, SWT.CLOSE);		
		paramTabItem.setImage(ImagesUtils.getImage(IImagesKeys.PARAMETER_ICON));
		ctabFolder.setSelection(paramTabItem);
		paramTabItem.setText(Messages.getString("FeaturesComposite.ParameterTabItemTitle") + ctabFolder.getItemCount()); //$NON-NLS-1$
		paramTabItem.setControl(new Contents(ctabFolder));
		//ctabFolder.setSelection(paramTabItem);
	}
	
	private Composite createOptionComposite(Composite parent){
	
		options = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(1,false);
		options.setLayout(layout);
		options.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite optionsUp = new Composite(options, SWT.NONE);
		GridLayout layout3 = new GridLayout(2,false);
		optionsUp.setLayout(layout3);
		optionsUp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label matlabFunctionComments = new Label(optionsUp,SWT.NONE);
		matlabFunctionComments.setText(Messages.getString("FeaturesComposite.MatlabCommentLabelTitle")); //$NON-NLS-1$
		matlabFunctionCommentsText = new Text(optionsUp, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		matlabFunctionCommentsText.setToolTipText(Messages.getString("FeaturesComposite.MatlabCommentLabelTooltip")); //$NON-NLS-1$
		
		matlabFunctionCommentsText.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				function.setMatlabComments(matlabFunctionCommentsText.getText());
			}
		});
		
		GridData gridData1 = new GridData(GridData.FILL_BOTH);
		gridData1.horizontalSpan=2;
		matlabFunctionCommentsText.setLayoutData(gridData1);
		if (function.getMatlabComments()!=null){
			matlabFunctionCommentsText.setText(function.getMatlabComments());
		}else{
			matlabFunctionCommentsText.setText(Messages.getString("FeaturesComposite.MatlabCommentTextInitialText")); //$NON-NLS-1$
		}
		
		Label matlabFunctionName = new Label(optionsUp,SWT.NONE);
		matlabFunctionName.setText(Messages.getString("FeaturesComposite.FunctionNameLabelTitle")); //$NON-NLS-1$
		matlabFunctionNameText = new Text(optionsUp, SWT.BORDER);
		matlabFunctionNameText.setToolTipText(Messages.getString("FeaturesComposite.FunctionNameLabelTooltip")); //$NON-NLS-1$
		matlabFunctionNameText.setData(new String("Function's name")); //$NON-NLS-1$
		matlabFunctionNameText.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				function.setMatlabFunctionName(matlabFunctionNameText.getText());
				matlabPatternMatcher(matlabFunctionNameText, "^[a-zA-Z][a-zA-Z0-9_]{0,31}$"); //$NON-NLS-1$

			}
		});
		matlabFunctionNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		matlabFunctionNameText.setText(function.getMatlabFunctionName());
		
		Label guiMatlabFunctionName = new Label(optionsUp,SWT.NONE);
		guiMatlabFunctionName.setText(Messages.getString("FeaturesComposite.GUIFunctionNameLabelTitle")); //$NON-NLS-1$
		guiMatlabFunctionNameText = new Text(optionsUp, SWT.BORDER);
		guiMatlabFunctionNameText.setData(new String("GUI function's name")); //$NON-NLS-1$
		guiMatlabFunctionNameText.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				function.setMatlabGUIFunctionName(guiMatlabFunctionNameText.getText());
				matlabPatternMatcher(guiMatlabFunctionNameText,""); //$NON-NLS-1$
			}
		});
		
		guiMatlabFunctionNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (function.getGUIFunctionName()!=null){
			guiMatlabFunctionNameText.setText(function.getGUIFunctionName());
		}else{
			guiMatlabFunctionNameText.setText(Messages.getString("FeaturesComposite.GUIFunctionNameTextInitialText")); //$NON-NLS-1$
		}		
		
		Label MatlabFunctionShortDescription = new Label(optionsUp,SWT.NONE);
		MatlabFunctionShortDescription.setText(Messages.getString("FeaturesComposite.ShortDescriptionLabelTitle")); //$NON-NLS-1$
		matlabFunctionShortDescriptionText = new Text(optionsUp, SWT.BORDER);
		matlabFunctionShortDescriptionText.setToolTipText(Messages.getString("FeaturesComposite.ShortDescriptionTextTooltip")); //$NON-NLS-1$
		matlabFunctionShortDescriptionText.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				function.setShortDescription(matlabFunctionShortDescriptionText.getText());
			}
		});
		
		GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL);
		gridData2.horizontalSpan=2;
		matlabFunctionShortDescriptionText.setLayoutData(gridData2);
		if (function.getShortDescription()!=null){
			matlabFunctionShortDescriptionText.setText(function.getShortDescription());
		}else{
			matlabFunctionShortDescriptionText.setText(Messages.getString("FeaturesComposite.ShortDescriptionTextInitialText")); //$NON-NLS-1$
		}
		
		Label matlabFunctionLongDescription = new Label(optionsUp,SWT.NONE);
		matlabFunctionLongDescription.setText(Messages.getString("FeaturesComposite.GUILongDescriptionLabelTitle")); //$NON-NLS-1$
		matlabFunctionLongDescriptionText = new Text(optionsUp, SWT.BORDER|SWT.MULTI|SWT.WRAP);
		matlabFunctionLongDescriptionText.setToolTipText(Messages.getString("FeaturesComposite.LongDescriptionTextTooltip")); //$NON-NLS-1$
		matlabFunctionLongDescriptionText.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				String description = matlabFunctionLongDescriptionText.getText().replaceAll("\\r","\\\n");
				description = description.replaceAll("\\n"," ");
				function.setLongDescription(description);
			}
		});
		
		GridData gridData3 = new GridData(GridData.FILL_BOTH);
		gridData3.horizontalSpan=2;
		matlabFunctionLongDescriptionText.setLayoutData(gridData3);
		if (function.getLongDescription()!=null){
			String lgDescription = function.getLongDescription().replace("<form>", ""); //$NON-NLS-1$ //$NON-NLS-2$
			lgDescription = lgDescription.replace("</form>", ""); //$NON-NLS-1$ //$NON-NLS-2$
			matlabFunctionLongDescriptionText.setText(lgDescription);
		}else{
			matlabFunctionLongDescriptionText.setText(Messages.getString("FeaturesComposite.LongDescriptionTextInitialText")); //$NON-NLS-1$
		}
		
		Composite spinners = new Composite(options, SWT.NONE);
		GridLayout layout2 = new GridLayout(2,false);
		spinners.setLayout(layout2);
		spinners.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label numberChannelUsed = new Label(spinners,SWT.NONE);
		numberChannelUsed.setText(Messages.getString("FeaturesComposite.NbChannelsUsedLabelTitle")); //$NON-NLS-1$
		nbUsedChannelsSpinner = new Spinner (spinners, SWT.BORDER);
		nbUsedChannelsSpinner.setMinimum(0);
		nbUsedChannelsSpinner.setMaximum(100);
		nbUsedChannelsSpinner.setSelection(0);
		nbUsedChannelsSpinner.setIncrement(1);
		nbUsedChannelsSpinner.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				function.setSignalsUsedNumber(nbUsedChannelsSpinner.getSelection());
			}
		});
		
		nbUsedChannelsSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nbUsedChannelsSpinner.setSelection(function.getSignalsUsedNumber());
		
		
		Label numberMarkersUsed = new Label(spinners,SWT.NONE);
		numberMarkersUsed.setText(Messages.getString("FeaturesComposite.NbMarkersUsedLabelTitle")); //$NON-NLS-1$
		nbUsedMarkersSpinner = new Spinner (spinners, SWT.BORDER);
		nbUsedMarkersSpinner.setMinimum(0);
		nbUsedMarkersSpinner.setMaximum(100);
		nbUsedMarkersSpinner.setSelection(0);
		nbUsedMarkersSpinner.setIncrement(1);
		nbUsedMarkersSpinner.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				function.setMarkersUsedNumber(nbUsedMarkersSpinner.getSelection());
			}
		});
		
		nbUsedMarkersSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nbUsedMarkersSpinner.setSelection(function.getMarkersUsedNumber());
		
		Label numberFieldsUsed = new Label(spinners,SWT.NONE);
		numberFieldsUsed.setText(Messages.getString("FeaturesComposite.NbFieldsUsedLabelTitle")); //$NON-NLS-1$
		nbUsedFieldsSpinner = new Spinner (spinners, SWT.BORDER);
		nbUsedFieldsSpinner.setMinimum(0);
		nbUsedFieldsSpinner.setMaximum(100);
		nbUsedFieldsSpinner.setSelection(0);
		nbUsedFieldsSpinner.setIncrement(1);
		nbUsedFieldsSpinner.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				function.setFieldsUsedNumber(nbUsedFieldsSpinner.getSelection());
			}
		});
		
		nbUsedFieldsSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nbUsedFieldsSpinner.setSelection(function.getFieldsUsedNumber());
		
		Label numberChannelCreated = new Label(spinners,SWT.NONE);
		numberChannelCreated.setText(Messages.getString("FeaturesComposite.NbCreatedChannelsLabelTitle")); //$NON-NLS-1$
		nbCreatedChannelsSpinner = new Spinner (spinners, SWT.BORDER);
		nbCreatedChannelsSpinner.setMinimum(0);
		nbCreatedChannelsSpinner.setMaximum(100);
		nbCreatedChannelsSpinner.setSelection(0);
		nbCreatedChannelsSpinner.setIncrement(1);
		nbCreatedChannelsSpinner.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				function.setSignalsCreatedNumber(nbCreatedChannelsSpinner.getSelection());
			}
		});
		
		nbCreatedChannelsSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nbCreatedChannelsSpinner.setSelection(function.getSignalsCreatedNumber());
		
		Label numberMarkersCreated = new Label(spinners,SWT.NONE);
		numberMarkersCreated.setText(Messages.getString("FeaturesComposite.NbCreatedMarkersLabelTitle")); //$NON-NLS-1$
		nbCreatedMarkersSpinner = new Spinner (spinners, SWT.BORDER);
		nbCreatedMarkersSpinner.setMinimum(0);
		nbCreatedMarkersSpinner.setMaximum(100);
		nbCreatedMarkersSpinner.setSelection(0);
		nbCreatedMarkersSpinner.setIncrement(1);
		nbCreatedMarkersSpinner.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				function.setMarkersCreatedNumber(nbCreatedMarkersSpinner.getSelection());
			}
		});
		
		nbCreatedMarkersSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nbCreatedMarkersSpinner.setSelection(function.getMarkersCreatedNumber());
		
		Label numberFieldsCreated = new Label(spinners,SWT.NONE);
		numberFieldsCreated.setText(Messages.getString("FeaturesComposite.NbCreatedFieldsLabelTitle")); //$NON-NLS-1$
		nbCreatedFieldsSpinner = new Spinner (spinners, SWT.BORDER);
		nbCreatedFieldsSpinner.setMinimum(0);
		nbCreatedFieldsSpinner.setMaximum(100);
		nbCreatedFieldsSpinner.setSelection(0);
		nbCreatedFieldsSpinner.setIncrement(1);
		nbCreatedFieldsSpinner.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				function.setFieldsCreatedNumber(nbCreatedFieldsSpinner.getSelection());
			}
		});
		
		nbCreatedFieldsSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nbCreatedFieldsSpinner.setSelection(function.getFieldsCreatedNumber());
		
		Label numberSignalsModified = new Label(spinners,SWT.NONE);
		numberSignalsModified.setText(Messages.getString("FeaturesComposite.NbModifiedSignalsLabelTitle")); //$NON-NLS-1$
		nbModifiedSignalsSpinner = new Spinner (spinners, SWT.BORDER);
		nbModifiedSignalsSpinner.setMinimum(0);
		nbModifiedSignalsSpinner.setMaximum(100);
		nbModifiedSignalsSpinner.setSelection(0);
		nbModifiedSignalsSpinner.setIncrement(1);
		nbModifiedSignalsSpinner.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				function.setSignalsModifiedNumber(nbModifiedSignalsSpinner.getSelection());
			}
		});
		
		nbModifiedSignalsSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nbModifiedSignalsSpinner.setSelection(function.getSignalsModifiedNumber());
		
		options.setEnabled(editable);
		
		return options;
	}
	
	protected void matlabPatternMatcher(Text text, String regularExpression) {
		String matched = ""; //$NON-NLS-1$
		String title = (String) text.getData();
		if (text.getText().equals("")) { //$NON-NLS-1$
			MessageDialog messageDialog = new MessageDialog(getParent().getShell(),Messages.getString("FeaturesComposite.ErrorMessageDialogTitle"), ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON), title + Messages.getString("FeaturesComposite.ErrorMessageDialogText1"), MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL}, 0); //$NON-NLS-1$ //$NON-NLS-2$
			messageDialog.open();
			text.setText(Messages.getString("FeaturesComposite.TextResetedValue")); //$NON-NLS-1$
			text.selectAll();
		}
		if(!regularExpression.equals("")) //$NON-NLS-1$
		if (!text.getText().matches(regularExpression)) {
			if (text.getText().matches("^[0-9_].*")) { //$NON-NLS-1$
				MessageDialog messageDialog = new MessageDialog(getParent().getShell(),Messages.getString("FeaturesComposite.ErrorMessageDialogTitle"), ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON),  title + Messages.getString("FeaturesComposite.ErrorMessageDialogText2"), MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL}, 0); //$NON-NLS-1$ //$NON-NLS-2$
				messageDialog.open();
				text.setText(text.getText().replaceAll("^[0-9_]+","")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!text.getText().matches("^[a-zA-Z][a-zA-Z0-9_]*$")) { //$NON-NLS-1$
				MessageDialog messageDialog = new MessageDialog(getParent().getShell(),Messages.getString("FeaturesComposite.ErrorMessageDialogTitle"), ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON),  title + Messages.getString("FeaturesComposite.ErrorMessageDialogText3"), MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL}, 0); //$NON-NLS-1$ //$NON-NLS-2$
				messageDialog.open();
				Pattern myPattern = Pattern.compile("[a-zA-Z0-9_]+"); //$NON-NLS-1$
				Matcher result = myPattern.matcher(text.getText());
				while(result.find()){
					matched = matched + result.group();
				}
				text.setText(matched);				
			}
			if (text.getText().length()>32) {
				MessageDialog messageDialog = new MessageDialog(getParent().getShell(),Messages.getString("FeaturesComposite.ErrorMessageDialogTitle"), ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON),  title + Messages.getString("FeaturesComposite.ErrorMessageDialogText4"), MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL}, 0); //$NON-NLS-1$ //$NON-NLS-2$
				messageDialog.open();
				text.setText(text.getText().substring(0, 32));
			}
			if (!text.getText().matches("^[A-Z][a-zA-Z0-9_]*$")) { //$NON-NLS-1$
				MessageDialog messageDialog = new MessageDialog(getParent().getShell(),Messages.getString("FeaturesComposite.ErrorMessageDialogTitle"), ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON),  title + Messages.getString("FeaturesComposite.ErrorMessageDialogText5"), MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL}, 0); //$NON-NLS-1$ //$NON-NLS-2$
				messageDialog.open();
				Pattern myPattern = Pattern.compile("[A-Z0-9_]+"); //$NON-NLS-1$
				Matcher result = myPattern.matcher(text.getText());
				while(result.find()){
					matched = matched + result.group();
				}
				if(matched.equals("")) //$NON-NLS-1$
					text.setText(Messages.getString("FeaturesComposite.TextResetedValue")); //$NON-NLS-1$
				else text.setText(matched);				
			}
		}
	}

	private class Contents extends Composite {		
		
		private Combo matlabTypeParameterCombo;
		private Combo componentCombo;
		private Text labelText;
		private Text toolTipText;
		private Text regExpText;
		
		private List availableValuesList;
		private Button addElementsList;
		private Text defaultValueText;
		/*private Button channelsAvailableCheck;
		private Button markerAvailableCheck;
		private Button fieldAvailableCheck;*/
		
		
		public Contents(final CTabFolder parent) {
		
			super( parent, SWT.NONE);
						
			setLayout(new GridLayout(2, false));
			
			Label matlabType = new Label(this,SWT.NONE);
			matlabType.setText(Messages.getString("FeaturesComposite.ParamMatlabTypeLabelTitle")); //$NON-NLS-1$
			matlabType.setLayoutData(new GridData(GridData.END,GridData.CENTER,false, false));
			
			matlabTypeParameterCombo = new Combo(this, SWT.NONE | SWT.READ_ONLY );
			matlabTypeParameterCombo.setItems(new String[]{Function.GUI_MATLAB_TYPE_INTEGER,Function.GUI_MATLAB_TYPE_FLOAT,Function.GUI_MATLAB_TYPE_STRING});
			matlabTypeParameterCombo.setSize(100, 100);
			matlabTypeParameterCombo.addModifyListener(new ModifyListener(){

				public void modifyText(ModifyEvent e) {
					function.setParameterMatlabType(matlabTypeParameterCombo.getText(), parent.getSelectionIndex());					
				}
				
			});
			matlabTypeParameterCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			matlabTypeParameterCombo.setText(function.getParametersMatlabType(parent.getSelectionIndex()));
			
			
			Label component = new Label(this,SWT.NONE);
			component.setText(Messages.getString("FeaturesComposite.ParamGUIComponentComboTitle")); //$NON-NLS-1$
			component.setLayoutData(new GridData(GridData.END,GridData.CENTER,false, false));
			componentCombo = new Combo(this,SWT.NONE | SWT.READ_ONLY);
			componentCombo.setItems(new String[]{Function.GUI_TYPE_TEXT_LABEL,
												 Function.GUI_TYPE_CHECK_BUTTON_LABEL,
												 Function.GUI_TYPE_COMBOBOX_LABEL,
												 Function.GUI_TYPE_LIST_LABEL});
			componentCombo.setSize(100, 100);
			componentCombo.addModifyListener(new ModifyListener(){

				public void modifyText(ModifyEvent e) {
					function.setParameterComponent(componentCombo.getSelectionIndex() + 1, parent.getSelectionIndex());
					
				}
				
			});
			componentCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			componentCombo.select(function.getParameterType(parent.getSelectionIndex()) - 1);
			
			Label label = new Label(this,SWT.NONE);
			label.setText(Messages.getString("FeaturesComposite.ParamLabelLabelTitle")); //$NON-NLS-1$
			label.setLayoutData(new GridData(GridData.END,GridData.CENTER,false, false));
			labelText = new Text(this,SWT.BORDER);
			labelText.setData("Parameter's name"); //$NON-NLS-1$
			labelText.addModifyListener(new ModifyListener(){

				public void modifyText(ModifyEvent e) {
					function.setParameterLabel(labelText.getText(), parent.getSelectionIndex());
					matlabPatternMatcher(labelText, "^[A-Z][a-zA-Z0-9_]{0,31}$"); //$NON-NLS-1$
				}
			});
			
			labelText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			labelText.setText(function.getParameterLabel(parent.getSelectionIndex()));
			
			Label toolTip = new Label(this,SWT.NONE);
			toolTip.setText(Messages.getString("FeaturesComposite.ParamTooltipLabelTitle")); //$NON-NLS-1$
			toolTip.setLayoutData(new GridData(GridData.END,GridData.CENTER,false, false));
			toolTipText = new Text(this,SWT.BORDER);
			toolTipText.addModifyListener(new ModifyListener(){

				public void modifyText(ModifyEvent e) {
					function.setParameterToolTip(toolTipText.getText(), parent.getSelectionIndex());
				}
			});
			
			toolTipText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			toolTipText.setText(function.getToolTip(parent.getSelectionIndex()));
			
			Label regExp = new Label(this,SWT.NONE);
			regExp.setText(Messages.getString("FeaturesComposite.ParamRegExpLabelTitle")); //$NON-NLS-1$
			regExp.setLayoutData(new GridData(GridData.END,GridData.CENTER,false, false));
			
			Composite regExpContainer  = new Composite(this, SWT.NONE);
			regExpContainer.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true, false));
			regExpContainer.setLayout(new GridLayout(2,false));
			
			regExpText = new Text(regExpContainer,SWT.BORDER);
			regExpText.addModifyListener(new ModifyListener(){

				public void modifyText(ModifyEvent e) {
					function.setParameterRegExp(regExpText.getText(), parent.getSelectionIndex());
				}
			});
			
			regExpText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			regExpText.setText(function.getParametersRegExp(parent.getSelectionIndex()));
			
			Button selectButton = new Button(regExpContainer, SWT.PUSH);
			selectButton.setText(Messages.getString("FeaturesComposite.SelectRegExpButtonTitle")); //$NON-NLS-1$
			selectButton.setLayoutData(new GridData(GridData.FILL,GridData.FILL,false, false));
			
			selectButton.addSelectionListener(new SelectionAdapter(){

				public void widgetSelected(SelectionEvent e) {
					RegExpSelectionDialog regExpSelectionDialog = new RegExpSelectionDialog(Display.getCurrent().getActiveShell());
					if(regExpSelectionDialog.open() == Window.OK) {
						regExpText.setText(regExpSelectionDialog.getRegularExpression());
					}
				}
				
			});
	
			Label defaultValue = new Label(this, SWT.NONE);
			defaultValue.setText(Messages.getString("FeaturesComposite.ParamDefaultValueLabelTitle")); //$NON-NLS-1$
			defaultValue.setLayoutData(new GridData(GridData.END,GridData.CENTER,false, false));
			defaultValueText = new Text(this,SWT.BORDER);
			defaultValueText.setToolTipText(Messages.getString("FeaturesComposite.ParamDefaultValueTextTooltip")); //$NON-NLS-1$
			defaultValueText.addModifyListener(new ModifyListener(){

				public void modifyText(ModifyEvent e) {
					function.setParameterDefaultValue(defaultValueText.getText(), parent.getSelectionIndex());
				}
			});
			
			defaultValueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			defaultValueText.setText((String) function.getParameterValue(parent.getSelectionIndex()));
			
			/*Label channelsAvailable = new Label(this, SWT.NONE);
			channelsAvailable.setText(Messages.getString("FeaturesComposite.SignalAvailableLabelTitle")); //$NON-NLS-1$
			channelsAvailable.setLayoutData(new GridData(GridData.END,GridData.CENTER,false, false));
			channelsAvailableCheck = new Button(this,SWT.CHECK);
			channelsAvailableCheck.setBackground(null);
			channelsAvailableCheck.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseUp(MouseEvent e) {
					super.mouseUp(e);
					function.setSignalsAvailableBool(parent.getSelectionIndex(), channelsAvailableCheck.getSelection());
				}
			});
			channelsAvailableCheck.setSelection(function.getSignalsAvailableBool(parent.getSelectionIndex()));
			
			Label markerAvailable = new Label(this, SWT.NONE);
			markerAvailable.setText(Messages.getString("FeaturesComposite.MarkerAvailableLabelTitle")); //$NON-NLS-1$
			markerAvailable.setLayoutData(new GridData(GridData.END,GridData.CENTER,false, false));
			markerAvailableCheck = new Button(this,SWT.CHECK);
			markerAvailableCheck.setBackground(null);
			markerAvailableCheck.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseUp(MouseEvent e) {
					super.mouseUp(e);
					function.setMarkersAvailableBool(parent.getSelectionIndex(), markerAvailableCheck.getSelection());
				}
			});
			markerAvailableCheck.setSelection(function.getMarkersAvailableBool(parent.getSelectionIndex()));
			
			Label fieldAvailable = new Label(this, SWT.NONE);
			fieldAvailable.setText(Messages.getString("FeaturesComposite.FieldAvailableLabelTitle")); //$NON-NLS-1$
			fieldAvailable.setLayoutData(new GridData(GridData.END,GridData.CENTER,false, false));
			fieldAvailableCheck = new Button(this,SWT.CHECK);
			fieldAvailableCheck.setBackground(null);
			fieldAvailableCheck.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseUp(MouseEvent e) {
					super.mouseUp(e);
					function.setFieldsAvailableBool(parent.getSelectionIndex(), fieldAvailableCheck.getSelection());
				}
			});
			fieldAvailableCheck.setSelection(function.getFieldAvailableBool(parent.getSelectionIndex()));*/
			
			Label availableListValue = new Label(this, SWT.NONE);
			availableListValue.setText(Messages.getString("FeaturesComposite.ParamListValueLabelTitle")); //$NON-NLS-1$
			availableListValue.setLayoutData(new GridData(GridData.FILL,GridData.CENTER,false, false,2,1));
			
			availableValuesList = new List(this, SWT.BORDER|SWT.MULTI);
			availableValuesList.setLayoutData(new GridData(GridData.FILL,GridData.FILL,false, true,2,1));
			if(function.getAvailablesValues(parent.getSelectionIndex()) != null)
			availableValuesList.setItems(function.getAvailablesValues(parent.getSelectionIndex()));
			
			Composite buttonsContainer = new Composite(this, SWT.NONE);
			buttonsContainer.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true, false,2,1));
			buttonsContainer.setLayout(new GridLayout(2,true));
			
			addElementsList = new Button(buttonsContainer, SWT.NONE);
			addElementsList.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true, false));
			addElementsList.setText(Messages.getString("FeaturesComposite.AddAvailableValueButtonTitle")); //$NON-NLS-1$
			addElementsList.addMouseListener(new MouseAdapter(){
				public void mouseUp(MouseEvent e){
					InputDialog inputDialog = new InputDialog(Display.getCurrent().getActiveShell(),Messages.getString("FeaturesComposite.InputDialogAvailableValueTitle"),Messages.getString("FeaturesComposite.InputDialogAvailableValueLabel"),Messages.getString("FeaturesComposite.InputDialogAvailableValueInitialTextValue"),null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					if(inputDialog.open() == Window.OK) {
						int n = availableValuesList.indexOf(inputDialog.getValue());
						if (n != -1) {
							MessageDialog messageDialog = new MessageDialog(Display.getCurrent().getActiveShell(),Messages.getString("FeaturesComposite.ErrorMessageDialogTitle"),null,Messages.getString("FeaturesComposite.ErrorMessageDialogText6"),MessageDialog.ERROR,new String[]{"OK"},0); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							messageDialog.open();
						}else
						availableValuesList.add(inputDialog.getValue());
						function.setAvailablesValues(parent.getSelectionIndex(), availableValuesList.getItems());
					}
				}
			});
			
			Button deleteElementsList = new Button(buttonsContainer, SWT.NONE);
			deleteElementsList.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true, false));
			deleteElementsList.setText(Messages.getString("FeaturesComposite.DeleteAvailableListValuesButtonTitle")); //$NON-NLS-1$
			deleteElementsList.addMouseListener(new MouseAdapter(){
				public void mouseUp(MouseEvent e){
					String[] deletedSelection = availableValuesList.getSelection();
					for(int i = 0; i < deletedSelection.length; i++)
					availableValuesList.remove(deletedSelection[i]);
					function.setAvailablesValues(parent.getSelectionIndex(), availableValuesList.getItems());
				}
			});
			
			
		}

	}
	
}



