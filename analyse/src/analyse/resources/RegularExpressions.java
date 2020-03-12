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
package analyse.resources;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import analyse.Log;


public class RegularExpressions implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private List<String> regExpression = new ArrayList<String>(0);
		private List<String> regExpressionLabel = new ArrayList<String>(0);
		
		public RegularExpressions() {
			addRegularExpression("^[+]?0*[0-9]+$", Messages.getString("RegularExpressions.PositiveOrNullIntLabel")); //$NON-NLS-1$ //$NON-NLS-2$
			addRegularExpression("^[+]?0*[1-9]+[0-9]*$", Messages.getString("RegularExpressions.PositiveIntLabel")); //$NON-NLS-1$ //$NON-NLS-2$
			addRegularExpression("^[+-]?0*[0-9]+$", Messages.getString("RegularExpressions.AllIntLabel")); //$NON-NLS-1$ //$NON-NLS-2$
			addRegularExpression("^[+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$", Messages.getString("RegularExpressions.PositiveOrNullFloatLabel")); //$NON-NLS-1$ //$NON-NLS-2$
			addRegularExpression("(^[+]?\\d*\\.?\\d*[1-9]+\\d*([eE][-+]?[0-9]+)?$)|(^[+]?[1-9]+\\d*\\.\\d*([eE][-+]?[0-9]+)?$)", Messages.getString("RegularExpressions.PositiveFloatLabel")); //$NON-NLS-1$ //$NON-NLS-2$
			addRegularExpression("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$", Messages.getString("RegularExpressions.AllFloatLabel")); //$NON-NLS-1$ //$NON-NLS-2$
			addRegularExpression("^(true|false)$", Messages.getString("RegularExpressions.BooleanLabel")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		public void addRegularExpression(String regExp, String regExpLabel) {
			for (int i = 0; i < regExpressionLabel.size(); i++) {
				String label = regExpressionLabel.get(i);
				if(label.equals(regExpLabel)) {
					MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(), Messages.getString("RegularExpressions.DialogErrorTitle"), ImagesUtils.getImage(IImagesKeys.ANALYSE_ICON), //$NON-NLS-1$
																Messages.getString("RegularExpressions.DialogErrorText"), MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL}, 0);  //$NON-NLS-1$
			        dialog.open();
					return;
				}
			}
			regExpression.add(regExp);
			regExpressionLabel.add(regExpLabel);
		}
		
		private void removeRegularExpression(int n) {
			regExpression.remove(n);
			regExpressionLabel.remove(n);
		}
		
		public void removeRegularExpression(String regularExpressionLabel) {
			for (int i = 0; i < regExpressionLabel.size(); i++) {
				String label = regExpressionLabel.get(i);
				if(label.equals(regularExpressionLabel)) {
					removeRegularExpression(i);
					return;
				}
			}
		}
		
		public String getRegularExpression(int index) {
			return regExpression.get(index);
		}
		
		public String getRegularExpressionLabel(int index) {
			return regExpressionLabel.get(index);
		}
		
		public String getRegularExpressionLabel(String regularExpression) {
			for (int i = 0; i < regExpression.size(); i++) {
				if(regExpression.get(i).equals(regularExpression)) return regExpressionLabel.get(i);
			}
			return ""; //$NON-NLS-1$
		}
		
		public String[] getContent() {
			String[] content = new String[regExpression.size()];
			for (int i = 0; i < content.length; i++) content[i] = regExpressionLabel.get(i) + " [" + regExpression.get(i) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			return content;
		}
		
		public static RegularExpressions readRegularExpressions() {
			RegularExpressions regExp;
			try {
				try {
					ObjectInputStream in = new ObjectInputStream(new FileInputStream("regexp.ser")); //$NON-NLS-1$
					regExp = (RegularExpressions) in.readObject();
					in.close();
					return regExp;
				} catch (ClassNotFoundException e) {
					Log.logErrorMessage(e);
				}
			} catch (FileNotFoundException e) {
				regExp = new RegularExpressions();
				return regExp;
			} catch (IOException e) {
				Log.logErrorMessage(e);
			}
			return null;
		}
		
		public static void saveRegularExpressions(RegularExpressions regExp) {
			try {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("regexp.ser")); //$NON-NLS-1$
				out.writeObject(regExp);
				out.flush();
				out.close();
			} catch (FileNotFoundException e) {
				Log.logErrorMessage(e);
			} catch (IOException e) {
				Log.logErrorMessage(e);
			}
		}
		
	}
