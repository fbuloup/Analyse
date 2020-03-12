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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class CheckFilesVersionsTask extends Task {
	
	private String distantFileURLName;
	private String localFileURLName;

	public void setDistantFileURLName(String distantFileURLName) {
		this.distantFileURLName = distantFileURLName;
	}

	public void setLocalFileURLName(String localFileURLName) {
		this.localFileURLName = localFileURLName;
	}

	@Override
	public void execute() throws BuildException {
		try {
			URL distantURL = new URL(distantFileURLName);
			URL localURL = new URL(localFileURLName);
			BufferedReader distantFile = new BufferedReader(new InputStreamReader(distantURL.openConnection().getInputStream()));
			BufferedReader localFile = new BufferedReader(new InputStreamReader(localURL.openConnection().getInputStream()));
			String distantDateVersionString = distantFile.readLine();
			String localeDateVersionString = localFile.readLine();
			localFile.close();
			distantFile.close();
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");

			System.out.println("Distant version : " + distantDateVersionString);
			System.out.println("Local version : " + localeDateVersionString);
			
			Date distantDateVersion = formatter.parse(distantDateVersionString);
			Date localeDateVersion = formatter.parse(localeDateVersionString);
			
			int value = distantDateVersion.compareTo(localeDateVersion);
			
			if(value == 0)
				System.out.println("Don't need to commit anything");
			else if(value > 0)
				System.out.println("Distant release is newer than local one ! Please update local version");
			else System.out.println("Distant release is older than local one ! Please commit this new version to server");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		
	}

}
