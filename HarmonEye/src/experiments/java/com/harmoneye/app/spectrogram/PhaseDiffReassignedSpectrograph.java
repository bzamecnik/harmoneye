package com.harmoneye.app.spectrogram;

import com.harmoneye.math.Modulo;
import com.harmoneye.math.fft.ShortTimeFourierTransform;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.math.matrix.DComplex;
import com.harmoneye.math.window.BlackmanWindow;

public class PhaseDiffReassignedSpectrograph implements MagnitudeSpectrograph {

	private int windowSize;
	private int hopSize;
	private ShortTimeFourierTransform fft;

	public PhaseDiffReassignedSpectrograph(int windowSize, double overlapRatio) {
		this.windowSize = windowSize;
		this.hopSize = (int) (windowSize * (1 - overlapRatio));
		System.out.println("windowSize:" + windowSize);
		System.out.println("overlapRatio:" + overlapRatio);
		System.out.println("hopSize:" + hopSize);
		this.fft = new ShortTimeFourierTransform(windowSize,
			new BlackmanWindow());
	}

	public MagnitudeSpectrogram computeMagnitudeSpectrogram(SampledAudio audio) {
		// -1 to allow the frames shifted by one sample
		int offset = 1;
		int frameCount = (int) Math.floor((audio.getLength() - offset - windowSize)
			/ (double) hopSize) + 1;
		System.out.println("framecount:" + frameCount);

		double[] amplitudes = audio.getSamples();
		double[] amplitudeFrame = new double[windowSize];
		double[][] reassignedMagFrames = new double[frameCount][];
		int positiveFreqCount = windowSize / 2;
		double[] freqEstimates = new double[positiveFreqCount];
		double[] magnitudes = new double[positiveFreqCount];

		for (int i = 0; i < frameCount; i++) {
			ComplexVector frame = computeFrame(amplitudes, amplitudeFrame, i
				* hopSize);
			ComplexVector nextFrame = computeFrame(amplitudes,
				amplitudeFrame,
				i * hopSize + offset);

			freqEstimates = estimateFreqs(frame, nextFrame, freqEstimates);
			magnitudes = magnitudes(frame, magnitudes);

			reassignedMagFrames[i] = reassignMagnitudes(magnitudes,
				freqEstimates);
		}

		return new MagnitudeSpectrogram(reassignedMagFrames, positiveFreqCount);
	}

	private double[] reassignMagnitudes(double[] magnitudes,
		double[] freqEstimates) {
		int length = magnitudes.length;
		double[] reassignedMagnitudes = new double[length];
		for (int i = 0; i < length; i++) {
			int targetBin = linearBinByFrequency(freqEstimates[i], length);
//			if (Math.abs(targetBin - i) / (double) length > 0.01) {
//				targetBin = i;
//			}
			if (targetBin < 0 || targetBin >= length) {	
				targetBin = i;
			}
			reassignedMagnitudes[targetBin] += magnitudes[i];
		}
		return reassignedMagnitudes;
	}

	private int linearBinByFrequency(double frequency, int length) {
		return (int) Math.round(windowSize * frequency);
	}

	// High-precision frequency estimates using single-sample phase difference.
	// Frequencies are normalized: [0.0; 1.0] means [0.0; sampleRate]. 
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
			double freq = Modulo.modulo(phaseDiff / (2 * Math.PI), 1.0);
//			if (freq > 0.5) {
//				freq = 0.5 - freq;
//			}
			freqEstimates[i] = freq;
		}
		return freqEstimates;
	}

	private double[] magnitudes(ComplexVector frame, double[] magnitudes) {
		return MagnitudeSpectrogram.toLogMagnitudeSpectrum(frame, magnitudes);
	}

	private ComplexVector computeFrame(double[] amplitudes,
		double[] amplitudeFrame, int index) {
		System.arraycopy(amplitudes, index, amplitudeFrame, 0, windowSize);
		return new ComplexVector(fft.transform(amplitudeFrame));
	}
}
