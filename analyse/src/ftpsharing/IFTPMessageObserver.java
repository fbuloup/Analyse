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
package ftpsharing;

public interface IFTPMessageObserver {
	
	public static int NORMAL = 1;
	public static int ERROR = 2;
	public static int WARNING = 3;
	
	public static int USER_LOGGEDIN = 10;
	public static int USER_DISCONNECTED = 11;
	
	void update(int messageID, String message);

}
