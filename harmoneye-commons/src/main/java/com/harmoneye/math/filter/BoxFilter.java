package com.harmoneye.math.filter;

import java.util.Arrays;

// TODO: implement a better algorithm independent on the kernel size
// (spread only the kernel difference and then integrate)

public class BoxFilter implements Filter {

	private int size;
	private double[] resultBuffer;

	/**
	 * @param size diameter of the kernel
	 */
	public BoxFilter(int size) {
		this.size = size;
	}

	@Override
	public double[] filter(double[] signal) {
		int length = signal.length;
		if (resultBuffer == null || resultBuffer.length != length) {
			resultBuffer = new double[length];
		} else {
			Arrays.fill(resultBuffer, 0);
		}
		int lowerOffset = -size / 2;
		double weight = 1.0 / size;
		for (int src = 0; src < length; src++) {
			for (int offset = 0; offset < size; offset++) {
				int dest = src + offset + lowerOffset;
				if (dest >= 0 && dest < length) {
					resultBuffer[dest] += weight * signal[src];
				}
			}
		}
		return resultBuffer;
	}
}
