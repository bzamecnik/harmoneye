package com.harmoneye.math.window;

import org.apache.commons.math3.util.FastMath;

public class HannWindow implements WindowFunction {

	private static final double TWO_PI = 2 * FastMath.PI;

	@Override
	public double value(double x) {
		return 0.5 * (1.0 - FastMath.cos(TWO_PI * x));
	}

}
