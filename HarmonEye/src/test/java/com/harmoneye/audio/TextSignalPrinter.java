package com.harmoneye.audio;

import com.harmoneye.math.matrix.ComplexVector;
import com.harmoneye.math.matrix.DComplex;

public class TextSignalPrinter {
	public static void printSignal(double[] signal) {
		System.out.println("signal of length: " + signal.length);
		for (int i = 0; i < signal.length; i++) {
			printLine(i, signal[i]);
		}
		System.out.println();
	}

	public static void printSignal(ComplexVector signal) {
		printSignal(signal, true);
	}

	public static void printSignal(ComplexVector signal, boolean normalize) {
		int size = signal.size();
		double[] elements = signal.getElements();
		System.out.println("signal of length: " + size);

		double scaleFactor = 1.0;
		if (normalize) {
			double max = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < size; i++) {
				double abs = DComplex.abs(elements[2 * i], elements[2 * i + 1]);
				max = Math.max(max, abs);
			}
			;
			System.out.println("max: " + max);
			scaleFactor = 1.0 / max;
		}

		System.out.println("abs: ");
		for (int i = 0; i < size; i++) {
			double abs = DComplex.abs(elements[2 * i], elements[2 * i + 1]);
			printLine(i, abs * scaleFactor);
		}
		System.out.println("real component: ");
		for (int i = 0; i < size; i++) {
			printLine(i, elements[2 * i] * scaleFactor);
		}
		System.out.println("imaginary component: ");
		for (int i = 0; i < size; i++) {
			printLine(i, elements[2 * i + 1] * scaleFactor);
		}
		System.out.println();
	}

	private static void printLine(int index, double value) {
		System.out.println("[" + index + "]\t" + toStars(value) + "\t" + value);
		// System.out.println(value);
	}

	private static String toStars(double amplitude) {
		StringBuilder sb = new StringBuilder();
		int totalSize = 80;
		int starCount = (int) Math.round(0.5 * Math.abs(amplitude) * totalSize
			+ 1);
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
