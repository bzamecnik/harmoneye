package com.harmoneye.math.filter;

import com.harmoneye.math.MaxNorm;
import com.harmoneye.math.Norm;

public class NormalizingHighPassFilter implements Filter {

	private HighPassFilter highPassFilter;
	private Normalizer normalizer;
	private Norm maxNorm = new MaxNorm();

	public NormalizingHighPassFilter(HighPassFilter highPassFilter,
		Normalizer normalizer) {
		this.highPassFilter = highPassFilter;
		this.normalizer = normalizer;
		
	}

	@Override
	public double[] filter(double[] values) {
		double oldNorm = normalizer.norm(values);
		double[] highPass = highPassFilter.filter(values);
		double newNorm = normalizer.norm(values);
		double newMax = maxNorm.norm(values);
		if (oldNorm > 0) {
			double targetNorm = Math.min(newNorm / oldNorm, newMax);
			normalizer.normalize(highPass,  targetNorm);
		}
		return highPass;
	}

}
