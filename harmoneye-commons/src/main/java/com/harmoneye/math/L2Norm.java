package com.harmoneye.math;

import org.apache.commons.math3.util.FastMath;

public class L2Norm implements Norm {

	public double norm(double[] values) {
		double norm = 0;
		int length = values.length;
		for (int i = 0; i < length; i++) {
			double value = values[i];
			norm += value * value;
		}
		return FastMath.sqrt(norm);
	}
}
