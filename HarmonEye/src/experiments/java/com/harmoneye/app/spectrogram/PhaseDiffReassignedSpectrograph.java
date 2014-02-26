package com.harmoneye.app.spectrogram;

import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

import com.harmoneye.math.Modulo;
import com.harmoneye.math.fft.ShortTimeFourierTransform;
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
	private int binsPerTone = 11;

	/** ratio of the baseFrequency to the sampleRate */
	private double relativeBaseFreq;
	/** size of the target chromagram (log-frequency resampled spectrogram) */
	private int chromagramSize;
	/**
	 * inverse of the {@link #windowSize}, can be used to convert an absolute
	 * frequency to the normalized one [0; windowSize] to [0; 1]
	 */
	private double windowSizeInv;

	/** a single frame of time-domain signal (amplitudes) of windowSize length */
	private double[] amplitudeFrame;
	/** a single frame of reassigned frequencies (only the positive freq. half) */
	private double[] freqEstimateFrame;
	/** a single frame of magnitudes (only the positive freq. half) */
	private double[] magnitudeFrame;
	/**
	 * A histogram of target chromagram bins which got some energy. For each
	 * target bin there's the number of source bins that were assigned here.
	 * This is needed for normalizing the chromagram later.
	 */
	private double[] chromaFrameHistogram;

	private ShortTimeFourierTransform fft;

	public PhaseDiffReassignedSpectrograph(int windowSize, double overlapRatio,
		double sampleRate) {
		this.windowSize = windowSize;
		this.hopSize = (int) (windowSize * (1 - overlapRatio));
		this.relativeBaseFreq = sampleRate / baseFrequency;
		chromagramSize = musicalBinByFrequency(0.5);
		windowSizeInv = 1.0 / windowSize;
		int positiveFreqCount = windowSize / 2;

		amplitudeFrame = new double[windowSize];
		freqEstimateFrame = new double[positiveFreqCount];
		magnitudeFrame = new double[positiveFreqCount];
		chromaFrameHistogram = new double[chromagramSize];

		System.out.println("sampleRate: " + sampleRate);
		System.out.println("windowSize: " + windowSize);
		System.out.println("overlapRatio: " + overlapRatio);
		System.out.println("hopSize: " + hopSize);
		System.out.println("chromagram size: " + chromagramSize);

		this.fft = new ShortTimeFourierTransform(windowSize,
			new BlackmanWindow());
	}

	public MagnitudeSpectrogram computeMagnitudeSpectrogram(SampledAudio audio) {
		// -1 to allow the frames shifted by one sample
		int offset = 1;
		int frameCount = (int) Math
			.floor((audio.getLength() - offset - windowSize) / (double) hopSize) + 1;
		System.out.println("frame count: " + frameCount);

		double[] amplitudes = audio.getSamples();
		double[][] reassignedMagFrames = new double[frameCount][];

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
		}

		return new MagnitudeSpectrogram(reassignedMagFrames, chromagramSize);
	}

	private double log2(double value) {
		return FastMath.log(2, value);
	}

	private double[] reassignMagnitudes(double[] magnitudes,
		double[] freqEstimates, int chromagramSize) {
		int length = magnitudes.length;

		double[] reassignedMagnitudes = new double[chromagramSize];

		Arrays.fill(chromaFrameHistogram, 0);

		// iterate from 1 to ignore the DC
		for (int i = 1; i < length; i++) {
			// int targetBin = linearBinByFrequency(freqEstimates[i]);
			double sourceFreq = i * windowSizeInv;
			int sourceBin = musicalBinByFrequency(sourceFreq);
			if (sourceBin < 0 || sourceBin >= chromagramSize) {
				continue;
			}
			double targetFreq = freqEstimates[i];
			double targetBin = musicalBinByFrequency(targetFreq);
			// if (Math.abs(targetBin - i) / (double) length > 0.01) {
			// targetBin = i;
			// }
			if (targetBin < 0 || targetBin >= chromagramSize) {
				continue;
			}
			// if (Math.abs(sourceBin - targetBin) > 2 * binsPerTone) {
			// targetBin = sourceBin;
			// }

			// DEBUG: wrap around the octaves

			int lowerBin = (int) Math.floor(targetBin);
			int upperBin = lowerBin + 1;
			double upperContribution = targetBin - lowerBin;
			double lowerContribution = 1 - upperContribution;
			double magnitude = magnitudes[i];
			if (lowerBin >= 0 && lowerBin < chromagramSize) {
				reassignedMagnitudes[lowerBin] += lowerContribution * magnitude;
				chromaFrameHistogram[lowerBin] += lowerContribution;
			}
			if (upperBin >= 0 && upperBin < chromagramSize) {
				reassignedMagnitudes[upperBin] += upperContribution * magnitude;
				chromaFrameHistogram[upperBin] += upperContribution;
			}
		}
		// normalization
		for (int i = 0; i < chromagramSize; i++) {
			double weight = chromaFrameHistogram[i];
			if (weight > 0) {
				reassignedMagnitudes[i] /= (double) weight;
			}
		}
		return reassignedMagnitudes;
	}

	// private int linearBinByFrequency(double frequency) {
	// return (int) Math.round(windowSize * frequency);
	// }

	/**
	 * @param frequency normalized frequency
	 */
	private int musicalBinByFrequency(double frequency) {
		return (int) FastMath.round(log2(frequency * relativeBaseFreq)
			* tonesPerOctave * binsPerTone);
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
}
