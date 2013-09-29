package com.harmoneye;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import com.harmoneye.cqt.CqtContext;
import com.harmoneye.cqt.FastCqt;
import com.harmoneye.cqt.HarmonicPatternPitchClassDetector;
import com.harmoneye.util.DoubleCircularBuffer;

public class MusicAnalyzer implements SoundConsumer {

	/** [0.0; 1.0] 1.0 = no smooting */
	private static final double SMOOTHING_FACTOR = 0.25;

	private CqtContext ctx = CqtContext.create().build();
	private FastCqt cqt = new FastCqt(ctx);

	private final double DB_THRESHOLD = -(20 * FastMath.log10(2 << (16 - 1)));
	private final int BINS_PER_HALFTONE = ctx.getBinsPerHalftone();
	private final int PITCH_BIN_COUNT = ctx.getBinsPerOctave();
	private final int HALFTONE_PER_OCTAVE_COUNT = ctx.getHalftonesPerOctave();

	// in samples
	private int signalBlockSize = ctx.getSignalBlockSize();

	private double[] amplitudes = new double[signalBlockSize];
	/** peak amplitude spectrum */
	private double[] amplitudeSpectrumDb;
	private double[] octaveBinsDb = new double[PITCH_BIN_COUNT];

	private DoubleCircularBuffer amplitudeBuffer = new DoubleCircularBuffer(signalBlockSize);

	private HarmonicPatternPitchClassDetector pcDetector = new HarmonicPatternPitchClassDetector(ctx);

	private ExpSmoother binSmoother = new ExpSmoother(PITCH_BIN_COUNT, SMOOTHING_FACTOR);

	private MovingAverageAccumulator accumulator = new MovingAverageAccumulator(PITCH_BIN_COUNT);
	private boolean accumulatorEnabled = false;

	private Visualizer<PitchClassProfile> visualizer;

	private boolean initialized;

	public MusicAnalyzer(Visualizer<PitchClassProfile> visualizer) {
		this.visualizer = visualizer;
	}

	public void init() {
		cqt.init();
		initialized = true;
	}

	@Override
	public void consume(double[] samples) {
		amplitudeBuffer.write(samples);
	}

	public void updateSignal() {
		if (!initialized) {
			return;
		}
		amplitudeBuffer.readLast(amplitudes, amplitudes.length);
		computeAmplitudeSpectrum(amplitudes);
		double[] pitchClassProfileDb = computePitchClassProfile();
		PitchClassProfile pcProfile = new PitchClassProfile(pitchClassProfileDb, HALFTONE_PER_OCTAVE_COUNT,
			BINS_PER_HALFTONE);
		visualizer.update(pcProfile);
	}

	private void computeAmplitudeSpectrum(double[] signal) {
		Complex[] cqSpectrum = cqt.transform(signal);
		if (amplitudeSpectrumDb == null) {
			amplitudeSpectrumDb = new double[cqSpectrum.length];
		}
		for (int i = 0; i < amplitudeSpectrumDb.length; i++) {
			double amplitude = cqSpectrum[i].abs();
			double referenceAmplitude = 1;
			double amplitudeDb = 20 * FastMath.log10(amplitude / referenceAmplitude);
			if (amplitudeDb < DB_THRESHOLD) {
				amplitudeDb = DB_THRESHOLD;
			}
			double scaledAmplitudeDb = 1 + amplitudeDb / -DB_THRESHOLD;
			amplitudeSpectrumDb[i] = scaledAmplitudeDb;
		}
	}

	private double[] computePitchClassProfile() {
		for (int i = 0; i < PITCH_BIN_COUNT; i++) {
			// maximum over octaves:
			double value = 0;
			for (int j = i; j < amplitudeSpectrumDb.length; j += PITCH_BIN_COUNT) {
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

		double[] pitchClassProfileDb = null;
		if (accumulatorEnabled) {
			accumulator.add(octaveBinsDb);
			pitchClassProfileDb = accumulator.getAverage();
		} else {
			binSmoother.smooth(octaveBinsDb);
			pitchClassProfileDb = binSmoother.smooth(octaveBinsDb);
		}
		return pitchClassProfileDb;
	}

	public int getSignalBlockSize() {
		return signalBlockSize;
	}

	private static class ExpSmoother {
		double[] data;
		double currentWeight;
		double previousWeight;

		public ExpSmoother(int size, double currentWeight) {
			data = new double[size];
			this.currentWeight = currentWeight;
			previousWeight = 1 - currentWeight;
		}

		public double[] smooth(double[] currentFrame) {
			assert data.length == currentFrame.length;

			for (int i = 0; i < data.length; i++) {
				data[i] = previousWeight * data[i] + currentWeight * currentFrame[i];
			}
			return data;
		}
	}

	private static class MovingAverageAccumulator {
		double[] data;
		int frameCount;

		public MovingAverageAccumulator(int size) {
			data = new double[size];
		}

		public double[] getAverage() {
			return data;
		}

		public double[] add(double[] currentFrame) {
			assert data.length == currentFrame.length;

			frameCount++;

			double weight = 1.0 / frameCount;

			for (int i = 0; i < data.length; i++) {
				data[i] = (1 - weight) * data[i] + weight * currentFrame[i];
			}

			return data;
		}

		public void reset() {
			frameCount = 0;
		}
	}

	public void toggleAccumulatorEnabled() {
		accumulatorEnabled = !accumulatorEnabled;
		if (accumulatorEnabled) {
			accumulator.reset();
		}
	}

}
