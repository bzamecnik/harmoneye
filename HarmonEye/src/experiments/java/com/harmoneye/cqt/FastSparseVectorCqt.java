package com.harmoneye.cqt;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;
import org.apache.commons.math3.linear.SparseFieldVector;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import com.harmoneye.util.LinkedListNonZeroSparseFieldVector;

public class FastSparseVectorCqt implements Cqt {
	protected CqtContext ctx;
	protected CqtCalculator calc;

	private FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

	// spectral kernels - already Hermite-conjugated (conjugated and transposed) for the transform
	private List<FieldVector<Complex>> spectralKernels;

	public FastSparseVectorCqt(CqtContext ctx) {
		this.ctx = ctx;
		this.calc = new CqtCalculator(ctx);
	}

	public void init() {
		if (spectralKernels == null) {
			computeSpectralKernels();
		}
	}

	@Override
	public Complex[] transform(double[] signal) {
		signal = calc.padRight(signal, ctx.getSignalBlockSize());
		Complex[] spectrum = fft.transform(signal, TransformType.FORWARD);
		FieldVector<Complex> spectrumVector = new ArrayFieldVector<Complex>(spectrum);

		//		long start = System.nanoTime();
		Complex normalizationFactor = ctx.getNormalizationFactor();
		Complex[] product = new Complex[spectralKernels.size()];
		for (int i = 0; i < spectralKernels.size(); i++) {
			FieldVector<Complex> kernel = spectralKernels.get(i);
			Complex dotProduct = kernel.dotProduct(spectrumVector);
			Complex value = dotProduct.multiply(normalizationFactor);
			product[i] = value;
		}

		//		long end = System.nanoTime();
		//		System.out.println("total: " + (end - start) / 1e6 + " ms");

		return product;
	}

	protected void computeSpectralKernels() {
		if (spectralKernels != null) {
			return;
		}
		ComplexField field = ComplexField.getInstance();
		int totalBins = ctx.getTotalBins();
		spectralKernels = new ArrayList<FieldVector<Complex>>(totalBins);
		int signalBlockSize = ctx.getSignalBlockSize();
		for (int k = 0; k < totalBins; k++) {
			Complex[] kernel = calc.padRight(calc.conjugate(calc.spectralKernel(k)), signalBlockSize);
			SparseFieldVector<Complex> sparseKernel = new SparseFieldVector<Complex>(field, kernel);
			spectralKernels.add(new LinkedListNonZeroSparseFieldVector<Complex>(sparseKernel));
		}
	}
}
