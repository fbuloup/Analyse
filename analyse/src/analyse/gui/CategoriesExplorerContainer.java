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

import mathengine.IMathEngine;
import mathengine.MathEngineFactory;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import analyse.Log;
import analyse.gui.dialogs.NewCategoryInputDialog;
import analyse.model.Experiments;
import analyse.model.IResource;
import analyse.model.IResourceObserver;
import analyse.model.Subject;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;
import analyse.resources.Messages;

@SuppressWarnings("deprecation")
public final class CategoriesExplorerContainer extends Composite implements ISelectionChangedListener{

		private TableViewer categoriesListViewer;
		private CLabel crieriaValuelabel;
		private CLabel trialsListValuelabel;
		private CLabel subjectNamelabel;
		private Button newCategoryButton;
		
		CategoriesExplorerContainer(Composite parent) {
			super(parent, SWT.NONE);
			setLayout(new GridLayout(3,false));
			createContent();
		}

		public void update(int message, Subject selectedSubject) {
			
			if(message == IResourceObserver.LOADED ||
			   message == IResourceObserver.DELETED ||
			   message == IResourceObserver.SELECTION_CHANGED ||
			   message == IResourceObserver.CATEGORY_CREATED) {

				subjectNamelabel.setText(Messages.getString("NONE"));
				
				crieriaValuelabel.setText("");
				trialsListValuelabel.setText("");
				newCategoryButton.setEnabled(false);
				
				if(selectedSubject != null)	{
					subjectNamelabel.setText(selectedSubject.getLocalPath() + (selectedSubject.isLoaded()?"":" (" + Messages.getString("NotLoaded") + ")"));
					newCategoryButton.setEnabled(selectedSubject.isLoaded());
				}
//				long t1 = System.currentTimeMillis();
				categoriesListViewer.setInput(selectedSubject);
//				t1 = System.currentTimeMillis() - t1;
//				System.out.println(">>>>>>>>>>>>>>>>>> Time to categoriesListViewer.setInput : " + t1);

			} else if(message == IResourceObserver.RENAMED) {
				subjectNamelabel.setText(Messages.getString("NONE"));
				if(selectedSubject != null)	subjectNamelabel.setText(selectedSubject.getLocalPath() + (selectedSubject.isLoaded()?"":" (" + Messages.getString("NotLoaded") + ")"));
			} else if(message == IResourceObserver.CATEGORY_CREATED) {
				selectionChanged(new SelectionChangedEvent(categoriesListViewer ,categoriesListViewer.getSelection()));
			} else if(message == IResourceObserver.CHANNEL_DELETED) {
				categoriesListViewer.refresh();
				categoriesListViewer.getTable().select(0);
				selectionChanged(new SelectionChangedEvent(categoriesListViewer ,categoriesListViewer.getSelection()));
			} else if(message == IResourceObserver.PROCESS_RUN) {
				categoriesListViewer.refresh();
				selectionChanged(new SelectionChangedEvent(categoriesListViewer ,categoriesListViewer.getSelection()));
			}
			/* else if(message == IResourceObserver.MARKER_DELETED ||
			}
					message == IResourceObserver.MARKER_ADDED ||
					message == IResourceObserver.MARKERS_GROUP_DELETED) {
				selectionChanged(new SelectionChangedEvent(categoriesListViewer ,categoriesListViewer.getSelection()));
			} else if(message == IResourceObserver.FIELD_DELETED) {
				selectionChanged(new SelectionChangedEvent(categoriesListViewer ,categoriesListViewer.getSelection()));
			}*/
			
			
			
		}

		private void createContent() {
			CLabel selectedSubjectlabel = new CLabel(this, SWT.NONE);
			selectedSubjectlabel.setText(Messages.getString("SelectedSubject"));
			selectedSubjectlabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			subjectNamelabel = new CLabel(this, SWT.NONE);
			subjectNamelabel.setText(Messages.getString("NONE"));
			subjectNamelabel.setToolTipText(Messages.getString("SelectedSubjectToolTip"));
			subjectNamelabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			
			newCategoryButton = new Button(this, SWT.PUSH | SWT.FLAT);
			newCategoryButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
			newCategoryButton.setToolTipText(Messages.getString("ChannelsView.AddCategoryButtonTooltip"));
			newCategoryButton.setImage(ImagesUtils.getImage(IImagesKeys.ADD_CATEGORY_ICON));
			newCategoryButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					NewCategoryInputDialog newCategoryInputDialog = new NewCategoryInputDialog(getShell(), ((Subject)categoriesListViewer.getInput()));
					if(newCategoryInputDialog.open() == Window.OK) {
						Subject subject = ((Subject)categoriesListViewer.getInput());
						IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
						if(mathEngine.isStarted()) 
							if(!mathEngine.createNewCategory(subject.getLocalPath(), newCategoryInputDialog.getValue(), newCategoryInputDialog.getCriteria(), newCategoryInputDialog.getTrialsList()))
								Log.logErrorMessage("Problem while trying to create category !");
							else subject.setModified(true);
						Experiments.notifyObservers(IResourceObserver.CATEGORY_CREATED, new IResource[]{subject});
					}
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			newCategoryButton.setEnabled(false);
			newCategoryButton.addFocusListener((FocusListener) getParent());
			
			categoriesListViewer  = new TableViewer(this, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
			categoriesListViewer.getTable().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,3,1));
			categoriesListViewer.getTable().addFocusListener((FocusListener) getParent());
			categoriesListViewer.setContentProvider(new IStructuredContentProvider() {
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}
				public void dispose() {
				}
				public Object[] getElements(Object inputElement) {
					if(inputElement instanceof Subject) {
						Subject subject = (Subject)inputElement;
						if(subject.isLoaded()) {
							String[] categoriesNames = subject.getCategoriesNames();//MathEngineFactory.getInstance().getMathEngine().getCategoriesNames(subject.getLocalPath());
							return categoriesNames;
						}
					}
					return new String[0];
				}
			});
			categoriesListViewer.setLabelProvider(new ILabelProvider() {
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
					return null;
				}
			});
			categoriesListViewer.addSelectionChangedListener(this);
			categoriesListViewer.addSelectionChangedListener((ISelectionChangedListener)ChannelsView.deleteChannelsAction.getAction());
			categoriesListViewer.setSorter(new ViewerSorter());
			
			Composite bottomContainer = new Composite(this, SWT.NONE);
			bottomContainer.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,3,1));
			bottomContainer.setLayout(new GridLayout(2,false));
			
			CLabel criterialabel = new CLabel(bottomContainer, SWT.NONE);
			criterialabel.setText(Messages.getString("ChannelsView.CategoriesItemCriteriaLabelTitle"));
			criterialabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			crieriaValuelabel = new CLabel(bottomContainer, SWT.NONE);
			crieriaValuelabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			
			CLabel trialsListlabel = new CLabel(bottomContainer, SWT.NONE);
			trialsListlabel.setText(Messages.getString("ChannelsView.CategoriesItemTrialsListLabelTitle"));
			trialsListlabel.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			trialsListValuelabel = new CLabel(bottomContainer, SWT.NONE);
			trialsListValuelabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
			
			FontRegistry fontRegistry = JFaceResources.getFontRegistry();
			Font textFont = categoriesListViewer.getTable().getFont();
			FontData fontData = new  FontData(textFont.getFontData()[0].getName(), textFont.getFontData()[0].getHeight(), SWT.BOLD | SWT.ITALIC);
			fontRegistry.put("MY_SELECTED_TEXT_FONT", new FontData[]{fontData});
			fontRegistry.put("MY_TEXT_FONT",textFont.getFontData());
		}

		public void selectionChanged(SelectionChangedEvent event) {
			if(categoriesListViewer.getTable().getSelectionIndex() > -1) {
				TableItem selectedItem = categoriesListViewer.getTable().getItem(categoriesListViewer.getTable().getSelectionIndex());
				String categoryName  = selectedItem.getText();
				String fullCategoryName = ((Subject)categoriesListViewer.getInput()).getLocalPath() + "." + categoryName;
				IMathEngine mathEngine = MathEngineFactory.getInstance().getMathEngine();
				String criteria = mathEngine.getCriteriaForCategory(fullCategoryName);
				crieriaValuelabel.setText(criteria);
				int[] trialsList = mathEngine.getTrialsListForCategory(fullCategoryName);
				String trialsListString = "";
				for (int i = 0; i < trialsList.length; i++) trialsListString = trialsListString + " - "  + trialsList[i]; 
				trialsListString = trialsListString.replaceAll("^ - ", "");
				trialsListValuelabel.setText(trialsListString);
				
				for (int i = 0; i < categoriesListViewer.getTable().getItemCount(); i++) {
					TableItem item = categoriesListViewer.getTable().getItem(i);
					if(selectedItem == item) item.setFont(JFaceResources.getFont("MY_SELECTED_TEXT_FONT"));
					else item.setFont(JFaceResources.getFont("MY_TEXT_FONT"));
				}
			}
		}
		
	}
