package com.harmoneye.viz;

import java.awt.Color;

public class TemperatureColorFunction implements ColorFunction {

	public Color toColor(float value) {
		float hue = (1.8f - value) % 1.0f;
		//float saturation = 0.75f * value + 0.25f * value * value;
		float saturation = value > 0.5f ? value : 0.05f + value * value;
		float brightness = 0.25f + 0.75f * value;
		return Color.getHSBColor(hue, saturation, brightness);
	}

}
