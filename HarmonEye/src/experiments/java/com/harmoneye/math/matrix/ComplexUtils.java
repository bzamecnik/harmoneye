package com.harmoneye.math.matrix;

import org.apache.commons.math3.complex.Complex;

public class ComplexUtils {

	public static ComplexVector complexVectorFromArray(Complex[] array) {
		int size = array.length;
		ComplexVector vector = new ComplexVector(size);
		double[] elements = vector.getElements();

		for (int i = 0; i < size; i++) {
			Complex value = array[i];
			elements[2 * i] = value.getReal();
			elements[2 * i + 1] = value.getImaginary();
		}
		return vector;
	}

	public static Complex[] complexArrayFromVector(ComplexVector vector) {
		int size = vector.size();
		Complex[] array = new Complex[size];
		double[] elements = vector.getElements();

		for (int i = 0; i < size; i++) {
			array[i] = new Complex(elements[2 * i], elements[2 * i + 1]);
		}
		return array;
	}
}
