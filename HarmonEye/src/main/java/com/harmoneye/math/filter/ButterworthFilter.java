package com.harmoneye.math.filter;

public class ButterworthFilter implements Filter {
	/*
	 * Digital filter designed by mkfilter/mkshape/gencode by A.J. Fisher
	 * 
	 * Command line: /www/usr/fisher/helpers/mkfilter -Bu -Lp -o 6 -a
	 * 2.5000000000e-01 0.0000000000e+00 -l
	 */

	private static final int NZEROS = 6;
	private static final int NPOLES = 6;
	private static final float GAIN_INV = 1.0f / 3.379723001e+01f;

	private static final float INPUT_COEFFS[] = new float[] { 1, 6, 15, 20, 15,
		6, 1 };
	private static final float OUTPUT_COEFFS[] = new float[] { -0.0017509260f, 0,
		-0.1141994251f, 0, -0.7776959619f, 0, 0 };

	private float inputSamples[] = new float[NZEROS + 1];
	private float outputSamples[] = new float[NPOLES + 1];

	/**
	 * Low-pass filters the time-domain signal in place by a 6th order Butterworth
	 * filter. The cut-off frequency is half of the Nyquist frequency (ie. 0.25 x
	 * sampling frequency.
	 * 
	 * This function is not thead-safe.
	 */
	public float[] filter(float[] signal, float[] filteredSignal) {
		if (filteredSignal == null) {
			filteredSignal = new float[signal.length];
		}

		clean(inputSamples);
		clean(outputSamples);

		for (int i = 0; i < signal.length; i++) {
			// TODO: instead of shifting use a "pointer" - like in a ring buffer
			shift(inputSamples);
			inputSamples[6] = signal[i] * GAIN_INV;
			shift(outputSamples);
			outputSamples[6] = dot(INPUT_COEFFS, inputSamples)
				+ dot(OUTPUT_COEFFS, outputSamples);
			filteredSignal[i] = outputSamples[6];
		}

		return filteredSignal;
	}

	private void shift(float[] values) {
		for (int i = 0; i < values.length - 1; i++) {
			values[i] = values[i + 1];
		}
	}

	private float dot(float[] a, float[] b) {
		float sum = 0;
		for (int i = 0; i < a.length; i++) {
			sum += a[i] * b[i];
		}
		return sum;
	}

	private void clean(float[] values) {
		for (int i = 0; i < values.length; i++) {
			values[i] = 0;
		}
	}
}
