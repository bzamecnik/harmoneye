package com.harmoneye.app.spectrogram;

import org.apache.commons.math3.util.FastMath;

import com.harmoneye.math.Modulo;
import com.harmoneye.math.matrix.ComplexVector;

public class SpectralReassigner {
	private static final double TWO_PI_INV = 1 / (2 * Math.PI);

	// chanelled instantious frequency - derivative of phase by time
	// cif = angle(crossSpectrumTime) * sampleRate / (2 * pi);
	// in this case the return value is normalized (not multiples by sampleRate)
	// [0.0; 1.0] instead of absolute [0.0; sampleRate]
	public static double[] estimateFreqs(ComplexVector crossTimeSpectrum,
		double[] freqEstimates) {
		int length = freqEstimates.length;
		double[] elems = crossTimeSpectrum.getElements();
		for (int i = 0, reIndex = 0, imIndex = 1; i < length; i++, reIndex += 2, imIndex += 2) {
			double phase = FastMath.atan2(elems[imIndex], elems[reIndex]);
			double freq = phase * TWO_PI_INV;
			freq = Modulo.modulo(freq, 1.0);

			freqEstimates[i] = freq;
		}
		return freqEstimates;
	}

	// local group delay - derivative of phase by frequency
	// lgd = angle(crossFreqSpectrum) * windowSize / (2 * pi * sampleRate);
	// normalized to [-1; 1] without using the windowDuration
	public static double[] estimateGroupDelays(ComplexVector crossFreqSpectrum,
		double[] groupDelays) {
		int length = groupDelays.length;
		double[] elems = crossFreqSpectrum.getElements();
		for (int i = 0, reIndex = 0, imIndex = 1; i < length; i++, reIndex += 2, imIndex += 2) {
			double phase = FastMath.atan2(elems[imIndex], elems[reIndex]);
			double delay = phase * TWO_PI_INV;
			delay = Modulo.modulo(delay, 1.0);
			// the delay is relative to the window beginning, but we might
			// relate the window time instant to the center
			// delay = (delay - 0.5) * windowDuration;
			delay = 2 * delay - 1;

			groupDelays[i] = delay;
		}
		return groupDelays;
	}

	public static double[] estimateSecondDerivatives(
		ComplexVector crossFreqTimeSpectrum, double[] secondDerivatives) {
		return estimateGroupDelays(crossFreqTimeSpectrum, secondDerivatives);
	}

	public static double[] shiftPrevFreqSpectrum(ComplexVector spectrum,
		ComplexVector prevFreqSpectrum) {
		double[] target = prevFreqSpectrum.getElements();
		System.arraycopy(spectrum.getElements(),
			2,
			target,
			0,
			spectrum.getElements().length - 2);
		target[0] = 0;
		target[1] = 0;
		return target;
	}

	// shifts all values one sample to the right with left zero padding
	public static void shiftRight(double[] values) {
		System.arraycopy(values, 0, values, 1, values.length - 1);
		values[0] = 0;
	}

}
