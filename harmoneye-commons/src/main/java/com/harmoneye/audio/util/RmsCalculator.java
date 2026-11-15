package com.harmoneye.audio.util;

public class RmsCalculator {

	public static double computeRms(double[] amplitudes) {
		double sum = 0;
		for (double amplitude : amplitudes) {
			sum += amplitude * amplitude;
		}
		return (double) Math.sqrt(sum / (double) amplitudes.length);
	}

}
