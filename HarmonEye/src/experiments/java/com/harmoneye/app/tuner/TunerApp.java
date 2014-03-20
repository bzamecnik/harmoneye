package com.harmoneye.app.tuner;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;

import processing.core.PApplet;

import com.harmoneye.analysis.StreamingReassignedSpectrograph.OutputFrame;
import com.harmoneye.audio.Capture;

public class TunerApp extends PApplet {
	// private static final int FRAME_RATE = 30;

	private static final long serialVersionUID = -1188263388156753697L;

	private static final String[] TONE_NAMES = { "C", "Db", "D", "Eb", "E",
		"F", "Gb", "G", "Ab", "A", "Bb", "B" };

	public static void main(String args[]) {
		PApplet.main(TunerApp.class.getName(), args);
	}

	private int windowSize = 4096;

	private double sampleRate = 44100;
	private int bitsPerSample = 16;

	private int FRAME_RATE = (int) (sampleRate / windowSize);

	private Capture audioCapture;
	private ReassignedTuningAnalyzer tuningAnalyzer;

	public void setup() {
		frame.setTitle("Tuner");
		try {
			prepareOptions();

			size(1024, 480, JAVA2D);

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

		pushMatrix();

		textSize(32);
		textAlign(CENTER, CENTER);

		scale(1, -1);
		translate(0, -height);

		// drawSingleTone();

		// drawSecondDerivatives();

		drawWholeOctave();

		popMatrix();

		// tone names
		fill(0, 0, 0.75f);
		for (int i = 0; i < 12; i++) {
			text(TONE_NAMES[i], (i + 0.5f) / 12.0f * width, height - 32);
		}
	}

	private void drawSecondDerivatives() {
		OutputFrame outputFrame = tuningAnalyzer.getOutputFrame();
		if (outputFrame == null) {
			return;
		}
		double[] values = outputFrame.getSecondDerivatives();
		int length = 500;// values.length;
		for (int i = 0; i < length - 1; i++) {
			double v1 = values[i];
			double v2 = values[i + 1];
			// if(Math.abs(v1)>0.01||Math.abs(v2)>0.01){
			// continue;
			// }
			float y1 = (float) ((v1 * 0.25 + 0.5) * height);
			float y2 = (float) ((v2 * 0.25 + 0.5) * height);
			line(i * width / length, y1, (i + 1) * width / length, y2);
		}
	}

	private void drawSingleTone() {
		// double pitch = tuningAnalyzer.getPitch();
		// boolean pitchDetected = tuningAnalyzer.isPitchDetected();

		// pitch curve
		stroke(0);
		double[] history = tuningAnalyzer.getErrorHistory();
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
		double[] spectrum = tuningAnalyzer.getSpectrum();
		if (spectrum == null) {
			return;
		}

		float xScale = width / (float) spectrum.length;
		float yScale = height;

		double pitch = tuningAnalyzer.getPitch();
		boolean pitchDetected = tuningAnalyzer.isPitchDetected();

		float margin = 64+20;

		if (pitchDetected) {
			{
				float xSize = (float) (1 / 12.0 * width);
				float x = (float) ((tuningAnalyzer.getNearestTone() - 0.5) * xSize);
				fill(0, 0, 0.95f);
				rect(x, 0, xSize, height);
			}
		}
		
		// grid
		stroke(0.85f);
		for (int i = 1; i <= 12; i++) {
			float x = i / 12.0f * width;
			line(x, 0, x, height);
		}
		line(0, margin, width, margin);
		line(0, margin-20, width, margin-20);

		

		// spectrum plot
		// stroke(0.75f);
		// for (int i = 1; i < spectrum.length; i++) {
		// float prevValue = (float) spectrum[i - 1];
		// float value = (float) spectrum[i];
		// line((i - 1) * xScale, prevValue * yScale, i * xScale, value
		// * yScale);
		// }

		if (pitchDetected) {
			// nearest tone line
			stroke(0.25f, 0.5f, 0.5f);
			float nearestX = (float) (tuningAnalyzer.getNearestTone() / 12.0 * width);
			line(nearestX, margin, nearestX, height);
			//
			// // pitch line
			// stroke(1, 1, 1);
			// float pitchX = (float) (pitch / 12.0 * width);
			// line(pitchX, 0, pitchX, height);
		}

		// pitch curve
		{

			float h = height - margin;
			stroke(0);
			double[] pitchHistory = tuningAnalyzer.getPitchHistory();
			double maxSkip = 0.7;
			for (int i = 0; i < pitchHistory.length - 1; i++) {
				double p1 = pitchHistory[i];
				double p2 = pitchHistory[i + 1];
				float x1 = (float) (p1 / 12.0 * width);
				float x2 = (float) (p2 / 12.0 * width);
				if (p1 - p2 > maxSkip) {
					x1 = x2;
				} else if (p1 - p2 < -maxSkip) {
					x2 = x1;
				}
				line(x1, margin + i * h / pitchHistory.length, x2, margin
					+ (i + 1) * h / pitchHistory.length);
			}
		}

		// error indicator
		{
			double error = tuningAnalyzer.getDistToNearestTone();
			fill(0.25f * (float) (1 - 2 * Math.abs(error)), 0.75f, 0.75f);
			noStroke();
			float x = width / 2;
			float xSize = (float) (error * width);
			rect(x, 64, xSize, 20);
		}
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
