package com.harmoneye.math.filter;

public interface Filter {
	/**
	 * 1D digital filter
	 * 
	 * @param signal input signal
	 * @param filteredSignal output signal, can be equal to signal (then the
	 * computation is in-place), can be null, then a new array is allocated
	 * @return filtered signal
	 */
	double[] filter(double[] signal, double[] filteredSignal);
}
