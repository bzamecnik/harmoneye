package com.harmoneye.math.matrix;

import static org.junit.Assert.assertArrayEquals;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.FieldMatrix;
import org.junit.Test;

public class MatrixTest {

	@Test
	public void testApacheMatrix() {
		FieldMatrix<Complex> matrix = new NonZeroSparseFieldMatrix<Complex>(
		// FieldMatrix<Complex> matrix = new Array2DRowFieldMatrix<Complex>(
			ComplexField.getInstance(), 4, 5);
		Complex z = Complex.ZERO;
		matrix.setRow(0, new Complex[] { z, z, z, new Complex(1.0, 2.0),
			new Complex(3.5, 4.5) });
		matrix.setRow(1, new Complex[] { z, z, new Complex(5.0, 6.0),
			new Complex(7.5, 8.5), z });
		matrix.setRow(2, new Complex[] { z, new Complex(9.0, 10.0),
			new Complex(11.5, 12.5), z, z });
		matrix.setRow(3, new Complex[] { new Complex(13.0, 14.0),
			new Complex(15.5, 16.5), z, z, z });

		FieldMatrix<Complex> llMatrix = new LinkedListNonZeroSparseFieldMatrix<>(
			matrix);

		Complex[] vector = new Complex[] { new Complex(1.0, 2.0),
			new Complex(3.0, 4.0), new Complex(5.0, 6.0),
			new Complex(7.0, 8.0), new Complex(9.0, 10.0) };

		Complex[] result = llMatrix.operate(vector);

		ComplexVector resultVector = ComplexUtils
			.complexVectorFromArray(result);
		System.out.println(resultVector);
		assertArrayEquals(new double[] { -22.5, 97.5, -26.5, 179.5, -30.5,
			197.5, -34.5, 151.5 }, resultVector.getElements(), 1e-6);
	}

	@Test
	public void testMyMatrix() {
		DenseDComplexMatrix2D matrix = new DenseDComplexMatrix2D(4, 5);

		matrix.setRow(0, new double[] { 0, 0, 0, 0, 0, 0, 1.0, 2.0, 3.5, 4.5 });
		matrix.setRow(1, new double[] { 0, 0, 0, 0, 5.0, 6.0, 7.5, 8.5, 0, 0 });
		matrix.setRow(2,
			new double[] { 0, 0, 9.0, 10.0, 11.5, 12.5, 0, 0, 0, 0 });
		matrix.setRow(3, new double[] { 13.0, 14.0, 15.5, 16.5, 0, 0, 0, 0, 0,
			0 });

		double[] vector = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0,
			8.0, 9.0, 10.0 };

		double[] result = matrix.operate(vector);

		ComplexVector resultVector = new ComplexVector(result);
		System.out.println(resultVector);
		assertArrayEquals(new double[] { -22.5, 97.5, -26.5, 179.5, -30.5,
			197.5, -34.5, 151.5 }, resultVector.getElements(), 1e-6);
	}
}
