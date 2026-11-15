package com.harmoneye.audio;

import static org.junit.Assert.assertEquals;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

public class DecibelCalculatorTest {

	@Test
	public void zero() {
		int bitsPerSample = 16;
		double amplitudeStep = amplitudeStep(bitsPerSample);
		test(bitsPerSample, toDecibel(amplitudeStep), 0.0);
	}

	@Test
	public void amplitudeStep() {
		int bitsPerSample = 16;
		double amplitudeStep = amplitudeStep(bitsPerSample);
		test(bitsPerSample, toDecibel(amplitudeStep), amplitudeStep);
	}

	@Test
	public void one() {
		int bitsPerSample = 16;
		test(bitsPerSample, 0.0, 1.0);
	}
	
	private void test(int bitsPerSample, double expectedDb, double amplitude) {
		DecibelCalculator calc = new DecibelCalculator(bitsPerSample);
		double actual = calc.amplitudeToDb(amplitude);
		System.out.println("expected: " + expectedDb);
		System.out.println("actual: " + actual);
		System.out.println("diff: " + (expectedDb - actual));
		assertEquals(expectedDb, actual, 1e-6);
	}

	private double toDecibel(double amplitude) {
		return 20 * FastMath.log10(amplitude);
	}

	private double amplitudeStep(int bitsPerSample) {
		return 1.0 / (2 << (bitsPerSample - 1));
	}
}
