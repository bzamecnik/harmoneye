package com.harmoneye.math.filter;

import static org.junit.Assert.*;
import static com.harmoneye.math.filter.SignalUtils.*;
import org.junit.Test;

public class DownsamplerTest {

	@Test
	public void downsampleSomeSignal() {
		float freq = 20.0f;
		float sampleFreq = 100.0f;
		float duration = 2.0f;
		float[] signal = generateSinWave(freq, sampleFreq, duration);
		float[] expectedSignal = generateSinWave(freq, 0.5f * sampleFreq, duration);

		System.out.println("Original signal:");
		System.out.println("RMS: " + computeRms(signal));
		printSignal(signal);

		Decimator downsampler = Decimator.withDefaultFilter();
		float[] downsampledSignal = downsampler.decimate(signal, null);

		System.out.println("Decimated signal:");
		System.out.println("RMS: " + computeRms(downsampledSignal));
		printSignal(downsampledSignal);

		System.out.println("Expected signal:");
		System.out.println("RMS: " + computeRms(expectedSignal));
		printSignal(expectedSignal);
	}

}
