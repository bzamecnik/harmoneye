package com.harmoneye.math.filter;

import org.junit.Test;
import static com.harmoneye.math.filter.SignalUtils.*;

public class ZeroPhaseButterworthFilterTest {

	@Test
	public void filterFrequencyRange() {
		Filter filter = new ZeroPhaseFilter(new ButterworthFilter());

		for (int i = 0; i < 20; i++) {
			double frequency = i * 0.5f;
			double[] signal = generateSinWave(frequency, 20.0f, 2.0f);

			filter.filter(signal, signal);

			System.out.println("freq: " + frequency + ", RMS: " + computeRms(signal));
		}
	}

	@Test
	public void filterSpike() {
		Filter butterworthFilter = new ButterworthFilter();
		Filter zeroPhasefilter = new ZeroPhaseFilter(butterworthFilter);

		double[] signal = new double[20];
		signal[10] = 1.0f;

		System.out.println("Original signal - spike:");
		printSignal(signal);
		
		double[] bwFiltered = butterworthFilter.filter(signal, null);

		System.out.println("Single-pass Butterworth:");
		printSignal(bwFiltered);

		double[] zpFiltered = zeroPhasefilter.filter(signal, null);

		System.out.println("Forward-and-reverse two-pass Butterworth:");
		printSignal(zpFiltered);
	}

	/**
	 * @param frequency in Hz
	 * @param samplingFreq in samples per second
	 * @param length in seconds
	 * @return
	 */
	private double[] generateSinWave(double frequency, double samplingFreq,
		double length) {
		int sampleCount = (int) Math.ceil(length * samplingFreq);
		double[] signal = new double[sampleCount];
		for (int i = 0; i < signal.length; i++) {
			signal[i] = (double) Math.sin(i * 2 * Math.PI * frequency / samplingFreq);
		}
		return signal;
	}

	private double computeRms(double[] samples) {
		double sum = 0;
		for (double amplitude : samples) {
			sum += amplitude * amplitude;
		}
		return (double) Math.sqrt(sum / (double) samples.length);
	}

}
