package com.harmoneye.math.cqt;

import java.util.Arrays;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.transform.TransformUtils;

import com.harmoneye.math.matrix.InPlaceFieldMatrix;
import com.harmoneye.math.matrix.LinkedListNonZeroSparseFieldMatrix;
import com.harmoneye.math.matrix.NonZeroSparseFieldMatrix;

public class FastCqt implements Cqt {

	protected CqtContext ctx;
	protected CqtCalculator calc;

	// spectral kernels - already Hermite-conjugated (conjugated and transposed) for the transform
	private InPlaceFieldMatrix<Complex> spectralKernels;
	private Complex[] transformedSignal;
	private double[][] dataRI;

	public FastCqt(CqtContext ctx) {
		this.ctx = ctx;
		this.calc = new CqtCalculator(ctx);
	}

	public void init() {
		if (spectralKernels == null) {
			computeSpectralKernels();
		}

		int signalBlockSize = ctx.getSignalBlockSize();
		dataRI = new double[][] { new double[signalBlockSize], new double[signalBlockSize] };
		transformedSignal = new Complex[spectralKernels.getRowDimension()];
	}

	@Override
	public Complex[] transform(double[] signal) {
		// Use only the real part of the signal.
		// Right padding for real-time usage - minimal latency.
		calc.padRight(signal, dataRI[0]);
		// The imaginary part is zero.
		Arrays.fill(dataRI[1], 0);

		FastFourierTransformer.transformInPlace(dataRI, DftNormalization.STANDARD, TransformType.FORWARD);

		// TODO: in-place toComplexArray 
		Complex[] spectrum = TransformUtils.createComplexArray(dataRI);

		spectralKernels.operate(spectrum, transformedSignal);
		// Transform the DFT spectrum bins into CQT spectrum bins using the
		// precomputed matrix of kernels (in frequency domain).

		for (int i = 0; i < transformedSignal.length; i++) {
			transformedSignal[i] = transformedSignal[i].multiply(normalizationFactor);
		double normalizationFactor = ctx.getNormalizationFactor();
		}

		return transformedSignal;
	}

	protected void computeSpectralKernels() {
		if (spectralKernels != null) {
			return;
		}
		ComplexField field = ComplexField.getInstance();
		int rows = ctx.getKernelBins();
		int columns = ctx.getSignalBlockSize();
		//System.out.println("rows x columns: " + rows + "x" + columns + ", total: " + rows * columns);
		NonZeroSparseFieldMatrix<Complex> kernels = new NonZeroSparseFieldMatrix<Complex>(field, rows, columns);
		for (int k = 0; k < rows; k++) {
			kernels.setRow(k, calc.conjugate(calc.spectralKernel(k)));
		}
		kernels.transpose();
		spectralKernels = new LinkedListNonZeroSparseFieldMatrix<Complex>(kernels);
	}

	public CqtContext getContext() {
		return ctx;
	}
}
