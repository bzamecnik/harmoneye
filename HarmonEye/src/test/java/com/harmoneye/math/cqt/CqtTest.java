package com.harmoneye.math.cqt;

import org.apache.commons.math3.complex.Complex;
import org.junit.Test;

import com.harmoneye.audio.DecibelCalculator;
import com.harmoneye.audio.MultiRateRingBufferBank;
import com.harmoneye.audio.TextSignalPrinter;
import com.harmoneye.audio.ToneGenerator;
import com.harmoneye.math.matrix.ComplexUtils;

public class CqtTest {

	@Test
	public void testCqt() {
		double samplingFreq = 1000.0;
		//@formatter:off
		CqtContext ctx = CqtContext.create()
			.samplingFreq(samplingFreq)
			.maxFreq(400.0)
			.octaves(3)
//			.kernelOctaves(1)
			.binsPerHalftone(5)
			.build();
		//@formatter:on

		FastCqt cqt = new FastCqt(ctx);
		cqt.init();
		
		CqtCalculator cqtCalc = new CqtCalculator(ctx);

		ToneGenerator toneGen = new ToneGenerator(ctx.getSamplingFreq());
		double duration = ctx.getSignalBlockSize() / ctx.getSamplingFreq();
		System.out.println("signal block size: " + ctx.getSignalBlockSize());
		int bin = ctx.getTotalBins() - ctx.getBinsPerHalftone() * 5;
		System.out.println("bin: " + bin);
		double freq = cqtCalc.centerFreq(bin);
		System.out.println("frequency: " + freq);
		double[] signal = toneGen.generateSinWave(freq, duration);
		//TextSignalPrinter.printSignal(signal);

		Complex[] cqtSpectrum = ComplexUtils.complexArrayFromVector(cqt.transform(signal));

		double[] bins = toAmplitudeDbSpectrum(cqtSpectrum);

		TextSignalPrinter.printSignal(bins);
	}

	@Test
	public void testIteratedSingleOctaveCqt() {
		double samplingFreq = 1000.0;
		//@formatter:off
		CqtContext ctx = CqtContext.create()
			.samplingFreq(samplingFreq)
			.maxFreq(400.0)
			.octaves(3)
			.kernelOctaves(1)
			.binsPerHalftone(5)
			.build();
		//@formatter:on

		FastCqt cqt = new FastCqt(ctx);
		cqt.init();
		
		CqtCalculator cqtCalc = new CqtCalculator(ctx);

		MultiRateRingBufferBank ringBufferBank = new MultiRateRingBufferBank(ctx.getSignalBlockSize(), ctx.getOctaves());
		
		ToneGenerator toneGen = new ToneGenerator(ctx.getSamplingFreq());
		double duration = ctx.getOctaves() * ctx.getSignalBlockSize() / ctx.getSamplingFreq();
		int bin = ctx.getTotalBins() - ctx.getBinsPerHalftone() * 5;
		System.out.println("bin: " + bin);
		double freq = cqtCalc.centerFreq(bin);
		System.out.println("frequency: " + freq);
		double[] signal = toneGen.generateSinWave(freq, duration);
//		TextSignalPrinter.printSignal(signal);
		ringBufferBank.write(signal);

		double[] bins = new double[ctx.getTotalBins()];
		double[] samples = new double[ctx.getSignalBlockSize()];
		int startIndex = (ctx.getOctaves() - 1) * ctx.getBinsPerOctave();
		for (int octave = 0; octave < ctx.getOctaves(); octave++, startIndex -= ctx.getBinsPerOctave()) {
			System.out.println("start index: " + startIndex);
			ringBufferBank.readLast(octave, samples.length, samples);
			System.out.println("samples from octave " + octave + ":");
			TextSignalPrinter.printSignal(samples);
			Complex[] cqtSpectrum = ComplexUtils.complexArrayFromVector(cqt.transform(samples));
			toAmplitudeDbSpectrum(cqtSpectrum, bins, startIndex);
		}

		TextSignalPrinter.printSignal(bins);
	}
	
	private double[] toAmplitudeDbSpectrum(Complex[] cqtSpectrum) {
		return toAmplitudeDbSpectrum(cqtSpectrum, new double[cqtSpectrum.length], 0);
	}
	
	private double[] toAmplitudeDbSpectrum(Complex[] cqtSpectrum, double[] amplitudeSpectrum, int startIndex) {
		DecibelCalculator dbCalculator = new DecibelCalculator(16);
		for (int i = 0; i < cqtSpectrum.length; i++) {
			double amplitude = cqtSpectrum[i].abs();
			double amplitudeDb = dbCalculator.amplitudeToDb(amplitude);
			double value = dbCalculator.rescale(amplitudeDb);
			amplitudeSpectrum[startIndex + i] = value;
		}
		return amplitudeSpectrum;
	}
}
