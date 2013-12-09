package com.harmoneye.math.filter;


public class SignalUtils {
	/**
	 * @param frequency in Hz
	 * @param samplingFreq in samples per second
	 * @param length in seconds
	 * @return
	 */
	public static double[] generateSinWave(double frequency, double samplingFreq,
		double length) {
		int sampleCount = (int) Math.ceil(length * samplingFreq);
		double[] signal = new double[sampleCount];
		for (int i = 0; i < signal.length; i++) {
			signal[i] = (double) Math.sin(i * 2 * Math.PI * frequency / samplingFreq);
		}
		return signal;
	}

	public static void printSignal(double[] signal) {
		System.out.println("signal:");
		System.out.println("length: " + signal.length);
		for (int i = 0; i < signal.length; i++) {
			System.out.println(toStars(signal[i]) + "\t" + signal[i]);
		}
		System.out.println();
	}

	public static double computeRms(double[] samples) {
		double sum = 0;
		for (double amplitude : samples) {
			sum += amplitude * amplitude;
		}
		return (double) Math.sqrt(sum / (double) samples.length);
	}
	

	private static String toStars(double amplitude) {
		StringBuilder sb = new StringBuilder();
		int totalSize = 80;
		int starCount = (int) Math.round(0.5 * Math.abs(amplitude) * totalSize + 1);
		for (int i = 0; i < 0.5 * totalSize; i++) {
			sb.append(' ');
		}
		for (int i = 0; i < starCount; i++) {
			sb.append('*');
		}
		for (int i = sb.length(); i <= totalSize; i++) {
			sb.append(' ');
		}
		if (amplitude < 0) {
			sb.reverse();
		}
		return sb.toString();
	}
}
