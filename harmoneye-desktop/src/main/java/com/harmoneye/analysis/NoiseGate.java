package com.harmoneye.analysis;

import com.harmoneye.math.filter.ScalarExpSmoother;


public class NoiseGate {
	private int signalLength;
	
	private double openThreshold = 0.25;
	private double closeThreshold = 0.25;
	
	private ScalarExpSmoother smoother = new ScalarExpSmoother(0.5);

	private double[] previousValues;

	public NoiseGate(int signalLength) {
		this.signalLength = signalLength;
		previousValues = new double[signalLength];
	}

	public void filter(double[] values) {
		int size = values.length;
		for (int i = 0; i < size; i++) {
			double prevValue = previousValues[i];
			double value = values[i];
			double diff = value - prevValue;
			double threshold = (diff >= 0) ? openThreshold : closeThreshold;
			if (value < threshold) {
				values[i] *= 0;//Math.abs(value - threshold);
			}
		}
		System.arraycopy(values, 0, previousValues, 0, signalLength);
	}

	public void setOpenThreshold(double threshold) {
		threshold = smoother.smooth(threshold);
		this.openThreshold = threshold;
		this.closeThreshold = this.openThreshold * 0.8;
	}
}
