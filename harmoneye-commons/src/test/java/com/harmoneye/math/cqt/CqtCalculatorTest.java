package com.harmoneye.math.cqt;

import org.junit.Test;

import com.harmoneye.audio.util.TextSignalPrinter;
import com.harmoneye.math.matrix.ComplexVector;

public class CqtCalculatorTest {

	private double samplingFreq = 1000.0;
	//@formatter:off
	private CqtContext ctx = CqtContext.create()
		.samplingFreq(samplingFreq)
		.maxFreq(400.0)
		.octaves(1)
//		.kernelOctaves(1)
		.binsPerHalftone(1)
		.build();
	//@formatter:on

	@Test
	public void printBandWidths() {
		CqtCalculator calc = new CqtCalculator(ctx);

		int totalBins = ctx.getTotalBins();
		for (int i = 0; i < totalBins; i++) {
			int bandWidth = calc.bandWidth(i);
			System.out.println(bandWidth + " (<= " + calc.nextPowerOf2(bandWidth) + ")");
		}
	}

	@Test
	public void printCenterFrequencies() {
		CqtCalculator calc = new CqtCalculator(ctx);

		int totalBins = ctx.getTotalBins();
		for (int i = 0; i < totalBins; i++) {
			System.out.println(calc.centerFreq(i));
		}
	}
	
	@Test
	public void printKernel() {
		System.out.println("signal block size:" + ctx.getSignalBlockSize());
		CqtCalculator calc = new CqtCalculator(ctx);

		int totalBins = ctx.getKernelBins();
		//for (int i = 0; i < totalBins; i++) {
//		ComplexVector kernel = calc.temporalKernel(ctx.getKernelBins() - 1);
		ComplexVector kernel = calc.spectralKernel(ctx.getKernelBins() - 1);
		TextSignalPrinter.printSignal(kernel);
		//}
	}
	
}
