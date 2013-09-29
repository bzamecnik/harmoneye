package com.harmoneye.cqt.window;

public class HammingWindow implements WindowFunction {

	private static final double TWO_PI = 2 * Math.PI;

	@Override
	public double value(double x) {
		return 0.54 - 0.46 * Math.cos(TWO_PI * x);
	}

}
