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

	public double[] decimate(double[] signal, double[] decimatedSignal) {
		int halfLength = signal.length / 2;
		if (decimatedSignal == null) {
			decimatedSignal = new double[halfLength];
		} else if (decimatedSignal.length != halfLength) {
			throw new IllegalArgumentException(
				"The downsampled signal must be half as long as the original.");
		}

		// TODO: reuse some existing array
		double[] lowPassSignal = lowPassFilter.filter(signal, null);
		return downsample(lowPassSignal, decimatedSignal);
	}

	// downsample by factor of two
	private double[] downsample(double[] signal, double[] downsampledSignal) {
		for (int to = 0, from = 0; to < downsampledSignal.length; to++, from += 2) {
			downsampledSignal[to] = signal[from];
		}
		return downsampledSignal;
	}
}
