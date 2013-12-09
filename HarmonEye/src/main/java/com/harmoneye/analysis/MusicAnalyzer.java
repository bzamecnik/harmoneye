package com.harmoneye.analysis;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import com.harmoneye.audio.MultiRateRingBufferBank;
import com.harmoneye.math.cqt.CqtContext;
import com.harmoneye.math.cqt.FastCqt;
import com.harmoneye.viz.Visualizer;

public class MusicAnalyzer implements SoundConsumer {

	/** [0.0; 1.0] 1.0 = no smoothing */
	private static final double SMOOTHING_FACTOR = 0.25;

	private CqtContext ctx;

	private FastCqt cqt;
	private MultiRateRingBufferBank ringBufferBank;
	private HarmonicPatternPitchClassDetector pcDetector;
	private Visualizer<PitchClassProfile> visualizer;
	private ExpSmoother binSmoother;

	private double[] samples;
	/** peak amplitude spectrum */
	private double[] amplitudeSpectrumDb;
	private double[] octaveBinsDb;

	private double dbThreshold;
	private double dbThresholdInv;

	private AtomicBoolean initialized = new AtomicBoolean();

	private MovingAverageAccumulator accumulator;
	private AtomicBoolean accumulatorEnabled = new AtomicBoolean();

	public MusicAnalyzer(Visualizer<PitchClassProfile> visualizer,
		float sampleRate, int bitsPerSample) {
		this.visualizer = visualizer;

		dbThreshold = -(20 * FastMath.log10(2 << (bitsPerSample - 1)));
		dbThresholdInv = 1.0 / dbThreshold;

		//@formatter:off
		ctx = CqtContext.create()
			.samplingFreq(sampleRate)
//			.maxFreq((2 << 6) * 65.4063913251)
//			.octaves(2)
			.kernelOctaves(1)
			.binsPerHalftone(7)
			.build();
		//@formatter:on

		samples = new double[ctx.getSignalBlockSize()];
		amplitudeSpectrumDb = new double[ctx.getTotalBins()];
		octaveBinsDb = new double[ctx.getBinsPerOctave()];
		ringBufferBank = new MultiRateRingBufferBank(ctx.getSignalBlockSize(), ctx.getOctaves());
		pcDetector = new HarmonicPatternPitchClassDetector(ctx);
		binSmoother = new ExpSmoother(ctx.getBinsPerOctave(), SMOOTHING_FACTOR);
		accumulator = new MovingAverageAccumulator(ctx.getBinsPerOctave());

		cqt = new FastCqt(ctx);
	}

	public void init() {
		cqt.init();
		initialized.set(true);
	}

	@Override
	public void consume(double[] samples) {
		ringBufferBank.write(samples);
	}

	public void updateSignal() {
		if (!initialized.get()) {
			return;
		}
		for (int octave = 0; octave < ctx.getOctaves(); octave++) {
			ringBufferBank.readLast(octave, samples.length, samples);
			Complex[] cqSpectrum = cqt.transform(samples);
			int startIndex = octave * ctx.getBinsPerOctave();
			toAmplitudeDbSpectrum(cqSpectrum, amplitudeSpectrumDb, startIndex);
		}
		PitchClassProfile pcProfile = computePitchClassProfile(amplitudeSpectrumDb);
		visualizer.update(pcProfile);
	}

	private void toAmplitudeDbSpectrum(Complex[] cqSpectrum, double[] amplitudeSpectrum, int startIndex) {
		for (int i = 0; i < cqSpectrum.length; i++) {
			double amplitude = cqSpectrum[i].abs();
			// Since reference amplitude is 1, this code is implied:
			// double referenceAmplitude = 1;
			// amplitude /= referenceAmplitude; 
			double amplitudeDb = 20 * FastMath.log10(amplitude);
			if (amplitudeDb < dbThreshold) {
				amplitudeDb = dbThreshold;
			}
			// rescale: [DB_THRESHOLD; 0] -> [-1; 0] -> [0; 1]
			amplitudeSpectrumDb[startIndex + i] = 1 - (amplitudeDb * dbThresholdInv);
		}
	}

	private PitchClassProfile computePitchClassProfile(double[] amplitudeSpectrumDb) {
		int binsPerOctave = ctx.getBinsPerOctave();
		for (int i = 0; i < binsPerOctave; i++) {
			// maximum over octaves:
			double value = 0;
			for (int j = i; j < amplitudeSpectrumDb.length; j += binsPerOctave) {
				value = FastMath.max(value, amplitudeSpectrumDb[j]);
			}
			octaveBinsDb[i] = value;
		}

		double[] pitchClassBinsDb = pcDetector.detectPitchClasses(amplitudeSpectrumDb);
		double max = 0;
		for (int i = 0; i < amplitudeSpectrumDb.length; i++) {
			max = FastMath.max(max, amplitudeSpectrumDb[i]);
		}
		// just an ad hoc reduction of noise and equalization
		for (int i = 0; i < pitchClassBinsDb.length; i++) {
			pitchClassBinsDb[i] = FastMath.pow(pitchClassBinsDb[i], 3);
		}
		for (int i = 0; i < pitchClassBinsDb.length; i++) {
			pitchClassBinsDb[i] *= max;
		}
		for (int i = 0; i < octaveBinsDb.length; i++) {
			octaveBinsDb[i] *= pitchClassBinsDb[i];
		}
		for (int i = 0; i < octaveBinsDb.length; i++) {
			octaveBinsDb[i] = FastMath.pow(octaveBinsDb[i], 1 / 3.0);
		}

		double[] pitchClassProfileDb = null;//octaveBinsDb;
		if (accumulatorEnabled.get()) {
			accumulator.add(octaveBinsDb);
			pitchClassProfileDb = accumulator.getAverage();
		} else {
			binSmoother.smooth(octaveBinsDb);
			pitchClassProfileDb = binSmoother.smooth(octaveBinsDb);
		}
		
		PitchClassProfile pcProfile = new PitchClassProfile(pitchClassProfileDb,
			ctx.getHalftonesPerOctave(), ctx.getBinsPerHalftone());
		return pcProfile;
	}

	public void toggleAccumulatorEnabled() {
		accumulatorEnabled.set(!accumulatorEnabled.get());
		if (accumulatorEnabled.get()) {
			accumulator.reset();
		}
	}

}
