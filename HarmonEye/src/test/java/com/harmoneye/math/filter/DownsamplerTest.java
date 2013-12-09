package com.harmoneye.math.filter;

import static org.junit.Assert.*;
import static com.harmoneye.math.filter.SignalUtils.*;
import org.junit.Test;

public class DownsamplerTest {

	@Test
	public void downsampleSomeSignal() {
		double freq = 20.0f;
		double sampleFreq = 100.0f;
		double duration = 2.0f;
		double[] signal = generateSinWave(freq, sampleFreq, duration);
		double[] expectedSignal = generateSinWave(freq, 0.5f * sampleFreq, duration);

		System.out.println("Original signal:");
		System.out.println("RMS: " + computeRms(signal));
		printSignal(signal);

		Decimator downsampler = Decimator.withDefaultFilter();
		double[] downsampledSignal = downsampler.decimate(signal, null);

		System.out.println("Decimated signal:");
		System.out.println("RMS: " + computeRms(downsampledSignal));
		printSignal(downsampledSignal);

		System.out.println("Expected signal:");
		System.out.println("RMS: " + computeRms(expectedSignal));
		printSignal(expectedSignal);
	}

}
