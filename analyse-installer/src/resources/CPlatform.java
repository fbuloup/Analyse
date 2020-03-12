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
package resources;

public class CPlatform {

	private static String osName = java.lang.System.getProperty("os.name"); //$NON-NLS-1$
	private static String osArch = java.lang.System.getProperty("os.arch"); //$NON-NLS-1$

	public static String getOsName() {
		return osName;
	}

	public static void setOsName(String osName) {
		CPlatform.osName = osName;
	}

	public static String getOsArch() {
		return osArch;
	}

	public static void setOsArch(String osArch) {
		CPlatform.osArch = osArch;
	}
	
	public static boolean isWindows() {
		return osName.contains("Windows"); //$NON-NLS-1$
	}
	
	public static boolean isLinux() {
		return osName.contains("Linux"); //$NON-NLS-1$
	}
	
	public static boolean isMacOSX() {
		return osName.contains("Mac"); //$NON-NLS-1$
	}
	
	public static boolean isX86Arch() {
		return osArch.contains("i386"); //$NON-NLS-1$
	}
	
	public static boolean isPPCArch() {
		return osArch.contains("ppc"); //$NON-NLS-1$
	}
}
