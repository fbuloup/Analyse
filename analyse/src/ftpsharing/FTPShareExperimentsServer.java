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
package ftpsharing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.eclipse.swt.widgets.Display;

import analyse.resources.Messages;

public class FTPShareExperimentsServer extends DefaultFtplet {
	
	public static String SHARED_EXPERIMENTS_FILE_NAME = "sharedExperimentsList.properties";

	private static HashSet<String> connectedUsers = new HashSet<String>(0);
	
	private static FtpServer server;
	private static boolean isStarted = false;
	
	private static ArrayList<IFTPMessageObserver> observers = new ArrayList<IFTPMessageObserver>(0);
	private static FTPShareExperimentsServer ftpShareExperimentsServer;
	
	private static Listener ftpListener;

	private static void init() throws FtpException {
		if(ftpShareExperimentsServer == null) ftpShareExperimentsServer = new FTPShareExperimentsServer(); 
	}
	
	private FTPShareExperimentsServer() throws FtpException {
		
		File usersFile = new File("./users.properties");
		if(!usersFile.exists()) throw new FtpException(Messages.getString("FTPShareExperimentsServer.ErrorUsersFile"));
		
		FtpServerFactory serverFactory = new FtpServerFactory();
		
		
		
		//User Manager
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		userManagerFactory.setFile(usersFile);
		userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
		UserManager userManager = userManagerFactory.createUserManager();
		serverFactory.setUserManager(userManager);

		//FTP listener
		ListenerFactory factory = new ListenerFactory();
		// set the port of the listener
		factory.setPort(2221);
		// replace the default listener
		ftpListener = factory.createListener();
		serverFactory.addListener("default", ftpListener);
		
		//FTPlet
		HashMap<String, Ftplet> ftplets = new HashMap<String, Ftplet>(0);
		ftplets.put("DEFAULT", this);

		serverFactory.setFtplets(ftplets);
		server = serverFactory.createServer();
		
	}
	
	public static boolean isStarted() {
		return isStarted;
	}
	
	public static void stop() {
		server.stop();
		ftpShareExperimentsServer = null;
		isStarted = false;
	}
	
	public static void addObserver(IFTPMessageObserver observer) {
		observers.add(observer);
	}
	
	public static void removeObserver(IFTPMessageObserver observer) {
		observers.remove(observer);
	}
	
	private static void notifyObservers(final int messageID, final String message) {
		Display.getDefault().syncExec( new Runnable() {
			public void run() {
				for (int i = 0; i < observers.size(); i++) {
					observers.get(i).update(messageID, message);
				}
			}
		});
	}

	public static void startServer() throws FtpException {
		init();
		server.start();
		isStarted = true;
	}

    @Override
    public FtpletResult onLogin(FtpSession session, FtpRequest request) throws FtpException, IOException {
    	String userName = session.getUser().getName();
		if(connectedUsers.contains(userName)) return FtpletResult.SKIP;
		connectedUsers.add(userName);
		notifyObservers(IFTPMessageObserver.USER_LOGGEDIN, userName);
    	return  FtpletResult.DEFAULT;
    }	
	
	@Override
	public FtpletResult onDisconnect(FtpSession session) throws FtpException, IOException {
		String userName = session.getUser().getName();
		connectedUsers.remove(userName);
		notifyObservers(IFTPMessageObserver.USER_DISCONNECTED, userName);
    	return  FtpletResult.DEFAULT;
	}

	public static String[] getConnectedUser() {
		return connectedUsers.toArray(new String[connectedUsers.size()]);
	}

	public static void disconnectUsers(String[] users) {
		Iterator<FtpIoSession> sessions = ftpListener.getActiveSessions().iterator();
		while (sessions.hasNext()) {
			FtpIoSession ftpIoSession = (FtpIoSession) sessions.next();
			for (int i = 0; i < users.length; i++) {
				if(ftpIoSession.getUser().getName().equals(users[i])) {
					ftpIoSession.close(true);
					connectedUsers.remove(users[i]);
					notifyObservers(IFTPMessageObserver.USER_DISCONNECTED, users[i]);
				}
			}
			
		}
		
	}
	
}
