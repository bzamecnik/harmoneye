package com.harmoneye.app.spectrogram;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.util.FastMath;

import com.harmoneye.math.Modulo;
import com.harmoneye.math.fft.ShortTimeFourierTransform;
import com.harmoneye.math.filter.BoxFilter;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.math.matrix.DComplex;
import com.harmoneye.math.window.BlackmanWindow;

public class PhaseDiffReassignedSpectrograph implements MagnitudeSpectrograph {

	private static final double TWO_PI_INV = 1 / (2 * Math.PI);

	private int windowSize;
	private int hopSize;

	/** frequency of the lowest chromagram bin */
	private double baseFrequency = 110.0 / 4;
	private int tonesPerOctave = 12;
	private int binsPerTone = 10;
	private int wrappedBinsPerTone = binsPerTone; // 1 for plain PCP
	private int octaveBinShift = 3; // A -> C
	private int harmonicCount = 20;
	private boolean octaveWrapEnabled = true;
	private boolean correlationEnabled = true;
	private boolean magnitudeSquaringEnabled = true;
	private boolean highPassFilterEnabled = true;
	private int boxFilterSize = 10;
	// enable L2-norm normalization, otherwise use just plain constant scaling
	private boolean normalizationEnabled = true;
	/** just to scale the chromagram to the [0; 1.0] range of PNG... */
	private double postScalingFactor = 1;

	/** ratio of the baseFrequency to the sampleRate */
	private double normalizedBaseFreq;
	private double normalizedBaseFreqInv;
	/** size of the target chromagram (log-frequency resampled spectrogram) */
	private int chromagramSize;

	/** a single frame of time-domain signal (amplitudes) of windowSize length */
	private double[] amplitudeFrame;
	/** a single frame of reassigned frequencies (only the positive freq. half) */
	private double[] freqEstimates;
	/** a single frame of magnitudes (only the positive freq. half) */
	private double[] magnitudeSpectrum;

	private ShortTimeFourierTransform fft;

	private HarmonicPattern harmonicPattern;

	private double[] correlation;

	private BoxFilter boxFilter = new BoxFilter(boxFilterSize);

	// private StandardDeviation stdDev = new StandardDeviation();

	public PhaseDiffReassignedSpectrograph(int windowSize, double overlapRatio,
		double sampleRate) {
		this.windowSize = windowSize;
		this.hopSize = (int) (windowSize * (1 - overlapRatio));
		this.normalizedBaseFreq = baseFrequency / sampleRate;
		this.normalizedBaseFreqInv = 1.0 / normalizedBaseFreq;
		double bin = musicalBinByFrequency(0.5 - normalizedBaseFreq);
		int positiveFreqCount = windowSize / 2;
		chromagramSize = (int) FastMath.round(bin);

		amplitudeFrame = new double[windowSize];
		freqEstimates = new double[positiveFreqCount];
		magnitudeSpectrum = new double[positiveFreqCount];

		System.out.println("sampleRate: " + sampleRate);
		System.out.println("windowSize: " + windowSize);
		System.out.println("overlapRatio: " + overlapRatio);
		System.out.println("hopSize: " + hopSize);
		System.out.println("chromagram size: " + chromagramSize);

		this.fft = new ShortTimeFourierTransform(windowSize,
			new BlackmanWindow());

		harmonicPattern = new HarmonicPattern(harmonicCount);
	}

	public MagnitudeSpectrogram computeMagnitudeSpectrogram(SampledAudio audio) {
		// -1 to allow the frames shifted by one sample
		int offset = 1;
		int frameCount = (int) Math
			.floor((audio.getLength() - offset - windowSize) / (double) hopSize) + 1;
		System.out.println("frame count: " + frameCount);
		System.out.println("duration: " + audio.getDurationMillis() + " ms");

		double[] amplitudes = audio.getSamples();
		double[][] reassignedMagFrames = new double[frameCount][];

		System.out.println("Computing spectrogram.");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		int lastPercent = 0;
		double frameCountInv = 1.0 / frameCount;
		ComplexVector spectrum = new ComplexVector(windowSize);
		ComplexVector prevSpectrum = new ComplexVector(windowSize);
		for (int i = 0; i < frameCount; i++) {
			// Make a phase difference of two frames shifted by one sample.
			// We don't actually need the sample at position 0 since the window
			// is almost zero there anyway. It introduces almost no error.
			// Thus data from a single frame is practically sufficient.
			int sourceIndex = i * hopSize;
			System.arraycopy(amplitudes,
				sourceIndex,
				amplitudeFrame,
				0,
				windowSize);
			spectrum = transformFrame(amplitudeFrame, spectrum);
			shiftRight(amplitudeFrame);
			prevSpectrum = transformFrame(amplitudeFrame, prevSpectrum);

			freqEstimates = estimateFreqsByCrossSpectrum(prevSpectrum,
				spectrum,
				freqEstimates);
			magnitudeSpectrum = magnitudes(spectrum, magnitudeSpectrum);

			reassignedMagFrames[i] = reassignMagnitudes(magnitudeSpectrum,
				freqEstimates,
				chromagramSize);

			double percent = 10 * i * frameCountInv;

			if ((int) percent > lastPercent) {
				System.out.print(".");
				lastPercent = (int) percent;
			}
		}
		System.out.println();

		stopWatch.stop();
		System.out.println("Computed spectrogram in " + stopWatch.getTime()
			+ " ms");
		System.out.println(String.format("Audio time / computation time: %.4f",
			audio.getDurationMillis() / (double) stopWatch.getTime()));

		int size = octaveWrapEnabled ? tonesPerOctave * wrappedBinsPerTone
			: chromagramSize;
		return new MagnitudeSpectrogram(reassignedMagFrames, size);
	}

	// shifts all values one sample to the right with left zero padding
	private void shiftRight(double[] values) {
		System.arraycopy(values, 0, values, 1, values.length - 1);
		values[0] = 0;
	}

	private double log2(double value) {
		return FastMath.log(2, value);
	}

	private double[] reassignMagnitudes(double[] magnitudes,
		double[] freqEstimates, int chromagramSize) {
		int length = magnitudes.length;

		double[] reassignedMagnitudes = new double[chromagramSize];

		// iterate from 1 to ignore the DC
		for (int i = 1; i < length; i++) {
			double targetFreq = freqEstimates[i];
			double targetBin = musicalBinByFrequency(targetFreq);

			if (targetBin < 0 || targetBin >= chromagramSize) {
				continue;
			}

			double magnitude = magnitudes[i];

			if (magnitudeSquaringEnabled) {
				magnitude *= magnitude; // ^2
			}

			int lowerBin = (int) Math.floor(targetBin);
			int upperBin = lowerBin + 1;
			double upperContribution = targetBin - lowerBin;
			double lowerContribution = 1 - upperContribution;

			if (lowerBin >= 0 && lowerBin < chromagramSize) {
				reassignedMagnitudes[lowerBin] += lowerContribution * magnitude;
			}
			if (upperBin >= 0 && upperBin < chromagramSize) {
				reassignedMagnitudes[upperBin] += upperContribution * magnitude;
			}
		}

		if (highPassFilterEnabled) {
			highPassFilter(reassignedMagnitudes);
		}

		if (correlationEnabled) {
			computeHarmonicCorrellation(reassignedMagnitudes);
		}

		if (octaveWrapEnabled) {
			int octaveBins = tonesPerOctave * wrappedBinsPerTone;
			double binReductionFactorInv = wrappedBinsPerTone
				/ (double) binsPerTone;
			double[] wrappedMagnitudes = new double[octaveBins];
			int sourceOctaves = (chromagramSize / octaveBins) - 1;
			int maxIndex = sourceOctaves * octaveBins;
			int middleBin = binsPerTone / 2;
			for (int i = 0; i < maxIndex; i++) {
				double value = reassignedMagnitudes[i];
				wrappedMagnitudes[((int) ((i + middleBin) * binReductionFactorInv)
					+ octaveBins - octaveBinShift)
					% octaveBins] += value;
			}

			reassignedMagnitudes = wrappedMagnitudes;
		}

		if (normalizationEnabled) {
			normalize(reassignedMagnitudes);
		} else {
			for (int i = 1; i < reassignedMagnitudes.length; i++) {
				reassignedMagnitudes[i] *= postScalingFactor;
			}
		}

		// threshold(reassignedMagnitudes);

		return reassignedMagnitudes;
	}

	private void normalize(double[] reassignedMagnitudes) {
		// L2 norm
		double norm = 0;
		for (int i = 0; i < reassignedMagnitudes.length; i++) {
			double value = reassignedMagnitudes[i];
			norm += value * value;
		}
		norm = Math.sqrt(norm);
		double threshold = 1e-4; // zero out too weak signals
		double normInv = (norm > threshold) ? 1 / norm : 0;
		for (int i = 0; i < reassignedMagnitudes.length; i++) {
			reassignedMagnitudes[i] *= normInv;
		}
	}

	private void highPassFilter(double[] reassignedMagnitudes) {
		double[] lowPass = boxFilter.filter(reassignedMagnitudes);
		for (int i = 0; i < reassignedMagnitudes.length; i++) {
			double value = reassignedMagnitudes[i] - lowPass[i];
			value = Math.max(value, 0);
			reassignedMagnitudes[i] = value;
		}
	}

	private void computeHarmonicCorrellation(double[] reassignedMagnitudes) {
		if (correlation == null || correlation.length != chromagramSize) {
			correlation = new double[chromagramSize];
		}
		for (int i = 0; i < chromagramSize; i++) {
			double acc = 0;

			for (int harmonic = 0; harmonic < harmonicPattern.getLength(); harmonic++) {
				int bin = i + harmonicPattern.getIndex(harmonic);
				double weight = harmonicPattern.getWeight(harmonic);

				if (bin < 0 || bin >= chromagramSize) {
					continue;
				}
				acc += weight * reassignedMagnitudes[bin];
			}
			correlation[i] = acc;
		}
		// max filter with kernel size 3
		for (int i = 0; i < chromagramSize; i++) {
			double max = correlation[i];
			if (i - 1 >= 0) {
				max = Math.max(max, correlation[i - 1]);
			}
			if (i + 1 < chromagramSize) {
				max = Math.max(max, correlation[i + 1]);
			}
			reassignedMagnitudes[i] *= max;
		}
	}

	/**
	 * @param frequency normalized frequency
	 */
	private double musicalBinByFrequency(double frequency) {
		return log2(frequency * normalizedBaseFreqInv) * tonesPerOctave
			* binsPerTone;
	}

	/*
	 * High-precision frequency estimates using single-sample phase difference.
	 * It is a simple implementation of frequency reassignment.
	 * 
	 * Brown J.C. and Puckette M.S. (1993). A high resolution fundamental
	 * frequency determination based on phase changes of the Fourier transform.
	 * J. Acoust. Soc. Am. Volume 94, Issue 2, pp. 662-667
	 * 
	 * Frequencies are normalized: [0.0; 1.0] means [0.0; sampleRate].
	 */
	private double[] estimateFreqsByPhaseDiff(ComplexVector frame,
		ComplexVector nextFrame, double[] freqEstimates) {
		int length = freqEstimates.length;
		double[] frameElems = frame.getElements();
		double[] nextFrameElems = nextFrame.getElements();
		for (int i = 0; i < length; i++) {
			int reIndex = 2 * i;
			int imIndex = reIndex + 1;
			double phase = FastMath.atan2(
				frameElems[imIndex], frameElems[reIndex]);
			double nextPhase = DComplex.arg(
				nextFrameElems[imIndex], nextFrameElems[reIndex]);
			double phaseDiff = nextPhase - phase;
			double freq = Modulo.modulo(phaseDiff * TWO_PI_INV, 1.0);
			freqEstimates[i] = freq;
		}
		return freqEstimates;
	}

	private double[] estimateFreqsByCrossSpectrum(ComplexVector frame,
		ComplexVector nextFrame, double[] freqEstimates) {

		// mod(angle(conj(frame).*nextFrame) / (2*pi), 1.0)

		int length = freqEstimates.length;
		double[] frameElems = frame.getElements();
		double[] nextFrameElems = nextFrame.getElements();
		for (int i = 0, reIndex = 0; i < length; i++, reIndex += 2) {
			int imIndex = reIndex + 1;

			double conjRe = frameElems[reIndex];
			double conjIm = -frameElems[imIndex];
			double nextRe = nextFrameElems[reIndex];
			double nextIm = nextFrameElems[imIndex];
			double crossRe = conjRe * nextRe - conjIm * nextIm;
			double crossIm = conjIm * nextRe + conjRe * nextIm;

			double crossPhase = FastMath.atan2(crossIm, crossRe);
			double freq = crossPhase * TWO_PI_INV;
			freq = Modulo.modulo(freq, 1.0);
			freqEstimates[i] = freq;
		}
		return freqEstimates;
	}

	private double[] magnitudes(ComplexVector frame, double[] magnitudes) {
		return MagnitudeSpectrogram.toLogMagnitudeSpectrum(frame, magnitudes);
	}

	// private ComplexVector transformFrame(double[] amplitudes,
	// double[] amplitudeFrame, int index) {
	// System.arraycopy(amplitudes, index, amplitudeFrame, 0, windowSize);
	// return new ComplexVector(fft.transform(amplitudeFrame));
	// }

	private ComplexVector transformFrame(double[] amplitudeFrame, ComplexVector spectrum) {
		return fft.transform(amplitudeFrame, spectrum);
//		return new ComplexVector(fft.transform(amplitudeFrame));
	}

	// private void threshold(double[] reassignedMagnitudes) {
	// double threshold = 2.5 * stdDev.evaluate(reassignedMagnitudes,
	// 0,
	// reassignedMagnitudes.length);
	// for (int i = 0; i < reassignedMagnitudes.length; i++) {
	// reassignedMagnitudes[i] = reassignedMagnitudes[i] > threshold ? 1
	// : 0;
	// }
	// }

	private class HarmonicPattern {

		private List<Integer> binIndexes = new ArrayList<Integer>();
		private List<Double> binWeights = new ArrayList<Double>();
		private int harmonicCount;

		public HarmonicPattern(int harmonicCount) {
			this.harmonicCount = harmonicCount;
			addPatternForFreq(1.0, 1);
			addPatternForFreq(1.0 / 2, -1);
			addPatternForFreq(1.0 / 3, -1);
			// addPatternForFreq(1.0 / 4, -1);
			// addPatternForFreq(1.0 / 5, -1);
			// addPatternForFreq(1.0 / 6, -1);
			// addPatternForFreq(1.0 / 7, -1);
			// addPatternForFreq(2.0, -1);
			// addPatternForFreq(3.0, -1);
		}

		private void addPatternForFreq(double baseFreq, double sign) {
			double normalizationFactor = 0;
			for (int i = 0; i < harmonicCount; i++) {
				normalizationFactor += Math.abs(sign * weightForHarmonic(i));
			}
			normalizationFactor = sign / normalizationFactor;

			for (int i = 0; i < harmonicCount; i++) {
				double bin = FastMath.log(2, baseFreq * (i + 1))
					* tonesPerOctave * binsPerTone;

				int lowerBin = (int) Math.floor(bin);
				// int upperBin = lowerBin + 1;
				double weight = normalizationFactor * weightForHarmonic(i);

				if (weight != 0 && !binIndexes.contains(lowerBin)) {
					binIndexes.add(lowerBin);
					binWeights.add(weight);
				}

				// double upperWeight = sign * weight * (bin - lowerBin);
				// double lowerWeight = sign * weight * (upperBin - bin);
				//
				// if (lowerWeight != 0 && !binIndexes.contains(lowerBin)) {
				// binIndexes.add(lowerBin);
				// binWeights.add(lowerWeight);
				// }
				// if (upperWeight != 0 && !binIndexes.contains(upperBin)) {
				// binIndexes.add(upperBin);
				// binWeights.add(upperWeight);
				// }
			}

			System.out.println(binIndexes);
			System.out.println(binWeights);
		}

		private double weightForHarmonic(int i) {
			// return 1.0 / (i + 1);
			// return 1.0 - (i / (2.0 * (harmonicCount - 1)));
			return 1.0;
		}

		public int getIndex(int i) {
			return binIndexes.get(i);
		}

		public double getWeight(int i) {
			return binWeights.get(i);
		}

		public int getLength() {
			return binIndexes.size();
		}
	}
}
