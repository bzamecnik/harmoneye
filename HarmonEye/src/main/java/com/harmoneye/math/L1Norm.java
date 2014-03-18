package com.harmoneye.math;

import org.apache.commons.math3.util.FastMath;

public class L1Norm implements Norm {

	public double norm(double[] values) {
		double norm = 0;
		int length = values.length;
		for (int i = 0; i < length; i++) {
			double value = values[i];
			norm += FastMath.abs(value);
		}
		return norm;
	}
}
