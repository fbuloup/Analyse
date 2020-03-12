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

import java.util.Date;
import java.util.Random;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorsUtils {

	private static ColorRegistry colorsRegistry = null;
	
	private static final RGB WHITE_RGB = new RGB(255, 255, 255);
	public static final String WHITE = "WHITE"; //$NON-NLS-1$
	
	private static final RGB COMMENT_RGB = new RGB(0, 128, 0);
	public static final String COMMENT = "COMMENT"; //$NON-NLS-1$
	
	private static final RGB BLACK_RGB = new RGB(0, 0, 0);
	public static final String BLACK = "BLACK"; //$NON-NLS-1$
	
	private static final RGB KEYWORD_RGB = new RGB(140, 32, 107);
	public static final String KEYWORD = "KEYWORD"; //$NON-NLS-1$
	
	private static final RGB NUMBER_RGB = new RGB(255, 0, 255);
	public static final String NUMBER = "NUMBER"; //$NON-NLS-1$
	
	private static final RGB STRING_RGB = new RGB(255, 0, 0);
	public static final String STRING = "STRING"; //$NON-NLS-1$
	
	private static final RGB COLOR_TITLE_BACKGROUND_RGB = Display.getDefault().getSystemColor(SWT.COLOR_TITLE_BACKGROUND).getRGB();
	public static final String COLOR_TITLE_BACKGROUND = "COLOR_TITLE_BACKGROUND"; //$NON-NLS-1$

	private static final RGB COLOR_TITLE_BACKGROUND_GRADIENT_RGB = Display.getDefault().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT).getRGB();
	public static final String COLOR_TITLE_BACKGROUND_GRADIENT = "COLOR_TITLE_BACKGROUND_GRADIENT"; //$NON-NLS-1$

	private static final RGB COLOR_WIDGET_BACKGROUND_RGB = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB();
	public static final String COLOR_WIDGET_BACKGROUND = "COLOR_WIDGET_BACKGROUND"; //$NON-NLS-1$
	
	public static final RGB ANALYSE_KEYWORD_RGB = new RGB(0, 0, 192);
	public static final String ANALYSE_KEYWORD = "ANALYSE_KEYWORD"; //$NON-NLS-1$

	public static final RGB SPECIAL_CHARS_RGB = new RGB(0xce, 0x5c, 0x00);
	public static final String SPECIAL_CHARS_KEYWORD = "SPECIAL_CHARS_KEYWORD"; //$NON-NLS-1$
	
	private static Random randomGenerator;
	
	private static RGB[] RGB_COLORS = new RGB[]{
												new RGB(0x33, 0x00, 0x00),
												new RGB(0x33, 0x33, 0x99),
												new RGB(0x33, 0x33, 0x00),
												new RGB(0x33, 0x33, 0x99),
												new RGB(0x33, 0x66, 0x00),
												new RGB(0x33, 0x66, 0x99),
												new RGB(0x33, 0x99, 0x00),
												new RGB(0x33, 0x99, 0x99),
												new RGB(0x33, 0xcc, 0x00),
												new RGB(0x33, 0xcc, 0x99),
												new RGB(0x33, 0xFF, 0x00),
												
												new RGB(0x99, 0x00, 0x00),
												new RGB(0x99, 0x00, 0x99),
												new RGB(0x99, 0x33, 0x00),
												new RGB(0x99, 0x33, 0x99),
												new RGB(0x99, 0x66, 0x00),
												new RGB(0x99, 0x66, 0x99),
												new RGB(0x99, 0xcc, 0x00),
												new RGB(0x99, 0xcc, 0x99),												
												new RGB(0x99, 0x99, 0x00),
												new RGB(0x99, 0x99, 0x99),												
												new RGB(0x99, 0xff, 0x00),
												
												new RGB(0xff, 0x00, 0x00),
												new RGB(0xff, 0x00, 0x99),
												new RGB(0xff, 0x33, 0x00),
												new RGB(0xff, 0x33, 0x99),
												new RGB(0xff, 0x66, 0x00),
												new RGB(0xff, 0x66, 0x99),
												new RGB(0xff, 0x99, 0x00),
												new RGB(0xff, 0x99, 0x99),
												new RGB(0xff, 0xcc, 0x00),
												new RGB(0xff, 0xcc, 0x99),
												new RGB(0xff, 0xff, 0x00),
																			};
	
	private static int MAX_COLORS = RGB_COLORS.length;
	
	static {
		if(colorsRegistry == null) {		
			randomGenerator  = new Random((new Date()).getTime());
			colorsRegistry = JFaceResources.getColorRegistry();	
			colorsRegistry.put(WHITE, WHITE_RGB);
			colorsRegistry.put(COMMENT, COMMENT_RGB);
			colorsRegistry.put(BLACK, BLACK_RGB);
			colorsRegistry.put(KEYWORD, KEYWORD_RGB);
			colorsRegistry.put(NUMBER, NUMBER_RGB);
			colorsRegistry.put(STRING, STRING_RGB);
			colorsRegistry.put(ANALYSE_KEYWORD, ANALYSE_KEYWORD_RGB);
			colorsRegistry.put(SPECIAL_CHARS_KEYWORD, SPECIAL_CHARS_RGB);
			
			colorsRegistry.put(COLOR_TITLE_BACKGROUND,COLOR_TITLE_BACKGROUND_RGB);
			colorsRegistry.put(COLOR_TITLE_BACKGROUND_GRADIENT,COLOR_TITLE_BACKGROUND_GRADIENT_RGB);
			colorsRegistry.put(COLOR_WIDGET_BACKGROUND,COLOR_WIDGET_BACKGROUND_RGB);
			colorsRegistry.put(COLOR_WIDGET_BACKGROUND,COLOR_WIDGET_BACKGROUND_RGB);
			
		}
	}
	
	public static Color getColor(String colorName) {
		return colorsRegistry.get(colorName);
	}
	
	public static Color getRandomColor() {
		String key = null;
		if(colorsRegistry.getKeySet().size() <= MAX_COLORS) {
			for (int i = 0; i < MAX_COLORS; i++) {
				key  = String.valueOf(RGB_COLORS[i].red) + "_" + String.valueOf(RGB_COLORS[i].green) + "_" + String.valueOf(RGB_COLORS[i].blue);
				RGB rgb = new RGB(RGB_COLORS[i].red, RGB_COLORS[i].green, RGB_COLORS[i].blue);
				colorsRegistry.put(key, rgb);
			}
		} else {
			int numColor = randomGenerator.nextInt(MAX_COLORS);
			key = (String) colorsRegistry.getKeySet().toArray(new String[colorsRegistry.getKeySet().size()])[numColor];
		}
		return getColor(key);
	}
	
}
