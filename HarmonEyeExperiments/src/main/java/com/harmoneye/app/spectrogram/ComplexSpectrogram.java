package com.harmoneye.app.spectrogram;

import com.harmoneye.math.matrix.ComplexVector;

public class ComplexSpectrogram {

	private ComplexVector[] spectrumFrames;
	private int binCount;

	public ComplexSpectrogram(ComplexVector[] spectrumFrames, int binCount) {
		this.spectrumFrames = spectrumFrames;
		this.binCount = binCount;
	}

	public ComplexVector getFrame(int i) {
		return spectrumFrames[i];
	}

	public int getFrameCount() {
		return spectrumFrames.length;
	}

	public int getBinCount() {
		return binCount;
	}

}