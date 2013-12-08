package com.harmoneye.math.filter;


public class SignalUtils {
	/**
	 * @param frequency in Hz
	 * @param samplingFreq in samples per second
	 * @param length in seconds
	 * @return
	 */
	public static float[] generateSinWave(float frequency, float samplingFreq,
		float length) {
		int sampleCount = (int) Math.ceil(length * samplingFreq);
		float[] signal = new float[sampleCount];
		for (int i = 0; i < signal.length; i++) {
			signal[i] = (float) Math.sin(i * 2 * Math.PI * frequency / samplingFreq);
		}
		return signal;
	}

	public static void printSignal(float[] signal) {
		System.out.println("signal:");
		System.out.println("length: " + signal.length);
		for (int i = 0; i < signal.length; i++) {
			System.out.println(toStars(signal[i]) + "\t" + signal[i]);
		}
		System.out.println();
	}

	public static float computeRms(float[] samples) {
		float sum = 0;
		for (float amplitude : samples) {
			sum += amplitude * amplitude;
		}
		return (float) Math.sqrt(sum / (float) samples.length);
	}
	

	private static String toStars(float amplitude) {
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
