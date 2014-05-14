package com.harmoneye.music;

import java.util.Arrays;

public class ChromaKeyDetector {

	private static final int OCTAVE_SIZE = 12;

	// diatonic tones have weight 1, others -1
	// tonic (0) and dominant (7) have higher weights
	private static final double[] WEIGHTS = { 2.5, -1, 1, -1, 1, 1, -1, 1.8,
		-1, 1, -1, 1 };

	private double relativeExcerptLength = 0.5;

	public int findTonic(double[][] chromagram) {
		double[] keyWeightSums = new double[12];
		int size = chromagram.length;

		int searchedSize = (int) Math.round(size * relativeExcerptLength);
		for (int i = 0; i < searchedSize; i++) {
			double[] chroma = chromagram[i];
			for (int tonic = 0; tonic < OCTAVE_SIZE; tonic++) {
				keyWeightSums[tonic] += getWeight(chroma, tonic);
			}
		}
		// System.out.print("[");
		// for (int i = 0; i < keyWeightSums.length; i++) {
		// System.out.print(keyWeightSums[i*7%12] + ", ");
		// }
		// System.out.println("]");

		return findMax(keyWeightSums);
	}

	private double getWeight(double[] chroma, int tonic) {
		double sum = 0;
		int length = chroma.length;
		for (int i = 0; i < length; i++) {
			int t = (i - tonic + OCTAVE_SIZE) % OCTAVE_SIZE;
			sum += chroma[i] * WEIGHTS[t];
		}
		return sum;
	}

	private int findMax(double[] values) {
		int length = values.length;
		double sum = Double.MIN_VALUE;
		int maxIndex = 0;
		for (int i = 0; i < length; i++) {
			double value = values[i];
			if (value > sum) {
				sum = value;
				maxIndex = i;
			}
		}
		return maxIndex;
	}
}
