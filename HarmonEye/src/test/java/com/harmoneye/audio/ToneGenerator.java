package com.harmoneye.audio;

public class ToneGenerator {

	private double samplingFreq;

	public ToneGenerator(double samplingFreq) {
		this.samplingFreq = samplingFreq;
	}

	/**
	 * @param frequency in Hz
	 * @param samplingFreq in samples per second
	 * @param length in seconds
	 * @return
	 */
	public double[] generateSinWave(double frequency, double length) {
		int sampleCount = (int) Math.ceil(length * samplingFreq);
		double[] signal = new double[sampleCount];
		double xStep = 2 * Math.PI * frequency / samplingFreq;
		double x = 0;
		for (int i = 0; i < sampleCount; i++, x += xStep) {
			signal[i] = Math.sin(x);
		}
		return signal;
	}

}
