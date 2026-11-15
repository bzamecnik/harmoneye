package com.harmoneye.util;

public class Formatter {

	public static String formatArray(Object[] values) {
		StringBuilder sb = new StringBuilder("{");
		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				sb.append(",").append("\n");
			}
			sb.append(values[i]);
		}
		sb.append("}");
		return sb.toString();
	}

}
