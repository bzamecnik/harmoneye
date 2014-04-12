package com.harmoneye.app.spectrogram;

import org.apache.commons.math3.util.FastMath;

import com.harmoneye.math.filter.BoxFilter;
import com.harmoneye.math.filter.Filter;

public class HighPassFilter implements Filter {

	private BoxFilter boxFilter;

	public HighPassFilter(int size) {
		boxFilter = new BoxFilter(size);
	}

	public double[] filter(double[] values) {
		double[] lowPass = boxFilter.filter(values);
		for (int i = 0; i < values.length; i++) {
			values[i] = FastMath.max(values[i] - lowPass[i], 0);
		}
		return values;
	}
}
