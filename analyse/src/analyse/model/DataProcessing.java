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
import java.util.Iterator;
import java.util.Vector;

public class DataProcessing implements Serializable {

	private static final long serialVersionUID = 1L;
	private Vector<Function> usedFunctionsList = new Vector<Function>(0);
	
	public Vector<Function> getFunctionsList() {
		return usedFunctionsList;
	}

	public void addFunction(String functionName) {
		Function function = new Function(functionName);
		function.initializeFunction();
		usedFunctionsList.add(function);
	}
	
	public void removeFunctions(Object[] functions) {
		for (int i = 0; i < functions.length; i++) {
			usedFunctionsList.remove(functions[i]);	
		}		
	}

	public void moveUp(Function function) {
		int index = usedFunctionsList.indexOf(function);		
		if(index > 0) {
			usedFunctionsList.remove(function);
			usedFunctionsList.insertElementAt(function, index - 1);
		}		
	}

	public void moveDown(Function function) {
		int index = usedFunctionsList.indexOf(function);		
		if(index < usedFunctionsList.size() - 1) {
			usedFunctionsList.remove(function);
			usedFunctionsList.insertElementAt(function, index + 1);
		}	
	}
	
	public void moveUp(Object[] functions) {
		for (int i = 0; i < functions.length; i++) moveUp((Function) functions[i]);
	}

	public void moveDown(Object[] functions) {
		for (int i = functions.length - 1; i > -1 ; i--) {
			moveDown((Function) functions[i]);
		}
	}
	
	public Function[] getFunctions() {
		return usedFunctionsList.toArray(new Function[usedFunctionsList.size()]);
	}

	public void clear() {
		usedFunctionsList.clear();
	}

	public void replace(String replace, String by) {
		for (@SuppressWarnings("rawtypes")
		Iterator iterator = usedFunctionsList.iterator(); iterator.hasNext();) {
			Function function = (Function) iterator.next();
			function.replace(replace,by);
		}
	}

}
