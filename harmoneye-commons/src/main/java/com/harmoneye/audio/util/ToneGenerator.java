package com.harmoneye.audio.util;

import com.harmoneye.math.window.HammingWindow;
import com.harmoneye.math.window.WindowFunction;

public class ToneGenerator {

	private double samplingFreq;
	private boolean windowEnabled;
	private WindowFunction window;

	public ToneGenerator(double samplingFreq) {
		this(samplingFreq, false);
	}
	
	public ToneGenerator(double samplingFreq, boolean windowEnabled) {
		this.samplingFreq = samplingFreq;
		this.windowEnabled = windowEnabled;
		window = new HammingWindow();
	}

	/**
	 * @param frequency in Hz
	 * @param samplingFreq in samples per second
	 * @param length in seconds
	 * @return
	 */
	public double[] generateSinWave(double frequency, double length) {
		return generateSignal(new Sine(frequency, 1.0), length);
	}
	
	public double[] generateSignal(Function1D function, double length) {
		int sampleCount = (int) Math.ceil(length * samplingFreq);
		double timeStep = 1.0 / samplingFreq;
		double[] signal = new double[sampleCount];
		for (int i = 0; i < sampleCount; i++) {
			signal[i] = function.value(i * timeStep);
		}
		
		if (windowEnabled) {
			double sizeInv = 1.0 / sampleCount;
			for (int i = 0; i < signal.length; i++) {
				signal[i] *= window.value(i * sizeInv);
			}
		}
		
		return signal;
	}

	interface Function1D {
		double value(double time);
	}

	static class Sine implements Function1D {
		private double xStep;
		private double amplitude;

		public Sine(double frequency, double amplitude) {
			this.xStep = 2 * Math.PI * frequency;
			this.amplitude = amplitude;
		}

		@Override
		public double value(double time) {
			return amplitude * Math.sin(time * xStep);
		}
	}

	static class Sum implements Function1D {
		private Function1D[] functions;

		public Sum(Function1D[] functions) {
			super();
			this.functions = functions;
		}

		@Override
		public double value(double time) {
			double sum = 0;
			for (int i = 0; i < functions.length; i++) {
				sum += functions[i].value(time);
			}
			return sum;
		}
	}
	
	static class HarmonicSines implements Function1D {
		private Sine fundamental;
		private int harmonicCount;

		public HarmonicSines(Sine fundamental, int harmonicCount) {
			this.fundamental = fundamental;
			this.harmonicCount = harmonicCount;
		}

		@Override
		public double value(double time) {
			double sum = 0;
			for (int i = 0; i < harmonicCount; i++) {
				sum += fundamental.value(time / i);
			}
			return sum;
		}
	}
}
