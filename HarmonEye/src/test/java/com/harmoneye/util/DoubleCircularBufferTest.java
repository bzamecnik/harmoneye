package com.harmoneye.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class DoubleCircularBufferTest {

	@Test
	public void writeAndReadFullBuffer() {
		DoubleCircularBuffer buffer = new DoubleCircularBuffer(5);

		double[] writtenData = new double[] { 1, 2, 3, 4, 5 };
		buffer.write(writtenData);

		double[] result = new double[writtenData.length];
		buffer.readLast(result, result.length);

		assertEquals(writtenData.length, result.length);
		for (int i = 0; i < result.length; i++) {
			assertEquals(writtenData[i], result[i], 1e-6);
		}
	}

	@Test
	public void writeFullBufferAndReadLess() {
		DoubleCircularBuffer buffer = new DoubleCircularBuffer(5);

		double[] writtenData = new double[] { 1, 2, 3, 4, 5 };
		buffer.write(writtenData);

		double[] result = new double[3];
		buffer.readLast(result, result.length);

		assertEquals(3, result.length);
		int offset = writtenData.length - result.length;
		for (int i = 0; i < result.length; i++) {
			assertEquals(writtenData[i + offset], result[i], 1e-6);
		}
	}

	@Test
	public void writePastBufferCapacity() {
		DoubleCircularBuffer buffer = new DoubleCircularBuffer(5);

		double[] writtenData = new double[] { 1, 2, 3, 4, 5, 6, 7 };
		buffer.write(writtenData);

		double[] result = new double[5];
		buffer.readLast(result, result.length);

		double[] expectedData = new double[] { 3, 4, 5, 6, 7 };
		assertEquals(expectedData.length, result.length);
		for (int i = 0; i < result.length; i++) {
			assertEquals(expectedData[i], result[i], 1e-6);
		}
	}
}
