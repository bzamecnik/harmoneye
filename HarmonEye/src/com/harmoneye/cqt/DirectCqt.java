package com.harmoneye.cqt;

import org.apache.commons.math3.complex.Complex;

public class DirectCqt extends AbstractCqt {

	@Override
	public Complex[] transform(double[] signal) {
		Complex[] cqBins = new Complex[totalBins];
		for (int k = 0; k < totalBins; k++) {
			int binSize = bandWidth(k);
			Complex[] coeffs = temporalKernel(k, binSize);
			Complex binValue = Complex.ZERO;
			for (int n = 0; n < binSize; n++) {
				Complex multiple = coeffs[n].multiply(signal[n]);
				binValue = binValue.add(multiple);
			}
			double normalizationFactor = 2 / windowIntegral;
			binValue = binValue.multiply(normalizationFactor);
			cqBins[k] = binValue;
		}
		return cqBins;
	}

	public static void main(String[] args) {
		double signal[] = { 0., 0.995641, -0.185724, -0.960996, 0.364986, 0.892913, -0.531547, -0.79376, 0.679613,
			0.666986, -0.804031, -0.517005, 0.900472, 0.349033, -0.965579, -0.168916, 0.997089, -0.0170778, -0.993903,
			0.202478, 0.956133, -0.380832, -0.885094, 0.545935, 0.783257, -0.692042, -0.654165, 0.814068, 0.502311,
			-0.907768, -0.332978, 0.969881, 0.152059, -0.998245, 0.0341507, 0.991875, -0.219172, -0.950991, 0.396568,
			0.877017, -0.560164, -0.772525, 0.704269, 0.641153, -0.823868, -0.487471, 0.914799, 0.316826, -0.973899,
			-0.135158, 0.999111, -0.0512135, -0.989558, 0.235803, 0.945572, -0.412187, -0.868683, 0.574229, 0.761568,
			-0.71629, -0.627953, 0.833427, 0.472488, -0.921564, -0.300582, 0.977633, 0.118217, -0.999685 };

		Complex[] cqBins = new DirectCqt().transform(signal);

		System.out.println("cq bins:");
		for (int i = 0; i < cqBins.length; i++) {
			System.out.println(cqBins[i].abs());
		}
	}
}
