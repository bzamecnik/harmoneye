package com.harmoneye.analysis;

import org.apache.commons.math3.util.FastMath;

import com.harmoneye.audio.analysis.ScalarExpSmoother;
import com.harmoneye.math.cqt.CqtContext;

public class HarmonicPatternPitchClassDetector {

	private static final int DEFAULT_HARMONIC_COUNT = 7;
	private static final double HARMONIC_WEIGHT_FALLOFF = 0.4;

	private ScalarExpSmoother smoother = new ScalarExpSmoother(0.1);

	private int harmonicCount;
	private int binsPerOctave;
	private int binsPerHalftoneHalf;
	private double baseFreq;
	private double baseFreqInv;
	private double harmonicCountMinusOneInv;

	private double[] filteredBins;
	private int[] harmonicBinsIndexes;
	private CqtContext ctx;

	public HarmonicPatternPitchClassDetector(CqtContext ctx) {
		this(ctx, DEFAULT_HARMONIC_COUNT);
	}

	public HarmonicPatternPitchClassDetector(CqtContext ctx, int harmonicCount) {
		this.ctx = ctx;
		this.harmonicCount = harmonicCount;
		binsPerOctave = ctx.getBinsPerOctave();
		binsPerHalftoneHalf = ctx.getBinsPerHalftone() / 2;
		baseFreq = ctx.getBaseFreq();
		baseFreqInv = 1.0 / baseFreq;

		harmonicBinsIndexes = new int[harmonicCount];
		for (int i = 0; i < harmonicCount; i++) {
			harmonicBinsIndexes[i] = harmonicFreqBin(ctx.getBaseFreq(), i + 1);
		}

		harmonicCountMinusOneInv = 1.0 / (harmonicCount - 1);
	}

	/**
	 * detect fundamental frequencies for bins within pitch classes via pattern
	 * matching of the harmonics
	 * 
	 * @param cqBins
	 * @return
	 */
	public double[] detectPitchClasses(double[] cqBins) {
		if (filteredBins == null) {
			filteredBins = new double[cqBins.length];
		}

		double[] octaveBins = new double[cqBins.length];
		for (int i = 0; i < cqBins.length; i++) {
			octaveBins[i] = extractHarmonics(cqBins, i, harmonicCount);
		}

		// normalize
		
		// TODO: this kind of normalization is bad and unstable
		
		double harmonicMax = 0;
		double cqMax = 0;
		for (int i = 0; i < octaveBins.length; i++) {
			harmonicMax = FastMath.max(harmonicMax, octaveBins[i]);
			cqMax = FastMath.max(cqMax, cqBins[i]);
		}
		if (harmonicMax > 0) {
			double factor = cqMax < 1 ? cqMax / harmonicMax : 1 / cqMax;
//			double smoothedFactor = smoother.smooth(factor);
			for (int i = 0; i < octaveBins.length; i++) {
				octaveBins[i] *= factor;
			}
		}

		return octaveBins;
	}

	private double extractHarmonics(double[] cqBins, int baseFreqBin,
		int harmonicCount) {
		double dotProduct = cqBins[baseFreqBin];
		int centerFreqBin = baseFreqBin - ctx.getBinsPerHalftone() / 2;
		for (int i = 1; i <= harmonicCount; i++) {
			int harmonicBin = centerFreqBin + harmonicBinsIndexes[i - 1];
			if (harmonicBin < cqBins.length) {
				double weight = 1 - HARMONIC_WEIGHT_FALLOFF * i
					* harmonicCountMinusOneInv;
				// TODO: interpolate the bin values using a continuous bin index
				dotProduct += weight * cqBins[harmonicBin];
			}
		}
		return dotProduct;
	}

	/**
	 * @param frequency of the first harmonic
	 * @param harmonicIndex one-based index of the desired k-th harmonic
	 * @return zero-based cq bin index of the k-th harmonic frequency
	 */
	private int harmonicFreqBin(double frequency, int harmonicIndex) {
		return (int) FastMath.round(binsPerOctave
			* FastMath.log(2, harmonicIndex * frequency * baseFreqInv))
			+ binsPerHalftoneHalf;
	}
}
