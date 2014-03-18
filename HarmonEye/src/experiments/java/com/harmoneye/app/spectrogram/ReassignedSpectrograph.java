package com.harmoneye.app.spectrogram;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.util.FastMath;

import com.harmoneye.math.Modulo;
import com.harmoneye.math.fft.ShortTimeFourierTransform;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.math.window.BlackmanWindow;

public class ReassignedSpectrograph implements MagnitudeSpectrograph {

	private static final double TWO_PI_INV = 1 / (2 * Math.PI);

	private int windowSize;
	private int hopSize;

	/** frequency of the lowest chromagram bin */
	private double baseFrequency = 110.0 / 4;
	private int tonesPerOctave = 12;
	private int binsPerTone = 10;
	private int wrappedBinsPerTone = 1; // 1 for plain PCP
	private int octaveBinShift = 3; // A -> C
	private int harmonicCount = 20;
	private boolean transientFilterEnabled = true;
	// minimum threshold for secondDerivatives [0; 1] to consider the
	// energy as not being a transient
	double transientThreshold = 0.4;
	private boolean magnitudeSquaringEnabled = true;
	private boolean highPassFilterEnabled = true;
	private int boxFilterSize = 10;
	private boolean correlationEnabled = true;
	private boolean octaveWrapEnabled = true;
	private boolean circleOfFifthsEnabled = true;
	// enable L2-norm normalization, otherwise use just plain constant scaling
	private boolean normalizationEnabled = true;
	// prevent zero-division - zero out too weak signals
	private double normalizationThreshold = 1e-2;
	/** just to scale the chromagram to the [0; 1.0] range of PNG... */
	private double postScalingFactor = 1;

	/** ratio of the baseFrequency to the sampleRate */
	private double normalizedBaseFreq;
	private double normalizedBaseFreqInv;
	/** size of the target chromagram (log-frequency resampled spectrogram) */
	private int chromagramSize;
	private int wrappedChromagramSize;

	/** a single frame of time-domain signal (amplitudes) of windowSize length */
	private double[] amplitudeFrame;
	/** a single frame of reassigned frequencies (only the positive freq. half) */
	private double[] freqEstimates;
	private double[] groupDelays;
	private double[] secondDerivatives;
	/** a single frame of magnitudes (only the positive freq. half) */
	private double[] magnitudeSpectrum;

	private ShortTimeFourierTransform fft;
	private HarmonicCorrellation harmonicCorrellation;
	private HighPassFilter highPassFilter = new HighPassFilter(boxFilterSize);	

	public ReassignedSpectrograph(int windowSize, double overlapRatio,
		double sampleRate) {
		this.windowSize = windowSize;
		this.hopSize = (int) (windowSize * (1 - overlapRatio));
		this.normalizedBaseFreq = baseFrequency / sampleRate;
		this.normalizedBaseFreqInv = 1.0 / normalizedBaseFreq;
		double bin = musicalBinByFrequency(0.5 - normalizedBaseFreq);
		int positiveFreqCount = windowSize / 2;
		chromagramSize = (int) FastMath.round(bin);
		wrappedChromagramSize = tonesPerOctave * wrappedBinsPerTone;

		amplitudeFrame = new double[windowSize];
		freqEstimates = new double[positiveFreqCount];
		groupDelays = new double[positiveFreqCount];
		secondDerivatives = new double[positiveFreqCount];
		magnitudeSpectrum = new double[positiveFreqCount];

		System.out.println("sampleRate: " + sampleRate);
		System.out.println("windowSize: " + windowSize);
		System.out.println("overlapRatio: " + overlapRatio);
		System.out.println("hopSize: " + hopSize);
		System.out.println("chromagram size: " + chromagramSize);

		this.fft = new ShortTimeFourierTransform(windowSize,
			new BlackmanWindow());

		HarmonicPattern harmonicPattern = new HarmonicPattern(harmonicCount, binsPerTone * tonesPerOctave);
		harmonicCorrellation = new HarmonicCorrellation(harmonicPattern, chromagramSize);
	}

	public MagnitudeSpectrogram computeMagnitudeSpectrogram(SampledAudio audio) {
		// -1 to allow the frames shifted by one sample
		int offset = 1;
		int frameCount = (int) Math
			.floor((audio.getLength() - offset - windowSize) / (double) hopSize) + 1;
		System.out.println("frame count: " + frameCount);
		System.out.println("duration: " + audio.getDurationMillis() + " ms");

		double[] amplitudes = audio.getSamples();
		double[][] reassignedMagFrames = new double[frameCount][chromagramSize];

		System.out.println("Computing spectrogram.");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		int lastPercent = 0;
		double frameCountInv = 1.0 / frameCount;
		ComplexVector spectrum = new ComplexVector(windowSize);
		ComplexVector prevTimeSpectrum = new ComplexVector(windowSize);
		ComplexVector prevFreqSpectrum = new ComplexVector(windowSize);
		ComplexVector crossTimeSpectrum = new ComplexVector(windowSize);
		ComplexVector crossFreqSpectrum = new ComplexVector(windowSize);
		ComplexVector crossFreqTimeSpectrum = new ComplexVector(windowSize);
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
			prevTimeSpectrum = transformFrame(amplitudeFrame, prevTimeSpectrum);

			shiftPrevFreqSpectrum(spectrum, prevFreqSpectrum);

			ComplexVector.crossSpectrum(prevTimeSpectrum,
				spectrum,
				crossTimeSpectrum);
			ComplexVector.crossSpectrum(prevFreqSpectrum,
				spectrum,
				crossFreqSpectrum);
			ComplexVector.crossSpectrum(crossFreqSpectrum,
				crossTimeSpectrum,
				crossFreqTimeSpectrum);

			freqEstimates = estimateFreqsByCrossSpectrum(crossTimeSpectrum,
				freqEstimates);
			groupDelays = estimateGroupDelays(crossFreqSpectrum, groupDelays);
			secondDerivatives = estimateGroupDelays(crossFreqTimeSpectrum,
				secondDerivatives);
			magnitudeSpectrum = magnitudes(spectrum, magnitudeSpectrum);

			reassignMagnitudes(magnitudeSpectrum,
				freqEstimates,
				groupDelays,
				secondDerivatives,
				reassignedMagFrames,
				i);

			double percent = 10 * i * frameCountInv;

			if ((int) percent > lastPercent) {
				System.out.print(".");
				lastPercent = (int) percent;
			}
		}
		System.out.println();

		int outputSize = octaveWrapEnabled ? wrappedChromagramSize
			: chromagramSize;
		double[][] resultFrames = postProcessSpectrogram(reassignedMagFrames,
			frameCount,
			outputSize);

		stopWatch.stop();
		System.out.println("Computed spectrogram in " + stopWatch.getTime()
			+ " ms");
		System.out.println(String.format("Audio time / computation time: %.4f",
			audio.getDurationMillis() / (double) stopWatch.getTime()));

		return new MagnitudeSpectrogram(resultFrames, outputSize);
	}

	private double[] reassignMagnitudes(double[] magnitudes,
		double[] freqEstimates, double[] groupDelays,
		double[] secondDerivatives, double[][] reassignedMagFrames,
		int frameIndex) {
		int length = magnitudes.length;

		double[] reassignedMagnitudes = reassignedMagFrames[frameIndex];
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

			if (magnitudeSquaringEnabled) {
				magnitude *= magnitude; // ^2
			}

			int lowerBin = (int) FastMath.floor(targetBin);
			int upperBin = lowerBin + 1;
			double upperContribution = targetBin - lowerBin;
			double lowerContribution = 1 - upperContribution;

			double groupDelay = groupDelays[i];
			// if (FastMath.abs(groupDelay) < 1e-1) {
			// // in case the sharpening effect would be small we wouldn't
			// // utilize group delay
			// groupDelay = 0;
			// }

			// the overlap factor seems to be already present in the group delay
			if (lowerBin >= 0 && lowerBin < chromagramSize) {
				// reassignedMagnitudes[lowerBin] += lowerContribution *
				// magnitude;
				distributeMagnitudeOverTime(reassignedMagFrames,
					frameIndex,
					lowerBin,
					groupDelay,
					lowerContribution * magnitude);
			}
			if (upperBin >= 0 && upperBin < chromagramSize) {
				// reassignedMagnitudes[upperBin] += upperContribution *
				// magnitude;
				distributeMagnitudeOverTime(reassignedMagFrames,
					frameIndex,
					upperBin,
					groupDelay,
					upperContribution * magnitude);
			}
		}

		return reassignedMagnitudes;
	}

	void distributeMagnitudeOverTime(double[][] frames, int frameIndex,
		int freqBin, double groupDelay, double magnitude) {
		double idealFrameIndex = frameIndex + groupDelay;
		int lowerT = (int) FastMath.floor(idealFrameIndex);
		int upperT = lowerT + 1;
		double upperW = idealFrameIndex - lowerT;
		double lowerW = 1 - upperW;

		int length = frames.length;
		if (lowerT >= 0 && lowerT < length) {
			frames[lowerT][freqBin] += lowerW * magnitude;
		}
		if (upperT >= 0 && upperT < length) {
			frames[upperT][freqBin] += upperW * magnitude;
		}
	}

	private double[][] postProcessSpectrogram(double[][] reassignedMagFrames,
		int frameCount, int outputSize) {
		double[][] outputFrames = reassignedMagFrames;
		if (octaveWrapEnabled) {
			outputFrames = new double[frameCount][outputSize];
		}

		for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
			double[] chromagram = reassignedMagFrames[frameIndex];

			if (highPassFilterEnabled) {
				highPassFilter.filter(chromagram);
			}

			// normalize(reassignedMagnitudes);

			if (correlationEnabled) {
				computeHarmonicCorrellation(chromagram);
			}

			if (octaveWrapEnabled) {
				int octaveBins = tonesPerOctave * wrappedBinsPerTone;
				double binReductionFactorInv = wrappedBinsPerTone
					/ (double) binsPerTone;
				double[] wrappedMagnitudes = outputFrames[frameIndex];
				int sourceOctaves = (chromagramSize / octaveBins) - 1;
				int maxIndex = sourceOctaves * octaveBins;
				int middleBin = binsPerTone / 2;
				for (int i = 0; i < maxIndex; i++) {
					double value = chromagram[i];
					int k = (int) ((i + middleBin) * binReductionFactorInv)
						+ octaveBins - octaveBinShift * wrappedBinsPerTone;
					if (circleOfFifthsEnabled) {
						k = k * 7;
						// map F as the first bin (make C key continuous)
						// (F C G D A E B ...)
						k += 1;
					}
					wrappedMagnitudes[k % octaveBins] += value;
				}

				chromagram = wrappedMagnitudes;
			}

			if (normalizationEnabled) {
				normalize(chromagram);
			}
			if (postScalingFactor != 1) {
				for (int i = 1; i < chromagram.length; i++) {
					chromagram[i] *= postScalingFactor;
				}
			}

			// threshold(reassignedMagnitudes);

			outputFrames[frameIndex] = chromagram;
		}
		return outputFrames;
	}

	private double[] shiftPrevFreqSpectrum(ComplexVector spectrum,
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
	private void shiftRight(double[] values) {
		System.arraycopy(values, 0, values, 1, values.length - 1);
		values[0] = 0;
	}

	private double log2(double value) {
		return FastMath.log(2, value);
	}

	private void normalize(double[] values) {
		// L2 norm
		double norm = 0;
		for (int i = 0; i < values.length; i++) {
			double value = values[i];
			norm += value * value;
		}
		norm = FastMath.sqrt(norm);
		double normInv = (norm > normalizationThreshold) ? 1 / norm : 0;
		for (int i = 0; i < values.length; i++) {
			values[i] *= normInv;
		}
	}

	private void computeHarmonicCorrellation(double[] chromagram) {
		double[] correlation = harmonicCorrellation.correlate(chromagram);
		// mask out the chromagram by the correlation
		// max filter with kernel size 3
		for (int i = 0; i < chromagramSize; i++) {
			double max = correlation[i];
			if (i - 1 >= 0) {
				max = FastMath.max(max, correlation[i - 1]);
			}
			if (i + 1 < chromagramSize) {
				max = FastMath.max(max, correlation[i + 1]);
			}
			chromagram[i] *= max;
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
	@SuppressWarnings("unused")
	@Deprecated
	private double[] estimateFreqsByPhaseDiff(ComplexVector frame,
		ComplexVector nextFrame, double[] freqEstimates) {
		int length = freqEstimates.length;
		double[] frameElems = frame.getElements();
		double[] nextFrameElems = nextFrame.getElements();
		for (int i = 0; i < length; i++) {
			int reIndex = 2 * i;
			int imIndex = reIndex + 1;
			double phase = FastMath.atan2(frameElems[imIndex],
				frameElems[reIndex]);
			double nextPhase = FastMath.atan2(nextFrameElems[imIndex],
				nextFrameElems[reIndex]);
			double phaseDiff = nextPhase - phase;
			double freq = Modulo.modulo(phaseDiff * TWO_PI_INV, 1.0);
			freqEstimates[i] = freq;
		}
		return freqEstimates;
	}

	// chanelled instantious frequency - derivative of phase by time
	// cif = angle(crossSpectrumTime) * sampleRate / (2 * pi);
	// in this case the return value is normalized (not multiples by sampleRate)
	// [0.0; 1.0] instead of absolute [0.0; sampleRate]
	private double[] estimateFreqsByCrossSpectrum(
		ComplexVector crossTimeSpectrum, double[] freqEstimates) {
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
	private double[] estimateGroupDelays(ComplexVector crossFreqSpectrum,
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

	private double[] magnitudes(ComplexVector frame, double[] magnitudes) {
		return MagnitudeSpectrogram.toLogMagnitudeSpectrum(frame, magnitudes);
	}

	private ComplexVector transformFrame(double[] amplitudeFrame,
		ComplexVector spectrum) {
		return fft.transform(amplitudeFrame, spectrum);
	}

}
