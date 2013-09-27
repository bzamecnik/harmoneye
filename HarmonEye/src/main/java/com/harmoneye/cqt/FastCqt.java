package com.harmoneye.cqt;

import java.util.Arrays;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.transform.TransformUtils;

import com.harmoneye.util.InPlaceFieldMatrix;
import com.harmoneye.util.LinkedListNonZeroSparseFieldMatrix;
import com.harmoneye.util.NonZeroSparseFieldMatrix;

public class FastCqt extends AbstractCqt {

	private static final double CHOP_THRESHOLD = 0.005;

	FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

	// spectral kernels - already Hermite-conjugated (conjugated and transposed) for the transform
	InPlaceFieldMatrix<Complex> spectralKernels;

	private int signalBlockSize;

	private Complex normalizationFactor;

	private Complex[] transformedSignal;

	private double[][] dataRI;

	public FastCqt() {
		signalBlockSize = nextPowerOf2(bandWidth(0));
		normalizationFactor = new Complex(2 / (signalBlockSize * windowIntegral));
	}

	public void init() {
		if (spectralKernels == null) {
			computeSpectralKernels();
			//printSpectralKernels();
		}

		dataRI = new double[][] { new double[signalBlockSize], new double[signalBlockSize] };
		transformedSignal = new Complex[spectralKernels.getRowDimension()];
	}


	@Override
	public Complex[] transform(double[] signal) {
		padRight(signal, dataRI[0]);
		Arrays.fill(dataRI[1], 0);

		FastFourierTransformer.transformInPlace(dataRI, DftNormalization.STANDARD, TransformType.FORWARD);

		// TODO: in-place toComplexArray 
		Complex[] spectrum = TransformUtils.createComplexArray(dataRI);

		spectralKernels.operate(spectrum, transformedSignal);

		for (int i = 0; i < transformedSignal.length; i++) {
			transformedSignal[i] = transformedSignal[i].multiply(normalizationFactor);
		}

		return transformedSignal;
	}

	protected void computeSpectralKernels() {
		if (spectralKernels != null) {
			return;
		}
		ComplexField field = ComplexField.getInstance();
		NonZeroSparseFieldMatrix<Complex> kernels = new NonZeroSparseFieldMatrix<Complex>(field, totalBins,
			nextPowerOf2(bandWidth(0)));
		for (int k = 0; k < totalBins; k++) {
			kernels.setRow(k, conjugate(spectralKernel(k)));
		}
		kernels.transpose();
		spectralKernels = new LinkedListNonZeroSparseFieldMatrix<>(kernels);
	}
	
//	private void printSpectralKernels() {
//		System.out.println(spectralKernels);
//	}

	protected Complex[] spectralKernel(int k) {
		Complex[] temporalKernel = padLeft(temporalKernel(k), nextPowerOf2(bandWidth(0)));
		Complex[] spectrum = fft.transform(temporalKernel, TransformType.FORWARD);

		chop(spectrum);
		return spectrum;
	}

	private Complex[] padLeft(Complex[] values, int totalSize) {
		Complex[] padded = new Complex[totalSize];
		int dataSize = Math.min(values.length, totalSize);
		int paddingSize = totalSize - dataSize;

		for (int i = 0; i < paddingSize; i++) {
			padded[i] = Complex.ZERO;
		}

		System.arraycopy(values, 0, padded, paddingSize, dataSize);

		return padded;
	}

	private void padRight(double[] in, double[] padded) {
		int dataSize = Math.min(in.length, padded.length);

		System.arraycopy(in, 0, padded, 0, dataSize);

		for (int i = dataSize; i < padded.length; i++) {
			padded[i] = 0;
		}
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

	public int getSignalBlockSize() {
		return signalBlockSize;
	}
}
