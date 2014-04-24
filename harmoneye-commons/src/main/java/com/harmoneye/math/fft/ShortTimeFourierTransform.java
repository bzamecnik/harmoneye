package com.harmoneye.math.fft;

import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.math.window.WindowFunction;
import com.harmoneye.math.window.WindowIntegrator;
import com.harmoneye.math.window.WindowSampler;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class ShortTimeFourierTransform {

	private DoubleFFT_1D fft;

	private ComplexVector dataRI;
	private double[] sampledWindow;
	private double normalizationFactor;

	public ShortTimeFourierTransform(int windowSize, WindowFunction window) {
		this.fft = new DoubleFFT_1D(windowSize);
		dataRI = new ComplexVector(windowSize);
		sampledWindow = new WindowSampler().sampleWindow(window, windowSize);
		double windowIntegral = new WindowIntegrator().integral(window);
		normalizationFactor = 2.0 / windowIntegral;
	}

	public ComplexVector transform(double[] signal) {
		return transform(signal, dataRI);
	}

	public ComplexVector transform(float[] signal) {
		return transform(signal, dataRI);
	}

	public ComplexVector transform(double[] signal, ComplexVector dataRI) {
		fillWindowedSignal(signal, sampledWindow, dataRI);
		return transform(dataRI);
	}

	public ComplexVector transform(float[] signal, ComplexVector dataRI) {
		fillWindowedSignal(signal, sampledWindow, dataRI);
		return transform(dataRI);
	}

	private ComplexVector transform(ComplexVector dataRI) {
		fft.realForwardFull(dataRI.getElements());
		return normalize(dataRI);
	}

	private void fillWindowedSignal(double[] signal, double[] sampledWindow,
		ComplexVector dataRI) {
		double[] dataRIElems = dataRI.getElements();
		int length = signal.length;
		for (int i = 0; i < length; i++) {
			dataRIElems[i] = signal[i] * sampledWindow[i];
		}
	}

	private void fillWindowedSignal(float[] signal, double[] sampledWindow,
		ComplexVector dataRI) {
		double[] dataRIElems = dataRI.getElements();
		int length = signal.length;
		for (int i = 0; i < length; i++) {
			dataRIElems[i] = signal[i] * sampledWindow[i];
		}
	}

	private ComplexVector normalize(ComplexVector spectrum) {
		int size = spectrum.size();
		double[] elements = spectrum.getElements();

		// TODO: DVector.mult(elements, normalizationFactor);

		for (int i = 0; i < size; i++) {
			double re = elements[2 * i];
			double im = elements[2 * i + 1];
			if (re != 0 && im != 0) {
				elements[2 * i] = normalizationFactor * re;
				elements[2 * i + 1] = normalizationFactor * im;
			}
		}
		return spectrum;
	}
}
