package com.harmoneye.ng;

import java.util.Arrays;

import com.harmoneye.analysis.MagnitudeSpectrogram;
import com.harmoneye.math.fft.ShortTimeFourierTransform;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.ng.SpectrumExtractor.MagnitudeSpectrum;

public class SpectrumExtractor implements FeatureExtractor<MagnitudeSpectrum> {

	private ShortTimeFourierTransform stft;
	private double[] magnitudeSpectrum;

	public SpectrumExtractor(ShortTimeFourierTransform stft, int windowSize) {
		this.stft = stft;
		int positiveFreqCount = windowSize / 2;
		magnitudeSpectrum = new double[positiveFreqCount];
	}

	@Override
	public MagnitudeSpectrum extract(double[] samples) {
		ComplexVector spectrum = stft.transform(samples);
		MagnitudeSpectrogram.toLogPowerSpectrum(spectrum, magnitudeSpectrum);
		return new MagnitudeSpectrum(magnitudeSpectrum);
	}

	public static class MagnitudeSpectrum {
		private double[] spectrum;

		public MagnitudeSpectrum(double[] spectrum) {
			this.spectrum = spectrum;
		}

		public double[] getSpectrum() {
			return spectrum;
		}

		@Override
		public String toString() {
			return Arrays.toString(spectrum);
		}
	}
}
