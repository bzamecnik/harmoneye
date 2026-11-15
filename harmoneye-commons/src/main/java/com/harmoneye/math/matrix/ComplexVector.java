package com.harmoneye.math.matrix;

public class ComplexVector {
	// interleaved elements
	// elements[2*i] - real
	// elements[2*i + 1] - imaginary
	private double[] elements;
	private int size;

	public ComplexVector(int size) {
		this.size = size;
		this.elements = new double[2 * size];
	}

	public ComplexVector(double[] elements) {
		if (elements.length % 2 != 0) {
			throw new IllegalArgumentException("element length must be even");
		}
		this.size = elements.length / 2;
		this.elements = elements;
	}

	public ComplexVector(ComplexVector copy) {
		this.size = copy.size;
		this.elements = new double[copy.getElements().length];
		System.arraycopy(copy.getElements(), 0, elements, 0, size);
	}

	public double[] getElements() {
		return elements;
	}

	public int size() {
		return size;
	}

	// cross spectrum: conj(A).*B
	public static void crossSpectrum(ComplexVector spectrumA,
		ComplexVector spectrumB, ComplexVector crossSpectrum) {
		double[] a = spectrumA.getElements();
		double[] b = spectrumB.getElements();
		double[] c = crossSpectrum.getElements();
		int length = spectrumA.size();
		for (int i = 0, reIndex = 0, imIndex = 1; i < length; i++, reIndex += 2, imIndex += 2) {
			double aConjRe = a[reIndex];
			double bConjIm = -a[imIndex];
			double bRe = b[reIndex];
			double bIm = b[imIndex];
			c[reIndex] = aConjRe * bRe - bConjIm * bIm;
			c[imIndex] = bConjIm * bRe + aConjRe * bIm;
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < elements.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(elements[i]);
		}
		sb.append("]");
		return sb.toString();
	}
}
