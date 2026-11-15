package com.harmoneye.math.fft;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.junit.Test;

import com.harmoneye.audio.util.TextSignalPrinter;
import com.harmoneye.audio.util.ToneGenerator;
import com.harmoneye.math.matrix.ComplexUtils;
import com.harmoneye.math.matrix.ComplexVector;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class MusicSignalFft {
	@Test
	public void EmorySineFftReal() {
		int signalBlockSize = 128;
		double[] signal = generateSineWave(signalBlockSize);
		TextSignalPrinter.printSignal(signal);

		DoubleFFT_1D fft = new DoubleFFT_1D(signalBlockSize);
		fft.realForward(signal);

		double normalizationFactor = 2.0 / signalBlockSize;
		for (int i = 0; i < signal.length; i++) {
			signal[i] *= normalizationFactor;
		}

		TextSignalPrinter.printSignal(new ComplexVector(signal));
	}

	@Test
	public void EmorySineFftFull() {
		int signalBlockSize = 128;
		double[] signal = generateSineWave(signalBlockSize);
		TextSignalPrinter.printSignal(signal);

		double[] data = new double[signalBlockSize * 2];
		System.arraycopy(signal, 0, data, 0, signalBlockSize);

		DoubleFFT_1D fft = new DoubleFFT_1D(signalBlockSize);
		fft.realForwardFull(data);

		double normalizationFactor = 2.0 / signalBlockSize;
		for (int i = 0; i < data.length; i++) {
			data[i] *= normalizationFactor;
		}

		TextSignalPrinter.printSignal(new ComplexVector(data));
	}

	@Test
	public void ApacheSineFft() {
		int signalBlockSize = 128;
		double[] signal = generateSineWave(signalBlockSize);
		TextSignalPrinter.printSignal(signal);

		FastFourierTransformer fft = new FastFourierTransformer(
			DftNormalization.STANDARD);
		Complex[] spectrum = fft.transform(signal, TransformType.FORWARD);

		double normalizationFactor = 2.0 / signalBlockSize;
		for (int i = 0; i < signal.length; i++) {
			spectrum[i] = spectrum[i].multiply(normalizationFactor);
		}

		TextSignalPrinter.printSignal(ComplexUtils.complexVectorFromArray(spectrum));
	}

	private double[] generateSineWave(int signalBlockSize) {
		double samplingFreq = 100.0;
		double freq = 1.0;
		double duration = signalBlockSize / samplingFreq;
		ToneGenerator toneGen = new ToneGenerator(samplingFreq, true);
		double[] signal = toneGen.generateSinWave(freq, duration);
		
		return signal;
	}

}
