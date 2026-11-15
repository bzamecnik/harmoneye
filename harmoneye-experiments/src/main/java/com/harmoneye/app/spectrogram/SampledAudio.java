package com.harmoneye.app.spectrogram;

public class SampledAudio {
	private double[] samples;
	private double sampleRate;
	private double durationMillis;

	public SampledAudio(double[] samples, double sampleRate) {
		this.samples = samples;
		this.sampleRate = sampleRate;
		this.durationMillis = 1000 * (samples.length / sampleRate);
	}

	public double[] getSamples() {
		return samples;
	}

	public double getSampleRate() {
		return sampleRate;
	}

	public double getDurationMillis() {
		return durationMillis;
	}

	/** length in samples */
	public int getLength() {
		return samples.length;
	}

}
