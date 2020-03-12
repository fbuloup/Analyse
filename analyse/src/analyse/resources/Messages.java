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
package analyse.resources;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import analyse.preferences.AnalysePreferences;


public final class Messages {
	
	private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$
	private static ResourceBundle messagesResource = null;

	private Messages() {
	}

	public static String getString(String key) {
		if(messagesResource == null) {
			Locale currentLocale;
			String languageValue = AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.LANGUAGE);
	        currentLocale = new Locale(languageValue.split("_")[0],languageValue.split("_")[1]); //$NON-NLS-1$ //$NON-NLS-2$
	        messagesResource = ResourceBundle.getBundle(BUNDLE_NAME,currentLocale);
		}
		try {
			return messagesResource.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
		
	}
}
