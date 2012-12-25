package org.zamecnik;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class SimpleFft {
	private static final int DATA_SIZE = 128;

	public static void main(String[] args) {
		float[] data = generateCosineWave();

		System.out.println("Original values: " + print(data));

		FloatFFT_1D fft = new FloatFFT_1D(DATA_SIZE);
		fft.realForward(data);
		normalize(data);
		chop(data);

		System.out.println("FFT-transformed values: " + print(data));
	}

	private static void normalize(float[] spectrum) {
		float sizeInverse = 1.0f / DATA_SIZE;
		for (int i = 0; i < spectrum.length; i++) {
			spectrum[i] = 2 * spectrum[i] * sizeInverse;
		}
		spectrum[0] *= 0.5; // DC component (zero frequency)
	}

	private static float[] generateCosineWave() {
		float[] data = new float[DATA_SIZE];
		for (int i = 0; i < data.length; i++) {
			data[i] = (float) Math.cos(2 * Math.PI * i / (double) DATA_SIZE);
		}
		return data;
	}

	private static void chop(float[] data) {
		chop(data, 10e-8f);
	}
	
	private static void chop(float[] data, float threshold) {
		for (int i = 0; i < data.length; i++) {
			if (Math.abs(data[i]) < threshold) {
				data[i] = 0;
			}
		}
	}
	
	private static String print(float[] array) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < array.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(array[i]);
		}
		sb.append("]");
		return sb.toString();
	}
}
