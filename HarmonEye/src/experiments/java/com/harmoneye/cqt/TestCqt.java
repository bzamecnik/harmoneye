package com.harmoneye.cqt;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.commons.math3.complex.Complex;
import org.junit.Test;

public class TestCqt {

	private static final double EPSILON = 1e-3;
	private CqtContext ctx = CqtContext.create().build();

	@Test
	public void testDirectCqt() {
		double expectedCqBins[] = { 0.28298965934681874, 0.9851996110068437, 0.48715254189231394, 0.0413224865910265,
			0.0027093561554847757, 0.005415844293418445, 9.444203347283641E-4, 0.004322391461315617,
			0.00880780790995201, 0.00402135826523725, 0.00425143658788258, 0.006013506365593061 };
		testCqt(new DirectCqt(ctx), expectedCqBins);
	}

	@Test
	public void testFastCqt() {
		double[] expectedCqBins = { 0.286063, 0.984672, 0.487545, 0.0407473, 0.00292965, 0.00356335, 0.00322763,
			0.00277204, 0.00246763, 0.00251696, 0.00224605, 0.00248127 };
		FastCqt cqt = new FastCqt(ctx);
		cqt.init();
		testCqt(cqt, expectedCqBins);
	}

	private void testCqt(Cqt cqt, double[] expectedCqBins) {

		double signal[] = { 0., 0.995641, -0.185724, -0.960996, 0.364986, 0.892913, -0.531547, -0.79376, 0.679613,
			0.666986, -0.804031, -0.517005, 0.900472, 0.349033, -0.965579, -0.168916, 0.997089, -0.0170778, -0.993903,
			0.202478, 0.956133, -0.380832, -0.885094, 0.545935, 0.783257, -0.692042, -0.654165, 0.814068, 0.502311,
			-0.907768, -0.332978, 0.969881, 0.152059, -0.998245, 0.0341507, 0.991875, -0.219172, -0.950991, 0.396568,
			0.877017, -0.560164, -0.772525, 0.704269, 0.641153, -0.823868, -0.487471, 0.914799, 0.316826, -0.973899,
			-0.135158, 0.999111, -0.0512135, -0.989558, 0.235803, 0.945572, -0.412187, -0.868683, 0.574229, 0.761568,
			-0.71629, -0.627953, 0.833427, 0.472488, -0.921564, -0.300582, 0.977633, 0.118217, -0.999685 };

		Complex[] cqBins = cqt.transform(signal);

		System.out.println("cq bins:");
//		System.out.println(Formatter.formatArray(abs(cqBins)));
		System.out.println();

		for (int i = 0; i < cqBins.length; i++) {
			double error = Math.abs(cqBins[i].abs() - expectedCqBins[i]);
			if (error >= EPSILON) {
				System.out.println("too high error " + error + " for bin " + i);
			}
			assertTrue(error < EPSILON);
		}

	}

	public void testFastCqtSpeed() {

		FastSparseVectorCqt cqt = new FastSparseVectorCqt(ctx);
		
		double signal[] = new double[ctx.getSignalBlockSize()];

		

		long start = System.nanoTime();
		int iterations = 100;
		for (int i = 0; i < iterations; i++) {
			Complex[] cqBins = cqt.transform(signal);

		}
		long end = System.nanoTime();
		System.out.println(new Date());
		System.out.println("total: " + (end - start) / 1e6 + " ms");
		System.out.println("average: " + (end - start) / (iterations * 1e6) + " ms");
		System.out.println("average: " + (iterations * 1e9) / (end - start) + " per sec");

	}

	public static void main(String[] args) {
		new TestCqt().testFastCqtSpeed();
	}

	private Double[] abs(Complex[] values) {
		Double[] absValues = new Double[values.length];
		for (int i = 0; i < values.length; i++) {
			absValues[i] = values[i].abs();
		}
		return absValues;
	}
}
