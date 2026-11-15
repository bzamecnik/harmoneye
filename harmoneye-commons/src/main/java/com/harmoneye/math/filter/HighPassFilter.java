package com.harmoneye.math.filter;

import org.apache.commons.math3.util.FastMath;

public class HighPassFilter implements Filter {

	private BoxFilter boxFilter;

	public HighPassFilter(int size) {
		boxFilter = new BoxFilter(size);
	}

	public double[] filter(double[] values) {
		double[] lowPass = boxFilter.filter(values);
		for (int i = 0; i < values.length; i++) {
			values[i] = FastMath.max(values[i] - lowPass[i], 0);
		}
		return values;
	}
}
