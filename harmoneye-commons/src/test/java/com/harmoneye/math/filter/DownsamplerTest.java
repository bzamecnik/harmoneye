package com.harmoneye.math.filter;

import static com.harmoneye.audio.util.TextSignalPrinter.printSignal;

import org.junit.Test;

import com.harmoneye.audio.util.ToneGenerator;
import com.harmoneye.math.Mean;

public class DownsamplerTest {

	@Test
	public void downsampleSomeSignal() {
		double freq = 2.0f;
		double sampleFreq = 100.0f;
		double duration = 2.0f;
		ToneGenerator toneGenerator = new ToneGenerator(sampleFreq);
		double[] signal = toneGenerator.generateSinWave(freq, duration);
		double[] expectedSignal = toneGenerator.generateSinWave(freq, 0.5f * duration);

		System.out.println("Original signal:");
		System.out.println("RMS: " + Mean.quadraticMean(signal));
		printSignal(signal);

		Decimator downsampler = Decimator.withDefaultFilter();
		double[] downsampledSignal = downsampler.decimate(signal);

		System.out.println("Decimated signal:");
		System.out.println("RMS: " + Mean.quadraticMean(downsampledSignal));
		printSignal(downsampledSignal);

		System.out.println("Expected signal:");
		System.out.println("RMS: " + Mean.quadraticMean(expectedSignal));
		printSignal(expectedSignal);
	}

}
