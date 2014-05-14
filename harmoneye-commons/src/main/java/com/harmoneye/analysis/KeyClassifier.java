package com.harmoneye.analysis;

import com.harmoneye.math.Modulo;
import com.harmoneye.math.filter.ExpSmoother;

// TODO: extract smoothing as a decorater
// TODO: refactor with com.harmoneye.music.KeyDetector and
// com.harmoneye.music.ChromaKeyDetector

public class KeyClassifier {

	private static double[] WEIGHTS = new double[] { 2.8, -1, 1, -1, 1, 1,
		-1, 1.5, -1, 1, -1, 1 };
	private static int OCTAVE_SIZE = 12;

	private double[] correlation = new double[OCTAVE_SIZE];
	
	private ExpSmoother smoother = new ExpSmoother(OCTAVE_SIZE, 0.0001);

	/**
	 * Given a vector of pitch class intensities try to get the most likely key.
	 * 
	 * @param pitchClasses vector of intensities of pitch classes
	 * @return root pitch class of the most likely key
	 */
	public int classifyKey(double[] pitchClasses) {
		if (pitchClasses.length != OCTAVE_SIZE) {
			throw new IllegalArgumentException("wrong octave size: "
				+ pitchClasses.length);
		}
		correlation = crossCorrelate(WEIGHTS,
			pitchClasses,
			correlation);
		
		correlation = smoother.smooth(correlation);
		
//		System.out.println("correlation: " + Arrays.toString(correlation));
		return findMax(correlation);
	}

	private double[] crossCorrelate(double[] one, double[] other,
		double[] result) {
		int n = result.length;
		for (int shift = 0; shift < n; shift++) {
			double sum = 0;
			for (int i = 0; i < n; i++) {
				sum += one[i] * other[Modulo.modulo(i + shift, n)];
			}
			result[shift] = sum;
		}
		return result;
	}

	/**
	 * Finds the index of the maximum value. If there are multiple equal maxima
	 * it finds the minimal index.
	 */
	private int findMax(double[] values) {
		int maxIndex = 0;
		double max = Double.MIN_VALUE;
		for (int i = 0; i < values.length; i++) {
			if (values[i] > max) {
				max = values[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}
}
