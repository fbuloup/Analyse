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

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import mathengine.MathEngineFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import analyse.AnalyseApplication;
import analyse.Log;
import analyse.preferences.AnalysePreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public final class ConsolesView extends View {
	
	private static StyleRange messageStyle;
	private static StyleRange warningMessageStyle;
	private static StyleRange errorMessageStyle;
	
	public final static int SHOW_MESSAGES_CONSOLE = 1;
	public final static int SHOW_MATH_CONSOLE = 2;
	public final static int SHOW_SHARING_CONSOLE = 3;
	public final static int SHOW_ALL_CONSOLES = 4;
	
	private static CTabItem messagesConsole;
	private static CTabItem mathConsole;
	private static CTabItem sharingConsole;

	private ArrayList<String> cmdsArrayList = new ArrayList<String>(0);
	private int cmdIndex = 1;
	private ListViewer cmdsListViewer;
	private ActionContributionItem clearActiveConsole;
	private MenuManager popupMenuManager;
	private static StyledText mathStyledText;
	private static StyledText messagesStyledText;
	
	private class ClearActiveConsole extends Action {
		public ClearActiveConsole() {
			setText(Messages.getString("ClearMessagesAction.Title")); 
			setToolTipText(Messages.getString("ClearMessagesAction.Title"));
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.CLEAR_MESSAGES_ICON));
		}
		@Override
		public void run() {
			if(ConsolesView.this.getSelection() == messagesConsole) messagesStyledText.setText("");
			if(ConsolesView.this.getSelection() == mathConsole) mathStyledText.setText("");
			if(ConsolesView.this.getSelection() == sharingConsole) sharingContainer.clearMessages();
		}
	}
	
	private class CopyActiveConsole extends Action {
		public CopyActiveConsole() {
			setText(Messages.getString("CopyAction.Title")); 
			setToolTipText(Messages.getString("ClearMessagesAction.Title"));
			setImageDescriptor(ImagesUtils.getImageDescriptor(IImagesKeys.COPY_ICON));
		}
		@Override
		public void run() {
			if(ConsolesView.this.getSelection() == messagesConsole) messagesStyledText.copy();
			if(ConsolesView.this.getSelection() == mathConsole) mathStyledText.copy();
		}
	}
	
	class ErrorsStream extends FilterOutputStream {
		public ErrorsStream(OutputStream aStream) {
			super(aStream);
		}
		
		public void write(byte b[]) throws IOException {
			String errorMessage = new String(b);
			if(!errorMessage.equals("\n")) logErrorMessage(errorMessage);
		}
		
		public void write(byte b[], int off, int len) throws IOException {
			String errorMessage = new String(b , off , len);
			if(!errorMessage.equals("\n")) logErrorMessage(errorMessage);
		}
	}
	
	PrintStream errorsPrintStream  = new PrintStream(new ErrorsStream(new ByteArrayOutputStream()));
	private SharingContainer sharingContainer;
	
	public ConsolesView(Composite parent, int style) {
		super(parent, style);
		initView(SHOW_ALL_CONSOLES);
		
		if(AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.REDIRECT_CONSOLE_MESSAGES)) {
		    System.setErr(errorsPrintStream);
		}
		
	}
	
	@Override
	protected void initView(int showMode) {
		
		messageStyle = new StyleRange();
		messageStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);		
		messageStyle.fontStyle = SWT.BOLD;
				
		warningMessageStyle = new StyleRange();
		warningMessageStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW);		
		warningMessageStyle.fontStyle = SWT.BOLD;
		
		errorMessageStyle = new StyleRange();
		errorMessageStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);		
		errorMessageStyle.fontStyle = SWT.BOLD;
		
		createToolBar();
		
		if((showMode == SHOW_MESSAGES_CONSOLE) || (showMode == SHOW_ALL_CONSOLES)) {
			boolean init = messagesConsole == null;
			if(!init) init = messagesConsole.isDisposed();
			if(init) {
				messagesConsole = createTabItem();
				messagesConsole.setText(Messages.getString("MessagesView.MessageViewTitle"));	
				messagesConsole.setImage(ImagesUtils.getImage(IImagesKeys.MESSAGES_VIEW_ICON));
				messagesStyledText = new StyledText(this, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
				messagesConsole.setControl(messagesStyledText);
				messagesStyledText.addFocusListener(this);
				messagesStyledText.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						messagesStyledText.setSelection(messagesStyledText.getText().length());
					}
				});
				
				messagesStyledText.addFocusListener(AnalyseApplicationWindow.copyAction);
				messagesConsole.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						messagesStyledText.dispose();
					}
				});
			}
		}
		
		
		if((showMode == SHOW_MATH_CONSOLE) || (showMode == SHOW_ALL_CONSOLES)) {
			boolean init = mathConsole == null;
			if(!init) init = mathConsole.isDisposed();
			if(init) {
				mathConsole = createTabItem();
				mathConsole.setText(Messages.getString("MessagesView.ConsoleViewTitle"));	
				mathConsole.setImage(ImagesUtils.getImage(IImagesKeys.CONSOLE_VIEW_ICON));
				
				final SashForm sashContainer = new SashForm(this,SWT.NONE | SWT.HORIZONTAL);
				
				mathConsole.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						sashContainer.dispose();
					}
				});
				
				Composite leftContainer = new Composite(sashContainer,SWT.BORDER);
				leftContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				leftContainer.setLayout(new GridLayout(2,false));
				GridLayout gridLayout = (GridLayout) leftContainer.getLayout();
				gridLayout.marginHeight = 0;
				gridLayout.marginWidth = 0;
				gridLayout.marginLeft = 5;
				gridLayout.marginTop = 5;;
				gridLayout.marginBottom = 5;
				
				Label label = new Label(leftContainer, SWT.NONE);
				label.setText(Messages.getString("MessagesView.CommandStringLabelTitle")); //$NON-NLS-1$
				label.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false,1,1));
				
				final Text commandText = new Text(leftContainer,SWT.SINGLE | SWT.BORDER);
				commandText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,1,1));
				commandText.addFocusListener(this);
				commandText.addFocusListener(AnalyseApplicationWindow.copyAction);
				commandText.addKeyListener(new KeyListener(){
					public void keyPressed(KeyEvent e) {
					}
					public void keyReleased(KeyEvent e) {
						if(e.keyCode == SWT.ARROW_DOWN) cmdIndex=(cmdIndex < cmdsArrayList.size())?cmdIndex+1:cmdIndex;	
						if(e.keyCode == SWT.ARROW_UP) cmdIndex=(cmdIndex > 0)?cmdIndex-1:0;
						if(e.keyCode == SWT.CR){
							final String cmd = commandText.getText();
							if(!cmd.equals("")) {
								if(cmdsArrayList.indexOf(cmd) == -1) {
									cmdsArrayList.add(cmd);
									cmdIndex = cmdsArrayList.size();
									cmdsListViewer.setInput(cmdsArrayList);
								}
								IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
									public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
										monitor.beginTask(Messages.getString("MessagesView.MessageViewSending"), IProgressMonitor.UNKNOWN);
										monitor.subTask(cmd);
										final String response = MathEngineFactory.getInstance().getMathEngine().sendCommand(cmd);
										Display.getDefault().syncExec(new Runnable() {
											public void run() {
												mathStyledText.append(response);
												cmdIndex = cmdsArrayList.size();
												cmdsListViewer.refresh();
											}
										});
										monitor.done();
									}
								};
								AnalyseApplication.getAnalyseApplicationWindow().run(true, true, false, false, runnableWithProgress);
							}
							commandText.setText("");
							commandText.setFocus();
						}

						if(e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_UP)
						if(cmdIndex < cmdsArrayList.size() && cmdIndex > -1) {
							commandText.setText(cmdsArrayList.get(cmdIndex));
							commandText.setSelection(cmdsArrayList.get(cmdIndex).length(), cmdsArrayList.get(cmdIndex).length());
						} else commandText.setText("");
						cmdsListViewer.refresh();
					}
				});
				
				mathStyledText = new StyledText(leftContainer, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
				mathStyledText.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
				mathStyledText.addFocusListener(this);
				mathStyledText.addFocusListener(AnalyseApplicationWindow.copyAction);
				mathStyledText.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						mathStyledText.setSelection(mathStyledText.getText().length());
					}
				});
				
				cmdsListViewer = new ListViewer(sashContainer, SWT.BORDER | SWT.MULTI);
				cmdsListViewer.getList().setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
				cmdsListViewer.setContentProvider(new IStructuredContentProvider() {
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					}
					public void dispose() {
					}
					public Object[] getElements(Object inputElement) {
						return (Object[]) cmdsArrayList.toArray(new String[cmdsArrayList.size()]);
					}
				});
				cmdsListViewer.setLabelProvider(new ILabelProvider() {
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
						String cmd = (String)element;
						if(cmdsArrayList.indexOf(cmd) == cmdIndex)
							 return ">> " + cmd;
						else return cmd;
					}
					public Image getImage(Object element) {
						return null;
					}
				});
				cmdsListViewer.setInput(cmdsArrayList);
				cmdsListViewer.getList().addMouseListener(new MouseAdapter() {
					public void mouseDown(MouseEvent e) {
						cmdIndex = -1;
						if(cmdsListViewer.getList().getSelectionIndices().length == 1) {
							cmdIndex = cmdsListViewer.getList().getSelectionIndex();
							commandText.setText(cmdsArrayList.get(cmdIndex));
							cmdsListViewer.refresh();
						}
					}
					public void mouseDoubleClick(MouseEvent e) {
						cmdIndex = cmdsListViewer.getList().getSelectionIndex();
						if(cmdIndex < cmdsArrayList.size() && cmdIndex > -1) {
							String cmd = cmdsArrayList.get(cmdIndex);
							commandText.setText("");
							String response = MathEngineFactory.getInstance().getMathEngine().sendCommand(cmd);
							Log.logMessage(Messages.getString("MessagesView.MessageViewSending") + " " + cmd);
							mathStyledText.append(response);
							cmdIndex = cmdsArrayList.size();
							cmdsListViewer.refresh();
						}
					}
				});
				cmdsListViewer.getList().addKeyListener(new KeyListener() {
					public void keyReleased(KeyEvent e) {
						if(e.keyCode == SWT.DEL) {
							commandText.setText("");
							int[] indexes = cmdsListViewer.getList().getSelectionIndices();
							for (int i = 0; i < indexes.length; i++) cmdsArrayList.remove(cmdsListViewer.getList().getItem(indexes[i]).replaceAll("^>> ", ""));
							cmdIndex = -1;
							cmdsListViewer.setInput(cmdsArrayList);
						}
					}
					public void keyPressed(KeyEvent e) {
					}
				});
				cmdsListViewer.getList().addFocusListener(this);
				
				sashContainer.setWeights(new int[]{80,20});
				
				mathConsole.setControl(sashContainer);
			}
		}
		
		if(showMode == SHOW_SHARING_CONSOLE) {
			boolean init = sharingConsole == null;
			if(!init) init = sharingConsole.isDisposed();
			if(init) {
				sharingConsole = createTabItem();
				sharingConsole.setText(Messages.getString("ExperimentsSharingView.Title"));	
				sharingConsole.setImage(ImagesUtils.getImage(IImagesKeys.SHARING_VIEW_ICON));
				
				sharingContainer = new SharingContainer(this, SWT.NONE);
//				addSelectionListener(sharingContainer);
				
				sharingConsole.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
//						removeSelectionListener(sharingContainer);
						sharingContainer.dispose();
					}
				});
				
				sharingConsole.setControl(sharingContainer);
			}
		}
		
		if((showMode == SHOW_MESSAGES_CONSOLE) || (showMode == SHOW_ALL_CONSOLES)) setSelection(messagesConsole);
		if(showMode == SHOW_MATH_CONSOLE) setSelection(mathConsole);
		if(showMode == SHOW_SHARING_CONSOLE) setSelection(sharingConsole);
		
		if(popupMenuManager == null) {
			popupMenuManager = new MenuManager("popupMenuManagerConsoles");
			popupMenuManager.add(new CopyActiveConsole());
			popupMenuManager.add(clearActiveConsole.getAction());
		}
		if(messagesStyledText != null)
			if(!messagesStyledText.isDisposed())
				messagesStyledText.setMenu(popupMenuManager.createContextMenu(messagesStyledText));
		if(mathStyledText != null)
			if(!mathStyledText.isDisposed())
				mathStyledText.setMenu(popupMenuManager.createContextMenu(mathStyledText));
		
	}
	

	private void logMessage(final String message, final StyleRange style) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if(!messagesStyledText.isDisposed() && message != null) {
					messagesStyledText.setCaretOffset(messagesStyledText.getText().length());								
					messagesStyledText.append(message + "\n");				
					style.start = messagesStyledText.getText().length() - message.length() - 1;
					style.length = message.length();
					messagesStyledText.setStyleRange(style);
				}
			}
		});
	}

	public void logMessage(String message) {
		logMessage(message, messageStyle);				
	}

	public void logWarningMessage(String warningMessage) {
		logMessage(warningMessage, warningMessageStyle);
	}
	
	public void logErrorMessage(String errorMessage) {
		logMessage(errorMessage, errorMessageStyle);
	}

	@Override
	protected void createToolBar() {
		if(clearActiveConsole == null) {
			clearActiveConsole= new ActionContributionItem((IAction)(new ClearActiveConsole()));
			ActionContributionItem startStopMathEngine = new ActionContributionItem(AnalyseApplicationWindow.startStopMathEngineAction);
			ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT | SWT.WRAP);
			toolBarManager.add(clearActiveConsole);
			toolBarManager.add(new Separator());
			toolBarManager.add(startStopMathEngine);
			setTopRight(toolBarManager.createControl(this), SWT.RIGHT);
			toolBarManager.getControl().addFocusListener(this);
		}
	}

	public void freeze() {
		if(mathConsole != null && !mathConsole.isDisposed()) mathConsole.getControl().setEnabled(false);
	}
	
	public void unFreeze() {
		if(mathConsole != null && !mathConsole.isDisposed()) mathConsole.getControl().setEnabled(true);
	}
		
	@Override
	public boolean setFocus() {
		if(getSelection() == sharingConsole) {
			sharingContainer.updateSelection(true);
		}
		return super.setFocus();
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		if(getSelection() == sharingConsole) {
			sharingContainer.updateSelection(true);
		}super.focusGained(e);
	}
	
	@Override
	public void focusLost(FocusEvent e) {
		if(getSelection() == sharingConsole) {
			sharingContainer.updateSelection(false);
		}
		super.focusLost(e);
	}
	
}
