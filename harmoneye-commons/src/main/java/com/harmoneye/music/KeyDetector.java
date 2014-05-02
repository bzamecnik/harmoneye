package com.harmoneye.music;

import java.util.HashSet;
import java.util.List;

import com.harmoneye.music.chord.TimedChordLabel;

public class KeyDetector {

	private KeyAwarePitchClassSetIndexer chordIndexer = new KeyAwarePitchClassSetIndexer();

	private double relativeExcerptLength = 1;//0.25;

	public int findTonic(List<TimedChordLabel> labels) {
		double totalTime = labels.get(labels.size() - 1).getEndTime();

		double[] keyIndexSums = new double[12];
		int size = labels.size();
		// try to find the first key in case of later modulations

		int searchedSize = (int) Math.round(size * relativeExcerptLength);
		double excerptTimeInv = 1.0 / (totalTime * relativeExcerptLength);
		for (int i = 0; i < searchedSize; i++) {
			TimedChordLabel label = labels.get(i);
			List<Integer> tones = label.getChordLabel().getTones();
			PitchClassSet pcs = PitchClassSet.fromSet(new HashSet<Integer>(
				tones));
			double weight = (label.getEndTime() - label.getStartTime())
				* excerptTimeInv;
			for (int tonic = 0; tonic < 12; tonic++) {
				int index = chordIndexer.getIndex(pcs, tonic);
				keyIndexSums[tonic] += weight * index;
			}
		}
		int minKeyIndex = 0;
		double keyIndexSum = Double.MAX_VALUE;
		for (int i = 0; i < 12; i++) {
			if (keyIndexSums[i] < keyIndexSum) {
				keyIndexSum = keyIndexSums[i];
				minKeyIndex = i;
			}
		}
		return minKeyIndex;
	}
}
