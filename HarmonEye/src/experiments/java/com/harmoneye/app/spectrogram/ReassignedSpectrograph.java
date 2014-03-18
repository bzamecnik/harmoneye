package com.harmoneye.app.spectrogram;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.util.FastMath;

import com.harmoneye.math.L2Norm;
import com.harmoneye.math.fft.ShortTimeFourierTransform;
import com.harmoneye.math.filter.Normalizer;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.math.window.BlackmanWindow;

public class ReassignedSpectrograph implements MagnitudeSpectrograph {

	private int windowSize;
	private int hopSize;

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
	private int binsPerOctave;

	/** a single frame of time-domain signal (amplitudes) of windowSize length */
	private double[] amplitudeFrame;
	/** a single frame of reassigned frequencies (only the positive freq. half) */
	private double[] freqEstimates;
	private double[] groupDelays;
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
	private HarmonicCorrelation harmonicCorrelation;
	private HighPassFilter highPassFilter = new HighPassFilter(boxFilterSize);
	private ChromagramWrapper chromagramWrapper;
	private Normalizer l2Normalizer = new Normalizer(new L2Norm(),
		normalizationThreshold);
	// private Normalizer maxNormalizer = new Normalizer(new MaxNorm(),
	// normalizationThreshold);

	public ReassignedSpectrograph(int windowSize, double overlapRatio,
		double sampleRate) {
		this.windowSize = windowSize;
		this.hopSize = (int) (windowSize * (1 - overlapRatio));
		this.normalizedBaseFreq = baseFrequency / sampleRate;
		this.normalizedBaseFreqInv = 1.0 / normalizedBaseFreq;
		binsPerOctave = binsPerTone * tonesPerOctave;
		double bin = musicalBinByFrequency(0.5 - normalizedBaseFreq);
		int positiveFreqCount = windowSize / 2;
		chromagramSize = (int) FastMath.round(bin);
		wrappedChromagramSize = tonesPerOctave * wrappedBinsPerTone;

		amplitudeFrame = new double[windowSize];
		freqEstimates = new double[positiveFreqCount];
		groupDelays = new double[positiveFreqCount];
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
		System.out.println("overlapRatio: " + overlapRatio);
		System.out.println("hopSize: " + hopSize);
		System.out.println("chromagram size: " + chromagramSize);

		this.fft = new ShortTimeFourierTransform(windowSize,
			new BlackmanWindow());

		
		HarmonicPattern harmonicPattern = new HarmonicPattern(harmonicCount,
			binsPerOctave);
		harmonicCorrelation = new HarmonicCorrelation(harmonicPattern,
			chromagramSize);

		chromagramWrapper = new ChromagramWrapper(tonesPerOctave, binsPerTone,
			wrappedBinsPerTone, chromagramSize, octaveBinShift,
			circleOfFifthsEnabled);
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

		for (int i = 0; i < frameCount; i++) {
			computeFrame(amplitudes, reassignedMagFrames, i);

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

	private void computeFrame(double[] amplitudes,
		double[][] reassignedMagFrames, int i) {
		// Make a phase difference of two frames shifted by one sample.
		// We don't actually need the sample at position 0 since the window
		// is almost zero there anyway. It introduces almost no error.
		// Thus data from a single frame is practically sufficient.
		int srcIndex = i * hopSize;
		System.arraycopy(amplitudes, srcIndex, amplitudeFrame, 0, windowSize);
		transformFrame(amplitudeFrame, spectrum);

		SpectralReassigner.shiftRight(amplitudeFrame);
		transformFrame(amplitudeFrame, prevTimeSpectrum);

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
		groupDelays = SpectralReassigner.estimateGroupDelays(crossFreqSpectrum,
			groupDelays);
		secondDerivatives = SpectralReassigner
			.estimateSecondDerivatives(crossFreqTimeSpectrum, secondDerivatives);
		magnitudeSpectrum = magnitudes(spectrum, magnitudeSpectrum);

		reassignMagnitudes(magnitudeSpectrum,
			freqEstimates,
			groupDelays,
			secondDerivatives,
			reassignedMagFrames,
			i);
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

			if (correlationEnabled) {
				computeHarmonicCorrelation(chromagram);
			}

			if (octaveWrapEnabled) {
				double[] wrappedChromagram = outputFrames[frameIndex];
				chromagram = chromagramWrapper.wrap(chromagram, wrappedChromagram);
			}

			if (normalizationEnabled) {
				l2Normalizer.filter(chromagram);
			}

			if (postScalingFactor != 1) {
				for (int i = 0; i < chromagram.length; i++) {
					chromagram[i] *= postScalingFactor;
				}
			}

			outputFrames[frameIndex] = chromagram;
		}
		return outputFrames;
	}

	private void computeHarmonicCorrelation(double[] chromagram) {
		double[] correlation = harmonicCorrelation.correlate(chromagram);
		// maxNormalizer.filter(correlation);

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
	private double musicalBinByFrequency(double frequency) {
		return FastMath.log(2, frequency * normalizedBaseFreqInv)
			* binsPerOctave;
	}

	private double[] magnitudes(ComplexVector frame, double[] magnitudes) {
		return MagnitudeSpectrogram.toLogPowerSpectrum(frame, magnitudes);
	}

	private ComplexVector transformFrame(double[] amplitudeFrame,
		ComplexVector spectrum) {
		return fft.transform(amplitudeFrame, spectrum);
	}

}
