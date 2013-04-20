package com.harmoneye;

import java.awt.Graphics2D;

import javax.swing.JPanel;

import org.apache.commons.math3.complex.Complex;

import com.harmoneye.cqt.AbstractCqt.HarmonicPatternPitchClassDetector;
import com.harmoneye.cqt.FastCqt;
import com.harmoneye.util.DoubleCircularBuffer;

public class MusicAnalyzer implements SoundConsumer {

	private FastCqt cqt = new FastCqt();

	private final double DB_THRESHOLD = -(20 * Math.log10(2 << (16 - 1)));
	private final int BINS_PER_HALFTONE = cqt.getBinsPerHalftone();
	private final int PITCH_BIN_COUNT = cqt.getBinsPerOctave();
	// private final int OCTAVE_COUNT = cqt.getOctaveCount();

	// in samples
	private int signalBlockSize = cqt.getSignalBlockSize();

	private double[] amplitudes = new double[signalBlockSize];
	private Complex[] cqSpectrum;
	/** peak amplitude spectrum */
	private double[] amplitudeSpectrumDb;
	private double[] octaveBinsDb = new double[PITCH_BIN_COUNT];
	private double[] smoothedOctaveBinsDb = new double[PITCH_BIN_COUNT];
	// private double[] normalizedOctaveBinsDb = new double[PITCH_BIN_COUNT];
	private double[] pitchClassProfileDb = new double[12];
	// private double[] smoothedPitchClassProfileDb = new double[12];

	private ExpSmoother binSmoother = new ExpSmoother(PITCH_BIN_COUNT, 0.4);
	// private ExpSmoother pcpSmoother = new ExpSmoother(12, 0.1);
	// private ExpSmoother binMaxAvgSmoother = new ExpSmoother(2, 0.1);

	private MovingAverageAccumulator accumulator = new MovingAverageAccumulator(PITCH_BIN_COUNT);
	private boolean accumulatorEnabled = false;

	private HarmonicPatternPitchClassDetector pcDetector = cqt.new HarmonicPatternPitchClassDetector();
	private DoubleCircularBuffer amplitudeBuffer = new DoubleCircularBuffer(signalBlockSize);

	private AbstractVisualizer visualizer;

	public MusicAnalyzer(JPanel panel) {
		visualizer = new CircularVisualizer(panel);
		visualizer.setBinsPerHalftone(BINS_PER_HALFTONE);
		visualizer.setPitchBinCount(PITCH_BIN_COUNT);
	}

	@Override
	public void consume(double[] samples) {
		amplitudeBuffer.write(samples);
	}

	public void updateSignal() {
		amplitudeBuffer.readLast(amplitudes, amplitudes.length);
		// long start = System.nanoTime();
		computeAmplitudeSpectrum(amplitudes);
		computePitchClassProfile();
		visualizer.setPitchClassProfile(pitchClassProfileDb);
		// long stop = System.nanoTime();
		// System.out.println("update: " + (stop - start) / 1000000.0);
	}

	private void computeAmplitudeSpectrum(double[] signal) {
		cqSpectrum = cqt.transform(signal);
		if (amplitudeSpectrumDb == null) {
			amplitudeSpectrumDb = new double[cqSpectrum.length];
		}
		for (int i = 0; i < amplitudeSpectrumDb.length; i++) {
			double amplitude = cqSpectrum[i].abs();
			double referenceAmplitude = 1;
			double amplitudeDb = 20 * Math.log10(amplitude / referenceAmplitude);
			if (amplitudeDb < DB_THRESHOLD) {
				amplitudeDb = DB_THRESHOLD;
			}
			double scaledAmplitudeDb = 1 + amplitudeDb / -DB_THRESHOLD;
			amplitudeSpectrumDb[i] = scaledAmplitudeDb;
		}
	}

	private void computePitchClassProfile() {
		// double octaveCountInv = 1.0 / OCTAVE_COUNT;
		for (int i = 0; i < PITCH_BIN_COUNT; i++) {
			// average over octaves:
			// double value = 0;
			// for (int j = i; j < amplitudeSpectrumDb.length; j +=
			// PITCH_BIN_COUNT) {
			// value += amplitudeSpectrumDb[j];
			// }
			// value *= octaveCountInv;

			// maximum over octaves:
			double value = 0;
			for (int j = i; j < amplitudeSpectrumDb.length; j += PITCH_BIN_COUNT) {
				value = Math.max(value, amplitudeSpectrumDb[j]);
			}

			// double value = 1;
			// for (int j = i; j < amplitudeSpectrumDb.length; j +=
			// PITCH_BIN_COUNT) {
			// value *= amplitudeSpectrumDb[j];
			// }
			// value = Math.pow(value, octaveCountInv);

			// double value = amplitudeSpectrumDb[i + 0 * PITCH_BIN_COUNT];

			octaveBinsDb[i] = value;
		}

		double[] pitchClassBinsDb = pcDetector.detectPitchClasses(amplitudeSpectrumDb);
		double max = 0;
		for (int i = 0; i < amplitudeSpectrumDb.length; i++) {
			max = Math.max(max, amplitudeSpectrumDb[i]);
		}
		// just an ad hoc reduction of noise
		for (int i = 0; i < pitchClassBinsDb.length; i++) {
			pitchClassBinsDb[i] = Math.pow(pitchClassBinsDb[i], 3);
		}
		for (int i = 0; i < pitchClassBinsDb.length; i++) {
			pitchClassBinsDb[i] *= max;
		}
		for (int i = 0; i < octaveBinsDb.length; i++) {
			octaveBinsDb[i] *= pitchClassBinsDb[i];
		}
		// for (int i = 0; i < octaveBinsDb.length; i++) {
		// octaveBinsDb[i] = pitchClassBinsDb[i];
		// }
		for (int i = 0; i < octaveBinsDb.length; i++) {
			octaveBinsDb[i] = Math.pow(octaveBinsDb[i], 1 / 3.0);
		}

		// double avg = 0;
		// for (int i = 0; i < octaveBinsDb.length; i++) {
		// avg += octaveBinsDb[i];
		// }
		// avg /= octaveBinsDb.length;
		// for (int i = 0; i < octaveBinsDb.length; i++) {
		// octaveBinsDb[i] = octaveBinsDb[i] > 1.75 * avg ? octaveBinsDb[i]
		// : 0.25 * octaveBinsDb[i];
		// }

		// octaveBinsDb = amplitudeSpectrumDb;
		// pitchClassProfileDb = octaveBinsDb;
		// smoothedOctaveBinsDb = binSmoother.smooth(octaveBinsDb);
		// smoothedOctaveBinsDb = octaveBinsDb;

		// double max = 0;
		// double average = 0;
		// for (int i = 0; i < octaveBinsDb.length; i++) {
		// max = Math.max(max, octaveBinsDb[i]);
		// average += octaveBinsDb[i];
		// }
		// average /= octaveBinsDb.length;
		// for (int pitchClass = 0; pitchClass < 12; pitchClass++) {
		// double pitchClassAverage = 0;
		// for (int i = 0; i < BINS_PER_HALFTONE; i++) {
		// pitchClassAverage += octaveBinsDb[BINS_PER_HALFTONE * pitchClass
		// + i];
		// }
		// pitchClassAverage /= BINS_PER_HALFTONE;
		// if (pitchClassAverage <= average) {
		// for (int i = 0; i < BINS_PER_HALFTONE; i++) {
		// octaveBinsDb[BINS_PER_HALFTONE * pitchClass + i] = 0;
		// }
		// }
		// }
		// double[] result = binMaxAvgSmoother.smooth(new double[]{max,
		// average});
		// max = result[0];
		// average = result[1];
		// double normalizationFactor = Math.abs(max - average) > 1e-6 ? 1 /
		// Math.abs(max - average) : 1;
		// double normalizationFactor = max > 1e-6 ? 1 / max : 1;
		// double normalizationFactor = 1;
		// for (int i = 0; i < octaveBinsDb.length; i++) {
		// normalizedOctaveBinsDb[i] = (octaveBinsDb[i] - average) *
		// normalizationFactor;
		// }

		// smoothedOctaveBinsDb =
		// binSmoother.smooth(normalizedOctaveBinsDb);
		//smoothedOctaveBinsDb = binSmoother.smooth(octaveBinsDb);
		// smoothedOctaveBinsDb = normalizedOctaveBinsDb;
		//
		//pitchClassProfileDb = smoothedOctaveBinsDb;

		if (accumulatorEnabled) {
			accumulator.add(octaveBinsDb);
			pitchClassProfileDb = accumulator.getAverage();
		} else {
			binSmoother.smooth(octaveBinsDb);
			pitchClassProfileDb = binSmoother.smooth(octaveBinsDb);
		}

		// for (int pitchClass = 0; pitchClass < pitchClassProfileDb.length;
		// pitchClass++) {
		// double value = 0;
		// for (int i = 0; i < BINS_PER_HALFTONE; i++) {
		// value = Math.max(value, octaveBinsDb[BINS_PER_HALFTONE *
		// pitchClass + i]);
		// }
		// pitchClassProfileDb[pitchClass] = value;
		// }
		// smoothedPitchClassProfileDb =
		// pcpSmoother.smooth(pitchClassProfileDb);
		// pitchClassProfileDb = smoothedPitchClassProfileDb;

		// TODO: smooth the data, eg. with a Kalman filter

		// for (int i = 0, pitchClass = 0; i < octaveBinsDb.length; i +=
		// BINS_PER_HALFTONE, pitchClass++) {
		// double value = 0;
		//
		// double center = octaveBinsDb[i + 2];
		// if (center >= octaveBinsDb[i] &&
		// center >= octaveBinsDb[i + 1] &&
		// center >= octaveBinsDb[i + 3] &&
		// center >= octaveBinsDb[i + 4])
		// {
		// value = center;
		// }
		// // double center = octaveBinsDb[i + 1];
		// // if (center >= octaveBinsDb[i] &&
		// // center >= octaveBinsDb[i + 2])
		// // {
		// // value = center;
		// // }
		// pitchClassProfileDb[pitchClass] = value;
		// }
	}

	public int getSignalBlockSize() {
		return signalBlockSize;
	}

	// TODO: should not be exposed
	AbstractVisualizer getVisualizer() {
		return visualizer;
	}

	public void paint(Graphics2D graphics) {
		// long start = System.nanoTime();

		visualizer.paint(graphics);

		// long stop = System.nanoTime();
		// System.out.println("update: " + (stop - start) / 1000000.0);
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
