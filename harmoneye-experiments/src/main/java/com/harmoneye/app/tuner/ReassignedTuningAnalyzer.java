package com.harmoneye.app.tuner;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.util.FastMath;

import com.harmoneye.analysis.ScalarExpSmoother;
import com.harmoneye.analysis.StreamingReassignedSpectrograph;
import com.harmoneye.analysis.StreamingReassignedSpectrograph.OutputFrame;
import com.harmoneye.audio.DoubleRingBuffer;
import com.harmoneye.audio.SoundConsumer;
import com.harmoneye.math.MaxNorm;
import com.harmoneye.math.Modulo;
import com.harmoneye.math.Norm;

public class ReassignedTuningAnalyzer implements SoundConsumer {

	int overlapFactor = 8;
	private double historyInSecs = 3.0;

	private DoubleRingBuffer samplesRingBuffer;
	private StreamingReassignedSpectrograph spectrograph;
	private Norm maxNorm = new MaxNorm();

	private double sampleRate;
	private int windowSize;
	private int hopSize;
	private int historySize;

	private boolean pitchDetected;
	private double pitch;
	private double neareastTone;
	private double distToNearestTone;
	private OutputFrame outputFrame;
	private double[] wrappedChromagram;
	private double[] pitchHistory;
	private double[] errorHistory;
	private double[] sampleWindow;
	private double frequency;

	private ScalarExpSmoother errorSmoother = new ScalarExpSmoother(0.1);
	private double smoothedDistToNearestTone;

	public ReassignedTuningAnalyzer(int windowSize, double sampleRate) {
		this.windowSize = windowSize;
		this.sampleRate = sampleRate;
		spectrograph = new StreamingReassignedSpectrograph(windowSize,
			sampleRate);
		samplesRingBuffer = new DoubleRingBuffer(4 * windowSize);
		sampleWindow = new double[windowSize];

		hopSize = windowSize / overlapFactor;

		historySize = (int) (historyInSecs * sampleRate / hopSize);

		pitchHistory = new double[historySize];
		errorHistory = new double[historySize];
	}

	@Override
	public void consume(double[] samples) {
		samplesRingBuffer.write(samples);
	}

	public void update() {
//		StopWatch sw = new StopWatch();
//		sw.start();
//		int updatedFrames = 0;
		while (samplesRingBuffer.getCapacityForRead() > windowSize) {
			samplesRingBuffer.read(windowSize, sampleWindow);
			samplesRingBuffer.incrementReadIndex(hopSize);

			if (maxNorm.norm(sampleWindow) < 1e-4) {
				continue;
			}

			outputFrame = spectrograph.computeChromagram(sampleWindow);
			wrappedChromagram = outputFrame.getWrappedChromagram();
			double[] chromagram = outputFrame.getChromagram();
			findPitch(chromagram);
			if (pitchDetected) {
				shift(pitchHistory);
				pitchHistory[pitchHistory.length - 1] = pitch;
				shift(errorHistory);
				errorHistory[errorHistory.length - 1] = distToNearestTone;
			}
//			updatedFrames++;
		}
//		sw.stop();
//		System.out.println("updated " + updatedFrames + " frames in "
//			+ (sw.getNanoTime() * 1e-6) + " ms");
	}

	private void shift(double[] values) {
		System.arraycopy(values, 1, values, 0, values.length - 1);
	}

	private void findPitch(double[] chromagram) {
		pitchDetected = false;
		pitch = 0;
		neareastTone = 0;
		distToNearestTone = 0;
		int maxChromaBin = findMaxBin(chromagram);
		if (maxChromaBin == 0) {
			return;
		}
		// too quiet
		if (chromagram[maxChromaBin] < 0.01) {
			return;
		}
		double binFreq = spectrograph.frequencyForMusicalBin(maxChromaBin);
		int linearBin = (int) FastMath.floor(spectrograph
			.linearBinByFrequency(binFreq));
		double secondD = outputFrame.getSecondDerivatives()[linearBin];
		if (Math.abs(secondD) > 0.1) {
			return;
		}
		double[] instantFrequencies = outputFrame.getFrequencies();
		double preciseFreq = instantFrequencies[linearBin];
		if (preciseFreq <= 0) {
			return;
		}
		// System.out.println(preciseFreq*sampleRate);
		frequency = preciseFreq * sampleRate;
		pitch = spectrograph.wrapMusicalBin(spectrograph
			.musicalBinByFrequency(preciseFreq))
			/ wrappedChromagram.length
			* 12;
		neareastTone = Modulo.modulo(Math.round(pitch), 12);
		distToNearestTone = phaseUnwrappedDiff(pitch, neareastTone);
		smoothedDistToNearestTone = errorSmoother.smooth(distToNearestTone);
		pitchDetected = true;
	}

	private int findMaxBin(double[] values) {
		double max = maxNorm.norm(values);
		if (max > 1e-6) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] == max) {
					return i;
				}
			}
		}
		return 0;
	}

	double phaseUnwrappedDiff(double u, double v) {
		double diff = u - v;
		// simple phase unwrapping
		if (diff > 6 && u > v) {
			diff -= 12;
		} else if (diff < -6 && u < v) {
			diff += 12;
		}
		return diff;
	}

	public void stop() {
		// TODO Auto-generated method stub

	}

	public void start() {
		// TODO Auto-generated method stub

	}

	public double getPitch() {
		return pitch;
	}

	public double getNearestTone() {
		return neareastTone;
	}

	public double getDistToNearestTone() {
		return distToNearestTone;
	}

	public double getSmoothedDistToNearestTone() {
		return smoothedDistToNearestTone;
	}

	public double[] getSpectrum() {
		return wrappedChromagram;
	}

	public double[] getPitchHistory() {
		return pitchHistory;
	}

	public double[] getErrorHistory() {
		return errorHistory;
	}

	public boolean isPitchDetected() {
		return pitchDetected;
	}

	public OutputFrame getOutputFrame() {
		return outputFrame;
	}

	public double[] getSamples() {
		return sampleWindow;
	}

	public double getFrequency() {
		return frequency;
	}
}