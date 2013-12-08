package com.harmoneye.math.filter;

/**
 * Decimates the signal using a low-pass filter followed by downsampling by
 * factor of two.
 * 
 * @see http://en.wikipedia.org/wiki/Downsampling
 */
public class Decimator {

	private Filter lowPassFilter;

	public Decimator(Filter lowPassFilter) {
		this.lowPassFilter = lowPassFilter;
	}

	public static Decimator withDefaultFilter() {
		return new Decimator(new ZeroPhaseFilter(new ButterworthFilter()));
	}

	public float[] decimate(float[] signal, float[] decimatedSignal) {
		int halfLength = signal.length / 2;
		if (decimatedSignal == null) {
			decimatedSignal = new float[halfLength];
		} else if (decimatedSignal.length != halfLength) {
			throw new IllegalArgumentException(
				"The downsampled signal must be half as long as the original.");
		}

		// TODO: reuse some existing array
		float[] lowPassSignal = lowPassFilter.filter(signal, null);
		return downsample(lowPassSignal, decimatedSignal);
	}

	// downsample by factor of two
	private float[] downsample(float[] signal, float[] downsampledSignal) {
		for (int to = 0, from = 0; to < downsampledSignal.length; to++, from += 2) {
			downsampledSignal[to] = signal[from];
		}
		return downsampledSignal;
	}
}
