package com.harmoneye.cqt;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.FieldVector;
import org.apache.commons.math3.linear.SparseFieldMatrix;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class FastCqt extends AbstractCqt {

	private static final double CHOP_THRESHOLD = 0.005;

	FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

	// spectral kernels - already Hermite-conjugated (conjugated and transposed) for the transform
	FieldMatrix<Complex> spectralKernels;

	private int signalBlockSize;

	private Complex normalizationFactor;

	public FastCqt() {
		computeSpectralKernels();
//		System.out.println(spectralKernels);
		
		signalBlockSize = nextPowerOf2(bandWidth(0));
		System.out.println("signalBlockSize:" + signalBlockSize);
		normalizationFactor = new Complex(2 / (signalBlockSize * windowIntegral));
	}
	
	@Override
	public Complex[] transform(double[] signal) {
		signal = padRight(signal, signalBlockSize);

		Complex[] spectrum = fft.transform(signal, TransformType.FORWARD);

		ArrayFieldVector<Complex> spectrumVector = new ArrayFieldVector<Complex>(spectrum);
		FieldVector<Complex> product = spectralKernels.operate(spectrumVector);
		product.mapMultiplyToSelf(normalizationFactor);
		return product.toArray();
	}

	protected void computeSpectralKernels() {
		if (spectralKernels != null) {
			return;
		}
		spectralKernels = new SparseFieldMatrix<Complex>(ComplexField.getInstance(), totalBins,
			nextPowerOf2(bandWidth(0)));
		for (int k = 0; k < totalBins; k++) {
			spectralKernels.setRow(k, conjugate(spectralKernel(k)));
		}
		spectralKernels.transpose();
	}

	protected Complex[] spectralKernel(int k) {
		Complex[] temporalKernel = padRight(temporalKernel(k), nextPowerOf2(bandWidth(0)));
		Complex[] spectrum = fft.transform(temporalKernel, TransformType.FORWARD);
		chop(spectrum);
		return spectrum;
	}

	private Complex[] padRight(Complex[] values, int totalSize) {
		Complex[] padded = new Complex[totalSize];
		int size = Math.min(values.length, totalSize);
		for (int i = 0; i < size; i++) {
			padded[i] = values[i];
		}
		for (int i = values.length; i < totalSize; i++) {
			padded[i] = Complex.ZERO;
		}
		return padded;
	}

	private double[] padRight(double[] values, int totalSize) {
		double[] padded = new double[totalSize];
		int size = Math.min(values.length, totalSize);
		for (int i = 0; i < size; i++) {
			padded[i] = values[i];
		}
		for (int i = values.length; i < totalSize; i++) {
			padded[i] = 0;
		}
		return padded;
	}

	private void chop(Complex[] values) {
		for (int i = 0; i < values.length; i++) {
			if (values[i].abs() < CHOP_THRESHOLD) {
				values[i] = Complex.ZERO;
			}
		}
	}

	private Complex[] conjugate(Complex[] values) {
		for (int i = 0; i < values.length; i++) {
			values[i] = values[i].conjugate();
		}
		return values;
	}

	// http://acius2.blogspot.cz/2007/11/calculating-next-power-of-2.html
	private int nextPowerOf2(int value) {
		value--;
		value |= (value >> 1);
		value |= (value >> 2);
		value |= (value >> 4);
		value |= (value >> 8);
		value |= (value >> 16);
		value++;
		return value;
	}

}
