package com.harmoneye.audio;

import org.apache.commons.math3.complex.Complex;


public class TextSignalPrinter {
	public static void printSignal(double[] signal) {
		System.out.println("signal of length: " + signal.length);
		for (int i = 0; i < signal.length; i++) {
			printLine(signal[i]);
		}
		System.out.println();
	}
	
	public static void printSignal(Complex[] signal) {
		System.out.println("signal of length: " + signal.length);
		
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < signal.length; i++) {
			double abs = signal[i].abs();
			max = Math.max(max, abs);
		};
		System.out.println("max: " + max);
		double scaleFactor = 1.0 / max;
		
		System.out.println("abs: ");
		for (int i = 0; i < signal.length; i++) {
			printLine(signal[i].abs() * scaleFactor);
		}
		System.out.println("real component: ");
		for (int i = 0; i < signal.length; i++) {
			printLine(signal[i].getReal() * scaleFactor);
		}
		System.out.println("imaginary component: ");
		for (int i = 0; i < signal.length; i++) {
			printLine(signal[i].getImaginary() * scaleFactor);
		}
		System.out.println();
	}
	
	private static void printLine(double value) {
		System.out.println(toStars(value) + "\t" + value);
//		System.out.println(value);
	}

	private static String toStars(double amplitude) {
		StringBuilder sb = new StringBuilder();
		int totalSize = 80;
		int starCount = (int) Math.round(0.5 * Math.abs(amplitude) * totalSize + 1);
		for (int i = 0; i < 0.5 * totalSize; i++) {
			sb.append(' ');
		}
		for (int i = 0; i < starCount; i++) {
			sb.append('*');
		}
		for (int i = sb.length(); i <= totalSize; i++) {
			sb.append(' ');
		}
		if (amplitude < 0) {
			sb.reverse();
		}
		return sb.toString();
	}
}
