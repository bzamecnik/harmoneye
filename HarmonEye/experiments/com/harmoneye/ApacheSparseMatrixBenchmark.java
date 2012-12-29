package com.harmoneye;

import java.util.Random;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldMatrix;

public class ApacheSparseMatrixBenchmark {

	private static final int NON_ZERO_ENTRY_COUNT = 2000;
	private static final int COLUMN_COUNT = 8 * 1024;
	private static final int ROW_COUNT = 10;

	public static void main(String[] args) {

		Random random = new Random();

		ComplexField field = ComplexField.getInstance();
		//		 FieldMatrix<Complex> rwMatrix = new SparseFieldMatrix<Complex>(field, ROW_COUNT, COLUMN_COUNT);
		FieldMatrix<Complex> rwMatrix = new NonZeroSparseFieldMatrix<Complex>(field, ROW_COUNT, COLUMN_COUNT);

		for (int i = 0; i < NON_ZERO_ENTRY_COUNT; i++) {
			rwMatrix.setEntry(random.nextInt(ROW_COUNT), random.nextInt(COLUMN_COUNT), randomComplex(random));
		}

		//		FieldMatrix<Complex> matrix = rwMatrix;
		FieldMatrix<Complex> matrix = new LinkedListNonZeroSparseFieldMatrix<Complex>(rwMatrix);

		ArrayFieldVector<Complex> vector = new ArrayFieldVector<Complex>(COLUMN_COUNT, Complex.ZERO);
		for (int i = 0; i < vector.getDimension(); i++) {
			vector.setEntry(i, randomComplex(random));
		}

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

	private static Complex randomComplex(Random random) {
		Complex value = new Complex(random.nextDouble(), random.nextDouble());
		return value;
	}
}
