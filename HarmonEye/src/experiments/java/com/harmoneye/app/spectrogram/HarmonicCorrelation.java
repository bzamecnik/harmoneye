package com.harmoneye.app.spectrogram;

public class HarmonicCorrelation {

	private HarmonicPattern harmonicPattern;
	private double[] correlation;
	private int size;

	public HarmonicCorrelation(HarmonicPattern harmonicPattern, int size) {
		this.harmonicPattern = harmonicPattern;
		this.size = size;
		this.correlation = new double[size];
	}

	public double[] correlate(double[] reassignedMagnitudes) {
		int[] indexes = harmonicPattern.getIndexes();
		double[] weights = harmonicPattern.getWeights();
		int length = indexes.length;
		for (int i = 0; i < size; i++) {
			double acc = 0;

			for (int harmonic = 0; harmonic < length; harmonic++) {
				int bin = i + indexes[harmonic];
				double weight = weights[harmonic];

				if (bin >= 0 && bin < size) {
					acc += weight * reassignedMagnitudes[bin];
				}
			}
			correlation[i] = acc;
		}
		return correlation;
	}

}
