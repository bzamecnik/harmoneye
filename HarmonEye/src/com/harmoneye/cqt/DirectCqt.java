package com.harmoneye.cqt;

import org.apache.commons.math3.complex.Complex;

public class DirectCqt extends AbstractCqt {

	@Override
	public Complex[] transform(double[] signal) {
		Complex[] cqBins = new Complex[totalBins];
		for (int k = 0; k < totalBins; k++) {
			Complex[] kernel = temporalKernel(k);
			Complex binValue = Complex.ZERO;
			for (int n = 0; n < kernel.length; n++) {
				Complex multiple = kernel[n].multiply(signal[n]);
				binValue = binValue.add(multiple);
			}
			double normalizationFactor = 2 / windowIntegral;
			binValue = binValue.multiply(normalizationFactor);
			cqBins[k] = binValue;
		}
		return cqBins;
	}
}
