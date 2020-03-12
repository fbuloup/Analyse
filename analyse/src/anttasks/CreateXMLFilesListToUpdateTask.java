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
package anttasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class CreateXMLFilesListToUpdateTask extends Task {
	
	private String[] fromFolders;
	private String fileName;
	private String destinationFolder;
	private boolean append;
	private String pathPrefix;
	private String exclude;
	
	private String fullPathXMLFile;
	private Document xmlDocument;
	private DocumentBuilder builder;
	private Node rootNode;
	
	public void setFromFolder(String fromFolders) {
		this.fromFolders = fromFolders.split(";");
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}
	
	public void setAppend(boolean append) {
		this.append = append;
	}
	
	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}

	public void setExclude(String exclude) {
		this.exclude = exclude;
	}

	@Override
	public void execute() throws BuildException {
		for (int i = 0; i < fromFolders.length; i++) {
			System.out.println("Create files list to update from : " + fromFolders[i]);
		}
		if(exclude != null) {
			String[] excludes = exclude.split(";");
			for (int i = 0; i < excludes.length; i++) {
				System.out.println("Excluding : " + excludes[i]);
			}
		}
		
		System.out.println("In file : " + fileName);
		System.out.println("Saved in : " + destinationFolder);
		System.out.println("Add to existing file : " + append);
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			fullPathXMLFile = destinationFolder + File.separator + fileName;
			/*
			 * xml buidler
			 */
			if(append) {
				System.out.println("Open file : " + fullPathXMLFile);
				openXMLFile(); 
			}
			else {
				System.out.println("Create file : " + fullPathXMLFile);
				createXMLFile();
			}
			
			String[] files = getFiles();
			System.out.println("Number of files to proceed : " + files.length);
			for (int i = 0; i < files.length; i++) {
				Element fileElement = xmlDocument.createElement("file");
				Attr attribute = xmlDocument.createAttribute("name");
				attribute.setNodeValue(pathPrefix + (pathPrefix.equals("")?"":"/") + files[i]);
				fileElement.setAttributeNode(attribute);
				rootNode.appendChild(fileElement);
				System.out.println("Element added : " + attribute.getNodeValue());
			}
			
			saveXMLFilesList();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	private String[] getFiles() {
		ArrayList<String> files = new ArrayList<String>(0);
		for (int i = 0; i < fromFolders.length; i++) {
			String[] filesTemp = (new File(fromFolders[i])).list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					boolean accept = (new File(dir.getAbsolutePath() + File.separator + name)).isFile() && !name.equals(".project");
					if(exclude != null) {
						String[] excludes = exclude.split(";");
						for (int j = 0; j < excludes.length; j++) {
							accept = accept && !name.matches(excludes[j]);
						}
					}
					return accept;
				}
			});
			files.addAll(Arrays.asList(filesTemp));
		}
		return files.toArray(new String[files.size()]);
	}

	private void createXMLFile() {
		File XMLFile = new File(fullPathXMLFile);
		XMLFile.delete();
		//System.out.println("Create XML file result : " + XMLFile.createNewFile());;
		xmlDocument = builder.newDocument();
		rootNode = xmlDocument.createElement("files");
		xmlDocument.appendChild(rootNode);
	}

	private void openXMLFile() {
		try {
			xmlDocument = builder.parse(new FileInputStream(fullPathXMLFile));
			rootNode = xmlDocument.getFirstChild();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveXMLFilesList() {
		try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(xmlDocument);
            // Prepare the output file
            File file = new File(fullPathXMLFile); //$NON-NLS-1$
            Result result = new StreamResult(file.toURI().getPath());
            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
			e.printStackTrace();
        } catch (TransformerException e) {
			e.printStackTrace();
        }
	}

}
