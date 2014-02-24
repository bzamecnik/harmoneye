package com.harmoneye.app.spectrogram;

import com.harmoneye.math.fft.ShortTimeFourierTransform;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.math.window.BlackmanWindow;
import com.harmoneye.math.window.HammingWindow;
import com.harmoneye.math.window.HannWindow;

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
		return MagnitudeSpectrogram
			.fromComplexSpectrogram(computeSpectrogram(audio));
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
}
