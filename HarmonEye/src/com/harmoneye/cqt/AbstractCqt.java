package com.harmoneye.cqt;

import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.apache.commons.math3.util.FastMath;

public abstract class AbstractCqt implements Cqt {

	//	protected final double baseFreq = 130.8127826503;
	//	protected final double maxFreq = 4186.0090448096;
	//	protected final double samplingFreq = 22050;

	protected final double baseFreq = 10.0f;
	protected final double maxFreq = 20.0f;
	protected final double samplingFreq = 2 * maxFreq;

	protected final int binsPerHalftone = 1;
	protected final int binsPerOctave = 12 * binsPerHalftone;
	protected final double binsPerOctaveInv = 1.0 / binsPerOctave;

	protected final double q = 1 / (FastMath.pow(2, binsPerOctaveInv) - 1);

	protected final int totalBins = (int) Math.ceil(binsPerOctave * FastMath.log(2, maxFreq / baseFreq));

	protected WindowFunction window = new HammingWindow();
	protected double windowIntegral = windowIntegral(window);

	protected double centerFreq(int k) {
		return (baseFreq * FastMath.pow(2, k * binsPerOctaveInv));
	}

	protected int bandWidth(int k) {
		return (int) Math.ceil(q * samplingFreq / centerFreq(k));
	}

	protected Complex[] temporalKernel(int k, int size) {
		Complex[] coeffs = new Complex[size];
		double sizeInv = 1.0 / size;
		double factor = -2 * Math.PI * q * sizeInv;
		for (int i = 0; i < size; i++) {
			Complex value = ComplexUtils.polar2Complex(window.value(i * sizeInv) * sizeInv, i * factor);
			coeffs[i] = value;
		}
		return coeffs;
	}

	private static double windowIntegral(WindowFunction window) {
		UnivariateIntegrator integrator = new TrapezoidIntegrator();
		return integrator.integrate(100, window, 0, 1);
	}

}
