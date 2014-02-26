package com.harmoneye.app.spectrogram;

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
	private int binsPerTone = 10;
	private boolean octaveWrapEnabled = false;

	/** ratio of the baseFrequency to the sampleRate */
	private double normalizedBaseFreq;
	/** size of the target chromagram (log-frequency resampled spectrogram) */
	private int chromagramSize;

	/** a single frame of time-domain signal (amplitudes) of windowSize length */
	private double[] amplitudeFrame;
	/** a single frame of reassigned frequencies (only the positive freq. half) */
	private double[] freqEstimateFrame;
	/** a single frame of magnitudes (only the positive freq. half) */
	private double[] magnitudeFrame;

	private ShortTimeFourierTransform fft;

	private double normalizedBaseFreqInv;

	/** just to scale the chormagram to the [0; 1.0] range of PNG... */
	private double normalizationFactor = 0.2;

	// private ButterworthFilter lowPassFilter;

	public PhaseDiffReassignedSpectrograph(int windowSize, double overlapRatio,
		double sampleRate) {
		this.windowSize = windowSize;
		this.hopSize = (int) (windowSize * (1 - overlapRatio));
		this.normalizedBaseFreq = baseFrequency / sampleRate;
		this.normalizedBaseFreqInv = 1.0 / normalizedBaseFreq;
		if (octaveWrapEnabled) {
			chromagramSize = tonesPerOctave * binsPerTone;
		} else {
			double bin = musicalBinByFrequency(0.5 - normalizedBaseFreq);
			chromagramSize = (int) FastMath.round(bin);
		}
		int positiveFreqCount = windowSize / 2;

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

		// this.lowPassFilter = new ZeroPhaseFilter(ButterworthFilter());
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

		

		// iterate from 1 to ignore the DC
		for (int i = 1; i < length; i++) {
			// int targetBin = linearBinByFrequency(freqEstimates[i]);

			double targetFreq = freqEstimates[i];
			double targetBin = musicalBinByFrequency(targetFreq);

			if (octaveWrapEnabled) {
				targetBin %= chromagramSize;
			}

			// if (Math.abs(targetBin - i) / (double) length > 0.01) {
			// targetBin = i;
			// }
			if (targetBin < 0 || targetBin >= chromagramSize) {
				continue;
			}
			// if (Math.abs(sourceBin - targetBin) > 2 * binsPerTone) {
			// targetBin = sourceBin;
			// }

			double magnitude = normalizationFactor  * magnitudes[i];
//			
//			reassignedMagnitudes[(int)targetBin] += magnitude;
//			
			
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

		// double[] reassignedMagnitudesLowPass =
		// lowPassFilter.filter(reassignedMagnitudes);
		// System.arraycopy(reassignedMagnitudesLowPass, 0,
		// reassignedMagnitudes, 0, reassignedMagnitudes.length);

		return reassignedMagnitudes;
	}

	// private int linearBinByFrequency(double frequency) {
	// return (int) Math.round(windowSize * frequency);
	// }

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
}
