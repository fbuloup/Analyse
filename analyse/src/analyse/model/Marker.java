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
package analyse.model;

public class Marker {
	
	private double x;
	private double y;
	private MarkersGroup markersGroup;
	private int trialNumber;
	
	public Marker(MarkersGroup markersGroup, int trialNumber, double x, double y) {
		this.markersGroup = markersGroup;
		this.trialNumber = trialNumber;
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public String getLabel() {
		return markersGroup.getLabel() + " - Trial n°" + trialNumber + " - [" + x + " ; " + y + "]";
	}

	public MarkersGroup getMarkersGroup() {
		return markersGroup;
	}

	public int getTrialNumber() {
		return trialNumber;
	}
	
}
