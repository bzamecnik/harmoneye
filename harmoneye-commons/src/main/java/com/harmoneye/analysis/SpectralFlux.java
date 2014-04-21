package com.harmoneye.analysis;

import org.apache.commons.math3.exception.NullArgumentException;

import com.harmoneye.math.L2Norm;
import com.harmoneye.math.Norm;

public class SpectralFlux {
	private Norm norm = new L2Norm();
	private double[] diff;

	/**
	 * Computes the spectral flux between two magnitude spectra.
	 * 
	 * @param magSpectrum
	 * @param nextMagSpectrum
	 * @return
	 */
	public double flux(double[] magSpectrum, double[] nextMagSpectrum) {
		if (magSpectrum == null) {
			throw new NullArgumentException();
		}
		if (nextMagSpectrum == null) {
			throw new NullArgumentException();
		}

		int length = magSpectrum.length;

		diff = initArray(diff, length);

		for (int i = 0; i < length; i++) {
			diff[i] = nextMagSpectrum[i] - magSpectrum[i];
		}
		return norm.norm(diff);
	}

	private double[] initArray(double[] array, int length) {
		if (array == null || array.length != length) {
			array = new double[length];
		}
		return array;
	}
}
