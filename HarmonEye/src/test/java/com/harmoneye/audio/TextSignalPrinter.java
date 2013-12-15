package com.harmoneye.audio;


public class TextSignalPrinter {
	public static void printSignal(double[] signal) {
		System.out.println("signal:");
		System.out.println("length: " + signal.length);
		for (int i = 0; i < signal.length; i++) {
			System.out.println(toStars(signal[i]) + "\t" + signal[i]);
		}
		System.out.println();
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
