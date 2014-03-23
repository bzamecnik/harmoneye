package com.harmoneye.app.tuner;

import processing.core.PApplet;

import com.harmoneye.audio.Capture;

public class TunerApp extends PApplet {

	private static final int WINDOW_SIZE = 4096;
	private static final double SAMPLE_RATE = 44100;
	private static final int BITS_PER_SAMPLE = 16;
	private static final int INPUT_BUFFER_SIZE = 1024;
	private static final int FRAME_RATE = 30;

	private static final String renderer = P3D;
	// private static final String renderer =
	// "processing.core.PGraphicsRetina2D";

	private static final String[] TONE_NAMES = { "C", "Db", "D", "Eb", "E",
		"F", "Gb", "G", "Ab", "A", "Bb", "B" };

	private static final long serialVersionUID = -1188263388156753697L;

	public static void main(String args[]) {
		PApplet.main(TunerApp.class.getName(), args);
	}

	private Capture audioCapture;
	private ReassignedTuningAnalyzer tuningAnalyzer;

	private float[] toneSelectionWeights = new float[12];

	int currentTone = 0;
	float centerPitch = 5.5f;// currentTone;

	private boolean movementEnabled = false;

	public void setup() {
		size(1024, 480, renderer);
		// frameRate(FRAME_RATE);
		frame.setTitle("Tuner");
		colorMode(HSB, 1.0f);
		smooth();
		try {
			tuningAnalyzer = new ReassignedTuningAnalyzer(WINDOW_SIZE,
				SAMPLE_RATE);
			audioCapture = new Capture(tuningAnalyzer, (float) SAMPLE_RATE,
				BITS_PER_SAMPLE, INPUT_BUFFER_SIZE);

			tuningAnalyzer.start();
			audioCapture.start();
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
		// noLoop();
	}

	public void draw() {
		tuningAnalyzer.update();

		float incr = 1 / (0.25f * frameRate);
		for (int i = 0; i < toneSelectionWeights.length; i++) {
			if (tuningAnalyzer.isPitchDetected()
				&& (int) tuningAnalyzer.getNearestTone() == i) {
				toneSelectionWeights[i] = Math.min(1, toneSelectionWeights[i]
					+ incr);
			} else {
				toneSelectionWeights[i] = Math.max(0, toneSelectionWeights[i]
					- incr);
			}
		}

		if (movementEnabled) {
			double diff = phaseUnwrappedDiff(currentTone, centerPitch);
			float velocity = 0.1f * (float) diff;
			if (Math.abs(diff) > 0.01) {
				centerPitch += velocity;
			} else {
				centerPitch = currentTone;
			}
		}

		background(1.0f);

		// drawSingleTone();

		drawWholeOctave();
	}

	private void drawSingleTone() {
		// pitch curve

		double[] history = tuningAnalyzer.getErrorHistory();
		if (history == null) {
			return;
		}

		float margin = width / 12f;

		strokeWeight(1.5f);
		stroke(0.85f);
		float h = height - margin;
		line(width / 2, 0, width / 2, height);
		for (int i = 0; i < history.length - 1; i++) {
			double p1 = history[i] + 0.5;
			double p2 = history[i + 1] + 0.5;
			float x1 = (float) (p1 * width);
			float x2 = (float) (p2 * width);
			stroke(errorHue(p1 - 0.5), 0.75f, 0.75f);
			line(x1, height - i * h, x2, height - (i + 1) * h);
		}

	}

	private void drawWholeOctave() {
		float margin = width / 12f;

		boolean pitchDetected = tuningAnalyzer.isPitchDetected();

		for (int i = 0; i < toneSelectionWeights.length; i++) {
			if (toneSelectionWeights[i] > 0) {
				float x = (float) (lensify(mod(i - 0.5 + 6 - centerPitch) / 12.0) * width);
				float xSize = (float) ((lensify(mod(i + 0.5 + 6 - centerPitch) / 12.0) - lensify(mod(i
					- 0.5 + 6 - centerPitch) / 12.0)) * width);
				fill(0, 0, 1 - 0.05f * toneSelectionWeights[i]);
				rect(x, 0, xSize, height);
			}
		}

		// tone center lines
		for (int i = 0; i < 12; i++) {
			float pos = (float) lensify(mod(i + 6 - centerPitch) / 12.0);
			float x = pos * width;
			stroke(movementEnabled ? (0.5f + (float) Math.abs(pos - 0.5))
				: 0.85f);
			line(x, height, x, margin);
		}

		double[] pitchHistory = tuningAnalyzer.getPitchHistory();
		double[] errorHistory = tuningAnalyzer.getErrorHistory();

		if (pitchDetected) {
			currentTone = (int) tuningAnalyzer.getNearestTone();
		}

		// pitch curve
		{
			float h = height - margin;
			double maxSkip = 0.7 / 12;
			float yStep = 1.0f / pitchHistory.length;
			for (int i = 0; i < pitchHistory.length - 1; i++) {
				double p1 = mod(pitchHistory[i] - centerPitch + 6) / 12.0;
				double p2 = mod(pitchHistory[i + 1] - centerPitch + 6) / 12.0;
				if (p1 - p2 > maxSkip) {
					p1 = p2;
				} else if (p1 - p2 < -maxSkip) {
					p2 = p1;
				}
				float x1 = (float) lensify(p1) * width;
				float x2 = (float) lensify(p2) * width;

				double error = errorHistory[i];
				float weight = (i * yStep);
				stroke(errorHue(error),
					0.25f + 0.75f * weight,
					1 - 0.25f * weight);
				line(x1, height - i * h * yStep, x2, height - (i + 1) * h
					* yStep);
			}

		}

		// error indicator
		// {
		// double error = tuningAnalyzer.getDistToNearestTone();
		// fill(0.25f * (float) (1 - 2 * Math.abs(error)), 0.75f, 0.75f);
		// noStroke();
		// float x = width / 2;
		// float xSize = (float) (error * width);
		// rect(x, height - margin, xSize, 20);
		// }

		// tone names
		int textSize = 32;
		textSize(textSize);
		textAlign(CENTER, CENTER);
		ellipseMode(RADIUS);
		noStroke();
		for (int i = 0; i < 12; i++) {
			double pos = lensify(mod(i + 6 - centerPitch) / 12.0f);
			float x = (float) (pos * width);
			float y = width / (12 * 2.0f);
			boolean isSelectedTone = pitchDetected
				&& (int) tuningAnalyzer.getNearestTone() == i;
			if (isSelectedTone) {
				fill(errorHue(errorHistory[errorHistory.length - 1]),
					0.75f,
					0.75f);
			} else {
				fill(movementEnabled ? (0.5f + (float) Math.abs(pos - 0.5))
					: 0.75f);
			}

			text(TONE_NAMES[i], x, y);
			ellipse(x, margin - 3, 3, 3);
		}
	}

	private float errorHue(double error) {
		return 0.25f * (float) (1 - 2 * Math.abs(error));
	}

	int mod(int value) {
		return ((value % 12) + 12) % 12;
	}

	float mod(float value) {
		return ((value % 12) + 12) % 12;
	}

	double mod(double value) {
		return ((value % 12) + 12) % 12;
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

	double smoothstep(double x) {
		return (x * x * (3 - 2 * x));
	}

	double lensify(double x) {
		int smoothStepCount = 0;
		for (int i = 0; i < smoothStepCount; i++) {
			x = smoothstep(x);
		}
		return x;
	}

	@Override
	public void stop() {
		audioCapture.stop();
		tuningAnalyzer.stop();
	}
}
