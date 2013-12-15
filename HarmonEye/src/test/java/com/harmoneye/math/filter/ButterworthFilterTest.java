package com.harmoneye.math.filter;

import static org.junit.Assert.assertTrue;
import static com.harmoneye.audio.TextSignalPrinter.*;

import java.util.Arrays;

import org.junit.Test;

import com.harmoneye.audio.RmsCalculator;
import com.harmoneye.audio.ToneGenerator;

public class ButterworthFilterTest {

	@Test
	public void filterSomeSignal() {
		double[] signal = ToneGenerator.generateSinWave(5.0f, 20.0f, 2.0f);

		printSignal(signal);

		ButterworthFilter filter = new ButterworthFilter();
		double[] filteredSignal = filter.filter(signal);

		System.out.println("low-pass filtered signal");
		printSignal(filteredSignal);
	}

	@Test
	public void filterIsStateless() {
		double[] signal = ToneGenerator.generateSinWave(5.0f, 20.0f, 2.0f);

		ButterworthFilter filter = new ButterworthFilter();
		filter.filter(signal);

		double[] signal2 = ToneGenerator.generateSinWave(5.0f, 20.0f, 2.0f);

		filter.filter(signal2);

		assertTrue(Arrays.equals(signal, signal2));
	}

	@Test
	public void filterFrequencyRange() {
		ButterworthFilter filter = new ButterworthFilter();
		for (int i = 0; i < 20; i++) {
			double frequency = i * 0.5f;
			double[] signal = ToneGenerator.generateSinWave(frequency, 20.0f, 2.0f);

			double[] lowPassSignal = filter.filter(signal);

			System.out.println("freq: " + frequency + ", RMS: " + RmsCalculator.computeRms(lowPassSignal));
		}
	}
}
