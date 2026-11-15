package com.harmoneye.math.matrix;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import com.harmoneye.math.matrix.SparseRCComplexMatrix2D.Builder;

public class SparseRCComplexMatrix2DTest {

	@Test
	public void testMySparseMatrix() {
		Builder builder = new Builder(4, 5);

		builder.addRow(0, new ComplexVector(new double[] { 0, 0, 0, 0, 0, 0,
			1.0, 2.0, 3.5, 4.5 }));
		builder.addRow(1, new ComplexVector(new double[] { 0, 0, 0, 0, 5.0,
			6.0, 7.5, 8.5, 0, 0 }));
		builder.addRow(2, new ComplexVector(new double[] { 0, 0, 9.0, 10.0,
			11.5, 12.5, 0, 0, 0, 0 }));
		builder.addRow(3, new ComplexVector(new double[] { 13.0, 14.0, 15.5,
			16.5, 0, 0, 0, 0, 0, 0 }));

		SparseRCComplexMatrix2D matrix = builder.build();
		
		System.out.println(matrix);

		double[] vector = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0,
			8.0, 9.0, 10.0 };

		double[] result = matrix.operate(vector);

		ComplexVector resultVector = new ComplexVector(result);
		System.out.println(resultVector);
		assertArrayEquals(new double[] { -22.5, 97.5, -26.5, 179.5, -30.5,
			197.5, -34.5, 151.5 }, resultVector.getElements(), 1e-6);
	}

}
