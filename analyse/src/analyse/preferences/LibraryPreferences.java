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
package analyse.preferences;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import com.sun.org.apache.xerces.internal.dom.ElementImpl;

import analyse.Log;
import analyse.model.Function;
import analyse.resources.IImagesKeys;
import analyse.resources.ImagesUtils;

@SuppressWarnings("deprecation")
public class LibraryPreferences {
	
	private static String defaultMessage = 
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + //$NON-NLS-1$
							"<form>" + //$NON-NLS-1$
							"<p>When you select a function, a <b>short</b> description will be displayed in this area.</p>" + //$NON-NLS-1$
							"</form>"; //$NON-NLS-1$
	
	private static Node libraryNode = null;
	private static LibraryContentProvider libraryContentProvider;
	private static LibraryLabelProvider libraryLableProvider; 

	private static Document libraryDocument;
	
	private static ArrayList<ILibraryObserver> libraryObservers = new ArrayList<ILibraryObserver>(0);
	
	public static String functionNameAttribute = "functionName"; //$NON-NLS-1$
	public static String shortDescriptionAttribute = "shortDescription"; //$NON-NLS-1$
	public static String editableAttribute = "editable"; //$NON-NLS-1$
	public static String trueAttributeValue = "true"; //$NON-NLS-1$
	public static String falseAttributeValue = "false"; //$NON-NLS-1$
	
	private static boolean standardLibrary = true;
	
	public static ViewerSorter libraryViewerSorter = new ViewerSorter() {
		public int category(Object object) {
			if(object instanceof Node) {
				Node node = (Node) object;
				boolean isFunction =  node.getAttributes().getNamedItem(functionNameAttribute) != null;
				if(isFunction) return 1;
				return 2;
			}
			return 3;
		}
	};
	
	private LibraryPreferences() {
		
	}
	
	public static void save() {
		try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(libraryDocument);
            // Prepare the output file
            String libraryName = "library.xml";
            if(!standardLibrary) libraryName = "extendedLibrary.xml";
            File file = new File(libraryName); //$NON-NLS-1$
            Result result = new StreamResult(file);
    
            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
        	Log.logErrorMessage(e);
        } catch (TransformerException e) {
        	Log.logErrorMessage(e);
        }
	}
	
	public static void initialize()  {
		
		if(libraryNode == null) {		
			libraryDocument = null;
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				
//				System.out.println(Messages.getString("LibraryPreferences.ConsoleCurrentDirLabel") + (new File(".")).getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
//				String path = System.getProperty("java.library.path"); //$NON-NLS-1$
//			    System.out.println(Messages.getString("LibraryPreferences.ConsoleJavaLibPathLabel") + path); //$NON-NLS-1$
				String libraryName = "library.xml";
	            if(!standardLibrary) libraryName = "extendedLibrary.xml";
	            File libraryFile = new File(libraryName);
	            if(!standardLibrary && !libraryFile.exists()) {
	            	BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(libraryFile));
	            	bufferedWriter.append("<?xml version=\"1.0\" encoding=\"UTF-8\"  standalone=\"no\"?>");
	            	bufferedWriter.newLine();
	            	bufferedWriter.append("<library rootDirectory=\"./matlabscripts/extendedLibrary/\">");
	            	bufferedWriter.newLine();
	            	bufferedWriter.append("	<root editable=\"true\">");
	            	bufferedWriter.newLine();
	            	bufferedWriter.append("	</root>");
	            	bufferedWriter.newLine();
	            	bufferedWriter.append("</library>");
	            	bufferedWriter.close();
	            	File extendedDirectory = new File("./matlabscripts/extendedLibrary/");
	            	extendedDirectory.mkdirs();
	            }
				FileInputStream file = new FileInputStream(libraryFile); //$NON-NLS-1$
				libraryDocument = builder.parse(file);			
			} catch (ParserConfigurationException e) {
				Log.logErrorMessage(e);
			} catch (FileNotFoundException e) {
				Log.logErrorMessage(e);
			} catch (SAXException e) {
				Log.logErrorMessage(e);
			} catch (IOException e) {
				Log.logErrorMessage(e);
			}
		
			libraryNode = libraryDocument.getFirstChild();
			libraryContentProvider = new LibraryContentProvider();
			libraryLableProvider = new LibraryLabelProvider();
		}
		
	}
	
	public static Node getLibrary() {
		return libraryNode;
	}
	

	public static Document getLibraryDocument() {
		if(libraryNode == null) initialize();
		return libraryDocument;
	}
	
	public static String getRootDirectory() {
		if(libraryNode == null) initialize();
		NamedNodeMap attributes = libraryNode.getAttributes();		
		return attributes.getNamedItem("rootDirectory").getNodeValue();
	}
	
	private static class LibraryContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			NodeList nodeList = ((Node)parentElement).getChildNodes();		
			ArrayList<Object> objectsList = new ArrayList<Object>(nodeList.getLength());			
			for (int i = 0; i < nodeList.getLength(); i++) 
				if(nodeList.item(i) instanceof Element) objectsList.add(nodeList.item(i));					
			return objectsList.toArray();
		}

		public Object getParent(Object element) {
			return ((Node)element).getParentNode();
		}

		public boolean hasChildren(Object element) {
			return ((Node)element).hasChildNodes();
		}

		public Object[] getElements(Object inputElement) {		
			return getChildren(inputElement);
		}


		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		public void dispose() {
			
		}
	}
	
	private static class LibraryLabelProvider implements ILabelProvider {
		public Image getImage(Object element) {
			Node functionNode = ((Node)element).getAttributes().getNamedItem(functionNameAttribute);
			if(functionNode == null)
			return ImagesUtils.getImage(IImagesKeys.LIBRARY_ICON);
			else return ImagesUtils.getImage(IImagesKeys.FUNCTION_ICON);
		}

		public String getText(Object element) {			
			Node functionNode = ((Node)element).getAttributes().getNamedItem(functionNameAttribute);
			return (functionNode == null) ? ((Node)element).getNodeName():functionNode.getNodeValue();						
		}

		public void addListener(ILabelProviderListener listener) {
			
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			
		}
		
		public void dispose() {
			
		}
	}
	
	public static ITreeContentProvider getContentProvider() {
		if(libraryNode == null) initialize();
		return libraryContentProvider;
	}
	
	public static ILabelProvider getLabelProvider() {
		if(libraryNode == null) initialize();
		return libraryLableProvider;
	}

	public static String getFunctionDescription(Object function) {		
		/*
		 * This is the short description retrieved from the matlab script file.
		 * It's just a copy/paste of this value in the right xml attribute of library.xml
		 * file.
		 * 
		 * When there will exist a GUI for matlab function construction, this will
		 * be done automatically : no need to do a copy/paste operation.
		 */
		if(libraryNode == null) initialize();
		if(function != null) {
			Node nodeElement = (Node) function;		
			if(nodeElement.getAttributes().getLength() > 0) {			
				String description = nodeElement.getAttributes().getNamedItem("shortDescription").getNodeValue();			
				return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><form>" + description + "</form>"; //$NON-NLS-1$ //$NON-NLS-2$
			}		
			else return defaultMessage;
		} else return defaultMessage;
	}

	public static String getDefaultFunctionDescription() {
		return defaultMessage;
	}

	public static void notifyObservers(Node node, Node nodeToSelect) {
		for (int i = 0; i < libraryObservers.size(); i++) {
			libraryObservers.get(i).update(node, nodeToSelect);			
		}
	}
	
	public static void addObserver(ILibraryObserver libraryObserver){
		libraryObservers.add(libraryObserver);
	}
	
	public static void removeObserver(ILibraryObserver libraryObserver) {
		libraryObservers.remove(libraryObserver);
	}

	public static void updateFunction(String oldFunctionName, String newFunctionName, String newShortDescription) {			
		NodeList nodeList = libraryNode.getChildNodes();		
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);					
			updateFunctionName(node,oldFunctionName, newFunctionName, newShortDescription);
		}
		save();
	}

	private static void updateFunctionName(Node node, String oldFunctionName,String newFunctionName, String newShortDescription) {
		if(node.hasChildNodes()) {
			NodeList nodeList = node.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				updateFunctionName(nodeList.item(i),oldFunctionName, newFunctionName, newShortDescription);
			}
		}
		boolean isFunction =  node.getAttributes().getNamedItem(functionNameAttribute) != null;
		if(isFunction) {			
			if(node.getAttributes().getNamedItem(functionNameAttribute).getNodeValue().equals(oldFunctionName)) {
				Element element = libraryDocument.createElement(newFunctionName);
				element.setAttribute(LibraryPreferences.functionNameAttribute, newFunctionName);
				element.setAttribute(LibraryPreferences.shortDescriptionAttribute,newShortDescription);
				node.getParentNode().appendChild(element);
				node.getParentNode().removeChild(node);
				notifyObservers(element.getParentNode(), element);
			}
		}
		
	}

	public static void updateShortDescription(String matlabFunctionName,String shortDescription) {
		NodeList nodeList = libraryNode.getChildNodes();		
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);					
			updateFunctionShortDescription(node,matlabFunctionName, shortDescription);
		}
	}

	private static void updateFunctionShortDescription(Node node,String matlabFunctionName, String shortDescription) {
		if(node.hasChildNodes()) {
			NodeList nodeList = node.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				updateFunctionShortDescription(nodeList.item(i),matlabFunctionName, shortDescription);
			}
		}
		boolean isFunction =  node.hasAttributes() && (node.getAttributes().getNamedItem(functionNameAttribute) != null);
		if(isFunction) {			
			if(node.getAttributes().getNamedItem(functionNameAttribute).getNodeValue().equals(matlabFunctionName)) {				
				node.getAttributes().getNamedItem(shortDescriptionAttribute).setNodeValue(shortDescription);
			}
		}
	}
	
	private static Function[] getFunctions(Node node) {
		HashSet<Function> functions = new HashSet<Function>(0);
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);
			boolean isFunction =  node.getAttributes().getNamedItem(functionNameAttribute) != null;
			if(isFunction) {
				Function function = new Function(childNode.getAttributes().getNamedItem(functionNameAttribute).getNodeValue());
				function.initializeFunction();
				functions.add(function);
			} else {
				Function[] childFunctions = getFunctions(childNode);
				functions.addAll(Arrays.asList(childFunctions));
			}
		}
		return functions.toArray(new Function[functions.size()]);
	}
	
	public static Function[] getAllFunctions() {
		return getFunctions(libraryNode);
	}
	
	public static boolean isStandardLibrary() {
		return standardLibrary;
	}

	public static boolean doSwitch() {
		save();
		libraryNode = null;
		if(standardLibrary) {
			standardLibrary = false;
		} else {
			standardLibrary = true;
		}
		initialize();
//		notifyObservers(libraryNode, null);
		return true;
	}

}

