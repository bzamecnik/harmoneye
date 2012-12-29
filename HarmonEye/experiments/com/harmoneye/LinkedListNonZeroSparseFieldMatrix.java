package com.harmoneye;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.AbstractFieldMatrix;
import org.apache.commons.math3.linear.FieldMatrix;

public class LinkedListNonZeroSparseFieldMatrix<T extends FieldElement<T>> extends AbstractFieldMatrix<T> {

	private final List<List<SparseEntry<T>>> rows;
	private final int rowCount;
	private final int columnCount;

	public LinkedListNonZeroSparseFieldMatrix(FieldMatrix<T> other) {
		super(other.getField(), other.getRowDimension(), other.getColumnDimension());

		rowCount = other.getRowDimension();
		columnCount = other.getColumnDimension();

		rows = new ArrayList<List<SparseEntry<T>>>();
		T zero = getField().getZero();
		for (int i = 0; i < rowCount; i++) {
			List<SparseEntry<T>> sparseColumns = new LinkedList<SparseEntry<T>>();
			for (int j = 0; j < columnCount; j++) {
				T entry = other.getEntry(i, j);
				if (!zero.equals(entry)) {
					sparseColumns.add(new SparseEntry<T>(entry, j));
				}
			}
			rows.add(sparseColumns);
		}
	}

	@Override
	public T[] operate(final T[] vector) {
		final int rowCount = getRowDimension();
		final int columnCount = getColumnDimension();
		if (vector.length != columnCount) {
			throw new DimensionMismatchException(vector.length, columnCount);
		}

		T zero = getField().getZero();
		final T[] out = buildArray(getField(), rowCount);
		for (int rowIndex = 0; rowIndex < rowCount; ++rowIndex) {
			T sum = zero;
			List<SparseEntry<T>> row = rows.get(rowIndex);
			for (SparseEntry<T> columnEntry : row) {
				T entry = columnEntry.getEntry();
				int columnIndex = columnEntry.getIndex();
				if (!zero.equals(entry)) {
					sum = sum.add(entry.multiply(vector[columnIndex]));
				}
			}
			out[rowIndex] = sum;
		}

		return out;
	}

	@Override
	public FieldMatrix<T> createMatrix(int rowDimension, int columnDimension) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldMatrix<T> copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public T getEntry(int row, int column) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setEntry(int row, int column, T value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addToEntry(int row, int column, T increment) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void multiplyEntry(int row, int column, T factor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getRowDimension() {
		return rowCount;
	}

	@Override
	public int getColumnDimension() {
		return columnCount;
	}

	private static class SparseEntry<T> {

		T entry;
		int index;

		public SparseEntry(T entry, int index) {
			this.entry = entry;
			this.index = index;
		}

		public T getEntry() {
			return entry;
		}

		public int getIndex() {
			return index;
		}

	}
}