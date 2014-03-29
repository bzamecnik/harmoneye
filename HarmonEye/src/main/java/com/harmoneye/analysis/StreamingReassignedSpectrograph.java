package com.harmoneye.analysis;

import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

import com.harmoneye.app.spectrogram.ChromagramWrapper;
import com.harmoneye.app.spectrogram.HarmonicCorrelation;
import com.harmoneye.app.spectrogram.HarmonicPattern;
import com.harmoneye.app.spectrogram.HighPassFilter;
import com.harmoneye.app.spectrogram.MagnitudeSpectrogram;
import com.harmoneye.app.spectrogram.SpectralReassigner;
import com.harmoneye.math.L2Norm;
import com.harmoneye.math.MaxNorm;
import com.harmoneye.math.fft.ShortTimeFourierTransform;
import com.harmoneye.math.filter.Normalizer;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.math.window.BlackmanWindow;

public class StreamingReassignedSpectrograph {

	/** frequency of the lowest chromagram bin */
	private double baseFrequency = 110.0 / 4;
	private int tonesPerOctave = 12;
	private int binsPerTone = 10;
	private int wrappedBinsPerTone = 10; // 1 for plain PCP
	private int octaveBinShift = 3; // A -> C
	private int harmonicCount = 20;
	private boolean transientFilterEnabled = true;
	// minimum threshold for secondDerivatives [0; 1] to consider the
	// energy as not being a transient
	double transientThreshold = 0.4;
	private boolean highPassFilterEnabled = true;
	private int boxFilterSize = 10;
	private boolean correlationEnabled = true;
	private boolean octaveWrapEnabled = true;
	private boolean circleOfFifthsEnabled = false;
	// enable L2-norm normalization, otherwise use just plain constant scaling
	private boolean normalizationEnabled = true;
	// prevent zero-division - zero out too weak signals
	private double normalizationThreshold = 1e-2;

	private int windowSize;
	/** ratio of the baseFrequency to the sampleRate */
	private double normalizedBaseFreq;
	private double normalizedBaseFreqInv;
	/** size of the target chromagram (log-frequency resampled spectrogram) */
	private int chromagramSize;
	private int wrappedChromagramSize;
	private int binsPerOctave;

	private double[] chromagram;
	private double[] wrappedChromagram;
	/** a single frame of reassigned frequencies (only the positive freq. half) */
	private double[] freqEstimates;
	private double[] secondDerivatives;
	/** a single frame of magnitudes (only the positive freq. half) */
	private double[] magnitudeSpectrum;

	private ComplexVector spectrum;
	private ComplexVector prevTimeSpectrum;
	private ComplexVector prevFreqSpectrum;
	private ComplexVector crossTimeSpectrum;
	private ComplexVector crossFreqSpectrum;
	private ComplexVector crossFreqTimeSpectrum;

	private ShortTimeFourierTransform fft;
	private HarmonicCorrelation harmonicCorrellation;
	private HighPassFilter highPassFilter = new HighPassFilter(boxFilterSize);
	private ChromagramWrapper chromagramWrapper;
	private Normalizer l2Normalizer = new Normalizer(new L2Norm(),
		normalizationThreshold);
	private Normalizer maxNormalizer = new Normalizer(new MaxNorm(), 0);

	public StreamingReassignedSpectrograph(int windowSize, double sampleRate) {
		this.windowSize = windowSize;
		this.normalizedBaseFreq = baseFrequency / sampleRate;
		this.normalizedBaseFreqInv = 1.0 / normalizedBaseFreq;
		binsPerOctave = binsPerTone * tonesPerOctave;
		double bin = musicalBinByFrequency(0.5 - normalizedBaseFreq);
		int positiveFreqCount = windowSize / 2;
		chromagramSize = (int) FastMath.round(bin);
		wrappedChromagramSize = tonesPerOctave * wrappedBinsPerTone;

		chromagram = new double[chromagramSize];
		wrappedChromagram = new double[wrappedChromagramSize];
		freqEstimates = new double[positiveFreqCount];
		secondDerivatives = new double[positiveFreqCount];
		magnitudeSpectrum = new double[positiveFreqCount];

		spectrum = new ComplexVector(windowSize);
		prevTimeSpectrum = new ComplexVector(windowSize);
		prevFreqSpectrum = new ComplexVector(windowSize);
		crossTimeSpectrum = new ComplexVector(windowSize);
		crossFreqSpectrum = new ComplexVector(windowSize);
		crossFreqTimeSpectrum = new ComplexVector(windowSize);

		System.out.println("sampleRate: " + sampleRate);
		System.out.println("windowSize: " + windowSize);
		System.out.println("chromagram size: " + chromagramSize);

		this.fft = new ShortTimeFourierTransform(windowSize,
			new BlackmanWindow());

		HarmonicPattern harmonicPattern = HarmonicPattern.create(harmonicCount,
			binsPerOctave);
		harmonicCorrellation = new HarmonicCorrelation(harmonicPattern,
			chromagramSize);

		chromagramWrapper = new ChromagramWrapper(tonesPerOctave, binsPerTone,
			wrappedBinsPerTone, chromagramSize, octaveBinShift,
			circleOfFifthsEnabled, true);
	}

	/**
	 * @param amplitudeFrame a single frame of time-domain signal (amplitudes)
	 * of windowSize length
	 */
	public OutputFrame computeChromagram(double[] amplitudeFrame) {

		// Make a phase difference of two frames shifted by one sample.
		// We don't actually need the sample at position 0 since the window
		// is almost zero there anyway. It introduces almost no error.
		// Thus data from a single frame is practically sufficient.
		spectrum = transformFrame(amplitudeFrame, spectrum);

		SpectralReassigner.shiftRight(amplitudeFrame);
		prevTimeSpectrum = transformFrame(amplitudeFrame, prevTimeSpectrum);

		SpectralReassigner.shiftPrevFreqSpectrum(spectrum, prevFreqSpectrum);

		ComplexVector.crossSpectrum(prevTimeSpectrum,
			spectrum,
			crossTimeSpectrum);
		ComplexVector.crossSpectrum(prevFreqSpectrum,
			spectrum,
			crossFreqSpectrum);
		ComplexVector.crossSpectrum(crossFreqSpectrum,
			crossTimeSpectrum,
			crossFreqTimeSpectrum);

		freqEstimates = SpectralReassigner.estimateFreqs(crossTimeSpectrum,
			freqEstimates);
		secondDerivatives = SpectralReassigner
			.estimateSecondDerivatives(crossFreqTimeSpectrum, secondDerivatives);
		magnitudeSpectrum = magnitudes(spectrum, magnitudeSpectrum);

		reassignMagnitudes(magnitudeSpectrum,
			freqEstimates,
			secondDerivatives,
			chromagram);

		postProcessChromagram(chromagram);
		wrappedChromagram = wrapChromagram(chromagram);
		if (normalizationEnabled) {
			l2Normalizer.filter(wrappedChromagram);
		}

		return new OutputFrame(chromagram, wrappedChromagram, freqEstimates,
			secondDerivatives, spectrum, magnitudeSpectrum);
	}

	private double[] wrapChromagram(double[] chromagram) {
		if (octaveWrapEnabled) {
			Arrays.fill(wrappedChromagram, 0);
			chromagramWrapper.wrap(chromagram, wrappedChromagram);
		}
		return wrappedChromagram;
	}

	private double[] reassignMagnitudes(double[] magnitudes,
		double[] freqEstimates, double[] secondDerivatives, double[] chromagram) {
		int length = magnitudes.length;

		Arrays.fill(chromagram, 0);

		// iterate from 1 to ignore the DC
		for (int i = 1; i < length; i++) {
			if (transientFilterEnabled
				&& (FastMath.abs(secondDerivatives[i]) > transientThreshold)) {
				continue;
			}
			double targetFreq = freqEstimates[i];
			if (targetFreq <= 0) {
				continue;
			}
			// normal spectrograph
			// double targetBin = i;
			// reassigned log-freq spectrograph
			double targetBin = musicalBinByFrequency(targetFreq);
			// reassigned linear spectrograph
			// double targetBin = targetFreq * windowSize;

			if (targetBin < 0 || targetBin >= chromagramSize) {
				continue;
			}

			double magnitude = magnitudes[i];
			// double magnitude = 0.5*(groupDelays[i] + 1);//debug
			// double magnitude = 0.5 + 0.05 * windowSize * (freqEstimates[i] -
			// i/(double)windowSize);//debug
			// double magnitude = 0.5*(secondDerivatives[i] + 1);//debug
			// double magnitude = FastMath.abs(secondDerivatives[i]);// debug

			double magnitudeThreshold = 1e-2;
			if (magnitude < magnitudeThreshold) {
				continue;
			}

			int lowerBin = (int) FastMath.floor(targetBin);
			int upperBin = lowerBin + 1;
			double upperContribution = targetBin - lowerBin;
			double lowerContribution = 1 - upperContribution;

			if (lowerBin >= 0 && lowerBin < chromagramSize) {
				chromagram[lowerBin] += lowerContribution * magnitude;
			}
			if (upperBin >= 0 && upperBin < chromagramSize) {
				chromagram[upperBin] += upperContribution * magnitude;
			}
		}

		return chromagram;
	}

	public double[] postProcessChromagram(double[] chromagram) {
		if (highPassFilterEnabled) {
			highPassFilter.filter(chromagram);
		}

		if (correlationEnabled) {
			computeHarmonicCorrellation(chromagram);
		}

		return chromagram;
	}

	private void computeHarmonicCorrellation(double[] chromagram) {
		double[] correlation = harmonicCorrellation.correlate(chromagram);
//		maxNormalizer.filter(correlation);
		// mask out the chromagram by the correlation
		int maxFilterSize = 1;
		if (maxFilterSize > 1) {
			int maxFilterSizeHalf = maxFilterSize / 2;
			for (int i = 0; i < chromagramSize; i++) {
				double max = correlation[i];
				for (int j = 0; j < maxFilterSize; j++) {
					int k = i + j - maxFilterSizeHalf;
					if (k >= 0 && k < chromagramSize) {
						max = FastMath.max(max, correlation[k]);
					}
				}
				chromagram[i] *= max;
			}
		} else {
			for (int i = 0; i < chromagramSize; i++) {
				chromagram[i] *= correlation[i];
			}
		}
	}

	/**
	 * @param frequency normalized frequency
	 */
	public double musicalBinByFrequency(double frequency) {
		return FastMath.log(2, frequency * normalizedBaseFreqInv)
			* binsPerOctave;
	}

	/**
	 * @return normalized frequency
	 */
	public double frequencyForMusicalBin(int bin) {
		return normalizedBaseFreq
			* FastMath.pow(2, bin / (double) binsPerOctave);
	}

	/**
	 * @param frequency normalized frequency
	 */
	public double linearBinByFrequency(double frequency) {
		return frequency * windowSize;
	}

	public double wrapMusicalBin(double sourceBin) {
		return chromagramWrapper.wrapBin(sourceBin);
	}

	private double[] magnitudes(ComplexVector frame, double[] magnitudes) {
		return MagnitudeSpectrogram.toLogPowerSpectrum(frame, magnitudes);
	}

	private ComplexVector transformFrame(double[] amplitudeFrame,
		ComplexVector spectrum) {
		return fft.transform(amplitudeFrame, spectrum);
	}

	public static class OutputFrame {
		private double[] chromagram;
		private double[] wrappedChromagram;
		private double[] frequencies;
		private double[] secondDerivatives;
		private ComplexVector spectrum;
		private double[] magnitudes;

		public OutputFrame(double[] chromagram, double[] wrappedChromagram,
			double[] frequencies, double[] secondDerivatives,
			ComplexVector spectrum, double[] magnitudes) {
			this.chromagram = chromagram;
			this.wrappedChromagram = wrappedChromagram;
			this.frequencies = frequencies;
			this.secondDerivatives = secondDerivatives;
			this.spectrum = spectrum;
			this.magnitudes = magnitudes;
		}

		public double[] getChromagram() {
			return chromagram;
		}

		public double[] getWrappedChromagram() {
			return wrappedChromagram;
		}

		public double[] getFrequencies() {
			return frequencies;
		}

		public ComplexVector getSpectrum() {
			return spectrum;
		}

		public double[] getMagnitudes() {
			return magnitudes;
		}

		public double[] getSecondDerivatives() {
			return secondDerivatives;
		}
	}
}
