package com.harmoneye.ng;

import com.harmoneye.app.spectrogram.SampledAudio;
import com.harmoneye.math.matrix.DVector;

class BasicAudioFrameProvider implements AudioFrameProvider {
	private SampledAudio audio;
	private int sampleCount;
	private double[] allSamples;
	private int frameCount;
	private int frameSize;
	private int hopSize;

	public BasicAudioFrameProvider(SampledAudio audio, int windowSize, double overlapFactor) {
		sampleCount = audio.getLength();
		allSamples = audio.getSamples();
		this.frameSize = windowSize;
//		frameCount = (int) Math
//			.ceil(audio.getLength() / (double) frameSize);
		double overlapRatio = 1 - (1.0 / overlapFactor);
		this.hopSize = (int) (windowSize * (1 - overlapRatio));
		int offset = 1;
		frameCount = (int) Math
			.floor((audio.getLength() - offset - windowSize) / (double) hopSize) + 1;
	}

	public int getFrameCount() {
		return frameCount;
	}

	@Override
	public double[] getFrame(int frameIndex, double[] samples) {
		samples = DVector.ensureSize(frameSize, samples);
		int srcPos = hopSize * frameIndex;
		int length = Math.min(frameSize, sampleCount - srcPos);
		System.arraycopy(allSamples, srcPos, samples, 0, length);
		return samples;
	}
}