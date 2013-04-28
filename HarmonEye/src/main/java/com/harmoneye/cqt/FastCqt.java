package com.harmoneye.cqt;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.FieldVector;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import com.harmoneye.util.LinkedListNonZeroSparseFieldMatrix;
import com.harmoneye.util.NonZeroSparseFieldMatrix;

public class FastCqt extends AbstractCqt {

	private static final double CHOP_THRESHOLD = 0.005;

	FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

	// spectral kernels - already Hermite-conjugated (conjugated and transposed) for the transform
	FieldMatrix<Complex> spectralKernels;

	private int signalBlockSize;

	private Complex normalizationFactor;

	public FastCqt() {
		
//		System.out.println(spectralKernels);
//		System.out.println("kernel: " + spectralKernels.getColumnDimension() + " x " + spectralKernels.getRowDimension());
		
		signalBlockSize = nextPowerOf2(bandWidth(0));
//		System.out.println("signalBlockSize:" + signalBlockSize);
		normalizationFactor = new Complex(2 / (signalBlockSize * windowIntegral));
	}
	
	@Override
	public Complex[] transform(double[] signal) {
		if (spectralKernels == null) {
			computeSpectralKernels();
		}
		signal = padRight(signal, signalBlockSize);
		Complex[] spectrum = fft.transform(signal, TransformType.FORWARD);
		ArrayFieldVector<Complex> spectrumVector = new ArrayFieldVector<Complex>(spectrum);
		
//		long start = System.nanoTime();
		FieldVector<Complex> product = spectralKernels.operate(spectrumVector);
//		long end = System.nanoTime();
//		System.out.println("total: " + (end - start) / 1e6 + " ms");
		
		product.mapMultiplyToSelf(normalizationFactor);
		Complex[] productArray = product.toArray();
		return productArray;
	}

	protected void computeSpectralKernels() {
		if (spectralKernels != null) {
			return;
		}
		ComplexField field = ComplexField.getInstance();
//		spectralKernels = new SparseFieldMatrix<Complex>(field, totalBins, nextPowerOf2(bandWidth(0)));
		spectralKernels = new NonZeroSparseFieldMatrix<Complex>(field, totalBins, nextPowerOf2(bandWidth(0)));
		for (int k = 0; k < totalBins; k++) {
			spectralKernels.setRow(k, conjugate(spectralKernel(k)));
		}
		spectralKernels.transpose();
		spectralKernels = new LinkedListNonZeroSparseFieldMatrix<>(spectralKernels);
	}

	protected Complex[] spectralKernel(int k) {
		Complex[] temporalKernel = padLeft(temporalKernel(k), nextPowerOf2(bandWidth(0)));
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
	
	private Complex[] padLeft(Complex[] values, int totalSize) {
		Complex[] padded = new Complex[totalSize];
		int dataSize = Math.min(values.length, totalSize);
		int paddingSize = totalSize - dataSize;
		for (int i = 0; i < paddingSize; i++) {
			padded[i] = Complex.ZERO;
		}
		for (int i = paddingSize; i < totalSize; i++) {
			padded[i] = values[i - paddingSize];
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


	public int getSignalBlockSize() {
		return signalBlockSize;
	}
	
}
