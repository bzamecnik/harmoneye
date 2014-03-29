package com.harmoneye.app.spectrogram;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

public class HarmonicPattern {

	private int[] binIndexes;
	private double[] binWeights;

	private HarmonicPattern(int[] binIndexes, double[] binWeights) {
		this.binIndexes = binIndexes;
		this.binWeights = binWeights;
	}

	public int[] getIndexes() {
		return binIndexes;
	}

	public double[] getWeights() {
		return binWeights;
	}

	public static HarmonicPattern create(int harmonicCount, int binsPerOctave) {
		return new Builder(harmonicCount, binsPerOctave).build();
	}

	private static class Builder {
		List<Integer> binIndexes = new ArrayList<Integer>();
		List<Double> binWeights = new ArrayList<Double>();
		private int harmonicCount;
		private double binsPerOctave;

		private HarmonicPattern instance;

		public Builder(int harmonicCount, int binsPerOctave) {
			this.harmonicCount = harmonicCount;
			this.binsPerOctave = binsPerOctave;

			addPatternForFreq(1.0, 1);
			addPatternForFreq(1.0 / 2, -1);
			addPatternForFreq(1.0 / 3, -1);
			// addPatternForFreq(1.0 / 4, -1);
			// addPatternForFreq(1.0 / 5, -1);
			// addPatternForFreq(1.0 / 6, -1);
			// addPatternForFreq(1.0 / 7, -1);
			// addPatternForFreq(2.0, -1);
			// addPatternForFreq(3.0, -1);

			int[] binIndexesArray = new int[binIndexes.size()];
			for (int i = 0; i < binIndexesArray.length; i++) {
				binIndexesArray[i] = binIndexes.get(i);
			}
			double[] binWeightsArray = new double[binWeights.size()];
			for (int i = 0; i < binWeightsArray.length; i++) {
				binWeightsArray[i] = binWeights.get(i);
			}
			instance = new HarmonicPattern(binIndexesArray, binWeightsArray);
		}

		public HarmonicPattern build() {
			return instance;
		}

		private void addPatternForFreq(double baseFreq, double sign) {
			double normalizationFactor = 0;
			for (int i = 0; i < harmonicCount; i++) {
				normalizationFactor += FastMath
					.abs(sign * weightForHarmonic(i));
			}
			normalizationFactor = sign / normalizationFactor;

			for (int i = 0; i < harmonicCount; i++) {

				double bin = FastMath.log(2, baseFreq * (i + 1))
					* binsPerOctave;

				int lowerBin = (int) FastMath.floor(bin);
				// int upperBin = lowerBin + 1;
				double weight = normalizationFactor * weightForHarmonic(i);

				if (weight != 0 && !binIndexes.contains(lowerBin)) {
					binIndexes.add(lowerBin);
					binWeights.add(weight);
				}

				// double upperWeight = sign * weight * (bin - lowerBin);
				// double lowerWeight = sign * weight * (upperBin - bin);
				//
				// if (lowerWeight != 0 && !binIndexes.contains(lowerBin)) {
				// binIndexes.add(lowerBin);
				// binWeights.add(lowerWeight);
				// }
				// if (upperWeight != 0 && !binIndexes.contains(upperBin)) {
				// binIndexes.add(upperBin);
				// binWeights.add(upperWeight);
				// }
			}

			// System.out.println(binIndexes);
			// System.out.println(binWeights);
		}

		private double weightForHarmonic(int i) {
			// return 1.0 / (i + 1);
			// return 1.0 - (i / (2.0 * (harmonicCount - 1)));
			return 1.0;
		}
	}

}
