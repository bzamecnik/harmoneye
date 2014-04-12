package com.harmoneye.math.filter;

import static org.junit.Assert.*;

import org.junit.Test;

import com.harmoneye.audio.TextSignalPrinter;

public class BoxFilterTest {

	@Test
	public void diracPulse() {
		double signal[] = { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 };
		int size = 5;
		BoxFilter box = new BoxFilter(size);
		double[] filtered = box.filter(signal);
		TextSignalPrinter.printSignal(filtered);
		assertArrayEquals(new double[] { 0, 0, 0.2, 0.2, 0.2, 0.2, 0.2, 0, 0, 0 },
			filtered,
			1e-6);
	}

	@Test
	public void diracPulseAtBeginning() {
		double signal[] = { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		int size = 4;
		BoxFilter box = new BoxFilter(size);
		double[] filtered = box.filter(signal);
		TextSignalPrinter.printSignal(filtered);
		assertArrayEquals(new double[] { 0.5, 0.5, 0, 0, 0, 0, 0, 0, 0, 0 },
			filtered,
			1e-6);
	}

	@Test
	public void diracPulseAtEnd() {
		double signal[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 4 };
		int size = 3;
		BoxFilter box = new BoxFilter(size);
		double[] filtered = box.filter(signal);
		TextSignalPrinter.printSignal(filtered);
		assertArrayEquals(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 4 / 3.0,
			4 / 3.0 }, filtered, 1e-6);
	}

	@Test
	public void largeBox() {
		double signal[] = { 0, 0, 0, 0, 2, 0, 0, 0, 0, 0 };
		int size = 20;
		BoxFilter box = new BoxFilter(size);
		double[] filtered = box.filter(signal);
		TextSignalPrinter.printSignal(filtered);
		assertArrayEquals(new double[] { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1,
			0.1, 0.1, 0.1 }, filtered, 1e-6);
	}

	@Test
	public void someSignal() {
		double signal[] = { 1, 2, 3, 4, 5, 0, 0, 3, 2, 1 };
		int size = 5;
		BoxFilter box = new BoxFilter(size);
		double[] filtered = box.filter(signal);
		TextSignalPrinter.printSignal(filtered);
		assertArrayEquals(new double[] { 1.2, 2.0, 3.0, 2.8, 2.4, 2.4, 2.0,
			1.2, 1.2, 1.2 }, filtered, 1e-6);
	}
}
