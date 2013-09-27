package com.harmoneye.math.fft;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.transform.TransformUtils;

public class InPlaceApacheFft {
	private static final int DATA_SIZE = 8 * 1024;

	public static void main(String[] args) {
		double[] data = generateCosineWave();
		double[][] dataRI = new double[][] { new double[DATA_SIZE], new double[DATA_SIZE] };

		padRight(data, dataRI[0]);
		Arrays.fill(dataRI[1], 0);
		
		System.out.println("Original values: " + print(data));
		
		int iterations = 10000;
		
		System.out.println(new Date());
		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			FastFourierTransformer.transformInPlace(dataRI, DftNormalization.STANDARD, TransformType.FORWARD);
		}
		
		long end = System.nanoTime();
		System.out.println(new Date());
		System.out.println("total: " + (end - start) / 1e6 + " ms");
		System.out.println("average: " + (end - start) / (iterations * 1e6) + " ms");
		System.out.println("average: " + (iterations * 1e9) / (end - start) + " per sec");

		Complex[] spectrum = TransformUtils.createComplexArray(dataRI);
		double[] amplitudeSpectrum = abs(spectrum);

		normalize(amplitudeSpectrum);
		chop(amplitudeSpectrum);

//		System.out.println("FFT-transformed values: " + print(amplitudeSpectrum));
	}

	private static double[] abs(Complex[] values) {
		double[] absValues = new double[values.length];
		for (int i = 0; i < absValues.length; i++) {
			absValues[i] = values[i].abs();
		}
		return absValues;
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
			data[i] = Math.cos(2 * Math.PI * i / (double) DATA_SIZE);
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


	private static void padRight(double[] in, double[] padded) {
		int dataSize = Math.min(in.length, padded.length);

		System.arraycopy(in, 0, padded, 0, dataSize);

		for (int i = dataSize; i < padded.length; i++) {
			padded[i] = 0;
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
