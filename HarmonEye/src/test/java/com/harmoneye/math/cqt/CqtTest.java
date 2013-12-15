package com.harmoneye.math.cqt;

import org.apache.commons.math3.complex.Complex;
import org.junit.Test;

import com.harmoneye.audio.DecibelCalculator;
import com.harmoneye.audio.TextSignalPrinter;
import com.harmoneye.audio.ToneGenerator;

public class CqtTest {

	@Test
	public void testCqt() {
		double samplingFreq = 1000.0;
	//@formatter:off
	CqtContext ctx = CqtContext.create()
		.samplingFreq(samplingFreq)
		.maxFreq(400.0)
		.octaves(1)
		.kernelOctaves(1)
		.binsPerHalftone(5)
		.build();
	//@formatter:on

		FastCqt cqt = new FastCqt(ctx);
		cqt.init();
		
		CqtCalculator cqtCalc = new CqtCalculator(ctx);

		ToneGenerator toneGen = new ToneGenerator(ctx.getSamplingFreq());
		double duration = ctx.getSignalBlockSize() / ctx.getSamplingFreq();
		System.out.println("signal block size: " + ctx.getSignalBlockSize());
		int bin = ctx.getTotalBins() - ctx.getBinsPerHalftone() * 2;
		System.out.println("bin: " + bin);
		double freq = cqtCalc.centerFreq(bin);
		System.out.println("frequency: " + freq);
		double[] signal = toneGen.generateSinWave(freq, duration);
		//TextSignalPrinter.printSignal(signal);

		Complex[] cqSpectrum = cqt.transform(signal);

		double[] bins = toAmplitudeDbSpectrum(cqSpectrum);

		TextSignalPrinter.printSignal(bins);
	}

	private double[] toAmplitudeDbSpectrum(Complex[] cqSpectrum) {
		DecibelCalculator dbCalculator = new DecibelCalculator(16);
		double[] amplitudeSpectrum = new double[cqSpectrum.length];
		for (int i = 0; i < cqSpectrum.length; i++) {
			double amplitude = cqSpectrum[i].abs();
			double amplitudeDb = dbCalculator.amplitudeToDb(amplitude);
			double value = dbCalculator.rescale(amplitudeDb);
			amplitudeSpectrum[i] = value;
		}
		return amplitudeSpectrum;
	}
}
