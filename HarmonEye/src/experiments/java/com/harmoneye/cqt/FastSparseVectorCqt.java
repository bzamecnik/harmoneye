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

public class FastSparseVectorCqt extends AbstractCqt {

	private static final double CHOP_THRESHOLD = 0.005;

	FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

	// spectral kernels - already Hermite-conjugated (conjugated and transposed) for the transform
	List<FieldVector<Complex>> spectralKernels;

	private int signalBlockSize;

	private Complex normalizationFactor;

	public FastSparseVectorCqt() {
		signalBlockSize = nextPowerOf2(bandWidth(0));
		computeSpectralKernels();
//		System.out.println(spectralKernels);
//		System.out.println("kernel: " + spectralKernels.size() + " x " + spectralKernels.get(0).getDimension());
		
		
//		System.out.println("signalBlockSize:" + signalBlockSize);
		normalizationFactor = new Complex(2 / (signalBlockSize * windowIntegral));
	}
	
	@Override
	public Complex[] transform(double[] signal) {
		signal = padRight(signal, signalBlockSize);
		Complex[] spectrum = fft.transform(signal, TransformType.FORWARD);
		FieldVector<Complex> spectrumVector = new ArrayFieldVector<Complex>(spectrum);
		
//		long start = System.nanoTime();
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
		spectralKernels = new ArrayList<FieldVector<Complex>>(totalBins);
		for (int k = 0; k < totalBins; k++) {
			Complex[] kernel = padRight(conjugate(spectralKernel(k)), signalBlockSize);
			SparseFieldVector<Complex> sparseKernel = new SparseFieldVector<Complex>(field, kernel);
			spectralKernels.add(new LinkedListNonZeroSparseFieldVector<Complex>(sparseKernel));
		}
	}

	protected Complex[] spectralKernel(int k) {
		Complex[] temporalKernel = temporalKernel(k);
		temporalKernel = padLeft(temporalKernel, signalBlockSize);
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
