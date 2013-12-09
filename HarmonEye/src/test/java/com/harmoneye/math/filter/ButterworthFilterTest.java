package com.harmoneye.math.filter;

import static org.junit.Assert.assertTrue;
import static com.harmoneye.math.filter.SignalUtils.*;

import java.util.Arrays;

import org.junit.Test;

public class ButterworthFilterTest {

	@Test
	public void filterSomeSignal() {
		double[] signal = generateSinWave(5.0f, 20.0f, 2.0f);

		printSignal(signal);

		ButterworthFilter filter = new ButterworthFilter();
		double[] filteredSignal = filter.filter(signal, null);

		System.out.println("low-pass filtered signal");
		printSignal(filteredSignal);
	}

	@Test
	public void filterIsStateless() {
		double[] signal = generateSinWave(5.0f, 20.0f, 2.0f);

		ButterworthFilter filter = new ButterworthFilter();
		filter.filter(signal, signal);

		double[] signal2 = generateSinWave(5.0f, 20.0f, 2.0f);

		filter.filter(signal2, signal2);

		assertTrue(Arrays.equals(signal, signal2));
	}

	@Test
	public void filterFrequencyRange() {
		ButterworthFilter filter = new ButterworthFilter();
		for (int i = 0; i < 20; i++) {
			double frequency = i * 0.5f;
			double[] signal = generateSinWave(frequency, 20.0f, 2.0f);

			filter.filter(signal, signal);

			System.out.println("freq: " + frequency + ", RMS: " + computeRms(signal));
		}
	}



}
