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
package functioneditor.windows;

import java.util.Iterator;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import analyse.preferences.ILibraryObserver;
import analyse.preferences.LibraryPreferences;

public class LibraryComposite extends Composite implements ILibraryObserver, DragSourceListener/*, DropTargetListener*/ {

	private TreeViewer libraryTreeViewer;
	private TreeSelection treeSelectionDND;
	
	private class LibraryDragDrop extends ViewerDropAdapter {

		protected LibraryDragDrop(Viewer viewer) {
			super(viewer);
		}
		
		private boolean isChild(Node nodeToTest, Node node) {
			boolean child = false;
			NodeList childNodes = node.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node localNode = childNodes.item(i);
				child = child || isChild(nodeToTest, (Node) localNode);
			}
			return child || node.equals(nodeToTest);
		}
		
		private boolean isAChildOfSelection(Node nodeToTest) {
			boolean child = false;
			for (@SuppressWarnings("rawtypes")
			Iterator iterator = treeSelectionDND.iterator(); iterator.hasNext();) {
				Node node = (Node) iterator.next();
				child = child || isChild(nodeToTest,node);
			}
			return child;
		}

		@Override
		public boolean performDrop(Object data) {
			Node targetNode =  (Node) getCurrentTarget();
			for (@SuppressWarnings("rawtypes")
			Iterator iterator = treeSelectionDND.iterator(); iterator.hasNext();) {
				Node node = (Node) iterator.next();
				node.getParentNode().removeChild(node);
				targetNode.appendChild(node);
			}
			LibraryPreferences.notifyObservers(LibraryPreferences.getLibrary(),null);
			LibraryPreferences.save();
			return true;
			
		}

		@Override
		public boolean validateDrop(Object target, int operation, TransferData transferType) {
			if(target == null) return false;
			Node targetNode = (Node)target;
			boolean isChild = isAChildOfSelection(targetNode);
			boolean isEditable =  targetNode.getAttributes().getNamedItem(LibraryPreferences.editableAttribute).getNodeValue().equals(LibraryPreferences.trueAttributeValue);
			boolean valid = TextTransfer.getInstance().isSupportedType(transferType)  && isEditable && !isChild;
			return valid;
		}
	}

	@SuppressWarnings("deprecation")
	public LibraryComposite(Composite parent, int style) {
		super(parent, style);
		
		GridLayout gridLayout = new GridLayout(1,true);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);
		libraryTreeViewer = new TreeViewer(this,SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		libraryTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		libraryTreeViewer.setContentProvider(LibraryPreferences.getContentProvider());
		libraryTreeViewer.setLabelProvider(LibraryPreferences.getLabelProvider());
		libraryTreeViewer.setSorter(LibraryPreferences.libraryViewerSorter);//new LibraryTreeViewerSorter());
		libraryTreeViewer.setInput(LibraryPreferences.getLibrary());
		
		LibraryDragDrop viewerDropAdapter = new LibraryDragDrop(libraryTreeViewer);
		viewerDropAdapter.setExpandEnabled(true);
		viewerDropAdapter.setFeedbackEnabled(true);
		viewerDropAdapter.setScrollEnabled(true);
		viewerDropAdapter.setSelectionFeedbackEnabled(true);
		
		
		libraryTreeViewer.addDragSupport(DND.DROP_MOVE, new Transfer[]{TextTransfer.getInstance()}, this);
		libraryTreeViewer.addDropSupport(DND.DROP_MOVE, new Transfer[]{TextTransfer.getInstance()}, viewerDropAdapter);
		
		LibraryPreferences.addObserver(this);
		
		libraryTreeViewer.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = libraryTreeViewer.getSelection();
				if(selection.isEmpty()) return;
				if(selection instanceof IStructuredSelection) {
					IStructuredSelection selection2 = (IStructuredSelection) selection;					
					if(selection2.size() == 1) {
						Node currentNode = (Node)selection2.getFirstElement();	
						TreeItem item = libraryTreeViewer.getTree().getSelection()[0];	
						boolean isFunction =  (currentNode.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute) != null);
						if(!isFunction) {
							item.setExpanded(!item.getExpanded());
							libraryTreeViewer.refresh(currentNode);
						}
						else {
							
						}
					}
				}
				
			}
			
		});
		
		libraryTreeViewer.expandToLevel(2);

		MenuManager menuManager = new MenuManager();
		menuManager.add(MatlabFunctionEditorWindow.newFolderAction);
		menuManager.add(MatlabFunctionEditorWindow.newFileAction);
		menuManager.add(MatlabFunctionEditorWindow.openFileAction);
		menuManager.add(new Separator());
		menuManager.add(MatlabFunctionEditorWindow.deleteAction);
		libraryTreeViewer.getTree().setMenu(menuManager.createContextMenu(libraryTreeViewer.getTree()));
		
		
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener action) {
		libraryTreeViewer.addSelectionChangedListener(action);
	}
	
	public void update(Node node, Node nodeToSelect) {
		libraryTreeViewer.refresh(node);
		
		//experimentsTreeViewer.refresh();
		//experimentsTreeViewer.setSelection(new StructuredSelection(resource));
		//experimentsTreeViewer.expandToLevel(2);
		libraryTreeViewer.expandToLevel(node, 1);
		if(nodeToSelect != null)
		libraryTreeViewer.setSelection(new StructuredSelection(nodeToSelect));
	}
	
	public void refreshInput() {
		libraryTreeViewer.setInput(LibraryPreferences.getLibrary());
		libraryTreeViewer.expandToLevel(2);
	}

	public void addDoubleClickListener(IDoubleClickListener doubleClickListener) {
		libraryTreeViewer.addDoubleClickListener(doubleClickListener);		
	}
	
	//DND DRAG
	public void dragFinished(DragSourceEvent event) {
	}

	public void dragSetData(DragSourceEvent event) {
		if(TextTransfer.getInstance().isSupportedType(event.dataType)) {
			String elements = ""; //$NON-NLS-1$
			for (@SuppressWarnings("rawtypes")
			Iterator iterator = treeSelectionDND.iterator(); iterator.hasNext();) {
				Node element = (Node) iterator.next();
				elements = elements + ";" + element.getNodeName();			 //$NON-NLS-1$
			}
			event.data = elements.replaceAll("^;", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void dragStart(DragSourceEvent event) {
		treeSelectionDND = (TreeSelection) libraryTreeViewer.getSelection();
		event.doit = !treeSelectionDND.isEmpty();
		for (@SuppressWarnings("rawtypes")
		Iterator iterator = treeSelectionDND.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			boolean isEditable =  node.getAttributes().getNamedItem(LibraryPreferences.editableAttribute).getNodeValue().equals("true");
			if(!isEditable) event.doit = false;
		}
	}
}
