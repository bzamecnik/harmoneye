package com.harmoneye.ng;

interface AudioFrameProvider {
	double[] getFrame(int frameIndex, double[] outSamples);
}