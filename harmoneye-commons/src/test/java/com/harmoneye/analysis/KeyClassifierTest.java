package com.harmoneye.analysis;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class KeyClassifierTest {

	KeyClassifier classifier = new KeyClassifier();

	@Test
	public void correlateDiatonicSets() {
		assertKey(0, new double[] { 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1 });
		assertKey(11, new double[] { 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 1 });
		assertKey(5, new double[] { 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0 });
	}

	@Test
	public void correlateDimChord() {
		assertKey(0, new double[] { 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1 });
		assertKey(1, new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0 });
		assertKey(2, new double[] { 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0 });
	}

	@Test
	public void correlateDominantSevenChord() {
		assertKey(0, new double[] { 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1 });
		assertKey(1, new double[] { 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0 });
		assertKey(9, new double[] { 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 1 });
	}

	@Test
	public void correlateMajorChord() {
		assertKey(0, new double[] { 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0 });
		assertKey(2, new double[] { 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0 });
	}

	@Test
	public void correlateSomeAveragedPitchClasses() {
		assertKey(10, new double[] { 2.2449, 1.8102, 2.3195,
			1.8381, 1.4552, 2.5768, 1.8468, 2.2686, 1.4548, 1.8837, 2.7198, 1.8649 });
	}

	private void assertKey(int expectedKey, double[] pitchClasses) {
		assertEquals(expectedKey, classifier.classifyKey(pitchClasses));
	}
}
