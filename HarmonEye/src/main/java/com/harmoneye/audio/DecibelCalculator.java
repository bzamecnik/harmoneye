package com.harmoneye.audio;

import org.apache.commons.math3.util.FastMath;

public class DecibelCalculator {

	private double dbThreshold;
	private double dbThresholdInv;

	public DecibelCalculator(int bitsPerSample) {
		dbThreshold = -(20 * FastMath.log10(2 << (bitsPerSample - 1)));
		dbThresholdInv = 1.0 / dbThreshold;
	}

	public double amplitudeToDb(double amplitude) {
		// TODO: fix for amplitude == 0
		
		// Since reference amplitude is 1, this code is implied:
		// double referenceAmplitude = 1;
		// amplitude /= referenceAmplitude;
		double amplitudeDb = 20 * FastMath.log10(amplitude);
		if (amplitudeDb < dbThreshold) {
			amplitudeDb = dbThreshold;
		}
		return amplitudeDb;
	}

	/** rescale: [DB_THRESHOLD; 0] -> [-1; 0] -> [0; 1] */
	public double rescale(double amplitudeDb) {
		return 1 - (amplitudeDb * dbThresholdInv);
	}
}
