package com.harmoneye;

import org.apache.commons.math3.Field;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.SparseFieldMatrix;

public class NonZeroSparseFieldMatrix extends SparseFieldMatrix<Complex> {

	private final Field<Complex> field;

	public NonZeroSparseFieldMatrix(int rowCount, int columnCount) {
		super(ComplexField.getInstance(), rowCount, columnCount);
		this.field = ComplexField.getInstance();
	}

	@Override
	public Complex[] operate(final Complex[] v) {

		final int nRows = getRowDimension();
		final int nCols = getColumnDimension();
		if (v.length != nCols) {
			throw new DimensionMismatchException(v.length, nCols);
		}

		final Complex[] out = buildArray(field, nRows);
		for (int row = 0; row < nRows; ++row) {
			Complex sum = field.getZero();
			for (int i = 0; i < nCols; ++i) {
				Complex entry = getEntry(row, i);
				if (entry.abs() > 0) {
					sum = sum.add(entry.multiply(v[i]));
				}
			}
			out[row] = sum;
		}

		return out;
	}

}