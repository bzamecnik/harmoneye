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
		for (int i = 0; i < size; i++) {
			double acc = 0;

			for (int harmonic = 0; harmonic < harmonicPattern.getLength(); harmonic++) {
				int bin = i + harmonicPattern.getIndex(harmonic);
				double weight = harmonicPattern.getWeight(harmonic);

				if (bin < 0 || bin >= size) {
					continue;
				}
				acc += weight * reassignedMagnitudes[bin];
			}
			correlation[i] = acc;
		}
		return correlation;
	}

}
