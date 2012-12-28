package com.harmoneye;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SparseRealMatrix;

public class SparseMatrixExperiment {
	public static void main(String[] args) {
		SparseRealMatrix matrix = new OpenMapRealMatrix(3, 3);
		matrix.setEntry(0, 0, 1);
		matrix.setEntry(0, 1, 2);
		matrix.setEntry(1, 0, 3);
		matrix.setEntry(2, 2, 4);
		System.out.println(matrix);
		
		RealVector result = matrix.operate(new ArrayRealVector(new double[] {1, 1, 1}));
		System.out.println(result);
	}
}
