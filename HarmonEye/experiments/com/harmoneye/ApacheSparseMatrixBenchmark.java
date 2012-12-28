package com.harmoneye;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.SparseFieldMatrix;

public class ApacheSparseMatrixBenchmark {

	private static final int COLUMN_COUNT = 8 * 1024;
	private static final int ROW_COUNT = 100;

	public static void main(String[] args) {

		//		SparseFieldMatrix<Complex> matrix = new SparseFieldMatrix(ComplexField.getInstance(), ROW_COUNT, COLUMN_COUNT);
		SparseFieldMatrix<Complex> matrix = new NonZeroSparseFieldMatrix(ROW_COUNT, COLUMN_COUNT);
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
