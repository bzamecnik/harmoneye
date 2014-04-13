package com.harmoneye.math.filter;

import static com.harmoneye.audio.util.TextSignalPrinter.printSignal;
import static org.junit.Assert.*;

import org.junit.Test;

public class DecimatorTest {

	@Test
	public void test() {
		Filter filter = new ZeroPhaseFilter(new ButterworthFilter());
		Decimator decimator = new Decimator(filter);
		
		double[] signal = new double[256];
		for (int i = 0; i < signal.length; i++) {
			signal[i] = 10 + 0.1 * Math.random();
		}
		double [] signal1 = new double[signal.length / 2];
		System.arraycopy(signal, 0, signal1, 0, signal1.length);
		double [] signal2 = new double[signal.length / 2];
		System.arraycopy(signal, 0, signal2, 0, signal2.length);
		
		//printSignal(signal);
		
		printSignal(filter.filter(signal1));
		printSignal(filter.filter(signal2));
		
		double[] decimated = decimator.decimate(signal);
		
		//printSignal(decimated);
	}

}
