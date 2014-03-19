package com.harmoneye.app.tuner;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;

import processing.core.PApplet;

import com.harmoneye.audio.Capture;

public class TunerApp extends PApplet {
	// private static final int FRAME_RATE = 30;

	private static final long serialVersionUID = -1188263388156753697L;

	public static void main(String args[]) {
		PApplet.main(TunerApp.class.getName(), args);
	}

	private int windowSize = 2048;

	private double sampleRate = 44100;
	private int bitsPerSample = 16;

	private int FRAME_RATE = (int) (sampleRate / windowSize);

	private Capture audioCapture;
	private ReassignedTuningAnalyzer tuningAnalyzer;

	public void setup() {
		try {
			prepareOptions();

			size(640, 480, JAVA2D);

			// spectrograph = new StreamingReassignedSpectrograph(windowSize,
			// overlapRatio, sampleRate);
			tuningAnalyzer = new ReassignedTuningAnalyzer(windowSize,
				sampleRate);
			audioCapture = new Capture(tuningAnalyzer, (float) sampleRate,
				bitsPerSample, windowSize);

			tuningAnalyzer.start();
			audioCapture.start();

			colorMode(HSB, 1.0f);
			smooth();

			frameRate(FRAME_RATE);
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
	}

	public void draw() {
		background(1.0f);

		// double pitch = tuningAnalyzer.getDetectedPitch();

		// float y = (float)(1-pitch) * height;
		// line(0, y, width, y);

		double[] spectrum = tuningAnalyzer.getSpectrum();
		if (spectrum == null) {
			return;
		}

		pushMatrix();

		scale(1, -1);
		translate(0, -height);

		float xScale = width / (float) spectrum.length;
		float yScale = height;

		double pitch = tuningAnalyzer.getPitch();
		boolean pitchDetected = pitch > 0;

		if (pitchDetected) {
			{
				float xSize = (float) (1 / 12.0 * width);
				float x = (float) ((tuningAnalyzer.getNearestTone() - 0.5) * xSize);
				fill(0, 0, 0.9f);
				rect(x, 0, xSize, height);
			}
		}

		// spectrum plot
		stroke(0.75f);
		for (int i = 1; i < spectrum.length; i++) {
			float prevValue = (float) spectrum[i - 1];
			float value = (float) spectrum[i];
			line((i - 1) * xScale, prevValue * yScale, i * xScale, value
				* yScale);
		}

		if (pitchDetected) {
			// nearest tone line
			stroke(0.25f, 1, 1);
			float nearestX = (float) (tuningAnalyzer.getNearestTone() / 12.0 * width);
			line(nearestX, 0, nearestX, height);

			// pitch line
			stroke(1, 1, 1);
			float pitchX = (float) (pitch / 12.0 * width);
			line(pitchX, 0, pitchX, height);
		}

		stroke(0.75f);
		for (int i = 1; i <= 12; i++) {
			float x = i / 12.0f * width;
			line(x, 0, x, height);
		}

		{
			double error = tuningAnalyzer.getDistToNearestTone();
			fill(0.25f * (float) (1 - 2 * Math.abs(error)), 0.75f, 0.75f);
			noStroke();
			float x = width / 2;
			float xSize = (float) (error * width);
			rect(x, height - 20, xSize, height);
		}

		popMatrix();
	}

	private void prepareOptions() throws Exception {
		Options options = new Options();
		options.addOption("w", "window-size", true, "Window size");
		options.addOption("l", "overlap-factor", true, "Overlap factor");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption("w")) {
			windowSize = Integer.valueOf(cmd.getOptionValue("w"));
		}
	}

	@Override
	public void stop() {
		audioCapture.stop();
		tuningAnalyzer.stop();
	}
}
