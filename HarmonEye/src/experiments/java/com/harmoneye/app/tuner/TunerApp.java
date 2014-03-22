package com.harmoneye.app.tuner;

import processing.core.PApplet;

import com.harmoneye.audio.Capture;

public class TunerApp extends PApplet {

	private static final int WINDOW_SIZE = 4096;
	private static final double SAMPLE_RATE = 44100;
	private static final int BITS_PER_SAMPLE = 16;
	private static final int INPUT_BUFFER_SIZE = 1024;
	private static final int FRAME_RATE = 30;

	// private static final String renderer = P2D;
	private static final String renderer = "processing.core.PGraphicsRetina2D";

	private static final String[] TONE_NAMES = { "C", "Db", "D", "Eb", "E",
		"F", "Gb", "G", "Ab", "A", "Bb", "B" };

	private static final long serialVersionUID = -1188263388156753697L;

	public static void main(String args[]) {
		PApplet.main(TunerApp.class.getName(), args);
	}

	private Capture audioCapture;
	private ReassignedTuningAnalyzer tuningAnalyzer;

	private float[] toneSelectionWeights = new float[12];

	public void setup() {
		size(1024, 480, renderer);
		frameRate(FRAME_RATE);
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

		background(1.0f);

		// drawSingleTone();

		drawWholeOctave();
	}

	private void drawSingleTone() {
		// double pitch = tuningAnalyzer.getPitch();
		// boolean pitchDetected = tuningAnalyzer.isPitchDetected();

		// pitch curve

		double[] history = tuningAnalyzer.getErrorHistory();
		if (history == null) {
			return;
		}
		stroke(0);
		for (int i = 0; i < history.length - 1; i++) {
			// double p1 = history[i] + 0.5;
			// double p2 = history[i + 1] + 0.5;
			// float x1 = (float) (p1 * width);
			// float x2 = (float) (p2 * width);
			// line(x1, i * height / history.length, x2, (i + 1) * height
			// / history.length);

			double p1 = history[i] + 0.5;
			float x1 = (float) (p1 * width);
			line(width / 2, i * height / history.length, x1, i * height
				/ history.length);
		}

	}

	private void drawWholeOctave() {
		// float margin = 64;// + 20;
		float margin = width / 12f;

		boolean pitchDetected = tuningAnalyzer.isPitchDetected();

		for (int i = 0; i < toneSelectionWeights.length; i++) {
			if (toneSelectionWeights[i] > 0) {
				float xSize = (float) (1 / 12.0 * width);
				float x = (float) ((i) * xSize);
				fill(0, 0, 1 - 0.05f * toneSelectionWeights[i]);
				rect(x, 0, xSize, height);
			}
		}

		// grid
		stroke(0.85f);
		// for (int i = 1; i <= 12; i++) {
		// float x = i / 12.0f * width;
		// line(x, 0, x, height);
		// }
		// tone center lines
		for (int i = 0; i < 12; i++) {
			float x = (i + 0.5f) / 12.0f * width;
			line(x, height, x, margin);
		}

		// double[] spectrum = tuningAnalyzer.getSpectrum();
		// if (spectrum == null) {
		// return;
		// }

		// line(0, height - margin, width, height - margin);
		// line(0, height - (margin - 20), width, height - (margin - 20));

		// spectrum plot
		// stroke(0.75f);
		// for (int i = 1; i < spectrum.length; i++) {
		// float prevValue = (float) spectrum[i - 1];
		// float value = (float) spectrum[i];
		// line((i - 1) * xScale, prevValue * yScale, i * xScale, value
		// * yScale);
		// }

		// if (pitchDetected) {
		// // nearest tone line
		// stroke(0.25f, 0.5f, 0.5f);
		// float nearestX = (float) (tuningAnalyzer.getNearestTone() / 12.0 *
		// width);
		// line(nearestX, 0, nearestX, height - margin);
		// //
		// // // pitch line
		// // stroke(1, 1, 1);
		// // float pitchX = (float) (pitch / 12.0 * width);
		// // line(pitchX, 0, pitchX, height);
		// }

		double[] pitchHistory = tuningAnalyzer.getPitchHistory();
		double[] errorHistory = tuningAnalyzer.getErrorHistory();

		// pitch curve
		{
			float h = height - margin;
			double maxSkip = 0.7;
			for (int i = 0; i < pitchHistory.length - 1; i++) {
				stroke(1 - ((pitchDetected ? 1 : 0.25f) * (i / (float) pitchHistory.length)));
				double p1 = pitchHistory[i];
				double p2 = pitchHistory[i + 1];
				float x1 = (float) (p1 / 12.0 * width);
				float x2 = (float) (p2 / 12.0 * width);
				if (p1 - p2 > maxSkip) {
					x1 = x2;
				} else if (p1 - p2 < -maxSkip) {
					x2 = x1;
				}
				double error = errorHistory[i];
				stroke(errorHue(error), 0.75f, 0.75f);
				line(x1, height - i * h / pitchHistory.length, x2, height
					- (i + 1) * h / pitchHistory.length);
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
			float x = (i + 0.5f) / 12.0f * width;
			float y = width / (12 * 2.0f);
			boolean isSelectedTone = pitchDetected
				&& (int) tuningAnalyzer.getNearestTone() == i;
			if (isSelectedTone) {
				fill(errorHue(errorHistory[errorHistory.length - 1]),
					0.75f,
					0.75f);
			} else {
				fill(0, 0, 0.75f);
			}

			text(TONE_NAMES[i], x, y);
			ellipse(x, margin - 3, 3, 3);
		}
	}

	private float errorHue(double error) {
		return 0.25f * (float) (1 - 2 * Math.abs(error));
	}

	@Override
	public void stop() {
		audioCapture.stop();
		tuningAnalyzer.stop();
	}
}
