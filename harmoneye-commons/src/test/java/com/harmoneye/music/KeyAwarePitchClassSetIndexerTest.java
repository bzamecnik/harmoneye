package com.harmoneye.music;

import static org.junit.Assert.*;
import static com.harmoneye.music.PitchClassSet.fromArray;

import org.junit.Test;

public class KeyAwarePitchClassSetIndexerTest {

	KeyAwarePitchClassSetIndexer indexer = new KeyAwarePitchClassSetIndexer();

	@Test
	public void tonicMajorChordInC() {
		assertEquals(19, indexer.getIndex(fromArray(0, 4, 7), 0));
	}

	@Test
	public void majorChord() {
		assertEquals(769, indexer.getIndex(fromArray(0, 4, 7), 4));
	}

	@Test
	public void tonicMajorChordInD() {
		assertEquals(19, indexer.getIndex(fromArray(2, 6, 9), 2));
	}

	@Test
	public void dominantChordInAb() {
		assertEquals(102, indexer.getIndex(fromArray(3, 7, 10, 1), 8));
	}

	@Test
	public void diatonicScaleInSameKey() {
		assertEquals(127, indexer.getIndex(fromArray(0, 2, 4, 5, 7, 9, 11), 0));
	}

	@Test
	public void fDiatonicScaleInKeyD() {
		assertEquals(967, indexer.getIndex(fromArray(5, 7, 9, 10, 0, 2, 4), 2));
	}

	@Test
	public void empty() {
		assertEquals(0, indexer.getIndex(fromArray(), 7));
	}

	@Test
	public void full() {
		PitchClassSet chord = fromArray(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
		assertEquals(4095, indexer.getIndex(chord, 9));
	}

}
