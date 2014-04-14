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

	public ComplexVector transform(double[] signal, ComplexVector spectrum) {
		// StopWatch sw = new StopWatch();
		// sw.start();

		double[] dataRIElems = spectrum.getElements();
		// System.arraycopy(signal, 0, dataRIElems, 0, signal.length);

		for (int i = 0; i < signal.length; i++) {
			dataRIElems[i] = signal[i] * sampledWindow[i];
		}

		fft.realForwardFull(dataRIElems);

		normalize(spectrum);

		// sw.stop();
		// System.out.println("Computed transformed signal in " +
		// acc.smooth(sw.getNanoTime()) * 0.001 + " us");

		return spectrum;
	}

	public ComplexVector transform(double[] signal) {
		return transform(signal, dataRI);
	}

	private void normalize(ComplexVector spectrum) {
		int size = spectrum.size();
		double[] elements = spectrum.getElements();
		for (int i = 0; i < size; i++) {
			double re = elements[2 * i];
			double im = elements[2 * i + 1];
			if (re != 0 && im != 0) {
				elements[2 * i] = normalizationFactor * re;
				elements[2 * i + 1] = normalizationFactor * im;
			}
		}
	}
}
