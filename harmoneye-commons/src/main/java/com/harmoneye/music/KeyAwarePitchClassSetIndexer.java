package com.harmoneye.music;

public class KeyAwarePitchClassSetIndexer {

	private static final int[] MAPPING = new int[] { 0, 10, 2, 8, 4, 6, 11, 1,
		9, 3, 7, 5 };
	private static final int TONE_COUNT = MAPPING.length;
	private static final double NORMALIZATION_FACTOR = 1.0 / 12.0;
	private static final double LN_TO_LOG_2 = 1.0 / Math.log(2);

	public int getIndex(PitchClassSet set, int key) {
		int index = 0;
		for (int tone : set.asList()) {
			index += 1 << MAPPING[(tone - key + TONE_COUNT) % TONE_COUNT];
		}
		return index;
	}

	public double getLogIndex(PitchClassSet set, int key) {
		return Math.log(getIndex(set, key) + 1) * LN_TO_LOG_2
			* NORMALIZATION_FACTOR;
	}
}
