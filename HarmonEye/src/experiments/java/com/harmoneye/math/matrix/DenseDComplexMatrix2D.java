package com.harmoneye.math.matrix;

public class DenseDComplexMatrix2D {

	private double[] elements;
	private int rows;
	private int columns;

	public DenseDComplexMatrix2D(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		this.elements = new double[2 * rows * columns];
	}

	public void setRow(int row, double[] vector) {
		int rowStart = 2 * row * columns;
		int length = vector.length;
		for (int i = 0; i < length; i++) {
			elements[rowStart + i] = vector[i];
		}
	}

	public double[] operate(double[] vector) {
		return operate(vector, new double[2 * rows]);
	}

	// vector size - 2 * columns
	// result size - 2 * rows
	public double[] operate(double[] vector, double[] result) {
		int matrixIndex = 0;
		int resultIndex = 0;
		for (int row = 0; row < rows; row++, resultIndex += 2) {
			double sumRe = 0;
			double sumIm = 0;
			int vectorIndex = 0;
			for (int col = 0; col < columns; col++, matrixIndex += 2, vectorIndex += 2) {
				double mRe = elements[matrixIndex];
				double vRe = vector[vectorIndex];
				double mIm = elements[matrixIndex + 1];
				double vIm = vector[vectorIndex + 1];
				sumRe += mRe * vRe - mIm * vIm;
				sumIm += mIm * vRe + mRe * vIm;
			}
			result[resultIndex] = sumRe;
			result[resultIndex + 1] = sumIm;
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getSimpleName()).append(' ');
		sb.append(rows).append('x').append(columns).append('\n');
		sb.append("[");
		int index = 0;
		for (int row = 0; row < rows; row++) {
			if (row > 0) {
				sb.append("\n");
			}
			for (int col = 0; col < columns; col++, index += 2) {
				if (col > 0) {
					sb.append(", ");
				}
				sb.append('[').append(elements[index]).append(", ")
					.append(elements[index + 1]).append(']');
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
