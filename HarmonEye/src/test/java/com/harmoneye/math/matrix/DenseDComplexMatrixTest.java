package com.harmoneye.math.matrix;

import static org.junit.Assert.*;

import org.junit.Test;

public class DenseDComplexMatrixTest {

	@Test
	public void testSetRow() {
		DenseDComplexMatrix2D matrix = new DenseDComplexMatrix2D(3, 4);
		matrix.setRow(0, new double[] { 1, 2, 3, 4, 5, 6, 7, 8 });
		matrix.setRow(1, new double[] { 9, 10, 11, 12, 13, 14, 15, 16 });
		matrix.setRow(2, new double[] { 17, 18, 19, 20, 21, 22, 23, 24 });
		System.out.println(matrix);
	}

}
