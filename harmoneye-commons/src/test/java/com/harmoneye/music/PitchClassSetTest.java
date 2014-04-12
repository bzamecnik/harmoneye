package com.harmoneye.music;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PitchClassSetTest {

	@Test
	public void testCanonicalization() {
		PitchClassSet set = PitchClassSet.fromArray(0, 4, 7);
		PitchClassSet canonic = set.getCanonic();
		for (int i = 0; i < PitchClassSet.OCTAVE_SIZE; i++) {
			assertEquals(canonic, set.getCanonic());
			assertEquals(i, set.getRoot());
			set = set.transpose(1);
		}
	}

	@Test
	public void testEquals() {
		assertEquals(PitchClassSet.fromArray(), PitchClassSet.fromIndex(0));
		assertEquals(PitchClassSet.fromArray(1, 5, 8), PitchClassSet.fromIndex(290));
		assertThat(PitchClassSet.fromArray(1, 5, 8), is(not(PitchClassSet.fromIndex(145))));
	}
	
	@Test
	public void testGet() {
		PitchClassSet set = PitchClassSet.fromArray(0, 4, 7);
		assertTrue(set.get(0));
		assertTrue(set.get(4));
		assertTrue(set.get(7));
		
		assertFalse(set.get(1));
		assertFalse(set.get(2));
		assertFalse(set.get(8));
	}
}
