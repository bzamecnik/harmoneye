package com.harmoneye.viz;

import java.awt.Color;

public interface ColorFunction {
	/**
	 * Converts a value into a color.
	 * 
	 * @param value from interval [0.0; 1.0]
	 * @return
	 */
	Color toColor(float value);
}
