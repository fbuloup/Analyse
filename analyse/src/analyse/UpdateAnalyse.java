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
package analyse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.internal.Platform;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sun.net.www.protocol.file.FileURLConnection;

import analyse.gui.dialogs.UpdateDialog;
import analyse.preferences.AnalysePreferences;
import analyse.resources.CPlatform;
import analyse.resources.Messages;

public class UpdateAnalyse {
	
	public static final String prefix = Platform.PLATFORM.equals("win32")?"/":"";
	public static final String updateDirectory = "UPDATE";
	
	private String distantBasePath;
	private String localBasePath;
	private String localBasePathURL;
	
//	private String distantMatlabFunctionsScriptsPath;
//	private String localMatlabFunctionsScriptsPathURL;
	
//	private Document localLibraryDocument;
	
	private boolean compareContentFiles;
	private boolean verboseUpdate;
	
//	private ArrayList<Node> newFoldersList = new ArrayList<Node>(0);
//	private Map<Node, Node> movedFoldersList = new Hashtable<Node, Node>(0);
//	private ArrayList<Node> newFunctionsList = new ArrayList<Node>(0);
//	private Map<Node, Node> changedMovedFunctionsList = new Hashtable<Node, Node>(0);
//	private ArrayList<Node> changedFunctionsList = new ArrayList<Node>(0);
//	private Map<Node, Node> movedFunctionsList = new Hashtable<Node, Node>(0);
	private ArrayList<Node> changedCoreFilesList = new ArrayList<Node>(0);

//	private String localMatlabFunctionsScriptsPath;

	private URL distantXMLUpdateFilesListURL;
//	private URL localXMLLibraryFileURL;
//	private URL distantXMLLibraryFileURL;

	private UpdateDialog updateDialog;

//	private Node distantLibraryNode;

	private Node distantScriptsFilesNode;

	private int nbSteps;
	
	public UpdateAnalyse(String localBasePath, URL distantXMLUpdateFilesListURL, URL localBasePathURL, /*URL distantXMLLibraryFileURL,*/ UpdateDialog updateDialog) throws ParserConfigurationException, IOException, SAXException {
		this.localBasePath = localBasePath;
		this.distantXMLUpdateFilesListURL = distantXMLUpdateFilesListURL; 
//		this.localXMLLibraryFileURL = localXMLLibraryFileURL; 
//		this.distantXMLLibraryFileURL = distantXMLLibraryFileURL;
		distantBasePath = distantXMLUpdateFilesListURL.toString().replaceAll("updateFilesList.xml$", "");
		this.localBasePathURL = localBasePathURL.toString();//localXMLLibraryFileURL.getPath().replaceAll("library.xml$", "");
//		distantMatlabFunctionsScriptsPath = distantXMLUpdateFilesListURL.toString().replaceAll("updateFilesList.xml$", "matlabscripts/library/");
//		localMatlabFunctionsScriptsPathURL = localBasePathURL + "matlabscripts/library/";
//		localMatlabFunctionsScriptsPath = localBasePath + "matlabscripts" + File.separator + "library" + File.separator;
		compareContentFiles = AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.COMPARE_FILE_CONTENT_DURING_UPDATE);
		verboseUpdate = AnalysePreferences.getPreferenceStore().getBoolean(AnalysePreferences.VERBOSE_DURING_UPDATE);
		this.updateDialog = updateDialog;
		setUp();
	}
	
	private void setUp() throws ParserConfigurationException, IOException, SAXException {
		/*
		 * xml buidler
		 */
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		//Open distant library file
//		URLConnection distantXMLLibraryFileURLConnection = distantXMLLibraryFileURL.openConnection();
//		distantXMLLibraryFileURLConnection.setUseCaches(false);
//		distantXMLLibraryFileURLConnection.connect();
//		InputStream xmlInputStream = distantXMLLibraryFileURLConnection.getInputStream();
//		Document libraryDocument = builder.parse(xmlInputStream);
//		distantLibraryNode = libraryDocument.getFirstChild();
		//Open local library file
//		URLConnection localURLConnection = localXMLLibraryFileURL.openConnection();
//		localURLConnection.setUseCaches(false);
//		localURLConnection.connect();
//		InputStream localXMLInputStream = localURLConnection.getInputStream();
//		localLibraryDocument = builder.parse(localXMLInputStream);
		//Open distant folder
		URLConnection distantXMLScriptsFilesListURLConnection = distantXMLUpdateFilesListURL.openConnection();
		distantXMLScriptsFilesListURLConnection.setUseCaches(false);
		distantXMLScriptsFilesListURLConnection.connect();
		InputStream distantXMLScriptsFilesInputStream = distantXMLScriptsFilesListURLConnection.getInputStream();
		Document scriptsFilesDocument = builder.parse(distantXMLScriptsFilesInputStream);
		distantScriptsFilesNode = scriptsFilesDocument.getFirstChild();
//		int nbLibrarySteps = libraryDocument.getElementsByTagName("*").getLength();
		int nbCoreSteps = scriptsFilesDocument.getElementsByTagName("*").getLength();
		nbSteps = /*nbLibrarySteps +*/ nbCoreSteps;
	}
	
	public int getNbSteps() {
		return nbSteps;
	}
	
	public void doCompare(IProgressMonitor monitor) throws IOException {
		//Clear changes
		/*newFoldersList.clear();
		movedFoldersList.clear();
		newFunctionsList.clear();
		changedMovedFunctionsList.clear();
		changedFunctionsList.clear();
		movedFunctionsList.clear();*/
		changedCoreFilesList.clear();
		//Compare Library
		//compareLibrary(distantLibraryNode, monitor);
		/*
		 * Compare Core files including matlab scripts
		 * Files list come from updateFilesList.xml
		 */
		compareCoreFiles(monitor);
	}
	
	/*private void compareLibrary(Node distantNode, IProgressMonitor monitor) throws IOException {
		NodeList localNodes = localLibraryDocument.getElementsByTagName(distantNode.getNodeName());
		boolean isFunction =  distantNode.getAttributes().getNamedItem(LibraryPreferences.functionNameAttribute) != null;
		if(localNodes.getLength() == 1) {
			//This Node already exists. Check if it has same place.
			Node localNode = localNodes.item(0);
			monitor.subTask(Messages.getString("UpdateAnalyse.ComparingNode") + localNode.getNodeName());
			Node parentLocalNode = localNode;
			Node parentNode = distantNode;
			boolean end = false;
			boolean hasSamePlace = true;
			while (!end) {
				parentLocalNode = parentLocalNode.getParentNode();
				parentNode = parentNode.getParentNode();
				if(!(parentLocalNode == null) && !(parentNode == null)) {
					if(!parentLocalNode.getNodeName().equals(parentNode.getNodeName())) {
						end = true;
						hasSamePlace = false;
					}
				} else if((parentLocalNode == null) && (parentNode == null)) {
					end = true;
					hasSamePlace = true;
				} else {
					end = true;
					hasSamePlace = false;
				}
			}
			
			if(!hasSamePlace) {
				if(!isFunction) {
					//If container : Container has been moved -> add to new location containers list without any children.
					movedFoldersList.put(distantNode,localNode);
				}
				if(isFunction) {
					//If function, compare distant and local files.
					//Compare files
					URL file1URL = new URL(distantMatlabFunctionsScriptsPath + distantNode.getNodeName() + ".m");
					URL file2URL = new URL(localMatlabFunctionsScriptsPathURL + distantNode.getNodeName() + ".m");
					boolean equals = compareFiles(file1URL, file2URL);
					if(equals) {
						//If equal, function has been moved -> add to new location function list
						movedFunctionsList.put(distantNode,localNode);
					}
					else {
						//If not : this is possibly an error, add this function to new location function list with override flag.
						changedMovedFunctionsList.put(distantNode,localNode);
					}
				}
			} else {
				//Same place
				if(!distantNode.getNodeName().equals("library")) {
					if(isFunction) {
						//If function, compare distant and local files.
						URL file1URL = new URL(distantMatlabFunctionsScriptsPath + distantNode.getNodeName() + ".m");
						URL file2URL = new URL(localMatlabFunctionsScriptsPathURL + distantNode.getNodeName() + ".m");
						boolean equals = compareFiles(file1URL, file2URL);
						if(!equals)  changedFunctionsList.add(distantNode);
					} //else, this is a folder that have same place... nothing to do
				} //else, this is library node, don't bother
			}
			monitor.worked(1);
		} else if(localNodes.getLength() == 0) {
			if(!isFunction) {
				newFoldersList.add(distantNode);
				monitor.worked(1);
			} else {
				newFunctionsList.add(distantNode);
				monitor.worked(1);
			}
		} else {
			//Error : more than two matching local functions or folders, must be impossible
		}
		NodeList distantNodeList = distantNode.getChildNodes();		
		for (int i = 0; i < distantNodeList.getLength(); i++) {
			if(distantNodeList.item(i) instanceof Element) {
				compareLibrary(distantNodeList.item(i), monitor);
			}
		}
	}*/
	
	private void compareCoreFiles(IProgressMonitor monitor) throws DOMException, IOException {
		NodeList distantNodeList = distantScriptsFilesNode.getChildNodes();
		for (int i = 0; i < distantNodeList.getLength(); i++) {
			if(distantNodeList.item(i) instanceof Element) {
				NamedNodeMap attribute = distantNodeList.item(i).getAttributes();
				monitor.subTask(Messages.getString("UpdateAnalyse.ComparingCoreFile") + attribute.item(0).getNodeValue());
				boolean equals = false;
				String fullLocalPath = localBasePath + attribute.item(0).getNodeValue();
				if(CPlatform.isWindows()) fullLocalPath = fullLocalPath.replace("/", "\\");
				if((new File(fullLocalPath)).exists()) {
					URL file1URL = new URL(distantBasePath + attribute.item(0).getNodeValue());
					URL file2URL = new URL(localBasePathURL + attribute.item(0).getNodeValue());
					equals = compareFiles(file1URL, file2URL);
				} else if(verboseUpdate) updateDialog.logMessage(Messages.getString("UpdateAnalyse.ComparingFile") + attribute.item(0).getNodeValue() + "\n****\t---> " + Messages.getString("UpdateAnalyse.FileDoesNotExist"));
				if(!equals) changedCoreFilesList.add(distantNodeList.item(i));
				monitor.worked(1);
			}
		}
	}
	
	public boolean applyChanges(IProgressMonitor monitor) throws IOException {
		//New Folders
		/*{Node[] nodes = newFoldersList.toArray(new Node[newFoldersList.size()]);
		for (int i = 0; i < nodes.length; i++) {
			monitor.subTask(Messages.getString("UpdateAnalyse.CreateNewFolder") + nodes[i].getNodeName());
			String name = nodes[i].getParentNode().getNodeName();
			Node parentNode = localLibraryDocument.getElementsByTagName(name).item(0);
			Element newNode = localLibraryDocument.createElement(nodes[i].getNodeName());
			parentNode.appendChild(newNode);
			monitor.worked(100);
		}}
		//Moved folders
		{Set<Node> nodes = movedFoldersList.keySet();
		for (Node toNode : nodes) {
			monitor.subTask(Messages.getString("UpdateAnalyse.MoveFolder") + toNode.getNodeName());
			Node fromNode = movedFoldersList.get(toNode);
			String name = toNode.getParentNode().getNodeName();
			Node parentNode = localLibraryDocument.getElementsByTagName(name).item(0);
			parentNode.appendChild(fromNode);
			monitor.worked(100);
		}}
		//New functions
		{Node[] nodes = newFunctionsList.toArray(new Node[newFunctionsList.size()]);
		for (int i = 0; i < nodes.length; i++) {
			monitor.subTask(Messages.getString("UpdateAnalyse.GettingNewFunction") + nodes[i].getNodeName());
			if(getFunctionFileFromURL(nodes[i].getNodeName(), monitor)) {
				String name = nodes[i].getParentNode().getNodeName();
				Node parentNode = localLibraryDocument.getElementsByTagName(name).item(0);
				Element newNode = localLibraryDocument.createElement(nodes[i].getNodeName());
				NamedNodeMap attributes = nodes[i].getAttributes();
				for (int j = 0; j < attributes.getLength(); j++) {
					Attr attribute = localLibraryDocument.createAttribute(attributes.item(j).getNodeName());
					attribute.setNodeValue(attributes.item(j).getNodeValue());
					newNode.setAttributeNode(attribute);
				}
				parentNode.appendChild(newNode);
			} else throw new IOException(Messages.getString("UpdateAnalyse.ImpossibleToGetFunction") + nodes[i].getNodeName());
		}}
		//changed moved functions
		{Set<Node> nodes = changedMovedFunctionsList.keySet();
		for (Node toNode : nodes) {
			monitor.subTask(Messages.getString("UpdateAnalyse.ChangedAndMovedFunction") + toNode.getNodeName());
			if(getFunctionFileFromURL(toNode.getNodeName(), monitor)) {
				Node fromNode = changedMovedFunctionsList.get(toNode);
				String name = toNode.getParentNode().getNodeName();
				Node parentNode = localLibraryDocument.getElementsByTagName(name).item(0);
				parentNode.appendChild(fromNode);
			} else throw new IOException(Messages.getString("UpdateAnalyse.ImpossibleToGetFunction") + toNode.getNodeName());
		}}
		//changed functions
		{Node[] nodes = changedFunctionsList.toArray(new Node[changedFunctionsList.size()]);
		for (int i = 0; i < nodes.length; i++) {
			monitor.subTask(Messages.getString("UpdateAnalyse.ChangedFunction") + nodes[i].getNodeName());
			if(!getFunctionFileFromURL(nodes[i].getNodeName(), monitor)) throw new IOException(Messages.getString("UpdateAnalyse.ImpossibleToGetFunction") + nodes[i].getNodeName());
		}}
		//Moved functions
		{Set<Node> nodes = movedFunctionsList.keySet();
		for (Node toNode : nodes) {
			monitor.subTask(Messages.getString("UpdateAnalyse.MovedFunction") + toNode.getNodeName());
			Node fromNode = movedFunctionsList.get(toNode);
			String name = toNode.getParentNode().getNodeName();
			Node parentNode = localLibraryDocument.getElementsByTagName(name).item(0);
			parentNode.appendChild(fromNode);
			monitor.worked(100);
		}}
		saveXMLLibrary();*/
		//changed core files
		{Node[] nodes = changedCoreFilesList.toArray(new Node[changedCoreFilesList.size()]);
		for (int i = 0; i < nodes.length; i++) {
			monitor.subTask(Messages.getString("UpdateAnalyse.GettingCoreFile") + nodes[i].getAttributes().item(0).getNodeValue());
			if(!getCoreFileFromURL(nodes[i].getAttributes().item(0).getNodeValue(), monitor)) throw new IOException(Messages.getString("UpdateAnalyse.ImpossibleToGetFunction") + nodes[i].getAttributes().item(0).getNodeValue());
		}}
		return true;
	}
	
	private boolean compareFiles(URL distantFileURL, URL localFileURL) throws IOException {
		String[] segments =  localFileURL.getFile().split("/");
		URLConnection localFileURLConnection = localFileURL.openConnection();
		URLConnection distantFileURLConnection = distantFileURL.openConnection();
		//Compare files sizes
		if(verboseUpdate) updateDialog.logMessage(Messages.getString("UpdateAnalyse.ComparingFile") + segments[segments.length - 1]);
		if(localFileURLConnection.getContentLength() == distantFileURLConnection.getContentLength()) {
			if(verboseUpdate) updateDialog.logMessage("\t---> " + Messages.getString("UpdateAnalyse.EqualSize"));
			if(compareChecksum(distantFileURL, localFileURL)) {
				if(verboseUpdate) updateDialog.logMessage("\t---> " + Messages.getString("UpdateAnalyse.SameChecksum"));
				//Compare files contents
				if(compareContentFiles) {
					return compareContentFiles(distantFileURLConnection.getInputStream(),localFileURLConnection.getInputStream());
				} 
				return true;
			} else {
				if(verboseUpdate) updateDialog.logMessage("****\t---> " + Messages.getString("UpdateAnalyse.DifferentChecksum"));
				return false;
			}
		} else {
			if(verboseUpdate) updateDialog.logMessage("****\t---> " + Messages.getString("UpdateAnalyse.DifferentSize"));
			return false;
		}
	}
	
	private boolean compareChecksum(URL distantFileURL, URL localFileURL) throws IOException  {
		//Compare files checksums
		//Check if sha distant file exists
		String shaDistantFile = distantFileURL.getFile().replaceAll("\\.\\w+", ".sha");
		URL shaDistantFileURL = new URL(distantFileURL.getProtocol() + "://" + distantFileURL.getHost() + shaDistantFile);
		URLConnection urlConnection = shaDistantFileURL.openConnection();
		boolean shaDistantFileExists = false;
		if(urlConnection instanceof HttpURLConnection) {
			HttpURLConnection httpURLConnection = ((HttpURLConnection)urlConnection);
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.connect();
			shaDistantFileExists = httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK ;
		}
		if(urlConnection instanceof FileURLConnection) {
			FileURLConnection fileURLConnection = ((FileURLConnection)urlConnection);
			shaDistantFileExists = fileURLConnection.getContentLength() > 0;
		}
		if(verboseUpdate) updateDialog.logMessage("\t---> " + Messages.getString("UpdateAnalyse.DoesSHAFileExist") + shaDistantFileExists);
		String cs1;
		if(shaDistantFileExists) {
			if(verboseUpdate) updateDialog.logMessage("\t---> " + Messages.getString("UpdateAnalyse.GetSHACode"));
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			cs1 = bufferedReader.readLine();
			bufferedReader.close();
		} else {
			if(verboseUpdate) updateDialog.logMessage("\t---> " + Messages.getString("UpdateAnalyse.ComputeSHACode"));
			cs1 =  Utils.convertChecksum2String(Utils.computeCheckSum(distantFileURL.getProtocol() + "://" + distantFileURL.getHost() + distantFileURL.getFile()));
		}
		String cs2 =  Utils.convertChecksum2String(Utils.computeCheckSum(localFileURL.getProtocol() + "://" + localFileURL.getHost() + localFileURL.getFile()));
		if(verboseUpdate) {
			updateDialog.logMessage("\t---> " + Messages.getString("UpdateAnalyse.DistantSHACode") + cs1);
			updateDialog.logMessage("\t---> " + Messages.getString("UpdateAnalyse.LocalSHACode") + cs2);
		}
		if(cs1 != null)	return cs1.equals(cs2);
		else return false;
	}
	
	private boolean compareContentFiles(InputStream distantFile, InputStream localFile) throws IOException {
		int byte1 = 0, byte2 = 0;
		boolean result = true;
		while(byte1 != -1 || byte2 != -1) {
			byte1 = distantFile.read();
			byte2 = localFile.read();
			if(byte1 != byte2) {
				result = false;
				break;
			}
		}
		distantFile.close();
		localFile.close();
		result = result && (byte1 == -1) && (byte2 == -1);
		if(verboseUpdate) {
			if(!result) updateDialog.logMessage("****\t---> " + Messages.getString("UpdateAnalyse.DifferentContent"));
			else updateDialog.logMessage("\t---> " + Messages.getString("UpdateAnalyse.SameContent"));
		}
		return result;
	}
	
	private boolean getFileFromURL(String fromFileName, String toFileName, IProgressMonitor monitor) {
		InputStream fileInputStream;
		FileOutputStream fileOutputStream;
		try {
			URL fileURL = new URL(fromFileName);
			URLConnection urlConnection = fileURL.openConnection();
			int fileSize = urlConnection.getContentLength();
			fileInputStream = urlConnection.getInputStream();
			File file = new File(toFileName);
			File directory = new File(file.getParent());
			boolean exists = directory.exists();
			if(!exists) exists = directory.mkdirs();
			if(exists) {
				fileOutputStream = new FileOutputStream(file);
				byte[] bytesBuffer = new byte[1024];
				int bytesRead;
				int totalBytesRead = 0;
				int previousWork = 0;
				while((bytesRead = fileInputStream.read(bytesBuffer)) > -1) {
					fileOutputStream.write(bytesBuffer, 0, bytesRead);
					totalBytesRead += bytesRead;
					int work = (int) ((100.0*totalBytesRead) /fileSize);
					monitor.worked(work - previousWork);
					previousWork = work;
				}
				fileInputStream.close();
				fileOutputStream.close();
			} else return false;
		} catch (MalformedURLException e) {
			Log.logErrorMessage(e);
			return false;
		} catch (IOException e) {
			Log.logErrorMessage(e);
			return false;
		}
		return true;
	}
	
//	private boolean getFunctionFileFromURL(String fileName, IProgressMonitor monitor) {
//		String fromFile = distantMatlabFunctionsScriptsPath + fileName + ".m";
//		String toFile = localMatlabFunctionsScriptsPath + fileName + ".m";
//		return getFileFromURL(fromFile, toFile, monitor);
//	}
	
	private boolean getCoreFileFromURL(String fileName, IProgressMonitor monitor) {
		String fromFile = distantBasePath + fileName;
		File updateFolderFile = new File(localBasePath + updateDirectory);
		boolean exists = updateFolderFile.exists();
		if(!exists) exists = updateFolderFile.mkdir();
		if(exists) {
			String toFile = localBasePath + updateDirectory + File.separator + fileName;
			if(CPlatform.isWindows()) toFile = toFile.replace("/", "\\");
			return getFileFromURL(fromFile, toFile, monitor);
		}
		return false;
	}

//	private void saveXMLLibrary() {
//		try {
//            // Prepare the DOM document for writing
//            Source source = new DOMSource(localLibraryDocument);
//            // Prepare the output file
//            File file = new File(localBasePath + File.separator + "library.xml"); //$NON-NLS-1$
//            Result result = new StreamResult(file);
//            // Write the DOM document to the file
//            Transformer xformer = TransformerFactory.newInstance().newTransformer();
//            xformer.transform(source, result);
//        } catch (TransformerConfigurationException e) {
//			Log.logErrorMessage(e);
//        } catch (TransformerException e) {
//			Log.logErrorMessage(e);
//        }
//	}
	
	/*public String[] getNewFoldersList() {
		return getStringsFromArray(Messages.getString("UpdateAnalyse.NewFolder"), newFoldersList);
	}

	public String[] getMovedFoldersList() {
		return getStringsFromMap(Messages.getString("UpdateAnalyse.MovedFolder"), movedFoldersList);
	}

	public String[] getNewFunctionsList() {
		return getStringsFromArray(Messages.getString("UpdateAnalyse.NewFunction"), newFunctionsList);
	}

	public String[] getChangedMovedFunctionsList() {
		return getStringsFromMap(Messages.getString("UpdateAnalyse.ChangedAndMovedFunction"), changedMovedFunctionsList);
	}

	public String[] getChangedFunctionsList() {
		return getStringsFromArray(Messages.getString("UpdateAnalyse.ChangedFunction"), changedFunctionsList);
	}

	public String[] getMovedFunctionsList() {
		return getStringsFromMap(Messages.getString("UpdateAnalyse.MovedFunction"), movedFunctionsList);
	}*/

	public String[] getChangedCoreFilesList() {
		Node[] nodes = changedCoreFilesList.toArray(new Node[changedCoreFilesList.size()]);
		String[] strings = new String[nodes.length];
		for (int i = 0; i < strings.length; i++) {
			String fileName =  nodes[i].getAttributes().item(0).getNodeValue();
			if(fileName.startsWith("matlabscripts/library")) strings[i] = Messages.getString("UpdateAnalyse.ChangedFunction") + fileName;
			else strings[i] = Messages.getString("UpdateAnalyse.ChangedCoreFile") + fileName;
		}
		return strings;
	}
	
//	private String getFullPath(Node node, String path) {
//		if(!node.getNodeName().equals("library")) return getFullPath(node.getParentNode(), node.getNodeName() + "/" + path);
//		else return path;
//	}
	
//	private String[] getStringsFromMap(String prefix, Map<Node,Node> newList) {
//		Set<Node> nodesKeys = newList.keySet();
//		String[] strings = new String[nodesKeys.size()];
//		int i = 0;
//		for (Node toNode : nodesKeys) {
//			Node fromNode = newList.get(toNode);
//			strings[i] = prefix + fromNode.getNodeName() + Messages.getString("UpdateAnalyse.From") + getFullPath(fromNode.getParentNode(), "") + Messages.getString("UpdateAnalyse.To") + getFullPath(toNode.getParentNode(), "");
//			i++;
//		}
//		return strings;
//	}
//	
//	private String[] getStringsFromArray(String prefix, ArrayList<Node> newList) {
//		Node[] nodes = newList.toArray(new Node[newList.size()]);
//		String[] strings = new String[nodes.length];
//		for (int i = 0; i < strings.length; i++) {
//			strings[i] = prefix + nodes[i].getNodeName() + Messages.getString("UpdateAnalyse.In") + getFullPath(nodes[i].getParentNode(), "");
//		}
//		return strings;
//	}
	
	public String[] getChanges() {
		ArrayList<String> changes = new ArrayList<String>(0);
		/*changes.addAll(Arrays.asList(getNewFoldersList()));
		changes.addAll(Arrays.asList(getMovedFoldersList()));
		changes.addAll(Arrays.asList(getNewFunctionsList()));
		changes.addAll(Arrays.asList(getChangedMovedFunctionsList()));
		changes.addAll(Arrays.asList(getChangedFunctionsList()));
		changes.addAll(Arrays.asList(getMovedFunctionsList()));*/
		changes.addAll(Arrays.asList(getChangedCoreFilesList()));
		return changes.toArray(new String[changes.size()]);
	}
	
	public String[] getCoreChanges() {
		ArrayList<String> changes = new ArrayList<String>(0);
		changes.addAll(Arrays.asList(getChangedCoreFilesList()));
		return changes.toArray(new String[changes.size()]);
	}
	
	/*public String[] getLibraryChanges() {
		ArrayList<String> changes = new ArrayList<String>(0);
		changes.addAll(Arrays.asList(getNewFoldersList()));
		changes.addAll(Arrays.asList(getMovedFoldersList()));
		changes.addAll(Arrays.asList(getNewFunctionsList()));
		changes.addAll(Arrays.asList(getChangedMovedFunctionsList()));
		changes.addAll(Arrays.asList(getChangedFunctionsList()));
		changes.addAll(Arrays.asList(getMovedFunctionsList()));
		return changes.toArray(new String[changes.size()]);
	}*/
	
	public boolean needUpdate() {
		String[] changes = getChanges();
		return changes.length > 0;
	}
	
}
