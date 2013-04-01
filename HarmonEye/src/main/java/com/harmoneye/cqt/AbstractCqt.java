package com.harmoneye.cqt;

import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.apache.commons.math3.util.FastMath;

public abstract class AbstractCqt implements Cqt {

	protected final int octaveCount = 7;

	protected final double baseFreq = 65.4063913251;
	protected final double maxFreq = Math.pow(2, octaveCount) * baseFreq;
	protected final double samplingFreq = 11025;

	protected final int binsPerHalftone = 5;
	protected final int binsPerHalftoneHalf = binsPerHalftone / 2;
	protected final int binsPerOctave = 12 * binsPerHalftone;
	protected final double binsPerOctaveInv = 1.0 / binsPerOctave;

	protected final double q = 1 / (FastMath.pow(2, binsPerOctaveInv) - 1);

	protected final int totalBins = (int) Math.ceil(binsPerOctave * FastMath.log(2, maxFreq / baseFreq));

	protected WindowFunction window = new HammingWindow();
	protected double windowIntegral = windowIntegral(window);

	public AbstractCqt() {
//		System.out.println("octave count: " + octaveCount);
//		System.out.println("base freq: " + baseFreq);
//		System.out.println("max freq: " + maxFreq);
//		System.out.println("bandWidth[0]: " + bandWidth(0));
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

	public class HarmonicPatternPitchClassDetector {

		private static final int HARMONIC_COUNT = 7;

		/**
		 * detect fundamental frequencies for bins within pitch classes via
		 * pattern matching of the harmonics
		 * 
		 * @param cqBins
		 * @return
		 */
		public double[] detectPitchClasses(double[] cqBins) {
			double[] octaveBins = new double[binsPerOctave];
			for (int i = 0; i < cqBins.length; i++) {
				octaveBins[i % binsPerOctave] += sum(extractHarmonics(cqBins, i, HARMONIC_COUNT));
			}
			
//			double[] octaveBins = new double[cqBins.length];
//			for (int i = 0; i < cqBins.length; i++) {
//				octaveBins[i] = sum(extractHarmonics(cqBins, i, HARMONIC_COUNT));
//			}
			
			// normalize
			double max = 0;
			for (int i = 0; i < octaveBins.length; i++) {
				max = Math.max(max, octaveBins[i]);
			}
			if (max > 0) {
				for (int i = 0; i < octaveBins.length; i++) {
					octaveBins[i] /= max;
				}
			}
			
			return octaveBins;
		}

		private double[] extractHarmonics(double[] cqBins, int baseFreqBin, int harmonicCount) {
			double[] harmonics = new double[harmonicCount];
			for (int i = 1; i <= harmonicCount; i++) {
				double frequency = centerFreq(baseFreqBin);
				int harmonicBin = harmonicFreqBin(frequency, i);
				if (harmonicBin < cqBins.length) {
					double weight = 1 - 0.4 * i / (double) (harmonicCount - 1);
					harmonics[i - 1] = weight * cqBins[harmonicBin];
				}
			}
			return harmonics;
		}

		/**
		 * @param frequency of the first harmonic
		 * @param harmonicIndex one-based index of the desired k-th harmonic
		 * @return zero-based cq bin index of the k-th harmonic frequency
		 */
		private int harmonicFreqBin(double frequency, int harmonicIndex) {
			return (int) FastMath.round(binsPerOctave * FastMath.log(2, harmonicIndex * frequency / baseFreq))
				+ binsPerHalftoneHalf;
		}

		private double sum(double[] values) {
			double sum = 0;
			for (int i = 0; i < values.length; i++) {
				sum += values[i];
			}
			return sum;
		}
	}
}
