package com.harmoneye.math;

public class Mean {

	public static double arithmeticMean(double[] values) {
		double sum = 0;
		for (double amplitude : values) {
			sum += amplitude;
		}
		return sum / values.length;
	}

	/** Quadratic mean (aka root mean square). */
	public static double quadraticMean(double[] values) {
		double sum = 0;
		for (double amplitude : values) {
			sum += amplitude * amplitude;
		}
		return Math.sqrt(sum / values.length);
	}

	public static double geometricMean(double[] values) {
		double product = 1;
		for (double amplitude : values) {
			product *= amplitude;
		}
		return Math.pow(product, 1.0 / values.length);
	}

	public static double harmonicMean(double[] values) {
		double sum = 0;
		for (double amplitude : values) {
			sum += 1.0 / amplitude;
		}
		return values.length / sum;
	}
}
