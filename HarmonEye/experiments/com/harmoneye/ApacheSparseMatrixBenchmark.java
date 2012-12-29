package com.harmoneye;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldMatrix;

public class ApacheSparseMatrixBenchmark {

	private static final int COLUMN_COUNT = 8 * 1024;
	private static final int ROW_COUNT = 10;

	public static void main(String[] args) {

		// FieldMatrix<Complex> matrix = new SparseFieldMatrix<Complex>(field, ROW_COUNT, COLUMN_COUNT);
		ComplexField field = ComplexField.getInstance();
		FieldMatrix<Complex> matrix = new NonZeroSparseFieldMatrix<Complex>(field, ROW_COUNT, COLUMN_COUNT);
		ArrayFieldVector<Complex> vector = new ArrayFieldVector<Complex>(COLUMN_COUNT, Complex.ZERO);

		long start = System.nanoTime();

		int iterations = (int) 1e3;
		for (int i = 0; i < iterations; i++) {
			matrix.operate(vector);
		}

		long end = System.nanoTime();
		long timeNanos = end - start;
		double timeMillis = timeNanos / 1e6;
		System.out.println("total: " + timeMillis + " ms");
		System.out.println("average: " + (timeMillis / iterations) + " ms");
		System.out.println("average: " + (iterations / timeMillis * 1e3) + " / sec");
	}
}
