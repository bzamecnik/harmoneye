package com.harmoneye.math.matrix;

public class DVector {
	public static double[] add(double[] values, double addend) {
		int n = values.length;
		for (int i = 0; i < n; i++) {
			values[i] += addend;
		}
		return values;
	}

	public static double[] add(double[] values, double factor, double[] result) {
		result = copyToResult(values, result);
		return add(result, factor);
	}

	public static double[] add(double[] a, double[] b, double[] result) {
		int n = a.length;
		result = ensureSize(n, result);
		for (int i = 0; i < n; i++) {
			result[i] = a[i] + b[i];
		}
		return result;
	}

	public static double[] subtract(double[] a, double[] b) {
		return subtract(a, b, null);
	}

	public static double[] subtract(double[] a, double[] b, double[] result) {
		int n = a.length;
		result = ensureSize(n, result);
		for (int i = 0; i < n; i++) {
			result[i] = a[i] - b[i];
		}
		return result;
	}

	public static double[] mult(double[] values, double factor) {
		int n = values.length;
		for (int i = 0; i < n; i++) {
			values[i] *= factor;
		}
		return values;
	}

	public static double[] mult(double[] values, double factor, double[] result) {
		result = copyToResult(values, result);
		return mult(result, factor);
	}

	private static double[] copyToResult(double[] values, double[] result) {
		result = ensureSize(values.length, result);
		System.arraycopy(values, 0, result, 0, values.length);
		return result;
	}

	/**
	 * Ensures that the provided vector is allocated and has the given size.
	 * 
	 * Allocates the vector if null given.
	 * 
	 * @param length the desired length of the vector
	 * @param result potentially-preallocated vector, can be null
	 * @return the original pre-allocated vector of the given size or a newly
	 * allocated one
	 */
	public static double[] ensureSize(int length, double result[]) {
		if (result == null || result.length != length) {
			result = new double[length];
		}
		return result;
	}

	public static float[] toFloats(double[] doubles, float[] floats) {
		int count = doubles.length;
		for (int i = 0; i < count; i++) {
			floats[i] = (float) doubles[i];
		}
		return floats;
	}

	public static double[] toDoubles(float[] floats, double[] doubles) {
		int count = floats.length;
		for (int i = 0; i < count; i++) {
			doubles[i] = floats[i];
		}
		return doubles;
	}
}
