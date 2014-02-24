package com.harmoneye.app.spectrogram;

import com.harmoneye.audio.DecibelCalculator;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.math.matrix.DComplex;

public class MagnitudeSpectrogram {
	private static final DecibelCalculator dbCalculator = new DecibelCalculator(
		16);

	private double[][] spectrumFrames;
	private int binCount;

	public MagnitudeSpectrogram(double[][] spectrumFrames, int binCount) {
		this.spectrumFrames = spectrumFrames;
		this.binCount = binCount;
	}

	public double[] getFrame(int i) {
		return spectrumFrames[i];
	}

	public int getBinCount() {
		return binCount;
	}

	public int getFrameCount() {
		return spectrumFrames.length;
	}

	public static MagnitudeSpectrogram fromComplexSpectrogram(
		ComplexSpectrogram spectrogram) {
		// only positive frequencies
		int binCount = spectrogram.getBinCount() / 2;
		int frameCount = spectrogram.getFrameCount();
		double[][] magnitudeFrames = new double[frameCount][];
		for (int i = 0; i < frameCount; i++) {
			ComplexVector complexFrame = spectrogram.getFrame(i);
			magnitudeFrames[i] = toLogMagnitudeSpectrum(complexFrame,
				new double[binCount]);
		}
		return new MagnitudeSpectrogram(magnitudeFrames, binCount);
	}

	// for the positive-frequency magnitude spectrum, just pass a half-sized
	// output array
	public static double[] toLogMagnitudeSpectrum(ComplexVector cplxSpectrum,
		double[] magnitudeSpectrum) {
		double[] elements = cplxSpectrum.getElements();
		for (int i = 0, index = 0; i < magnitudeSpectrum.length; i++, index += 2) {
			double re = elements[index];
			double im = elements[index + 1];
			double amplitude = DComplex.abs(re, im);
			double amplitudeDb = dbCalculator.amplitudeToDb(amplitude);
			double value = dbCalculator.rescale(amplitudeDb);
			magnitudeSpectrum[i] = value;
		}
		return magnitudeSpectrum;
	}
}
