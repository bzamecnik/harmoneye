package com.harmoneye.math.filter;

import com.harmoneye.math.Norm;

public class Normalizer implements Filter {

	private Norm norm;
	private double threshold;

	public Normalizer(Norm norm, double threshold) {
		this.norm = norm;
		this.threshold = threshold;
	}

	public double[] filter(double[] values) {
		double normValue = norm.norm(values);
		double normInv = (normValue > threshold) ? 1 / normValue : 0;
		for (int i = 0; i < values.length; i++) {
			values[i] *= normInv;
		}
		return values;
	}

}
