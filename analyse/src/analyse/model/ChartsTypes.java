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

public final class ChartsTypes {
	
	public static String  TIME_CHART_ID_STRING = "Time chart";
	public static String  XY_CHART_ID_STRING = "XY chart";
	public static String  XYZ_CHART_ID_STRING = "XYZ chart";
	public static String  PIE_CHART_ID_STRING = "Pie chart";
	public static String  BAR_CHART_ID_STRING = "Bar chart";
	public static String MULTI_CHARTS_ID_STRING = "Mutli charts";
	
	
	public static String[] getChartsTypes() {
		return new String[]{TIME_CHART_ID_STRING, XY_CHART_ID_STRING, XYZ_CHART_ID_STRING, MULTI_CHARTS_ID_STRING};//,PIE_CHART_ID_STRING,BAR_CHART_ID_STRING}; 
	}
	
}
