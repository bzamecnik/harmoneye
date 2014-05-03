package com.harmoneye.ng;

import java.util.Arrays;

import com.harmoneye.analysis.MagnitudeSpectrogram;
import com.harmoneye.analysis.StreamingReassignedSpectrograph;
import com.harmoneye.analysis.StreamingReassignedSpectrograph.OutputFrame;
import com.harmoneye.math.fft.ShortTimeFourierTransform;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.ng.ChromagramExtractor.Chromagram;

public class ChromagramExtractor implements FeatureExtractor<Chromagram> {

	private StreamingReassignedSpectrograph chromagraph;

	public ChromagramExtractor(StreamingReassignedSpectrograph chromagraph,
		int windowSize) {
		this.chromagraph = chromagraph;
	}

	@Override
	public Chromagram extract(double[] samples) {
		OutputFrame outputFrame = chromagraph.computeChromagram(samples);
		return new Chromagram(outputFrame.getWrappedChromagram());
	}

	public static class Chromagram {
		private double[] chromagram;

		public Chromagram(double[] chromagram) {
			this.chromagram = chromagram;
		}

		public double[] getChromagram() {
			return chromagram;
		}

		@Override
		public String toString() {
			return Arrays.toString(chromagram);
		}
	}
}
