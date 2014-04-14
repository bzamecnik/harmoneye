package com.harmoneye.analysis;

import com.harmoneye.math.filter.ExpSmoother;
import com.harmoneye.math.window.BlackmanWindow;
import com.harmoneye.math.window.WindowSampler;

class KeyDetector {
	private final int binsPerHalfTone;
	private final int octaveSize;

	private KeyClassifier classifier = new KeyClassifier();
	private ExpSmoother smoother;
	private double[] window;
	private double[] pitchClasses;

	public KeyDetector(int binsPerHalfTone, int octaveSize) {
		this.binsPerHalfTone = binsPerHalfTone;
		this.octaveSize = octaveSize;
		smoother = new ExpSmoother(octaveSize, 0.0005);
		window = new WindowSampler()
		.sampleWindow(new BlackmanWindow(), binsPerHalfTone);
		pitchClasses = new double[octaveSize];
	}
	
	public int detectKey(double[] octaveBins) {
		int sourceIndex = 0;
		for (int pitchClass = 0; pitchClass < octaveSize; pitchClass++) {
			double sum = 0;
			for (int bin = 0; bin < binsPerHalfTone; bin++, sourceIndex++) {
				sum += window[bin] * octaveBins[sourceIndex];
			}
			pitchClasses[pitchClass] = sum;
		}
		pitchClasses = smoother.smooth(pitchClasses);
		return classifier.classifyKey(pitchClasses);
	}
}