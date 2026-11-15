package com.harmoneye.app.spectrogram;

import com.harmoneye.analysis.MagnitudeSpectrogram;
import com.harmoneye.math.fft.ShortTimeFourierTransform;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.math.window.BlackmanWindow;

public class BasicSpectrograph implements MagnitudeSpectrograph {

	private int windowSize;
	private int hopSize;
	private ShortTimeFourierTransform fft;

	public BasicSpectrograph(int windowSize, double overlapRatio) {
		this.windowSize = windowSize;
		this.hopSize = (int) (windowSize * (1 - overlapRatio));
		System.out.println("windowSize:" + windowSize);
		System.out.println("overlapRatio:" + overlapRatio);
		System.out.println("hopSize:" + hopSize);
		this.fft = new ShortTimeFourierTransform(windowSize,
			new BlackmanWindow());
	}

	public MagnitudeSpectrogram computeMagnitudeSpectrogram(SampledAudio audio) {
		return 
			fromComplexSpectrogram(computeSpectrogram(audio));
	}

	public ComplexSpectrogram computeSpectrogram(SampledAudio audio) {
		int frameCount = (int) Math.floor((audio.getLength() - windowSize)
			/ hopSize) + 1;
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
		return new ComplexSpectrogram(spectrumFrames, windowSize);
	}
	
	public static MagnitudeSpectrogram fromComplexSpectrogram(
		ComplexSpectrogram spectrogram) {
		// only positive frequencies
		int binCount = spectrogram.getBinCount() / 2;
		int frameCount = spectrogram.getFrameCount();
		double[][] magnitudeFrames = new double[frameCount][];
		for (int i = 0; i < frameCount; i++) {
			ComplexVector complexFrame = spectrogram.getFrame(i);
			magnitudeFrames[i] = MagnitudeSpectrogram.toLogPowerSpectrum(complexFrame,
				new double[binCount]);
		}
		return new MagnitudeSpectrogram(magnitudeFrames, binCount);
	}
}
