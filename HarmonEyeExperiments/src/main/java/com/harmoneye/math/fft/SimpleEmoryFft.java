package com.harmoneye.math.fft;

import java.util.Arrays;
import java.util.Date;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class SimpleEmoryFft {
	private static final int DATA_SIZE = 8 * 1024;

	public static void main(String[] args) {
		double[] data = generateCosineWave();

		//System.out.println("Original values: " + print(data));

		DoubleFFT_1D fft = new DoubleFFT_1D(DATA_SIZE);
		

		System.out.println(new Date());
		long counter = 0;
		
		int iterations = 10000;
		
		for (int i = 0; i < iterations; i++) {
			double[] dataCopy = Arrays.copyOf(data, data.length);
			long start = System.nanoTime();
			fft.realForward(dataCopy);
			long end = System.nanoTime();
			counter += end - start;
		}
		
		System.out.println(new Date());
		System.out.println("total: " + counter / 1e6 + " ms");
		System.out.println("average: " + counter / (iterations * 1e6) + " ms");
		System.out.println("average: " + (iterations * 1e9) / counter + " per sec");


		
		normalize(data);
		chop(data);

//		System.out.println("FFT-transformed values: " + print(data));
	}

	private static void normalize(double[] spectrum) {
		double sizeInverse = 1.0f / DATA_SIZE;
		for (int i = 0; i < spectrum.length; i++) {
			spectrum[i] = 2 * spectrum[i] * sizeInverse;
		}
		spectrum[0] *= 0.5; // DC component (zero frequency)
	}

	private static double[] generateCosineWave() {
		double[] data = new double[DATA_SIZE];
		for (int i = 0; i < data.length; i++) {
			data[i] = (double) Math.cos(2 * Math.PI * i / (double) DATA_SIZE);
		}
		return data;
	}

	private static void chop(double[] data) {
		chop(data, 10e-8f);
	}
	
	private static void chop(double[] data, double threshold) {
		for (int i = 0; i < data.length; i++) {
			if (Math.abs(data[i]) < threshold) {
				data[i] = 0;
			}
		}
	}
	
	private static String print(double[] array) {
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
