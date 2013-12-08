package com.harmoneye.math.filter;

import org.junit.Test;
import static com.harmoneye.math.filter.SignalUtils.*;

public class ZeroPhaseButterworthFilterTest {

	@Test
	public void filterFrequencyRange() {
		Filter filter = new ZeroPhaseFilter(new ButterworthFilter());

		for (int i = 0; i < 20; i++) {
			float frequency = i * 0.5f;
			float[] signal = generateSinWave(frequency, 20.0f, 2.0f);

			filter.filter(signal, signal);

			System.out.println("freq: " + frequency + ", RMS: " + computeRms(signal));
		}
	}

	@Test
	public void filterSpike() {
		Filter butterworthFilter = new ButterworthFilter();
		Filter zeroPhasefilter = new ZeroPhaseFilter(butterworthFilter);

		float[] signal = new float[20];
		signal[10] = 1.0f;

		System.out.println("Original signal - spike:");
		printSignal(signal);
		
		float[] bwFiltered = butterworthFilter.filter(signal, null);

		System.out.println("Single-pass Butterworth:");
		printSignal(bwFiltered);

		float[] zpFiltered = zeroPhasefilter.filter(signal, null);

		System.out.println("Forward-and-reverse two-pass Butterworth:");
		printSignal(zpFiltered);
	}

	/**
	 * @param frequency in Hz
	 * @param samplingFreq in samples per second
	 * @param length in seconds
	 * @return
	 */
	private float[] generateSinWave(float frequency, float samplingFreq,
		float length) {
		int sampleCount = (int) Math.ceil(length * samplingFreq);
		float[] signal = new float[sampleCount];
		for (int i = 0; i < signal.length; i++) {
			signal[i] = (float) Math.sin(i * 2 * Math.PI * frequency / samplingFreq);
		}
		return signal;
	}

	private float computeRms(float[] samples) {
		float sum = 0;
		for (float amplitude : samples) {
			sum += amplitude * amplitude;
		}
		return (float) Math.sqrt(sum / (float) samples.length);
	}

}
