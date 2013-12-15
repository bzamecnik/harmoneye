package com.harmoneye.audio;

public class ToneGenerator {

	/**
	 * @param frequency in Hz
	 * @param samplingFreq in samples per second
	 * @param length in seconds
	 * @return
	 */
	public static double[] generateSinWave(double frequency,
		double samplingFreq, double length) {
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
