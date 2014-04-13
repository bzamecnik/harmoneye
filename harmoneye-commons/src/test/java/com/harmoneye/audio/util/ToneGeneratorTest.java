package com.harmoneye.audio.util;

import static com.harmoneye.audio.util.TextSignalPrinter.printSignal;

import org.junit.Test;

import com.harmoneye.audio.util.ToneGenerator;

import static org.junit.Assert.*;

public class ToneGeneratorTest {

	@Test
	public void generateUnitSignal() {
		ToneGenerator toneGenerator = new ToneGenerator(20.0);
		double[] signal = toneGenerator.generateSinWave(1.0, 1.0);

		printSignal(signal);

		double[] expectedSignal = new double[] { 0.0, 0.3090169943749474,
			0.5877852522924731, 0.8090169943749475, 0.9510565162951535, 1.0,
			0.9510565162951535, 0.8090169943749475, 0.5877852522924732,
			0.3090169943749475, 1.2246467991473532E-16, -0.30901699437494773,
			-0.5877852522924734, -0.8090169943749473, -0.9510565162951535,
			-1.0, -0.9510565162951536, -0.809016994374947, -0.5877852522924734,
			-0.3090169943749476 };
		assertEquals(expectedSignal.length, signal.length);
		for (int i = 0; i < expectedSignal.length; i++) {
			assertEquals(expectedSignal[i], signal[i], 1e-6);
		}
	}
	
	@Test
	public void generateSomeSignal() {
		ToneGenerator toneGenerator = new ToneGenerator(20.0);
		double[] signal = toneGenerator.generateSinWave(5.0, 2.0);

		printSignal(signal);
	}
}