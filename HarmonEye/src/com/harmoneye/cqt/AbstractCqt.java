package com.harmoneye.cqt;

import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.apache.commons.math3.util.FastMath;

public abstract class AbstractCqt implements Cqt {

	protected final int octaveCount = 4;
	
	protected final double baseFreq = 2 * 65.4063913251;
	protected final double maxFreq = Math.pow(2, octaveCount) * baseFreq;
	protected final double samplingFreq = 11025;

	protected final int binsPerHalftone = 7;
	protected final int binsPerHalftoneHalf = binsPerHalftone / 2;
	protected final int binsPerOctave = 12 * binsPerHalftone;
	protected final double binsPerOctaveInv = 1.0 / binsPerOctave;

	protected final double q = 1 / (FastMath.pow(2, binsPerOctaveInv) - 1);

	protected final int totalBins = (int) Math.ceil(binsPerOctave * FastMath.log(2, maxFreq / baseFreq));

	protected WindowFunction window = new HammingWindow();
	protected double windowIntegral = windowIntegral(window);

	public AbstractCqt() {
		System.out.println("octave count: " + octaveCount);
		System.out.println("base freq: " + baseFreq);
		System.out.println("max freq: " + maxFreq);
		System.out.println("bandWidth[0]: " + bandWidth(0));
	}
	
	protected double centerFreq(int k) {
		// (k - binsPerHalftoneHalf) instead of k
		// in order to put the center frequency in the center bin for that haftone
		return (baseFreq * FastMath.pow(2, (k - binsPerHalftoneHalf) * binsPerOctaveInv));
	}

	protected int bandWidth(int k) {
		return (int) Math.ceil(q * samplingFreq / centerFreq(k));
	}

	protected Complex[] temporalKernel(int k) {
		int size = bandWidth(k);
		Complex[] coeffs = new Complex[size];
		double sizeInv = 1.0 / size;
		double factor = 2 * Math.PI * q * sizeInv;
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

	
	public int getOctaveCount() {
		return octaveCount;
	}

	public int getBinsPerHalftone() {
		return binsPerHalftone;
	}

	public int getBinsPerOctave() {
		return binsPerOctave;
	}
}
