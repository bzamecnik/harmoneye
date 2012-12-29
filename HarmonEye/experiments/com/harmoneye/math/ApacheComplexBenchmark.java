package com.harmoneye.math;

import java.util.Date;
import java.util.Random;

import org.apache.commons.math3.complex.Complex;

public class ApacheComplexBenchmark {

	public static void main(String[] args) {

		Random random = new Random();
		
		Complex a = new Complex(random.nextDouble(), random.nextDouble());
		Complex b = new Complex(random.nextDouble(), random.nextDouble());
		
		long start = System.nanoTime();
		
		int iterations = (int)1e8;
		for (int i = 0; i < iterations; i++) {
			a.multiply(b);
		}
		
		long end = System.nanoTime();
		System.out.println(new Date());
		long timeNanos = end - start;
		double timeMillis = timeNanos / 1e6;
		System.out.println("total: " + timeMillis + " ms");
		System.out.println("average: " + (timeMillis / iterations) + " ms");
		System.out.println("average: " + (iterations / timeMillis * 1e3) + " / sec");
	}
}
