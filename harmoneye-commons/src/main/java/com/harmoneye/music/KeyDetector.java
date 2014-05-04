package com.harmoneye.music;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.harmoneye.music.chord.TimedChordLabel;

public class KeyDetector {

	private static final Set<Integer> DIATONIC_TONES = new HashSet<Integer>(
		Arrays.asList(0, 2, 4, 5, 7, 9, 11));

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
		// System.out.println(Arrays.toString(keyIndexSums));
		// for(int i = 0; i < 12; i++) {
		// System.out.print(keyIndexSums[(i*7)%12] + ", ");
		// }
		// System.out.println();

		return findMinIndex(keyWeightSums);
	}

	private int findMinIndex(double[] values) {
		int minIndex = 0;
		double sum = Double.MAX_VALUE;
		for (int i = 0; i < 12; i++) {
			if (values[i] < sum) {
				sum = values[i];
				minIndex = i;
			}
		}
		return minIndex;
	}

	private double getWeight(List<Integer> tones, int tonic) {
		// TODO: try to make weights like a sine
		double sum = 0;
		for (Integer tone : tones) {
			int t = (tone - tonic + 12) % 12;
			double weight = (DIATONIC_TONES.contains(t)) ? -1 : 1;
			if (t == 0) {
				weight *= 2.5;
			} else if (t == 7) {
				weight *= 1.8;
			}
			sum += weight;
		}
		return sum;
	}
}
