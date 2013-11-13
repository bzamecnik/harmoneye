 package com.harmoneye.cqt;

import org.apache.commons.math3.complex.Complex;

public class DirectCqt implements Cqt {

	private CqtContext ctx;
	private CqtCalculator calc;

	public DirectCqt(CqtContext ctx) {
		this.ctx = ctx;
		this.calc = new CqtCalculator(ctx);
	}

	@Override
	public Complex[] transform(double[] signal) {
		int totalBins = ctx.getTotalBins();
		Complex[] cqBins = new Complex[totalBins];
		double windowIntegral = ctx.getWindowIntegral();
		double normalizationFactor = 2 / windowIntegral;
		for (int k = 0; k < totalBins; k++) {
			Complex[] kernel = calc.temporalKernel(k);
			Complex binValue = Complex.ZERO;
			for (int n = 0; n < kernel.length; n++) {
				Complex multiple = kernel[n].multiply(signal[n]);
				binValue = binValue.add(multiple);
			}
			binValue = binValue.multiply(normalizationFactor);
			cqBins[k] = binValue;
		}
		return cqBins;
	}
}
