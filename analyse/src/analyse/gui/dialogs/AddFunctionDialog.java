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
package analyse.gui.dialogs;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.w3c.dom.Node;

import analyse.model.Function;
import analyse.model.IResource;
import analyse.model.Processing;
import analyse.preferences.LibraryPreferences;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

public class AddFunctionDialog extends TitleAreaDialog {
	
	public static ArrayList<Node> selectedNodeFunctions = new ArrayList<Node>(0);

	private class FunctionViewerFilter extends ViewerFilter {
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			Node currentNode = (Node)element;	
			boolean isFunction =  currentNode.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute) != null;
			if(isFunction) {
				String match = filterText.getText();
				if(!match.equals("")) return currentNode.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute).getNodeValue().contains(filterText.getText());
			}
			return true;
		}
	}
	
	private IResource resource;
	private TreeViewer libraryTreeViewer;
	private Text filterText;
	private ViewerFilter[] functionViewerFilter;

	public AddFunctionDialog(Shell parentShell, IResource resource) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.resource = resource;
		functionViewerFilter = new ViewerFilter[]{new FunctionViewerFilter()};
		selectedNodeFunctions.clear();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("AddFunctionDialog.ShellTitle"));
		newShell.setImage(ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON));
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents =  super.createContents(parent);		
		setTitle(Messages.getString("AddFunctionDialog.Title"));
		setMessage(Messages.getString("AddFunctionDialog.Text") + resource.getLocalPath());
		setTitleImage(ImagesUtils.getImage(IImagesKeys.REFACTOR_BANNER));
		return contents;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea =  (Composite) super.createDialogArea(parent);

		final Composite container = new Composite(dialogArea, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		container.setLayout(new GridLayout(2,false));
		
		Composite headContainer =  new Composite(container, SWT.NONE);
		headContainer.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,2,1));
		headContainer.setLayout(new GridLayout(2,false));
		
		Label filterLabel = new Label(headContainer, SWT.NONE);
		filterLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		filterLabel.setText(Messages.getString("AddFunctionDialog.FilterLabelTitle"));
		filterText = new Text(headContainer, SWT.SEARCH | SWT.CANCEL);
		filterText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		libraryTreeViewer = new TreeViewer(container,SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		libraryTreeViewer.getTree().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		libraryTreeViewer.setContentProvider(LibraryPreferences.getContentProvider());
		libraryTreeViewer.setLabelProvider(LibraryPreferences.getLabelProvider());
		libraryTreeViewer.setSorter(LibraryPreferences.libraryViewerSorter);
		libraryTreeViewer.setFilters(functionViewerFilter);
		libraryTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		//libraryTreeViewer.setSorter(LibraryComposite.getLibraryTreeViewerSorter());//new LibraryTreeViewerSorter());
		libraryTreeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		libraryTreeViewer.setInput(LibraryPreferences.getLibrary());
		filterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				libraryTreeViewer.setFilters(functionViewerFilter);
				libraryTreeViewer.expandAll();
			}
		});
		
		Composite buttonsContainer = new Composite(container, SWT.NONE);
		buttonsContainer.setLayout(new GridLayout(1,false));
		buttonsContainer.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,true));
		
		Button expandButton = new Button(buttonsContainer, SWT.PUSH | SWT.FLAT);
		expandButton.setImage(ImagesUtils.getImage(IImagesKeys.EXPAND_ALL_ICON));
		expandButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		expandButton.setToolTipText(Messages.getString("ExpandAllAction"));
		expandButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				libraryTreeViewer.expandAll();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Button collapseButton = new Button(buttonsContainer, SWT.PUSH | SWT.FLAT);
		collapseButton.setImage(ImagesUtils.getImage(IImagesKeys.COLLAPSE_ALL_ICON));
		collapseButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		collapseButton.setToolTipText(Messages.getString("CollapseAllAction"));
		collapseButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				libraryTreeViewer.collapseAll();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Button switchLibraryButton = new Button(buttonsContainer, SWT.PUSH | SWT.FLAT);
		switchLibraryButton.setImage(ImagesUtils.getImage(IImagesKeys.SWITCH_LIBRARY_ICON));
		switchLibraryButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		switchLibraryButton.setToolTipText(Messages.getString("SwitchLibrary"));
		switchLibraryButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if(LibraryPreferences.doSwitch()) {
					libraryTreeViewer.setInput(LibraryPreferences.getLibrary());
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		FormToolkit toolkit = new FormToolkit(Display.getDefault());
		final FormText descriptionFormText = toolkit.createFormText(container, false);
		descriptionFormText.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,false,2,1));
		descriptionFormText.setText("<form>" + Messages.getString("AddFunctionDialog.FunctionDescription") + "</form>", true, true);
		descriptionFormText.marginHeight = 5;
		descriptionFormText.marginWidth = 5;
		descriptionFormText.setBackground(container.getBackground());
		
		libraryTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				if(selection.getFirstElement() instanceof Node) {
					Node node = (Node)selection.getFirstElement(); 
					boolean isFunction =  node.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute) != null;
					if(isFunction) {
						Function function = new Function(node.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute).getNodeValue());
						function.initializeFunction();
						descriptionFormText.setText("<form>" + function.getLongDescription() + "</form>", true, true);
						container.layout();
					} else descriptionFormText.setText("<form>" + Messages.getString("AddFunctionDialog.FunctionDescription") + "</form>", true, true);
				}
			}
		});
		
		return dialogArea;
	}
	
	@Override
	protected void okPressed() {
		if(resource instanceof Processing) {
			StructuredSelection selection = (StructuredSelection)libraryTreeViewer.getSelection();
			Object[] selectionObjects = selection.toArray();
			for (int i = 0; i < selectionObjects.length; i++) {
				Node node = (Node)selectionObjects[i]; 
				boolean isFunction =  node.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute) != null;
				if(isFunction) selectedNodeFunctions.add(node);
			}
		}
		super.okPressed();
	}

}
