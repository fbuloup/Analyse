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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.internal.forms.MessageManager;

import analyse.Log;
import analyse.gui.dialogs.AddFunctionDialog;
import analyse.gui.dialogs.InputsSelectionDialog;
import analyse.gui.dialogs.RefactorDialog;
import analyse.gui.dialogs.TrialsListSelectionDialog;
import analyse.model.DataProcessing;
import analyse.model.Experiments;
import analyse.model.Function;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Processing;
import analyse.preferences.LibraryPreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class ProcessingEditor extends CTabItem implements IResourceObserver {
	
	public final static String PARAMETER_NUMBER = "PARAMETER_NUMBER"; //$NON-NLS-1$
	public final static String OUTPUT_KIND = "OUTPUT_KIND"; //$NON-NLS-1$

	private class UpFunctionAction extends Action {
		public UpFunctionAction() {
			setToolTipText(Messages.getString("ProcessEditor.MoveFunctionUpButtonTooltip"));
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.MOVE_FUNCTION_UP));
		}
		@Override
		public void run() {
			Object[] functions = ((IStructuredSelection)usedFunctionsTableViewer.getSelection()).toArray();
			processing.getDataProcessing().moveUp(functions);
			processing.saveProcessing();
			usedFunctionsTableViewer.refresh();
			usedFunctionsForm.setFocus();
		}
	}
	private class DownFunctionAction extends Action {
		public DownFunctionAction() {
			setToolTipText( Messages.getString("ProcessEditor.MoveFunctionDownButtonTooltip"));
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.MOVE_FUNCTION_DOWN));
		}
		@Override
		public void run() {
			Object[] functions = ((IStructuredSelection)usedFunctionsTableViewer.getSelection()).toArray();
			processing.getDataProcessing().moveDown(functions);
			processing.saveProcessing();
			usedFunctionsTableViewer.refresh();
			usedFunctionsForm.setFocus();
		}
	}
	private class DeleteFunctionAction extends Action {
		public DeleteFunctionAction() {
			setToolTipText(Messages.getString("ProcessEditor.RemoveSelectedFunctionsButtonTooltip"));
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.DELETE_ICON));
		}
		@Override
		public void run() {
			Object[] functions = ((IStructuredSelection)usedFunctionsTableViewer.getSelection()).toArray();
			processing.getDataProcessing().removeFunctions(functions);
			processing.saveProcessing();
			usedFunctionsTableViewer.refresh();
			usedFunctionsForm.setFocus();
			messageManager = null;
		}
	}
	private class AddFunctionAction extends Action {
		public AddFunctionAction() {
			setToolTipText( Messages.getString("ProcessEditor.AddFunction"));
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.ADD_FUNCTION_ICON));
		}
		@Override
		public void run() {
			if(new AddFunctionDialog(null, processing).open() == Window.OK) {
				for (int i = 0; i < AddFunctionDialog.selectedNodeFunctions.size(); i++) {
					processing.getDataProcessing().addFunction(AddFunctionDialog.selectedNodeFunctions.get(i).getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute).getNodeValue());
				}
				processing.saveProcessing();
				usedFunctionsTableViewer.refresh();
			}
			usedFunctionsForm.setFocus();
		}
	}
	private final class RefactorAction extends Action {
		public RefactorAction() {
			super(Messages.getString("RefactorAction.Title"),AS_PUSH_BUTTON);
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.REFACTOR_ICON));
			setEnabled(true);
		}
		public void run() {
			(new RefactorDialog(Display.getDefault().getActiveShell(),new IResource[]{processing})).open();
		}
	}
	private final class RunProcessAction extends Action {
		public RunProcessAction() {
			super(Messages.getString("RunProcessAction.Title"),AS_PUSH_BUTTON);
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.RUN_PROCESS_ICON));
			setEnabled(true);
		}
		public void run() {
			boolean valid = validateProcess(true);
			if(valid) AnalyseApplicationWindow.runProcessingAction.run(processing);
		}
	}
	private final class CheckProcessAction extends Action {
		public CheckProcessAction() {
			super(Messages.getString("CheckProcessAction.Title"),AS_PUSH_BUTTON);
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.CHECK_PROCESS_ICON));
			setEnabled(true);
		}
		public void run() {
			if(validateProcess(true)) Log.logMessage(processing.getLocalPath() + Messages.getString("ProcessingEditor.Ready"));
		}
	}
	
	private Processing processing;
	private ToolBarManager toolBarManager;
	private FormToolkit formToolkit;
	private Table usedFunctionsTable;
	private ScrolledForm usedFunctionsForm;
	private FormText functionDescriptionFormText;
	private Text trialsListText;
	private Button trialsListButton;
	private CTabFolder functionInsOutsParamsTabFolder;
	private Label trialsListLabel;
	private TableViewer usedFunctionsTableViewer;
	private SashForm sashFormContainer;
	private Composite selectedFunctionFormContainer;
	private ScrolledForm selectedFunctionForm;
	private CTabItem inputsSignalsTabItem;
	private CTabItem inputsMarkersTabItem;
	private CTabItem inputsFieldsTabItem;
	private CTabItem outputsTabItem;
	private CTabItem parametersTabItem;
	private MessageManager messageManager;
	private Function selectedFunction;
	
	public ProcessingEditor(CTabFolder parent, int style, final Processing processing) {
		super(parent, style);
		this.processing = processing;
		formToolkit = new FormToolkit(getDisplay());
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeToolBar();
				Experiments.getInstance().removeExperimentObserver(ProcessingEditor.this);
				formToolkit.dispose();
			}
		});
		createContents();
		usedFunctionsTable.setFocus();
		validateProcess(true);
	}

	public boolean validateProcess(boolean logMessages) {
		boolean valid = processing.validate(messageManager, selectedFunction, logMessages);
		usedFunctionsTableViewer.refresh();
		AnalyseApplicationWindow.refreshExperimentsView();
		setImage((new OverlayImage(ImagesUtils.getImage(IImagesKeys.PROCESS_EDITOR_ICON),processing.hasError(),processing.hasWarning())).createImage());
		return valid;
	}

	private void createContents() {
		sashFormContainer = new SashForm(getParent(), SWT.HORIZONTAL);

		createUsedFunctionForm();
		selectedFunctionFormContainer = new Composite(sashFormContainer, SWT.BORDER);
		selectedFunctionFormContainer.setLayout(new FillLayout());
		
		sashFormContainer.setWeights(new int[]{25,75});
		
		setControl(sashFormContainer);
		
	}
	
	private void createUsedFunctionForm() {
		Composite usedFunctionsFormContainer = new Composite(sashFormContainer, SWT.BORDER);
		usedFunctionsFormContainer.setLayout(new FillLayout());
		usedFunctionsForm = formToolkit.createScrolledForm(usedFunctionsFormContainer);
		usedFunctionsForm.setText(Messages.getString("ProcessEditor.AvailAndUsedFunctionFormTitle"));
		usedFunctionsForm.getForm().getBody().setLayout(new GridLayout(1, false));
		formToolkit.decorateFormHeading(usedFunctionsForm.getForm());
		
		usedFunctionsTable = formToolkit.createTable(usedFunctionsForm.getForm().getBody(), SWT.BORDER | SWT.MULTI);
		usedFunctionsTable.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		usedFunctionsTable.addFocusListener((FocusListener) getParent());
		usedFunctionsTableViewer = new TableViewer(usedFunctionsTable);
		usedFunctionsTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				Function[] functions = ((DataProcessing) inputElement).getFunctions();
				for (int i = 0; i < functions.length; i++) {
					functions[i].addFunctionObservers(processing);
				}
				return functions;
			}
		});
		ILabelProvider usedFunctionsTableViewerLabelProvider = new UsedFunctionsTableViewerLabelProvider();
		ILabelDecorator usedFunctionsTableViewerLabelDecorator = new UsedFunctionsTableViewerLabelDecorator();
		usedFunctionsTableViewer.setLabelProvider(new DecoratingLabelProvider(usedFunctionsTableViewerLabelProvider, usedFunctionsTableViewerLabelDecorator));
		
		usedFunctionsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if(selection instanceof IStructuredSelection) {
					setSelectedFunction((Function) ((IStructuredSelection)selection).getFirstElement());
					return; 
				}
				setSelectedFunction(null);
			}
		});
		usedFunctionsTableViewer.setInput(processing.getDataProcessing());
		
		usedFunctionsForm.getToolBarManager().add(new UpFunctionAction());
		usedFunctionsForm.getToolBarManager().add(new DownFunctionAction());
		usedFunctionsForm.getToolBarManager().add(new AddFunctionAction());
		usedFunctionsForm.getToolBarManager().add(new DeleteFunctionAction());
		usedFunctionsForm.getToolBarManager().update(true);
		((ToolBarManager)usedFunctionsForm.getToolBarManager()).getControl().addFocusListener((FocusListener) getParent());
	}

	private void createSelectedFunctionForm() {
		selectedFunctionForm = formToolkit.createScrolledForm(selectedFunctionFormContainer);
		selectedFunctionForm.setText(Messages.getString("ProcessEditor.SelectedFunctionFormTitle") + " : " + selectedFunction.getGUIFunctionName());
		selectedFunctionForm.getForm().getBody().setLayout(new GridLayout(3, false));
		formToolkit.decorateFormHeading(selectedFunctionForm.getForm());
		functionDescriptionFormText = formToolkit.createFormText(selectedFunctionForm.getForm().getBody(), false);
		functionDescriptionFormText.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,false,3,1));
		functionDescriptionFormText.setText("<form>" + selectedFunction.getLongDescription() + "</form>", true, false);
		GridData gridData = new GridData(GridData.FILL,GridData.FILL,true,false,3,1);
		gridData.heightHint = 2;
		formToolkit.createCompositeSeparator(selectedFunctionForm.getForm().getBody()).setLayoutData(gridData);	
		
		trialsListLabel = formToolkit.createLabel(selectedFunctionForm.getForm().getBody(), Messages.getString("ProcessEditor.TrialsListLabel"));
		trialsListLabel.setLayoutData(new GridData(GridData.END,GridData.CENTER,false,false)); //$NON-NLS-1$
		trialsListText = formToolkit.createText(selectedFunctionForm.getForm().getBody(), "", SWT.BORDER);
		trialsListText.setLayoutData(new GridData(GridData.FILL,GridData.CENTER,true,false,1,1));
		trialsListText.addFocusListener((FocusListener) getParent());
		trialsListText.setText(selectedFunction.getTrialsList());
		trialsListText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				selectedFunction.setTrialsList(trialsListText.getText());					
			}				
		});
		
		trialsListButton = formToolkit.createButton(selectedFunctionForm.getForm().getBody(), "...", SWT.PUSH);
		trialsListButton.setLayoutData(new GridData(GridData.FILL,GridData.VERTICAL_ALIGN_BEGINNING,false,false,1,1)); //$NON-NLS-1$
		trialsListButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if((new TrialsListSelectionDialog(null)).open() == Window.OK) {
					trialsListText.setText(TrialsListSelectionDialog.selectedTrialsList);
				}
			}
		});
		functionInsOutsParamsTabFolder = new CTabFolder(selectedFunctionForm.getForm().getBody(), SWT.FLAT | SWT.TOP | SWT.BORDER);
		formToolkit.adapt(functionInsOutsParamsTabFolder, true, true);
		functionInsOutsParamsTabFolder.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
				getParent().setFocus();
			}
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		functionInsOutsParamsTabFolder.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 3, 1));
		if(selectedFunction.getSignalsUsedNumber() > 0) {
			inputsSignalsTabItem = new CTabItem(functionInsOutsParamsTabFolder, SWT.NONE);
			inputsSignalsTabItem.setText(Messages.getString("ProcessEditor.InputSignalsTabItemLabel")); 
			inputsSignalsTabItem.setImage(ImagesUtils.getImage(IImagesKeys.INPUTS_ICON));
		}
		if(selectedFunction.getMarkersUsedNumber() > 0) {
			inputsMarkersTabItem = new CTabItem(functionInsOutsParamsTabFolder, SWT.NONE);
			inputsMarkersTabItem.setText(Messages.getString("ProcessEditor.InputMarkersTabItemLabel")); 
			inputsMarkersTabItem.setImage(ImagesUtils.getImage(IImagesKeys.INPUTS_ICON));
		}
		if(selectedFunction.getFieldsUsedNumber() > 0) {
			inputsFieldsTabItem = new CTabItem(functionInsOutsParamsTabFolder, SWT.NONE);
			inputsFieldsTabItem.setText(Messages.getString("ProcessEditor.InputFieldsTabItemLabel")); 
			inputsFieldsTabItem.setImage(ImagesUtils.getImage(IImagesKeys.INPUTS_ICON));
		}
		if(selectedFunction.getMarkersCreatedNumber() > 0 || selectedFunction.getSignalsCreatedNumber() > 0 || selectedFunction.getFieldsCreatedNumber() > 0) {
			outputsTabItem = new CTabItem(functionInsOutsParamsTabFolder, SWT.NONE);
			outputsTabItem.setText(Messages.getString("ProcessEditor.OutputsTabItemLabel"));
			outputsTabItem.setImage(ImagesUtils.getImage(IImagesKeys.OUTPUTS_ICON));
		}
		if(selectedFunction.getParametersCount() > 0) {
			parametersTabItem = new CTabItem(functionInsOutsParamsTabFolder, SWT.NONE);
			parametersTabItem.setText(Messages.getString("ProcessEditor.ParametersTabItemLabel")); 
			parametersTabItem.setImage(ImagesUtils.getImage(IImagesKeys.PARAMETERS_ICON));
		}
		functionInsOutsParamsTabFolder.setSelection(0);
		selectedFunctionFormContainer.layout();
		
		messageManager = new MessageManager(selectedFunctionForm);
		
	}

	private void setSelectedFunction(Function function) {
		if(selectedFunctionForm != null) selectedFunctionForm.dispose();
		if(function != null) {
			selectedFunction = function;
			createSelectedFunctionForm();
			populateFunctionInformations();
			validateProcess(false);
		} else {
			selectedFunctionForm = null;
		}
	}

	private void populateFunctionInformations() {
		if(selectedFunction.getSignalsUsedNumber() > 0) inputsSignalsTabItem.setControl(new InputsComposite(inputsSignalsTabItem.getParent(), SWT.NONE, InputsOutputsKinds.SIGNAL));
		if(selectedFunction.getMarkersUsedNumber() > 0) inputsMarkersTabItem.setControl(new InputsComposite(inputsMarkersTabItem.getParent(), SWT.NONE,  InputsOutputsKinds.MARKER));
		if(selectedFunction.getFieldsUsedNumber() > 0) inputsFieldsTabItem.setControl(new InputsComposite(inputsFieldsTabItem.getParent(), SWT.NONE,  InputsOutputsKinds.FIELD));
		if((selectedFunction.getSignalsCreatedNumber() > 0) || (selectedFunction.getMarkersCreatedNumber() > 0) || (selectedFunction.getFieldsCreatedNumber() > 0)) outputsTabItem.setControl(new OutputsComposite(outputsTabItem.getParent(),SWT.NONE));
		if((selectedFunction.getParametersCount() > 0))	parametersTabItem.setControl(new ParametersComposite(parametersTabItem.getParent(), SWT.NONE));
	}

	public Processing getProcessing() {
		return processing;
	}

	public void update(int message, IResource[] resources) {
		if(message == IResourceObserver.RENAMED) {
			if(processing == resources[0] || processing.hasParent(resources[0])) {
				setText(processing.getNameWithoutExtension());
				setToolTipText(processing.getLocalPath());
			} 
			setSelectedFunction(selectedFunction);
		}
		if(message == IResourceObserver.DELETED) {
			for (int i = 0; i < resources.length; i++) {
				if(processing == resources[i] || processing.hasParent(resources[i])) {
					dispose();
					break;
				}
			}
		}
		if(message == IResourceObserver.REFACTORED) {
			setSelectedFunction(selectedFunction);
		}
	}
	
	protected void disposeToolBar() {
		if(toolBarManager != null) {
			toolBarManager.dispose();
			toolBarManager = null;
		}
	}

	protected ToolBar getToolBar() {
		toolBarManager = new ToolBarManager(SWT.FLAT | SWT.WRAP);
		toolBarManager.add(new RunProcessAction());
		toolBarManager.add(new CheckProcessAction());
		toolBarManager.add(new Separator());
		toolBarManager.add(new RefactorAction());
		toolBarManager.add(new Separator());
		toolBarManager.add(AnalyseApplicationWindow.clearActiveEditorAction);
		ToolBar toolBar = toolBarManager.createControl(getParent());
		toolBar.addFocusListener((FocusListener) getParent());
		return toolBar;
	}

	public void clearProcessing() {
		processing.clear();
		usedFunctionsTableViewer.refresh();
		setSelectedFunction(null);
	}
	
	public enum InputsOutputsKinds {
		SIGNAL, MARKER, FIELD
	}
	
	private class InputsComposite extends Composite {
		
		private InputsOutputsKinds inputsKind;
		private List<ListViewer> listViewers = new ArrayList<ListViewer>(0);
		private int nbInputs;
		private int nbUplets;

		public InputsComposite(Composite parent, int style, InputsOutputsKinds inputsKind) {
			super(parent, style);
			this.inputsKind = inputsKind;
			formToolkit.adapt(this,true,true);
			nbInputs = 0;
			nbUplets = 0;
			switch (inputsKind) {
				case SIGNAL:
					nbInputs = selectedFunction.getSignalsUsedNumber();
					nbUplets = selectedFunction.getSignalsNbUplets();
					break;
				case MARKER:
					nbInputs = selectedFunction.getMarkersUsedNumber();
					nbUplets = selectedFunction.getMarkersNbUplets();
					break;
				case FIELD:
					nbInputs = selectedFunction.getFieldsUsedNumber();
					nbUplets = selectedFunction.getFieldsNbUplets();
					break;
				default:
					break;
			}
			setLayout(new GridLayout(nbInputs,true));
			for (int i = 0; i < nbInputs; i++) formToolkit.createLabel(this, Messages.getString("ProcessEditor.InputLabelTitle") + (i+1)).setLayoutData(new GridData(GridData.CENTER,GridData.CENTER,true,false)); //$NON-NLS-1$
			for (int i = 0; i < nbInputs; i++) {
				ListViewer listViewer = new ListViewer(this, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
				listViewers.add(listViewer);
				listViewer.getList().setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,true,1,1));		
				formToolkit.adapt(listViewer.getList(), true, true);
				listViewer.getList().addFocusListener((FocusListener) ProcessingEditor.this.getParent());
				for (int j = 0; j < nbUplets; j++) {
					String inputsString = ""; //$NON-NLS-1$
					switch (inputsKind) {
						case SIGNAL:
							inputsString = selectedFunction.getSignalsNamesList(j);
							break;
						case MARKER:
							inputsString = selectedFunction.getMarkersNamesList(j);
							break;
						case FIELD:
							inputsString = selectedFunction.getFieldsNamesList(j);
							break;
						default:
							break;
					}
					
					if(!inputsString.equals("")) {
						String[] splitted = inputsString.split(",");
						if(splitted.length > i) listViewer.add(inputsString.split(",")[i]); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
			for (int i = 0; i < nbInputs; i++) {
				Composite buttonsContainer = formToolkit.createComposite(this);
				buttonsContainer.setLayout(new GridLayout(5,false));
				buttonsContainer.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
				Button removeButton = formToolkit.createButton(buttonsContainer, "...", SWT.PUSH); //$NON-NLS-1$
				removeButton.setData(i);
				removeButton.setText(Messages.getString("ProcessEditor.RemoveInputButtonTitle")); //$NON-NLS-1$
				removeButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));	
				removeButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						removeInputHandler(e);
					}
				});
				Button addButton = formToolkit.createButton(buttonsContainer, "...", SWT.PUSH); //$NON-NLS-1$
				addButton.setData(i);
				addButton.setText(Messages.getString("ProcessEditor.AddInputButtonTitle")); //$NON-NLS-1$
				addButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
				addButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						addInputHandler(e);
					}
				});
				Button selectAllButton = formToolkit.createButton(buttonsContainer, "...", SWT.PUSH); //$NON-NLS-1$
				selectAllButton.setData(i);
				selectAllButton.setText(Messages.getString("ProcessEditor.SelectAllInputButtonTitle")); //$NON-NLS-1$
				selectAllButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
				selectAllButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						int inputNumber = (Integer)e.widget.getData();
						ListViewer listViewer = listViewers.get(inputNumber);
						listViewer.getList().setFocus();
						listViewer.getList().selectAll();
					}
				});
				Button moveChannelUpButton = formToolkit.createButton(buttonsContainer, null, SWT.PUSH); //$NON-NLS-1$
				moveChannelUpButton.setData(i);
				moveChannelUpButton.setImage(ImagesUtils.getImage(IImagesKeys.MOVE_FUNCTION_UP));
				moveChannelUpButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
				moveChannelUpButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						int inputNumber = (Integer)e.widget.getData();
						ListViewer listViewer = listViewers.get(inputNumber);
						int selectionIndex = listViewer.getList().getSelectionIndex();						
						if(selectionIndex > 0) {
							String[] items = listViewer.getList().getItems();
							String saveItem = items[selectionIndex-1];
							items[selectionIndex-1] = items[selectionIndex];
							items[selectionIndex] = saveItem;
							listViewer.getList().setItems(items);
							listViewer.getList().setSelection(selectionIndex-1);
							updateChannelsList();
						}
					}
				});
				Button moveChannelDownButton = formToolkit.createButton(buttonsContainer, null, SWT.PUSH); //$NON-NLS-1$
				moveChannelDownButton.setData(i);
				moveChannelDownButton.setImage(ImagesUtils.getImage(IImagesKeys.MOVE_FUNCTION_DOWN));
				moveChannelDownButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
				moveChannelDownButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						int inputNumber = (Integer)e.widget.getData();
						ListViewer listViewer = listViewers.get(inputNumber);
						int selectionIndex = listViewer.getList().getSelectionIndex();						
						if(selectionIndex < listViewer.getList().getItemCount() - 1) {
							String[] items = listViewer.getList().getItems();
							String saveItem = items[selectionIndex+1];
							items[selectionIndex+1] = items[selectionIndex];
							items[selectionIndex] = saveItem;
							listViewer.getList().setItems(items);
							listViewer.getList().setSelection(selectionIndex+1);
							updateChannelsList();
						}
					}
				});
			}
		}
		
		private void updateChannelsList() {
			int nbChannelsMax = 0;
			int nbChannelsMin = Integer.MAX_VALUE;
			for (int i = 0; i < nbInputs; i++) {
				ListViewer listViewer2 = listViewers.get(i);
				nbChannelsMax = (nbChannelsMax < listViewer2.getList().getItemCount()) ? listViewer2.getList().getItemCount() : nbChannelsMax;
				nbChannelsMin = (nbChannelsMin > listViewer2.getList().getItemCount()) ? listViewer2.getList().getItemCount() : nbChannelsMin;
			}
			
			String[] channelsNamesList = new String[nbChannelsMax];
			
			for (int i = 0; i < nbChannelsMax; i++) {
				String upletString = ""; //$NON-NLS-1$
				for (int j = 0; j < nbInputs; j++) {
					org.eclipse.swt.widgets.List list = listViewers.get(j).getList();
					if(list.getItemCount() > i)	upletString = upletString + list.getItem(i) + ","; //$NON-NLS-1$
					else upletString = upletString + ","; //$NON-NLS-1$
				}
				upletString = upletString.replaceAll(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
				channelsNamesList[i] = upletString;
			}
			switch (inputsKind) {
				case SIGNAL:
					selectedFunction.setSignalsNamesList(channelsNamesList);
					break;
				case MARKER:
					selectedFunction.setMarkersNamesList(channelsNamesList);
					break;
				case FIELD:
					selectedFunction.setFieldsNameslist(channelsNamesList);
					break;
				default:
					break;
			}
		}
		
		private void removeInputHandler(SelectionEvent e) {
			int inputNumber = (Integer)e.widget.getData();
			ListViewer listViewer = listViewers.get(inputNumber);
			listViewer.getList().remove(listViewer.getList().getSelectionIndices());
			updateChannelsList();
		}
		
		private void addInputHandler(SelectionEvent e) {
			int inputNumber = (Integer)e.widget.getData();
			InputsSelectionDialog inputsSelectionDialog = new InputsSelectionDialog(null, processing.getDataProcessing(), selectedFunction, inputNumber, inputsKind);
			ListViewer listViewer = listViewers.get(inputNumber);
			if(inputsSelectionDialog.open() == Window.OK) {
				String[] selectedChannels = inputsSelectionDialog.getSelectedChannels();
				for (int i = 0; i < selectedChannels.length; i++) {
					listViewer.getList().add(selectedChannels[i]);
				}
				updateChannelsList();
			}
		}
		

//		protected void updateChannelsNames() {
//			for (int i = 0; i < nbInputs; i++) {
//				ListViewer listViewer = listViewers.get(i);
//				listViewer.getList().removeAll();
//				for (int j = 0; j < nbUplets; j++) {
//					String inputsString = ""; //$NON-NLS-1$
//					switch (inputsKind) {
//						case SIGNAL:
//							inputsString = function.getSignalsNamesList(j);
//							break;
//						case MARKER:
//							inputsString = function.getMarkersNamesList(j);
//							break;
//						case FIELD:
//							inputsString = function.getFieldsNamesList(j);
//							break;
//						default:
//							break;
//					}
//					
//					if(!inputsString.equals("")) listViewer.add(inputsString.split(",")[i]); //$NON-NLS-1$ //$NON-NLS-2$
//				}
//			}
//		}
	}
	
	private class OutputsComposite extends Composite implements ModifyListener {
		
		private List<Text> textListSignals = new ArrayList<Text>(0);
		private List<Text> textListMarkers = new ArrayList<Text>(0);
		private List<Text> textListFields = new ArrayList<Text>(0);
		
		private void populate(String valuesUplet, InputsOutputsKinds outputsKinds, int nbChannels) {
			String[] valuesUpletSplitted = valuesUplet.split(":");  //$NON-NLS-1$
			for (int i = 0; i < nbChannels; i++) {
				switch (outputsKinds) {
					case SIGNAL:
						formToolkit.createLabel(this, Messages.getString("ProcessEditor.OutputSignalLabelTitle") + (i+1) + Messages.getString("ProcessEditor.OutputSignalLabelTitleEnd")).setLayoutData(new GridData(GridData.END,GridData.CENTER,false,false)); //$NON-NLS-1$ //$NON-NLS-2$
						break;
					case MARKER:
						formToolkit.createLabel(this, Messages.getString("ProcessEditor.OutputMarkerLabelTitle") + (i+1) + Messages.getString("ProcessEditor.OutputMarkerLabelTitleEnd")).setLayoutData(new GridData(GridData.END,GridData.CENTER,false,false)); //$NON-NLS-1$ //$NON-NLS-2$
						break;
					case FIELD:
						formToolkit.createLabel(this, Messages.getString("ProcessEditor.OutputFieldLabelTitle") + (i+1) + Messages.getString("ProcessEditor.OutputFieldLabelTitleEnd")).setLayoutData(new GridData(GridData.END,GridData.CENTER,false,false)); //$NON-NLS-1$ //$NON-NLS-2$
						break;
					default:
						break;
				}
				Text text = formToolkit.createText(this, "", SWT.BORDER); //$NON-NLS-1$
				text.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
				text.setData(OUTPUT_KIND,outputsKinds);
				text.addFocusListener((FocusListener) ProcessingEditor.this.getParent());
				String value= ""; //$NON-NLS-1$
				for (int j = 0; j < valuesUpletSplitted.length; j++) {
					if(valuesUpletSplitted[j].split(",").length > i) //$NON-NLS-1$
					value  = value + valuesUpletSplitted[j].split(",")[i] + ":"; //$NON-NLS-1$ //$NON-NLS-2$
					else value  = value + ":"; //$NON-NLS-1$
				}
				value = value.replaceAll(":$", ""); //$NON-NLS-1$ //$NON-NLS-2$
				text.setText(value);
				text.addModifyListener(this);
				switch (outputsKinds) {
					case SIGNAL:
						textListSignals.add(text);
						break;
					case MARKER:
						textListMarkers.add(text);
						break;
					case FIELD:
						textListFields.add(text);
						break;
					default:
						break;
				}
				
			}
		}
		
		public OutputsComposite(Composite parent, int style) {
			super(parent, style);
			formToolkit.adapt(this,true,true);
			setLayout(new GridLayout(2,false));
			populate(selectedFunction.getNewSignalsNamesSuffix(),InputsOutputsKinds.SIGNAL,selectedFunction.getSignalsCreatedNumber());
			populate(selectedFunction.getNewMarkersGroupLabels(),InputsOutputsKinds.MARKER,selectedFunction.getMarkersCreatedNumber());
			populate(selectedFunction.getNewFieldsNamesList(),InputsOutputsKinds.FIELD,selectedFunction.getFieldsCreatedNumber());
		}
		
		private void updateChannel(InputsOutputsKinds outputsKinds) {
			int nbChannelsMax = 0;
			int nbChannelsMin = Integer.MAX_VALUE;
			int nbCreatedChannels = 0;
			List<Text> textList = null;
//			String outputKey = ""; //$NON-NLS-1$
//			String outputKey2 = ""; //$NON-NLS-1$
//			String message = ""; //$NON-NLS-1$
//			String message2 = ""; //$NON-NLS-1$
			switch (outputsKinds) {
				case SIGNAL:
					nbCreatedChannels = selectedFunction.getSignalsCreatedNumber();
					textList = textListSignals;
//					outputKey = "OUTPUTS_SIGNALS_ERROR"; //$NON-NLS-1$
//					message = Messages.getString("ProcessEditor.ErrorMessageOutputSignalsUplets"); //$NON-NLS-1$
//					outputKey2 = "OUTPUTS_SIGNALS_WARNING"; //$NON-NLS-1$
//					message2 = Messages.getString("ProcessEditor.ErrorMessageOutputMarkersUplets"); //$NON-NLS-1$
					break;
				case MARKER:
					nbCreatedChannels = selectedFunction.getMarkersCreatedNumber();
					textList = textListMarkers;
//					outputKey = "OUTPUTS_MARKERS_ERROR"; //$NON-NLS-1$
//					message = Messages.getString("ProcessEditor.ErrorMessageOutputFieldsUplets"); //$NON-NLS-1$
//					outputKey2 = "OUTPUTS_MARKERS_WARNING"; //$NON-NLS-1$
//					message2 = Messages.getString("ProcessEditor.WarningOutputSignalsEmpty"); //$NON-NLS-1$
					break;
				case FIELD:
					nbCreatedChannels = selectedFunction.getFieldsCreatedNumber();
					textList = textListFields;
//					outputKey = "OUTPUTS_FIELDS_ERROR"; //$NON-NLS-1$
//					message = Messages.getString("ProcessEditor.WarningOutputMarkersEmpty"); //$NON-NLS-1$
//					outputKey2 = "OUTPUTS_FIELDS_WARNING"; //$NON-NLS-1$
//					message2 = Messages.getString("ProcessEditor.WarningOutputFieldsEmpty"); //$NON-NLS-1$
					break;
				default:
					break;
			}
			for (int i = 0; i < nbCreatedChannels; i++) {
				//outputsUplets.add(textListSignals.get(i).getText());
				String textListString = textList.get(i).getText();
//				if(textListString.equals("")) { //$NON-NLS-1$
//					messageManager.addMessage(outputKey2,	message2, null, IMessageProvider.WARNING);
//					return;
//				} else messageManager.removeMessage(outputKey2);
				int lastIndex = 0;
//				int nbUpletsExpected = 0;
				while(true){
					lastIndex = textListString.indexOf(":",lastIndex); //$NON-NLS-1$
//				    if( lastIndex != -1) nbUpletsExpected ++;
				    if(lastIndex == -1) break;
				    lastIndex++;
				}
				int nbUplets = textListString.split(":").length; //$NON-NLS-1$
//				if(nbUplets != nbUpletsExpected + 1)  {
//					messageManager.addMessage(outputKey, message, null, IMessageProvider.ERROR);
//					return;
//				} else messageManager.removeMessage(outputKey);
				nbChannelsMax = (nbChannelsMax < nbUplets) ? nbUplets : nbChannelsMax;
				nbChannelsMin = (nbChannelsMin > nbUplets) ? nbUplets : nbChannelsMin;
			}
//			if(nbChannelsMax != nbChannelsMin) {
//				 messageManager.addMessage(outputKey, message, null, IMessageProvider.ERROR);
//				 return;
//			} else messageManager.removeMessage(outputKey);
			
			List<String> outputsUplets = new ArrayList<String>(0);
			List<String[]> outputsUpletsSplitted = new ArrayList<String[]>(0);
			String value = ""; //$NON-NLS-1$
			for (int i = 0; i < nbCreatedChannels; i++) {
				outputsUplets.add(textList.get(i).getText());
			}
			for (int i = 0; i < nbCreatedChannels; i++) {
				outputsUpletsSplitted.add(outputsUplets.get(i).split(":")); //$NON-NLS-1$
			}
			for (int j = 0; j < nbChannelsMax; j++) {
				for (int i = 0; i < nbCreatedChannels; i++) {
					value = value + outputsUpletsSplitted.get(i)[j] + ","; //$NON-NLS-1$
				}
				value = value.replaceAll(",$", ":"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			value = value.replaceAll(":$", ""); //$NON-NLS-1$ //$NON-NLS-2$
			switch (outputsKinds) {
				case SIGNAL:
					selectedFunction.setNewSignalsNamesSuffix(value);
					break;
				case MARKER:
					selectedFunction.setNewMarkersGroupLabels(value);
					break;
				case FIELD:
					selectedFunction.setNewFieldsNamesList(value);
					break;
				default:
					break;
			}
		}
	
		public void modifyText(ModifyEvent e) {
			updateChannel((InputsOutputsKinds) e.widget.getData(OUTPUT_KIND));
		}
	}
	
	private class ParametersComposite extends Composite implements ModifyListener, SelectionListener {

		
		public ParametersComposite(Composite parent, int style) {
			super(parent, style);
			formToolkit.adapt(this,true,true);
			setLayout(new GridLayout(2,false));
			String[] choices;
			for (int i = 0; i < selectedFunction.getParametersCount(); i++) {
				switch (selectedFunction.getParameterType(i)) {
					case Function.GUI_TYPE_TEXT:
						Label labelText = formToolkit.createLabel(this, selectedFunction.getParameterLabel(i) + " :"); //$NON-NLS-1$
						labelText.setLayoutData(new GridData(GridData.END,GridData.CENTER,false,false));
						labelText.setToolTipText(selectedFunction.getToolTip(i));
						Text text =	formToolkit.createText(this ,(String)selectedFunction.getParameterValue(i), SWT.BORDER);
						text.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,false));
						text.setToolTipText(selectedFunction.getToolTip(i));
						text.setData(PARAMETER_NUMBER, i);
						text.setData(String.valueOf(i));				
						text.addModifyListener(this);
						text.addFocusListener((FocusListener) ProcessingEditor.this.getParent());
						//text.addModifyListener(function);					
						break;
					case Function.GUI_TYPE_COMBOBOX:		
						Label labelCombo = formToolkit.createLabel(this, selectedFunction.getParameterLabel(i) + " :");					 //$NON-NLS-1$
						labelCombo.setLayoutData(new GridData(GridData.END,GridData.CENTER,false,false));
						labelCombo.setToolTipText(selectedFunction.getToolTip(i));
						CCombo combo = new CCombo(this, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);		
						combo.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,false));
						choices = selectedFunction.getAvailablesValues(i);
						for (int j = 0; j < choices.length; j++) combo.add(choices[j]);
						combo.setText((String)selectedFunction.getParameterValue(i));
						combo.setData(FormToolkit.KEY_DRAW_BORDER,FormToolkit.TEXT_BORDER);
						combo.setData(PARAMETER_NUMBER, i);
						combo.addModifyListener(this);
						combo.setToolTipText(selectedFunction.getToolTip(i));
						//combo.addModifyListener(function);
						formToolkit.adapt(combo);
						combo.addFocusListener((FocusListener) ProcessingEditor.this.getParent());
						//formToolkit.paintBordersFor(this);
						break;
					case Function.GUI_TYPE_CHECK_BUTTON:
						Button button = formToolkit.createButton(this, selectedFunction.getParameterLabel(i), SWT.CHECK);	
						button.setToolTipText(selectedFunction.getToolTip(i));				
						button.setData(PARAMETER_NUMBER, i);
						button.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,false,2,1));	
						button.setSelection((Boolean.valueOf((String)selectedFunction.getParameterValue(i))).booleanValue());
						button.addSelectionListener(this);
						button.addFocusListener((FocusListener) ProcessingEditor.this.getParent());
						//button.addSelectionListener(function);
						break;
					case Function.GUI_TYPE_LIST:
						Label labelList = formToolkit.createLabel(this, selectedFunction.getParameterLabel(i) + " :");	 //$NON-NLS-1$
						labelList.setLayoutData(new GridData(GridData.BEGINNING,GridData.FILL,true,false,2,1));
						labelList.setToolTipText(selectedFunction.getToolTip(i));
						//ListViewer listViewer = new ListViewer(this, SWT.BORDER | SWT.MULTI);
						org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(this, SWT.BORDER | SWT.MULTI);
						formToolkit.adapt(list, true, true);
						list.setData(PARAMETER_NUMBER, i);
						list.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,true,2,1));
						choices = selectedFunction.getAvailablesValues(i);
						for (int j = 0; j < choices.length; j++) list.add(choices[j]);
						String selectionString  = (String)selectedFunction.getParameterValue(i);
						String[] selection = selectionString.split(","); //$NON-NLS-1$
						list.setSelection(selection);
						list.addSelectionListener(this);
						list.addFocusListener((FocusListener) ProcessingEditor.this.getParent());
						//list.addSelectionListener(function);
						break;
					default:
						break;
				}
				
//				boolean addSelectionButton = selectedFunction.getSignalsAvailableBool(i) || selectedFunction.getMarkersAvailableBool(i) || selectedFunction.getFieldAvailableBool(i); 
//				if(addSelectionButton) {
//					//TODO
//				}
				
			}
		}

		public void modifyText(ModifyEvent e) {
			int parameterNumber = (Integer) e.widget.getData(PARAMETER_NUMBER);
			String value = ""; //$NON-NLS-1$
			if(e.widget instanceof Text) value = ((Text)e.widget).getText();
			if(e.widget instanceof CCombo) value = ((CCombo)e.widget).getText();
			//Check with regexp
//			if(!selectedFunction.getParametersRegExp(parameterNumber).equals("")) //$NON-NLS-1$
//				if(value.matches(selectedFunction.getParametersRegExp(parameterNumber))) {
					selectedFunction.setParameterDefaultValue(value, parameterNumber);
//					messageManager.removeMessage(e.widget);
//				} else {
//					RegularExpressions regExp = RegularExpressions.readRegularExpressions(); 
//					String message = Messages.getString("ProcessEditor.ErrorInvalidParameterExpression") + selectedFunction.getParameterLabel(parameterNumber) + Messages.getString("ProcessEditor.ErrorInvalidParameterExpressionb") + regExp.getRegularExpressionLabel(selectedFunction.getParametersRegExp(parameterNumber)) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//					messageManager.addMessage(e.widget, message, null, IMessageProvider.ERROR);
//				}
		}

		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {
			int parameterNumber = (Integer) e.widget.getData(PARAMETER_NUMBER);
			if(e.widget instanceof Button) {
				//No regExp to check
				boolean value = ((Button)e.widget).getSelection();
				selectedFunction.setParameterDefaultValue((Boolean.valueOf(value)).toString(), parameterNumber);
			}
			if(e.widget instanceof org.eclipse.swt.widgets.List) {
				//No regExp to check
				String[] values =((org.eclipse.swt.widgets.List)e.widget).getSelection();
				String valuesString = ""; //$NON-NLS-1$
				for (int i = 0; i < values.length; i++) {
					valuesString = valuesString + values[i] + ","; //$NON-NLS-1$
				}
				valuesString = valuesString.replaceAll(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
				selectedFunction.setParameterDefaultValue( valuesString, parameterNumber);
			}
		}
	
	}
	
	private class UsedFunctionsTableViewerLabelProvider implements ILabelProvider {
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
			return ((Function)element).getGUIFunctionName();
		}
		public Image getImage(Object element) {
			return ImagesUtils.getImage(IImagesKeys.FUNCTION_ICON);
		}
	}
	
	private class UsedFunctionsTableViewerLabelDecorator implements ILabelDecorator {
		public Image decorateImage(Image image, Object element) {
			if(element instanceof Function) {
				Function function = (Function)element;
				if(function.hasErrors() | function.hasWarnings())
				return (new OverlayImage(image,function.hasErrors(),function.hasWarnings() )).createImage();
			}
			return null;
		}
		public String decorateText(String text, Object element) {
			return null;
		}
		public void addListener(ILabelProviderListener listener) {
		}
		public void dispose() {
		}
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		public void removeListener(ILabelProviderListener listener) {
		}
	}
	
	private class OverlayImage extends CompositeImageDescriptor {
		private Image baseImage;
		private boolean error;
		private boolean warning;
		public OverlayImage(Image baseImage, boolean error, boolean warning) {
			this.baseImage = baseImage;
			this.error = error;
			this.warning = warning;
		}
		@SuppressWarnings("deprecation")
		protected void drawCompositeImage(int width, int height) {
			// To draw a composite image, the base image should be 
			// drawn first (first layer) and then the overlay image 
			// (second layer) 
			// Draw the base image using the base image's image data 
			drawImage(baseImage.getImageData(), 0, 0);			
			// Method to create the overlay image data 
			// Get the image data from the Image store or by other means ImageData overlayImageData = overlayImageDescriptor.getImageData();
			// Overlaying the icon in the top left corner i.e. x and y 
			// coordinates are both zero 
			if(error)
			drawImage(ImagesUtils.getImageDescriptor(IImagesKeys.ERROR_ICON_DECORATOR).getImageData(), 0, 0);
			if(warning)
			drawImage(ImagesUtils.getImageDescriptor(IImagesKeys.WARNING_ICON_DECORATOR).getImageData(), 5, 6);
		}
		protected Point getSize() {
			return  new Point(baseImage.getBounds().width , baseImage.getBounds().height );
		}		
	}
	
}
