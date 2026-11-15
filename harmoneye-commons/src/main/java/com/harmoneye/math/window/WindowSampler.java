package com.harmoneye.math.window;

public class WindowSampler {
	public double[] sampleWindow(WindowFunction window, int size) {
		double[] coeffs = new double[size];
		double sizeInv = 1.0 / size;
		for (int i = 0; i < size; i++) {
			coeffs[i] = window.value(i * sizeInv) * sizeInv;
		}
		return coeffs;
	}
}
