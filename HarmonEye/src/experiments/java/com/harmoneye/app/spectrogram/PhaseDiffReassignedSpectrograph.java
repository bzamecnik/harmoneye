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
	private int harmonicCount = 20;
	private boolean octaveWrapEnabled = false;
	private boolean correlationEnabled = false;
	/** just to scale the chromagram to the [0; 1.0] range of PNG... */
	private double normalizationFactor = 1;
	private boolean magnitudeSquaringEnabled = false;
	private boolean highPassFilterEnabled = false;

	/** ratio of the baseFrequency to the sampleRate */
	private double normalizedBaseFreq;
	private double normalizedBaseFreqInv;
	/** size of the target chromagram (log-frequency resampled spectrogram) */
	private int chromagramSize;

	/** a single frame of time-domain signal (amplitudes) of windowSize length */
	private double[] amplitudeFrame;
	/** a single frame of reassigned frequencies (only the positive freq. half) */
	private double[] freqEstimateFrame;
	/** a single frame of magnitudes (only the positive freq. half) */
	private double[] magnitudeFrame;

	private ShortTimeFourierTransform fft;

	private HarmonicPattern harmonicPattern;

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
		freqEstimateFrame = new double[positiveFreqCount];
		magnitudeFrame = new double[positiveFreqCount];

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
		for (int i = 0; i < frameCount; i++) {
			ComplexVector frame = transformFrame(amplitudes, amplitudeFrame, i
				* hopSize);
			ComplexVector nextFrame = transformFrame(amplitudes,
				amplitudeFrame,
				i * hopSize + offset);

			freqEstimateFrame = estimateFreqs(frame,
				nextFrame,
				freqEstimateFrame);
			magnitudeFrame = magnitudes(frame, magnitudeFrame);

			reassignedMagFrames[i] = reassignMagnitudes(magnitudeFrame,
				freqEstimateFrame,
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

		int size = octaveWrapEnabled ? tonesPerOctave * binsPerTone
			: chromagramSize;
		return new MagnitudeSpectrogram(reassignedMagFrames, size);
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

			magnitude *= normalizationFactor;

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

		if (correlationEnabled) {
			computeHarmonicCorrellation(reassignedMagnitudes);
		}

		if (highPassFilterEnabled) {
			highPassFilter(reassignedMagnitudes);
		}

		if (octaveWrapEnabled) {
			int octaveBins = tonesPerOctave * binsPerTone;
			double[] wrappedMagnitudes = new double[octaveBins];
			int sourceOctaves = (chromagramSize / octaveBins) - 1;
			int maxIndex = sourceOctaves * octaveBins;
			for (int i = octaveBins; i < maxIndex; i++) {
				double value = reassignedMagnitudes[i];
				wrappedMagnitudes[i % octaveBins] += value;
			}

			reassignedMagnitudes = wrappedMagnitudes;
		}

		return reassignedMagnitudes;
	}

	private void highPassFilter(double[] reassignedMagnitudes) {
		int boxFilterSize = 10;
		BoxFilter boxFilter = new BoxFilter(boxFilterSize);
		double[] lowPass = boxFilter.filter(reassignedMagnitudes);
		for (int i = 0; i < reassignedMagnitudes.length; i++) {
			reassignedMagnitudes[i] -= lowPass[i];
		}
	}

	private void computeHarmonicCorrellation(double[] reassignedMagnitudes) {

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
			reassignedMagnitudes[i] = acc;
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
	private double[] estimateFreqs(ComplexVector frame,
		ComplexVector nextFrame, double[] freqEstimates) {
		int length = freqEstimates.length;
		double[] frameElems = frame.getElements();
		double[] nextFrameElems = nextFrame.getElements();
		for (int i = 0; i < length; i++) {
			int reIndex = 2 * i;
			int imIndex = reIndex + 1;
			double phase = DComplex.arg(frameElems[reIndex],
				frameElems[imIndex]);
			double nextPhase = DComplex.arg(nextFrameElems[reIndex],
				nextFrameElems[imIndex]);
			double phaseDiff = nextPhase - phase;
			double freq = Modulo.modulo(phaseDiff * TWO_PI_INV, 1.0);
			freqEstimates[i] = freq;
		}
		return freqEstimates;
	}

	private double[] magnitudes(ComplexVector frame, double[] magnitudes) {
		return MagnitudeSpectrogram.toLogMagnitudeSpectrum(frame, magnitudes);
	}

	private ComplexVector transformFrame(double[] amplitudes,
		double[] amplitudeFrame, int index) {
		System.arraycopy(amplitudes, index, amplitudeFrame, 0, windowSize);
		return new ComplexVector(fft.transform(amplitudeFrame));
	}

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
