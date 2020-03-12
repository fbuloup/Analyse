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
package analyse.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import analyse.preferences.AnalysePreferences;

public class DataNote implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Date creationDate;
	private Date modificationDate;
	private String text;
	
	public DataNote() {
		creationDate = new Date();
		modificationDate = creationDate;
		text = "";
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public Date getModificationDate() {
		return modificationDate;
	}
	
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public String getModificationDateString() {
		DateFormat dateFormat = SimpleDateFormat.getDateInstance();
		if(AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.LANGUAGE).equals(AnalysePreferences.FRENCH_LANGUAGE))
			dateFormat = new SimpleDateFormat("EEEE, d MMM yyyy HH:mm:ss", Locale.FRENCH);
		if(AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.LANGUAGE).equals(AnalysePreferences.ENGLISH_LANGUAGE))
			dateFormat = new SimpleDateFormat("EEEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
		return dateFormat.format(modificationDate); 
	}

	public String getCreationDateString() {
		DateFormat dateFormat = SimpleDateFormat.getDateInstance();
		if(AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.LANGUAGE).equals(AnalysePreferences.FRENCH_LANGUAGE))
			dateFormat = new SimpleDateFormat("EEEE, d MMM yyyy HH:mm:ss", Locale.FRENCH);
		if(AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.LANGUAGE).equals(AnalysePreferences.ENGLISH_LANGUAGE))
			dateFormat = new SimpleDateFormat("EEEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
		return dateFormat.format(creationDate); 
	}

}
