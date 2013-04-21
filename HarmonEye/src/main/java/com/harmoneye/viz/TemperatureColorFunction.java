package com.harmoneye.viz;

import java.awt.Color;

public class TemperatureColorFunction implements ColorFunction {

	public Color toColor(float value) {
		float hue = (1.8f - value) % 1.0f;
		return Color.getHSBColor(hue, 0.25f + 0.75f * value, 0.25f + 0.75f * value);
	}

}
