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

public interface IResourceObserver {

	public static int DELETED = 0;
	public static int RENAMED = 1;
	public static int COPIED = 3;
	public static int LOADED = 4;
	public static int SAVED = 5;
	public static int SELECTION_CHANGED = 6;
	public static int MARKERS_GROUP_SYMBOL_CHANGED = 7;
	
	public static int EXPERIMENT_CREATED = 10;
	public static int SUBJECT_CREATED = 20;
	public static int FOLDER_CREATED = 30;
	public static int CHART_CREATED = 40;
	public static int PROCESSING_CREATED = 50;
	public static int NOTE_CREATED = 170;

	public static int FIELD_DELETED = 60;
	public static int MARKERS_GROUP_DELETED = 70;
	public static int MARKER_DELETED = 80;
	public static int EVENT_DELETED = 90;
	
	public static int CHANNEL_DELETED = 100;
	public static int CATEGORY_CREATED = 110;
	
	public static int MARKER_ADDED = 120;
	public static int MARKER_LABEL_ADDED = 130;
	
	public static int REFACTORED = 140;

	public static int PROCESS_RUN = 150;
	
	public static int MATH_ENGINE_STOPPED = 160;
	
	public static int SAMPLE_FREQUENCY_CHANGED = 170;
	
	
	void update(int message, IResource[] resources);
	
}
