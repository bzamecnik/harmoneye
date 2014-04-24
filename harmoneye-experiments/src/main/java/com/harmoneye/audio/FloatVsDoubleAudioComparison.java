package com.harmoneye.audio;

import java.util.Arrays;
import java.util.Random;

import com.harmoneye.math.L2Norm;
import com.harmoneye.math.matrix.DVector;

// compare the numerical error of using floats vs. doubles for audio samples
public class FloatVsDoubleAudioComparison {
	public static void main(String[] args) {
		short[] shortSamples = generate16BitSamples(100);
		float[] floatSamples = shortsToFloats(shortSamples);
		double[] doubleSamples = shortsToDoubles(shortSamples);
		System.out.println("shorts: " + Arrays.toString(shortSamples));
		System.out.println("floats: " + Arrays.toString(floatSamples));
		System.out.println("doubles: " + Arrays.toString(doubleSamples));
		
		double[] diff = DVector.subtract(floatsToDoubles(floatSamples), doubleSamples);
		System.out.println("diff: " + Arrays.toString(diff));
		double error = new L2Norm().norm(diff);
		System.out.println("||float - double||: " + error);
	}

	private static short[] generate16BitSamples(int count) {
		short[] samples = new short[count];
		Random random = new Random();
		int fullRange = 2 << 15;
		int halfRange = fullRange >> 1;
		for (int i = 0; i < count; i++) {
			samples[i] = (short) (random.nextInt(fullRange) - halfRange);
		}
		return samples;
	}

	private static float[] shortsToFloats(short[] shorts) {
		int count = shorts.length;
		float[] floats = new float[count];
		float factor = 1.0f / (2 << 14);
		for (int i = 0; i < count; i++) {
			floats[i] = factor * shorts[i];
		}
		return floats;
	}

	private static double[] shortsToDoubles(short[] shorts) {
		int count = shorts.length;
		double[] doubles = new double[count];
		double factor = 1.0 / (2 << 14);
		for (int i = 0; i < count; i++) {
			doubles[i] = factor * shorts[i];
		}
		return doubles;
	}
	
	private static double[] floatsToDoubles(float[] floats) {
		int count = floats.length;
		double[] doubles = new double[count];
		for (int i = 0; i < count; i++) {
			doubles[i] = floats[i];
		}
		return doubles;
	}
}
