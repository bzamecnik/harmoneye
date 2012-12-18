package org.zamecnik;

import edu.emory.mathcs.jtransforms.fft.*;

public class SimpleFft {
	private static final int DATA_SIZE = 128;

	public static void main(String[] args) {
		float[] data = generateCosineWave();

		System.out.println("Original values: " + print(data));

		FloatFFT_1D fft = new FloatFFT_1D(DATA_SIZE);
		fft.realForward(data);

		System.out.println("FFT-transformed values: " + print(data));
	}

	private static float[] generateCosineWave() {
		float[] data = new float[DATA_SIZE];
		for (int i = 0; i < data.length; i++) {
			data[i] = (float) Math.cos(2 * Math.PI * i / (double) DATA_SIZE);
		}
		return data;
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
