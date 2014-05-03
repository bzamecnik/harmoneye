package com.harmoneye.ng;

import com.harmoneye.math.Mean;

class RmsExtractor implements FeatureExtractor<RmsExtractor.RmsFeature> {

	@Override
	public RmsExtractor.RmsFeature extract(double[] samples) {
		double rms = Mean.quadraticMean(samples);
		return new RmsFeature(rms);
	}

	static class RmsFeature {
		private double rms;

		public RmsFeature(double rms) {
			this.rms = rms;
		}

		public double getRms() {
			return rms;
		}

		@Override
		public String toString() {
			return "FeatureSet [rms=" + rms + "]";
		}
	}
}