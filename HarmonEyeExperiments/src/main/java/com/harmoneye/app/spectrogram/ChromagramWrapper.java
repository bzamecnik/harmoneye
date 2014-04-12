package com.harmoneye.app.spectrogram;

import org.apache.commons.math3.util.FastMath;

import com.harmoneye.math.Modulo;

public class ChromagramWrapper {

	private boolean circleOfFifthsEnabled;

	private int wrappedBinsPerTone;
	private int octaveBinShift;
	private int binsPerWrappedOctave;
	private int maxSourceIndex;

	private double binsPerToneInv;

	private boolean tonesAtZero;

	public ChromagramWrapper(int tonesPerOctave, int binsPerTone,
		int wrappedBinsPerTone, int chromaSpectrumSize, int octaveBinShift,
		boolean circleOfFifthsEnabled, boolean tonesAtZero) {

		this.wrappedBinsPerTone = wrappedBinsPerTone;
		this.octaveBinShift = octaveBinShift;
		this.circleOfFifthsEnabled = circleOfFifthsEnabled;
		this.tonesAtZero = tonesAtZero;

		binsPerToneInv = 1.0 / binsPerTone;
		binsPerWrappedOctave = tonesPerOctave * wrappedBinsPerTone;
		int binsPerOctave = tonesPerOctave * binsPerTone;
		// skip the last octave since it typically do not contain fundamentals
		int sourceOctaves = (chromaSpectrumSize / binsPerOctave) - 1;
		maxSourceIndex = sourceOctaves * binsPerOctave;
	}

	public double[] wrap(double[] chromaSpectrum, double[] wrappedChromaSpectrum) {
		for (int srcBin = 0; srcBin < maxSourceIndex; srcBin++) {
			double value = chromaSpectrum[srcBin];
			int wrappedDestBin = (int) FastMath.floor(wrapBin(srcBin));
			wrappedChromaSpectrum[wrappedDestBin] += value;
		}

		return wrappedChromaSpectrum;
	}

	public double wrapBin(double sourceBin) {
		double srcPitch = sourceBin * binsPerToneInv - octaveBinShift;
		int srcPitchInt = (int) FastMath.round(srcPitch);
		if (circleOfFifthsEnabled) {
			srcPitch = srcPitchInt * 7 + (srcPitch - srcPitchInt);
			// map F as the first bin (make C key continuous)
			// (F C G D A E B ...)
			// srcPitch += 1;
		}
		double destBin = (srcPitch + (tonesAtZero ? 0 : 0.5)) * wrappedBinsPerTone;
		double wrappedDestBin = Modulo.modulo(destBin, binsPerWrappedOctave);
		return wrappedDestBin;
	}
}
