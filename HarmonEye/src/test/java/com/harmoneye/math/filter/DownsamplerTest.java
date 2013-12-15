package com.harmoneye.math.filter;

import static org.junit.Assert.*;
import static com.harmoneye.audio.TextSignalPrinter.*;

import org.junit.Test;

import com.harmoneye.audio.RmsCalculator;
import com.harmoneye.audio.ToneGenerator;

public class DownsamplerTest {

	@Test
	public void downsampleSomeSignal() {
		double freq = 2.0f;
		double sampleFreq = 100.0f;
		double duration = 2.0f;
		double[] signal = ToneGenerator.generateSinWave(freq, sampleFreq, duration);
		double[] expectedSignal = ToneGenerator.generateSinWave(freq, 0.5f * sampleFreq, duration);

		System.out.println("Original signal:");
		System.out.println("RMS: " + RmsCalculator.computeRms(signal));
		printSignal(signal);

		Decimator downsampler = Decimator.withDefaultFilter();
		double[] downsampledSignal = downsampler.decimate(signal);

		System.out.println("Decimated signal:");
		System.out.println("RMS: " + RmsCalculator.computeRms(downsampledSignal));
		printSignal(downsampledSignal);

		System.out.println("Expected signal:");
		System.out.println("RMS: " + RmsCalculator.computeRms(expectedSignal));
		printSignal(expectedSignal);
	}

}
