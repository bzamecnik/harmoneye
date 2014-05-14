package com.harmoneye.ng;

import java.util.Arrays;

import com.harmoneye.analysis.MagnitudeSpectrogram;
import com.harmoneye.analysis.StreamingReassignedSpectrograph;
import com.harmoneye.analysis.StreamingReassignedSpectrograph.OutputFrame;
import com.harmoneye.math.fft.ShortTimeFourierTransform;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.ng.ChromaExtractor.Chroma;

public class ChromaExtractor implements FeatureExtractor<Chroma> {

	private static final int OCTAVE_SIZE = 12;
	private StreamingReassignedSpectrograph chromagraph;

	public ChromaExtractor(StreamingReassignedSpectrograph chromagraph,
		int windowSize) {
		this.chromagraph = chromagraph;
	}

	@Override
	public Chroma extract(double[] samples) {
		OutputFrame outputFrame = chromagraph.computeChromagram(samples);
		double[] preciseChroma = outputFrame.getWrappedChromagram();
		double[] fullPreciseChroma = outputFrame.getChromagram();
		double[] chroma = summarizePreciseChroma(preciseChroma);
		return new Chroma(chroma, preciseChroma, fullPreciseChroma);
	}

	private double[] summarizePreciseChroma(double[] preciseChroma) {
		double[] chroma = new double[OCTAVE_SIZE];
		int binsPerTone = preciseChroma.length / OCTAVE_SIZE;
		for (int tone = 0; tone < OCTAVE_SIZE; tone++) {
			double sum = 0;
			for (int bin = 0; bin < binsPerTone; bin++) {
				sum += preciseChroma[tone * binsPerTone + bin];
			}
			chroma[tone] = sum;
		}
		return chroma;
	}

	/** Represents a pitch class histogram (with various resolutions). */
	public static class Chroma {
		/** 1 bin per pitch class (wrapped to a single octave) */
		private double[] chroma;
		/** N bins per pitch class (wrapped to a single octave) */
		private double[] preciseChroma;
		/** N bins per pitch class (unwrapped) */
		private double[] fullPreciseChroma;

		public Chroma(double[] chroma, double[] preciseChroma,
			double[] fullPreciseChroma) {
			this.chroma = chroma;
			this.preciseChroma = preciseChroma;
			this.fullPreciseChroma = fullPreciseChroma;
		}

		public double[] getChroma() {
			return chroma;
		}

		public double[] getPreciseChroma() {
			return preciseChroma;
		}

		public double[] getFullPreciseChroma() {
			return fullPreciseChroma;
		}

		@Override
		public String toString() {
			return "Chroma [chroma=" + Arrays.toString(chroma)
				+ ", preciseChroma=" + Arrays.toString(preciseChroma)
				+ ", fullPreciseChroma=" + Arrays.toString(fullPreciseChroma)
				+ "]";
		}
	}
}
