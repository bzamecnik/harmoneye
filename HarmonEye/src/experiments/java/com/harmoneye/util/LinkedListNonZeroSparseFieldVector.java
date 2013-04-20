package com.harmoneye.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.Field;
import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.FieldVector;

public class LinkedListNonZeroSparseFieldVector<T extends FieldElement<T>> implements FieldVector<T> {

	private final List<SparseEntry<T>> entries;
	private final int dimension;
	private Field<T> field;

	public LinkedListNonZeroSparseFieldVector(FieldVector<T> other) {
		this.field = other.getField();
		this.dimension = other.getDimension();
		this.entries = new ArrayList<SparseEntry<T>>();
		T zero = getField().getZero();
		for (int i = 0; i < dimension; i++) {
			T entry = other.getEntry(i);
			if (!zero.equals(entry)) {
				entries.add(new SparseEntry<T>(entry, i));
			}
		}
	}

	@Override
	public T dotProduct(FieldVector<T> other) throws IllegalArgumentException {
		if (other.getDimension() != getDimension()) {
			throw new DimensionMismatchException(other.getDimension(), getDimension());
		}

		T dotProduct = getField().getZero();
		for (SparseEntry<T> entry : entries) {
			T entryValue = entry.getEntry();
			int columnIndex = entry.getIndex();
			dotProduct = dotProduct.add(entryValue.multiply(other.getEntry(columnIndex)));
		}

		return dotProduct;
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

	@Override
	public Field<T> getField() {
		return field;
	}

	@Override
	public FieldVector<T> copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> add(FieldVector<T> v) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> subtract(FieldVector<T> v) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> mapAdd(T d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> mapAddToSelf(T d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> mapSubtract(T d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> mapSubtractToSelf(T d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> mapMultiply(T d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> mapMultiplyToSelf(T d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> mapDivide(T d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> mapDivideToSelf(T d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> mapInv() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> mapInvToSelf() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> ebeMultiply(FieldVector<T> v) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> ebeDivide(FieldVector<T> v) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public T[] getData() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> projection(FieldVector<T> v) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldMatrix<T> outerProduct(FieldVector<T> v) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T getEntry(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setEntry(int index, T value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getDimension() {
		return dimension;
	}

	@Override
	public FieldVector<T> append(FieldVector<T> v) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> append(T d) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldVector<T> getSubVector(int index, int n) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSubVector(int index, FieldVector<T> v) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(T value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T[] toArray() {
		throw new UnsupportedOperationException();
	}

}