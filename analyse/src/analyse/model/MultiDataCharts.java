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
import java.util.ArrayList;

public class MultiDataCharts  implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<DataChart> dataCharts = new ArrayList<DataChart>(0);
	private int numberOfColumns;
	
	public DataChart[] getDataCharts() {
		return dataCharts.toArray(new DataChart[dataCharts.size()]);
	}

	public int getNumberOfColumns() {
		return numberOfColumns;
	}

	public void setNumberOfColumns(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
	}
	
	public void addDataChart(DataChart dataChart) {
		dataCharts.add(dataChart);
	}

	public void remove(DataChart dataChart) {
		dataCharts.remove(dataChart);
	}

	public void clear() {
		dataCharts.clear();
	}

	
}
