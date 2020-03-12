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
package mathengine;

import analyse.Log;
import analyse.preferences.AnalysePreferences;
import analyse.resources.CPlatform;
import analyse.resources.Messages;

public final class MathEngineFactory {
	
	IMathEngine mathEngine;
	
	private static MathEngineFactory mathEngineFactory = new MathEngineFactory();
	
	public static MathEngineFactory getInstance() {
		return mathEngineFactory;  
	}
	
	private void initMathEngine() {
		if(CPlatform.isWindows()) {
			String mathEngineName = AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.MATH_ENGINE);
			if(mathEngineName.equals(AnalysePreferences.MATLAB_ENGINE))	mathEngine = new WindowsMatlabEngine(Log.getInstance());
			else mathEngine = new OctaveEngine(Log.getInstance());
		}
		if(CPlatform.isLinux() && CPlatform.isPPCArch()) {
			String mathEngineName = AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.MATH_ENGINE);
			if(mathEngineName.equals(AnalysePreferences.MATLAB_ENGINE))	Log.logErrorMessage(Messages.getString("MathEngineFactory.NoMatlabEngineForPPC"));
			else mathEngine = new OctaveEngine(Log.getInstance());
		}
		if(CPlatform.isLinux() && CPlatform.isX86Arch()) {
			String mathEngineName = AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.MATH_ENGINE);
			if(mathEngineName.equals(AnalysePreferences.MATLAB_ENGINE))	mathEngine = new UnixMatlabEngine(Log.getInstance());
			else mathEngine = new OctaveEngine(Log.getInstance());
		}
		if(CPlatform.isMacOSX()) {
			String mathEngineName = AnalysePreferences.getPreferenceStore().getString(AnalysePreferences.MATH_ENGINE);
			if(mathEngineName.equals(AnalysePreferences.MATLAB_ENGINE))	mathEngine = new UnixMatlabEngine(Log.getInstance());
			else mathEngine = new OctaveEngine(Log.getInstance());
		}
	}
	
	private MathEngineFactory() {
		initMathEngine();
	}
	
	public IMathEngine getMathEngine() {
		if(mathEngine == null) initMathEngine(); 
		return mathEngine;
	}
	
}
