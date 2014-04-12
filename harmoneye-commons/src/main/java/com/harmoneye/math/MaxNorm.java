package com.harmoneye.math;

import org.apache.commons.math3.util.FastMath;

public class MaxNorm implements Norm {

	@Override
	public double norm(double[] values) {
		double norm = Double.MIN_VALUE;
		for (int i = 0; i < values.length; i++) {
			double value = FastMath.abs(values[i]);
			norm = FastMath.max(value, norm);
		}
		return norm;
	}

}
