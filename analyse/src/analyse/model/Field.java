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


public class Field {
	
	private Signal signal;
	private String label;
	private double[] values;
	
	public Field(Signal signal, String label, double[] values) {
		this.signal = signal;
		this.label = label;
		this.values = values;
	}

	public Signal getSignal() {
		return signal;
	}

	public String getLabel() {
		return label;
	}

	public double[] getValues() {
		return values;
	}
	
}
