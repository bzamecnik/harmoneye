package com.harmoneye.app.spectrogram;

import com.harmoneye.audio.DecibelCalculator;
import com.harmoneye.math.fft.Fft;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.math.matrix.DComplex;
import com.harmoneye.math.window.HammingWindow;

public class Spectrograph {

	private int windowSize;
	private int hopSize;
	private Fft fft;

	public Spectrograph(int windowSize, double overlapRatio) {
		this.windowSize = windowSize;
		this.hopSize = (int)(windowSize * (1 - overlapRatio));
		System.out.println("windowSize:" + windowSize);
		System.out.println("overlapRatio:" + overlapRatio);
		System.out.println("hopSize:" + hopSize);
		this.fft = new Fft(windowSize, new HammingWindow());
	}

	public Spectrogram computeSpectrogram(SampledAudio audio) {
		int frameCount = (int)Math.floor((audio.getLength() - windowSize) / hopSize) + 1;
		System.out.println("framecount:" + frameCount);

		double[] amplitudes = audio.getSamples();
		double[] amplitudeFrame = new double[windowSize];

		ComplexVector[] spectrumFrames = new ComplexVector[frameCount];
		for (int i = 0; i < frameCount; i++) {
			System.arraycopy(amplitudes,
				i * hopSize,
				amplitudeFrame,
				0,
				windowSize);
			spectrumFrames[i] = new ComplexVector(fft.transform(amplitudeFrame));
		}
		return new Spectrogram(spectrumFrames, windowSize);
	}

	public static class Spectrogram {
		private DecibelCalculator dbCalculator = new DecibelCalculator(16);

		private ComplexVector[] spectrumFrames;
		private int binCount;

		public Spectrogram(ComplexVector[] spectrumFrames, int binCount) {
			this.spectrumFrames = spectrumFrames;
			this.binCount = binCount;
		}

		public ComplexVector getFrame(int i) {
			return spectrumFrames[i];
		}

		public double[] getMagnitudeFrame(int i, double[] dest) {
			return toLogMagnitudeSpectrum(spectrumFrames[i], dest);
		}

		public int getFrameCount() {
			return spectrumFrames.length;
		}

		public int getBinCount() {
			return binCount;
		}

		private double[] toLogMagnitudeSpectrum(ComplexVector cplxSpectrum,
			double[] magnitudeSpectrum) {
			double[] elements = cplxSpectrum.getElements();
			for (int i = 0, index = 0; i < cplxSpectrum.size(); i++, index += 2) {
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
}
