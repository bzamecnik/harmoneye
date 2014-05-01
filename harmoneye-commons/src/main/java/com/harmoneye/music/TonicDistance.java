package com.harmoneye.music;

/**
 * Calculates "distance" of a tone to given tonic (both represented as continuous
 * pitch classes. The values rises from the tonic via circle of fifths and in
 * the middle there is a skip and it continoues down the rest of the circle of
 * fifths.
 * 
 * Eg. for the tonic of C the distances rises as follows from 0.0 to 1.0:
 * 
 * In fact it is not true distance, since it is not symmetric.
 * 
 * C, G, D, A, E, B, F, Bb, Eb, Ab, Db, Gb
 */
public class TonicDistance {

	private final int octaveSize;

	public TonicDistance(int octaveSize) {
		this.octaveSize = octaveSize;
	}

	// normalized float distance [0.0; 1.0]
	public float distance(float tone, int tonic) {
		float i = ((tone - tonic + octaveSize) * 7) % octaveSize;
		return (i < 6 ? i : octaveSize - 1 + 6 - i) / (octaveSize - 1.0f);
	}
	
	// unnormalized integer distance [0; octaveSize - 1]
	public int distanceInt(int tone, int tonic) {
		int i = ((tone - tonic + octaveSize) * 7) % octaveSize;
		return (i < 6 ? i : octaveSize - 1 + 6 - i);
	}

	/**
	 * Maps the tonic distance to HSB hue.
	 * 
	 * [0.0; 1.0] -> [1/3; 0.0]
	 * 
	 * TODO: This function might need to go to a different class since it is
	 * about presentation.
	 */
	public float distanceToHue(float distance) {
		return (1.0f - distance) / 3.0f;
	}
}
