package com.harmoneye.music;

import java.util.List;

import com.harmoneye.music.chord.TimedChordLabel;

public class KeyDetector {

	// diatonic tones have weight 1, others -1
	// tonic (0) and dominant (7) have higher weights
	private static final double[] WEIGHTS = { 2.5, -1, 1, -1, 1, 1, -1, 1.8,
		-1, 1, -1, 1 };

	private double relativeExcerptLength = 0.5;

	public int findTonic(List<TimedChordLabel> labels) {
		double totalTime = labels.get(labels.size() - 1).getEndTime();

		double[] keyWeightSums = new double[12];
		int size = labels.size();

		int searchedSize = (int) Math.round(size * relativeExcerptLength);
		double excerptTimeInv = 1.0 / (totalTime * relativeExcerptLength);
		for (int i = 0; i < searchedSize; i++) {
			TimedChordLabel label = labels.get(i);
			List<Integer> tones = label.getChordLabel().getTones();
			if (tones.isEmpty()) {
				continue;
			}
			double timeWeight = (label.getEndTime() - label.getStartTime())
				* excerptTimeInv;
			for (int tonic = 0; tonic < 12; tonic++) {
				double weight = getWeight(tones, tonic);
				keyWeightSums[tonic] += timeWeight * weight;
			}
		}

		return findMax(keyWeightSums);
	}

	private double getWeight(List<Integer> tones, int tonic) {
		double sum = 0;
		for (Integer tone : tones) {
			int t = (tone - tonic + 12) % 12;
			sum += WEIGHTS[t];
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
