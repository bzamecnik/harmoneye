package com.harmoneye.app.tuner;

import com.harmoneye.app.spectrogram.MagnitudeSpectrogram;
import com.harmoneye.audio.SoundConsumer;
import com.harmoneye.math.MaxNorm;
import com.harmoneye.math.Norm;
import com.harmoneye.math.fft.ShortTimeFourierTransform;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.math.window.BlackmanWindow;

public class StftTuningAnalyzer implements SoundConsumer {

	private ShortTimeFourierTransform fft;
	private double[] spectrum;
	private ComplexVector cplxSpectrum;

	private Norm maxNorm = new MaxNorm();

	private double pitch;
	private double sampleRate;
	private double windowSize;

	public StftTuningAnalyzer(int windowSize, double sampleRate) {
		this.windowSize = windowSize;
		this.sampleRate = sampleRate;
		
		fft = new ShortTimeFourierTransform(windowSize, new BlackmanWindow());
		spectrum = new double[windowSize / 2];
		cplxSpectrum = new ComplexVector(windowSize);
	}

	@Override
	public void consume(double[] samples) {
		fft.transform(samples, cplxSpectrum);
		MagnitudeSpectrogram.toLogPowerSpectrum(cplxSpectrum, spectrum);
		int maxBin = findMaxBin(spectrum);
		pitch = frequencyForBin(maxBin);
	}

	private double frequencyForBin(int bin) {
		return (float)bin * sampleRate / windowSize; 
	}

	private int findMaxBin(double[] values) {
		double max = maxNorm.norm(values);
		if (max > 1e-6) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] == max) {
					return i;
				}
			}
		}
		return 0;
	}

	public void stop() {
		// TODO Auto-generated method stub

	}

	public void start() {
		// TODO Auto-generated method stub

	}

	public double getDetectedPitch() {
		return pitch;
	}

	public double[] getSpectrum() {
		return spectrum;
	}
}
