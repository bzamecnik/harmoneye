package com.harmoneye.app.tuner;

import org.apache.commons.math3.util.FastMath;

import com.harmoneye.analysis.StreamingReassignedSpectrograph;
import com.harmoneye.analysis.StreamingReassignedSpectrograph.OutputFrame;
import com.harmoneye.audio.SoundConsumer;
import com.harmoneye.math.MaxNorm;
import com.harmoneye.math.Modulo;
import com.harmoneye.math.Norm;

// TODO:
// - use a ring buffer and process the samples not in consume()
//   (allow arbitrary size of the input buffer)
// - return a frame of features
// - decorate this analyzer with a history (a ring buffer or a list)

public class ReassignedTuningAnalyzer implements SoundConsumer {

	private StreamingReassignedSpectrograph spectrograph;

	private Norm maxNorm = new MaxNorm();

	private double pitch;

	private OutputFrame outputFrame;

	private double[] wrappedChromagram;

	private double neareastTone;

	private double distToNeareastTone;

	private int historySize = 100;

	private double[] pitchHistory = new double[historySize];
	private double[] errorHistory = new double[historySize];

	private boolean pitchDetected;

	private double sampleRate;

	private double[] samples;

	public ReassignedTuningAnalyzer(int windowSize, double sampleRate) {
		this.sampleRate = sampleRate;
		spectrograph = new StreamingReassignedSpectrograph(windowSize,
			sampleRate);
	}

	@Override
	public void consume(double[] samples) {
		this.samples = samples;
		outputFrame = spectrograph.computeChromagram(samples);
		wrappedChromagram = outputFrame.getWrappedChromagram();
		double[] chromagram = outputFrame.getChromagram();
		findPitch(chromagram);
		if (pitchDetected) {
			shift(pitchHistory);
			pitchHistory[pitchHistory.length - 1] = pitch;
			shift(errorHistory);
			errorHistory[errorHistory.length - 1] = distToNeareastTone;
		}
	}

	private void shift(double[] values) {
		System.arraycopy(values, 1, values, 0, values.length - 1);
	}

	private void findPitch(double[] chromagram) {
		pitchDetected = false;
		pitch = 0;
		neareastTone = 0;
		distToNeareastTone = 0;
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
		pitch = spectrograph.wrapMusicalBin(spectrograph
			.musicalBinByFrequency(preciseFreq))
			/ wrappedChromagram.length
			* 12;
		neareastTone = Modulo.modulo(Math.round(pitch - 0.5), 12) + 0.5;
		distToNeareastTone = pitch - neareastTone;
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
		return distToNeareastTone;
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
		return samples;
	}
}
