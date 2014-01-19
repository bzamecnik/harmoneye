package com.harmoneye.math.fft;

import com.harmoneye.math.cqt.Cqt;
import com.harmoneye.math.cqt.CqtCalculator;
import com.harmoneye.math.cqt.CqtContext;
import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.math.window.WindowFunction;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class Fft implements Cqt {

	protected CqtContext ctx;
	protected CqtCalculator calc;
	private DoubleFFT_1D fft;

	private ComplexVector dataRI;
	private double[] window;
	private double normalizationFactor;

	public Fft(CqtContext ctx) {
		this.ctx = ctx;
		this.calc = new CqtCalculator(ctx);
		this.fft = new DoubleFFT_1D(ctx.getSignalBlockSize());
	}

	public void init() {
		int signalBlockSize = ctx.getSignalBlockSize();
		dataRI = new ComplexVector(signalBlockSize);
		window = sampleWindow(signalBlockSize);

		double windowIntegral = calc.windowIntegral(ctx.getWindow());
		normalizationFactor = 2 / windowIntegral;
	}

	private double[] sampleWindow(int size) {
		double[] coeffs = new double[size];
		double sizeInv = 1.0 / size;
		WindowFunction window = ctx.getWindow();
		for (int i = 0; i < size; i++) {
			coeffs[i] = window.value(i * sizeInv) * sizeInv;
		}
		return coeffs;
	}

	// signal must to be as long as ctx.signalBlockLength()
	@Override
	public ComplexVector transform(double[] signal) {
		// StopWatch sw = new StopWatch();
		// sw.start();

		double[] dataRIElems = dataRI.getElements();
		// System.arraycopy(signal, 0, dataRIElems, 0, signal.length);

		for (int i = 0; i < signal.length; i++) {
			dataRIElems[i] = signal[i] * window[i];
		}

		fft.realForwardFull(dataRIElems);

		normalize(dataRI);

		// sw.stop();
		// System.out.println("Computed transformed signal in " +
		// acc.smooth(sw.getNanoTime()) * 0.001 + " us");

		return dataRI;
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

	public CqtContext getContext() {
		return ctx;
	}
}
