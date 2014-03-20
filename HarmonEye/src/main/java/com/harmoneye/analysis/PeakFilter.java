package com.harmoneye.analysis;

import org.apache.commons.math3.util.FastMath;

public class PeakFilter {
	double[] data;
	double weight;

	public PeakFilter(int size, double weight) {
		data = new double[size];
		this.weight = weight;
	}

	public double[] smooth(double[] currentFrame) {
		assert data.length == currentFrame.length;

		for (int i = 0; i < data.length; i++) {
			data[i] = FastMath.max(weight * data[i], currentFrame[i]);
		}
		return data;
	}
}
